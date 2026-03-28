const rateLimit = require('express-rate-limit');
const { logger } = require('../utils/logger');

/**
 * Rate Limiting Middleware
 * Provides rate limiting for API endpoints
 */

// General rate limiting for all endpoints
const generalRateLimit = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: 100, // Limit each IP to 100 requests per windowMs
    message: {
        success: false,
        data: null,
        error: {
            code: 'RATE_LIMIT_EXCEEDED',
            message: 'Too many requests, please try again later'
        }
    },
    standardHeaders: true,
    legacyHeaders: false,
    handler: (req, res) => {
        const correlationId = req.headers['x-correlation-id'] || 'unknown';
        logger.warn('Rate limit exceeded for general endpoint', { 
            correlationId, 
            ip: req.ip, 
            userAgent: req.get('User-Agent') 
        });
        
        res.status(429).json({
            success: false,
            data: null,
            error: {
                code: 'RATE_LIMIT_EXCEEDED',
                message: 'Too many requests, please try again later'
            }
        });
    }
});

// Rate limiting for authentication endpoints
const authRateLimit = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: 5, // Limit each IP to 5 requests per windowMs
    message: {
        success: false,
        data: null,
        error: {
            code: 'RATE_LIMIT_EXCEEDED',
            message: 'Too many authentication attempts, please try again later'
        }
    },
    standardHeaders: true,
    legacyHeaders: false,
    handler: (req, res) => {
        const correlationId = req.headers['x-correlation-id'] || 'unknown';
        logger.warn('Rate limit exceeded for auth endpoint', { 
            correlationId, 
            ip: req.ip, 
            userAgent: req.get('User-Agent') 
        });
        
        res.status(429).json({
            success: false,
            data: null,
            error: {
                code: 'RATE_LIMIT_EXCEEDED',
                message: 'Too many authentication attempts, please try again later'
            }
        });
    }
});

// Rate limiting for sensitive operations
const sensitiveRateLimit = rateLimit({
    windowMs: 60 * 60 * 1000, // 1 hour
    max: 10, // Limit each IP to 10 requests per hour
    message: {
        success: false,
        data: null,
        error: {
            code: 'RATE_LIMIT_EXCEEDED',
            message: 'Too many sensitive operations, please try again later'
        }
    },
    standardHeaders: true,
    legacyHeaders: false,
    handler: (req, res) => {
        const correlationId = req.headers['x-correlation-id'] || 'unknown';
        logger.warn('Rate limit exceeded for sensitive operation', { 
            correlationId, 
            ip: req.ip, 
            userAgent: req.get('User-Agent') 
        });
        
        res.status(429).json({
            success: false,
            data: null,
            error: {
                code: 'RATE_LIMIT_EXCEEDED',
                message: 'Too many sensitive operations, please try again later'
            }
        });
    }
});

module.exports = {
    generalRateLimit,
    authRateLimit,
    sensitiveRateLimit
};
