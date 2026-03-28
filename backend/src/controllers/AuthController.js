const AuthService = require('../services/AuthService');
const { validateRequest } = require('../middleware/validation');
const { rateLimit } = require('../middleware/rateLimit');
const logger = require('../utils/logger');

/**
 * Authentication Controller - API Layer
 * Handles all authentication endpoints with proper validation and security
 */
class AuthController {
    /**
     * User registration
     * POST /api/v1/auth/register
     */
    async register(req, res) {
        const correlationId = req.headers['x-correlation-id'] || 'unknown';
        
        try {
            logger.info('User registration attempt', { correlationId, email: req.body.email });
            
            // Validate request
            const validationResult = validateRequest(req.body, {
                email: { type: 'email', required: true },
                password: { type: 'string', required: true, minLength: 8 },
                firstName: { type: 'string', required: true },
                lastName: { type: 'string', required: true }
            });
            
            if (!validationResult.isValid) {
                logger.warn('Invalid registration data', { correlationId, errors: validationResult.errors });
                return res.status(400).json({
                    success: false,
                    data: null,
                    error: {
                        code: 'VALIDATION_ERROR',
                        message: 'Invalid input data',
                        details: validationResult.errors
                    }
                });
            }
            
            // Register user
            const result = await AuthService.register(req.body, correlationId);
            
            if (result.success) {
                logger.info('User registered successfully', { correlationId, userId: result.data.userId });
                return res.status(201).json({
                    success: true,
                    data: {
                        userId: result.data.userId,
                        email: result.data.email,
                        message: 'Registration successful. Please check your email for verification.'
                    },
                    error: null
                });
            } else {
                logger.warn('Registration failed', { correlationId, error: result.error });
                return res.status(result.statusCode || 500).json({
                    success: false,
                    data: null,
                    error: {
                        code: result.error.code,
                        message: result.error.message
                    }
                });
            }
            
        } catch (error) {
            logger.error('Unexpected error in registration', { correlationId, error: error.message, stack: error.stack });
            return res.status(500).json({
                success: false,
                data: null,
                error: {
                    code: 'INTERNAL_ERROR',
                    message: 'An unexpected error occurred'
                }
            });
        }
    }
    
    /**
     * User login
     * POST /api/v1/auth/login
     */
    async login(req, res) {
        const correlationId = req.headers['x-correlation-id'] || 'unknown';
        
        try {
            logger.info('User login attempt', { correlationId, email: req.body.email });
            
            // Validate request
            const validationResult = validateRequest(req.body, {
                email: { type: 'email', required: true },
                password: { type: 'string', required: true }
            });
            
            if (!validationResult.isValid) {
                logger.warn('Invalid login data', { correlationId, errors: validationResult.errors });
                return res.status(400).json({
                    success: false,
                    data: null,
                    error: {
                        code: 'VALIDATION_ERROR',
                        message: 'Invalid input data',
                        details: validationResult.errors
                    }
                });
            }
            
            // Authenticate user
            const result = await AuthService.login(req.body, correlationId);
            
            if (result.success) {
                logger.info('User logged in successfully', { correlationId, userId: result.data.userId });
                
                // Set HTTP-only cookie for JWT
                res.cookie('auth_token', result.data.token, {
                    httpOnly: true,
                    secure: process.env.NODE_ENV === 'production',
                    sameSite: 'strict',
                    maxAge: 24 * 60 * 60 * 1000 // 24 hours
                });
                
                return res.status(200).json({
                    success: true,
                    data: {
                        user: {
                            id: result.data.userId,
                            email: result.data.email,
                            firstName: result.data.firstName,
                            lastName: result.data.lastName,
                            role: result.data.role
                        },
                        token: result.data.token
                    },
                    error: null
                });
            } else {
                logger.warn('Login failed', { correlationId, error: result.error });
                return res.status(result.statusCode || 401).json({
                    success: false,
                    data: null,
                    error: {
                        code: result.error.code,
                        message: result.error.message
                    }
                });
            }
            
        } catch (error) {
            logger.error('Unexpected error in login', { correlationId, error: error.message, stack: error.stack });
            return res.status(500).json({
                success: false,
                data: null,
                error: {
                    code: 'INTERNAL_ERROR',
                    message: 'An unexpected error occurred'
                }
            });
        }
    }
    
    /**
     * User logout
     * POST /api/v1/auth/logout
     */
    async logout(req, res) {
        const correlationId = req.headers['x-correlation-id'] || 'unknown';
        
        try {
            logger.info('User logout attempt', { correlationId });
            
            // Clear auth cookie
            res.clearCookie('auth_token');
            
            // Logout user (invalidate token)
            const token = req.cookies?.auth_token;
            if (token) {
                await AuthService.logout(token, correlationId);
            }
            
            logger.info('User logged out successfully', { correlationId });
            
            return res.status(200).json({
                success: true,
                data: {
                    message: 'Logged out successfully'
                },
                error: null
            });
            
        } catch (error) {
            logger.error('Unexpected error in logout', { correlationId, error: error.message, stack: error.stack });
            return res.status(500).json({
                success: false,
                data: null,
                error: {
                    code: 'INTERNAL_ERROR',
                    message: 'An unexpected error occurred'
                }
            });
        }
    }
    
    /**
     * Refresh token
     * POST /api/v1/auth/refresh
     */
    async refreshToken(req, res) {
        const correlationId = req.headers['x-correlation-id'] || 'unknown';
        
        try {
            const token = req.cookies?.auth_token;
            
            if (!token) {
                return res.status(401).json({
                    success: false,
                    data: null,
                    error: {
                        code: 'NO_TOKEN',
                        message: 'Authentication token required'
                    }
                });
            }
            
            const result = await AuthService.refreshToken(token, correlationId);
            
            if (result.success) {
                logger.info('Token refreshed successfully', { correlationId });
                
                // Set new token cookie
                res.cookie('auth_token', result.data.token, {
                    httpOnly: true,
                    secure: process.env.NODE_ENV === 'production',
                    sameSite: 'strict',
                    maxAge: 24 * 60 * 60 * 1000 // 24 hours
                });
                
                return res.status(200).json({
                    success: true,
                    data: {
                        token: result.data.token
                    },
                    error: null
                });
            } else {
                logger.warn('Token refresh failed', { correlationId, error: result.error });
                return res.status(result.statusCode || 401).json({
                    success: false,
                    data: null,
                    error: {
                        code: result.error.code,
                        message: result.error.message
                    }
                });
            }
            
        } catch (error) {
            logger.error('Unexpected error in token refresh', { correlationId, error: error.message, stack: error.stack });
            return res.status(500).json({
                success: false,
                data: null,
                error: {
                    code: 'INTERNAL_ERROR',
                    message: 'An unexpected error occurred'
                }
            });
        }
    }
    
    /**
     * Verify email
     * POST /api/v1/auth/verify-email
     */
    async verifyEmail(req, res) {
        const correlationId = req.headers['x-correlation-id'] || 'unknown';
        
        try {
            // Validate request
            const validationResult = validateRequest(req.body, {
                token: { type: 'string', required: true }
            });
            
            if (!validationResult.isValid) {
                return res.status(400).json({
                    success: false,
                    data: null,
                    error: {
                        code: 'VALIDATION_ERROR',
                        message: 'Invalid verification token'
                    }
                });
            }
            
            const result = await AuthService.verifyEmail(req.body.token, correlationId);
            
            if (result.success) {
                logger.info('Email verified successfully', { correlationId, userId: result.data.userId });
                return res.status(200).json({
                    success: true,
                    data: {
                        message: 'Email verified successfully'
                    },
                    error: null
                });
            } else {
                logger.warn('Email verification failed', { correlationId, error: result.error });
                return res.status(result.statusCode || 400).json({
                    success: false,
                    data: null,
                    error: {
                        code: result.error.code,
                        message: result.error.message
                    }
                });
            }
            
        } catch (error) {
            logger.error('Unexpected error in email verification', { correlationId, error: error.message, stack: error.stack });
            return res.status(500).json({
                success: false,
                data: null,
                error: {
                    code: 'INTERNAL_ERROR',
                    message: 'An unexpected error occurred'
                }
            });
        }
    }
}

// Create controller instance
const authController = new AuthController();

// Export middleware functions
module.exports = {
    register: (req, res) => authController.register(req, res),
    login: (req, res) => authController.login(req, res),
    logout: (req, res) => authController.logout(req, res),
    refreshToken: (req, res) => authController.refreshToken(req, res),
    verifyEmail: (req, res) => authController.verifyEmail(req, res)
};
