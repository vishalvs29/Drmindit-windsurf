const request = require('supertest');
const app = require('../src/app').getApp();
const { logger } = require('../src/utils/logger');
const { cache } = require('../src/utils/cache');
const jwt = require('jsonwebtoken');

/**
 * Authentication Validation Test Suite
 * Comprehensive testing of all authentication flows and security
 */

class AuthValidator {
    constructor() {
        this.testResults = [];
        this.testUsers = [];
        this.jwtSecret = process.env.JWT_SECRET || 'test-secret';
    }
    
    /**
     * Run comprehensive authentication tests
     */
    async runAuthTests() {
        logger.info('Starting comprehensive authentication validation tests');
        
        try {
            // Clear cache before tests
            await cache.clear();
            
            // Test 1: User Registration Flow
            await this.testRegistrationFlow();
            
            // Test 2: User Login Flow
            await this.testLoginFlow();
            
            // Test 3: Token Management
            await this.testTokenManagement();
            
            // Test 4: Protected Endpoint Access
            await this.testProtectedEndpoints();
            
            // Test 5: Role-Based Access Control
            await this.testRoleBasedAccess();
            
            // Test 6: Security Validation
            await this.testSecurityValidation();
            
            // Test 7: Error Handling
            await this.testErrorHandling();
            
            // Generate comprehensive report
            this.generateAuthTestReport();
            
        } catch (error) {
            logger.error('Authentication validation failed', { error: error.message, stack: error.stack });
            throw error;
        }
    }
    
    /**
     * Test user registration flow
     */
    async testRegistrationFlow() {
        logger.info('Testing user registration flow');
        
        const testResult = {
            testName: 'User Registration Flow',
            tests: [],
            passed: 0,
            failed: 0,
            issues: []
        };
        
        try {
            // Test 1.1: Valid registration
            const validUser = {
                email: 'validuser@test.com',
                password: 'ValidPass123!',
                firstName: 'Valid',
                lastName: 'User'
            };
            
            const validResponse = await request(app)
                .post('/api/v1/auth/register')
                .send(validUser);
            
            this.recordTestResult(testResult, 'Valid Registration', {
                passed: validResponse.status === 201,
                expected: 201,
                actual: validResponse.status,
                details: validResponse.body
            });
            
            if (validResponse.status === 201) {
                this.testUsers.push({
                    email: validUser.email,
                    password: validUser.password,
                    userId: validResponse.body.data.userId
                });
            }
            
            // Test 1.2: Duplicate email registration
            const duplicateResponse = await request(app)
                .post('/api/v1/auth/register')
                .send(validUser);
            
            this.recordTestResult(testResult, 'Duplicate Email Registration', {
                passed: duplicateResponse.status === 409,
                expected: 409,
                actual: duplicateResponse.status,
                details: duplicateResponse.body
            });
            
            // Test 1.3: Invalid email format
            const invalidEmailResponse = await request(app)
                .post('/api/v1/auth/register')
                .send({
                    email: 'invalid-email',
                    password: 'ValidPass123!',
                    firstName: 'Test',
                    lastName: 'User'
                });
            
            this.recordTestResult(testResult, 'Invalid Email Format', {
                passed: invalidEmailResponse.status === 400,
                expected: 400,
                actual: invalidEmailResponse.status,
                details: invalidEmailResponse.body
            });
            
            // Test 1.4: Weak password
            const weakPasswordResponse = await request(app)
                .post('/api/v1/auth/register')
                .send({
                    email: 'weakpass@test.com',
                    password: 'weak',
                    firstName: 'Test',
                    lastName: 'User'
                });
            
            this.recordTestResult(testResult, 'Weak Password', {
                passed: weakPasswordResponse.status === 400,
                expected: 400,
                actual: weakPasswordResponse.status,
                details: weakPasswordResponse.body
            });
            
            // Test 1.5: Missing required fields
            const missingFieldsResponse = await request(app)
                .post('/api/v1/auth/register')
                .send({
                    email: 'missing@test.com'
                });
            
            this.recordTestResult(testResult, 'Missing Required Fields', {
                passed: missingFieldsResponse.status === 400,
                expected: 400,
                actual: missingFieldsResponse.status,
                details: missingFieldsResponse.body
            });
            
        } catch (error) {
            testResult.issues.push(`Registration flow test error: ${error.message}`);
        }
        
        this.testResults.push(testResult);
    }
    
    /**
     * Test user login flow
     */
    async testLoginFlow() {
        logger.info('Testing user login flow');
        
        const testResult = {
            testName: 'User Login Flow',
            tests: [],
            passed: 0,
            failed: 0,
            issues: []
        };
        
        try {
            // Ensure we have a test user
            if (this.testUsers.length === 0) {
                await this.createTestUser();
            }
            
            const testUser = this.testUsers[0];
            
            // Test 2.1: Valid login
            const validLoginResponse = await request(app)
                .post('/api/v1/auth/login')
                .send({
                    email: testUser.email,
                    password: testUser.password
                });
            
            this.recordTestResult(testResult, 'Valid Login', {
                passed: validLoginResponse.status === 200,
                expected: 200,
                actual: validLoginResponse.status,
                details: validLoginResponse.body
            });
            
            // Test 2.2: Invalid password
            const invalidPasswordResponse = await request(app)
                .post('/api/v1/auth/login')
                .send({
                    email: testUser.email,
                    password: 'wrongpassword'
                });
            
            this.recordTestResult(testResult, 'Invalid Password', {
                passed: invalidPasswordResponse.status === 401,
                expected: 401,
                actual: invalidPasswordResponse.status,
                details: invalidPasswordResponse.body
            });
            
            // Test 2.3: Non-existent user
            const nonExistentResponse = await request(app)
                .post('/api/v1/auth/login')
                .send({
                    email: 'nonexistent@test.com',
                    password: 'password123'
                });
            
            this.recordTestResult(testResult, 'Non-existent User', {
                passed: nonExistentResponse.status === 401,
                expected: 401,
                actual: nonExistentResponse.status,
                details: nonExistentResponse.body
            });
            
            // Test 2.4: Missing credentials
            const missingCredentialsResponse = await request(app)
                .post('/api/v1/auth/login')
                .send({
                    email: testUser.email
                });
            
            this.recordTestResult(testResult, 'Missing Credentials', {
                passed: missingCredentialsResponse.status === 400,
                expected: 400,
                actual: missingCredentialsResponse.status,
                details: missingCredentialsResponse.body
            });
            
            // Test 2.5: Check for auth cookie
            if (validLoginResponse.status === 200) {
                const hasAuthCookie = validLoginResponse.headers['set-cookie']?.some(cookie => 
                    cookie.includes('auth_token')
                );
                
                this.recordTestResult(testResult, 'Auth Cookie Set', {
                    passed: hasAuthCookie,
                    expected: true,
                    actual: hasAuthCookie,
                    details: { hasAuthCookie }
                });
            }
            
        } catch (error) {
            testResult.issues.push(`Login flow test error: ${error.message}`);
        }
        
        this.testResults.push(testResult);
    }
    
    /**
     * Test token management
     */
    async testTokenManagement() {
        logger.info('Testing token management');
        
        const testResult = {
            testName: 'Token Management',
            tests: [],
            passed: 0,
            failed: 0,
            issues: []
        };
        
        try {
            // Get valid token
            const token = await this.getValidToken();
            
            // Test 3.1: Token refresh
            const refreshResponse = await request(app)
                .post('/api/v1/auth/refresh')
                .set('Cookie', `auth_token=${token}`)
                .send({});
            
            this.recordTestResult(testResult, 'Token Refresh', {
                passed: refreshResponse.status === 200,
                expected: 200,
                actual: refreshResponse.status,
                details: refreshResponse.body
            });
            
            // Test 3.2: Invalid token refresh
            const invalidTokenResponse = await request(app)
                .post('/api/v1/auth/refresh')
                .set('Cookie', 'auth_token=invalid-token')
                .send({});
            
            this.recordTestResult(testResult, 'Invalid Token Refresh', {
                passed: invalidTokenResponse.status === 401,
                expected: 401,
                actual: invalidTokenResponse.status,
                details: invalidTokenResponse.body
            });
            
            // Test 3.3: Missing token refresh
            const missingTokenResponse = await request(app)
                .post('/api/v1/auth/refresh')
                .send({});
            
            this.recordTestResult(testResult, 'Missing Token Refresh', {
                passed: missingTokenResponse.status === 401,
                expected: 401,
                actual: missingTokenResponse.status,
                details: missingTokenResponse.body
            });
            
            // Test 3.4: Token logout
            const logoutResponse = await request(app)
                .post('/api/v1/auth/logout')
                .set('Cookie', `auth_token=${token}`)
                .send({});
            
            this.recordTestResult(testResult, 'Token Logout', {
                passed: logoutResponse.status === 200,
                expected: 200,
                actual: logoutResponse.status,
                details: logoutResponse.body
            });
            
            // Test 3.5: Verify token is blacklisted after logout
            const blacklistedResponse = await request(app)
                .get('/api/v1/users/profile')
                .set('Cookie', `auth_token=${token}`)
                .send({});
            
            this.recordTestResult(testResult, 'Token Blacklisted After Logout', {
                passed: blacklistedResponse.status === 401,
                expected: 401,
                actual: blacklistedResponse.status,
                details: blacklistedResponse.body
            });
            
        } catch (error) {
            testResult.issues.push(`Token management test error: ${error.message}`);
        }
        
        this.testResults.push(testResult);
    }
    
    /**
     * Test protected endpoint access
     */
    async testProtectedEndpoints() {
        logger.info('Testing protected endpoint access');
        
        const testResult = {
            testName: 'Protected Endpoint Access',
            tests: [],
            passed: 0,
            failed: 0,
            issues: []
        };
        
        try {
            const token = await this.getValidToken();
            
            // Test 4.1: Access protected endpoint without token
            const noTokenResponse = await request(app)
                .get('/api/v1/users/profile')
                .send({});
            
            this.recordTestResult(testResult, 'Access Without Token', {
                passed: noTokenResponse.status === 401,
                expected: 401,
                actual: noTokenResponse.status,
                details: noTokenResponse.body
            });
            
            // Test 4.2: Access protected endpoint with valid token
            const validTokenResponse = await request(app)
                .get('/api/v1/users/profile')
                .set('Cookie', `auth_token=${token}`)
                .send({});
            
            this.recordTestResult(testResult, 'Access With Valid Token', {
                passed: validTokenResponse.status === 200,
                expected: 200,
                actual: validTokenResponse.status,
                details: validTokenResponse.body
            });
            
            // Test 4.3: Access protected endpoint with invalid token
            const invalidTokenResponse = await request(app)
                .get('/api/v1/users/profile')
                .set('Cookie', 'auth_token=invalid-token')
                .send({});
            
            this.recordTestResult(testResult, 'Access With Invalid Token', {
                passed: invalidTokenResponse.status === 401,
                expected: 401,
                actual: invalidTokenResponse.status,
                details: invalidTokenResponse.body
            });
            
            // Test 4.4: Access protected endpoint with expired token
            const expiredToken = jwt.sign(
                { userId: 'test-user', email: 'test@test.com' },
                this.jwtSecret,
                { expiresIn: '-1h' }
            );
            
            const expiredTokenResponse = await request(app)
                .get('/api/v1/users/profile')
                .set('Cookie', `auth_token=${expiredToken}`)
                .send({});
            
            this.recordTestResult(testResult, 'Access With Expired Token', {
                passed: expiredTokenResponse.status === 401,
                expected: 401,
                actual: expiredTokenResponse.status,
                details: expiredTokenResponse.body
            });
            
        } catch (error) {
            testResult.issues.push(`Protected endpoint test error: ${error.message}`);
        }
        
        this.testResults.push(testResult);
    }
    
    /**
     * Test role-based access control
     */
    async testRoleBasedAccess() {
        logger.info('Testing role-based access control');
        
        const testResult = {
            testName: 'Role-Based Access Control',
            tests: [],
            passed: 0,
            failed: 0,
            issues: []
        };
        
        try {
            // Create admin user (this would require admin setup in test environment)
            const adminUser = await this.createAdminUser();
            const regularUser = this.testUsers[0];
            
            // Test 5.1: Regular user accessing user endpoints
            const userAccessResponse = await request(app)
                .get('/api/v1/users/profile')
                .set('Cookie', `auth_token=${regularUser.token}`)
                .send({});
            
            this.recordTestResult(testResult, 'Regular User Access', {
                passed: userAccessResponse.status === 200,
                expected: 200,
                actual: userAccessResponse.status,
                details: userAccessResponse.body
            });
            
            // Test 5.2: Regular user accessing admin endpoints
            const adminAccessResponse = await request(app)
                .get('/api/v1/admin/users')
                .set('Cookie', `auth_token=${regularUser.token}`)
                .send({});
            
            this.recordTestResult(testResult, 'Regular User Admin Access', {
                passed: adminAccessResponse.status === 403 || adminAccessResponse.status === 404,
                expected: [403, 404],
                actual: adminAccessResponse.status,
                details: adminAccessResponse.body
            });
            
            // Test 5.3: Admin user accessing admin endpoints
            if (adminUser) {
                const adminAdminAccessResponse = await request(app)
                    .get('/api/v1/admin/users')
                    .set('Cookie', `auth_token=${adminUser.token}`)
                    .send({});
                
                this.recordTestResult(testResult, 'Admin User Admin Access', {
                    passed: adminAdminAccessResponse.status === 200,
                    expected: 200,
                    actual: adminAdminAccessResponse.status,
                    details: adminAdminAccessResponse.body
                });
            }
            
        } catch (error) {
            testResult.issues.push(`Role-based access test error: ${error.message}`);
        }
        
        this.testResults.push(testResult);
    }
    
    /**
     * Test security validation
     */
    async testSecurityValidation() {
        logger.info('Testing security validation');
        
        const testResult = {
            testName: 'Security Validation',
            tests: [],
            passed: 0,
            failed: 0,
            issues: []
        };
        
        try {
            // Test 6.1: SQL injection attempt
            const sqlInjectionResponse = await request(app)
                .post('/api/v1/auth/login')
                .send({
                    email: "'; DROP TABLE users; --",
                    password: 'password'
                });
            
            this.recordTestResult(testResult, 'SQL Injection Protection', {
                passed: sqlInjectionResponse.status === 400 || sqlInjectionResponse.status === 401,
                expected: [400, 401],
                actual: sqlInjectionResponse.status,
                details: sqlInjectionResponse.body
            });
            
            // Test 6.2: XSS attempt
            const xssResponse = await request(app)
                .post('/api/v1/auth/register')
                .send({
                    email: 'xss@test.com',
                    password: 'XssPass123!',
                    firstName: '<script>alert("xss")</script>',
                    lastName: 'User'
                });
            
            this.recordTestResult(testResult, 'XSS Protection', {
                passed: xssResponse.status === 201 && !xssResponse.body.data.firstName.includes('<script>'),
                expected: 'Sanitized response',
                actual: xssResponse.body.data.firstName,
                details: xssResponse.body
            });
            
            // Test 6.3: Rate limiting
            const rateLimitPromises = [];
            for (let i = 0; i < 10; i++) {
                rateLimitPromises.push(
                    request(app)
                        .post('/api/v1/auth/login')
                        .send({
                            email: 'ratelimit@test.com',
                            password: 'password'
                        })
                );
            }
            
            const rateLimitResponses = await Promise.all(rateLimitPromises);
            const rateLimited = rateLimitResponses.some(res => res.status === 429);
            
            this.recordTestResult(testResult, 'Rate Limiting', {
                passed: rateLimited,
                expected: true,
                actual: rateLimited,
                details: { responses: rateLimitResponses.map(r => r.status) }
            });
            
            // Test 6.4: CORS headers
            const corsResponse = await request(app)
                .get('/api/v1/programs')
                .set('Origin', 'http://localhost:3000')
                .send({});
            
            const hasCorsHeaders = corsResponse.headers['access-control-allow-origin'] !== undefined;
            
            this.recordTestResult(testResult, 'CORS Headers', {
                passed: hasCorsHeaders,
                expected: true,
                actual: hasCorsHeaders,
                details: { headers: corsResponse.headers }
            });
            
        } catch (error) {
            testResult.issues.push(`Security validation test error: ${error.message}`);
        }
        
        this.testResults.push(testResult);
    }
    
    /**
     * Test error handling
     */
    async testErrorHandling() {
        logger.info('Testing error handling');
        
        const testResult = {
            testName: 'Error Handling',
            tests: [],
            passed: 0,
            failed: 0,
            issues: []
        };
        
        try {
            // Test 7.1: Malformed JSON
            const malformedResponse = await request(app)
                .post('/api/v1/auth/login')
                .set('Content-Type', 'application/json')
                .send('{"email": "test@test.com", "password": "password"');
            
            this.recordTestResult(testResult, 'Malformed JSON', {
                passed: malformedResponse.status === 400,
                expected: 400,
                actual: malformedResponse.status,
                details: malformedResponse.body
            });
            
            // Test 7.2: Large payload
            const largePayload = {
                email: 'large@test.com',
                password: 'LargePass123!',
                firstName: 'A'.repeat(10000),
                lastName: 'User'
            };
            
            const largePayloadResponse = await request(app)
                .post('/api/v1/auth/register')
                .send(largePayload);
            
            this.recordTestResult(testResult, 'Large Payload', {
                passed: [200, 400, 413].includes(largePayloadResponse.status),
                expected: [200, 400, 413],
                actual: largePayloadResponse.status,
                details: largePayloadResponse.body
            });
            
            // Test 7.3: Non-existent endpoint
            const nonExistentResponse = await request(app)
                .get('/api/v1/non-existent-endpoint')
                .send({});
            
            this.recordTestResult(testResult, 'Non-existent Endpoint', {
                passed: nonExistentResponse.status === 404,
                expected: 404,
                actual: nonExistentResponse.status,
                details: nonExistentResponse.body
            });
            
            // Test 7.4: Invalid HTTP method
            const invalidMethodResponse = await request(app)
                .patch('/api/v1/auth/login')
                .send({});
            
            this.recordTestResult(testResult, 'Invalid HTTP Method', {
                passed: invalidMethodResponse.status === 404 || invalidMethodResponse.status === 405,
                expected: [404, 405],
                actual: invalidMethodResponse.status,
                details: invalidMethodResponse.body
            });
            
        } catch (error) {
            testResult.issues.push(`Error handling test error: ${error.message}`);
        }
        
        this.testResults.push(testResult);
    }
    
    /**
     * Record test result
     */
    recordTestResult(testResult, testName, result) {
        testResult.tests.push({
            testName,
            passed: result.passed,
            expected: result.expected,
            actual: result.actual,
            details: result.details
        });
        
        if (result.passed) {
            testResult.passed++;
        } else {
            testResult.failed++;
        }
    }
    
    /**
     * Create test user
     */
    async createTestUser() {
        const userData = {
            email: `testuser${Date.now()}@test.com`,
            password: 'TestPass123!',
            firstName: 'Test',
            lastName: 'User'
        };
        
        const registerResponse = await request(app)
            .post('/api/v1/auth/register')
            .send(userData);
        
        if (registerResponse.status === 201) {
            const loginResponse = await request(app)
                .post('/api/v1/auth/login')
                .send({
                    email: userData.email,
                    password: userData.password
                });
            
            if (loginResponse.status === 200) {
                this.testUsers.push({
                    email: userData.email,
                    password: userData.password,
                    userId: registerResponse.body.data.userId,
                    token: loginResponse.body.data.token
                });
            }
        }
    }
    
    /**
     * Create admin user (simplified for testing)
     */
    async createAdminUser() {
        // In a real test environment, this would create an actual admin user
        // For now, return null to indicate admin testing is not available
        return null;
    }
    
    /**
     * Get valid token
     */
    async getValidToken() {
        if (this.testUsers.length === 0) {
            await this.createTestUser();
        }
        
        const user = this.testUsers[0];
        if (user && user.token) {
            return user.token;
        }
        
        // Fallback: login and get token
        const loginResponse = await request(app)
            .post('/api/v1/auth/login')
            .send({
                email: user.email,
                password: user.password
            });
        
        return loginResponse.body.data.token;
    }
    
    /**
     * Generate comprehensive auth test report
     */
    generateAuthTestReport() {
        const report = {
            timestamp: new Date().toISOString(),
            testEnvironment: process.env.NODE_ENV || 'development',
            summary: {
                totalTests: this.testResults.reduce((sum, result) => sum + result.tests.length, 0),
                totalPassed: this.testResults.reduce((sum, result) => sum + result.passed, 0),
                totalFailed: this.testResults.reduce((sum, result) => sum + result.failed, 0),
                passRate: 0
            },
            testResults: this.testResults,
            securityAnalysis: this.analyzeSecurity(),
            recommendations: this.generateAuthRecommendations()
        };
        
        // Calculate pass rate
        if (report.summary.totalTests > 0) {
            report.summary.passRate = (report.summary.totalPassed / report.summary.totalTests) * 100;
        }
        
        // Log report
        logger.info('Authentication validation completed', {
            summary: report.summary,
            recommendations: report.recommendations
        });
        
        // Save report to file
        const fs = require('fs');
        const path = require('path');
        const reportPath = path.join(__dirname, '../reports/auth-validation-report.json');
        
        fs.writeFileSync(reportPath, JSON.stringify(report, null, 2));
        logger.info(`Auth validation report saved to ${reportPath}`);
        
        return report;
    }
    
    /**
     * Analyze security aspects
     */
    analyzeSecurity() {
        const analysis = {
            securityScore: 0,
            vulnerabilities: [],
            strengths: []
        };
        
        this.testResults.forEach(testResult => {
            testResult.tests.forEach(test => {
                if (test.testName.includes('SQL Injection') && test.passed) {
                    analysis.strengths.push('SQL injection protection');
                    analysis.securityScore += 20;
                }
                
                if (test.testName.includes('XSS') && test.passed) {
                    analysis.strengths.push('XSS protection');
                    analysis.securityScore += 20;
                }
                
                if (test.testName.includes('Rate Limiting') && test.passed) {
                    analysis.strengths.push('Rate limiting');
                    analysis.securityScore += 15;
                }
                
                if (test.testName.includes('Token') && test.passed) {
                    analysis.strengths.push('Token security');
                    analysis.securityScore += 15;
                }
                
                if (test.testName.includes('CORS') && test.passed) {
                    analysis.strengths.push('CORS configuration');
                    analysis.securityScore += 10;
                }
                
                if (!test.passed) {
                    analysis.vulnerabilities.push({
                        test: test.testName,
                        issue: `Failed security test: ${test.testName}`,
                        severity: 'HIGH'
                    });
                }
            });
        });
        
        return analysis;
    }
    
    /**
     * Generate authentication recommendations
     */
    generateAuthRecommendations() {
        const recommendations = [];
        const analysis = this.analyzeSecurity();
        
        // Security score recommendations
        if (analysis.securityScore < 80) {
            recommendations.push({
                priority: 'HIGH',
                category: 'SECURITY',
                issue: 'Security score below 80%',
                recommendation: 'Review and fix security vulnerabilities',
                action: 'Address failed security tests immediately'
            });
        }
        
        // Vulnerability recommendations
        analysis.vulnerabilities.forEach(vuln => {
            recommendations.push({
                priority: 'CRITICAL',
                category: 'VULNERABILITY',
                issue: vuln.issue,
                recommendation: 'Fix security vulnerability',
                action: `Address ${vuln.test} immediately`
            });
        });
        
        // General recommendations
        if (analysis.strengths.length < 5) {
            recommendations.push({
                priority: 'MEDIUM',
                category: 'ENHANCEMENT',
                issue: 'Missing security features',
                recommendation: 'Implement comprehensive security measures',
                action: 'Add missing security protections'
            });
        }
        
        return recommendations;
    }
}

// Export for use in test runner
module.exports = AuthValidator;

// Run tests if this file is executed directly
if (require.main === module) {
    const authValidator = new AuthValidator();
    authValidator.runAuthTests()
        .then(() => {
            console.log('Authentication validation completed successfully');
            process.exit(0);
        })
        .catch((error) => {
            console.error('Authentication validation failed:', error);
            process.exit(1);
        });
}
