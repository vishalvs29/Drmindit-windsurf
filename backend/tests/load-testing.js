const request = require('supertest');
const app = require('../src/app').getApp();
const { logger } = require('../src/utils/logger');
const { cache } = require('../src/utils/cache');

/**
 * Load Testing Suite - API Stress and Scale Testing
 * Comprehensive load testing for production readiness
 */

class LoadTester {
    constructor() {
        this.baseURL = process.env.TEST_BASE_URL || 'http://localhost:3000';
        this.testResults = [];
        this.concurrentUsers = 0;
        this.maxConcurrentUsers = 1000;
        this.testDuration = 60000; // 60 seconds per test
        this.rampUpTime = 10000; // 10 seconds ramp up
    }
    
    /**
     * Run comprehensive load tests
     */
    async runLoadTests() {
        logger.info('Starting comprehensive load testing', {
            baseURL: this.baseURL,
            maxConcurrentUsers: this.maxConcurrentUsers,
            testDuration: this.testDuration
        });
        
        try {
            // Clear cache before tests
            await cache.clear();
            
            // Test 1: Normal Load (50-100 concurrent users)
            await this.runLoadTest('Normal Load', 75, 60000);
            
            // Test 2: Peak Load (500-1000 concurrent users)
            await this.runLoadTest('Peak Load', 750, 60000);
            
            // Test 3: Spike Traffic (sudden burst)
            await this.runSpikeTest();
            
            // Test 4: Endurance Test (sustained load)
            await this.runEnduranceTest();
            
            // Generate comprehensive report
            this.generateLoadTestReport();
            
        } catch (error) {
            logger.error('Load testing failed', { error: error.message, stack: error.stack });
            throw error;
        }
    }
    
    /**
     * Run load test with specified concurrent users
     */
    async runLoadTest(testName, concurrentUsers, duration) {
        logger.info(`Starting ${testName} test`, { concurrentUsers, duration });
        
        const testResult = {
            testName,
            concurrentUsers,
            duration,
            startTime: Date.now(),
            requests: [],
            errors: [],
            responseTimes: [],
            throughput: 0,
            errorRate: 0,
            p95ResponseTime: 0,
            p99ResponseTime: 0
        };
        
        try {
            // Create user pool
            const users = await this.createTestUsers(concurrentUsers);
            
            // Ramp up users gradually
            await this.rampUpUsers(users, testResult);
            
            // Run test for specified duration
            await this.runTestDuration(users, duration, testResult);
            
            // Calculate metrics
            this.calculateTestMetrics(testResult);
            
            this.testResults.push(testResult);
            
            logger.info(`${testName} test completed`, {
                totalRequests: testResult.requests.length,
                throughput: testResult.throughput,
                errorRate: testResult.errorRate,
                p95ResponseTime: testResult.p95ResponseTime,
                p99ResponseTime: testResult.p99ResponseTime
            });
            
        } catch (error) {
            logger.error(`${testName} test failed`, { error: error.message });
            testResult.errors.push({ type: 'TEST_ERROR', message: error.message });
        }
        
        return testResult;
    }
    
    /**
     * Run spike traffic test
     */
    async runSpikeTest() {
        logger.info('Starting spike traffic test');
        
        const testResult = {
            testName: 'Spike Traffic',
            concurrentUsers: 0,
            duration: 30000,
            startTime: Date.now(),
            requests: [],
            errors: [],
            responseTimes: [],
            throughput: 0,
            errorRate: 0,
            p95ResponseTime: 0,
            p99ResponseTime: 0
        };
        
        try {
            // Start with 10 users
            const initialUsers = await this.createTestUsers(10);
            await this.rampUpUsers(initialUsers, testResult);
            
            // Spike to 500 users suddenly
            logger.info('Spiking to 500 concurrent users');
            const spikeUsers = await this.createTestUsers(490);
            await this.rampUpUsers(spikeUsers, testResult, 1000); // 1 second ramp up
            
            // Maintain spike for 20 seconds
            await this.runTestDuration([...initialUsers, ...spikeUsers], 20000, testResult);
            
            // Calculate metrics
            this.calculateTestMetrics(testResult);
            this.testResults.push(testResult);
            
            logger.info('Spike traffic test completed', {
                maxConcurrentUsers: 500,
                throughput: testResult.throughput,
                errorRate: testResult.errorRate,
                p95ResponseTime: testResult.p95ResponseTime
            });
            
        } catch (error) {
            logger.error('Spike traffic test failed', { error: error.message });
            testResult.errors.push({ type: 'TEST_ERROR', message: error.message });
        }
        
        return testResult;
    }
    
    /**
     * Run endurance test (sustained load)
     */
    async runEnduranceTest() {
        logger.info('Starting endurance test (5 minutes sustained load)');
        
        const testResult = {
            testName: 'Endurance Test',
            concurrentUsers: 100,
            duration: 300000, // 5 minutes
            startTime: Date.now(),
            requests: [],
            errors: [],
            responseTimes: [],
            throughput: 0,
            errorRate: 0,
            p95ResponseTime: 0,
            p99ResponseTime: 0
        };
        
        try {
            const users = await this.createTestUsers(100);
            await this.rampUpUsers(users, testResult);
            
            // Run sustained test
            await this.runTestDuration(users, 300000, testResult);
            
            // Calculate metrics
            this.calculateTestMetrics(testResult);
            this.testResults.push(testResult);
            
            logger.info('Endurance test completed', {
                duration: '5 minutes',
                totalRequests: testResult.requests.length,
                throughput: testResult.throughput,
                errorRate: testResult.errorRate
            });
            
        } catch (error) {
            logger.error('Endurance test failed', { error: error.message });
            testResult.errors.push({ type: 'TEST_ERROR', message: error.message });
        }
        
        return testResult;
    }
    
    /**
     * Create test users with authentication
     */
    async createTestUsers(count) {
        const users = [];
        
        for (let i = 0; i < count; i++) {
            const userData = {
                email: `testuser${i}@loadtest.com`,
                password: 'LoadTest123!',
                firstName: 'Test',
                lastName: `User${i}`
            };
            
            try {
                // Register user
                const registerResponse = await request(app)
                    .post('/api/v1/auth/register')
                    .send(userData);
                
                if (registerResponse.status === 201) {
                    // Login user
                    const loginResponse = await request(app)
                        .post('/api/v1/auth/login')
                        .send({
                            email: userData.email,
                            password: userData.password
                        });
                    
                    if (loginResponse.status === 200) {
                        users.push({
                            id: registerResponse.body.data.userId,
                            email: userData.email,
                            token: loginResponse.body.data.token,
                            authCookie: loginResponse.headers['set-cookie']?.[0]
                        });
                    }
                }
            } catch (error) {
                logger.warn(`Failed to create test user ${i}`, { error: error.message });
            }
        }
        
        logger.info(`Created ${users.length} test users`);
        return users;
    }
    
    /**
     * Ramp up users gradually
     */
    async rampUpUsers(users, testResult, rampUpDuration = this.rampUpTime) {
        const rampUpInterval = rampUpDuration / users.length;
        
        for (let i = 0; i < users.length; i++) {
            await new Promise(resolve => setTimeout(resolve, rampUpInterval));
            this.startUserRequests(users[i], testResult);
        }
    }
    
    /**
     * Start making requests for a user
     */
    startUserRequests(user, testResult) {
        const endpoints = [
            { method: 'GET', path: '/api/v1/programs' },
            { method: 'GET', path: '/api/v1/users/profile' },
            { method: 'POST', path: '/api/v1/users/insights', data: { mood: 7, stress: 3 } },
            { method: 'GET', path: '/api/v1/analytics/overview' }
        ];
        
        const makeRequest = async () => {
            const endpoint = endpoints[Math.floor(Math.random() * endpoints.length)];
            const startTime = Date.now();
            
            try {
                let req = request(app);
                
                // Set authentication
                if (user.authCookie) {
                    req = req.set('Cookie', user.authCookie);
                } else {
                    req = req.set('Authorization', `Bearer ${user.token}`);
                }
                
                // Make request
                const response = await req[endpoint.method.toLowerCase()](endpoint.path)
                    .send(endpoint.data || {});
                
                const responseTime = Date.now() - startTime;
                
                testResult.requests.push({
                    timestamp: startTime,
                    endpoint: endpoint.path,
                    method: endpoint.method,
                    statusCode: response.status,
                    responseTime,
                    success: response.status < 400
                });
                
                testResult.responseTimes.push(responseTime);
                
            } catch (error) {
                const responseTime = Date.now() - startTime;
                
                testResult.requests.push({
                    timestamp: startTime,
                    endpoint: endpoint.path,
                    method: endpoint.method,
                    statusCode: 0,
                    responseTime,
                    success: false,
                    error: error.message
                });
                
                testResult.errors.push({
                    timestamp: startTime,
                    endpoint: endpoint.path,
                    error: error.message
                });
            }
        };
        
        // Make requests continuously
        const requestInterval = setInterval(() => {
            makeRequest();
        }, 1000 + Math.random() * 2000); // 1-3 seconds between requests
        
        // Store interval for cleanup
        user.requestInterval = requestInterval;
    }
    
    /**
     * Run test for specified duration
     */
    async runTestDuration(users, duration, testResult) {
        return new Promise(resolve => {
            setTimeout(() => {
                // Stop all user requests
                users.forEach(user => {
                    if (user.requestInterval) {
                        clearInterval(user.requestInterval);
                    }
                });
                resolve();
            }, duration);
        });
    }
    
    /**
     * Calculate test metrics
     */
    calculateTestMetrics(testResult) {
        const { requests, responseTimes } = testResult;
        
        // Calculate throughput (requests per second)
        const actualDuration = Date.now() - testResult.startTime;
        testResult.throughput = (requests.length / actualDuration) * 1000;
        
        // Calculate error rate
        const failedRequests = requests.filter(req => !req.success);
        testResult.errorRate = (failedRequests.length / requests.length) * 100;
        
        // Calculate percentiles
        const sortedTimes = responseTimes.sort((a, b) => a - b);
        const p95Index = Math.floor(sortedTimes.length * 0.95);
        const p99Index = Math.floor(sortedTimes.length * 0.99);
        
        testResult.p95ResponseTime = sortedTimes[p95Index] || 0;
        testResult.p99ResponseTime = sortedTimes[p99Index] || 0;
        
        // Calculate average response time
        testResult.avgResponseTime = responseTimes.reduce((sum, time) => sum + time, 0) / responseTimes.length || 0;
        
        // Identify slow endpoints
        const endpointStats = {};
        requests.forEach(req => {
            if (!endpointStats[req.endpoint]) {
                endpointStats[req.endpoint] = {
                    count: 0,
                    totalTime: 0,
                    errors: 0
                };
            }
            endpointStats[req.endpoint].count++;
            endpointStats[req.endpoint].totalTime += req.responseTime;
            if (!req.success) {
                endpointStats[req.endpoint].errors++;
            }
        });
        
        testResult.slowEndpoints = Object.entries(endpointStats)
            .map(([endpoint, stats]) => ({
                endpoint,
                avgResponseTime: stats.totalTime / stats.count,
                errorRate: (stats.errors / stats.count) * 100,
                requestCount: stats.count
            }))
            .sort((a, b) => b.avgResponseTime - a.avgResponseTime)
            .slice(0, 5); // Top 5 slowest endpoints
    }
    
    /**
     * Generate comprehensive load test report
     */
    generateLoadTestReport() {
        const report = {
            timestamp: new Date().toISOString(),
            testEnvironment: process.env.NODE_ENV || 'development',
            baseURL: this.baseURL,
            summary: {
                totalTests: this.testResults.length,
                totalRequests: this.testResults.reduce((sum, test) => sum + test.requests.length, 0),
                totalErrors: this.testResults.reduce((sum, test) => sum + test.errors.length, 0)
            },
            testResults: this.testResults,
            performanceAnalysis: this.analyzePerformance(),
            recommendations: this.generateRecommendations()
        };
        
        // Log report
        logger.info('Load testing completed', {
            summary: report.summary,
            recommendations: report.recommendations
        });
        
        // Save report to file
        const fs = require('fs');
        const path = require('path');
        const reportPath = path.join(__dirname, '../reports/load-test-report.json');
        
        fs.writeFileSync(reportPath, JSON.stringify(report, null, 2));
        logger.info(`Load test report saved to ${reportPath}`);
        
        return report;
    }
    
    /**
     * Analyze performance across all tests
     */
    analyzePerformance() {
        const analysis = {
            overallThroughput: 0,
            overallErrorRate: 0,
            slowestEndpoints: [],
            performanceIssues: []
        };
        
        if (this.testResults.length === 0) return analysis;
        
        // Calculate overall metrics
        const totalRequests = this.testResults.reduce((sum, test) => sum + test.requests.length, 0);
        const totalErrors = this.testResults.reduce((sum, test) => sum + test.errors.length, 0);
        analysis.overallThroughput = this.testResults.reduce((sum, test) => sum + test.throughput, 0) / this.testResults.length;
        analysis.overallErrorRate = (totalErrors / totalRequests) * 100;
        
        // Identify consistently slow endpoints
        const endpointPerformance = {};
        this.testResults.forEach(test => {
            if (test.slowEndpoints) {
                test.slowEndpoints.forEach(endpoint => {
                    if (!endpointPerformance[endpoint.endpoint]) {
                        endpointPerformance[endpoint.endpoint] = {
                            avgResponseTime: 0,
                            errorRate: 0,
                            testCount: 0
                        };
                    }
                    endpointPerformance[endpoint.endpoint].avgResponseTime += endpoint.avgResponseTime;
                    endpointPerformance[endpoint.endpoint].errorRate += endpoint.errorRate;
                    endpointPerformance[endpoint.endpoint].testCount++;
                });
            }
        });
        
        analysis.slowestEndpoints = Object.entries(endpointPerformance)
            .map(([endpoint, stats]) => ({
                endpoint,
                avgResponseTime: stats.avgResponseTime / stats.testCount,
                avgErrorRate: stats.errorRate / stats.testCount,
                testCount: stats.testCount
            }))
            .sort((a, b) => b.avgResponseTime - a.avgResponseTime)
            .slice(0, 10);
        
        // Identify performance issues
        if (analysis.overallErrorRate > 5) {
            analysis.performanceIssues.push('High error rate detected (>5%)');
        }
        
        if (analysis.overallThroughput < 100) {
            analysis.performanceIssues.push('Low throughput detected (<100 req/sec)');
        }
        
        const slowEndpoints = analysis.slowestEndpoints.filter(ep => ep.avgResponseTime > 1000);
        if (slowEndpoints.length > 0) {
            analysis.performanceIssues.push(`${slowEndpoints.length} endpoints with slow response times (>1s)`);
        }
        
        return analysis;
    }
    
    /**
     * Generate optimization recommendations
     */
    generateRecommendations() {
        const recommendations = [];
        const analysis = this.analyzePerformance();
        
        // Error rate recommendations
        if (analysis.overallErrorRate > 5) {
            recommendations.push({
                priority: 'HIGH',
                category: 'ERROR_HANDLING',
                issue: 'High error rate detected',
                recommendation: 'Investigate error patterns and improve error handling',
                action: 'Review error logs and implement better error recovery'
            });
        }
        
        // Throughput recommendations
        if (analysis.overallThroughput < 100) {
            recommendations.push({
                priority: 'HIGH',
                category: 'PERFORMANCE',
                issue: 'Low throughput',
                recommendation: 'Optimize database queries and add caching',
                action: 'Implement Redis caching for frequently accessed data'
            });
        }
        
        // Slow endpoint recommendations
        analysis.slowestEndpoints.forEach(endpoint => {
            if (endpoint.avgResponseTime > 1000) {
                recommendations.push({
                    priority: 'MEDIUM',
                    category: 'OPTIMIZATION',
                    issue: `Slow endpoint: ${endpoint.endpoint}`,
                    recommendation: 'Optimize database queries and add caching',
                    action: `Review and optimize ${endpoint.endpoint} implementation`
                });
            }
        });
        
        // General recommendations
        if (analysis.performanceIssues.length === 0) {
            recommendations.push({
                priority: 'LOW',
                category: 'GENERAL',
                issue: 'No major performance issues detected',
                recommendation: 'Continue monitoring and consider load testing at higher scales',
                action: 'Implement automated performance monitoring'
            });
        }
        
        return recommendations;
    }
}

// Export for use in test runner
module.exports = LoadTester;

// Run tests if this file is executed directly
if (require.main === module) {
    const loadTester = new LoadTester();
    loadTester.runLoadTests()
        .then(() => {
            console.log('Load testing completed successfully');
            process.exit(0);
        })
        .catch((error) => {
            console.error('Load testing failed:', error);
            process.exit(1);
        });
}
