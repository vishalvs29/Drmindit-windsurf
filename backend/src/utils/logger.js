const winston = require('winston');

/**
 * Structured Logging Utility
 * Provides consistent logging across the application with correlation IDs
 */

/**
 * Create Winston logger with structured format
 */
const createLogger = () => {
    const logFormat = winston.format.combine(
        winston.format.timestamp({
            format: 'YYYY-MM-DD HH:mm:ss.SSS'
        }),
        winston.format.errors({ stack: true }),
        winston.format.json(),
        winston.format.printf(({ timestamp, level, message, correlationId, userId, error, stack, ...meta }) => {
            const logEntry = {
                timestamp,
                level: level.toUpperCase(),
                message,
                correlationId,
                userId,
                service: 'drmindit-backend',
                version: process.env.APP_VERSION || '1.0.0',
                environment: process.env.NODE_ENV || 'development'
            };
            
            // Add error details if present
            if (error) {
                logEntry.error = {
                    message: error.message || error,
                    stack: error.stack || stack
                };
            }
            
            // Add additional metadata
            if (Object.keys(meta).length > 0) {
                logEntry.metadata = meta;
            }
            
            return JSON.stringify(logEntry);
        })
    );
    
    return winston.createLogger({
        level: process.env.LOG_LEVEL || 'info',
        format: logFormat,
        defaultMeta: {
            service: 'drmindit-backend'
        },
        transports: [
            // Console transport for development
            new winston.transports.Console({
                format: winston.format.combine(
                    winston.format.colorize(),
                    winston.format.simple()
                )
            }),
            
            // File transport for production
            ...(process.env.NODE_ENV === 'production' ? [
                new winston.transports.File({
                    filename: 'logs/error.log',
                    level: 'error',
                    maxsize: 5242880, // 5MB
                    maxFiles: 5,
                    format: logFormat
                }),
                new winston.transports.File({
                    filename: 'logs/combined.log',
                    maxsize: 5242880, // 5MB
                    maxFiles: 5,
                    format: logFormat
                })
            ] : [])
        ],
        
        // Handle uncaught exceptions and rejections
        exceptionHandlers: [
            new winston.transports.File({ filename: 'logs/exceptions.log' })
        ],
        rejectionHandlers: [
            new winston.transports.File({ filename: 'logs/rejections.log' })
        ]
    });
};

const logger = createLogger();

/**
 * Logger middleware for Express
 */
const loggerMiddleware = (req, res, next) => {
    const correlationId = req.headers['x-correlation-id'] || generateCorrelationId();
    
    // Add correlation ID to request headers
    req.correlationId = correlationId;
    res.setHeader('x-correlation-id', correlationId);
    
    // Add correlation ID to logger context
    logger.defaultMeta = {
        ...logger.defaultMeta,
        correlationId,
        userId: req.user?.id,
        ip: req.ip,
        userAgent: req.get('User-Agent'),
        method: req.method,
        url: req.url,
        path: req.path
    };
    
    // Log request start
    logger.info('Request started', {
        method: req.method,
        url: req.url,
        path: req.path,
        ip: req.ip,
        userAgent: req.get('User-Agent')
    });
    
    // Override res.end to log response
    const originalEnd = res.end;
    res.end = function(chunk, encoding) {
        logger.info('Request completed', {
            statusCode: res.statusCode,
            responseTime: Date.now() - req.startTime
        });
        originalEnd.call(this, chunk, encoding);
    };
    
    req.startTime = Date.now();
    next();
};

/**
 * Generate correlation ID
 */
const generateCorrelationId = () => {
    return 'req_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
};

/**
 * Structured logging methods
 */
const structuredLogger = {
    /**
     * Log API request
     */
    logRequest: (req, res, responseTime) => {
        logger.info('API Request', {
            type: 'api_request',
            method: req.method,
            url: req.url,
            path: req.path,
            statusCode: res.statusCode,
            responseTime,
            ip: req.ip,
            userAgent: req.get('User-Agent'),
            userId: req.user?.id,
            correlationId: req.correlationId
        });
    },
    
    /**
     * Log API error
     */
    logError: (error, req, additionalContext = {}) => {
        logger.error('API Error', {
            type: 'api_error',
            error: {
                message: error.message,
                stack: error.stack,
                code: error.code
            },
            method: req?.method,
            url: req?.url,
            path: req?.path,
            ip: req?.ip,
            userAgent: req?.get('User-Agent'),
            userId: req?.user?.id,
            correlationId: req?.correlationId,
            ...additionalContext
        });
    },
    
    /**
     * Log database operation
     */
    logDatabase: (operation, table, duration, additionalContext = {}) => {
        logger.info('Database Operation', {
            type: 'database_operation',
            operation,
            table,
            duration,
            ...additionalContext
        });
    },
    
    /**
     * Log authentication event
     */
    logAuth: (event, userId, additionalContext = {}) => {
        logger.info('Authentication Event', {
            type: 'auth_event',
            event,
            userId,
            ...additionalContext
        });
    },
    
    /**
     * Log background job
     */
    logJob: (jobId, jobType, status, additionalContext = {}) => {
        logger.info('Background Job', {
            type: 'background_job',
            jobId,
            jobType,
            status,
            ...additionalContext
        });
    },
    
    /**
     * Log performance metric
     */
    logPerformance: (metric, value, unit, additionalContext = {}) => {
        logger.info('Performance Metric', {
            type: 'performance_metric',
            metric,
            value,
            unit,
            ...additionalContext
        });
    },
    
    /**
     * Log security event
     */
    logSecurity: (event, severity, additionalContext = {}) => {
        logger.warn('Security Event', {
            type: 'security_event',
            event,
            severity,
            ...additionalContext
        });
    },
    
    /**
     * Log business event
     */
    logBusiness: (event, userId, additionalContext = {}) => {
        logger.info('Business Event', {
            type: 'business_event',
            event,
            userId,
            ...additionalContext
        });
    }
};

/**
 * Performance monitoring
 */
const performanceLogger = {
    timers: new Map(),
    
    /**
     * Start timer
     */
    startTimer: (name) => {
        performanceLogger.timers.set(name, process.hrtime.bigint());
    },
    
    /**
     * End timer and log duration
     */
    endTimer: (name, additionalContext = {}) => {
        const startTime = performanceLogger.timers.get(name);
        if (startTime) {
            const endTime = process.hrtime.bigint();
            const duration = Number(endTime - startTime) / 1000000; // Convert to milliseconds
            
            structuredLogger.logPerformance(`${name}_duration`, duration, 'ms', additionalContext);
            performanceLogger.timers.delete(name);
            
            return duration;
        }
        return 0;
    }
};

/**
 * Error tracking
 */
const errorTracker = {
    /**
     * Track error with context
     */
    trackError: (error, context = {}) => {
        logger.error('Tracked Error', {
            type: 'tracked_error',
            error: {
                message: error.message,
                stack: error.stack,
                name: error.name,
                code: error.code
            },
            context,
            timestamp: new Date().toISOString()
        });
    },
    
    /**
     * Track unhandled promise rejection
     */
    trackUnhandledRejection: (reason, promise) => {
        logger.error('Unhandled Promise Rejection', {
            type: 'unhandled_rejection',
            reason: reason?.toString?.() || reason,
            promise: promise?.toString?.() || promise,
            timestamp: new Date().toISOString()
        });
    }
};

// Handle unhandled promise rejections
process.on('unhandledRejection', (reason, promise) => {
    errorTracker.trackUnhandledRejection(reason, promise);
});

// Handle uncaught exceptions
process.on('uncaughtException', (error) => {
    errorTracker.trackError(error, { type: 'uncaught_exception' });
    process.exit(1);
});

module.exports = {
    logger,
    loggerMiddleware,
    structuredLogger,
    performanceLogger,
    errorTracker,
    generateCorrelationId
};
