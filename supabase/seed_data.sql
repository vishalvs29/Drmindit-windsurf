-- DrMindit Backend Seed Data
-- This file contains sample data for testing and demonstration

-- Insert sample sessions with real audio URLs
INSERT INTO public.sessions (
    id, 
    title, 
    description, 
    instructor, 
    duration_minutes, 
    audio_url, 
    image_url, 
    category, 
    tags, 
    rating, 
    total_ratings, 
    is_premium, 
    difficulty, 
    language,
    is_active,
    created_at,
    updated_at
) VALUES 
-- Mindfulness Sessions
('session_001', 'Morning Mindfulness', 'Start your day with clarity and peace through this gentle morning meditation practice.', 'Dr. Sarah Chen', 10, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3', 'https://picsum.photos/seed/morning/400/300.jpg', 'mindfulness', ARRAY['morning', 'clarity', 'peace'], 4.8, 234, false, 'beginner', 'en', true, NOW(), NOW()),

('session_002', 'Evening Wind Down', 'Release the day's stress and prepare for restful sleep with this calming evening meditation.', 'Prof. James Miller', 15, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3', 'https://picsum.photos/seed/evening/400/300.jpg', 'mindfulness', ARRAY['evening', 'relaxation', 'sleep'], 4.7, 189, false, 'beginner', 'en', true, NOW(), NOW()),

('session_003', 'Mindful Breathing', 'Focus on your breath to anchor yourself in the present moment and reduce anxiety.', 'Dr. Emily Brown', 8, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3', 'https://picsum.photos/seed/breathing/400/300.jpg', 'breathing', ARRAY['breathing', 'anxiety', 'focus'], 4.9, 412, false, 'beginner', 'en', true, NOW(), NOW()),

-- Anxiety Management
('session_004', 'Anxiety Relief Meditation', 'A guided meditation specifically designed to help you manage and reduce anxiety symptoms.', 'Dr. Michael Roberts', 20, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3', 'https://picsum.photos/seed/anxiety/400/300.jpg', 'anxiety', ARRAY['anxiety', 'stress', 'calm'], 4.6, 327, false, 'intermediate', 'en', true, NOW(), NOW()),

('session_005', 'Panic Attack Support', 'Immediate support for panic attacks with grounding techniques and calming guidance.', 'Dr. Lisa Anderson', 12, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3', 'https://picsum.photos/seed/panic/400/300.jpg', 'anxiety', ARRAY['panic', 'grounding', 'emergency'], 4.8, 156, false, 'beginner', 'en', true, NOW(), NOW()),

('session_006', 'Worry Time Management', 'Learn to contain your worries to specific times and reclaim your mental space.', 'Prof. David Wilson', 18, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3', 'https://picsum.photos/seed/worry/400/300.jpg', 'anxiety', ARRAY['worry', 'time-management', 'cognitive'], 4.5, 98, true, 'intermediate', 'en', true, NOW(), NOW()),

-- Stress Management
('session_007', 'Stress Reduction Body Scan', 'Release physical tension and mental stress through this comprehensive body scan meditation.', 'Dr. Jennifer Taylor', 25, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3', 'https://picsum.photos/seed/bodyscan/400/300.jpg', 'stress', ARRAY['body-scan', 'relaxation', 'tension'], 4.7, 289, false, 'beginner', 'en', true, NOW(), NOW()),

('session_008', 'Quick Stress Reset', 'A 5-minute meditation to quickly reset your stress levels during busy days.', 'Dr. Robert Martinez', 5, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3', 'https://picsum.photos/seed/quick/400/300.jpg', 'stress', ARRAY['quick', 'stress', 'break'], 4.4, 445, false, 'beginner', 'en', true, NOW(), NOW()),

('session_009', 'Progressive Muscle Relaxation', 'Systematically release tension throughout your entire body for deep relaxation.', 'Dr. Maria Garcia', 22, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3', 'https://picsum.photos/seed/muscle/400/300.jpg', 'stress', ARRAY['muscle', 'relaxation', 'physical'], 4.6, 178, false, 'beginner', 'en', true, NOW(), NOW()),

-- Focus & Concentration
('session_010', 'Deep Focus Meditation', 'Enhance your concentration and mental clarity for work or study.', 'Prof. Kevin Thompson', 15, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3', 'https://picsum.photos/seed/focus/400/300.jpg', 'focus', ARRAY['focus', 'concentration', 'work'], 4.8, 367, true, 'intermediate', 'en', true, NOW(), NOW()),

('session_011', 'Study Session Support', 'Optimize your learning and retention with this study-focused meditation.', 'Dr. Amanda White', 12, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-11.mp3', 'https://picsum.photos/seed/study/400/300.jpg', 'focus', ARRAY['study', 'learning', 'memory'], 4.5, 234, false, 'beginner', 'en', true, NOW(), NOW()),

-- Sleep Support
('session_012', 'Deep Sleep Journey', 'Drift into restorative sleep with this soothing bedtime meditation.', 'Dr. Christopher Lee', 30, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3', 'https://picsum.photos/seed/sleep/400/300.jpg', 'sleep', ARRAY['sleep', 'bedtime', 'rest'], 4.9, 523, false, 'beginner', 'en', true, NOW(), NOW()),

('session_013', 'Insomnia Relief', 'Specifically designed to help you overcome insomnia and fall asleep naturally.', 'Dr. Nancy Davis', 25, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-13.mp3', 'https://picsum.photos/seed/insomnia/400/300.jpg', 'sleep', ARRAY['insomnia', 'sleep-disorder', 'treatment'], 4.7, 189, true, 'intermediate', 'en', true, NOW(), NOW()),

-- Yoga & Movement
('session_014', 'Gentle Yoga Flow', 'Combine mindfulness with gentle movement in this beginner-friendly yoga session.', 'Rachel Johnson', 20, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-14.mp3', 'https://picsum.photos/seed/yoga/400/300.jpg', 'yoga', ARRAY['yoga', 'movement', 'gentle'], 4.6, 298, false, 'beginner', 'en', true, NOW(), NOW()),

('session_015', 'Mindful Walking', 'Transform your daily walks into mindful meditation practices.', 'Dr. Patricia Brown', 15, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-15.mp3', 'https://picsum.photos/seed/walking/400/300.jpg', 'yoga', ARRAY['walking', 'movement', 'outdoors'], 4.4, 167, false, 'beginner', 'en', true, NOW(), NOW()),

-- Depression Support
('session_016', 'Hope and Healing', 'A gentle meditation for finding hope and managing depressive symptoms.', 'Dr. Susan Miller', 18, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-16.mp3', 'https://picsum.photos/seed/hope/400/300.jpg', 'depression', ARRAY['hope', 'healing', 'depression'], 4.8, 234, true, 'intermediate', 'en', true, NOW(), NOW()),

('session_017', 'Self-Compassion Practice', 'Cultivate kindness and compassion toward yourself during difficult times.', 'Dr. Barbara Wilson', 16, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-17.mp3', 'https://picsum.photos/seed/compassion/400/300.jpg', 'depression', ARRAY['compassion', 'self-care', 'kindness'], 4.9, 312, false, 'beginner', 'en', true, NOW(), NOW()),

-- Advanced Meditation
('session_018', 'Advanced Mindfulness', 'Deepen your meditation practice with advanced mindfulness techniques.', 'Master Wei Chen', 35, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-18.mp3', 'https://picsum.photos/seed/advanced/400/300.jpg', 'meditation', ARRAY['advanced', 'deep-meditation', 'practice'], 4.7, 145, true, 'advanced', 'en', true, NOW(), NOW()),

('session_019', 'Vipassana Insight', 'Develop insight into the nature of reality through Vipassana meditation.', 'Dr. Thomas Kumar', 40, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-19.mp3', 'https://picsum.photos/seed/vipassana/400/300.jpg', 'meditation', ARRAY['vipassana', 'insight', 'advanced'], 4.8, 89, true, 'advanced', 'en', true, NOW(), NOW()),

-- Quick Sessions
('session_020', '3-Minute Reset', 'Quick mental reset for busy moments throughout your day.', 'Dr. Laura Smith', 3, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-20.mp3', 'https://picsum.photos/seed/reset/400/300.jpg', 'mindfulness', ARRAY['quick', 'reset', 'break'], 4.3, 567, false, 'beginner', 'en', true, NOW(), NOW());

-- Insert sample programs
INSERT INTO public.programs (
    id,
    title,
    description,
    image_url,
    category,
    duration_days,
    difficulty,
    rating,
    total_ratings,
    is_premium,
    target_audiences,
    is_active,
    created_at,
    updated_at
) VALUES 
('program_001', '21-Day Anxiety Reset', 'A comprehensive 21-day program to systematically reduce anxiety and build resilience. Each day introduces new techniques and builds upon previous lessons.', 'https://picsum.photos/seed/anxiety-program/600/400.jpg', 'anxiety_reset', 21, 'beginner', 4.8, 89, false, ARRAY['anxiety', 'stress', 'beginners'], true, NOW(), NOW()),

('program_002', 'Sleep Better Program', 'Transform your sleep quality in just 3 weeks with proven techniques for better rest and recovery.', 'https://picsum.photos/seed/sleep-program/600/400.jpg', 'sleep_better', 21, 'beginner', 4.7, 134, false, ARRAY['sleep', 'insomnia', 'recovery'], true, NOW(), NOW()),

('program_003', 'Focus Mastery Challenge', '21 days to sharpen your concentration and enhance productivity through targeted mindfulness exercises.', 'https://picsum.photos/seed/focus-program/600/400.jpg', 'focus_boost', 21, 'intermediate', 4.6, 67, true, ARRAY['focus', 'productivity', 'work'], true, NOW(), NOW()),

('program_004', 'Mindfulness Foundation', 'Build a strong foundation in mindfulness with this beginner-friendly 21-day program.', 'https://picsum.photos/seed/mindfulness-program/600/400.jpg', 'mindfulness_foundation', 21, 'beginner', 4.9, 234, false, ARRAY['mindfulness', 'beginners', 'foundation'], true, NOW(), NOW()),

('program_005', 'Stress Management Bootcamp', 'Intensive 21-day program to master stress management techniques and build emotional resilience.', 'https://picsum.photos/seed/stress-program/600/400.jpg', 'stress_management', 21, 'intermediate', 4.5, 98, true, ARRAY['stress', 'resilience', 'management'], true, NOW(), NOW()),

('program_006', 'Depression Support Journey', 'A gentle 21-day program designed to support mental health and provide tools for managing depression.', 'https://picsum.photos/seed/depression-program/600/400.jpg', 'depression_support', 21, 'beginner', 4.7, 156, false, ARRAY['depression', 'support', 'healing'], true, NOW(), NOW());

-- Insert program sessions (linking programs to individual sessions)
INSERT INTO public.program_sessions (
    id,
    program_id,
    day_number,
    session_id,
    is_unlocked,
    created_at,
    updated_at
) VALUES 
-- Anxiety Reset Program
('ps_001', 'program_001', 1, 'session_003', true, NOW(), NOW()),
('ps_002', 'program_001', 2, 'session_004', false, NOW(), NOW()),
('ps_003', 'program_001', 3, 'session_008', false, NOW(), NOW()),
('ps_004', 'program_001', 4, 'session_005', false, NOW(), NOW()),
('ps_005', 'program_001', 5, 'session_006', false, NOW(), NOW()),
('ps_006', 'program_001', 6, 'session_007', false, NOW(), NOW()),
('ps_007', 'program_001', 7, 'session_009', false, NOW(), NOW()),

-- Sleep Better Program
('ps_008', 'program_002', 1, 'session_012', true, NOW(), NOW()),
('ps_009', 'program_002', 2, 'session_013', false, NOW(), NOW()),
('ps_010', 'program_002', 3, 'session_002', false, NOW(), NOW()),
('ps_011', 'program_002', 4, 'session_009', false, NOW(), NOW()),
('ps_012', 'program_002', 5, 'session_007', false, NOW(), NOW()),

-- Focus Mastery Program
('ps_013', 'program_003', 1, 'session_010', true, NOW(), NOW()),
('ps_014', 'program_003', 2, 'session_011', false, NOW(), NOW()),
('ps_015', 'program_003', 3, 'session_003', false, NOW(), NOW()),
('ps_016', 'program_003', 4, 'session_008', false, NOW(), NOW()),

-- Mindfulness Foundation Program
('ps_017', 'program_004', 1, 'session_001', true, NOW(), NOW()),
('ps_018', 'program_004', 2, 'session_003', false, NOW(), NOW()),
('ps_019', 'program_004', 3, 'session_002', false, NOW(), NOW()),
('ps_020', 'program_004', 4, 'session_020', false, NOW(), NOW()),

-- Stress Management Program
('ps_021', 'program_005', 1, 'session_007', true, NOW(), NOW()),
('ps_022', 'program_005', 2, 'session_008', false, NOW(), NOW()),
('ps_023', 'program_005', 3, 'session_009', false, NOW(), NOW()),
('ps_024', 'program_005', 4, 'session_004', false, NOW(), NOW()),

-- Depression Support Program
('ps_025', 'program_006', 1, 'session_016', true, NOW(), NOW()),
('ps_026', 'program_006', 2, 'session_017', false, NOW(), NOW()),
('ps_027', 'program_006', 3, 'session_001', false, NOW(), NOW()),
('ps_028', 'program_006', 4, 'session_002', false, NOW(), NOW());

-- Insert emergency helplines (India-specific)
INSERT INTO public.emergency_helplines (
    id,
    name,
    phone,
    website,
    description,
    country,
    is_active,
    priority,
    created_at,
    updated_at
) VALUES 
('helpline_001', 'National Suicide Prevention Lifeline', '988', 'https://988lifeline.org', '24/7, free and confidential support for people in distress', 'US', true, 1, NOW(), NOW()),

('helpline_002', 'Crisis Text Line', 'Text HOME to 741741', 'https://www.crisistextline.org', 'Text with a trained crisis counselor', 'US', true, 2, NOW(), NOW()),

('helpline_003', 'iCall Mental Health Helpline', '9152987821', 'https://icall.org.in', 'Psychological support by trained professionals', 'IN', true, 1, NOW(), NOW()),

('helpline_004', 'Vandrevala Foundation', '1860-266-2600', 'https://vandrevalafoundation.com', '24/7 mental health helpline', 'IN', true, 2, NOW(), NOW()),

('helpline_005', 'NIMHANS Helpline', '080-46110007', 'https://nimhans.ac.in', 'National Institute of Mental Health and Neurosciences', 'IN', true, 3, NOW(), NOW()),

('helpline_006', 'Snehi Foundation', '011-4222123', 'https://snehi.org', 'Emotional support and counseling', 'IN', true, 4, NOW(), NOW()),

('helpline_007', 'AASRA', '91-9820466726', 'https://aasra.info', '24/7 suicide prevention helpline', 'IN', true, 5, NOW(), NOW()),

('helpline_008', 'Lifeline Foundation', '033-24744844', 'https://lifelinefoundation.net', 'Crisis intervention and suicide prevention', 'IN', true, 6, NOW(), NOW());

-- Insert sample user profiles for testing
INSERT INTO public.profiles (
    id,
    email,
    full_name,
    role,
    personal_goals,
    stress_level,
    preferences,
    is_high_risk,
    created_at,
    updated_at
) VALUES 
('user_001', 'test.user@example.com', 'Test User', 'general', ARRAY['stress_management', 'anxiety_relief'], 5, '{"reminder_time": "09:00", "preferred_session_duration": 15, "dark_mode": false, "notifications_enabled": true, "offline_downloads_enabled": false}', false, NOW(), NOW()),

('user_002', 'demo.user@example.com', 'Demo User', 'enterprise', ARRAY['focus_improvement', 'sleep_better'], 3, '{"reminder_time": "08:00", "preferred_session_duration": 20, "dark_mode": true, "notifications_enabled": true, "offline_downloads_enabled": true}', false, NOW(), NOW()),

('user_003', 'premium.user@example.com', 'Premium User', 'premium', ARRAY['anxiety_reset', 'depression_support'], 7, '{"reminder_time": "19:00", "preferred_session_duration": 25, "dark_mode": false, "notifications_enabled": true, "offline_downloads_enabled": true}', true, NOW(), NOW());

-- Insert sample user analytics
INSERT INTO public.user_analytics (
    id,
    user_id,
    week_start,
    total_mindful_minutes,
    sessions_completed,
    average_session_duration,
    current_streak,
    longest_streak,
    mood_improvement,
    stress_reduction,
    sleep_improvement,
    created_at,
    updated_at
) VALUES 
('analytics_001', 'user_001', '2024-03-18', 145, 12, 12.1, 5, 14, 0.23, 0.31, 0.18, NOW(), NOW()),

('analytics_002', 'user_002', '2024-03-18', 234, 18, 13.0, 8, 21, 0.35, 0.28, 0.42, NOW(), NOW()),

('analytics_003', 'user_003', '2024-03-18', 89, 7, 12.7, 3, 8, 0.15, 0.22, 0.31, NOW(), NOW());

-- Insert sample mood logs
INSERT INTO public.mood_logs (
    id,
    user_id,
    mood_type,
    mood_score,
    notes,
    triggers,
    activities,
    created_at,
    updated_at
) VALUES 
('mood_001', 'user_001', 'anxious', 4, 'Feeling anxious about work deadline', ARRAY['work', 'deadline'], ARRAY['meditation', 'breathing'], NOW(), NOW()),

('mood_002', 'user_001', 'calm', 7, 'Morning meditation helped', ARRAY['meditation'], ARRAY['mindfulness'], NOW() - INTERVAL '1 day', NOW()),

('mood_003', 'user_002', 'focused', 8, 'Productive morning session', ARRAY['work', 'focus'], ARRAY['concentration', 'breathing'], NOW(), NOW()),

('mood_004', 'user_003', 'sad', 3, 'Difficult day emotionally', ARRAY['stress', 'emotional'], ARRAY['self_compassion'], NOW(), NOW());

-- Insert sample user sessions
INSERT INTO public.user_sessions (
    id,
    user_id,
    session_id,
    progress_seconds,
    total_seconds,
    is_completed,
    is_favorite,
    last_played_at,
    created_at,
    updated_at
) VALUES 
('us_001', 'user_001', 'session_001', 600, 600, true, true, NOW(), NOW(), NOW()),

('us_002', 'user_001', 'session_003', 240, 480, false, false, NOW() - INTERVAL '2 hours', NOW(), NOW()),

('us_003', 'user_002', 'session_010', 900, 900, true, true, NOW() - INTERVAL '1 day', NOW(), NOW()),

('us_004', 'user_002', 'session_011', 360, 720, false, false, NOW() - INTERVAL '3 hours', NOW(), NOW()),

('us_005', 'user_003', 'session_016', 1080, 1080, true, true, NOW() - INTERVAL '2 days', NOW(), NOW()),

('us_006', 'user_003', 'session_017', 480, 960, false, false, NOW() - INTERVAL '1 hour', NOW(), NOW());

-- Insert sample user programs
INSERT INTO public.user_programs (
    id,
    user_id,
    program_id,
    current_day,
    completed_days,
    total_days,
    is_completed,
    completion_percentage,
    started_at,
    completed_at,
    last_active_at,
    created_at,
    updated_at
) VALUES 
('up_001', 'user_001', 'program_001', 8, 7, 21, false, 33.3, NOW() - INTERVAL '7 days', NULL, NOW(), NOW(), NOW()),

('up_002', 'user_002', 'program_003', 15, 14, 21, false, 66.7, NOW() - INTERVAL '14 days', NULL, NOW(), NOW(), NOW()),

('up_003', 'user_003', 'program_006', 21, 21, 21, true, 100.0, NOW() - INTERVAL '21 days', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', NOW(), NOW()),

('up_004', 'user_001', 'program_002', 3, 2, 21, false, 9.5, NOW() - INTERVAL '3 days', NULL, NOW(), NOW(), NOW());

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_sessions_category ON sessions(category);
CREATE INDEX IF NOT EXISTS idx_sessions_rating ON sessions(rating DESC);
CREATE INDEX IF NOT EXISTS idx_sessions_active ON sessions(is_active);
CREATE INDEX IF NOT EXISTS idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_user_sessions_session_id ON user_sessions(session_id);
CREATE INDEX IF NOT EXISTS idx_user_programs_user_id ON user_programs(user_id);
CREATE INDEX IF NOT EXISTS idx_mood_logs_user_id ON mood_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_mood_logs_created_at ON mood_logs(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_user_analytics_user_id ON user_analytics(user_id);
CREATE INDEX IF NOT EXISTS idx_crisis_events_user_id ON crisis_events(user_id);
CREATE INDEX IF NOT EXISTS idx_crisis_events_created_at ON crisis_events(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_emergency_helplines_country ON emergency_helplines(country);
CREATE INDEX IF NOT EXISTS idx_emergency_helplines_priority ON emergency_helplines(priority);

-- Update statistics for better query planning
ANALYZE sessions;
ANALYZE user_sessions;
ANALYZE user_programs;
ANALYZE mood_logs;
ANALYZE user_analytics;
ANALYZE emergency_helplines;
