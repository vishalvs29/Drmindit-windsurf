-- Sample Data for DrMindit Supabase Project
-- Insert sample sessions and steps

-- Insert sample sessions
INSERT INTO public.sessions (title, description, total_duration, category, difficulty_level, thumbnail_url, audio_url) VALUES
('Evening Meditation', 'A calming meditation to help you unwind and prepare for restful sleep', 900, 'meditation', 'beginner', 'https://cdn.drmindit.com/sessions/evening-meditation.jpg', 'https://cdn.drmindit.com/audio/evening-meditation.mp3'),
('Stress Relief Breathing', 'Deep breathing exercises to reduce stress and anxiety', 600, 'breathing', 'beginner', 'https://cdn.drmindit.com/sessions/breathing.jpg', 'https://cdn.drmindit.com/audio/breathing.mp3'),
('Focus Enhancement', 'Techniques to improve concentration and mental clarity', 1200, 'focus', 'intermediate', 'https://cdn.drmindit.com/sessions/focus.jpg', 'https://cdn.drmindit.com/audio/focus.mp3'),
('Sleep Journey', 'Guided imagery to help you fall asleep naturally', 1800, 'sleep', 'beginner', 'https://cdn.drmindit.com/sessions/sleep.jpg', 'https://cdn.drmindit.com/audio/sleep.mp3'),
('Morning Mindfulness', 'Start your day with intention and clarity', 450, 'meditation', 'beginner', 'https://cdn.drmindit.com/sessions/morning.jpg', 'https://cdn.drmindit.com/audio/morning.mp3'),
('Anxiety Relief', 'Gentle techniques to calm anxiety and find inner peace', 750, 'meditation', 'beginner', 'https://cdn.drmindit.com/sessions/anxiety-relief.jpg', 'https://cdn.drmindit.com/audio/anxiety-relief.mp3'),
('Deep Relaxation', 'Progressive muscle relaxation for complete body release', 1500, 'meditation', 'intermediate', 'https://cdn.drmindit.com/sessions/deep-relaxation.jpg', 'https://cdn.drmindit.com/audio/deep-relaxation.mp3'),
('Body Scan', 'Complete body awareness meditation for deep relaxation', 1200, 'meditation', 'beginner', 'https://cdn.drmindit.com/sessions/body-scan.jpg', 'https://cdn.drmindit.com/audio/body-scan.mp3'),
('Loving Kindness', 'Cultivate compassion and kindness towards yourself and others', 900, 'meditation', 'intermediate', 'https://cdn.drmindit.com/sessions/loving-kindness.jpg', 'https://cdn.drmindit.com/audio/loving-kindness.mp3'),
('Mindful Walking', 'Walking meditation for connecting with your body and environment', 600, 'meditation', 'beginner', 'https://cdn.drmindit.com/sessions/mindful-walking.jpg', 'https://cdn.drmindit.com/audio/mindful-walking.mp3');

-- Insert session steps for Evening Meditation
INSERT INTO public.session_steps (session_id, title, description, audio_url, duration, order_index, step_type) VALUES
((SELECT id FROM public.sessions WHERE title = 'Evening Meditation' LIMIT 1), 'Settle Into Comfort', 'Find a comfortable position and allow your body to relax', 'https://cdn.drmindit.com/audio/evening-meditation/step-1.mp3', 120, 0, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Evening Meditation' LIMIT 1), 'Body Scan Relaxation', 'Progressive relaxation through your entire body', 'https://cdn.drmindit.com/audio/evening-meditation/step-2.mp3', 300, 1, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Evening Meditation' LIMIT 1), 'Mindful Breathing', 'Focus on your breath and let go of the day', 'https://cdn.drmindit.com/audio/evening-meditation/step-3.mp3', 240, 2, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Evening Meditation' LIMIT 1), 'Gentle Return', 'Slowly return to awareness with a peaceful mind', 'https://cdn.drmindit.com/audio/evening-meditation/step-4.mp3', 240, 3, 'audio');

-- Insert session steps for Stress Relief Breathing
INSERT INTO public.session_steps (session_id, title, description, audio_url, duration, order_index, step_type) VALUES
((SELECT id FROM public.sessions WHERE title = 'Stress Relief Breathing' LIMIT 1), 'Introduction to Breathing', 'Learn the basics of deep breathing for stress relief', 'https://cdn.drmindit.com/audio/breathing/step-1.mp3', 180, 0, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Stress Relief Breathing' LIMIT 1), '4-7-8 Breathing Technique', 'Practice the powerful 4-7-8 breathing pattern', 'https://cdn.drmindit.com/audio/breathing/step-2.mp3', 240, 1, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Stress Relief Breathing' LIMIT 1), 'Box Breathing', 'Equal breathing for balance and calm', 'https://cdn.drmindit.com/audio/breathing/step-3.mp3', 180, 2, 'audio');

-- Insert session steps for Focus Enhancement
INSERT INTO public.session_steps (session_id, title, description, audio_url, duration, order_index, step_type) VALUES
((SELECT id FROM public.sessions WHERE title = 'Focus Enhancement' LIMIT 1), 'Grounding Exercise', 'Anchor your awareness in the present moment', 'https://cdn.drmindit.com/audio/focus/step-1.mp3', 180, 0, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Focus Enhancement' LIMIT 1), 'Concentration Practice', 'Train your mind to stay focused on one object', 'https://cdn.drmindit.com/audio/focus/step-2.mp3', 480, 1, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Focus Enhancement' LIMIT 1), 'Mindful Observation', 'Practice observing thoughts without judgment', 'https://cdn.drmindit.com/audio/focus/step-3.mp3', 300, 2, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Focus Enhancement' LIMIT 1), 'Integration', 'Bring focused awareness into daily activities', 'https://cdn.drmindit.com/audio/focus/step-4.mp3', 240, 3, 'audio');

-- Insert session steps for Sleep Journey
INSERT INTO public.session_steps (session_id, title, description, audio_url, duration, order_index, step_type) VALUES
((SELECT id FROM public.sessions WHERE title = 'Sleep Journey' LIMIT 1), 'Bedtime Preparation', 'Create the perfect environment for restful sleep', 'https://cdn.drmindit.com/audio/sleep/step-1.mp3', 300, 0, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Sleep Journey' LIMIT 1), 'Body Relaxation', 'Release tension from every part of your body', 'https://cdn.drmindit.com/audio/sleep/step-2.mp3', 600, 1, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Sleep Journey' LIMIT 1), 'Peaceful Imagery', 'Visualize a peaceful place for deep relaxation', 'https://cdn.drmindit.com/audio/sleep/step-3.mp3', 600, 2, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Sleep Journey' LIMIT 1), 'Drifting Off', 'Allow yourself to naturally fall into sleep', 'https://cdn.drmindit.com/audio/sleep/step-4.mp3', 300, 3, 'audio');

-- Insert session steps for Morning Mindfulness
INSERT INTO public.session_steps (session_id, title, description, audio_url, duration, order_index, step_type) VALUES
((SELECT id FROM public.sessions WHERE title = 'Morning Mindfulness' LIMIT 1), 'Waking Awareness', 'Gently bring attention to the present moment', 'https://cdn.drmindit.com/audio/morning/step-1.mp3', 120, 0, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Morning Mindfulness' LIMIT 1), 'Setting Intentions', 'Choose your focus for the day ahead', 'https://cdn.drmindit.com/audio/morning/step-2.mp3', 180, 1, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Morning Mindfulness' LIMIT 1), 'Mindful Movement', 'Gentle stretches to awaken your body', 'https://cdn.drmindit.com/audio/morning/step-3.mp3', 150, 2, 'audio');

-- Verification query
SELECT 
    s.title as session_title,
    COUNT(ss.id) as step_count,
    s.total_duration as total_duration_seconds,
    s.category
FROM public.sessions s
LEFT JOIN public.session_steps ss ON s.id = ss.session_id
GROUP BY s.id, s.title, s.total_duration, s.category
ORDER BY s.title;
