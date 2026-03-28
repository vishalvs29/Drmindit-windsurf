const ProgramService = require('../services/ProgramService');
const { validateRequest } = require('../middleware/validation');
const { requireAuth } = require('../middleware/auth');
const { rateLimit } = require('../middleware/rateLimit');
const logger = require('../utils/logger');

/**
 * Program Controller - API Layer
 * Handles all program-related endpoints with proper validation and security
 */
class ProgramController {
    /**
     * Get available programs
     * GET /api/v1/programs
     */
    async getPrograms(req, res) {
        const correlationId = req.headers['x-correlation-id'] || 'unknown';
        
        try {
            logger.info('Fetching available programs', { correlationId });
            
            // Parse query parameters
            const { category, difficulty, page = 1, limit = 10 } = req.query;
            
            // Validate query parameters
            const validationResult = validateRequest({ category, difficulty, page, limit }, {
                category: { type: 'string', enum: ['ANXIETY', 'STRESS', 'CONFIDENCE', 'SLEEP', 'FOCUS'] },
                difficulty: { type: 'string', enum: ['BEGINNER', 'INTERMEDIATE', 'ADVANCED'] },
                page: { type: 'number', min: 1 },
                limit: { type: 'number', min: 1, max: 100 }
            });
            
            if (!validationResult.isValid) {
                logger.warn('Invalid query parameters', { correlationId, errors: validationResult.errors });
                return res.status(400).json({
                    success: false,
                    data: null,
                    error: {
                        code: 'VALIDATION_ERROR',
                        message: 'Invalid query parameters',
                        details: validationResult.errors
                    }
                });
            }
            
            // Get programs
            const result = await ProgramService.getPrograms(
                { category, difficulty, page: parseInt(page), limit: parseInt(limit) },
                correlationId
            );
            
            if (result.success) {
                logger.info('Programs fetched successfully', { correlationId, count: result.data.programs.length });
                return res.status(200).json({
                    success: true,
                    data: {
                        programs: result.data.programs,
                        pagination: result.data.pagination,
                        filters: { category, difficulty }
                    },
                    error: null
                });
            } else {
                logger.error('Failed to fetch programs', { correlationId, error: result.error });
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
            logger.error('Unexpected error in getPrograms', { correlationId, error: error.message, stack: error.stack });
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
     * Get program by ID
     * GET /api/v1/programs/:id
     */
    async getProgramById(req, res) {
        const correlationId = req.headers['x-correlation-id'] || 'unknown';
        
        try {
            const { id } = req.params;
            
            logger.info('Fetching program by ID', { correlationId, programId: id });
            
            // Validate program ID
            const validationResult = validateRequest({ id }, {
                id: { type: 'string', required: true }
            });
            
            if (!validationResult.isValid) {
                return res.status(400).json({
                    success: false,
                    data: null,
                    error: {
                        code: 'VALIDATION_ERROR',
                        message: 'Invalid program ID'
                    }
                });
            }
            
            // Get program
            const result = await ProgramService.getProgramById(id, correlationId);
            
            if (result.success) {
                logger.info('Program fetched successfully', { correlationId, programId: id });
                return res.status(200).json({
                    success: true,
                    data: result.data.program,
                    error: null
                });
            } else {
                if (result.error.code === 'PROGRAM_NOT_FOUND') {
                    return res.status(404).json({
                        success: false,
                        data: null,
                        error: {
                            code: 'PROGRAM_NOT_FOUND',
                            message: 'Program not found'
                        }
                    });
                } else {
                    logger.error('Failed to fetch program', { correlationId, error: result.error });
                    return res.status(result.statusCode || 500).json({
                        success: false,
                        data: null,
                        error: {
                            code: result.error.code,
                            message: result.error.message
                        }
                    });
                }
            }
            
        } catch (error) {
            logger.error('Unexpected error in getProgramById', { correlationId, error: error.message, stack: error.stack });
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
     * Start program for user
     * POST /api/v1/programs/:id/start
     */
    async startProgram(req, res) {
        const correlationId = req.headers['x-correlation-id'] || 'unknown';
        
        try {
            const { id } = req.params;
            const userId = req.user.id; // From auth middleware
            
            logger.info('Starting program for user', { correlationId, programId: id, userId });
            
            // Validate program ID
            const validationResult = validateRequest({ id }, {
                id: { type: 'string', required: true }
            });
            
            if (!validationResult.isValid) {
                return res.status(400).json({
                    success: false,
                    data: null,
                    error: {
                        code: 'VALIDATION_ERROR',
                        message: 'Invalid program ID'
                    }
                });
            }
            
            // Start program
            const result = await ProgramService.startProgram(id, userId, correlationId);
            
            if (result.success) {
                logger.info('Program started successfully', { correlationId, programId: id, userId });
                return res.status(201).json({
                    success: true,
                    data: {
                        userProgramId: result.data.userProgramId,
                        programId: result.data.programId,
                        currentDay: result.data.currentDay,
                        totalDays: result.data.totalDays,
                        startedAt: result.data.startedAt
                    },
                    error: null
                });
            } else {
                logger.error('Failed to start program', { correlationId, error: result.error });
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
            logger.error('Unexpected error in startProgram', { correlationId, error: error.message, stack: error.stack });
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
     * Get user's program progress
     * GET /api/v1/users/:userId/programs/progress
     */
    async getUserProgress(req, res) {
        const correlationId = req.headers['x-correlation-id'] || 'unknown';
        
        try {
            const { userId } = req.params;
            const currentUserId = req.user.id; // From auth middleware
            
            // Users can only access their own progress (unless admin)
            if (userId !== currentUserId && req.user.role !== 'admin') {
                return res.status(403).json({
                    success: false,
                    data: null,
                    error: {
                        code: 'ACCESS_DENIED',
                        message: 'Access denied'
                    }
                });
            }
            
            logger.info('Fetching user program progress', { correlationId, userId });
            
            // Get progress
            const result = await ProgramService.getUserProgress(userId, correlationId);
            
            if (result.success) {
                logger.info('User progress fetched successfully', { correlationId, userId });
                return res.status(200).json({
                    success: true,
                    data: {
                        activePrograms: result.data.activePrograms,
                        completedPrograms: result.data.completedPrograms,
                        overallStats: result.data.overallStats
                    },
                    error: null
                });
            } else {
                logger.error('Failed to fetch user progress', { correlationId, error: result.error });
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
            logger.error('Unexpected error in getUserProgress', { correlationId, error: error.message, stack: error.stack });
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
     * Update program progress
     * PUT /api/v1/users/:userId/programs/:programId/progress
     */
    async updateProgress(req, res) {
        const correlationId = req.headers['x-correlation-id'] || 'unknown';
        
        try {
            const { userId, programId } = req.params;
            const currentUserId = req.user.id; // From auth middleware
            
            // Users can only update their own progress (unless admin)
            if (userId !== currentUserId && req.user.role !== 'admin') {
                return res.status(403).json({
                    success: false,
                    data: null,
                    error: {
                        code: 'ACCESS_DENIED',
                        message: 'Access denied'
                    }
                });
            }
            
            logger.info('Updating program progress', { correlationId, userId, programId });
            
            // Validate progress data
            const validationResult = validateRequest(req.body, {
                currentDay: { type: 'number', min: 1 },
                currentStep: { type: 'number', min: 0 },
                stepProgress: { type: 'object' },
                timeSpentMinutes: { type: 'number', min: 0 }
            });
            
            if (!validationResult.isValid) {
                logger.warn('Invalid progress data', { correlationId, errors: validationResult.errors });
                return res.status(400).json({
                    success: false,
                    data: null,
                    error: {
                        code: 'VALIDATION_ERROR',
                        message: 'Invalid progress data',
                        details: validationResult.errors
                    }
                });
            }
            
            // Update progress
            const result = await ProgramService.updateProgress(
                userId,
                programId,
                req.body,
                correlationId
            );
            
            if (result.success) {
                logger.info('Progress updated successfully', { correlationId, userId, programId });
                return res.status(200).json({
                    success: true,
                    data: {
                        userProgramId: result.data.userProgramId,
                        currentDay: result.data.currentDay,
                        currentStep: result.data.currentStep,
                        progress: result.data.progress,
                        updatedAt: result.data.updatedAt
                    },
                    error: null
                });
            } else {
                logger.error('Failed to update progress', { correlationId, error: result.error });
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
            logger.error('Unexpected error in updateProgress', { correlationId, error: error.message, stack: error.stack });
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
     * Complete program
     * POST /api/v1/users/:userId/programs/:programId/complete
     */
    async completeProgram(req, res) {
        const correlationId = req.headers['x-correlation-id'] || 'unknown';
        
        try {
            const { userId, programId } = req.params;
            const currentUserId = req.user.id; // From auth middleware
            
            // Users can only complete their own programs (unless admin)
            if (userId !== currentUserId && req.user.role !== 'admin') {
                return res.status(403).json({
                    success: false,
                    data: null,
                    error: {
                        code: 'ACCESS_DENIED',
                        message: 'Access denied'
                    }
                });
            }
            
            logger.info('Completing program', { correlationId, userId, programId });
            
            // Validate completion data
            const validationResult = validateRequest(req.body, {
                rating: { type: 'number', min: 1, max: 5 },
                feedback: { type: 'string', maxLength: 1000 }
            });
            
            if (!validationResult.isValid) {
                logger.warn('Invalid completion data', { correlationId, errors: validationResult.errors });
                return res.status(400).json({
                    success: false,
                    data: null,
                    error: {
                        code: 'VALIDATION_ERROR',
                        message: 'Invalid completion data',
                        details: validationResult.errors
                    }
                });
            }
            
            // Complete program
            const result = await ProgramService.completeProgram(
                userId,
                programId,
                req.body,
                correlationId
            );
            
            if (result.success) {
                logger.info('Program completed successfully', { correlationId, userId, programId });
                return res.status(200).json({
                    success: true,
                    data: {
                        userProgramId: result.data.userProgramId,
                        completedAt: result.data.completedAt,
                        totalDays: result.data.totalDays,
                        totalTimeSpent: result.data.totalTimeSpent,
                        certificate: result.data.certificate
                    },
                    error: null
                });
            } else {
                logger.error('Failed to complete program', { correlationId, error: result.error });
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
            logger.error('Unexpected error in completeProgram', { correlationId, error: error.message, stack: error.stack });
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
const programController = new ProgramController();

// Export middleware functions
module.exports = {
    getPrograms: (req, res) => programController.getPrograms(req, res),
    getProgramById: (req, res) => programController.getProgramById(req, res),
    startProgram: (req, res) => programController.startProgram(req, res),
    getUserProgramProgress: (req, res) => programController.getUserProgramProgress(req, res),
    updateUserProgramProgress: (req, res) => programController.updateUserProgramProgress(req, res),
    completeUserProgram: (req, res) => programController.completeUserProgram(req, res)
};
