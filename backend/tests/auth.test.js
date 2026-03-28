const request = require('supertest');
const app = require('../src/app');
const { cache } = require('../src/utils/cache');
const AuthService = require('../src/services/AuthService');

/**
 * Authentication Tests
 * Comprehensive test suite for authentication functionality
 */

describe('Authentication API', () => {
    let testUser;
    let authToken;
    
    beforeAll(async () => {
        // Clear cache before tests
        await cache.clear();
        
        testUser = {
            email: 'test@example.com',
            password: 'TestPass123!',
            firstName: 'Test',
            lastName: 'User'
        };
    });
    
    afterAll(async () => {
        // Clean up after tests
        await cache.clear();
    });
    
    describe('POST /api/v1/auth/register', () => {
        it('should register a new user successfully', async () => {
            const response = await request(app)
                .post('/api/v1/auth/register')
                .send(testUser);
            
            expect(response.status).toBe(201);
            expect(response.body.success).toBe(true);
            expect(response.body.data.userId).toBeDefined();
            expect(response.body.data.email).toBe(testUser.email);
            expect(response.body.error).toBeNull();
        });
        
        it('should return validation error for invalid email', async () => {
            const invalidUser = { ...testUser, email: 'invalid-email' };
            
            const response = await request(app)
                .post('/api/v1/auth/register')
                .send(invalidUser);
            
            expect(response.status).toBe(400);
            expect(response.body.success).toBe(false);
            expect(response.body.error.code).toBe('VALIDATION_ERROR');
            expect(response.body.error.details).toContainEqual(
                expect.arrayContaining([
                    expect.objectContaining({
                        field: 'email',
                        message: 'Invalid email format'
                    })
                ])
            );
        });
        
        it('should return validation error for weak password', async () => {
            const invalidUser = { ...testUser, password: 'weak' };
            
            const response = await request(app)
                .post('/api/v1/auth/register')
                .send(invalidUser);
            
            expect(response.status).toBe(400);
            expect(response.body.success).toBe(false);
            expect(response.body.error.code).toBe('VALIDATION_ERROR');
            expect(response.body.error.details).toContainEqual(
                expect.arrayContaining([
                    expect.objectContaining({
                        field: 'password'
                    })
                ])
            );
        });
        
        it('should return error for duplicate email', async () => {
            // Register user first time
            await request(app)
                .post('/api/v1/auth/register')
                .send(testUser);
            
            // Try to register same email again
            const response = await request(app)
                .post('/api/v1/auth/register')
                .send(testUser);
            
            expect(response.status).toBe(409);
            expect(response.body.success).toBe(false);
            expect(response.body.error.code).toBe('EMAIL_ALREADY_EXISTS');
        });
        
        it('should return error for missing required fields', async () => {
            const response = await request(app)
                .post('/api/v1/auth/register')
                .send({ email: testUser.email });
            
            expect(response.status).toBe(400);
            expect(response.body.success).toBe(false);
            expect(response.body.error.code).toBe('VALIDATION_ERROR');
            expect(response.body.error.details).toContainEqual(
                expect.arrayContaining([
                    expect.objectContaining({ field: 'password' }),
                    expect.objectContaining({ field: 'firstName' }),
                    expect.objectContaining({ field: 'lastName' })
                ])
            );
        });
    });
    
    describe('POST /api/v1/auth/login', () => {
        beforeEach(async () => {
            // Register a user for login tests
            await request(app)
                .post('/api/v1/auth/register')
                .send(testUser);
        });
        
        it('should login user successfully', async () => {
            const response = await request(app)
                .post('/api/v1/auth/login')
                .send({
                    email: testUser.email,
                    password: testUser.password
                });
            
            expect(response.status).toBe(200);
            expect(response.body.success).toBe(true);
            expect(response.body.data.user.id).toBeDefined();
            expect(response.body.data.user.email).toBe(testUser.email);
            expect(response.body.data.token).toBeDefined();
            expect(response.body.error).toBeNull();
            
            // Store token for subsequent tests
            authToken = response.body.data.token;
        });
        
        it('should return error for invalid credentials', async () => {
            const response = await request(app)
                .post('/api/v1/auth/login')
                .send({
                    email: testUser.email,
                    password: 'wrongpassword'
                });
            
            expect(response.status).toBe(401);
            expect(response.body.success).toBe(false);
            expect(response.body.error.code).toBe('INVALID_CREDENTIALS');
        });
        
        it('should return error for non-existent user', async () => {
            const response = await request(app)
                .post('/api/v1/auth/login')
                .send({
                    email: 'nonexistent@example.com',
                    password: testUser.password
                });
            
            expect(response.status).toBe(401);
            expect(response.body.success).toBe(false);
            expect(response.body.error.code).toBe('INVALID_CREDENTIALS');
        });
        
        it('should return validation error for missing fields', async () => {
            const response = await request(app)
                .post('/api/v1/auth/login')
                .send({ email: testUser.email });
            
            expect(response.status).toBe(400);
            expect(response.body.success).toBe(false);
            expect(response.body.error.code).toBe('VALIDATION_ERROR');
        });
        
        it('should set HTTP-only auth cookie', async () => {
            const response = await request(app)
                .post('/api/v1/auth/login')
                .send({
                    email: testUser.email,
                    password: testUser.password
                });
            
            expect(response.status).toBe(200);
            expect(response.headers['set-cookie']).toBeDefined();
            
            const cookieHeader = response.headers['set-cookie'][0];
            expect(cookieHeader).toContain('HttpOnly');
            expect(cookieHeader).toContain('SameSite=Strict');
        });
    });
    
    describe('POST /api/v1/auth/logout', () => {
        beforeEach(async () => {
            // Login user first
            const loginResponse = await request(app)
                .post('/api/v1/auth/login')
                .send({
                    email: testUser.email,
                    password: testUser.password
                });
            authToken = loginResponse.body.data.token;
        });
        
        it('should logout user successfully', async () => {
            const response = await request(app)
                .post('/api/v1/auth/logout')
                .set('Cookie', `auth_token=${authToken}`)
                .send({});
            
            expect(response.status).toBe(200);
            expect(response.body.success).toBe(true);
            expect(response.body.data.message).toBe('Logged out successfully');
        });
        
        it('should clear auth cookie on logout', async () => {
            const response = await request(app)
                .post('/api/v1/auth/logout')
                .set('Cookie', `auth_token=${authToken}`)
                .send({});
            
            expect(response.status).toBe(200);
            expect(response.headers['set-cookie']).toBeDefined();
            expect(response.headers['set-cookie'][0]).toContain('auth_token=;');
        });
        
        it('should logout successfully even without token', async () => {
            const response = await request(app)
                .post('/api/v1/auth/logout')
                .send({});
            
            expect(response.status).toBe(200);
            expect(response.body.success).toBe(true);
        });
    });
    
    describe('POST /api/v1/auth/refresh', () => {
        beforeEach(async () => {
            // Login user first
            const loginResponse = await request(app)
                .post('/api/v1/auth/login')
                .send({
                    email: testUser.email,
                    password: testUser.password
                });
            authToken = loginResponse.body.data.token;
        });
        
        it('should refresh token successfully', async () => {
            const response = await request(app)
                .post('/api/v1/auth/refresh')
                .set('Cookie', `auth_token=${authToken}`)
                .send({});
            
            expect(response.status).toBe(200);
            expect(response.body.success).toBe(true);
            expect(response.body.data.token).toBeDefined();
            expect(response.body.data.token).not.toBe(authToken); // New token should be different
        });
        
        it('should return error for invalid token', async () => {
            const response = await request(app)
                .post('/api/v1/auth/refresh')
                .set('Cookie', 'auth_token=invalid-token')
                .send({});
            
            expect(response.status).toBe(401);
            expect(response.body.success).toBe(false);
            expect(response.body.error.code).toBe('INVALID_TOKEN');
        });
        
        it('should return error for missing token', async () => {
            const response = await request(app)
                .post('/api/v1/auth/refresh')
                .send({});
            
            expect(response.status).toBe(401);
            expect(response.body.success).toBe(false);
            expect(response.body.error.code).toBe('NO_TOKEN');
        });
    });
    
    describe('Rate Limiting', () => {
        it('should limit auth requests', async () => {
            const promises = [];
            
            // Make 6 rapid requests (limit is 5 per 15 minutes)
            for (let i = 0; i < 6; i++) {
                promises.push(
                    request(app)
                        .post('/api/v1/auth/login')
                        .send({
                            email: 'ratelimit@example.com',
                            password: 'testpass123'
                        })
                );
            }
            
            const responses = await Promise.all(promises);
            
            // First 5 should succeed (or fail with invalid credentials)
            // 6th should be rate limited
            const rateLimitedResponse = responses.find(res => res.status === 429);
            expect(rateLimitedResponse).toBeDefined();
            expect(rateLimitedResponse.body.error.code).toBe('RATE_LIMIT_EXCEEDED');
        });
    });
    
    describe('Security Headers', () => {
        it('should include security headers', async () => {
            const response = await request(app)
                .post('/api/v1/auth/login')
                .send({
                    email: testUser.email,
                    password: testUser.password
                });
            
            // Check for security headers
            expect(response.headers['x-content-type-options']).toBe('nosniff');
            expect(response.headers['x-frame-options']).toBe('DENY');
            expect(response.headers['x-xss-protection']).toBe('1; mode=block');
            expect(response.headers['strict-transport-security']).toBeDefined();
        });
        
        it('should handle CORS properly', async () => {
            const response = await request(app)
                .post('/api/v1/auth/login')
                .set('Origin', 'http://localhost:3000')
                .send({
                    email: testUser.email,
                    password: testUser.password
                });
            
            expect(response.headers['access-control-allow-origin']).toBeDefined();
            expect(response.headers['access-control-allow-credentials']).toBe('true');
        });
    });
    
    describe('Input Validation', () => {
        it('should sanitize XSS attempts in registration', async () => {
            const maliciousUser = {
                email: 'test@example.com',
                password: 'TestPass123!',
                firstName: '<script>alert("xss")</script>',
                lastName: 'User'
            };
            
            const response = await request(app)
                .post('/api/v1/auth/register')
                .send(maliciousUser);
            
            expect(response.status).toBe(201);
            // The script should be sanitized in the database
            expect(response.body.data.firstName).not.toContain('<script>');
        });
        
        it('should handle large payloads', async () => {
            const largePayload = {
                email: 'test@example.com',
                password: 'TestPass123!',
                firstName: 'A'.repeat(10000), // Very large string
                lastName: 'User'
            };
            
            const response = await request(app)
                .post('/api/v1/auth/register')
                .send(largePayload);
            
            // Should either succeed with truncation or fail validation
            expect([200, 201, 400]).toContain(response.status);
        });
    });
});

describe('Authentication Service', () => {
    describe('Token Validation', () => {
        it('should validate valid JWT token', async () => {
            const validToken = AuthService.generateTestToken({ userId: 'test-user', email: 'test@example.com' });
            const validation = await AuthService.validateToken(validToken, 'test-correlation');
            
            expect(validation.valid).toBe(true);
            expect(validation.user.id).toBe('test-user');
            expect(validation.user.email).toBe('test@example.com');
        });
        
        it('should reject expired JWT token', async () => {
            const expiredToken = AuthService.generateTestToken({ userId: 'test-user' }, { expiresIn: '-1h' });
            const validation = await AuthService.validateToken(expiredToken, 'test-correlation');
            
            expect(validation.valid).toBe(false);
            expect(validation.error).toBe('TOKEN_EXPIRED');
        });
        
        it('should reject invalid JWT token', async () => {
            const invalidToken = 'invalid-token-string';
            const validation = await AuthService.validateToken(invalidToken, 'test-correlation');
            
            expect(validation.valid).toBe(false);
            expect(validation.error).toBe('TOKEN_INVALID');
        });
    });
});

// Helper function for AuthService testing (would be implemented in AuthService)
AuthService.generateTestToken = function(payload, options = {}) {
    const jwt = require('jsonwebtoken');
    const defaultOptions = { expiresIn: '24h' };
    const tokenOptions = { ...defaultOptions, ...options };
    
    return jwt.sign(payload, 'test-secret', tokenOptions);
};
