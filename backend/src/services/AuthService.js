const UserRepository = require('../repositories/UserRepository');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcryptjs');
const crypto = require('crypto');
const { logger } = require('../utils/logger');
const { cache } = require('../utils/cache');
const { sendEmail } = require('../utils/email');

/**
 * Authentication Service - Business Logic Layer
 * Handles all authentication business logic with security and validation
 */
class AuthService {
    constructor() {
        this.jwtSecret = process.env.JWT_SECRET || (() => {
            if (process.env.NODE_ENV === 'production') {
                throw new Error('JWT_SECRET environment variable is required in production');
            }
            return 'fallback-secret-change-in-production';
        })();
        this.jwtExpiry = '24h';
        this.saltRounds = 12;
    }
    
    /**
     * Register new user
     */
    async register(userData, correlationId) {
        try {
            logger.info('Registering new user', { correlationId, email: userData.email });
            
            // Check if user already exists
            const existingUser = await UserRepository.findByEmail(userData.email);
            if (existingUser) {
                logger.warn('Registration attempt with existing email', { correlationId, email: userData.email });
                return {
                    success: false,
                    error: {
                        code: 'EMAIL_ALREADY_EXISTS',
                        message: 'Email already registered'
                    },
                    statusCode: 409
                };
            }
            
            // Hash password
            const hashedPassword = await bcrypt.hash(userData.password, this.saltRounds);
            
            // Generate email verification token
            const verificationToken = crypto.randomBytes(32).toString('hex');
            const verificationTokenExpiry = new Date(Date.now() + 24 * 60 * 60 * 1000); // 24 hours
            
            // Create user
            const newUser = await UserRepository.create({
                email: userData.email,
                password: hashedPassword,
                firstName: userData.firstName,
                lastName: userData.lastName,
                role: 'user',
                isEmailVerified: false,
                emailVerificationToken: verificationToken,
                emailVerificationTokenExpiry: verificationTokenExpiry,
                createdAt: new Date(),
                updatedAt: new Date()
            });
            
            // Send verification email
            try {
                await sendEmail({
                    to: userData.email,
                    subject: 'Verify your DrMindit account',
                    template: 'email-verification',
                    data: {
                        firstName: userData.firstName,
                        verificationLink: `${process.env.FRONTEND_URL}/verify-email?token=${verificationToken}`
                    }
                });
                
                logger.info('Verification email sent', { correlationId, userId: newUser.id });
                
            } catch (emailError) {
                logger.error('Failed to send verification email', { 
                    correlationId, 
                    userId: newUser.id, 
                    error: emailError.message 
                });
                
                // Don't fail registration, but log the error
            }
            
            logger.info('User registered successfully', { correlationId, userId: newUser.id });
            
            return {
                success: true,
                data: {
                    userId: newUser.id,
                    email: newUser.email
                }
            };
            
        } catch (error) {
            logger.error('Error in user registration', { correlationId, error: error.message, stack: error.stack });
            return {
                success: false,
                error: {
                    code: 'REGISTRATION_ERROR',
                    message: 'Failed to register user'
                },
                statusCode: 500
            };
        }
    }
    
    /**
     * Authenticate user login
     */
    async login(loginData, correlationId) {
        try {
            logger.info('Authenticating user login', { correlationId, email: loginData.email });
            
            // Find user by email
            const user = await UserRepository.findByEmail(loginData.email);
            if (!user) {
                logger.warn('Login attempt with non-existent email', { correlationId, email: loginData.email });
                return {
                    success: false,
                    error: {
                        code: 'INVALID_CREDENTIALS',
                        message: 'Invalid email or password'
                    },
                    statusCode: 401
                };
            }
            
            // Check if email is verified
            if (!user.isEmailVerified) {
                logger.warn('Login attempt with unverified email', { correlationId, userId: user.id });
                return {
                    success: false,
                    error: {
                        code: 'EMAIL_NOT_VERIFIED',
                        message: 'Please verify your email before logging in'
                    },
                    statusCode: 401
                };
            }
            
            // Check password
            const isPasswordValid = await bcrypt.compare(loginData.password, user.password);
            if (!isPasswordValid) {
                logger.warn('Login attempt with invalid password', { correlationId, userId: user.id });
                return {
                    success: false,
                    error: {
                        code: 'INVALID_CREDENTIALS',
                        message: 'Invalid email or password'
                    },
                    statusCode: 401
                };
            }
            
            // Generate JWT token
            const token = jwt.sign(
                {
                    userId: user.id,
                    email: user.email,
                    role: user.role
                },
                this.jwtSecret,
                { expiresIn: this.jwtExpiry }
            );
            
            // Update last login
            await UserRepository.update(user.id, {
                lastLoginAt: new Date(),
                updatedAt: new Date()
            });
            
            // Cache user session
            await cache.set(`user_session:${user.id}`, {
                userId: user.id,
                email: user.email,
                role: user.role,
                loginTime: new Date()
            }, 24 * 60 * 60); // 24 hours
            
            logger.info('User logged in successfully', { correlationId, userId: user.id });
            
            return {
                success: true,
                data: {
                    userId: user.id,
                    email: user.email,
                    firstName: user.firstName,
                    lastName: user.lastName,
                    role: user.role,
                    token: token
                }
            };
            
        } catch (error) {
            logger.error('Error in user login', { correlationId, error: error.message, stack: error.stack });
            return {
                success: false,
                error: {
                    code: 'LOGIN_ERROR',
                    message: 'Failed to authenticate user'
                },
                statusCode: 500
            };
        }
    }
    
    /**
     * Logout user
     */
    async logout(token, correlationId) {
        try {
            if (!token) {
                return { success: true }; // Nothing to logout
            }
            
            // Decode token to get user ID
            const decoded = jwt.decode(token);
            if (decoded && decoded.userId) {
                // Remove from cache
                await cache.del(`user_session:${decoded.userId}`);
                
                // Add token to blacklist (optional, for extra security)
                await cache.set(`blacklisted_token:${token}`, true, 24 * 60 * 60);
                
                logger.info('User logged out successfully', { correlationId, userId: decoded.userId });
            }
            
            return { success: true };
            
        } catch (error) {
            logger.error('Error in user logout', { correlationId, error: error.message, stack: error.stack });
            return {
                success: false,
                error: {
                    code: 'LOGOUT_ERROR',
                    message: 'Failed to logout user'
                }
            };
        }
    }
    
    /**
     * Refresh JWT token
     */
    async refreshToken(token, correlationId) {
        try {
            // Check if token is blacklisted
            const isBlacklisted = await cache.get(`blacklisted_token:${token}`);
            if (isBlacklisted) {
                return {
                    success: false,
                    error: {
                        code: 'TOKEN_BLACKLISTED',
                        message: 'Token has been invalidated'
                    },
                    statusCode: 401
                };
            }
            
            // Verify token
            const decoded = jwt.verify(token, this.jwtSecret);
            if (!decoded || !decoded.userId) {
                return {
                    success: false,
                    error: {
                        code: 'INVALID_TOKEN',
                        message: 'Invalid authentication token'
                    },
                    statusCode: 401
                };
            }
            
            // Check if user session exists in cache
            const userSession = await cache.get(`user_session:${decoded.userId}`);
            if (!userSession) {
                return {
                    success: false,
                    error: {
                        code: 'SESSION_EXPIRED',
                        message: 'Session has expired'
                    },
                    statusCode: 401
                };
            }
            
            // Generate new token
            const newToken = jwt.sign(
                {
                    userId: decoded.userId,
                    email: decoded.email,
                    role: decoded.role
                },
                this.jwtSecret,
                { expiresIn: this.jwtExpiry }
            );
            
            // Update cache
            await cache.set(`user_session:${decoded.userId}`, {
                ...userSession,
                lastRefresh: new Date()
            }, 24 * 60 * 60);
            
            logger.info('Token refreshed successfully', { correlationId, userId: decoded.userId });
            
            return {
                success: true,
                data: {
                    token: newToken
                }
            };
            
        } catch (error) {
            logger.error('Error in token refresh', { correlationId, error: error.message, stack: error.stack });
            
            if (error.name === 'TokenExpiredError') {
                return {
                    success: false,
                    error: {
                        code: 'TOKEN_EXPIRED',
                        message: 'Authentication token has expired'
                    },
                    statusCode: 401
                };
            }
            
            return {
                success: false,
                error: {
                    code: 'TOKEN_REFRESH_ERROR',
                    message: 'Failed to refresh token'
                },
                statusCode: 500
            };
        }
    }
    
    /**
     * Verify email
     */
    async verifyEmail(token, correlationId) {
        try {
            // Find user by verification token
            const user = await UserRepository.findByEmailVerificationToken(token);
            
            if (!user) {
                return {
                    success: false,
                    error: {
                        code: 'INVALID_TOKEN',
                        message: 'Invalid verification token'
                    },
                    statusCode: 400
                };
            }
            
            // Check if token has expired
            if (user.emailVerificationTokenExpiry < new Date()) {
                return {
                    success: false,
                    error: {
                        code: 'TOKEN_EXPIRED',
                        message: 'Verification token has expired'
                    },
                    statusCode: 400
                };
            }
            
            // Mark email as verified
            await UserRepository.update(user.id, {
                isEmailVerified: true,
                emailVerificationToken: null,
                emailVerificationTokenExpiry: null,
                updatedAt: new Date()
            });
            
            logger.info('Email verified successfully', { correlationId, userId: user.id });
            
            return {
                success: true,
                data: {
                    userId: user.id
                }
            };
            
        } catch (error) {
            logger.error('Error in email verification', { correlationId, error: error.message, stack: error.stack });
            return {
                success: false,
                error: {
                    code: 'VERIFICATION_ERROR',
                    message: 'Failed to verify email'
                },
                statusCode: 500
            };
        }
    }
    
    /**
     * Validate token (middleware helper)
     */
    async validateToken(token, correlationId) {
        try {
            // Check if token is blacklisted
            const isBlacklisted = await cache.get(`blacklisted_token:${token}`);
            if (isBlacklisted) {
                return {
                    valid: false,
                    error: 'TOKEN_BLACKLISTED'
                };
            }
            
            // Verify token
            const decoded = jwt.verify(token, this.jwtSecret);
            if (!decoded || !decoded.userId) {
                return {
                    valid: false,
                    error: 'INVALID_TOKEN'
                };
            }
            
            // Check if user session exists
            const userSession = await cache.get(`user_session:${decoded.userId}`);
            if (!userSession) {
                return {
                    valid: false,
                    error: 'SESSION_EXPIRED'
                };
            }
            
            return {
                valid: true,
                user: {
                    id: decoded.userId,
                    email: decoded.email,
                    role: decoded.role
                }
            };
            
        } catch (error) {
            if (error.name === 'TokenExpiredError') {
                return {
                    valid: false,
                    error: 'TOKEN_EXPIRED'
                };
            }
            
            return {
                valid: false,
                error: 'TOKEN_INVALID'
            };
        }
    }
    
    /**
     * Change password
     */
    async changePassword(userId, currentPassword, newPassword, correlationId) {
        try {
            // Find user
            const user = await UserRepository.findById(userId);
            if (!user) {
                return {
                    success: false,
                    error: {
                        code: 'USER_NOT_FOUND',
                        message: 'User not found'
                    },
                    statusCode: 404
                };
            }
            
            // Verify current password
            const isCurrentPasswordValid = await bcrypt.compare(currentPassword, user.password);
            if (!isCurrentPasswordValid) {
                return {
                    success: false,
                    error: {
                        code: 'INVALID_CURRENT_PASSWORD',
                        message: 'Current password is incorrect'
                    },
                    statusCode: 400
                };
            }
            
            // Hash new password
            const hashedNewPassword = await bcrypt.hash(newPassword, this.saltRounds);
            
            // Update password
            await UserRepository.update(userId, {
                password: hashedNewPassword,
                updatedAt: new Date()
            });
            
            // Invalidate all user sessions (force re-login)
            await cache.del(`user_session:${userId}`);
            
            logger.info('Password changed successfully', { correlationId, userId });
            
            return {
                success: true,
                data: {
                    message: 'Password changed successfully'
                }
            };
            
        } catch (error) {
            logger.error('Error in password change', { correlationId, error: error.message, stack: error.stack });
            return {
                success: false,
                error: {
                    code: 'PASSWORD_CHANGE_ERROR',
                    message: 'Failed to change password'
                },
                statusCode: 500
            };
        }
    }
}

module.exports = new AuthService();
