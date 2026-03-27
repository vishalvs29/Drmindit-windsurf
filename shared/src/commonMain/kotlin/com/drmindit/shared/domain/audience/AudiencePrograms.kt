package com.drmindit.shared.domain.audience

import kotlinx.serialization.Serializable

/**
 * Predefined programs for each audience type
 * These are structured, guided programs that replace generic AI chat
 */
object AudiencePrograms {
    
    // ==================== STUDENT PROGRAMS ====================
    
    val studentPrograms = listOf(
        ProgramTemplate(
            id = "student_exam_anxiety",
            name = "Exam Anxiety Relief Program",
            description = "7-day program to overcome exam anxiety and build confidence",
            duration = ProgramDuration.DAYS_7,
            difficulty = ProgramDifficulty.BEGINNER,
            targetAudience = AudienceType.STUDENT,
            category = ProgramCategory.ANXIETY,
            steps = listOf(
                ProgramStepTemplate(
                    id = "day1_understanding",
                    day = 1,
                    title = "Understanding Exam Anxiety",
                    description = "Learn what causes exam anxiety and how it affects your body and mind",
                    type = StepType.INSTRUCTION,
                    exercise = ExerciseTemplate(
                        type = ExerciseType.JOURNALING,
                        instructions = "Write down your thoughts about upcoming exams. What specific fears do you have?",
                        duration = 10,
                        difficulty = ExerciseDifficulty.EASY
                    ),
                    reflection = ReflectionTemplate(
                        prompt = "How did writing about your fears make you feel?",
                        type = ReflectionType.OPEN_ENDED,
                        responseFormat = ResponseFormat.TEXT
                    ),
                    audioSession = null
                ),
                ProgramStepTemplate(
                    id = "day2_breathing",
                    day = 2,
                    title = "Calm Breathing Techniques",
                    description = "Master breathing exercises to calm your mind before exams",
                    type = StepType.EXERCISE,
                    exercise = ExerciseTemplate(
                        type = ExerciseType.BREATHING,
                        instructions = "Practice the 4-7-8 breathing technique: Inhale for 4, hold for 7, exhale for 8",
                        duration = 15,
                        difficulty = ExerciseDifficulty.EASY
                    ),
                    reflection = ReflectionTemplate(
                        prompt = "Rate your anxiety level before and after breathing (1-10)",
                        type = ReflectionType.RATING_SCALE,
                        responseFormat = ResponseFormat.RATING
                    ),
                    audioSession = AudioSessionTemplate(
                        title = "Exam Breathing Practice",
                        duration = 15,
                        type = AudioType.BREATHING_EXERCISE,
                        voiceGender = VoiceGender.FEMALE,
                        backgroundMusic = true,
                        script = "Find a comfortable position and close your eyes..."
                    )
                ),
                ProgramStepTemplate(
                    id = "day3_cbt",
                    day = 3,
                    title = "Thought Reframing",
                    description = "Challenge negative thoughts about exams using CBT techniques",
                    type = StepType.EXERCISE,
                    exercise = ExerciseTemplate(
                        type = ExerciseType.COGNITIVE_RESTRUCTURING,
                        instructions = "Identify one negative thought about exams and challenge it with evidence",
                        duration = 20,
                        difficulty = ExerciseDifficulty.MODERATE
                    ),
                    reflection = ReflectionTemplate(
                        prompt = "What evidence supports your new, balanced thought?",
                        type = ReflectionType.OPEN_ENDED,
                        responseFormat = ResponseFormat.TEXT
                    ),
                    audioSession = null
                ),
                ProgramStepTemplate(
                    id = "day4_practice",
                    day = 4,
                    title = "Practice Under Pressure",
                    description = "Simulate exam conditions while staying calm",
                    type = StepType.PRACTICE,
                    exercise = ExerciseTemplate(
                        type = ExerciseType.BREATHING,
                        instructions = "Take a practice quiz while using your breathing techniques",
                        duration = 25,
                        difficulty = ExerciseDifficulty.MODERATE
                    ),
                    reflection = ReflectionTemplate(
                        prompt = "How did your breathing help during the practice quiz?",
                        type = ReflectionType.OPEN_ENDED,
                        responseFormat = ResponseFormat.TEXT
                    ),
                    audioSession = null
                ),
                ProgramStepTemplate(
                    id = "day5_confidence",
                    day = 5,
                    title = "Building Exam Confidence",
                    description = "Develop positive self-talk and confidence for exams",
                    type = StepType.EXERCISE,
                    exercise = ExerciseTemplate(
                        type = ExerciseType.AFFIRMATIONS,
                        instructions = "Practice positive affirmations about your exam abilities",
                        duration = 15,
                        difficulty = ExerciseDifficulty.EASY
                    ),
                    reflection = ReflectionTemplate(
                        prompt = "Write three things you're good at that will help you in exams",
                        type = ReflectionType.CHECKLIST,
                        responseFormat = ResponseFormat.CHECKLIST
                    ),
                    audioSession = AudioSessionTemplate(
                        title = "Exam Confidence Affirmations",
                        duration = 15,
                        type = AudioType.AFFIRMATIONS,
                        voiceGender = VoiceGender.FEMALE,
                        backgroundMusic = true,
                        script = "You are prepared. You are capable..."
                    )
                ),
                ProgramStepTemplate(
                    id = "day6_sleep",
                    day = 6,
                    title = "Better Sleep for Better Performance",
                    description = "Improve sleep quality before exam day",
                    type = StepType.AUDIO_SESSION,
                    exercise = null,
                    reflection = ReflectionTemplate(
                        prompt = "How many hours of sleep did you get last night?",
                        type = ReflectionType.RATING_SCALE,
                        responseFormat = ResponseFormat.RATING
                    ),
                    audioSession = AudioSessionTemplate(
                        title = "Exam Night Sleep Meditation",
                        duration = 20,
                        type = AudioType.SLEEP_STORY,
                        voiceGender = VoiceGender.FEMALE,
                        backgroundMusic = true,
                        script = "Tonight is for rest. Tomorrow is for showing what you know..."
                    )
                ),
                ProgramStepTemplate(
                    id = "day7_readiness",
                    day = 7,
                    title = "Exam Day Readiness",
                    description = "Final preparation and confidence boost for exam day",
                    type = StepType.ASSESSMENT,
                    exercise = ExerciseTemplate(
                        type = ExerciseType.BREATHING,
                        instructions = "Quick 5-minute breathing exercise for exam day",
                        duration = 5,
                        difficulty = ExerciseDifficulty.EASY
                    ),
                    reflection = ReflectionTemplate(
                        prompt = "What's one thing you've learned that will help you most?",
                        type = ReflectionType.OPEN_ENDED,
                        responseFormat = ResponseFormat.TEXT
                    ),
                    audioSession = AudioSessionTemplate(
                        title = "Exam Day Confidence Boost",
                        duration = 10,
                        type = AudioType.AFFIRMATIONS,
                        voiceGender = VoiceGender.FEMALE,
                        backgroundMusic = false,
                        script = "You are ready. You have prepared well..."
                    )
                )
            )
        ),
        
        ProgramTemplate(
            id = "student_focus_improvement",
            name = "Focus Improvement Program",
            description = "Enhance concentration and reduce distractions for better study",
            duration = ProgramDuration.DAYS_14,
            difficulty = ProgramDifficulty.INTERMEDIATE,
            targetAudience = AudienceType.STUDENT,
            category = ProgramCategory.FOCUS,
            steps = listOf(
                ProgramStepTemplate(
                    id = "focus_day1_assessment",
                    day = 1,
                    title = "Focus Assessment",
                    description = "Evaluate your current focus patterns and identify distractions",
                    type = StepType.ASSESSMENT,
                    exercise = ExerciseTemplate(
                        type = ExerciseType.JOURNALING,
                        instructions = "Track your focus for 30 minutes and note distractions",
                        duration = 30,
                        difficulty = ExerciseDifficulty.EASY
                    ),
                    reflection = ReflectionTemplate(
                        prompt = "What are your top 3 distractions?",
                        type = ReflectionType.CHECKLIST,
                        responseFormat = ResponseFormat.CHECKLIST
                    ),
                    audioSession = null
                ),
                ProgramStepTemplate(
                    id = "focus_day2_pomodoro",
                    day = 2,
                    title = "Pomodoro Technique",
                    description = "Learn the Pomodoro technique for sustained focus",
                    type = StepType.EXERCISE,
                    exercise = ExerciseTemplate(
                        type = ExerciseType.PROGRESSIVE_RELAXATION,
                        instructions = "Practice 25-minute focused study with 5-minute breaks",
                        duration = 30,
                        difficulty = ExerciseDifficulty.MODERATE
                    ),
                    reflection = ReflectionTemplate(
                        prompt = "How many Pomodoro sessions did you complete?",
                        type = ReflectionType.RATING_SCALE,
                        responseFormat = ResponseFormat.RATING
                    ),
                    audioSession = AudioSessionTemplate(
                        title = "Focus Timer Music",
                        duration = 25,
                        type = AudioType.GUIDED_MEDITATION,
                        voiceGender = VoiceGender.NEUTRAL,
                        backgroundMusic = true,
                        script = "Focus music for 25 minutes of concentrated work..."
                    )
                )
                // Additional days would continue with distraction awareness, study tracking, etc.
            )
        )
    )
    
    // ==================== CORPORATE PROGRAMS ====================
    
    val corporatePrograms = listOf(
        ProgramTemplate(
            id = "corporate_burnout_recovery",
            name = "Burnout Recovery Program",
            description = "14-day program to recover from workplace burnout and restore energy",
            duration = ProgramDuration.DAYS_14,
            difficulty = ProgramDifficulty.INTERMEDIATE,
            targetAudience = AudienceType.CORPORATE,
            category = ProgramCategory.BURNOUT,
            steps = listOf(
                ProgramStepTemplate(
                    id = "burnout_day1_identification",
                    day = 1,
                    title = "Identifying Burnout Triggers",
                    description = "Recognize the signs and causes of workplace burnout",
                    type = StepType.INSTRUCTION,
                    exercise = ExerciseTemplate(
                        type = ExerciseType.JOURNALING,
                        instructions = "List your work stressors and how they affect you",
                        duration = 15,
                        difficulty = ExerciseDifficulty.EASY
                    ),
                    reflection = ReflectionTemplate(
                        prompt = "Which stressor affects you most?",
                        type = ReflectionType.MULTIPLE_CHOICE,
                        responseFormat = ResponseFormat.SELECTION
                    ),
                    audioSession = null
                ),
                ProgramStepTemplate(
                    id = "burnout_day2_boundaries",
                    day = 2,
                    title = "Setting Work Boundaries",
                    description = "Establish healthy boundaries between work and personal life",
                    type = StepType.EXERCISE,
                    exercise = ExerciseTemplate(
                        type = ExerciseType.COGNITIVE_RESTRUCTURING,
                        instructions = "Create 3 specific work boundaries you'll implement",
                        duration = 20,
                        difficulty = ExerciseDifficulty.MODERATE
                    ),
                    reflection = ReflectionTemplate(
                        prompt = "How will you enforce these boundaries?",
                        type = ReflectionType.OPEN_ENDED,
                        responseFormat = ResponseFormat.TEXT
                    ),
                    audioSession = AudioSessionTemplate(
                        title = "Work-Life Boundary Meditation",
                        duration = 15,
                        type = AudioType.GUIDED_MEDITATION,
                        voiceGender = VoiceGender.MALE,
                        backgroundMusic = true,
                        script = "Your work is important, but your wellbeing comes first..."
                    )
                ),
                ProgramStepTemplate(
                    id = "burnout_day3_energy",
                    day = 3,
                    title = "Energy Management",
                    description = "Learn to manage your energy throughout the workday",
                    type = StepType.EXERCISE,
                    exercise = ExerciseTemplate(
                        type = ExerciseType.BODY_SCAN,
                        instructions = "Practice body scan to identify energy levels",
                        duration = 15,
                        difficulty = ExerciseDifficulty.EASY
                    ),
                    reflection = ReflectionTemplate(
                        prompt = "When during the day do you feel most/least energetic?",
                        type = ReflectionType.RATING_SCALE,
                        responseFormat = ResponseFormat.RATING
                    ),
                    audioSession = null
                )
                // Additional days would continue with recovery routines, stress management, etc.
            )
        ),
        
        ProgramTemplate(
            id = "corporate_stress_management",
            name = "Daily Stress Management",
            description = "5-minute daily sessions for workplace stress relief",
            duration = ProgramDuration.ONGOING,
            difficulty = ProgramDifficulty.BEGINNER,
            targetAudience = AudienceType.CORPORATE,
            category = ProgramCategory.STRESS,
            steps = listOf(
                ProgramStepTemplate(
                    id = "stress_daily_reset",
                    day = 1,
                    title = "5-Minute Stress Reset",
                    description = "Quick breathing and mindfulness for immediate stress relief",
                    type = StepType.AUDIO_SESSION,
                    exercise = ExerciseTemplate(
                        type = ExerciseType.BREATHING,
                        instructions = "Box breathing: 4-4-4-4 pattern for stress relief",
                        duration = 5,
                        difficulty = ExerciseDifficulty.EASY
                    ),
                    reflection = ReflectionTemplate(
                        prompt = "Rate your stress level before and after (1-10)",
                        type = ReflectionType.RATING_SCALE,
                        responseFormat = ResponseFormat.RATING,
                        isOptional = true
                    ),
                    audioSession = AudioSessionTemplate(
                        title = "Workplace Stress Reset",
                        duration = 5,
                        type = AudioType.BREATHING_EXERCISE,
                        voiceGender = VoiceGender.MALE,
                        backgroundMusic = false,
                        script = "Take a moment to reset. Inhale peace, exhale stress..."
                    )
                )
            )
        )
    )
    
    // ==================== POLICE/MILITARY PROGRAMS ====================
    
    val policeMilitaryPrograms = listOf(
        ProgramTemplate(
            id = "police_trauma_safe_support",
            name = "Trauma-Safe Support Program",
            description = "Gentle, grounding exercises for emotional stabilization",
            duration = ProgramDuration.DAYS_21,
            difficulty = ProgramDifficulty.BEGINNER,
            targetAudience = AudienceType.POLICE_MILITARY,
            category = ProgramCategory.TRAUMA,
            steps = listOf(
                ProgramStepTemplate(
                    id = "trauma_day1_grounding",
                    day = 1,
                    title = "Grounding Techniques",
                    description = "Learn simple grounding exercises to stay present and safe",
                    type = StepType.EXERCISE,
                    exercise = ExerciseTemplate(
                        type = ExerciseType.GROUNDING,
                        instructions = "5-4-3-2-1 grounding: Name 5 things you see, 4 you touch, 3 you hear, 2 you smell, 1 you taste",
                        duration = 10,
                        difficulty = ExerciseDifficulty.EASY
                    ),
                    reflection = ReflectionTemplate(
                        prompt = "How did grounding affect your sense of presence?",
                        type = ReflectionType.OPEN_ENDED,
                        responseFormat = ResponseFormat.TEXT,
                        isOptional = true
                    ),
                    audioSession = AudioSessionTemplate(
                        title = "Safe Grounding Practice",
                        duration = 10,
                        type = AudioType.GROUNDING_EXERCISE,
                        voiceGender = VoiceGender.MALE,
                        backgroundMusic = false,
                        script = "You are safe. You are present. Notice your surroundings..."
                    )
                ),
                ProgramStepTemplate(
                    id = "trauma_day2_stabilization",
                    day = 2,
                    title = "Emotional Stabilization",
                    description = "Gentle techniques to regulate emotions without overwhelming",
                    type = StepType.EXERCISE,
                    exercise = ExerciseTemplate(
                        type = ExerciseType.BREATHING,
                        instructions = "Box breathing for emotional regulation",
                        duration = 8,
                        difficulty = ExerciseDifficulty.EASY
                    ),
                    reflection = ReflectionTemplate(
                        prompt = "Rate your emotional stability before and after (1-10)",
                        type = ReflectionType.RATING_SCALE,
                        responseFormat = ResponseFormat.RATING,
                        isOptional = true
                    ),
                    audioSession = null
                )
                // Additional days would continue with trauma-sensitive approaches
            )
        ),
        
        ProgramTemplate(
            id = "police_sleep_recovery",
            name = "Sleep Recovery Program",
            description = "Improve sleep quality with relaxation techniques",
            duration = ProgramDuration.DAYS_14,
            difficulty = ProgramDifficulty.BEGINNER,
            targetAudience = AudienceType.POLICE_MILITARY,
            category = ProgramCategory.SLEEP,
            steps = listOf(
                ProgramStepTemplate(
                    id = "sleep_day1_routine",
                    day = 1,
                    title = "Night Routine Setup",
                    description = "Create a calming bedtime routine for better sleep",
                    type = StepType.INSTRUCTION,
                    exercise = ExerciseTemplate(
                        type = ExerciseType.JOURNALING,
                        instructions = "Plan your ideal 30-minute bedtime routine",
                        duration = 15,
                        difficulty = ExerciseDifficulty.EASY
                    ),
                    reflection = ReflectionTemplate(
                        prompt = "What's one change you'll make to your routine?",
                        type = ReflectionType.OPEN_ENDED,
                        responseFormat = ResponseFormat.TEXT
                    ),
                    audioSession = AudioSessionTemplate(
                        title = "Deep Sleep Preparation",
                        duration = 20,
                        type = AudioType.SLEEP_STORY,
                        voiceGender = VoiceGender.MALE,
                        backgroundMusic = true,
                        script = "Your body deserves rest. Your mind deserves peace..."
                    )
                )
            )
        ),
        
        ProgramTemplate(
            id = "police_resilience_training",
            name = "Resilience Training Program",
            description = "Build mental strength and stress conditioning",
            duration = ProgramDuration.DAYS_21,
            difficulty = ProgramDifficulty.ADVANCED,
            targetAudience = AudienceType.POLICE_MILITARY,
            category = ProgramCategory.RESILIENCE,
            steps = listOf(
                ProgramStepTemplate(
                    id = "resilience_day1_mental_strength",
                    day = 1,
                    title = "Mental Strength Foundation",
                    description = "Build foundational mental strength exercises",
                    type = StepType.EXERCISE,
                    exercise = ExerciseTemplate(
                        type = ExerciseType.VISUALIZATION,
                        instructions = "Visualize yourself handling stressful situations with calm strength",
                        duration = 15,
                        difficulty = ExerciseDifficulty.MODERATE
                    ),
                    reflection = ReflectionTemplate(
                        prompt = "How confident do you feel in your ability to handle stress?",
                        type = ReflectionType.RATING_SCALE,
                        responseFormat = ResponseFormat.RATING
                    ),
                    audioSession = AudioSessionTemplate(
                        title = "Resilience Building Visualization",
                        duration = 15,
                        type = AudioType.GUIDED_MEDITATION,
                        voiceGender = VoiceGender.MALE,
                        backgroundMusic = false,
                        script = "You are stronger than any challenge you face..."
                    )
                )
            )
        )
    )
    
    /**
     * Get all programs for a specific audience
     */
    fun getProgramsForAudience(audience: AudienceType): List<ProgramTemplate> {
        return when (audience) {
            AudienceType.STUDENT -> studentPrograms
            AudienceType.CORPORATE -> corporatePrograms
            AudienceType.POLICE_MILITARY -> policeMilitaryPrograms
        }
    }
    
    /**
     * Get program by ID for any audience
     */
    fun getProgramById(programId: String): ProgramTemplate? {
        return (studentPrograms + corporatePrograms + policeMilitaryPrograms)
            .find { it.id == programId }
    }
}
