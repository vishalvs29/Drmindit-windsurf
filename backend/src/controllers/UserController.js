const { logger } = require('../utils/logger');
const { validateRequest } = require('../middleware/validation');
const { requireAuth } = require('../middleware/auth');

/**
 * User Controller
 * Handles user management and profile endpoints
 */

class UserController {
    /**
     * Get user profile
     */
    async getUserProfile(req, res) {
        const correlationId = req.correlationId || 'unknown';
        
        try {
            logger.info('Getting user profile', { correlationId, userId: req.user?.id });
            
            // Mock user profile data
            const userProfile = {
                id: req.user?.id,
                email: req.user?.email || 'user@example.com',
                firstName: 'John',
                lastName: 'Doe',
                role: 'user',
                isEmailVerified: true,
                createdAt: '2024-01-15T10:30:00Z',
                lastLoginAt: new Date().toISOString(),
                preferences: {
                    notifications: true,
                    theme: 'light',
                    language: 'en'
                },
                stats: {
                    totalPrograms: 5,
                    completedPrograms: 2,
                    totalMinutes: 450,
                    streakDays: 7
                }
            };
            
            res.status(200).json({
                success: true,
                data: userProfile,
                error: null
            });
            
            logger.info('User profile retrieved successfully', { correlationId });
            
        } catch (error) {
            logger.error('Error getting user profile', { 
                correlationId, 
                error: error.message, 
                stack: error.stack 
            });
            
            res.status(500).json({
                success: false,
                data: null,
                error: {
                    code: 'PROFILE_ERROR',
                    message: 'Failed to retrieve user profile'
                }
            });
        }
    }
    
    /**
     * Update user profile
     */
    async updateUserProfile(req, res) {
        const correlationId = req.correlationId || 'unknown';
        
        try {
            logger.info('Updating user profile', { correlationId, userId: req.user?.id });
            
            // Validate request data
            const validationResult = validateRequest(req.body, {
                firstName: { type: 'string', required: false, maxLength: 100 },
                lastName: { type: 'string', required: false, maxLength: 100 },
                preferences: { type: 'object', required: false }
            });
            
            if (!validationResult.isValid) {
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
            
            // Mock profile update
            const updatedProfile = {
                id: req.user?.id,
                email: req.user?.email || 'user@example.com',
                firstName: validationResult.data.firstName || 'John',
                lastName: validationResult.data.lastName || 'Doe',
                preferences: validationResult.data.preferences || {
                    notifications: true,
                    theme: 'light',
                    language: 'en'
                },
                updatedAt: new Date().toISOString()
            };
            
            res.status(200).json({
                success: true,
                data: updatedProfile,
                error: null
            });
            
            logger.info('User profile updated successfully', { correlationId });
            
        } catch (error) {
            logger.error('Error updating user profile', { 
                correlationId, 
                error: error.message, 
                stack: error.stack 
            });
            
            res.status(500).json({
                success: false,
                data: null,
                error: {
                    code: 'PROFILE_UPDATE_ERROR',
                    message: 'Failed to update user profile'
                }
            });
        }
    }
    
    /**
     * Get user insights
     */
    async getUserInsights(req, res) {
        const correlationId = req.correlationId || 'unknown';
        
        try {
            logger.info('Getting user insights', { correlationId, userId: req.user?.id });
            
            // Mock user insights data
            const userInsights = {
                overall: {
                    moodScore: 7.5,
                    stressLevel: 3.2,
                    anxietyLevel: 2.8,
                    sleepQuality: 8.1
                },
                trends: [
                    { date: '2024-03-20', mood: 6, stress: 4, anxiety: 3, sleep: 7 },
                    { date: '2024-03-21', mood: 7, stress: 3, anxiety: 3, sleep: 8 },
                    { date: '2024-03-22', mood: 8, stress: 2, anxiety: 2, sleep: 9 },
                    { date: '2024-03-23', mood: 7, stress: 3, anxiety: 3, sleep: 8 },
                    { date: '2024-03-24', mood: 8, stress: 2, anxiety: 2, sleep: 8 }
                ],
                recommendations: [
                    'Your mood has improved consistently over the past week',
                    'Continue with your current stress management techniques',
                    'Consider maintaining your current sleep schedule'
                ],
                achievements: [
                    { type: 'streak', value: 7, description: '7-day streak' },
                    { type: 'programs', value: 2, description: 'Programs completed' },
                    { type: 'minutes', value: 450, description: 'Total minutes practiced' }
                ]
            };
            
            res.status(200).json({
                success: true,
                data: userInsights,
                error: null
            });
            
            logger.info('User insights retrieved successfully', { correlationId });
            
        } catch (error) {
            logger.error('Error getting user insights', { 
                correlationId, 
                error: error.message, 
                stack: error.stack 
            });
            
            res.status(500).json({
                success: false,
                data: null,
                error: {
                    code: 'INSIGHTS_ERROR',
                    message: 'Failed to retrieve user insights'
                }
            });
        }
    }
    
    /**
     * Post user insights (submit mood/data)
     */
    async postUserInsights(req, res) {
        const correlationId = req.correlationId || 'unknown';
        
        try {
            logger.info('Posting user insights', { correlationId, userId: req.user?.id });
            
            // Validate request data
            const validationResult = validateRequest(req.body, {
                mood: { type: 'number', required: false, min: 1, max: 10 },
                stress: { type: 'number', required: false, min: 1, max: 10 },
                anxiety: { type: 'number', required: false, min: 1, max: 10 },
                sleep: { type: 'number', required: false, min: 1, max: 10 },
                notes: { type: 'string', required: false, maxLength: 500 }
            });
            
            if (!validationResult.isValid) {
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
            
            // Mock insights submission
            const submittedData = {
                userId: req.user?.id,
                data: validationResult.data,
                timestamp: new Date().toISOString(),
                id: 'insight_' + Date.now()
            };
            
            res.status(201).json({
                success: true,
                data: submittedData,
                error: null
            });
            
            logger.info('User insights submitted successfully', { correlationId });
            
        } catch (error) {
            logger.error('Error posting user insights', { 
                correlationId, 
                error: error.message, 
                stack: error.stack 
            });
            
            res.status(500).json({
                success: false,
                data: null,
                error: {
                    code: 'INSIGHTS_SUBMIT_ERROR',
                    message: 'Failed to submit user insights'
                }
            });
        }
    }
    
    /**
     * Change password
     */
    async changePassword(req, res) {
        const correlationId = req.correlationId || 'unknown';
        
        try {
            logger.info('Changing user password', { correlationId, userId: req.user?.id });
            
            // Validate request data
            const validationResult = validateRequest(req.body, {
                currentPassword: { type: 'string', required: true, minLength: 1 },
                newPassword: { type: 'password', required: true },
                confirmPassword: { type: 'string', required: true, minLength: 1 }
            });
            
            if (!validationResult.isValid) {
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
            
            // Check if passwords match
            if (validationResult.data.newPassword !== validationResult.data.confirmPassword) {
                return res.status(400).json({
                    success: false,
                    data: null,
                    error: {
                        code: 'PASSWORD_MISMATCH',
                        message: 'New password and confirmation do not match'
                    }
                });
            }
            
            // Mock password change
            const passwordChangeResult = {
                userId: req.user?.id,
                changedAt: new Date().toISOString(),
                success: true
            };
            
            res.status(200).json({
                success: true,
                data: passwordChangeResult,
                error: null
            });
            
            logger.info('Password changed successfully', { correlationId });
            
        } catch (error) {
            logger.error('Error changing password', { 
                correlationId, 
                error: error.message, 
                stack: error.stack 
            });
            
            res.status(500).json({
                success: false,
                data: null,
                error: {
                    code: 'PASSWORD_CHANGE_ERROR',
                    message: 'Failed to change password'
                }
            });
        }
    }
}

// Create controller instance
const userController = new UserController();

// Export middleware functions
module.exports = {
    getUserProfile: (req, res) => userController.getUserProfile(req, res),
    updateUserProfile: (req, res) => userController.updateUserProfile(req, res),
    getUserInsights: (req, res) => userController.getUserInsights(req, res),
    postUserInsights: (req, res) => userController.postUserInsights(req, res),
    changePassword: (req, res) => userController.changePassword(req, res)
};
