-- DrMindit Production Sample Data
-- High-quality mental wellness content

-- Insert production sessions
INSERT INTO public.sessions (title, description, total_duration, category, difficulty_level, instructor, thumbnail_url, is_active, tags, rating, total_ratings) VALUES
-- Beginner Sessions
('Morning Mindfulness', 'Start your day with clarity and intention through this gentle 10-minute mindfulness practice', 600, 'meditation', 'beginner', 'Sarah Chen', 'https://cdn.drmindit.com/thumbnails/morning-mindfulness.jpg', true, '{morning, mindfulness, beginner}', 4.8, 1247),
('Evening Wind Down', 'Release the day''s stress and prepare for restful sleep with this calming evening meditation', 900, 'sleep', 'beginner', 'Michael Rivers', 'https://cdn.drmindit.com/thumbnails/evening-wind-down.jpg', true, '{evening, sleep, relaxation}', 4.7, 892),
('Quick Stress Relief', '5-minute breathing exercise for instant stress reduction during busy moments', 300, 'stress_relief', 'beginner', 'Emma Wilson', 'https://cdn.drmindit.com/thumbnails/quick-stress-relief.jpg', true, '{stress, breathing, quick}', 4.6, 2156),

-- Intermediate Sessions
('Focus Enhancement', 'Improve concentration and mental clarity with this 20-minute focused attention meditation', 1200, 'focus', 'intermediate', 'Dr. James Park', 'https://cdn.drmindit.com/thumbnails/focus-enhancement.jpg', true, '{focus, productivity, intermediate}', 4.9, 743),
('Anxiety Relief Journey', 'A comprehensive 25-minute session to manage anxiety through proven techniques', 1500, 'anxiety', 'intermediate', 'Dr. Lisa Chen', 'https://cdn.drmindit.com/thumbnails/anxiety-relief.jpg', true, '{anxiety, relief, intermediate}', 4.8, 512),
('Deep Body Scan', 'Progressive muscle relaxation for complete physical and mental release', 1800, 'body_scan', 'intermediate', 'Robert Stone', 'https://cdn.drmindit.com/thumbnails/deep-body-scan.jpg', true, '{body_scan, relaxation, intermediate}', 4.7, 634),

-- Advanced Sessions
('Advanced Concentration', 'Challenge your focus with this 45-minute advanced concentration practice', 2700, 'focus', 'advanced', 'Master Wei', 'https://cdn.drmindit.com/thumbnails/advanced-concentration.jpg', true, '{focus, advanced, concentration}', 4.9, 287),
('Deep Meditation Journey', 'Experience profound states of awareness in this 60-minute advanced meditation', 3600, 'meditation', 'advanced', 'Guru Ananda', 'https://cdn.drmindit.com/thumbnails/deep-meditation.jpg', true, '{meditation, advanced, spiritual}', 4.8, 195),

-- Breathing Sessions
('4-7-8 Breathing', 'Master the powerful 4-7-8 breathing technique for instant calm and better sleep', 600, 'breathing', 'beginner', 'Sarah Chen', 'https://cdn.drmindit.com/thumbnails/4-7-8-breathing.jpg', true, '{breathing, 4-7-8, sleep}', 4.8, 1567),
('Box Breathing', 'Learn Navy SEAL breathing technique for stress management and focus', 450, 'breathing', 'beginner', 'Michael Rivers', 'https://cdn.drmindit.com/thumbnails/box-breathing.jpg', true, '{breathing, focus, military}', 4.7, 892),

-- Sleep Sessions
('Bedtime Stories', 'Gentle storytelling to help you drift off into peaceful sleep', 2400, 'sleep', 'beginner', 'Emma Wilson', 'https://cdn.drmindit.com/thumbnails/bedtime-stories.jpg', true, '{sleep, stories, relaxation}', 4.9, 2103),
('Ocean Waves Sleep', 'Fall asleep to the calming sounds of ocean waves with guided relaxation', 1800, 'sleep', 'beginner', 'Nature Sounds', 'https://cdn.drmindit.com/thumbnails/ocean-waves.jpg', true, '{sleep, nature, waves}', 4.8, 1876),

-- Mindfulness Sessions
('Mindful Walking', 'Practice mindfulness while moving with this guided walking meditation', 900, 'mindfulness', 'beginner', 'Dr. James Park', 'https://cdn.drmindit.com/thumbnails/mindful-walking.jpg', true, '{mindfulness, walking, movement}', 4.6, 543),
('Eating Mindfully', 'Transform your relationship with food through mindful eating practice', 1200, 'mindfulness', 'intermediate', 'Lisa Chen', 'https://cdn.drmindit.com/thumbnails/eating-mindfully.jpg', true, '{mindfulness, eating, intermediate}', 4.7, 321);

-- Insert session steps for Morning Mindfulness
INSERT INTO public.session_steps (session_id, title, description, audio_url, duration, order_index, step_type) VALUES
((SELECT id FROM public.sessions WHERE title = 'Morning Mindfulness' LIMIT 1), 
 'Settle Into Position', 'Find a comfortable seated position and gently close your eyes', 
 'https://cdn.drmindit.com/audio/morning-mindfulness/step-1.mp3', 120, 0, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Morning Mindfulness' LIMIT 1), 
 'Body Awareness', 'Bring gentle attention to your body and notice any sensations', 
 'https://cdn.drmindit.com/audio/morning-mindfulness/step-2.mp3', 180, 1, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Morning Mindfulness' LIMIT 1), 
 'Breath Focus', 'Focus on the natural rhythm of your breath without trying to change it', 
 'https://cdn.drmindit.com/audio/morning-mindfulness/step-3.mp3', 180, 2, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Morning Mindfulness' LIMIT 1), 
 'Setting Intention', 'Set a clear intention for how you want to feel today', 
 'https://cdn.drmindit.com/audio/morning-mindfulness/step-4.mp3', 120, 3, 'audio');

-- Insert session steps for Evening Wind Down
INSERT INTO public.session_steps (session_id, title, description, audio_url, duration, order_index, step_type) VALUES
((SELECT id FROM public.sessions WHERE title = 'Evening Wind Down' LIMIT 1), 
 'Releasing the Day', 'Let go of the day''s events and tensions with conscious release', 
 'https://cdn.drmindit.com/audio/evening-wind-down/step-1.mp3', 240, 0, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Evening Wind Down' LIMIT 1), 
 'Body Scan Relaxation', 'Progressive relaxation through your entire body', 
 'https://cdn.drmindit.com/audio/evening-wind-down/step-2.mp3', 360, 1, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Evening Wind Down' LIMIT 1), 
 'Peaceful Imagery', 'Visualize a peaceful place to calm your mind', 
 'https://cdn.drmindit.com/audio/evening-wind-down/step-2.mp3', 180, 2, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Evening Wind Down' LIMIT 1), 
 'Drifting Off', 'Allow yourself to naturally fall into peaceful sleep', 
 'https://cdn.drmindit.com/audio/evening-wind-down/step-3.mp3', 120, 3, 'audio');

-- Insert session steps for Focus Enhancement
INSERT INTO public.session_steps (session_id, title, description, audio_url, duration, order_index, step_type) VALUES
((SELECT id FROM public.sessions WHERE title = 'Focus Enhancement' LIMIT 1), 
 'Grounding Practice', 'Anchor your awareness in the present moment', 
 'https://cdn.drmindit.com/audio/focus-enhancement/step-1.mp3', 300, 0, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Focus Enhancement' LIMIT 1), 
 'Concentration Training', 'Practice sustained attention on a single object', 
 'https://cdn.drmindit.com/audio/focus-enhancement/step-2.mp3', 600, 1, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Focus Enhancement' LIMIT 1), 
 'Mindful Observation', 'Learn to observe thoughts without getting carried away', 
 'https://cdn.drmindit.com/audio/focus-enhancement/step-3.mp3', 180, 2, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Focus Enhancement' LIMIT 1), 
 'Integration Practice', 'Bring focused awareness into your daily activities', 
 'https://cdn.drmindit.com/audio/focus-enhancement/step-4.mp3', 120, 3, 'audio');

-- Insert session steps for Quick Stress Relief
INSERT INTO public.session_steps (session_id, title, description, audio_url, duration, order_index, step_type) VALUES
((SELECT id FROM public.sessions WHERE title = 'Quick Stress Relief' LIMIT 1), 
 'Immediate Calm', 'Quick breathing technique for instant stress relief', 
 'https://cdn.drmindit.com/audio/quick-stress-relief/step-1.mp3', 120, 0, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Quick Stress Relief' LIMIT 1), 
 'Body Release', 'Release physical tension with simple movements', 
 'https://cdn.drmindit.com/audio/quick-stress-relief/step-2.mp3', 90, 1, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Quick Stress Relief' LIMIT 1), 
 'Mental Reset', 'Clear your mind with a quick mental reset technique', 
 'https://cdn.drmindit.com/audio/quick-stress-relief/step-3.mp3', 90, 2, 'audio');

-- Insert session steps for Anxiety Relief Journey
INSERT INTO public.session_steps (session_id, title, description, audio_url, duration, order_index, step_type) VALUES
((SELECT id FROM public.sessions WHERE title = 'Anxiety Relief Journey' LIMIT 1), 
 'Acknowledging Anxiety', 'Gently acknowledge and accept your anxious feelings', 
 'https://cdn.drmindit.com/audio/anxiety-relief/step-1.mp3', 300, 0, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Anxiety Relief Journey' LIMIT 1), 
 'Grounding Techniques', 'Use your senses to ground yourself in the present', 
 'https://cdn.drmindit.com/audio/anxiety-relief/step-2.mp3', 420, 1, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Anxiety Relief Journey' LIMIT 1), 
 'Breathing for Calm', 'Specific breathing patterns to reduce anxiety', 
 'https://cdn.drmindit.com/audio/anxiety-relief/step-3.mp3', 360, 2, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Anxiety Relief Journey' LIMIT 1), 
 'Cognitive Reframing', 'Gently reframe anxious thoughts', 
 'https://cdn.drmindit.com/audio/anxiety-relief/step-4.mp3', 240, 3, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Anxiety Relief Journey' LIMIT 1), 
 'Peaceful Future', 'Visualize a peaceful outcome', 
 'https://cdn.drmindit.com/audio/anxiety-relief/step-5.mp3', 180, 4, 'audio');

-- Insert session steps for Deep Body Scan
INSERT INTO public.session_steps (session_id, title, description, audio_url, duration, order_index, step_type) VALUES
((SELECT id FROM public.sessions WHERE title = 'Deep Body Scan' LIMIT 1), 
 'Preparation', 'Prepare for deep relaxation with proper positioning', 
 'https://cdn.drmindit.com/audio/body-scan/step-1.mp3', 240, 0, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Deep Body Scan' LIMIT 1), 
 'Lower Body Scan', 'Progressive relaxation from toes to hips', 
 'https://cdn.drmindit.com/audio/body-scan/step-2.mp3', 480, 1, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Deep Body Scan' LIMIT 1), 
 'Upper Body Scan', 'Progressive relaxation from torso to head', 
 'https://cdn.drmindit.com/audio/body-scan/step-3.mp3', 480, 2, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Deep Body Scan' LIMIT 1), 
 'Whole Body Integration', 'Integrate relaxation throughout your entire body', 
 'https://cdn.drmindit.com/audio/body-scan/step-4.mp3', 300, 3, 'audio'),
((SELECT id FROM public.sessions WHERE title = 'Deep Body Scan' LIMIT 1), 
 'Emerging Awareness', 'Gently return to full awareness', 
 'https://cdn.drmindit.com/audio/body-scan/step-5.mp3', 300, 4, 'audio');

-- Insert session steps for 4-7-8 Breathing
INSERT INTO public.session_steps (session_id, title, description, audio_url, duration, order_index, step_type) VALUES
((SELECT id FROM public.sessions WHERE title = '4-7-8 Breathing' LIMIT 1), 
 'Introduction', 'Learn the powerful 4-7-8 breathing technique', 
 'https://cdn.drmindit.com/audio/4-7-8-breathing/step-1.mp3', 120, 0, 'audio'),
((SELECT id FROM public.sessions WHERE title = '4-7-8 Breathing' LIMIT 1), 
 'Practice Rounds', 'Practice several rounds of 4-7-8 breathing', 
 'https://cdn.drmindit.com/audio/4-7-8-breathing/step-2.mp3', 360, 1, 'audio'),
((SELECT id FROM public.sessions WHERE title = '4-7-8 Breathing' LIMIT 1), 
 'Integration', 'Integrate this technique into your daily life', 
 'https://cdn.drmindit.com/audio/4-7-8-breathing/step-3.mp3', 120, 2, 'audio');

-- Verification query to check data insertion
SELECT 
    s.title as session_title,
    s.category,
    s.difficulty_level,
    s.total_duration,
    COUNT(ss.id) as step_count,
    s.rating,
    s.total_ratings
FROM public.sessions s
LEFT JOIN public.session_steps ss ON s.id = ss.session_id
WHERE s.is_active = true
GROUP BY s.id, s.title, s.category, s.difficulty_level, s.total_duration, s.rating, s.total_ratings
ORDER BY s.title;
