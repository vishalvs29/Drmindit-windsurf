-- DrMindit Supabase Storage Setup - FIXED
-- Configure secure audio file storage without modifying system tables

-- Create storage buckets
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES 
('sessions-audio', 'sessions-audio', true, 52428800, ARRAY['audio/mpeg', 'audio/wav', 'audio/mp3', 'audio/ogg', 'audio/m4a']),
('user-avatars', 'user-avatars', false, 10485760, ARRAY['image/jpeg', 'image/png', 'image/webp']),
('session-thumbnails', 'session-thumbnails', true, 2097152, ARRAY['image/jpeg', 'image/png', 'image/webp'])
ON CONFLICT (id) DO NOTHING;

-- Storage policies for sessions-audio bucket (public read access)
CREATE POLICY "Public audio files are viewable by everyone" ON storage.objects
    FOR SELECT USING (
        bucket_id = 'sessions-audio' AND 
        (storage.foldername(name))[1] = 'public'
    );

CREATE POLICY "Anyone can upload public audio files" ON storage.objects
    FOR INSERT WITH CHECK (
        bucket_id = 'sessions-audio' AND 
        (storage.foldername(name))[1] = 'public'
    );

CREATE POLICY "Anyone can update public audio files" ON storage.objects
    FOR UPDATE USING (
        bucket_id = 'sessions-audio' AND 
        (storage.foldername(name))[1] = 'public'
    );

-- Storage policies for user-avatars bucket (user-specific access)
CREATE POLICY "Users can view own avatar" ON storage.objects
    FOR SELECT USING (
        bucket_id = 'user-avatars' AND 
        auth.uid()::text = (storage.foldername(name))[1]
    );

CREATE POLICY "Users can upload own avatar" ON storage.objects
    FOR INSERT WITH CHECK (
        bucket_id = 'user-avatars' AND 
        auth.uid()::text = (storage.foldername(name))[1]
    );

CREATE POLICY "Users can update own avatar" ON storage.objects
    FOR UPDATE USING (
        bucket_id = 'user-avatars' AND 
        auth.uid()::text = (storage.foldername(name))[1]
    );

-- Storage policies for session-thumbnails bucket (public read access)
CREATE POLICY "Public thumbnails are viewable by everyone" ON storage.objects
    FOR SELECT USING (bucket_id = 'session-thumbnails');

CREATE POLICY "Anyone can upload thumbnails" ON storage.objects
    FOR INSERT WITH CHECK (bucket_id = 'session-thumbnails');

CREATE POLICY "Anyone can update thumbnails" ON storage.objects
    FOR UPDATE USING (bucket_id = 'session-thumbnails');

-- Grant permissions
GRANT ALL ON storage.buckets TO authenticated;
GRANT ALL ON storage.objects TO authenticated;
GRANT SELECT ON storage.buckets TO anon;
GRANT SELECT ON storage.objects TO anon;

-- Function to generate signed URLs for private content
CREATE OR REPLACE FUNCTION public.generate_signed_url(file_path TEXT, expires_in INTERVAL DEFAULT INTERVAL '1 hour')
RETURNS TEXT AS $$
DECLARE
    signed_url TEXT;
BEGIN
    -- This would typically use storage.sign_url() function
    -- For now, return the public URL as placeholder
    SELECT storage.get_url(file_path) INTO signed_url;
    RETURN signed_url;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to check if user has access to premium content
CREATE OR REPLACE FUNCTION public.user_has_premium_access(user_uuid UUID)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM public.profiles 
        WHERE id = user_uuid 
        AND subscription_level = 'premium'
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to get accessible sessions for user
CREATE OR REPLACE FUNCTION public.get_accessible_sessions(user_uuid UUID DEFAULT NULL)
RETURNS TABLE (
    id UUID,
    title TEXT,
    description TEXT,
    total_duration INTEGER,
    category TEXT,
    difficulty_level TEXT,
    instructor TEXT,
    thumbnail_url TEXT,
    is_premium BOOLEAN,
    rating DECIMAL,
    total_ratings BIGINT,
    is_accessible BOOLEAN
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        s.id,
        s.title,
        s.description,
        s.total_duration,
        s.category::text,
        s.difficulty_level,
        s.instructor,
        s.thumbnail_url,
        s.is_premium,
        s.rating,
        s.total_ratings,
        (NOT s.is_premium OR public.user_has_premium_access(user_uuid)) as is_accessible
    FROM public.sessions s
    WHERE s.is_active = true
    ORDER BY s.rating DESC, s.total_ratings DESC;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to get session with steps for user
CREATE OR REPLACE FUNCTION public.get_session_with_steps(session_uuid UUID, user_uuid UUID DEFAULT NULL)
RETURNS TABLE (
    id UUID,
    title TEXT,
    description TEXT,
    total_duration INTEGER,
    category TEXT,
    difficulty_level TEXT,
    instructor TEXT,
    thumbnail_url TEXT,
    is_premium BOOLEAN,
    rating DECIMAL,
    total_ratings BIGINT,
    is_accessible BOOLEAN,
    steps JSONB
) AS $$
DECLARE
    session_accessible BOOLEAN;
BEGIN
    -- Check if user has access to this session
    session_accessible := (
        SELECT NOT s.is_premium OR public.user_has_premium_access(user_uuid)
        FROM public.sessions s 
        WHERE s.id = session_uuid AND s.is_active = true
    );
    
    RETURN QUERY
    SELECT 
        s.id,
        s.title,
        s.description,
        s.total_duration,
        s.category::text,
        s.difficulty_level,
        s.instructor,
        s.thumbnail_url,
        s.is_premium,
        s.rating,
        s.total_ratings,
        session_accessible,
        COALESCE(
            jsonb_agg(
                jsonb_build_object(
                    'id', ss.id,
                    'title', ss.title,
                    'description', ss.description,
                    'audio_url', ss.audio_url,
                    'duration', ss.duration,
                    'order_index', ss.order_index,
                    'step_type', ss.step_type
                ) ORDER BY ss.order_index
            ), '[]'::jsonb
        ) as steps
    FROM public.sessions s
    LEFT JOIN public.session_steps ss ON s.id = ss.session_id AND ss.is_active = true
    WHERE s.id = session_uuid AND s.is_active = true
    GROUP BY s.id, s.title, s.description, s.total_duration, s.category, s.difficulty_level, 
             s.instructor, s.thumbnail_url, s.is_premium, s.rating, s.total_ratings;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to update user progress with validation
CREATE OR REPLACE FUNCTION public.update_session_progress(
    user_uuid UUID,
    session_uuid UUID,
    current_step INTEGER DEFAULT NULL,
    progress_seconds INTEGER DEFAULT NULL,
    completed_steps JSONB DEFAULT NULL,
    is_completed BOOLEAN DEFAULT NULL
)
RETURNS public.user_session_progress AS $$
DECLARE
    progress_record public.user_session_progress;
    session_accessible BOOLEAN;
BEGIN
    -- Check if user has access to this session
    session_accessible := (
        SELECT NOT s.is_premium OR public.user_has_premium_access(user_uuid)
        FROM public.sessions s 
        WHERE s.id = session_uuid AND s.is_active = true
    );
    
    IF NOT session_accessible THEN
        RAISE EXCEPTION 'User does not have access to this premium session';
    END IF;
    
    -- Update or insert progress
    INSERT INTO public.user_session_progress (
        user_id, session_id, current_step_index, progress_seconds, 
        completed_steps, is_completed, last_played_at, updated_at
    ) VALUES (
        user_uuid, session_uuid, 
        COALESCE(current_step, current_step_index),
        COALESCE(progress_seconds, progress_seconds),
        COALESCE(completed_steps, completed_steps),
        COALESCE(is_completed, is_completed),
        NOW(), NOW()
    )
    ON CONFLICT (user_id, session_id) 
    DO UPDATE SET
        current_step_index = COALESCE(EXCLUDED.current_step_index, user_session_progress.current_step_index),
        progress_seconds = COALESCE(EXCLUDED.progress_seconds, user_session_progress.progress_seconds),
        completed_steps = COALESCE(EXCLUDED.completed_steps, user_session_progress.completed_steps),
        is_completed = COALESCE(EXCLUDED.is_completed, user_session_progress.is_completed),
        last_played_at = NOW(),
        updated_at = NOW()
    RETURNING * INTO progress_record;
    
    -- Create analytics event
    INSERT INTO public.session_events (
        user_id, session_id, event_type, event_data, created_at
    ) VALUES (
        user_uuid, session_uuid, 'PROGRESS_UPDATED', 
        jsonb_build_object(
            'current_step', progress_record.current_step_index,
            'progress_seconds', progress_record.progress_seconds,
            'is_completed', progress_record.is_completed
        ),
        NOW()
    );
    
    RETURN progress_record;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to log session events
CREATE OR REPLACE FUNCTION public.log_session_event(
    user_uuid UUID,
    session_uuid UUID,
    event_type public.event_type,
    step_index INTEGER DEFAULT NULL,
    event_data JSONB DEFAULT '{}'::jsonb,
    device_info JSONB DEFAULT '{}'::jsonb
)
RETURNS UUID AS $$
DECLARE
    event_id UUID;
    session_accessible BOOLEAN;
BEGIN
    -- Check if user has access to this session
    session_accessible := (
        SELECT NOT s.is_premium OR public.user_has_premium_access(user_uuid)
        FROM public.sessions s 
        WHERE s.id = session_uuid AND s.is_active = true
    );
    
    IF NOT session_accessible THEN
        RAISE EXCEPTION 'User does not have access to this session';
    END IF;
    
    -- Insert event
    INSERT INTO public.session_events (
        user_id, session_id, step_index, event_type, event_data, device_info, created_at
    ) VALUES (
        user_uuid, session_uuid, step_index, event_type, event_data, device_info, NOW()
    ) RETURNING id INTO event_id;
    
    RETURN event_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Grant usage of functions to authenticated users
GRANT EXECUTE ON FUNCTION public.generate_signed_url TO authenticated;
GRANT EXECUTE ON FUNCTION public.user_has_premium_access TO authenticated;
GRANT EXECUTE ON FUNCTION public.get_accessible_sessions TO authenticated;
GRANT EXECUTE ON FUNCTION public.get_session_with_steps TO authenticated;
GRANT EXECUTE ON FUNCTION public.update_session_progress TO authenticated;
GRANT EXECUTE ON FUNCTION public.log_session_event TO authenticated;

-- Grant read-only access to anon users for public functions
GRANT EXECUTE ON FUNCTION public.get_accessible_sessions TO anon;
