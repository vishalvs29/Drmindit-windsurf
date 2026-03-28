const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const compression = require('compression');
const { logger, loggerMiddleware } = require('./utils/logger');
const { generalRateLimit } = require('./middleware/auth');

/**
 * Main Application Entry Point
 * Production-grade Express app with security, logging, and middleware
 */

class Application {
    constructor() {
        this.app = express();
        this.setupMiddleware();
        this.setupRoutes();
        this.setupErrorHandling();
        this.setupGracefulShutdown();
    }
    
    /**
     * Setup application middleware
     */
    setupMiddleware() {
        // Security headers
        this.app.use(helmet({
            contentSecurityPolicy: {
                directives: {
                    defaultSrc: ["'self'"],
                    styleSrc: ["'self'", "'unsafe-inline'"],
                    scriptSrc: ["'self'"],
                    imgSrc: ["'self'", "data:", "https:"],
                    connectSrc: ["'self'"],
                    fontSrc: ["'self'"],
                    objectSrc: ["'none'"],
                    mediaSrc: ["'self'"],
                    frameSrc: ["'none'"],
                },
            },
            crossOriginEmbedderPolicy: false,
            crossOriginOpenerPolicy: false,
            crossOriginResourcePolicy: false,
            dnsPrefetchControl: false,
            frameguard: { action: 'deny' },
            hidePoweredBy: true,
            hsts: {
                maxAge: 31536000,
                includeSubDomains: true,
                preload: true
            },
            ieNoOpen: true,
            noSniff: true,
            originAgentCluster: false,
            permittedCrossDomainPolicies: false,
            referrerPolicy: { policy: 'strict-origin-when-cross-origin' },
            xssFilter: true
        }));
        
        // CORS configuration
        this.app.use(cors({
            origin: (origin, callback) => {
                // Allow specific origins in production
                const allowedOrigins = process.env.ALLOWED_ORIGINS?.split(',') || ['http://localhost:3000'];
                
                if (!origin || allowedOrigins.includes(origin)) {
                    callback(null, true);
                } else {
                    callback(new Error('Not allowed by CORS'));
                }
            },
            credentials: true,
            methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
            allowedHeaders: [
                'Origin',
                'X-Requested-With',
                'Content-Type',
                'Accept',
                'Authorization',
                'X-Correlation-ID'
            ],
            exposedHeaders: [
                'X-Total-Count',
                'X-Page-Count',
                'X-Correlation-ID'
            ]
        }));
        
        // Compression
        this.app.use(compression({
            filter: (req, res) => {
                if (req.headers['x-no-compression']) {
                    return false;
                }
                return compression.filter(req, res);
            },
            threshold: 1024,
            level: 6
        }));
        
        // Body parsing
        this.app.use(express.json({ 
            limit: '10mb',
            strict: true,
            type: 'application/json'
        }));
        
        this.app.use(express.urlencoded({ 
            extended: true,
            limit: '10mb'
        }));
        
        // Request logging
        this.app.use(loggerMiddleware);
        
        // Rate limiting
        this.app.use(generalRateLimit);
        
        // Trust proxy (if behind load balancer)
        if (process.env.TRUST_PROXY === 'true') {
            this.app.set('trust proxy', 1);
        }
        
        logger.info('Application middleware configured');
    }
    
    /**
     * Setup API routes
     */
    setupRoutes() {
        const API_VERSION = '/api/v1';
        
        // Health check
        this.app.get('/health', (req, res) => {
            res.status(200).json({
                success: true,
                data: {
                    status: 'healthy',
                    timestamp: new Date().toISOString(),
                    version: process.env.APP_VERSION || '1.0.0',
                    environment: process.env.NODE_ENV || 'development'
                },
                error: null
            });
        });
        
        // API routes
        const authController = require('./controllers/AuthController');
        const programController = require('./controllers/ProgramController');
        const userController = require('./controllers/UserController');
        const analyticsController = require('./controllers/AnalyticsController');
        
        this.app.use(`${API_VERSION}/auth`, authController);
        this.app.use(`${API_VERSION}/programs`, programController);
        this.app.use(`${API_VERSION}/users`, userController);
        this.app.use(`${API_VERSION}/analytics`, analyticsController);
        
        // API documentation route
        this.app.get(`${API_VERSION}/docs`, (req, res) => {
            res.status(200).json({
                success: true,
                data: {
                    title: 'DrMindit API Documentation',
                    version: 'v1',
                    baseUrl: `${req.protocol}://${req.get('host')}${API_VERSION}`,
                    endpoints: {
                        auth: {
                            'POST /auth/register': 'Register new user',
                            'POST /auth/login': 'User login',
                            'POST /auth/logout': 'User logout',
                            'POST /auth/refresh': 'Refresh JWT token',
                            'POST /auth/verify-email': 'Verify email address'
                        },
                        programs: {
                            'GET /programs': 'Get available programs',
                            'GET /programs/:id': 'Get program by ID',
                            'POST /programs/:id/start': 'Start program',
                            'GET /users/:userId/programs/progress': 'Get user progress',
                            'PUT /users/:userId/programs/:programId/progress': 'Update progress',
                            'POST /users/:userId/programs/:programId/complete': 'Complete program'
                        },
                        users: {
                            'GET /users/profile': 'Get user profile',
                            'PUT /users/profile': 'Update user profile',
                            'GET /users/insights': 'Get user insights',
                            'POST /users/change-password': 'Change password'
                        },
                        analytics: {
                            'GET /analytics/overview': 'Get analytics overview',
                            'GET /analytics/users': 'Get user analytics',
                            'GET /analytics/programs': 'Get program analytics'
                        }
                    }
                },
                error: null
            });
        });
        
        // 404 handler
        this.app.use('*', (req, res) => {
            res.status(404).json({
                success: false,
                data: null,
                error: {
                    code: 'NOT_FOUND',
                    message: 'Endpoint not found',
                    path: req.path,
                    method: req.method
                }
            });
        });
        
        logger.info('API routes configured');
    }
    
    /**
     * Setup global error handling
     */
    setupErrorHandling() {
        // Global error handler
        this.app.use((error, req, res, next) => {
            const correlationId = req.correlationId || 'unknown';
            
            // Log error
            logger.error('Global error handler', {
                correlationId,
                error: {
                    message: error.message,
                    stack: error.stack,
                    name: error.name
                },
                method: req.method,
                url: req.url,
                path: req.path,
                ip: req.ip,
                userAgent: req.get('User-Agent')
            });
            
            // Don't send error details in production
            const isDevelopment = process.env.NODE_ENV === 'development';
            
            const errorResponse = {
                success: false,
                data: null,
                error: {
                    code: error.code || 'INTERNAL_ERROR',
                    message: isDevelopment ? error.message : 'An internal error occurred',
                    ...(isDevelopment && { stack: error.stack })
                }
            };
            
            // Handle specific error types
            if (error.name === 'ValidationError') {
                return res.status(400).json({
                    success: false,
                    data: null,
                    error: {
                        code: 'VALIDATION_ERROR',
                        message: error.message,
                        details: error.details
                    }
                });
            }
            
            if (error.name === 'UnauthorizedError') {
                return res.status(401).json({
                    success: false,
                    data: null,
                    error: {
                        code: 'UNAUTHORIZED',
                        message: error.message
                    }
                });
            }
            
            if (error.name === 'ForbiddenError') {
                return res.status(403).json({
                    success: false,
                    data: null,
                    error: {
                        code: 'FORBIDDEN',
                        message: error.message
                    }
                });
            }
            
            if (error.name === 'NotFoundError') {
                return res.status(404).json({
                    success: false,
                    data: null,
                    error: {
                        code: 'NOT_FOUND',
                        message: error.message
                    }
                });
            }
            
            // Default error response
            const statusCode = error.statusCode || 500;
            res.status(statusCode).json(errorResponse);
        });
        
        logger.info('Global error handling configured');
    }
    
    /**
     * Setup graceful shutdown
     */
    setupGracefulShutdown() {
        const gracefulShutdown = (signal) => {
            logger.info(`Received ${signal}, starting graceful shutdown`);
            
            // Stop accepting new connections
            this.server.close(() => {
                logger.info('HTTP server closed');
                
                // Close database connections
                const UserRepository = require('./repositories/UserRepository');
                UserRepository.close().catch(err => {
                    logger.error('Error closing database', { error: err.message });
                });
                
                // Close cache connections
                const { cache } = require('./utils/cache');
                cache.close().catch(err => {
                    logger.error('Error closing cache', { error: err.message });
                });
                
                logger.info('Graceful shutdown completed');
                process.exit(0);
            });
            
            // Force shutdown after 30 seconds
            setTimeout(() => {
                logger.error('Forced shutdown after timeout');
                process.exit(1);
            }, 30000);
        };
        
        // Handle shutdown signals
        process.on('SIGTERM', () => gracefulShutdown('SIGTERM'));
        process.on('SIGINT', () => gracefulShutdown('SIGINT'));
        
        // Handle uncaught exceptions
        process.on('uncaughtException', (error) => {
            logger.error('Uncaught Exception', { error: error.message, stack: error.stack });
            gracefulShutdown('uncaughtException');
        });
        
        // Handle unhandled promise rejections
        process.on('unhandledRejection', (reason, promise) => {
            logger.error('Unhandled Rejection', { reason: reason?.toString?.() || reason, promise: promise?.toString?.() || promise });
            gracefulShutdown('unhandledRejection');
        });
        
        logger.info('Graceful shutdown handlers configured');
    }
    
    /**
     * Start the application
     */
    start(port = process.env.PORT || 3000) {
        this.server = this.app.listen(port, () => {
            logger.info('DrMindit Backend Server Started', {
                port,
                environment: process.env.NODE_ENV || 'development',
                version: process.env.APP_VERSION || '1.0.0',
                nodeVersion: process.version,
                platform: process.platform,
                pid: process.pid
            });
            
            // Log startup configuration
            logger.info('Server Configuration', {
                port,
                nodeEnv: process.env.NODE_ENV,
                dbHost: process.env.DB_HOST,
                redisHost: process.env.REDIS_HOST,
                corsOrigins: process.env.ALLOWED_ORIGINS,
                trustProxy: process.env.TRUST_PROXY
            });
        });
        
        this.server.on('error', (error) => {
            logger.error('Server error', { error: error.message, stack: error.stack });
            
            if (error.code === 'EADDRINUSE') {
                logger.error(`Port ${port} is already in use`);
            } else if (error.code === 'EACCES') {
                logger.error(`Permission denied to use port ${port}`);
            }
            
            process.exit(1);
        });
        
        return this.server;
    }
    
    /**
     * Get Express app instance (for testing)
     */
    getApp() {
        return this.app;
    }
}

// Create and export application instance
const app = new Application();

module.exports = app;
