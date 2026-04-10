-- DrMindit Clean Schema for New Supabase Project
-- https://supabase.com/dashboard/project/nlheesoshtczdhsqzjid

-- Enable necessary extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Sessions table for meditation/audio content
CREATE TABLE public.sessions (
  id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
  title TEXT NOT NULL,
  description TEXT,
  total_duration INTEGER NOT NULL, -- in seconds
  category TEXT,
  difficulty_level TEXT DEFAULT 'beginner',
  thumbnail_url TEXT,
  audio_url TEXT NOT NULL,
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Session steps table for individual audio steps
CREATE TABLE public.session_steps (
  id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
  session_id UUID REFERENCES public.sessions(id) ON DELETE CASCADE,
  title TEXT NOT NULL,
  description TEXT,
  audio_url TEXT NOT NULL,
  duration INTEGER NOT NULL, -- in seconds
  order_index INTEGER NOT NULL,
  step_type TEXT DEFAULT 'audio',
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  UNIQUE(session_id, order_index)
);

-- User session progress tracking
CREATE TABLE public.user_session_progress (
  id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
  user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
  session_id UUID REFERENCES public.sessions(id) ON DELETE CASCADE,
  current_step_index INTEGER DEFAULT 0,
  progress_seconds INTEGER DEFAULT 0,
  completed_steps JSONB DEFAULT '[]',
  is_completed BOOLEAN DEFAULT false,
  started_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  completed_at TIMESTAMP WITH TIME ZONE,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  UNIQUE(user_id, session_id)
);

-- User profiles (extends auth.users)
CREATE TABLE public.profiles (
  id UUID REFERENCES auth.users(id) ON DELETE CASCADE PRIMARY KEY,
  first_name TEXT,
  last_name TEXT,
  avatar_url TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- User preferences
CREATE TABLE public.user_preferences (
  id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
  user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
  preferences JSONB DEFAULT '{}',
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  UNIQUE(user_id)
);

-- User mood ratings
CREATE TABLE public.user_mood_ratings (
  id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
  user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
  session_id UUID REFERENCES public.sessions(id) ON DELETE CASCADE,
  mood_before INTEGER CHECK (mood_before >= 1 AND mood_before <= 10),
  mood_after INTEGER CHECK (mood_after >= 1 AND mood_after <= 10),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Session events for analytics
CREATE TABLE public.session_events (
  id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
  user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
  session_id UUID REFERENCES public.sessions(id) ON DELETE CASCADE,
  event_type TEXT NOT NULL,
  event_data JSONB DEFAULT '{}',
  device_info JSONB DEFAULT '{}',
  timestamp TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create indexes for performance
CREATE INDEX idx_sessions_active ON public.sessions(is_active);
CREATE INDEX idx_sessions_category ON public.sessions(category);
CREATE INDEX idx_sessions_created_at ON public.sessions(created_at);

CREATE INDEX idx_session_steps_session_id ON public.session_steps(session_id);
CREATE INDEX idx_session_steps_order ON public.session_steps(session_id, order_index);

CREATE INDEX idx_user_session_progress_user_id ON public.user_session_progress(user_id);
CREATE INDEX idx_user_session_progress_session_id ON public.user_session_progress(session_id);
CREATE INDEX idx_user_session_progress_completed ON public.user_session_progress(is_completed);
CREATE INDEX idx_user_session_progress_updated_at ON public.user_session_progress(updated_at);

CREATE INDEX idx_user_mood_ratings_user_id ON public.user_mood_ratings(user_id);
CREATE INDEX idx_user_mood_ratings_session_id ON public.user_mood_ratings(session_id);
CREATE INDEX idx_user_mood_ratings_created_at ON public.user_mood_ratings(created_at);

CREATE INDEX idx_user_preferences_user_id ON public.user_preferences(user_id);
CREATE INDEX idx_user_preferences_updated_at ON public.user_preferences(updated_at);

CREATE INDEX idx_session_events_user_id ON public.session_events(user_id);
CREATE INDEX idx_session_events_session_id ON public.session_events(session_id);
CREATE INDEX idx_session_events_timestamp ON public.session_events(timestamp);
CREATE INDEX idx_session_events_type ON public.session_events(event_type);

CREATE INDEX idx_profiles_user_id ON public.profiles(id);
CREATE INDEX idx_profiles_updated_at ON public.profiles(updated_at);

-- Enable Row Level Security (RLS)
ALTER TABLE public.sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.session_steps ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_session_progress ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_preferences ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_mood_ratings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.session_events ENABLE ROW LEVEL SECURITY;

-- RLS Policies

-- Sessions: Public read access
CREATE POLICY "Sessions are viewable by everyone" ON public.sessions FOR SELECT USING (is_active = true);

-- Session steps: Public read access (through sessions)
CREATE POLICY "Session steps are viewable by everyone" ON public.session_steps FOR SELECT USING (
  EXISTS (
    SELECT 1 FROM public.sessions 
    WHERE sessions.id = session_steps.session_id 
    AND sessions.is_active = true
  )
);

-- User session progress: Only user's own data
CREATE POLICY "Users can view own session progress" ON public.user_session_progress FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own session progress" ON public.user_session_progress FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update own session progress" ON public.user_session_progress FOR UPDATE USING (auth.uid() = user_id);

-- Profiles: Only user's own data
CREATE POLICY "Users can view own profile" ON public.profiles FOR SELECT USING (auth.uid() = id);
CREATE POLICY "Users can update own profile" ON public.profiles FOR UPDATE USING (auth.uid() = id);

-- User preferences: Only user's own data
CREATE POLICY "Users can view own preferences" ON public.user_preferences FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own preferences" ON public.user_preferences FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update own preferences" ON public.user_preferences FOR UPDATE USING (auth.uid() = user_id);

-- User mood ratings: Only user's own data
CREATE POLICY "Users can view own mood ratings" ON public.user_mood_ratings FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own mood ratings" ON public.user_mood_ratings FOR INSERT WITH CHECK (auth.uid() = user_id);

-- Session events: Only user's own data
CREATE POLICY "Users can view own session events" ON public.session_events FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own session events" ON public.session_events FOR INSERT WITH CHECK (auth.uid() = user_id);

-- Function for automatic timestamp updates
CREATE OR REPLACE FUNCTION public.handle_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers for updated_at columns
CREATE TRIGGER handle_sessions_updated_at BEFORE UPDATE ON public.sessions FOR EACH ROW EXECUTE FUNCTION public.handle_updated_at();
CREATE TRIGGER handle_user_session_progress_updated_at BEFORE UPDATE ON public.user_session_progress FOR EACH ROW EXECUTE FUNCTION public.handle_updated_at();
CREATE TRIGGER handle_profiles_updated_at BEFORE UPDATE ON public.profiles FOR EACH ROW EXECUTE FUNCTION public.handle_updated_at();
CREATE TRIGGER handle_user_preferences_updated_at BEFORE UPDATE ON public.user_preferences FOR EACH ROW EXECUTE FUNCTION public.handle_updated_at();

-- Function to create user profile automatically
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO public.profiles (id, first_name, last_name)
  VALUES (NEW.id, NEW.raw_user_meta_data->>'first_name', NEW.raw_user_meta_data->>'last_name');
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger to create profile on user signup
CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- Function to create user preferences automatically
CREATE OR REPLACE FUNCTION public.create_user_preferences()
RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO public.user_preferences (user_id, preferences)
  VALUES (NEW.id, '{"theme": "light", "notifications": true, "language": "en"}'::jsonb);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger to create preferences on user signup
CREATE TRIGGER create_user_preferences_trigger
  AFTER INSERT ON public.profiles
  FOR EACH ROW EXECUTE FUNCTION public.create_user_preferences();
