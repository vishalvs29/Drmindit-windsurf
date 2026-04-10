-- DrMindit Production-Ready Supabase Schema
-- Mental Wellness Application Backend
-- https://supabase.com/dashboard/project/nlheesoshtczdhsqzjid

-- Enable necessary extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create custom types for better data validation
CREATE TYPE session_category AS ENUM (
    'meditation',
    'breathing',
    'sleep',
    'focus',
    'stress_relief',
    'anxiety',
    'mindfulness',
    'body_scan'
);

CREATE TYPE event_type AS ENUM (
    'SESSION_STARTED',
    'STEP_STARTED',
    'STEP_COMPLETED',
    'SESSION_COMPLETED',
    'SESSION_PAUSED',
    'SESSION_RESUMED',
    'PROGRESS_UPDATED'
);

-- Core sessions table
CREATE TABLE public.sessions (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    title TEXT NOT NULL CHECK (length(title) >= 3 AND length(title) <= 200),
    description TEXT CHECK (length(description) <= 1000),
    total_duration INTEGER NOT NULL CHECK (total_duration > 0 AND total_duration <= 7200), -- max 2 hours
    category session_category NOT NULL DEFAULT 'meditation',
    difficulty_level TEXT DEFAULT 'beginner' CHECK (difficulty_level IN ('beginner', 'intermediate', 'advanced')),
    instructor TEXT CHECK (length(instructor) <= 100),
    thumbnail_url TEXT,
    is_active BOOLEAN DEFAULT true,
    is_premium BOOLEAN DEFAULT false,
    tags TEXT[] DEFAULT '{}',
    rating DECIMAL(3,2) DEFAULT 0.0 CHECK (rating >= 0.0 AND rating <= 5.0),
    total_ratings INTEGER DEFAULT 0 CHECK (total_ratings >= 0),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Session steps for multi-step audio content
CREATE TABLE public.session_steps (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES public.sessions(id) ON DELETE CASCADE,
    title TEXT NOT NULL CHECK (length(title) >= 3 AND length(title) <= 200),
    description TEXT CHECK (length(description) <= 500),
    audio_url TEXT NOT NULL,
    duration INTEGER NOT NULL CHECK (duration > 0 AND duration <= 3600), -- max 1 hour per step
    order_index INTEGER NOT NULL CHECK (order_index >= 0),
    step_type TEXT DEFAULT 'audio' CHECK (step_type IN ('audio', 'silence', 'breathing_guide')),
    transcript TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(session_id, order_index)
);

-- User session progress tracking
CREATE TABLE public.user_session_progress (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    session_id UUID NOT NULL REFERENCES public.sessions(id) ON DELETE CASCADE,
    current_step_index INTEGER DEFAULT 0 CHECK (current_step_index >= 0),
    progress_seconds INTEGER DEFAULT 0 CHECK (progress_seconds >= 0),
    completed_steps JSONB DEFAULT '[]' CHECK (jsonb_typeof(completed_steps) = 'array'),
    is_completed BOOLEAN DEFAULT false,
    is_favorite BOOLEAN DEFAULT false,
    last_played_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    started_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    completed_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(user_id, session_id)
);

-- Session events for analytics
CREATE TABLE public.session_events (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    session_id UUID NOT NULL REFERENCES public.sessions(id) ON DELETE CASCADE,
    step_index INTEGER CHECK (step_index >= 0),
    event_type event_type NOT NULL,
    event_data JSONB DEFAULT '{}',
    device_info JSONB DEFAULT '{}',
    session_duration INTEGER CHECK (session_duration >= 0),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- User profiles (extends auth.users)
CREATE TABLE public.profiles (
    id UUID REFERENCES auth.users(id) ON DELETE CASCADE PRIMARY KEY,
    first_name TEXT CHECK (length(first_name) <= 100),
    last_name TEXT CHECK (length(last_name) <= 100),
    avatar_url TEXT,
    phone TEXT CHECK (phone ~ '^[+]?[0-9\s\-\(\)]+$'),
    timezone TEXT DEFAULT 'UTC',
    preferences JSONB DEFAULT '{
        "theme": "light",
        "notifications": true,
        "language": "en",
        "audio_quality": "high",
        "auto_play_next": true,
        "download_for_offline": false
    }'::jsonb,
    subscription_level TEXT DEFAULT 'free' CHECK (subscription_level IN ('free', 'premium')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- User mood ratings for wellness tracking
CREATE TABLE public.user_mood_ratings (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    session_id UUID REFERENCES public.sessions(id) ON DELETE SET NULL,
    mood_before INTEGER CHECK (mood_before >= 1 AND mood_before <= 10),
    mood_after INTEGER CHECK (mood_after >= 1 AND mood_after <= 10),
    stress_level INTEGER CHECK (stress_level >= 1 AND stress_level <= 10),
    sleep_quality TEXT CHECK (sleep_quality IN ('poor', 'fair', 'good', 'excellent')),
    notes TEXT CHECK (length(notes) <= 500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Performance indexes
CREATE INDEX idx_sessions_id ON public.sessions(id);
CREATE INDEX idx_sessions_active ON public.sessions(is_active);
CREATE INDEX idx_sessions_category ON public.sessions(category);
CREATE INDEX idx_sessions_created_at ON public.sessions(created_at);

CREATE INDEX idx_session_steps_session_id ON public.session_steps(session_id);
CREATE INDEX idx_session_steps_order ON public.session_steps(session_id, order_index);
CREATE INDEX idx_session_steps_active ON public.session_steps(is_active);

CREATE INDEX idx_user_session_progress_user_id ON public.user_session_progress(user_id);
CREATE INDEX idx_user_session_progress_session_id ON public.user_session_progress(session_id);
CREATE INDEX idx_user_session_progress_completed ON public.user_session_progress(is_completed);
CREATE INDEX idx_user_session_progress_updated_at ON public.user_session_progress(updated_at);
CREATE INDEX idx_user_session_progress_favorite ON public.user_session_progress(is_favorite);

CREATE INDEX idx_session_events_user_id ON public.session_events(user_id);
CREATE INDEX idx_session_events_session_id ON public.session_events(session_id);
CREATE INDEX idx_session_events_type ON public.session_events(event_type);
CREATE INDEX idx_session_events_created_at ON public.session_events(created_at);

CREATE INDEX idx_profiles_user_id ON public.profiles(id);
CREATE INDEX idx_profiles_subscription ON public.profiles(subscription_level);

CREATE INDEX idx_mood_ratings_user_id ON public.user_mood_ratings(user_id);
CREATE INDEX idx_mood_ratings_session_id ON public.user_mood_ratings(session_id);
CREATE INDEX idx_mood_ratings_created_at ON public.user_mood_ratings(created_at);

-- Enable Row Level Security
ALTER TABLE public.sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.session_steps ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_session_progress ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.session_events ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_mood_ratings ENABLE ROW LEVEL SECURITY;

-- RLS Policies

-- Sessions: Public read access for active sessions
CREATE POLICY "Active sessions are viewable by everyone" ON public.sessions 
    FOR SELECT USING (is_active = true);

-- Session steps: Public read access for active steps of active sessions
CREATE POLICY "Active session steps are viewable by everyone" ON public.session_steps 
    FOR SELECT USING (
        is_active = true AND 
        EXISTS (
            SELECT 1 FROM public.sessions 
            WHERE sessions.id = session_steps.session_id 
            AND sessions.is_active = true
        )
    );

-- User session progress: Full access to own data
CREATE POLICY "Users can view own session progress" ON public.user_session_progress 
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own session progress" ON public.user_session_progress 
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own session progress" ON public.user_session_progress 
    FOR UPDATE USING (auth.uid() = user_id);

-- Session events: Users can insert and view own events
CREATE POLICY "Users can insert own session events" ON public.session_events 
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can view own session events" ON public.session_events 
    FOR SELECT USING (auth.uid() = user_id);

-- Profiles: Full access to own profile
CREATE POLICY "Users can view own profile" ON public.profiles 
    FOR SELECT USING (auth.uid() = id);

CREATE POLICY "Users can update own profile" ON public.profiles 
    FOR UPDATE USING (auth.uid() = id);

-- User mood ratings: Full access to own data
CREATE POLICY "Users can view own mood ratings" ON public.user_mood_ratings 
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own mood ratings" ON public.user_mood_ratings 
    FOR INSERT WITH CHECK (auth.uid() = user_id);

-- Functions for automatic timestamp updates
CREATE OR REPLACE FUNCTION public.handle_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers for updated_at columns
CREATE TRIGGER handle_sessions_updated_at 
    BEFORE UPDATE ON public.sessions 
    FOR EACH ROW EXECUTE FUNCTION public.handle_updated_at();

CREATE TRIGGER handle_user_session_progress_updated_at 
    BEFORE UPDATE ON public.user_session_progress 
    FOR EACH ROW EXECUTE FUNCTION public.handle_updated_at();

CREATE TRIGGER handle_profiles_updated_at 
    BEFORE UPDATE ON public.profiles 
    FOR EACH ROW EXECUTE FUNCTION public.handle_updated_at();

-- Function to create user profile automatically
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, first_name, last_name)
    VALUES (
        NEW.id, 
        NEW.raw_user_meta_data->>'first_name', 
        NEW.raw_user_meta_data->>'last_name'
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger to create profile on user signup
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- Function for session analytics
CREATE OR REPLACE FUNCTION public.get_session_analytics(session_uuid UUID)
RETURNS TABLE (
    total_users BIGINT,
    completion_rate DECIMAL,
    average_completion_time INTERVAL,
    average_rating DECIMAL
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(DISTINCT usp.user_id) as total_users,
        ROUND(
            (COUNT(*) FILTER (WHERE usp.is_completed = true)::DECIMAL / 
             NULLIF(COUNT(*), 0)) * 100, 2
        ) as completion_rate,
        AVG(usp.completed_at - usp.started_at) as average_completion_time,
        s.rating as average_rating
    FROM public.user_session_progress usp
    JOIN public.sessions s ON s.id = usp.session_id
    WHERE usp.session_id = session_uuid
    GROUP BY s.rating;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to get user statistics
CREATE OR REPLACE FUNCTION public.get_user_statistics(user_uuid UUID)
RETURNS TABLE (
    total_sessions BIGINT,
    completed_sessions BIGINT,
    total_minutes INTEGER,
    favorite_sessions BIGINT,
    current_streak INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*) as total_sessions,
        COUNT(*) FILTER (WHERE is_completed = true) as completed_sessions,
        COALESCE(
            (SELECT SUM(EXTRACT(EPOCH FROM (completed_at - started_at))/60) 
             FROM public.user_session_progress 
             WHERE user_id = user_uuid AND is_completed = true), 0
        ) as total_minutes,
        COUNT(*) FILTER (WHERE is_favorite = true) as favorite_sessions,
        -- Calculate streak based on consecutive days with activity
        (
            SELECT COUNT(DISTINCT DATE(last_played_at))
            FROM public.user_session_progress 
            WHERE user_id = user_uuid 
            AND last_played_at >= NOW() - INTERVAL '30 days'
            ORDER BY last_played_at DESC
        ) as current_streak
    FROM public.user_session_progress
    WHERE user_id = user_uuid;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Real-time publication for progress tracking
ALTER PUBLICATION supabase_realtime ADD TABLE public.user_session_progress;

-- Grant permissions for service role (for admin operations)
GRANT ALL ON ALL TABLES IN SCHEMA public TO service_role;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO service_role;

-- Grant permissions for authenticated users
GRANT SELECT ON public.sessions TO authenticated;
GRANT SELECT ON public.session_steps TO authenticated;
GRANT ALL ON public.user_session_progress TO authenticated;
GRANT ALL ON public.session_events TO authenticated;
GRANT ALL ON public.profiles TO authenticated;
GRANT ALL ON public.user_mood_ratings TO authenticated;

-- Grant permissions for anon users (public content)
GRANT SELECT ON public.sessions TO anon;
GRANT SELECT ON public.session_steps TO anon;
