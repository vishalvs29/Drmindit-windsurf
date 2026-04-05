-- Migration 002: Create programs related tables
-- DrMindit Backend Database Schema

-- Create programs table
CREATE TABLE IF NOT EXISTS programs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    duration VARCHAR(20) NOT NULL, -- DAYS_7, DAYS_14, DAYS_21, DAYS_30, ONGOING
    category VARCHAR(50) NOT NULL, -- ANXIETY, STRESS, CONFIDENCE, SLEEP, FOCUS
    difficulty VARCHAR(20) NOT NULL, -- BEGINNER, INTERMEDIATE, ADVANCED
    target_audience VARCHAR(50), -- STUDENT, CORPORATE, POLICE_MILITARY
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- Create program_days table
CREATE TABLE IF NOT EXISTS program_days (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    program_id UUID NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
    day_number INTEGER NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create program_steps table
CREATE TABLE IF NOT EXISTS program_steps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    program_day_id UUID NOT NULL REFERENCES program_days(id) ON DELETE CASCADE,
    step_type VARCHAR(50) NOT NULL, -- INSTRUCTION, QUESTION, GUIDED_EXERCISE, REFLECTION, COMPLETION
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    estimated_minutes INTEGER NOT NULL DEFAULT 5,
    exercise_type VARCHAR(50), -- BREATHING, AWARENESS, COGNITIVE_RESTRUCTURING, etc.
    audio_session_title VARCHAR(255),
    audio_session_duration INTEGER,
    audio_session_voice_gender VARCHAR(10), -- MALE, FEMALE, NEUTRAL
    audio_session_background_music BOOLEAN DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create user_programs table
CREATE TABLE IF NOT EXISTS user_programs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    program_id UUID NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
    current_day INTEGER NOT NULL DEFAULT 1,
    current_step INTEGER NOT NULL DEFAULT 0,
    completed_days INTEGER[] DEFAULT '{}', -- Array of completed day numbers
    day_progress JSONB DEFAULT '{}', -- Detailed progress per day
    started_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    last_accessed_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    total_minutes_spent BIGINT DEFAULT 0,
    streak_days INTEGER DEFAULT 1,
    is_completed BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create user_step_progress table
CREATE TABLE IF NOT EXISTS user_step_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_program_id UUID NOT NULL REFERENCES user_programs(id) ON DELETE CASCADE,
    step_id UUID NOT NULL REFERENCES program_steps(id) ON DELETE CASCADE,
    is_started BOOLEAN DEFAULT FALSE,
    is_completed BOOLEAN DEFAULT FALSE,
    time_spent_minutes INTEGER DEFAULT 0,
    reflection_response TEXT,
    exercise_completed BOOLEAN DEFAULT FALSE,
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    notes TEXT,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create program_completions table
CREATE TABLE IF NOT EXISTS program_completions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    program_id UUID NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
    user_program_id UUID NOT NULL REFERENCES user_programs(id) ON DELETE CASCADE,
    completion_data JSONB, -- Final completion metrics
    certificate_url VARCHAR(500),
    total_days_completed INTEGER NOT NULL,
    total_time_spent_minutes BIGINT NOT NULL,
    final_rating INTEGER CHECK (final_rating >= 1 AND final_rating <= 5),
    feedback TEXT,
    completed_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create indexes for programs table
CREATE INDEX IF NOT EXISTS idx_programs_category ON programs(category);
CREATE INDEX IF NOT EXISTS idx_programs_difficulty ON programs(difficulty);
CREATE INDEX IF NOT EXISTS idx_programs_target_audience ON programs(target_audience);
CREATE INDEX IF NOT EXISTS idx_programs_is_active ON programs(is_active);
CREATE INDEX IF NOT EXISTS idx_programs_created_at ON programs(created_at);

-- Create indexes for program_days table
CREATE INDEX IF NOT EXISTS idx_program_days_program_id ON program_days(program_id);
CREATE INDEX IF NOT EXISTS idx_program_days_day_number ON program_days(day_number);
CREATE UNIQUE INDEX IF NOT EXISTS idx_program_days_unique ON program_days(program_id, day_number);

-- Create indexes for program_steps table
CREATE INDEX IF NOT EXISTS idx_program_steps_program_day_id ON program_steps(program_day_id);
CREATE INDEX IF NOT EXISTS idx_program_steps_step_type ON program_steps(step_type);
CREATE INDEX IF NOT EXISTS idx_program_steps_sort_order ON program_steps(sort_order);

-- Create indexes for user_programs table
CREATE INDEX IF NOT EXISTS idx_user_programs_user_id ON user_programs(user_id);
CREATE INDEX IF NOT EXISTS idx_user_programs_program_id ON user_programs(program_id);
CREATE INDEX IF NOT EXISTS idx_user_programs_is_completed ON user_programs(is_completed);
CREATE INDEX IF NOT EXISTS idx_user_programs_started_at ON user_programs(started_at);
CREATE INDEX IF NOT EXISTS idx_user_programs_last_accessed_at ON user_programs(last_accessed_at);

-- Create indexes for user_step_progress table
CREATE INDEX IF NOT EXISTS idx_user_step_progress_user_program_id ON user_step_progress(user_program_id);
CREATE INDEX IF NOT EXISTS idx_user_step_progress_step_id ON user_step_progress(step_id);
CREATE INDEX IF NOT EXISTS idx_user_step_progress_is_completed ON user_step_progress(is_completed);

-- Create indexes for program_completions table
CREATE INDEX IF NOT EXISTS idx_program_completions_user_id ON program_completions(user_id);
CREATE INDEX IF NOT EXISTS idx_program_completions_program_id ON program_completions(program_id);
CREATE INDEX IF NOT EXISTS idx_program_completions_completed_at ON program_completions(completed_at);

-- Add constraints
ALTER TABLE programs ADD CONSTRAINT chk_programs_duration 
    CHECK (duration IN ('DAYS_7', 'DAYS_14', 'DAYS_21', 'DAYS_30', 'ONGOING'));

ALTER TABLE programs ADD CONSTRAINT chk_programs_category 
    CHECK (category IN ('ANXIETY', 'STRESS', 'CONFIDENCE', 'SLEEP', 'FOCUS'));

ALTER TABLE programs ADD CONSTRAINT chk_programs_difficulty 
    CHECK (difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED'));

ALTER TABLE programs ADD CONSTRAINT chk_programs_target_audience 
    CHECK (target_audience IN ('STUDENT', 'CORPORATE', 'POLICE_MILITARY'));

ALTER TABLE program_steps ADD CONSTRAINT chk_program_steps_step_type 
    CHECK (step_type IN ('INSTRUCTION', 'QUESTION', 'GUIDED_EXERCISE', 'REFLECTION', 'COMPLETION'));

ALTER TABLE program_steps ADD CONSTRAINT chk_program_steps_exercise_type 
    CHECK (exercise_type IN ('BREATHING', 'AWARENESS', 'COGNITIVE_RESTRUCTURING', 'EXPOSURE', 'VISUALIZATION', 'RELAXATION', 'JOURNALING'));

ALTER TABLE program_steps ADD CONSTRAINT chk_program_steps_voice_gender 
    CHECK (audio_session_voice_gender IN ('MALE', 'FEMALE', 'NEUTRAL'));

ALTER TABLE program_days ADD CONSTRAINT chk_program_days_day_number 
    CHECK (day_number > 0);

-- Add comments for documentation
COMMENT ON TABLE programs IS 'Therapeutic programs available in the platform';
COMMENT ON TABLE program_days IS 'Daily structure for each program';
COMMENT ON TABLE program_steps IS 'Individual steps within program days';
COMMENT ON TABLE user_programs IS 'User enrollment and progress tracking';
COMMENT ON TABLE user_step_progress IS 'Detailed progress for each program step';
COMMENT ON TABLE program_completions IS 'Completed program records with certificates';

COMMENT ON COLUMN programs.duration IS 'Program duration (DAYS_7, DAYS_14, DAYS_21, DAYS_30, ONGOING)';
COMMENT ON COLUMN programs.category IS 'Program category (ANXIETY, STRESS, CONFIDENCE, SLEEP, FOCUS)';
COMMENT ON COLUMN programs.difficulty IS 'Program difficulty level (BEGINNER, INTERMEDIATE, ADVANCED)';
COMMENT ON COLUMN programs.target_audience IS 'Target audience (STUDENT, CORPORATE, POLICE_MILITARY)';
COMMENT ON COLUMN user_programs.completed_days IS 'Array of completed day numbers';
COMMENT ON COLUMN user_programs.day_progress IS 'JSON object with detailed progress per day';
COMMENT ON COLUMN user_step_progress.reflection_response IS 'User reflection text for the step';
COMMENT ON COLUMN program_completions.completion_data IS 'JSON object with completion metrics';
