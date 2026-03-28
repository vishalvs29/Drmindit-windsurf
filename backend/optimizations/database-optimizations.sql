-- Database Performance Optimizations
-- Comprehensive optimizations for production performance

-- 1. Add missing indexes for frequently queried columns
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_email_active ON users(email) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_role_active ON users(role) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_created_at_active ON users(created_at) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_last_login_at_active ON users(last_login_at) WHERE deleted_at IS NULL;

-- 2. Program and user program optimization indexes
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_programs_category_active ON programs(category) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_programs_difficulty_active ON programs(difficulty) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_programs_target_audience_active ON programs(target_audience) WHERE deleted_at IS NULL;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_programs_is_active ON programs(is_active) WHERE deleted_at IS NULL;

-- 3. User programs optimization
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_user_programs_user_id_active ON user_programs(user_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_user_programs_program_id_active ON user_programs(program_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_user_programs_is_completed ON user_programs(is_completed);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_user_programs_started_at ON user_programs(started_at);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_user_programs_last_accessed_at ON user_programs(last_accessed_at);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_user_programs_user_completed ON user_programs(user_id, is_completed);

-- 4. Program days and steps optimization
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_program_days_program_id ON program_days(program_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_program_days_day_number ON program_days(day_number);
CREATE UNIQUE INDEX CONCURRENTLY IF NOT EXISTS idx_program_days_unique ON program_days(program_id, day_number);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_program_steps_program_day_id ON program_steps(program_day_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_program_steps_step_type ON program_steps(step_type);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_program_steps_sort_order ON program_steps(sort_order);

-- 5. User progress optimization
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_user_step_progress_user_program_id ON user_step_progress(user_program_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_user_step_progress_step_id ON user_step_progress(step_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_user_step_progress_is_completed ON user_step_progress(is_completed);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_user_step_progress_completed_at ON user_step_progress(completed_at);

-- 6. Program completions optimization
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_program_completions_user_id ON program_completions(user_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_program_completions_program_id ON program_completions(program_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_program_completions_completed_at ON program_completions(completed_at);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_program_completions_user_program_id ON program_completions(user_program_id);

-- 7. Composite indexes for common query patterns
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_user_programs_user_program_completed ON user_programs(user_id, program_id, is_completed);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_user_programs_user_started_at ON user_programs(user_id, started_at DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_user_step_progress_program_step_completed ON user_step_progress(user_program_id, step_id, is_completed);

-- 8. Analytics optimization indexes
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_program_completions_category_completed_at ON program_completions(program_id, completed_at);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_user_programs_completed_started_at ON user_programs(is_completed, started_at);

-- 9. Partial indexes for better performance
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_email_verified ON users(email) WHERE is_email_verified = true;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_user_programs_active ON user_programs(user_id) WHERE is_completed = false;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_program_steps_exercises ON program_steps(step_type) WHERE step_type IN ('GUIDED_EXERCISE', 'REFLECTION');

-- 10. Update table statistics for better query planning
ANALYZE users;
ANALYZE programs;
ANALYZE program_days;
ANALYZE program_steps;
ANALYZE user_programs;
ANALYZE user_step_progress;
ANALYZE program_completions;

-- 11. Create materialized views for complex analytics
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_user_program_stats AS
SELECT 
    u.id as user_id,
    u.email,
    COUNT(up.id) as total_programs,
    COUNT(CASE WHEN up.is_completed = true THEN 1 END) as completed_programs,
    COALESCE(SUM(up.total_minutes_spent), 0) as total_minutes_spent,
    MAX(up.streak_days) as max_streak_days,
    MAX(up.started_at) as last_program_start,
    MAX(up.last_accessed_at) as last_activity
FROM users u
LEFT JOIN user_programs up ON u.id = up.user_id
WHERE u.deleted_at IS NULL
GROUP BY u.id, u.email;

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_mv_user_program_stats_user_id ON mv_user_program_stats(user_id);

CREATE MATERIALIZED VIEW IF NOT EXISTS mv_program_analytics AS
SELECT 
    p.id as program_id,
    p.name as program_name,
    p.category,
    p.difficulty,
    COUNT(up.id) as enrollment_count,
    COUNT(CASE WHEN up.is_completed = true THEN 1 END) as completion_count,
    AVG(CASE WHEN up.is_completed = true THEN up.total_minutes_spent END) as avg_completion_time,
    MAX(up.streak_days) as max_streak_achieved,
    COUNT(DISTINCT up.user_id) as unique_users
FROM programs p
LEFT JOIN user_programs up ON p.id = up.program_id
WHERE p.deleted_at IS NULL
GROUP BY p.id, p.name, p.category, p.difficulty;

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_mv_program_analytics_category ON mv_program_analytics(category);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_mv_program_analytics_difficulty ON mv_program_analytics(difficulty);

-- 12. Create function to refresh materialized views
CREATE OR REPLACE FUNCTION refresh_analytics_views()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_user_program_stats;
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_program_analytics;
END;
$$ LANGUAGE plpgsql;

-- 13. Create trigger for automatic statistics update
CREATE OR REPLACE FUNCTION update_user_program_stats()
RETURNS trigger AS $$
BEGIN
    -- This trigger can be used to update cached statistics
    -- For now, we'll rely on scheduled refresh
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 14. Partition large tables for better performance (if needed)
-- This would be implemented for very large datasets
-- CREATE TABLE user_programs_partitioned (
--     LIKE user_programs INCLUDING ALL
-- ) PARTITION BY RANGE (started_at);

-- 15. Add table constraints for data integrity
ALTER TABLE users ADD CONSTRAINT chk_users_email_length 
    CHECK (length(email) >= 5 AND length(email) <= 255);

ALTER TABLE users ADD CONSTRAINT chk_users_name_length 
    CHECK (length(first_name) >= 1 AND length(first_name) <= 100 AND 
           length(last_name) >= 1 AND length(last_name) <= 100);

ALTER TABLE user_programs ADD CONSTRAINT chk_user_programs_current_day 
    CHECK (current_day >= 1);

ALTER TABLE user_programs ADD CONSTRAINT chk_user_programs_current_step 
    CHECK (current_step >= 0);

ALTER TABLE user_programs ADD CONSTRAINT chk_user_programs_total_minutes 
    CHECK (total_minutes_spent >= 0);

ALTER TABLE user_programs ADD CONSTRAINT chk_user_programs_streak_days 
    CHECK (streak_days >= 1);

ALTER TABLE program_steps ADD CONSTRAINT chk_program_steps_sort_order 
    CHECK (sort_order >= 0);

ALTER TABLE program_steps ADD CONSTRAINT chk_program_steps_estimated_minutes 
    CHECK (estimated_minutes >= 1 AND estimated_minutes <= 120);

-- 16. Create optimized queries for common operations
CREATE OR REPLACE FUNCTION get_user_program_details(p_user_id UUID)
RETURNS TABLE (
    program_id UUID,
    program_name VARCHAR,
    current_day INTEGER,
    current_step INTEGER,
    is_completed BOOLEAN,
    total_minutes_spent BIGINT,
    streak_days INTEGER,
    started_at TIMESTAMP WITH TIME ZONE,
    last_accessed_at TIMESTAMP WITH TIME ZONE
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        up.program_id,
        p.name,
        up.current_day,
        up.current_step,
        up.is_completed,
        up.total_minutes_spent,
        up.streak_days,
        up.started_at,
        up.last_accessed_at
    FROM user_programs up
    JOIN programs p ON up.program_id = p.id
    WHERE up.user_id = p_user_id
    ORDER BY up.started_at DESC;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_program_analytics(p_category VARCHAR DEFAULT NULL)
RETURNS TABLE (
    program_id UUID,
    program_name VARCHAR,
    category VARCHAR,
    difficulty VARCHAR,
    enrollment_count BIGINT,
    completion_count BIGINT,
    completion_rate DECIMAL,
    avg_completion_time DECIMAL,
    unique_users BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        pa.program_id,
        pa.program_name,
        pa.category,
        pa.difficulty,
        pa.enrollment_count,
        pa.completion_count,
        CASE 
            WHEN pa.enrollment_count > 0 THEN 
                ROUND((pa.completion_count::DECIMAL / pa.enrollment_count::DECIMAL) * 100, 2)
            ELSE 0 
        END as completion_rate,
        pa.avg_completion_time,
        pa.unique_users
    FROM mv_program_analytics pa
    WHERE (p_category IS NULL OR pa.category = p_category)
    ORDER BY pa.enrollment_count DESC;
END;
$$ LANGUAGE plpgsql;

-- 17. Create stored procedures for batch operations
CREATE OR REPLACE FUNCTION batch_update_user_progress(
    p_user_program_id UUID,
    p_step_progress JSONB
) RETURNS BOOLEAN AS $$
DECLARE
    step_record RECORD;
    step_id UUID;
    is_completed BOOLEAN;
    time_spent INTEGER;
    reflection_text TEXT;
BEGIN
    -- Loop through step progress data
    FOR step_record IN SELECT * FROM jsonb_each_text(p_step_progress)
    LOOP
        -- Parse step progress (assuming format: {"step_id": {"completed": true, "time": 30}})
        BEGIN
            -- This would need proper JSON parsing based on actual data structure
            -- For now, this is a placeholder for the concept
            
            -- Update user step progress
            UPDATE user_step_progress 
            SET 
                is_completed = true,
                time_spent_minutes = COALESCE(time_spent, 0),
                updated_at = NOW()
            WHERE user_program_id = p_user_program_id 
              AND step_id = step_id;
        END;
    END LOOP;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

-- 18. Create cleanup functions for maintenance
CREATE OR REPLACE FUNCTION cleanup_old_sessions()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    -- Delete sessions older than 30 days
    DELETE FROM user_sessions 
    WHERE created_at < NOW() - INTERVAL '30 days';
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION cleanup_expired_tokens()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    -- This would cleanup expired tokens from cache or token table
    -- For now, return 0 as placeholder
    RETURN 0;
END;
$$ LANGUAGE plpgsql;

-- 19. Add performance monitoring views
CREATE OR REPLACE VIEW v_slow_queries AS
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
ORDER BY mean_exec_time DESC;

CREATE OR REPLACE VIEW v_table_sizes AS
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size,
    pg_size_pretty(pg_relation_size(schemaname||'.'||tablename)) AS table_size,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename) - pg_relation_size(schemaname||'.'||tablename)) AS index_size
FROM pg_tables 
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- 20. Create scheduled job for statistics refresh
-- This would be called by a cron job or scheduler
CREATE OR REPLACE FUNCTION schedule_maintenance_tasks()
RETURNS void AS $$
BEGIN
    -- Refresh materialized views
    PERFORM refresh_analytics_views();
    
    -- Update table statistics
    ANALYZE;
    
    -- Log maintenance completion
    INSERT INTO maintenance_logs (task_name, completed_at, status)
    VALUES ('scheduled_maintenance', NOW(), 'completed');
END;
$$ LANGUAGE plpgsql;

-- Create maintenance logs table if it doesn't exist
CREATE TABLE IF NOT EXISTS maintenance_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_name VARCHAR(255) NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    status VARCHAR(50) NOT NULL,
    details TEXT
);

CREATE INDEX IF NOT EXISTS idx_maintenance_logs_completed_at ON maintenance_logs(completed_at);
CREATE INDEX IF NOT EXISTS idx_maintenance_logs_task_name ON maintenance_logs(task_name);

-- Grant necessary permissions (adjust as needed)
-- GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO drmindit_app_user;
-- GRANT SELECT ON ALL TABLES IN SCHEMA public TO drmindit_app_user;
-- GRANT SELECT ON ALL VIEWS IN SCHEMA public TO drmindit_app_user;
