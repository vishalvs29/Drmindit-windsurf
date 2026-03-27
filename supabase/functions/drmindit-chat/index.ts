import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { cors } from "https://deno.land/std@0.168.0/http/cors.ts";

// CORS headers for frontend
const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, content-type",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
  "Access-Control-Max-Age": "86400",
};

// Rate limiting storage (in production, use Redis or similar)
const rateLimitStore = new Map<string, { count: number; resetTime: number }>();

// Rate limiting middleware
function checkRateLimit(clientId: string, limit: number = 20, windowMs: number = 60000): boolean {
  const now = Date.now();
  const clientData = rateLimitStore.get(clientId);
  
  if (!clientData || now > clientData.resetTime) {
    rateLimitStore.set(clientId, { count: 1, resetTime: now + windowMs });
    return true;
  }
  
  if (clientData.count >= limit) {
    return false;
  }
  
  rateLimitStore.set(clientId, { count: clientData.count + 1, resetTime: clientData.resetTime });
  return true;
}

// Get client ID for rate limiting
function getClientId(req: Request): string {
  const forwarded = req.headers.get("x-forwarded-for");
  const ip = forwarded ? forwarded.split(",")[0].trim() : "127.0.0.1";
  const userAgent = req.headers.get("user-agent") || "unknown";
  return `${ip}_${userAgent}`;
}

// Validate request
function validateRequest(req: Request, body: any): { isValid: boolean; error?: string } {
  if (!body || typeof body.message !== "string") {
    return { isValid: false, error: "Message is required and must be a string" };
  }
  
  if (body.message.length < 1 || body.message.length > 1000) {
    return { isValid: false, error: "Message must be between 1 and 1000 characters" };
  }
  
  if (body.userId && typeof body.userId !== "string") {
    return { isValid: false, error: "User ID must be a string" };
  }
  
  return { isValid: true };
}

// Sanitize input
function sanitizeInput(input: string): string {
  return input
    .trim()
    .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*<\/script>/gi, "")
    .replace(/javascript:/gi, "")
    .substring(0, 1000);
}

// Call OpenAI API securely
async function callOpenAI(prompt: string): Promise<{ content: string; usage?: any; error?: string }> {
  const apiKey = Deno.env.get("OPENAI_API_KEY");
  
  if (!apiKey) {
    console.error("OpenAI API key not configured");
    return { content: "", error: "AI service not available" };
  }
  
  try {
    const response = await fetch("https://api.openai.com/v1/chat/completions", {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${apiKey}`,
        "Content-Type": "application/json",
        "User-Agent": "DrMindit-Secure-Proxy/1.0"
      },
      body: JSON.stringify({
        model: Deno.env.get("OPENAI_MODEL") || "gpt-4",
        messages: [
          {
            role: "system",
            content: `You are DrMindit, a compassionate AI mental health companion. Your role is to provide supportive, empathetic, and safe mental health guidance.

Safety Guidelines:
1. Always prioritize user safety and wellbeing
2. Never provide harmful or dangerous advice
3. Encourage professional help when needed
4. Maintain supportive, non-judgmental tone
5. If user expresses crisis, immediately provide resources
6. Respect privacy and boundaries

Current Context:
- User message: "${prompt}"
- Time: ${new Date().toISOString()}
- Platform: DrMindit Mobile App

Please respond to the user's message with:
- Empathy and understanding
- Relevant mental health insights
- Practical coping strategies
- Encouragement and support
- Questions to deepen understanding when appropriate
- If crisis is detected, prioritize safety and immediate help

Keep responses concise (2-3 paragraphs max) but thorough. Always maintain a hopeful, supportive tone.`
          },
          {
            role: "user",
            content: prompt
          }
        ],
        max_tokens: parseInt(Deno.env.get("OPENAI_MAX_TOKENS") || "150"),
        temperature: parseFloat(Deno.env.get("OPENAI_TEMPERATURE") || "0.7"),
        stream: false
      })
    });
    
    if (!response.ok) {
      const errorText = await response.text();
      console.error("OpenAI API error:", errorText);
      return { content: "", error: "AI service temporarily unavailable" };
    }
    
    const data = await response.json();
    return {
      content: data.choices[0]?.message?.content || "",
      usage: data.usage
    };
    
  } catch (error) {
    console.error("OpenAI API call failed:", error);
    return { content: "", error: "AI service temporarily unavailable" };
  }
}

// Main chat handler
async function handleChatRequest(req: Request): Promise<Response> {
  // Handle CORS preflight
  if (req.method === "OPTIONS") {
    return new Response(null, { headers: corsHeaders });
  }
  
  // Only allow POST requests
  if (req.method !== "POST") {
    return new Response(JSON.stringify({ error: "Method not allowed" }), {
      status: 405,
      headers: corsHeaders
    });
  }
  
  try {
    const body = await req.json();
    const validation = validateRequest(req, body);
    
    if (!validation.isValid) {
      return new Response(JSON.stringify({ 
        error: validation.error,
        code: "VALIDATION_ERROR"
      }), {
        status: 400,
        headers: corsHeaders
      });
    }
    
    // Rate limiting
    const clientId = getClientId(req);
    if (!checkRateLimit(clientId)) {
      return new Response(JSON.stringify({
        error: "Too many requests. Please try again later.",
        code: "RATE_LIMIT_EXCEEDED",
        resetTime: new Date(Date.now() + 60000).toISOString()
      }), {
        status: 429,
        headers: {
          ...corsHeaders,
          "Retry-After": "60"
        }
      });
    }
    
    // Sanitize input
    const sanitizedMessage = sanitizeInput(body.message);
    
    // Call OpenAI API
    const aiResponse = await callOpenAI(sanitizedMessage);
    
    if (aiResponse.error) {
      return new Response(JSON.stringify({
        error: aiResponse.error,
        code: "AI_SERVICE_ERROR"
      }), {
        status: 503,
        headers: corsHeaders
      });
    }
    
    // Log request (without sensitive data)
    console.log("Chat request processed", {
      timestamp: new Date().toISOString(),
      clientId: clientId.substring(0, 8) + "...", // Partial IP for privacy
      messageLength: sanitizedMessage.length,
      hasUsage: !!aiResponse.usage
    });
    
    return new Response(JSON.stringify({
      success: true,
      response: aiResponse.content,
      usage: aiResponse.usage,
      timestamp: new Date().toISOString(),
      sessionId: body.sessionId || null
    }), {
      headers: corsHeaders
    });
    
  } catch (error) {
    console.error("Chat request failed:", error);
    return new Response(JSON.stringify({
      error: "Internal server error",
      code: "INTERNAL_ERROR"
    }), {
      status: 500,
      headers: corsHeaders
    });
  }
}

// Health check endpoint
async function handleHealthCheck(): Promise<Response> {
  const apiKey = Deno.env.get("OPENAI_API_KEY");
  const supabaseUrl = Deno.env.get("SUPABASE_URL");
  
  return new Response(JSON.stringify({
    status: "healthy",
    timestamp: new Date().toISOString(),
    uptime: performance.now(),
    services: {
      openai: !!apiKey,
      supabase: !!supabaseUrl,
      rateLimiting: true
    },
    version: "1.0.0"
  }), {
    headers: corsHeaders
  });
}

// Rate limit status endpoint
async function handleRateLimitStatus(): Promise<Response> {
  const clientId = getClientId(new Request("http://localhost"));
  const clientData = rateLimitStore.get(clientId);
  
  return new Response(JSON.stringify({
    windowMs: 60000,
    maxRequests: 20,
    enabled: true,
    currentRequests: clientData?.count || 0,
    resetTime: clientData?.resetTime || new Date(Date.now() + 60000).toISOString()
  }), {
    headers: corsHeaders
  });
}

// Main server
serve(async (req) => {
  const url = new URL(req.url);
  const path = url.pathname;
  
  // Route requests
  switch (path) {
    case "/api/chat":
      return await handleChatRequest(req);
    
    case "/health":
      return await handleHealthCheck();
    
    case "/api/rate-limit/status":
      return await handleRateLimitStatus();
    
    default:
      return new Response(JSON.stringify({
        error: "Endpoint not found",
        availableEndpoints: [
          "POST /api/chat - Main chat endpoint",
          "GET /health - Health check",
          "GET /api/rate-limit/status - Rate limit status"
        ]
      }), {
        status: 404,
        headers: corsHeaders
      });
  }
});
