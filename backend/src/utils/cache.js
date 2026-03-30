const redis = require('redis');
const { logger } = require('./logger');

/**
 * Caching Utility - Redis-based caching with fallback
 * Provides caching layer for frequently accessed data
 */

class CacheManager {
    constructor() {
        this.redisClient = null;
        this.fallbackCache = new Map();
        this.isConnected = false;
        
        this.initializeRedis();
    }
    
    /**
     * Initialize Redis connection
     */
    async initializeRedis() {
        try {
            this.redisClient = redis.createClient({
                host: process.env.REDIS_HOST || 'localhost',
                port: process.env.REDIS_PORT || 6379,
                password: process.env.REDIS_PASSWORD || '',
                db: process.env.REDIS_DB || 0,
                retryDelayOnFailover: 100,
                maxRetriesPerRequest: 3,
                lazyConnect: true
            });
            
            // Event handlers
            this.redisClient.on('connect', () => {
                this.isConnected = true;
                logger.info('Redis connected successfully');
            });
            
            this.redisClient.on('ready', () => {
                logger.info('Redis ready for commands');
            });
            
            this.redisClient.on('error', (error) => {
                this.isConnected = false;
                logger.error('Redis connection error', { error: error.message });
            });
            
            this.redisClient.on('end', () => {
                this.isConnected = false;
                logger.warn('Redis connection ended');
            });
            
            this.redisClient.on('reconnecting', () => {
                logger.info('Redis reconnecting...');
            });
            
        } catch (error) {
            logger.error('Failed to initialize Redis', { error: error.message });
            this.isConnected = false;
        }
    }
    
    /**
     * Set cache value
     */
    async set(key, value, ttlSeconds = 3600) {
        try {
            const serializedValue = JSON.stringify({
                data: value,
                timestamp: Date.now(),
                ttl: ttlSeconds
            });
            
            if (this.isConnected && this.redisClient) {
                // Use Redis if available
                await this.redisClient.setEx(key, ttlSeconds, serializedValue);
                logger.debug('Cache set via Redis', { key, ttl: ttlSeconds });
                return true;
            } else {
                // Fallback to in-memory cache
                this.fallbackCache.set(key, {
                    data: value,
                    timestamp: Date.now(),
                    ttl: ttlSeconds
                });
                logger.debug('Cache set via fallback', { key, ttl: ttlSeconds });
                return true;
            }
            
        } catch (error) {
            logger.error('Error setting cache', { key, error: error.message });
            return false;
        }
    }
    
    /**
     * Get cache value
     */
    async get(key) {
        try {
            if (this.isConnected && this.redisClient) {
                // Use Redis if available
                const value = await this.redisClient.get(key);
                
                if (value) {
                    const parsed = JSON.parse(value);
                    
                    // Check if value is expired (double-check)
                    if (Date.now() - parsed.timestamp < parsed.ttl * 1000) {
                        logger.debug('Cache hit via Redis', { key });
                        return parsed.data;
                    } else {
                        // Remove expired key
                        await this.redisClient.del(key);
                        logger.debug('Cache expired and removed via Redis', { key });
                        return null;
                    }
                } else {
                    logger.debug('Cache miss via Redis', { key });
                    return null;
                }
            } else {
                // Fallback to in-memory cache
                const cached = this.fallbackCache.get(key);
                
                if (cached) {
                    // Check if value is expired
                    if (Date.now() - cached.timestamp < cached.ttl * 1000) {
                        logger.debug('Cache hit via fallback', { key });
                        return cached.data;
                    } else {
                        // Remove expired key
                        this.fallbackCache.delete(key);
                        logger.debug('Cache expired and removed via fallback', { key });
                        return null;
                    }
                } else {
                    logger.debug('Cache miss via fallback', { key });
                    return null;
                }
            }
            
        } catch (error) {
            logger.error('Error getting cache', { key, error: error.message });
            return null;
        }
    }
    
    /**
     * Delete cache value
     */
    async del(key) {
        try {
            if (this.isConnected && this.redisClient) {
                await this.redisClient.del(key);
                logger.debug('Cache deleted via Redis', { key });
            } else {
                this.fallbackCache.delete(key);
                logger.debug('Cache deleted via fallback', { key });
            }
            return true;
            
        } catch (error) {
            logger.error('Error deleting cache', { key, error: error.message });
            return false;
        }
    }
    
    /**
     * Check if key exists
     */
    async exists(key) {
        try {
            if (this.isConnected && this.redisClient) {
                const exists = await this.redisClient.exists(key);
                return exists === 1;
            } else {
                return this.fallbackCache.has(key);
            }
            
        } catch (error) {
            logger.error('Error checking cache existence', { key, error: error.message });
            return false;
        }
    }
    
    /**
     * Set multiple values (pipeline)
     */
    async mset(keyValuePairs, ttlSeconds = 3600) {
        try {
            const pipeline = this.isConnected && this.redisClient ? 
                this.redisClient.multi() : null;
            
            for (const [key, value] of Object.entries(keyValuePairs)) {
                const serializedValue = JSON.stringify({
                    data: value,
                    timestamp: Date.now(),
                    ttl: ttlSeconds
                });
                
                if (pipeline) {
                    pipeline.setEx(key, ttlSeconds, serializedValue);
                } else {
                    // Fallback for in-memory cache
                    this.fallbackCache.set(key, {
                        data: value,
                        timestamp: Date.now(),
                        ttl: ttlSeconds
                    });
                }
            }
            
            if (pipeline) {
                await pipeline.exec();
                logger.debug('Cache mset via Redis', { keys: Object.keys(keyValuePairs) });
            } else {
                logger.debug('Cache mset via fallback', { keys: Object.keys(keyValuePairs) });
            }
            
            return true;
            
        } catch (error) {
            logger.error('Error in cache mset', { error: error.message });
            return false;
        }
    }
    
    /**
     * Get multiple values
     */
    async mget(keys) {
        try {
            const results = {};
            
            if (this.isConnected && this.redisClient) {
                const values = await this.redisClient.mget(keys);
                
                keys.forEach((key, index) => {
                    const value = values[index];
                    
                    if (value) {
                        const parsed = JSON.parse(value);
                        
                        // Check if value is expired
                        if (Date.now() - parsed.timestamp < parsed.ttl * 1000) {
                            results[key] = parsed.data;
                        } else {
                            // Remove expired key asynchronously
                            this.redisClient.del(key).catch(err => 
                                logger.error('Error removing expired key', { key, error: err.message })
                            );
                        }
                    }
                });
            } else {
                // Fallback for in-memory cache
                keys.forEach(key => {
                    const cached = this.fallbackCache.get(key);
                    
                    if (cached) {
                        // Check if value is expired
                        if (Date.now() - cached.timestamp < cached.ttl * 1000) {
                            results[key] = cached.data;
                        } else {
                            // Remove expired key
                            this.fallbackCache.delete(key);
                        }
                    }
                });
            }
            
            return results;
            
        } catch (error) {
            logger.error('Error in cache mget', { error: error.message });
            return {};
        }
    }
    
    /**
     * Increment counter
     */
    async incr(key, amount = 1, ttlSeconds = 3600) {
        try {
            if (this.isConnected && this.redisClient) {
                const result = await this.redisClient.incrby(key, amount);
                
                // Set TTL if key is new
                if (result === amount) {
                    await this.redisClient.expire(key, ttlSeconds);
                }
                
                logger.debug('Cache increment via Redis', { key, amount, result });
                return result;
            } else {
                // Fallback for in-memory cache
                const current = this.fallbackCache.get(key) || { data: 0 };
                const newValue = current.data + amount;
                
                this.fallbackCache.set(key, {
                    data: newValue,
                    timestamp: Date.now(),
                    ttl: ttlSeconds
                });
                
                logger.debug('Cache increment via fallback', { key, amount, result: newValue });
                return newValue;
            }
            
        } catch (error) {
            logger.error('Error in cache increment', { key, error: error.message });
            return 0;
        }
    }
    
    /**
     * Get cache statistics
     */
    async getStats() {
        try {
            if (this.isConnected && this.redisClient) {
                const info = await this.redisClient.info();
                return {
                    connected: true,
                    type: 'redis',
                    info: info
                };
            } else {
                return {
                    connected: false,
                    type: 'memory',
                    size: this.fallbackCache.size,
                    keys: Array.from(this.fallbackCache.keys())
                };
            }
            
        } catch (error) {
            logger.error('Error getting cache stats', { error: error.message });
            return {
                connected: false,
                type: 'error',
                error: error.message
            };
        }
    }
    
    /**
     * Clear all cache
     */
    async clear() {
        try {
            if (this.isConnected && this.redisClient) {
                await this.redisClient.flushdb();
                logger.info('Redis cache cleared');
            } else {
                this.fallbackCache.clear();
                logger.info('Fallback cache cleared');
            }
            return true;
            
        } catch (error) {
            logger.error('Error clearing cache', { error: error.message });
            return false;
        }
    }
    
    /**
     * Close Redis connection
     */
    async close() {
        try {
            if (this.redisClient) {
                await this.redisClient.quit();
                logger.info('Redis connection closed');
            }
            this.fallbackCache.clear();
        } catch (error) {
            logger.error('Error closing cache', { error: error.message });
        }
    }
}

// Create singleton instance
const cache = new CacheManager();

/**
 * Cache helper functions for common use cases
 */
const cacheHelpers = {
    /**
     * Cache user session
     */
    cacheUserSession: async (userId, sessionData, ttlHours = 24) => {
        const key = `user_session:${userId}`;
        return await cache.set(key, sessionData, ttlHours * 3600);
    },
    
    /**
     * Get user session
     */
    getUserSession: async (userId) => {
        const key = `user_session:${userId}`;
        return await cache.get(key);
    },
    
    /**
     * Cache user insights
     */
    cacheUserInsights: async (userId, insights, ttlHours = 1) => {
        const key = `user_insights:${userId}`;
        return await cache.set(key, insights, ttlHours * 3600);
    },
    
    /**
     * Get user insights
     */
    getUserInsights: async (userId) => {
        const key = `user_insights:${userId}`;
        return await cache.get(key);
    },
    
    /**
     * Cache program data
     */
    cacheProgramData: async (programId, programData, ttlHours = 6) => {
        const key = `program_data:${programId}`;
        return await cache.set(key, programData, ttlHours * 3600);
    },
    
    /**
     * Get program data
     */
    getProgramData: async (programId) => {
        const key = `program_data:${programId}`;
        return await cache.get(key);
    },
    
    /**
     * Cache API rate limit
     */
    cacheRateLimit: async (identifier, windowMs, maxRequests) => {
        const key = `rate_limit:${identifier}`;
        const current = await cache.incr(key, 1, Math.ceil(windowMs / 1000));
        
        if (current === 1) {
            // Set expiry for new key
            await cache.set(key, current, Math.ceil(windowMs / 1000));
        }
        
        return {
            allowed: current <= maxRequests,
            remaining: Math.max(0, maxRequests - current),
            resetTime: Date.now() + windowMs
        };
    },
    
    /**
     * Invalidate user cache
     */
    invalidateUserCache: async (userId) => {
        const keys = [
            `user_session:${userId}`,
            `user_insights:${userId}`,
            `user_progress:${userId}`
        ];
        
        for (const key of keys) {
            await cache.del(key);
        }
        
        logger.info('User cache invalidated', { userId, keys });
    }
};

module.exports = {
    cache,
    cacheHelpers
};
