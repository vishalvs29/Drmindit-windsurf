const { logger } = require('../utils/logger');
const { cache } = require('../utils/cache');

/**
 * Program Service
 * Business logic for program management
 */

class ProgramService {
    constructor() {
        this.cacheKeyPrefix = 'program:';
    }
    
    /**
     * Get all available programs
     */
    async getAllPrograms() {
        const cacheKey = `${this.cacheKeyPrefix}all`;
        
        try {
            // Try to get from cache first
            const cachedPrograms = await cache.get(cacheKey);
            if (cachedPrograms) {
                logger.debug('Programs retrieved from cache');
                return cachedPrograms;
            }
            
            // Mock programs data
            const programs = [
                {
                    id: 'program-1',
                    name: 'Stress Management',
                    description: 'Learn techniques to manage and reduce stress',
                    duration: 'DAYS_21',
                    category: 'STRESS',
                    difficulty: 'BEGINNER',
                    targetAudience: 'STUDENT',
                    isActive: true,
                    createdAt: '2024-01-01T00:00:00Z',
                    updatedAt: '2024-01-01T00:00:00Z'
                },
                {
                    id: 'program-2',
                    name: 'Anxiety Relief',
                    description: 'Comprehensive program for anxiety management',
                    duration: 'DAYS_14',
                    category: 'ANXIETY',
                    difficulty: 'INTERMEDIATE',
                    targetAudience: 'CORPORATE',
                    isActive: true,
                    createdAt: '2024-01-01T00:00:00Z',
                    updatedAt: '2024-01-01T00:00:00Z'
                },
                {
                    id: 'program-3',
                    name: 'Sleep Improvement',
                    description: 'Improve your sleep quality and patterns',
                    duration: 'DAYS_7',
                    category: 'SLEEP',
                    difficulty: 'BEGINNER',
                    targetAudience: 'POLICE_MILITARY',
                    isActive: true,
                    createdAt: '2024-01-01T00:00:00Z',
                    updatedAt: '2024-01-01T00:00:00Z'
                },
                {
                    id: 'program-4',
                    name: 'Focus Enhancement',
                    description: 'Enhance your focus and concentration',
                    duration: 'DAYS_30',
                    category: 'FOCUS',
                    difficulty: 'ADVANCED',
                    targetAudience: 'STUDENT',
                    isActive: true,
                    createdAt: '2024-01-01T00:00:00Z',
                    updatedAt: '2024-01-01T00:00:00Z'
                },
                {
                    id: 'program-5',
                    name: 'Confidence Building',
                    description: 'Build self-confidence and self-esteem',
                    duration: 'DAYS_21',
                    category: 'CONFIDENCE',
                    difficulty: 'INTERMEDIATE',
                    targetAudience: 'CORPORATE',
                    isActive: true,
                    createdAt: '2024-01-01T00:00:00Z',
                    updatedAt: '2024-01-01T00:00:00Z'
                }
            ];
            
            // Cache the result
            await cache.set(cacheKey, programs, 3600); // 1 hour cache
            
            logger.info('Programs retrieved successfully', { count: programs.length });
            return programs;
            
        } catch (error) {
            logger.error('Error getting programs', { error: error.message, stack: error.stack });
            throw error;
        }
    }
    
    /**
     * Get program by ID
     */
    async getProgramById(programId) {
        const cacheKey = `${this.cacheKeyPrefix}${programId}`;
        
        try {
            // Try to get from cache first
            const cachedProgram = await cache.get(cacheKey);
            if (cachedProgram) {
                logger.debug('Program retrieved from cache', { programId });
                return cachedProgram;
            }
            
            // Get all programs and find the specific one
            const programs = await this.getAllPrograms();
            const program = programs.find(p => p.id === programId);
            
            if (!program) {
                return null;
            }
            
            // Cache the result
            await cache.set(cacheKey, program, 3600); // 1 hour cache
            
            logger.info('Program retrieved successfully', { programId });
            return program;
            
        } catch (error) {
            logger.error('Error getting program by ID', { programId, error: error.message, stack: error.stack });
            throw error;
        }
    }
    
    /**
     * Start program for user
     */
    async startProgram(userId, programId) {
        try {
            // Check if program exists
            const program = await this.getProgramById(programId);
            if (!program) {
                throw new Error('Program not found');
            }
            
            // Mock user program creation
            const userProgram = {
                id: `user-program-${Date.now()}`,
                userId: userId,
                programId: programId,
                currentDay: 1,
                currentStep: 0,
                completedDays: [],
                dayProgress: {},
                startedAt: new Date().toISOString(),
                lastAccessedAt: new Date().toISOString(),
                totalMinutesSpent: 0,
                streakDays: 1,
                isCompleted: false,
                completedAt: null,
                createdAt: new Date().toISOString(),
                updatedAt: new Date().toISOString()
            };
            
            // Invalidate cache
            await cache.del(`${this.cacheKeyPrefix}all`);
            await cache.del(`${this.cacheKeyPrefix}${programId}`);
            
            logger.info('Program started successfully', { userId, programId, userProgramId: userProgram.id });
            return userProgram;
            
        } catch (error) {
            logger.error('Error starting program', { userId, programId, error: error.message, stack: error.stack });
            throw error;
        }
    }
    
    /**
     * Get user program progress
     */
    async getUserProgramProgress(userId, programId) {
        try {
            // Mock user program progress
            const userProgram = {
                id: `user-program-${userId}-${programId}`,
                userId: userId,
                programId: programId,
                currentDay: 8,
                currentStep: 3,
                completedDays: [1, 2, 3, 4, 5, 6, 7],
                dayProgress: {
                    1: { completed: true, timeSpent: 45 },
                    2: { completed: true, timeSpent: 38 },
                    3: { completed: true, timeSpent: 52 },
                    4: { completed: true, timeSpent: 41 },
                    5: { completed: true, timeSpent: 48 },
                    6: { completed: true, timeSpent: 35 },
                    7: { completed: true, timeSpent: 44 },
                    8: { completed: false, timeSpent: 12 }
                },
                startedAt: '2024-03-01T10:00:00Z',
                lastAccessedAt: new Date().toISOString(),
                totalMinutesSpent: 315,
                streakDays: 7,
                isCompleted: false,
                completedAt: null,
                createdAt: '2024-03-01T10:00:00Z',
                updatedAt: new Date().toISOString()
            };
            
            logger.info('User program progress retrieved', { userId, programId });
            return userProgram;
            
        } catch (error) {
            logger.error('Error getting user program progress', { userId, programId, error: error.message, stack: error.stack });
            throw error;
        }
    }
    
    /**
     * Update user program progress
     */
    async updateUserProgramProgress(userId, programId, progressData) {
        try {
            // Validate progress data
            if (!progressData.currentDay || !progressData.currentStep) {
                throw new Error('Invalid progress data');
            }
            
            // Mock progress update
            const updatedProgress = {
                userId: userId,
                programId: programId,
                currentDay: progressData.currentDay,
                currentStep: progressData.currentStep,
                completedDays: progressData.completedDays || [],
                dayProgress: progressData.dayProgress || {},
                lastAccessedAt: new Date().toISOString(),
                totalMinutesSpent: progressData.totalMinutesSpent || 0,
                streakDays: progressData.streakDays || 1,
                updatedAt: new Date().toISOString()
            };
            
            logger.info('User program progress updated', { userId, programId, currentDay: progressData.currentDay });
            return updatedProgress;
            
        } catch (error) {
            logger.error('Error updating user program progress', { userId, programId, error: error.message, stack: error.stack });
            throw error;
        }
    }
    
    /**
     * Complete user program
     */
    async completeUserProgram(userId, programId) {
        try {
            // Mock program completion
            const completionData = {
                userId: userId,
                programId: programId,
                completedAt: new Date().toISOString(),
                totalDaysCompleted: 21,
                totalTimeSpent: 630,
                finalRating: 5,
                feedback: 'Excellent program, very helpful',
                certificateUrl: `https://drmindit.com/certificates/${userId}-${programId}`,
                achievements: [
                    { type: 'completion', value: 1, description: 'Program completed' },
                    { type: 'streak', value: 21, description: '21-day streak' },
                    { type: 'time', value: 630, description: '630 minutes practiced' }
                ]
            };
            
            // Invalidate cache
            await cache.del(`${this.cacheKeyPrefix}all`);
            await cache.del(`${this.cacheKeyPrefix}${programId}`);
            
            logger.info('User program completed', { userId, programId });
            return completionData;
            
        } catch (error) {
            logger.error('Error completing user program', { userId, programId, error: error.message, stack: error.stack });
            throw error;
        }
    }
    
    /**
     * Get programs by category
     */
    async getProgramsByCategory(category) {
        try {
            const programs = await this.getAllPrograms();
            const filteredPrograms = programs.filter(p => p.category === category);
            
            logger.info('Programs retrieved by category', { category, count: filteredPrograms.length });
            return filteredPrograms;
            
        } catch (error) {
            logger.error('Error getting programs by category', { category, error: error.message, stack: error.stack });
            throw error;
        }
    }
    
    /**
     * Get programs by difficulty
     */
    async getProgramsByDifficulty(difficulty) {
        try {
            const programs = await this.getAllPrograms();
            const filteredPrograms = programs.filter(p => p.difficulty === difficulty);
            
            logger.info('Programs retrieved by difficulty', { difficulty, count: filteredPrograms.length });
            return filteredPrograms;
            
        } catch (error) {
            logger.error('Error getting programs by difficulty', { difficulty, error: error.message, stack: error.stack });
            throw error;
        }
    }
    
    /**
     * Get programs by target audience
     */
    async getProgramsByTargetAudience(targetAudience) {
        try {
            const programs = await this.getAllPrograms();
            const filteredPrograms = programs.filter(p => p.targetAudience === targetAudience);
            
            logger.info('Programs retrieved by target audience', { targetAudience, count: filteredPrograms.length });
            return filteredPrograms;
            
        } catch (error) {
            logger.error('Error getting programs by target audience', { targetAudience, error: error.message, stack: error.stack });
            throw error;
        }
    }
}

// Create singleton instance
const programService = new ProgramService();

module.exports = programService;
