const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const compression = require('compression');
const { logger } = require('./src/utils/logger');

/**
 * Simple Test Server
 * Basic Express server for testing without complex middleware
 */

class TestServer {
    constructor() {
        this.app = express();
        this.setupMiddleware();
        this.setupRoutes();
        this.setupErrorHandling();
    }
    
    /**
     * Setup basic middleware
     */
    setupMiddleware() {
        // Basic security
        this.app.use(helmet());
        
        // CORS
        this.app.use(cors({
            origin: '*',
            credentials: true
        }));
        
        // Compression
        this.app.use(compression());
        
        // Body parsing
        this.app.use(express.json({ limit: '10mb' }));
        this.app.use(express.urlencoded({ extended: true, limit: '10mb' }));
        
        // Request logging
        this.app.use((req, res, next) => {
            logger.info('Request received', {
                method: req.method,
                url: req.url,
                timestamp: new Date().toISOString()
            });
            next();
        });
    }
    
    /**
     * Setup routes
     */
    setupRoutes() {
        // Health check
        this.app.get('/api/v1/health', (req, res) => {
            res.status(200).json({
                success: true,
                data: {
                    status: 'healthy',
                    timestamp: new Date().toISOString(),
                    version: '1.0.0',
                    environment: process.env.NODE_ENV || 'development'
                },
                error: null
            });
        });
        
        // Programs endpoint
        this.app.get('/api/v1/programs', (req, res) => {
            const programs = [
                {
                    id: 'program-1',
                    name: 'Stress Management',
                    description: 'Learn techniques to manage and reduce stress',
                    duration: 'DAYS_21',
                    category: 'STRESS',
                    difficulty: 'BEGINNER',
                    targetAudience: 'STUDENT',
                    isActive: true
                },
                {
                    id: 'program-2',
                    name: 'Anxiety Relief',
                    description: 'Comprehensive program for anxiety management',
                    duration: 'DAYS_14',
                    category: 'ANXIETY',
                    difficulty: 'INTERMEDIATE',
                    targetAudience: 'CORPORATE',
                    isActive: true
                },
                {
                    id: 'program-3',
                    name: 'Sleep Improvement',
                    description: 'Improve your sleep quality and patterns',
                    duration: 'DAYS_7',
                    category: 'SLEEP',
                    difficulty: 'BEGINNER',
                    targetAudience: 'POLICE_MILITARY',
                    isActive: true
                },
                {
                    id: 'program-4',
                    name: 'Focus Enhancement',
                    description: 'Enhance your focus and concentration',
                    duration: 'DAYS_30',
                    category: 'FOCUS',
                    difficulty: 'ADVANCED',
                    targetAudience: 'STUDENT',
                    isActive: true
                },
                {
                    id: 'program-5',
                    name: 'Confidence Building',
                    description: 'Build self-confidence and self-esteem',
                    duration: 'DAYS_21',
                    category: 'CONFIDENCE',
                    difficulty: 'INTERMEDIATE',
                    targetAudience: 'CORPORATE',
                    isActive: true
                }
            ];
            
            res.status(200).json({
                success: true,
                data: programs,
                error: null
            });
        });
        
        // Analytics endpoint
        this.app.get('/api/v1/analytics/overview', (req, res) => {
            const analyticsData = {
                totalUsers: 1250,
                activeUsers: 890,
                totalPrograms: 45,
                completedPrograms: 320,
                avgCompletionTime: 12.5,
                userGrowth: [
                    { month: 'Jan', users: 850 },
                    { month: 'Feb', users: 920 },
                    { month: 'Mar', users: 1050 },
                    { month: 'Apr', users: 1250 }
                ],
                programPopularity: [
                    { name: 'Stress Management', count: 450 },
                    { name: 'Anxiety Relief', count: 380 },
                    { name: 'Sleep Improvement', count: 290 },
                    { name: 'Focus Enhancement', count: 130 }
                ]
            };
            
            res.status(200).json({
                success: true,
                data: analyticsData,
                error: null
            });
        });
        
        // Auth endpoints (mock)
        this.app.post('/api/v1/auth/login', (req, res) => {
            const { email, password } = req.body;
            
            // Mock authentication
            if (email && password) {
                res.status(200).json({
                    success: true,
                    data: {
                        user: {
                            id: 'user-123',
                            email: email,
                            firstName: 'Test',
                            lastName: 'User',
                            role: 'user'
                        },
                        token: 'mock-jwt-token-' + Date.now()
                    },
                    error: null
                });
            } else {
                res.status(400).json({
                    success: false,
                    data: null,
                    error: {
                        code: 'INVALID_CREDENTIALS',
                        message: 'Email and password are required'
                    }
                });
            }
        });
        
        this.app.post('/api/v1/auth/register', (req, res) => {
            const { email, password, firstName, lastName } = req.body;
            
            // Mock registration
            if (email && password && firstName && lastName) {
                res.status(201).json({
                    success: true,
                    data: {
                        userId: 'user-' + Date.now(),
                        email: email,
                        firstName: firstName,
                        lastName: lastName
                    },
                    error: null
                });
            } else {
                res.status(400).json({
                    success: false,
                    data: null,
                    error: {
                        code: 'VALIDATION_ERROR',
                        message: 'All fields are required'
                    }
                });
            }
        });
        
        // User profile endpoint (protected - mock)
        this.app.get('/api/v1/users/profile', (req, res) => {
            // Mock protected endpoint - would normally check auth
            res.status(401).json({
                success: false,
                data: null,
                error: {
                    code: 'AUTHENTICATION_REQUIRED',
                    message: 'Authentication token required'
                }
            });
        });
        
        // User insights endpoint (protected - mock)
        this.app.get('/api/v1/users/insights', (req, res) => {
            // Mock protected endpoint - would normally check auth
            res.status(401).json({
                success: false,
                data: null,
                error: {
                    code: 'AUTHENTICATION_REQUIRED',
                    message: 'Authentication token required'
                }
            });
        });
        
        // API documentation
        this.app.get('/api/v1/docs', (req, res) => {
            res.status(200).json({
                success: true,
                data: {
                    title: 'DrMindit API Documentation',
                    version: 'v1',
                    baseUrl: `${req.protocol}://${req.get('host')}/api/v1`,
                    endpoints: {
                        auth: {
                            'POST /auth/register': 'Register new user',
                            'POST /auth/login': 'User login'
                        },
                        programs: {
                            'GET /programs': 'Get available programs'
                        },
                        analytics: {
                            'GET /analytics/overview': 'Get analytics overview'
                        },
                        users: {
                            'GET /users/profile': 'Get user profile (protected)',
                            'GET /users/insights': 'Get user insights (protected)'
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
    }
    
    /**
     * Setup error handling
     */
    setupErrorHandling() {
        // Global error handler
        this.app.use((error, req, res, next) => {
            logger.error('Global error handler', {
                error: error.message,
                stack: error.stack,
                method: req.method,
                url: req.url
            });
            
            res.status(500).json({
                success: false,
                data: null,
                error: {
                    code: 'INTERNAL_ERROR',
                    message: 'An internal error occurred'
                }
            });
        });
    }
    
    /**
     * Start the server
     */
    start(port = process.env.PORT || 3001) {
        this.server = this.app.listen(port, () => {
            logger.info('Test server started', {
                port,
                environment: process.env.NODE_ENV || 'development',
                version: '1.0.0'
            });
        });
        
        this.server.on('error', (error) => {
            logger.error('Server error', { error: error.message });
        });
        
        return this.server;
    }
    
    /**
     * Stop the server
     */
    stop() {
        if (this.server) {
            this.server.close(() => {
                logger.info('Test server stopped');
            });
        }
    }
}

// Create and start server if this file is executed directly
if (require.main === module) {
    const testServer = new TestServer();
    testServer.start();
    
    // Handle graceful shutdown
    process.on('SIGTERM', () => {
        console.log('Received SIGTERM, shutting down gracefully');
        testServer.stop();
        process.exit(0);
    });
    
    process.on('SIGINT', () => {
        console.log('Received SIGINT, shutting down gracefully');
        testServer.stop();
        process.exit(0);
    });
}

module.exports = TestServer;
