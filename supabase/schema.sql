-- DrMindit Database Schema for Supabase
-- Production-ready schema for mental health platform

-- Enable necessary extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Organizations table for enterprise clients (must be created before profiles)
CREATE TABLE public.organizations (
  id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
  name TEXT NOT NULL,
  type TEXT NOT NULL CHECK (type IN ('educational', 'corporate', 'government', 'healthcare')),
  contact_email TEXT,
  contact_phone TEXT,
  settings JSONB DEFAULT '{}',
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Users table (extends Supabase auth.users)
CREATE TABLE public.profiles (
  id UUID REFERENCES auth.users(id) ON DELETE CASCADE PRIMARY KEY,
  email TEXT NOT NULL,
  full_name TEXT,
  avatar_url TEXT,
  role TEXT NOT NULL CHECK (role IN ('student', 'corporate', 'government', 'police_military', 'general')),
  phone TEXT,
  organization_id UUID REFERENCES organizations(id),
  personal_goals TEXT[] DEFAULT '{}',
  stress_level INTEGER CHECK (stress_level >= 1 AND stress_level <= 10),
  preferences JSONB DEFAULT '{}',
  is_high_risk BOOLEAN DEFAULT FALSE,
  last_crisis_check TIMESTAMP WITH TIME ZONE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Sessions table for meditation/audio content
CREATE TABLE public.sessions (
  id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
  title TEXT NOT NULL,
  description TEXT,
  instructor TEXT NOT NULL,
  duration_minutes INTEGER NOT NULL,
  audio_url TEXT NOT NULL,
  image_url TEXT,
  category TEXT NOT NULL CHECK (category IN ('sleep', 'anxiety', 'stress', 'focus', 'depression', 'mindfulness', 'breathing', 'meditation', 'yoga', 'body_scan')),
  tags TEXT[] DEFAULT '{}',
  difficulty TEXT NOT NULL CHECK (difficulty IN ('beginner', 'intermediate', 'advanced')),
  rating DECIMAL(3,2) DEFAULT 0.0 CHECK (rating >= 0.0 AND rating <= 5.0),
  total_ratings INTEGER DEFAULT 0,
  is_premium BOOLEAN DEFAULT FALSE,
  language TEXT DEFAULT 'en',
  transcript TEXT,
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Programs table for structured programs
CREATE TABLE public.programs (
  id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
  title TEXT NOT NULL,
  description TEXT,
  image_url TEXT,
  category TEXT NOT NULL CHECK (category IN ('anxiety_reset', 'stress_management', 'sleep_better', 'focus_boost', 'depression_support', 'mindfulness_foundation', 'resilience_building')),
  duration_days INTEGER NOT NULL,
  difficulty TEXT NOT NULL CHECK (difficulty IN ('beginner', 'intermediate', 'advanced')),
  rating DECIMAL(3,2) DEFAULT 0.0 CHECK (rating >= 0.0 AND rating <= 5.0),
  total_ratings INTEGER DEFAULT 0,
  is_premium BOOLEAN DEFAULT FALSE,
  target_audiences TEXT[] DEFAULT '{}',
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Program sessions mapping
CREATE TABLE public.program_sessions (
  id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
  program_id UUID REFERENCES programs(id) ON DELETE CASCADE,
  day_number INTEGER NOT NULL,
  session_id UUID REFERENCES sessions(id) ON DELETE CASCADE,
  is_unlocked BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  UNIQUE(program_id, day_number)
);

-- User session progress tracking
CREATE TABLE public.user_sessions (
  id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
  user_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
  session_id UUID REFERENCES sessions(id) ON DELETE CASCADE,
  program_id UUID REFERENCES programs(id) ON DELETE SET NULL,
  progress_seconds INTEGER DEFAULT 0,
  total_seconds INTEGER NOT NULL,
  is_completed BOOLEAN DEFAULT FALSE,
  is_favorite BOOLEAN DEFAULT FALSE,
  last_played_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  UNIQUE(user_id, session_id)
);

-- User program progress tracking
CREATE TABLE public.user_programs (
  id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
  user_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
  program_id UUID REFERENCES programs(id) ON DELETE CASCADE,
  current_day INTEGER DEFAULT 1,
  completed_days INTEGER DEFAULT 0,
  total_days INTEGER NOT NULL,
  is_completed BOOLEAN DEFAULT FALSE,
  completion_percentage DECIMAL(5,2) DEFAULT 0.0,
  started_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  completed_at TIMESTAMP WITH TIME ZONE,
  last_active_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  UNIQUE(user_id, program_id)
);

-- Mood logs for tracking user mental state
CREATE TABLE public.mood_logs (
  id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
  user_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
  mood_score INTEGER NOT NULL CHECK (mood_score >= 1 AND mood_score <= 10),
  stress_level INTEGER CHECK (stress_level >= 1 AND stress_level <= 10),
  sleep_quality TEXT CHECK (sleep_quality IN ('poor', 'fair', 'good', 'excellent')),
  mood_type TEXT CHECK (mood_type IN ('very_happy', 'happy', 'neutral', 'sad', 'very_sad', 'anxious', 'calm', 'energetic', 'tired')),
  notes TEXT,
  ai_analysis JSONB,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Crisis events for safety monitoring
CREATE TABLE public.crisis_events (
  id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
  user_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
  trigger_reason TEXT NOT NULL CHECK (trigger_reason IN ('low_mood_score', 'suicidal_keywords', 'repeated_negative_logs', 'ai_detected_distress', 'manual_trigger')),
  severity TEXT NOT NULL CHECK (severity IN ('low', 'medium', 'high', 'critical')),
  mood_score INTEGER CHECK (mood_score >= 1 AND mood_score <= 10),
  context_data JSONB,
  is_resolved BOOLEAN DEFAULT FALSE,
  resolution_notes TEXT,
  follow_up_required BOOLEAN DEFAULT TRUE,
  organization_notified BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  resolved_at TIMESTAMP WITH TIME ZONE
);

-- User analytics for insights
CREATE TABLE public.user_analytics (
  id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
  user_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
  total_mindful_minutes INTEGER DEFAULT 0,
  current_streak INTEGER DEFAULT 0,
  longest_streak INTEGER DEFAULT 0,
  sessions_completed INTEGER DEFAULT 0,
  average_session_duration DECIMAL(5,2) DEFAULT 0.0,
  last_active_date DATE,
  week_start DATE,
  weekly_data JSONB DEFAULT '{}',
  month_start DATE,
  monthly_data JSONB DEFAULT '{}',
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  UNIQUE(user_id, week_start)
);

-- Emergency helplines for crisis support
CREATE TABLE public.emergency_helplines (
  id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
  name TEXT NOT NULL,
  phone TEXT NOT NULL,
  email TEXT,
  website TEXT,
  description TEXT,
  country TEXT DEFAULT 'IN',
  is_24_7 BOOLEAN DEFAULT TRUE,
  languages TEXT[] DEFAULT '{en}',
  is_active BOOLEAN DEFAULT TRUE,
  priority INTEGER DEFAULT 0,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create indexes for performance
CREATE INDEX idx_profiles_email ON public.profiles(email);
CREATE INDEX idx_profiles_role ON public.profiles(role);
CREATE INDEX idx_profiles_is_high_risk ON public.profiles(is_high_risk);
CREATE INDEX idx_sessions_category ON public.sessions(category);
CREATE INDEX idx_sessions_is_active ON public.sessions(is_active);
CREATE INDEX idx_user_sessions_user_id ON public.user_sessions(user_id);
CREATE INDEX idx_user_sessions_is_completed ON public.user_sessions(is_completed);
CREATE INDEX idx_mood_logs_user_id ON public.mood_logs(user_id);
CREATE INDEX idx_mood_logs_created_at ON public.mood_logs(created_at);
CREATE INDEX idx_crisis_events_user_id ON public.crisis_events(user_id);
CREATE INDEX idx_crisis_events_created_at ON public.crisis_events(created_at);
CREATE INDEX idx_crisis_events_severity ON public.crisis_events(severity);
CREATE INDEX idx_user_programs_user_id ON public.user_programs(user_id);
CREATE INDEX idx_user_programs_is_completed ON public.user_programs(is_completed);

-- Row Level Security (RLS) policies
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_programs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.mood_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.crisis_events ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_analytics ENABLE ROW LEVEL SECURITY;

-- Users can only access their own data
CREATE POLICY "Users can view own profile" ON public.profiles FOR SELECT USING (auth.uid() = id);
CREATE POLICY "Users can update own profile" ON public.profiles FOR UPDATE USING (auth.uid() = id);

CREATE POLICY "Users can view own sessions" ON public.user_sessions FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own sessions" ON public.user_sessions FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update own sessions" ON public.user_sessions FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY "Users can view own programs" ON public.user_programs FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own programs" ON public.user_programs FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update own programs" ON public.user_programs FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY "Users can view own mood logs" ON public.mood_logs FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own mood logs" ON public.mood_logs FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can view own crisis events" ON public.crisis_events FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own crisis events" ON public.crisis_events FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can view own analytics" ON public.user_analytics FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can update own analytics" ON public.user_analytics FOR UPDATE USING (auth.uid() = user_id);

-- Public access for sessions and helplines
CREATE POLICY "Sessions are viewable by everyone" ON public.sessions FOR SELECT USING (is_active = true);
CREATE POLICY "Helplines are viewable by everyone" ON public.emergency_helplines FOR SELECT USING (is_active = true);

-- Functions for automatic timestamp updates
CREATE OR REPLACE FUNCTION public.handle_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers for updated_at
CREATE TRIGGER handle_profiles_updated_at BEFORE UPDATE ON public.profiles FOR EACH ROW EXECUTE FUNCTION public.handle_updated_at();
CREATE TRIGGER handle_sessions_updated_at BEFORE UPDATE ON public.sessions FOR EACH ROW EXECUTE FUNCTION public.handle_updated_at();
CREATE TRIGGER handle_programs_updated_at BEFORE UPDATE ON public.programs FOR EACH ROW EXECUTE FUNCTION public.handle_updated_at();
CREATE TRIGGER handle_user_sessions_updated_at BEFORE UPDATE ON public.user_sessions FOR EACH ROW EXECUTE FUNCTION public.handle_updated_at();
CREATE TRIGGER handle_user_programs_updated_at BEFORE UPDATE ON public.user_programs FOR EACH ROW EXECUTE FUNCTION public.handle_updated_at();
CREATE TRIGGER handle_user_analytics_updated_at BEFORE UPDATE ON public.user_analytics FOR EACH ROW EXECUTE FUNCTION public.handle_updated_at();

-- Insert sample emergency helplines for India
INSERT INTO public.emergency_helplines (name, phone, description, country, priority) VALUES
('iCall', '+91 9152987821', '24/7 mental health helpline', 'IN', 1),
('Vandrevala Foundation', '+91 9999666555', 'Mental health support and counseling', 'IN', 2),
('NIMHANS', '+91 080 26995001', 'National Institute of Mental Health Helpline', 'IN', 3),
('Snehi', '+91 91 22 2772 7744', 'Emotional support helpline', 'IN', 4);

-- Function to check and update user risk status
CREATE OR REPLACE FUNCTION public.update_user_risk_status(user_uuid UUID)
RETURNS BOOLEAN AS $$
DECLARE
  recent_crisis_count INTEGER;
  avg_mood_score DECIMAL;
BEGIN
  -- Count crisis events in last 7 days
  SELECT COUNT(*) INTO recent_crisis_count
  FROM public.crisis_events
  WHERE user_id = user_uuid
    AND created_at >= NOW() - INTERVAL '7 days'
    AND severity IN ('high', 'critical');
  
  -- Get average mood score from last 7 days
  SELECT COALESCE(AVG(mood_score), 10) INTO avg_mood_score
  FROM public.mood_logs
  WHERE user_id = user_uuid
    AND created_at >= NOW() - INTERVAL '7 days';
  
  -- Update risk status
  UPDATE public.profiles
  SET is_high_risk = (recent_crisis_count >= 2 OR avg_mood_score <= 3),
      last_crisis_check = NOW()
  WHERE id = user_uuid;
  
  RETURN (recent_crisis_count >= 2 OR avg_mood_score <= 3);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
