const AuthService = require('../services/AuthService');
const logger = require('../utils/logger');
const rateLimit = require('express-rate-limit');

/**
 * Authentication Middleware
 * Handles JWT authentication and authorization
 */

/**
 * Rate limiting for auth endpoints
 */
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

/**
 * General rate limiting for authenticated endpoints
 */
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

/**
 * Require authentication middleware
 */
const requireAuth = async (req, res, next) => {
    const correlationId = req.headers['x-correlation-id'] || 'unknown';
    
    try {
        // Get token from cookie or header
        const token = req.cookies?.auth_token || req.headers.authorization?.replace('Bearer ', '');
        
        if (!token) {
            logger.warn('Authentication required but no token provided', { correlationId });
            return res.status(401).json({
                success: false,
                data: null,
                error: {
                    code: 'AUTHENTICATION_REQUIRED',
                    message: 'Authentication token required'
                }
            });
        }
        
        // Validate token
        const validation = await AuthService.validateToken(token, correlationId);
        
        if (!validation.valid) {
            logger.warn('Invalid authentication token', { correlationId, error: validation.error });
            
            // Clear invalid token cookie
            res.clearCookie('auth_token');
            
            return res.status(401).json({
                success: false,
                data: null,
                error: {
                    code: validation.error,
                    message: 'Invalid or expired authentication token'
                }
            });
        }
        
        // Attach user to request
        req.user = validation.user;
        
        logger.debug('Authentication successful', { correlationId, userId: validation.user.id });
        
        next();
        
    } catch (error) {
        logger.error('Error in authentication middleware', { correlationId, error: error.message, stack: error.stack });
        
        return res.status(500).json({
            success: false,
            data: null,
            error: {
                code: 'INTERNAL_ERROR',
                message: 'An unexpected error occurred during authentication'
            }
        });
    }
};

/**
 * Require admin role middleware
 */
const requireAdmin = async (req, res, next) => {
    const correlationId = req.headers['x-correlation-id'] || 'unknown';
    
    try {
        // First ensure user is authenticated
        await new Promise((resolve, reject) => {
            requireAuth(req, res, (error) => {
                if (error) {
                    return reject(error);
                }
                resolve();
            });
        });
        
        // Check if user has admin role
        if (req.user.role !== 'admin') {
            logger.warn('Admin access denied', { correlationId, userId: req.user.id, role: req.user.role });
            return res.status(403).json({
                success: false,
                data: null,
                error: {
                    code: 'ACCESS_DENIED',
                    message: 'Admin access required'
                }
            });
        }
        
        logger.debug('Admin access granted', { correlationId, userId: req.user.id });
        
        next();
        
    } catch (error) {
        logger.error('Error in admin middleware', { correlationId, error: error.message, stack: error.stack });
        
        return res.status(500).json({
            success: false,
            data: null,
            error: {
                code: 'INTERNAL_ERROR',
                message: 'An unexpected error occurred during authorization'
            }
        });
    }
};

/**
 * Optional authentication middleware (doesn't fail if no token)
 */
const optionalAuth = async (req, res, next) => {
    const correlationId = req.headers['x-correlation-id'] || 'unknown';
    
    try {
        // Get token from cookie or header
        const token = req.cookies?.auth_token || req.headers.authorization?.replace('Bearer ', '');
        
        if (token) {
            // Validate token
            const validation = await AuthService.validateToken(token, correlationId);
            
            if (validation.valid) {
                // Attach user to request
                req.user = validation.user;
                logger.debug('Optional authentication successful', { correlationId, userId: validation.user.id });
            } else {
                logger.debug('Optional authentication failed', { correlationId, error: validation.error });
                // Clear invalid token cookie
                res.clearCookie('auth_token');
            }
        }
        
        next();
        
    } catch (error) {
        logger.error('Error in optional authentication middleware', { correlationId, error: error.message, stack: error.stack });
        
        // Don't fail the request, just continue without user context
        next();
    }
};

/**
 * Check if user can access resource (user can access their own resources, admin can access any)
 */
const canAccessResource = (req, resourceUserId) => {
    // Admin can access any resource
    if (req.user && req.user.role === 'admin') {
        return true;
    }
    
    // User can access their own resources
    if (req.user && req.user.id === resourceUserId) {
        return true;
    }
    
    return false;
};

module.exports = {
    authRateLimit,
    generalRateLimit,
    requireAuth,
    requireAdmin,
    optionalAuth,
    canAccessResource
};
