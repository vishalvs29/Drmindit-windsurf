/**
 * DrMindit Secure Chat Proxy
 * 
 * SECURITY CRITICAL:
 * - All API keys stored in environment variables ONLY
 * - No API keys in source code
 * - Rate limiting prevents abuse
 * - Input validation and sanitization
 * - Secure error handling without data leakage
 */

require('dotenv').config();

const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
const { body, validationResult } = require('express-validator');
const axios = require('axios');
const winston = require('winston');

// Security: Validate required environment variables
const requiredEnvVars = ['OPENAI_API_KEY'];
const missingEnvVars = requiredEnvVars.filter(envVar => !process.env[envVar]);

if (missingEnvVars.length > 0) {
    console.error('CRITICAL SECURITY ERROR: Missing required environment variables:', missingEnvVars);
    console.error('Server cannot start safely. Please check your .env configuration.');
    process.exit(1);
}

// Initialize Express app
const app = express();
const PORT = process.env.PORT || 3001;

// Security middleware
app.use(helmet({
    contentSecurityPolicy: {
        directives: {
            defaultSrc: ["'self'"],
            styleSrc: ["'self'", "'unsafe-inline'"],
            scriptSrc: ["'self'"],
            imgSrc: ["'self'", "data:", "https:"]
        }
    },
    hsts: {
        maxAge: 31536000,
        includeSubDomains: true,
        preload: true
    },
    noCache: true,
    frameguard: { action: 'deny' }
}));

// CORS configuration
const corsOptions = {
    origin: process.env.NODE_ENV === 'production' 
        ? (process.env.CORS_ORIGIN || 'https://yourdomain.com')
        : (process.env.CORS_ORIGIN_DEV || 'http://localhost:3000'),
    credentials: true,
    optionsSuccessStatus: 200
};

app.use(cors(corsOptions));

// Body parsing middleware
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

// Request logging middleware
const logger = winston.createLogger({
    level: process.env.LOG_LEVEL || 'info',
    format: winston.format.combine(
        winston.format.timestamp(),
        winston.format.json()
    ),
    defaultMeta: { service: 'drmindit-proxy' },
    transports: [
        new winston.transports.File({ 
            filename: process.env.LOG_FILE || 'logs/app.log',
            maxsize: process.env.LOG_MAX_SIZE || '10m',
            maxFiles: parseInt(process.env.LOG_MAX_FILES) || 5
        }),
        new winston.transports.Console({
            format: winston.format.simple()
        })
    ]
});

app.use((req, res, next) => {
    logger.info('Request received', {
        method: req.method,
        url: req.url,
        ip: req.ip,
        userAgent: req.get('User-Agent'),
        timestamp: new Date().toISOString()
    });
    next();
});

// Rate limiting configuration
const rateLimitOptions = {
    windowMs: parseInt(process.env.RATE_LIMIT_WINDOW_MS) || 60000, // 1 minute
    max: parseInt(process.env.RATE_LIMIT_MAX_REQUESTS) || 20, // 20 requests per minute
    message: {
        error: 'Too many requests from this IP, please try again later.',
        code: 'RATE_LIMIT_EXCEEDED'
    },
    standardHeaders: true,
    legacyHeaders: false,
    skipSuccessfulRequests: process.env.RATE_LIMIT_SKIP_SUCCESSFUL === 'true'
};

if (process.env.ENABLE_RATE_LIMITING !== 'false') {
    const limiter = rateLimit(rateLimitOptions);
    app.use('/api/', limiter);
}

// Input validation schemas
const chatValidation = [
    body('message').trim().isLength({ min: 1, max: 1000 }).withMessage('Message must be between 1 and 1000 characters'),
    body('userId').optional().isUUID().withMessage('Valid user ID required'),
    body('sessionId').optional().isUUID().withMessage('Session ID must be valid UUID')
];

// Helper functions
function getClientId(req) {
    const forwarded = req.get('X-Forwarded-For');
    const ip = forwarded ? forwarded.split(',')[0].trim() : req.ip;
    const userAgent = req.get('User-Agent') || 'unknown';
    return `${ip}_${userAgent}`;
}

function sanitizeInput(input) {
    return input
        .trim()
        .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*<\/script>/gi, '')
        .replace(/javascript:/gi, '')
        .substring(0, 1000);
}

// Call OpenAI API securely
async function callOpenAI(prompt) {
    const apiKey = process.env.OPENAI_API_KEY;
    
    if (!apiKey) {
        logger.error('OpenAI API key not configured');
        return { content: '', error: 'AI service not available' };
    }
    
    try {
        const response = await axios.post('https://api.openai.com/v1/chat/completions', {
            model: process.env.OPENAI_MODEL || 'gpt-4',
            messages: [
                {
                    role: 'system',
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
                    role: 'user',
                    content: prompt
                }
            ],
            max_tokens: parseInt(process.env.OPENAI_MAX_TOKENS) || 150,
            temperature: parseFloat(process.env.OPENAI_TEMPERATURE) || 0.7,
            stream: false
        }, {
            headers: {
                'Authorization': `Bearer ${apiKey}`,
                'Content-Type': 'application/json',
                'User-Agent': 'DrMindit-Secure-Proxy/1.0'
            },
            timeout: parseInt(process.env.API_TIMEOUT) || 30000
        });
        
        return {
            content: response.data.choices[0]?.message?.content || '',
            usage: response.data.usage
        };
        
    } catch (error) {
        logger.error('OpenAI API call failed', {
            error: error.message,
            stack: error.stack,
            response: error.response?.data
        });
        return { content: '', error: 'AI service temporarily unavailable' };
    }
}

// API Routes

// Health check endpoint
app.get('/health', (req, res) => {
    const apiKey = process.env.OPENAI_API_KEY;
    const supabaseUrl = process.env.SUPABASE_URL;
    
    res.json({
        status: 'healthy',
        timestamp: new Date().toISOString(),
        uptime: process.uptime(),
        services: {
            openai: !!apiKey,
            supabase: !!supabaseUrl,
            rateLimiting: process.env.ENABLE_RATE_LIMITING !== 'false'
        },
        version: '1.0.0'
    });
});

// Rate limit status endpoint
app.get('/api/rate-limit/status', (req, res) => {
    const clientId = getClientId(req);
    
    res.json({
        windowMs: rateLimitOptions.windowMs,
        maxRequests: rateLimitOptions.max,
        enabled: process.env.ENABLE_RATE_LIMITING !== 'false',
        currentRequests: 'N/A', // Would need Redis or similar for accurate tracking
        resetTime: new Date(Date.now() + rateLimitOptions.windowMs).toISOString()
    });
});

// Main chat endpoint
app.post('/api/chat', chatValidation, async (req, res) => {
    try {
        // Check for validation errors
        const errors = validationResult(req);
        if (!errors.isEmpty()) {
            logger.warn('Validation failed', { 
                errors: errors.array(),
                ip: req.ip,
                timestamp: new Date().toISOString()
            });
            return res.status(400).json({
                error: 'Validation failed',
                details: errors.array(),
                code: 'VALIDATION_ERROR'
            });
        }
        
        // Sanitize input
        const sanitizedMessage = sanitizeInput(req.body.message);
        
        // Log request (without sensitive data)
        logger.info('Chat request processed', {
            timestamp: new Date().toISOString(),
            clientId: getClientId(req).substring(0, 8) + '...', // Partial IP for privacy
            messageLength: sanitizedMessage.length,
            userId: req.body.userId ? 'present' : 'absent'
        });
        
        // Call OpenAI API
        const aiResponse = await callOpenAI(sanitizedMessage);
        
        if (aiResponse.error) {
            return res.status(503).json({
                error: aiResponse.error,
                code: 'AI_SERVICE_ERROR'
            });
        }
        
        // Log response (without sensitive content)
        logger.info('Chat response generated', {
            timestamp: new Date().toISOString(),
            clientId: getClientId(req).substring(0, 8) + '...',
            hasUsage: !!aiResponse.usage,
            responseLength: aiResponse.content.length
        });
        
        res.json({
            success: true,
            response: aiResponse.content,
            usage: aiResponse.usage,
            timestamp: new Date().toISOString(),
            sessionId: req.body.sessionId || null
        });
        
    } catch (error) {
        logger.error('Chat request failed', {
            error: error.message,
            stack: error.stack,
            ip: req.ip,
            timestamp: new Date().toISOString()
        });
        
        res.status(500).json({
            error: 'Internal server error',
            code: 'INTERNAL_ERROR'
        });
    }
});

// 404 handler
app.use((req, res) => {
    logger.warn('404 Not Found', { 
        url: req.url,
        method: req.method,
        ip: req.ip,
        timestamp: new Date().toISOString()
    });
    
    res.status(404).json({
        error: 'Endpoint not found',
        availableEndpoints: [
            'POST /api/chat - Main chat endpoint',
            'GET /health - Health check',
            'GET /api/rate-limit/status - Rate limit status'
        ]
    });
});

// Error handling middleware
app.use((error, req, res, next) => {
    logger.error('Unhandled error', {
        error: error.message,
        stack: error.stack,
        url: req.url,
        method: req.method,
        ip: req.ip,
        timestamp: new Date().toISOString()
    });
    
    res.status(500).json({
        error: process.env.NODE_ENV === 'production' 
            ? 'Something went wrong' 
            : error.message,
        code: 'INTERNAL_ERROR'
    });
});

// Start server
app.listen(PORT, () => {
    logger.info('DrMindit Secure Chat Proxy Server Started', {
        port: PORT,
        environment: process.env.NODE_ENV || 'development',
        rateLimiting: process.env.ENABLE_RATE_LIMITING !== 'false',
        timestamp: new Date().toISOString()
    });
    
    console.log(`
🔒 DrMindit Secure Chat Proxy Server Started Successfully!
📍 Port: ${PORT}
🌍 Environment: ${process.env.NODE_ENV || 'development'}
🛡️  Rate Limiting: ${process.env.ENABLE_RATE_LIMITING !== 'false'}
📊 Logging: ${process.env.LOG_LEVEL || 'info'}
⏰ Started: ${new Date().toISOString()}
    `);
});

// Graceful shutdown
process.on('SIGTERM', () => {
    logger.info('SIGTERM received, shutting down gracefully');
    process.exit(0);
});

process.on('SIGINT', () => {
    logger.info('SIGINT received, shutting down gracefully');
    process.exit(0);
});

module.exports = app;
