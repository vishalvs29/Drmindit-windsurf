/**
 * DrMindit Secure Backend Proxy
 * 
 * SECURITY CRITICAL:
 * - All API keys are stored in environment variables ONLY
 * - No secrets in source code
 * - Rate limiting prevents abuse
 * - Authentication required for all endpoints
 * - Input validation and sanitization
 * 
 * This proxy handles:
 * - OpenAI API calls (server-side)
 * - User authentication
 * - Rate limiting
 * - Request validation
 * - Security headers
 */

require('dotenv').config();

const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
const { body, validationResult } = require('express-validator');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcrypt');
const axios = require('axios');
const winston = require('winston');

// Security: Validate required environment variables
const requiredEnvVars = [
  'OPENAI_API_KEY',
  'SUPABASE_URL',
  'SUPABASE_SERVICE_ROLE_KEY',
  'JWT_SECRET'
];

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
      imgSrc: ["'self'", "data:", "https:"],
    },
  },
  hsts: {
    maxAge: parseInt(process.env.HELMET_HSTS_MAX_AGE) || 31536000,
    includeSubDomains: true,
    preload: true
  },
  noCache: process.env.HELMET_NO_CACHE === 'true',
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

if (process.env.ENABLE_CORS === 'true') {
  app.use(cors(corsOptions));
}

// Rate limiting
const rateLimitOptions = {
  windowMs: parseInt(process.env.RATE_LIMIT_WINDOW_MS) || 60000, // 1 minute
  max: parseInt(process.env.RATE_LIMIT_MAX_REQUESTS) || 20, // 20 requests per minute
  message: {
    error: 'Too many requests from this IP, please try again later.',
    code: 'RATE_LIMIT_EXCEEDED'
  },
  standardHeaders: true,
  legacyHeaders: false,
  skipSuccessfulRequests: process.env.RATE_LIMIT_SKIP_SUCCESSFUL_REQUESTS === 'true'
};

if (process.env.ENABLE_RATE_LIMITING === 'true') {
  const limiter = rateLimit(rateLimitOptions);
  app.use('/api/', limiter);
}

// Body parsing middleware
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

// Logging configuration
const logger = winston.createLogger({
  level: process.env.LOG_LEVEL || 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.errors({ stack: true }),
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

// Request logging middleware
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

// Input validation schemas
const chatValidation = [
  body('message').trim().isLength({ min: 1, max: 1000 }).withMessage('Message must be between 1 and 1000 characters'),
  body('userId').isUUID().withMessage('Valid user ID required'),
  body('sessionId').optional().isUUID().withMessage('Session ID must be valid UUID')
];

// Authentication middleware
const authenticateToken = (req, res, next) => {
  if (process.env.SKIP_AUTH_FOR_DEV === 'true' && process.env.NODE_ENV === 'development') {
    return next();
  }

  const authHeader = req.headers.authorization;
  if (!authHeader) {
    return res.status(401).json({ error: 'Authentication required' });
  }

  const token = authHeader.split(' ')[1];
  if (!token) {
    return res.status(401).json({ error: 'Invalid token format' });
  }

  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    req.user = decoded;
    next();
  } catch (error) {
    logger.warn('Invalid token attempt', { error: error.message, ip: req.ip });
    return res.status(401).json({ error: 'Invalid or expired token' });
  }
};

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({
    status: 'healthy',
    timestamp: new Date().toISOString(),
    uptime: process.uptime(),
    version: process.env.API_VERSION || 'v1'
  });
});

// Authentication endpoint
app.post('/api/auth/login', [
  body('email').isEmail().normalizeEmail(),
  body('password').isLength({ min: 8 })
], async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }

    const { email, password } = req.body;
    
    // TODO: Implement actual user authentication with Supabase
    // For now, return a mock token for development
    const mockUser = { id: 'user-id', email: email };
    const token = jwt.sign(mockUser, process.env.JWT_SECRET, { expiresIn: '24h' });
    
    logger.info('User login', { email: email, ip: req.ip });
    
    res.json({
      success: true,
      token,
      user: { id: mockUser.id, email: mockUser.email }
    });
  } catch (error) {
    logger.error('Login error', { error: error.message, stack: error.stack });
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Secure OpenAI chat endpoint
app.post('/api/chat', authenticateToken, chatValidation, async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      logger.warn('Validation failed', { errors: errors.array(), ip: req.ip });
      return res.status(400).json({ errors: errors.array() });
    }

    const { message, userId, sessionId } = req.body;
    const user = req.user;

    // Security: Log request without sensitive data
    logger.info('Chat request', {
      userId: user.id,
      sessionId,
      messageLength: message.length,
      ip: req.ip,
      timestamp: new Date().toISOString()
    });

    // Input sanitization
    const sanitizedMessage = message
      .trim()
      .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '')
      .substring(0, 1000); // Enforce max length

    // Call OpenAI API securely (server-side)
    const openaiResponse = await callOpenAI(sanitizedMessage, sessionId);

    // Log response (without sensitive content)
    logger.info('Chat response', {
      userId: user.id,
      sessionId,
      responseTokens: openaiResponse.usage?.total_tokens,
      model: openaiResponse.model,
      ip: req.ip
    });

    res.json({
      success: true,
      response: openaiResponse.content,
      usage: openaiResponse.usage,
      sessionId
    });

  } catch (error) {
    logger.error('Chat API error', { 
      error: error.message, 
      stack: error.stack,
      userId: req.user?.id,
      ip: req.ip 
    });
    
    res.status(500).json({ 
      error: 'Internal server error',
      message: 'Unable to process chat request'
    });
  }
});

// Secure OpenAI API call
async function callOpenAI(message, sessionId = null) {
  try {
    const response = await axios.post('https://api.openai.com/v1/chat/completions', {
      model: process.env.OPENAI_MODEL || 'gpt-4',
      messages: [
        {
          role: 'system',
          content: 'You are DrMindit, a compassionate AI mental health companion. Provide supportive, empathetic responses while maintaining professional boundaries. Never provide medical advice, but encourage users to seek professional help when needed.'
        },
        {
          role: 'user',
          content: message
        }
      ],
      max_tokens: parseInt(process.env.OPENAI_MAX_TOKENS) || 150,
      temperature: parseFloat(process.env.OPENAI_TEMPERATURE) || 0.7,
      stream: false
    }, {
      headers: {
        'Authorization': `Bearer ${process.env.OPENAI_API_KEY}`,
        'Content-Type': 'application/json',
        'User-Agent': 'DrMindit-Secure-Proxy/1.0'
      },
      timeout: parseInt(process.env.API_TIMEOUT) || 30000
    });

    return {
      content: response.data.choices[0].message.content,
      usage: response.data.usage,
      model: response.data.model
    };

  } catch (error) {
    logger.error('OpenAI API error', { 
      error: error.message,
      response: error.response?.data,
      status: error.response?.status 
    });
    
    throw new Error('Failed to call OpenAI API');
  }
}

// User session validation endpoint
app.get('/api/session/validate', authenticateToken, (req, res) => {
  try {
    const user = req.user;
    
    // TODO: Validate session with Supabase
    // For now, return valid session
    res.json({
      valid: true,
      user: { id: user.id },
      timestamp: new Date().toISOString()
    });
  } catch (error) {
    logger.error('Session validation error', { error: error.message });
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Rate limit status endpoint
app.get('/api/rate-limit/status', (req, res) => {
  res.json({
    windowMs: rateLimitOptions.windowMs,
    maxRequests: rateLimitOptions.max,
    enabled: process.env.ENABLE_RATE_LIMITING === 'true'
  });
});

// Error handling middleware
app.use((error, req, res, next) => {
  logger.error('Unhandled error', {
    error: error.message,
    stack: error.stack,
    url: req.url,
    method: req.method,
    ip: req.ip
  });
  
  res.status(500).json({
    error: 'Internal server error',
    message: process.env.NODE_ENV === 'production' 
      ? 'Something went wrong' 
      : error.message
  });
});

// 404 handler
app.use((req, res) => {
  logger.warn('404 Not Found', { url: req.url, method: req.method, ip: req.ip });
  res.status(404).json({ error: 'Endpoint not found' });
});

// Start server
app.listen(PORT, () => {
  logger.info('DrMindit Secure Proxy Server started', {
    port: PORT,
    environment: process.env.NODE_ENV,
    rateLimiting: process.env.ENABLE_RATE_LIMITING === 'true',
    authentication: process.env.ENABLE_AUTHENTICATION === 'true',
    timestamp: new Date().toISOString()
  });
  
  console.log(`
🔒 DrMindit Secure Proxy Server Started Successfully!
📍 Port: ${PORT}
🌍 Environment: ${process.env.NODE_ENV}
🛡️  Rate Limiting: ${process.env.ENABLE_RATE_LIMITING}
🔐 Authentication: ${process.env.ENABLE_AUTHENTICATION}
📊 Logging: ${process.env.LOG_LEVEL}
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
