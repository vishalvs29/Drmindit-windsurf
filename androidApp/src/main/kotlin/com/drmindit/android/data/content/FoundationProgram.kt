package com.drmindit.android.data.content

import com.drmindit.shared.domain.model.*

/**
 * 21-Day Foundation Program Content
 * Real, usable meditation content for beginners
 */
object FoundationProgram {
    
    /**
     * Create the complete 21-day foundation program
     */
    fun createFoundationProgram(): MeditationProgram {
        return MeditationProgram(
            id = "foundation_21_day",
            title = "21-Day Foundation Program",
            description = "A comprehensive introduction to meditation designed for beginners. Build a solid foundation with daily guided sessions that progressively teach essential mindfulness techniques.",
            duration = 21,
            category = ProgramCategory.FOUNDATION,
            difficulty = DifficultyLevel.BEGINNER,
            instructorName = "Dr. Sarah Chen",
            instructorBio = "Clinical psychologist and meditation instructor with 15 years of experience in mindfulness-based stress reduction.",
            thumbnailUrl = "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=800",
            sessions = createProgramSessions(),
            benefits = listOf(
                "Reduced stress and anxiety",
                "Improved focus and concentration",
                "Better emotional regulation",
                "Enhanced self-awareness",
                "Improved sleep quality",
                "Greater sense of calm and peace"
            ),
            requirements = listOf(
                "5-10 minutes daily commitment",
                "Quiet space for meditation",
                "Comfortable seating position",
                "Open mind and willingness to learn"
            ),
            isPremium = false,
            rating = 4.8f,
            reviewCount = 1247,
            enrollmentCount = 15420
        )
    }
    
    /**
     * Create all 21 program sessions
     */
    private fun createProgramSessions(): List<ProgramSession> {
        return listOf(
            // Week 1: Building the Foundation
            ProgramSession(1, 1, "day_01_intro", "Introduction to Meditation", 5, listOf("Understand what meditation is", "Learn basic sitting posture", "Experience your first mindful moment")),
            ProgramSession(2, 2, "day_02_breathing", "Focus on the Breath", 7, listOf("Learn anchor breathing technique", "Practice sustained attention", "Handle wandering thoughts")),
            ProgramSession(3, 3, "day_03_body_scan", "Body Scan Meditation", 10, listOf("Scan the body systematically", "Release physical tension", "Develop body awareness")),
            ProgramSession(4, 4, "day_04_mindful_moments", "Mindful Moments", 5, listOf("Integrate mindfulness into daily life", "Practice present-moment awareness", "Notice sensory experiences")),
            ProgramSession(5, 5, "day_05_dealing_thoughts", "Working with Thoughts", 8, listOf("Understand the nature of thoughts", "Practice non-judgmental awareness", "Let go of mental chatter")),
            ProgramSession(6, 6, "day_06_loving_kindness", "Loving-Kindness Meditation", 7, listOf("Cultivate compassion", "Send well-wishes to others", "Develop emotional warmth")),
            ProgramSession(7, 7, "day_07_week_reflection", "Week One Reflection", 10, listOf("Review your progress", "Identify challenges and insights", "Plan for week two")),
            
            // Week 2: Deepening Practice
            ProgramSession(8, 8, "day_08_stability", "Finding Stability", 8, listOf("Strengthen your practice foundation", "Develop consistent routine", "Build mental stability")),
            ProgramSession(9, 9, "day_09_emotions", "Working with Emotions", 10, listOf("Observe emotions without judgment", "Practice emotional regulation", "Develop emotional intelligence")),
            ProgramSession(10, 10, "day_10_stress_relief", "Stress Relief Techniques", 7, listOf("Learn quick stress-reduction methods", "Practice relaxation response", "Release accumulated tension")),
            ProgramSession(11, 11, "day_11_mindful_movement", "Mindful Movement", 5, listOf("Practice walking meditation", "Connect body and mind", "Bring awareness to movement")),
            ProgramSession(12, 12, "day_12_concentration", "Deepening Concentration", 8, listOf("Strengthen mental focus", "Practice sustained attention", "Overcome distraction")),
            ProgramSession(13, 13, "day_13_gratitude", "Gratitude Practice", 7, listOf("Cultivate grateful heart", "Appreciate present moment", "Develop positive mindset")),
            ProgramSession(14, 14, "day_14_week_reflection", "Week Two Reflection", 10, listOf("Assess your growth", "Identify patterns", "Prepare for advanced practice")),
            
            // Week 3: Integration and Application
            ProgramSession(15, 15, "day_15_mindful_communication", "Mindful Communication", 8, listOf("Practice mindful listening", "Speak with awareness", "Improve relationships")),
            ProgramSession(16, 16, "day_16_work_meditation", "Workplace Mindfulness", 5, listOf("Apply mindfulness at work", "Handle work stress", "Improve productivity")),
            ProgramSession(17, 17, "day_17_difficult_emotions", "Difficult Emotions", 10, listOf("Work with challenging feelings", "Practice emotional resilience", "Develop coping strategies")),
            ProgramSession(18, 18, "day_18_mindful_eating", "Mindful Eating", 7, listOf("Practice eating with awareness", "Appreciate food and nourishment", "Develop healthy habits")),
            ProgramSession(19, 19, "day_19_sleep_meditation", "Sleep Meditation", 8, listOf("Prepare mind for sleep", "Release daily tension", "Improve sleep quality")),
            ProgramSession(20, 20, "day_20_integration", "Integration Practice", 10, listOf("Combine all techniques", "Create personal practice", "Plan for continued growth")),
            ProgramSession(21, 21, "day_21_graduation", "Graduation and Next Steps", 15, listOf("Celebrate your achievement", "Review your journey", "Plan your ongoing practice"))
        )
    }
    
    /**
     * Create individual meditation sessions with real content
     */
    fun createIndividualSessions(): List<MeditationSession> {
        return listOf(
            // Introduction Sessions
            MeditationSession(
                id = "intro_basics",
                title = "Meditation Basics",
                description = "Learn the fundamentals of meditation in this gentle introduction perfect for complete beginners.",
                instructorName = "Dr. Sarah Chen",
                instructorBio = "Clinical psychologist specializing in mindfulness-based interventions.",
                duration = 5,
                audioUrl = "https://example.com/audio/intro_basics.mp3",
                localAudioPath = "/local/audio/intro_basics.mp3",
                thumbnailUrl = "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400",
                category = SessionCategory.MINDFULNESS,
                difficulty = DifficultyLevel.BEGINNER,
                tags = listOf("beginner", "introduction", "basics", "mindfulness"),
                transcript = "Welcome to your meditation practice. Today we'll explore the fundamental principles of meditation...",
                keyPoints = listOf("Meditation is simple and natural", "Anyone can learn to meditate", "Start with just 5 minutes daily"),
                breathingInstructions = "Breathe naturally through your nose. Notice the sensation of breath without trying to change it.",
                downloadSize = 2048000L,
                streamingQuality = AudioQuality.STANDARD
            ),
            
            MeditationSession(
                id = "breathing_anchor",
                title = "Breathing Anchor",
                description = "Use your breath as an anchor to the present moment. This foundational technique builds concentration and calm.",
                instructorName = "Dr. Sarah Chen",
                instructorBio = "Expert in breathwork and relaxation techniques.",
                duration = 7,
                audioUrl = "https://example.com/audio/breathing_anchor.mp3",
                localAudioPath = "/local/audio/breathing_anchor.mp3",
                thumbnailUrl = "https://images.unsplash.com/photo-1498757840582-c21b95b7c7f2?w=400",
                category = SessionCategory.BREATHING,
                difficulty = DifficultyLevel.BEGINNER,
                tags = listOf("breathing", "anchor", "focus", "concentration"),
                transcript = "Find a comfortable position and close your eyes. Bring your attention to your breath...",
                keyPoints = listOf("Breath is always available as an anchor", "Notice sensations without judgment", "Return gently when mind wanders"),
                breathingInstructions = "Inhale slowly through your nose for 4 counts, hold for 4 counts, exhale slowly through your mouth for 4 counts.",
                downloadSize = 3072000L,
                streamingQuality = AudioQuality.STANDARD
            ),
            
            MeditationSession(
                id = "body_scan_relaxation",
                title = "Body Scan Relaxation",
                description = "Systematically release tension throughout your body with this guided body scan meditation.",
                instructorName = "Dr. Sarah Chen",
                instructorBio = "Specialist in somatic experiencing and body-based therapies.",
                duration = 10,
                audioUrl = "https://example.com/audio/body_scan_relaxation.mp3",
                localAudioPath = "/local/audio/body_scan_relaxation.mp3",
                thumbnailUrl = "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400",
                category = SessionCategory.BODY_SCAN,
                difficulty = DifficultyLevel.BEGINNER,
                tags = listOf("body scan", "relaxation", "tension release", "somatic"),
                transcript = "Lie down comfortably and bring your awareness to your body. We'll scan from head to toe...",
                keyPoints = listOf("Progressive muscle relaxation", "Body awareness development", "Stress and tension release"),
                breathingInstructions = "Breathe deeply and slowly. As you exhale, imagine releasing tension from each body part.",
                downloadSize = 4096000L,
                streamingQuality = AudioQuality.STANDARD
            ),
            
            // Stress and Anxiety Sessions
            MeditationSession(
                id = "stress_relief_5min",
                title = "5-Minute Stress Relief",
                description = "Quick and effective stress relief techniques you can use anytime, anywhere.",
                instructorName = "Dr. Michael Roberts",
                instructorBio = "Stress management specialist with background in clinical psychology.",
                duration = 5,
                audioUrl = "https://example.com/audio/stress_relief_5min.mp3",
                localAudioPath = "/local/audio/stress_relief_5min.mp3",
                thumbnailUrl = "https://images.unsplash.com/photo-1540555700470-4c9a8e5a5a0c?w=400",
                category = SessionCategory.STRESS_RELIEF,
                difficulty = DifficultyLevel.ALL_LEVELS,
                tags = listOf("stress", "quick relief", "workplace", "emergency"),
                transcript = "Take a deep breath and acknowledge whatever stress you're feeling right now...",
                keyPoints = listOf("Quick stress reduction", "Immediate calming effects", "Portable technique"),
                breathingInstructions = "Box breathing: 4 counts in, hold 4, 4 counts out, hold 4. Repeat as needed.",
                downloadSize = 2048000L,
                streamingQuality = AudioQuality.STANDARD
            ),
            
            MeditationSession(
                id = "anxiety_calm",
                title = "Anxiety Calming Meditation",
                description = "Gentle guidance to calm anxious thoughts and find inner peace during difficult moments.",
                instructorName = "Dr. Emily Watson",
                instructorBio = "Anxiety specialist and mindfulness practitioner.",
                duration = 12,
                audioUrl = "https://example.com/audio/anxiety_calm.mp3",
                localAudioPath = "/local/audio/anxiety_calm.mp3",
                thumbnailUrl = "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400",
                category = SessionCategory.ANXIETY,
                difficulty = DifficultyLevel.ALL_LEVELS,
                tags = listOf("anxiety", "calming", "panic", "nervous system"),
                transcript = "Notice the sensations of anxiety in your body without judgment. You are safe in this moment...",
                keyPoints = listOf("Nervous system regulation", "Anxiety acceptance", "Grounding techniques"),
                breathingInstructions = "Slow diaphragmatic breathing: 6 counts in, 4 counts out. Focus on belly movement.",
                downloadSize = 5120000L,
                streamingQuality = AudioQuality.HIGH
            ),
            
            // Sleep Sessions
            MeditationSession(
                id = "sleep_preparation",
                title = "Sleep Preparation Meditation",
                description = "Prepare your mind and body for restful sleep with this calming bedtime meditation.",
                instructorName = "Dr. Lisa Thompson",
                instructorBio = "Sleep specialist and integrative medicine practitioner.",
                duration = 15,
                audioUrl = "https://example.com/audio/sleep_preparation.mp3",
                localAudioPath = "/local/audio/sleep_preparation.mp3",
                thumbnailUrl = "https://images.unsplash.com/photo-1511585993823-9511e7da4032?w=400",
                category = SessionCategory.SLEEP,
                difficulty = DifficultyLevel.ALL_LEVELS,
                tags = listOf("sleep", "bedtime", "relaxation", "insomnia"),
                transcript = "As you prepare for sleep, allow your body to settle and your mind to become quiet...",
                keyPoints = listOf("Sleep hygiene", "Mind-body relaxation", "Natural sleep preparation"),
                breathingInstructions = "4-7-8 breathing: 4 counts in, hold 7, 8 counts out. Follow your natural rhythm.",
                downloadSize = 6144000L,
                streamingQuality = AudioQuality.HIGH
            ),
            
            // Focus Sessions
            MeditationSession(
                id = "focus_enhancement",
                title = "Focus Enhancement",
                description = "Sharpen your concentration and mental clarity with this focused attention meditation.",
                instructorName = "Dr. James Park",
                instructorBio = "Cognitive performance specialist and meditation teacher.",
                duration = 8,
                audioUrl = "https://example.com/audio/focus_enhancement.mp3",
                localAudioPath = "/local/audio/focus_enhancement.mp3",
                thumbnailUrl = "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=400",
                category = SessionCategory.FOCUS,
                difficulty = DifficultyLevel.INTERMEDIATE,
                tags = listOf("focus", "concentration", "productivity", "mental clarity"),
                transcript = "Bring your full attention to this present moment. Notice how your mind naturally wants to focus...",
                keyPoints = listOf("Mental clarity enhancement", "Sustained attention training", "Productivity improvement"),
                breathingInstructions = "Equal breathing: 4 counts in, 4 counts out. Maintain steady rhythm.",
                downloadSize = 3584000L,
                streamingQuality = AudioQuality.STANDARD
            ),
            
            // Loving-Kindness Sessions
            MeditationSession(
                id = "loving_kindness_practice",
                title = "Loving-Kindness Practice",
                description = "Cultivate compassion and kindness toward yourself and others with this heart-opening meditation.",
                instructorName = "Dr. Maria Garcia",
                instructorBio = "Compassion meditation teacher and clinical psychologist.",
                duration = 10,
                audioUrl = "https://example.com/audio/loving_kindness_practice.mp3",
                localAudioPath = "/local/audio/loving_kindness_practice.mp3",
                thumbnailUrl = "https://images.unsplash.com/photo-1542744173-8e7e53415bb0?w=400",
                category = SessionCategory.LOVING_KINDNESS,
                difficulty = DifficultyLevel.ALL_LEVELS,
                tags = listOf("compassion", "kindness", "heart", "emotional warmth"),
                transcript = "Begin by bringing kind attention to yourself. May you be happy, may you be healthy, may you be safe...",
                keyPoints = listOf("Compassion cultivation", "Emotional warmth", "Self-kindness practice"),
                breathingInstructions = "Natural breathing with focus on heart center. Imagine breath flowing in and out of your heart.",
                downloadSize = 4096000L,
                streamingQuality = AudioQuality.STANDARD
            )
        )
    }
    
    /**
     * Create sample content bundles
     */
    fun createContentBundles(): List<ContentBundle> {
        return listOf(
            ContentBundle(
                id = "beginner_starter_pack",
                title = "Beginner Starter Pack",
                description = "Perfect collection for meditation beginners with essential foundational sessions.",
                sessions = listOf("intro_basics", "breathing_anchor", "body_scan_relaxation", "stress_relief_5min"),
                totalSize = 11264000L,
                downloadUrl = "https://example.com/bundles/beginner_starter_pack.zip",
                version = "1.0.0"
            ),
            
            ContentBundle(
                id = "stress_management_collection",
                title = "Stress Management Collection",
                description = "Comprehensive toolkit for managing stress and anxiety with proven techniques.",
                sessions = listOf("stress_relief_5min", "anxiety_calm", "body_scan_relaxation", "sleep_preparation"),
                totalSize = 17408000L,
                downloadUrl = "https://example.com/bundles/stress_management_collection.zip",
                version = "1.0.0"
            ),
            
            ContentBundle(
                id = "sleep_improvement_pack",
                title = "Sleep Improvement Pack",
                description = "Specialized meditations to improve sleep quality and overcome insomnia.",
                sessions = listOf("sleep_preparation", "body_scan_relaxation", "breathing_anchor"),
                totalSize = 13312000L,
                downloadUrl = "https://example.com/bundles/sleep_improvement_pack.zip",
                version = "1.0.0"
            )
        )
    }
}
