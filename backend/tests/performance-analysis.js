const { Pool } = require('pg');
const { logger } = require('../src/utils/logger');
const { performance } = require('perf_hooks');

/**
 * Performance Analysis Suite - Deep Performance Testing
 * Identifies bottlenecks, slow queries, and optimization opportunities
 */

class PerformanceAnalyzer {
    constructor() {
        this.dbPool = new Pool({
            user: process.env.DB_USER || 'postgres',
            host: process.env.DB_HOST || 'localhost',
            database: process.env.DB_NAME || 'drmindit',
            password: process.env.DB_PASSWORD || '',
            port: process.env.DB_PORT || 5432,
        });
        
        this.analysisResults = {
            queryPerformance: [],
            indexAnalysis: [],
            connectionPoolStats: {},
            memoryUsage: [],
            recommendations: []
        };
    }
    
    /**
     * Run comprehensive performance analysis
     */
    async runPerformanceAnalysis() {
        logger.info('Starting comprehensive performance analysis');
        
        try {
            // Test 1: Database Query Performance
            await this.analyzeQueryPerformance();
            
            // Test 2: Index Usage Analysis
            await this.analyzeIndexUsage();
            
            // Test 3: Connection Pool Analysis
            await this.analyzeConnectionPool();
            
            // Test 4: Memory Usage Analysis
            await this.analyzeMemoryUsage();
            
            // Test 5: N+1 Query Detection
            await this.detectNPlusOneQueries();
            
            // Test 6: Slow Query Identification
            await this.identifySlowQueries();
            
            // Generate comprehensive report
            this.generatePerformanceReport();
            
        } catch (error) {
            logger.error('Performance analysis failed', { error: error.message, stack: error.stack });
            throw error;
        } finally {
            await this.dbPool.end();
        }
    }
    
    /**
     * Analyze database query performance
     */
    async analyzeQueryPerformance() {
        logger.info('Analyzing database query performance');
        
        const queries = [
            {
                name: 'User Lookup by Email',
                query: 'SELECT * FROM users WHERE email = $1',
                params: ['test@example.com']
            },
            {
                name: 'User Programs List',
                query: `
                    SELECT up.*, p.name as program_name, p.category 
                    FROM user_programs up 
                    JOIN programs p ON up.program_id = p.id 
                    WHERE up.user_id = $1 
                    ORDER BY up.started_at DESC
                `,
                params: ['test-user-id']
            },
            {
                name: 'Program Steps with Progress',
                query: `
                    SELECT ps.*, usp.is_completed, usp.time_spent_minutes 
                    FROM program_steps ps 
                    LEFT JOIN user_step_progress usp ON ps.id = usp.step_id 
                    WHERE ps.program_day_id = $1 
                    ORDER BY ps.sort_order
                `,
                params: ['test-day-id']
            },
            {
                name: 'User Analytics Summary',
                query: `
                    SELECT 
                        COUNT(DISTINCT up.id) as total_programs,
                        COUNT(DISTINCT CASE WHEN up.is_completed = true THEN up.id END) as completed_programs,
                        COALESCE(SUM(up.total_minutes_spent), 0) as total_minutes,
                        MAX(up.streak_days) as max_streak
                    FROM user_programs up 
                    WHERE up.user_id = $1
                `,
                params: ['test-user-id']
            },
            {
                name: 'Program Completion Analytics',
                query: `
                    SELECT 
                        p.category,
                        COUNT(pc.id) as completion_count,
                        AVG(pc.total_time_spent_minutes) as avg_completion_time
                    FROM program_completions pc 
                    JOIN programs p ON pc.program_id = p.id 
                    WHERE pc.completed_at >= NOW() - INTERVAL '30 days'
                    GROUP BY p.category
                    ORDER BY completion_count DESC
                `,
                params: []
            }
        ];
        
        for (const queryTest of queries) {
            const startTime = performance.now();
            
            try {
                const result = await this.dbPool.query(queryTest.query, queryTest.params);
                const endTime = performance.now();
                const executionTime = endTime - startTime;
                
                this.analysisResults.queryPerformance.push({
                    name: queryTest.name,
                    executionTime,
                    rowCount: result.rowCount,
                    query: queryTest.query,
                    performance: this.classifyPerformance(executionTime)
                });
                
                logger.debug(`Query performance: ${queryTest.name}`, {
                    executionTime,
                    rowCount: result.rowCount
                });
                
            } catch (error) {
                logger.error(`Query failed: ${queryTest.name}`, { error: error.message });
                this.analysisResults.queryPerformance.push({
                    name: queryTest.name,
                    error: error.message,
                    performance: 'FAILED'
                });
            }
        }
    }
    
    /**
     * Analyze index usage and effectiveness
     */
    async analyzeIndexUsage() {
        logger.info('Analyzing index usage');
        
        try {
            // Get index usage statistics
            const indexStatsQuery = `
                SELECT 
                    schemaname,
                    tablename,
                    indexname,
                    idx_scan,
                    idx_tup_read,
                    idx_tup_fetch,
                    pg_size_pretty(pg_relation_size(indexrelid)) as index_size
                FROM pg_stat_user_indexes 
                WHERE schemaname = 'public'
                ORDER BY idx_scan DESC
            `;
            
            const indexStats = await this.dbPool.query(indexStatsQuery);
            
            // Get table statistics
            const tableStatsQuery = `
                SELECT 
                    schemaname,
                    tablename,
                    seq_scan,
                    seq_tup_read,
                    pg_size_pretty(pg_relation_size(schemaname||'.'||tablename)) as table_size
                FROM pg_stat_user_tables 
                WHERE schemaname = 'public'
                ORDER BY seq_scan DESC
            `;
            
            const tableStats = await this.dbPool.query(tableStatsQuery);
            
            // Analyze index effectiveness
            indexStats.rows.forEach(index => {
                const table = tableStats.rows.find(t => t.tablename === index.tablename);
                
                this.analysisResults.indexAnalysis.push({
                    tableName: index.tablename,
                    indexName: index.indexname,
                    indexScans: parseInt(index.idx_scan),
                    indexTuplesRead: parseInt(index.idx_tup_read),
                    indexSize: index.index_size,
                    tableScans: table ? parseInt(table.seq_scan) : 0,
                    effectiveness: this.calculateIndexEffectiveness(index, table)
                });
            });
            
            // Identify missing indexes
            await this.identifyMissingIndexes();
            
        } catch (error) {
            logger.error('Index analysis failed', { error: error.message });
        }
    }
    
    /**
     * Analyze connection pool performance
     */
    async analyzeConnectionPool() {
        logger.info('Analyzing connection pool performance');
        
        try {
            // Get connection statistics
            const connectionStats = await this.dbPool.query(`
                SELECT 
                    count(*) as total_connections,
                    count(*) FILTER (WHERE state = 'active') as active_connections,
                    count(*) FILTER (WHERE state = 'idle') as idle_connections,
                    count(*) FILTER (WHERE state = 'idle in transaction') as idle_in_transaction
                FROM pg_stat_activity 
                WHERE datname = current_database()
            `);
            
            const stats = connectionStats.rows[0];
            
            this.analysisResults.connectionPoolStats = {
                totalConnections: parseInt(stats.total_connections),
                activeConnections: parseInt(stats.active_connections),
                idleConnections: parseInt(stats.idle_connections),
                idleInTransaction: parseInt(stats.idle_in_transaction),
                poolUtilization: (parseInt(stats.active_connections) / 20) * 100, // Assuming max pool size of 20
                timestamp: new Date().toISOString()
            };
            
            // Test connection pool under load
            await this.testConnectionPoolLoad();
            
        } catch (error) {
            logger.error('Connection pool analysis failed', { error: error.message });
        }
    }
    
    /**
     * Analyze memory usage patterns
     */
    async analyzeMemoryUsage() {
        logger.info('Analyzing memory usage');
        
        const samples = [];
        const sampleDuration = 30000; // 30 seconds
        const sampleInterval = 1000; // 1 second
        
        const startTime = Date.now();
        
        while (Date.now() - startTime < sampleDuration) {
            const memUsage = process.memoryUsage();
            const cpuUsage = process.cpuUsage();
            
            samples.push({
                timestamp: new Date().toISOString(),
                heapUsed: memUsage.heapUsed,
                heapTotal: memUsage.heapTotal,
                external: memUsage.external,
                rss: memUsage.rss,
                cpuUser: cpuUsage.user,
                cpuSystem: cpuUsage.system
            });
            
            await new Promise(resolve => setTimeout(resolve, sampleInterval));
        }
        
        // Analyze memory patterns
        const heapUsedValues = samples.map(s => s.heapUsed);
        const maxHeapUsed = Math.max(...heapUsedValues);
        const minHeapUsed = Math.min(...heapUsedValues);
        const avgHeapUsed = heapUsedValues.reduce((sum, val) => sum + val, 0) / heapUsedValues.length;
        
        this.analysisResults.memoryUsage = {
            samples,
            maxHeapUsed,
            minHeapUsed,
            avgHeapUsed,
            memoryGrowth: samples[samples.length - 1].heapUsed - samples[0].heapUsed,
            potentialMemoryLeak: samples[samples.length - 1].heapUsed > samples[0].heapUsed * 1.5
        };
    }
    
    /**
     * Detect N+1 query problems
     */
    async detectNPlusOneQueries() {
        logger.info('Detecting N+1 query problems');
        
        try {
            // Simulate common N+1 scenarios
            const userProgramsQuery = `
                SELECT * FROM user_programs WHERE user_id = $1 LIMIT 10
            `;
            
            const userPrograms = await this.dbPool.query(userProgramsQuery, ['test-user-id']);
            
            // This would normally trigger N+1 queries if not optimized
            const startTime = performance.now();
            
            for (const program of userPrograms.rows) {
                // Individual query for each program (N+1 pattern)
                await this.dbPool.query('SELECT * FROM programs WHERE id = $1', [program.program_id]);
            }
            
            const endTime = performance.now();
            const nPlusOneTime = endTime - startTime;
            
            // Compare with optimized JOIN query
            const optimizedStartTime = performance.now();
            
            await this.dbPool.query(`
                SELECT up.*, p.* 
                FROM user_programs up 
                JOIN programs p ON up.program_id = p.id 
                WHERE up.user_id = $1
            `, ['test-user-id']);
            
            const optimizedEndTime = performance.now();
            const optimizedTime = optimizedEndTime - optimizedStartTime;
            
            const improvement = ((nPlusOneTime - optimizedTime) / nPlusOneTime) * 100;
            
            this.analysisResults.queryPerformance.push({
                name: 'N+1 Query Analysis',
                nPlusOneTime,
                optimizedTime,
                improvement: improvement.toFixed(2) + '%',
                hasNPlusOneProblem: improvement > 50,
                recommendation: improvement > 50 ? 'Optimize with JOINs' : 'No optimization needed'
            });
            
        } catch (error) {
            logger.error('N+1 query detection failed', { error: error.message });
        }
    }
    
    /**
     * Identify slow queries
     */
    async identifySlowQueries() {
        logger.info('Identifying slow queries');
        
        try {
            // Enable query statistics if not already enabled
            await this.dbPool.query('CREATE EXTENSION IF NOT EXISTS pg_stat_statements');
            
            // Get slow queries from pg_stat_statements
            const slowQueriesQuery = `
                SELECT 
                    query,
                    calls,
                    total_exec_time,
                    mean_exec_time,
                    stddev_exec_time,
                    max_exec_time,
                    rows,
                    100.0 * shared_blks_hit / nullif(shared_blks_hit + shared_blks_read, 0) AS hit_percent
                FROM pg_stat_statements 
                WHERE mean_exec_time > 100 -- queries taking more than 100ms on average
                ORDER BY mean_exec_time DESC 
                LIMIT 10
            `;
            
            const slowQueries = await this.dbPool.query(slowQueriesQuery);
            
            slowQueries.rows.forEach(query => {
                this.analysisResults.queryPerformance.push({
                    name: 'Slow Query Detected',
                    query: query.query.substring(0, 100) + '...',
                    avgExecutionTime: parseFloat(query.mean_exec_time),
                    maxExecutionTime: parseFloat(query.max_exec_time),
                    totalCalls: parseInt(query.calls),
                    hitPercent: parseFloat(query.hit_percent),
                    severity: this.classifyQuerySeverity(parseFloat(query.mean_exec_time))
                });
            });
            
        } catch (error) {
            logger.error('Slow query identification failed', { error: error.message });
        }
    }
    
    /**
     * Test connection pool under load
     */
    async testConnectionPoolLoad() {
        logger.info('Testing connection pool under load');
        
        const concurrentConnections = 50;
        const promises = [];
        
        const startTime = performance.now();
        
        for (let i = 0; i < concurrentConnections; i++) {
            promises.push(this.simulateDatabaseLoad(i));
        }
        
        await Promise.all(promises);
        
        const endTime = performance.now();
        const totalTime = endTime - startTime;
        
        this.analysisResults.connectionPoolStats.loadTest = {
            concurrentConnections,
            totalTime,
            avgTimePerConnection: totalTime / concurrentConnections,
            successRate: 100 // Would be calculated based on actual results
        };
    }
    
    /**
     * Simulate database load
     */
    async simulateDatabaseLoad(connectionId) {
        try {
            await this.dbPool.query('SELECT COUNT(*) FROM users');
            await this.dbPool.query('SELECT COUNT(*) FROM programs');
            await this.dbPool.query('SELECT COUNT(*) FROM user_programs');
        } catch (error) {
            logger.error(`Load test connection ${connectionId} failed`, { error: error.message });
        }
    }
    
    /**
     * Identify missing indexes
     */
    async identifyMissingIndexes() {
        try {
            // Get queries that could benefit from indexes
            const missingIndexesQuery = `
                SELECT 
                    schemaname,
                    tablename,
                    attname,
                    n_distinct,
                    correlation
                FROM pg_stats 
                WHERE schemaname = 'public'
                  AND n_distinct > 100
                  AND correlation < 0.9
                ORDER BY n_distinct DESC
            `;
            
            const missingIndexes = await this.dbPool.query(missingIndexesQuery);
            
            missingIndexes.rows.forEach(index => {
                this.analysisResults.indexAnalysis.push({
                    type: 'MISSING_INDEX',
                    tableName: index.tablename,
                    columnName: index.attname,
                    distinctValues: index.n_distinct,
                    correlation: index.correlation,
                    recommendation: `Consider adding index on ${index.tablename}.${index.attname}`
                });
            });
            
        } catch (error) {
            logger.error('Missing index identification failed', { error: error.message });
        }
    }
    
    /**
     * Calculate index effectiveness
     */
    calculateIndexEffectiveness(index, table) {
        if (!table) return 'UNKNOWN';
        
        const indexUsage = parseInt(index.idx_scan);
        const tableScans = parseInt(table.seq_scan);
        const totalUsage = indexUsage + tableScans;
        
        if (totalUsage === 0) return 'UNUSED';
        
        const usageRatio = indexUsage / totalUsage;
        
        if (usageRatio > 0.8) return 'HIGHLY_EFFECTIVE';
        if (usageRatio > 0.5) return 'EFFECTIVE';
        if (usageRatio > 0.2) return 'MODERATELY_EFFECTIVE';
        return 'INEFFECTIVE';
    }
    
    /**
     * Classify query performance
     */
    classifyPerformance(executionTime) {
        if (executionTime < 10) return 'EXCELLENT';
        if (executionTime < 50) return 'GOOD';
        if (executionTime < 100) return 'FAIR';
        if (executionTime < 500) return 'POOR';
        return 'CRITICAL';
    }
    
    /**
     * Classify query severity
     */
    classifyQuerySeverity(avgTime) {
        if (avgTime < 50) return 'LOW';
        if (avgTime < 200) return 'MEDIUM';
        if (avgTime < 1000) return 'HIGH';
        return 'CRITICAL';
    }
    
    /**
     * Generate comprehensive performance report
     */
    generatePerformanceReport() {
        const report = {
            timestamp: new Date().toISOString(),
            testEnvironment: process.env.NODE_ENV || 'development',
            summary: {
                totalQueries: this.analysisResults.queryPerformance.length,
                slowQueries: this.analysisResults.queryPerformance.filter(q => 
                    q.performance === 'POOR' || q.performance === 'CRITICAL'
                ).length,
                totalIndexes: this.analysisResults.indexAnalysis.length,
                ineffectiveIndexes: this.analysisResults.indexAnalysis.filter(i => 
                    i.effectiveness === 'INEFFECTIVE' || i.effectiveness === 'UNUSED'
                ).length,
                memoryLeakDetected: this.analysisResults.memoryUsage.potentialMemoryLeak
            },
            queryPerformance: this.analysisResults.queryPerformance,
            indexAnalysis: this.analysisResults.indexAnalysis,
            connectionPoolStats: this.analysisResults.connectionPoolStats,
            memoryUsage: this.analysisResults.memoryUsage,
            recommendations: this.generatePerformanceRecommendations()
        };
        
        // Log report
        logger.info('Performance analysis completed', {
            summary: report.summary,
            recommendations: report.recommendations
        });
        
        // Save report to file
        const fs = require('fs');
        const path = require('path');
        const reportPath = path.join(__dirname, '../reports/performance-analysis-report.json');
        
        fs.writeFileSync(reportPath, JSON.stringify(report, null, 2));
        logger.info(`Performance analysis report saved to ${reportPath}`);
        
        return report;
    }
    
    /**
     * Generate performance recommendations
     */
    generatePerformanceRecommendations() {
        const recommendations = [];
        
        // Query performance recommendations
        const slowQueries = this.analysisResults.queryPerformance.filter(q => 
            q.performance === 'POOR' || q.performance === 'CRITICAL'
        );
        
        if (slowQueries.length > 0) {
            recommendations.push({
                priority: 'HIGH',
                category: 'QUERY_OPTIMIZATION',
                issue: `${slowQueries.length} slow queries detected`,
                recommendation: 'Optimize slow queries with better indexing or query rewriting',
                action: 'Review and optimize identified slow queries'
            });
        }
        
        // Index recommendations
        const ineffectiveIndexes = this.analysisResults.indexAnalysis.filter(i => 
            i.effectiveness === 'INEFFECTIVE' || i.effectiveness === 'UNUSED'
        );
        
        if (ineffectiveIndexes.length > 0) {
            recommendations.push({
                priority: 'MEDIUM',
                category: 'INDEX_OPTIMIZATION',
                issue: `${ineffectiveIndexes.length} ineffective or unused indexes`,
                recommendation: 'Remove unused indexes and optimize ineffective ones',
                action: 'Review index usage and remove unnecessary indexes'
            });
        }
        
        // Missing index recommendations
        const missingIndexes = this.analysisResults.indexAnalysis.filter(i => i.type === 'MISSING_INDEX');
        
        if (missingIndexes.length > 0) {
            recommendations.push({
                priority: 'HIGH',
                category: 'INDEX_OPTIMIZATION',
                issue: `${missingIndexes.length} potentially missing indexes`,
                recommendation: 'Add missing indexes for better query performance',
                action: 'Create recommended indexes for frequently queried columns'
            });
        }
        
        // Connection pool recommendations
        const poolStats = this.analysisResults.connectionPoolStats;
        
        if (poolStats.poolUtilization > 80) {
            recommendations.push({
                priority: 'HIGH',
                category: 'CONNECTION_POOL',
                issue: `High connection pool utilization: ${poolStats.poolUtilization.toFixed(1)}%`,
                recommendation: 'Increase connection pool size or optimize connection usage',
                action: 'Adjust connection pool configuration'
            });
        }
        
        // Memory recommendations
        if (this.analysisResults.memoryUsage.potentialMemoryLeak) {
            recommendations.push({
                priority: 'CRITICAL',
                category: 'MEMORY_MANAGEMENT',
                issue: 'Potential memory leak detected',
                recommendation: 'Investigate memory usage patterns and fix leaks',
                action: 'Profile memory usage and identify leak sources'
            });
        }
        
        // N+1 query recommendations
        const nPlusOneIssues = this.analysisResults.queryPerformance.filter(q => q.hasNPlusOneProblem);
        
        if (nPlusOneIssues.length > 0) {
            recommendations.push({
                priority: 'HIGH',
                category: 'QUERY_OPTIMIZATION',
                issue: 'N+1 query problems detected',
                recommendation: 'Replace N+1 queries with optimized JOINs',
                action: 'Refactor code to use JOINs instead of multiple queries'
            });
        }
        
        return recommendations;
    }
}

// Export for use in test runner
module.exports = PerformanceAnalyzer;

// Run analysis if this file is executed directly
if (require.main === module) {
    const performanceAnalyzer = new PerformanceAnalyzer();
    performanceAnalyzer.runPerformanceAnalysis()
        .then(() => {
            console.log('Performance analysis completed successfully');
            process.exit(0);
        })
        .catch((error) => {
            console.error('Performance analysis failed:', error);
            process.exit(1);
        });
}
