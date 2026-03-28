const { Pool } = require('pg');
const logger = require('../utils/logger');

/**
 * User Repository - Data Access Layer
 * Handles all database operations for users with proper error handling and logging
 */
class UserRepository {
    constructor() {
        this.pool = new Pool({
            user: process.env.DB_USER || 'postgres',
            host: process.env.DB_HOST || 'localhost',
            database: process.env.DB_NAME || 'drmindit',
            password: process.env.DB_PASSWORD || '',
            port: process.env.DB_PORT || 5432,
            max: 20,
            idleTimeoutMillis: 30000,
            connectionTimeoutMillis: 2000,
        });
        
        // Test connection on startup
        this.testConnection();
    }
    
    /**
     * Test database connection
     */
    async testConnection() {
        try {
            const client = await this.pool.connect();
            await client.query('SELECT NOW()');
            client.release();
            logger.info('Database connection established successfully');
        } catch (error) {
            logger.error('Database connection failed', { error: error.message, stack: error.stack });
            throw error;
        }
    }
    
    /**
     * Create new user
     */
    async create(userData) {
        const client = await this.pool.connect();
        try {
            const query = `
                INSERT INTO users (
                    email, password, first_name, last_name, role, 
                    is_email_verified, email_verification_token, 
                    email_verification_token_expiry, created_at, updated_at
                ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
                RETURNING id, email, first_name, last_name, role, created_at
            `;
            
            const values = [
                userData.email,
                userData.password,
                userData.firstName,
                userData.lastName,
                userData.role || 'user',
                userData.isEmailVerified || false,
                userData.emailVerificationToken || null,
                userData.emailVerificationTokenExpiry || null,
                userData.createdAt || new Date(),
                userData.updatedAt || new Date()
            ];
            
            const result = await client.query(query, values);
            const user = result.rows[0];
            
            logger.info('User created successfully', { userId: user.id, email: user.email });
            return user;
            
        } catch (error) {
            logger.error('Error creating user', { error: error.message, stack: error.stack });
            throw error;
        } finally {
            client.release();
        }
    }
    
    /**
     * Find user by ID
     */
    async findById(userId) {
        const client = await this.pool.connect();
        try {
            const query = `
                SELECT id, email, first_name, last_name, role, 
                       is_email_verified, last_login_at, created_at, updated_at
                FROM users 
                WHERE id = $1 AND deleted_at IS NULL
            `;
            
            const result = await client.query(query, [userId]);
            const user = result.rows[0];
            
            if (user) {
                logger.debug('User found by ID', { userId });
            } else {
                logger.debug('User not found by ID', { userId });
            }
            
            return user;
            
        } catch (error) {
            logger.error('Error finding user by ID', { userId, error: error.message, stack: error.stack });
            throw error;
        } finally {
            client.release();
        }
    }
    
    /**
     * Find user by email
     */
    async findByEmail(email) {
        const client = await this.pool.connect();
        try {
            const query = `
                SELECT id, email, first_name, last_name, role, password,
                       is_email_verified, email_verification_token, 
                       email_verification_token_expiry, last_login_at, created_at, updated_at
                FROM users 
                WHERE email = $1 AND deleted_at IS NULL
            `;
            
            const result = await client.query(query, [email]);
            const user = result.rows[0];
            
            if (user) {
                logger.debug('User found by email', { email });
            } else {
                logger.debug('User not found by email', { email });
            }
            
            return user;
            
        } catch (error) {
            logger.error('Error finding user by email', { email, error: error.message, stack: error.stack });
            throw error;
        } finally {
            client.release();
        }
    }
    
    /**
     * Find user by email verification token
     */
    async findByEmailVerificationToken(token) {
        const client = await this.pool.connect();
        try {
            const query = `
                SELECT id, email, first_name, last_name, role,
                       email_verification_token, email_verification_token_expiry
                FROM users 
                WHERE email_verification_token = $1 
                  AND deleted_at IS NULL
                  AND email_verification_token_expiry > NOW()
            `;
            
            const result = await client.query(query, [token]);
            const user = result.rows[0];
            
            if (user) {
                logger.debug('User found by verification token', { userId: user.id });
            } else {
                logger.debug('User not found by verification token', { token });
            }
            
            return user;
            
        } catch (error) {
            logger.error('Error finding user by verification token', { token, error: error.message, stack: error.stack });
            throw error;
        } finally {
            client.release();
        }
    }
    
    /**
     * Update user
     */
    async update(userId, updateData) {
        const client = await this.pool.connect();
        try {
            // Build dynamic update query
            const updateFields = [];
            const values = [];
            let paramIndex = 1;
            
            // Add fields dynamically
            if (updateData.email !== undefined) {
                updateFields.push(`email = $${paramIndex++}`);
                values.push(updateData.email);
            }
            if (updateData.password !== undefined) {
                updateFields.push(`password = $${paramIndex++}`);
                values.push(updateData.password);
            }
            if (updateData.firstName !== undefined) {
                updateFields.push(`first_name = $${paramIndex++}`);
                values.push(updateData.firstName);
            }
            if (updateData.lastName !== undefined) {
                updateFields.push(`last_name = $${paramIndex++}`);
                values.push(updateData.lastName);
            }
            if (updateData.role !== undefined) {
                updateFields.push(`role = $${paramIndex++}`);
                values.push(updateData.role);
            }
            if (updateData.isEmailVerified !== undefined) {
                updateFields.push(`is_email_verified = $${paramIndex++}`);
                values.push(updateData.isEmailVerified);
            }
            if (updateData.emailVerificationToken !== undefined) {
                updateFields.push(`email_verification_token = $${paramIndex++}`);
                values.push(updateData.emailVerificationToken);
            }
            if (updateData.emailVerificationTokenExpiry !== undefined) {
                updateFields.push(`email_verification_token_expiry = $${paramIndex++}`);
                values.push(updateData.emailVerificationTokenExpiry);
            }
            if (updateData.lastLoginAt !== undefined) {
                updateFields.push(`last_login_at = $${paramIndex++}`);
                values.push(updateData.lastLoginAt);
            }
            
            // Always add updated_at
            updateFields.push(`updated_at = $${paramIndex++}`);
            values.push(updateData.updatedAt || new Date());
            
            // Add userId as last parameter
            values.push(userId);
            
            const query = `
                UPDATE users 
                SET ${updateFields.join(', ')}
                WHERE id = $${paramIndex} AND deleted_at IS NULL
                RETURNING id, email, first_name, last_name, role, updated_at
            `;
            
            const result = await client.query(query, values);
            const user = result.rows[0];
            
            if (user) {
                logger.info('User updated successfully', { userId, fields: Object.keys(updateData) });
            } else {
                logger.warn('User update failed - user not found', { userId });
            }
            
            return user;
            
        } catch (error) {
            logger.error('Error updating user', { userId, error: error.message, stack: error.stack });
            throw error;
        } finally {
            client.release();
        }
    }
    
    /**
     * Soft delete user
     */
    async softDelete(userId) {
        const client = await this.pool.connect();
        try {
            const query = `
                UPDATE users 
                SET deleted_at = NOW(), updated_at = NOW()
                WHERE id = $1 AND deleted_at IS NULL
                RETURNING id, email
            `;
            
            const result = await client.query(query, [userId]);
            const user = result.rows[0];
            
            if (user) {
                logger.info('User soft deleted successfully', { userId, email: user.email });
            } else {
                logger.warn('User soft delete failed - user not found', { userId });
            }
            
            return user;
            
        } catch (error) {
            logger.error('Error soft deleting user', { userId, error: error.message, stack: error.stack });
            throw error;
        } finally {
            client.release();
        }
    }
    
    /**
     * Get user statistics
     */
    async getUserStats(userId) {
        const client = await this.pool.connect();
        try {
            const query = `
                SELECT 
                    u.id,
                    u.email,
                    u.created_at,
                    u.last_login_at,
                    COUNT(DISTINCT up.id) as total_programs,
                    COUNT(DISTINCT CASE WHEN up.is_completed = true THEN up.id END) as completed_programs,
                    COALESCE(SUM(up.total_minutes_spent), 0) as total_minutes_spent,
                    MAX(up.streak_days) as max_streak_days
                FROM users u
                LEFT JOIN user_programs up ON u.id = up.user_id
                WHERE u.id = $1 AND u.deleted_at IS NULL
                GROUP BY u.id, u.email, u.created_at, u.last_login_at
            `;
            
            const result = await client.query(query, [userId]);
            const stats = result.rows[0];
            
            if (stats) {
                logger.debug('User stats retrieved', { userId });
            } else {
                logger.debug('User stats not found', { userId });
            }
            
            return stats;
            
        } catch (error) {
            logger.error('Error getting user stats', { userId, error: error.message, stack: error.stack });
            throw error;
        } finally {
            client.release();
        }
    }
    
    /**
     * Search users (admin function)
     */
    async searchUsers(searchTerm, page = 1, limit = 10) {
        const client = await this.pool.connect();
        try {
            const offset = (page - 1) * limit;
            
            const query = `
                SELECT id, email, first_name, last_name, role, 
                       is_email_verified, created_at, last_login_at
                FROM users 
                WHERE deleted_at IS NULL
                  AND (
                      email ILIKE $1 
                   OR first_name ILIKE $1 
                   OR last_name ILIKE $1
                  )
                ORDER BY created_at DESC
                LIMIT $2 OFFSET $3
            `;
            
            const values = [`%${searchTerm}%`, limit, offset];
            
            const result = await client.query(query, values);
            const users = result.rows;
            
            // Get total count for pagination
            const countQuery = `
                SELECT COUNT(*) as total
                FROM users 
                WHERE deleted_at IS NULL
                  AND (
                      email ILIKE $1 
                   OR first_name ILIKE $1 
                   OR last_name ILIKE $1
                  )
            `;
            
            const countResult = await client.query(countQuery, [`%${searchTerm}%`]);
            const total = parseInt(countResult.rows[0].total);
            
            const pagination = {
                page,
                limit,
                total,
                totalPages: Math.ceil(total / limit),
                hasNext: page * limit < total,
                hasPrev: page > 1
            };
            
            logger.debug('Users search completed', { searchTerm, page, limit, total });
            
            return {
                users,
                pagination
            };
            
        } catch (error) {
            logger.error('Error searching users', { searchTerm, page, limit, error: error.message, stack: error.stack });
            throw error;
        } finally {
            client.release();
        }
    }
    
    /**
     * Close database connection pool
     */
    async close() {
        try {
            await this.pool.end();
            logger.info('Database connection pool closed');
        } catch (error) {
            logger.error('Error closing database pool', { error: error.message, stack: error.stack });
        }
    }
}

module.exports = new UserRepository();
