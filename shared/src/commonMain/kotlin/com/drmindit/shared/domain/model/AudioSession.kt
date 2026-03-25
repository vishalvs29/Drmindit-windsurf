package com.drmindit.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AudioSession(
    val id: String,
    val title: String,
    val description: String,
    val duration: Int, // in seconds
    val category: SessionCategory,
    val instructorName: String,
    val audioUrl: String, // REQUIRED - actual streaming URL
    val thumbnailUrl: String,
    val tags: List<String> = emptyList(),
    val rating: Float = 0.0f,
    val totalRatings: Int = 0,
    val isPremium: Boolean = false,
    val difficulty: Difficulty = Difficulty.BEGINNER,
    val language: String = "en",
    val transcript: String? = null,
    val backgroundColor: String? = null,
    val isDownloaded: Boolean = false,
    val localFilePath: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val isActive: Boolean = true
)

@Serializable
data class SessionCategory(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val color: String
)

enum class Difficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

// Predefined session categories
object SessionCategories {
    val SLEEP = SessionCategory(
        id = "sleep",
        name = "Sleep",
        description = "Deep sleep and relaxation sessions",
        icon = "moon",
        color = "#6B46C1"
    )
    
    val ANXIETY = SessionCategory(
        id = "anxiety",
        name = "Anxiety",
        description = "Anxiety relief and calming techniques",
        icon = "heart",
        color = "#DC2626"
    )
    
    val STRESS = SessionCategory(
        id = "stress",
        name = "Stress",
        description = "Stress management and relaxation",
        icon = "shield",
        color = "#059669"
    )
    
    val FOCUS = SessionCategory(
        id = "focus",
        name = "Focus",
        description = "Concentration and productivity",
        icon = "brain",
        color = "#2563EB"
    )
    
    val BREATHING = SessionCategory(
        id = "breathing",
        name = "Breathing",
        description = "Breathing exercises and techniques",
        icon = "wind",
        color = "#7C3AED"
    )
    
    val MINDFULNESS = SessionCategory(
        id = "mindfulness",
        name = "Mindfulness",
        description = "Mindfulness and meditation practices",
        icon = "sparkles",
        color = "#0891B2"
    )
    
    val YOGA = SessionCategory(
        id = "yoga",
        name = "Yoga",
        description = "Gentle yoga and movement",
        icon = "activity",
        color = "#EA580C"
    )
    
    val DEPRESSION = SessionCategory(
        id = "depression",
        name = "Depression",
        description = "Support for mood and depression",
        icon = "cloud",
        color = "#6B7280"
    )
    
    fun getAllCategories(): List<SessionCategory> {
        return listOf(SLEEP, ANXIETY, STRESS, FOCUS, BREATHING, MINDFULNESS, YOGA, DEPRESSION)
    }
    
    fun getById(id: String): SessionCategory? {
        return getAllCategories().find { it.id == id }
    }
}

// Real audio content dataset
object RealAudioContent {
    
    // High-quality, royalty-free meditation audio URLs
    private val sleepAudioUrls = listOf(
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3"
    )
    
    private val anxietyAudioUrls = listOf(
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3"
    )
    
    private val stressAudioUrls = listOf(
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-11.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-13.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-14.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-15.mp3"
    )
    
    private val focusAudioUrls = listOf(
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-16.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-17.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-18.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-19.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-20.mp3"
    )
    
    private val breathingAudioUrls = listOf(
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-21.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-22.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-23.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-24.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-25.mp3"
    )
    
    private val mindfulnessAudioUrls = listOf(
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-26.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-27.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-28.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-29.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-30.mp3"
    )
    
    private val yogaAudioUrls = listOf(
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-31.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-32.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-33.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-34.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-35.mp3"
    )
    
    private val depressionAudioUrls = listOf(
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-36.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-37.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-38.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-39.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-40.mp3"
    )
    
    fun getSampleSessions(): List<AudioSession> {
        return listOf(
            // Sleep Sessions
            AudioSession(
                id = "sleep_001",
                title = "Deep Sleep Journey",
                description = "Drift into restorative sleep with this soothing bedtime meditation. Let go of the day's worries and embrace peaceful slumber.",
                duration = 1800, // 30 minutes
                category = SessionCategories.SLEEP,
                instructorName = "Dr. Sarah Chen",
                audioUrl = sleepAudioUrls[0],
                thumbnailUrl = "https://picsum.photos/seed/sleep1/400/300.jpg",
                tags = listOf("sleep", "bedtime", "relaxation", "deep"),
                rating = 4.8f,
                totalRatings = 234,
                difficulty = Difficulty.BEGINNER
            ),
            
            AudioSession(
                id = "sleep_002",
                title = "Ocean Waves Sleep",
                description = "Gentle ocean waves combined with calming guidance to help you fall asleep naturally and deeply.",
                duration = 2400, // 40 minutes
                category = SessionCategories.SLEEP,
                instructorName = "Prof. James Miller",
                audioUrl = sleepAudioUrls[1],
                thumbnailUrl = "https://picsum.photos/seed/ocean/400/300.jpg",
                tags = listOf("ocean", "waves", "nature", "sleep"),
                rating = 4.7f,
                totalRatings = 189,
                difficulty = Difficulty.BEGINNER
            ),
            
            AudioSession(
                id = "sleep_003",
                title = "Progressive Muscle Relaxation for Sleep",
                description = "Systematically release tension throughout your body for deep, restorative sleep.",
                duration = 1500, // 25 minutes
                category = SessionCategories.SLEEP,
                instructorName = "Dr. Emily Brown",
                audioUrl = sleepAudioUrls[2],
                thumbnailUrl = "https://picsum.photos/seed/muscle/400/300.jpg",
                tags = listOf("muscle", "relaxation", "body", "sleep"),
                rating = 4.6f,
                totalRatings = 156,
                difficulty = Difficulty.BEGINNER
            ),
            
            // Anxiety Sessions
            AudioSession(
                id = "anxiety_001",
                title = "Anxiety Relief Meditation",
                description = "A guided meditation specifically designed to help you manage and reduce anxiety symptoms with proven techniques.",
                duration = 1200, // 20 minutes
                category = SessionCategories.ANXIETY,
                instructorName = "Dr. Michael Roberts",
                audioUrl = anxietyAudioUrls[0],
                thumbnailUrl = "https://picsum.photos/seed/anxiety1/400/300.jpg",
                tags = listOf("anxiety", "relief", "calm", "breathing"),
                rating = 4.8f,
                totalRatings = 327,
                difficulty = Difficulty.INTERMEDIATE
            ),
            
            AudioSession(
                id = "anxiety_002",
                title = "Panic Attack Support",
                description = "Immediate support for panic attacks with grounding techniques and calming guidance to help you regain control.",
                duration = 720, // 12 minutes
                category = SessionCategories.ANXIETY,
                instructorName = "Dr. Lisa Anderson",
                audioUrl = anxietyAudioUrls[1],
                thumbnailUrl = "https://picsum.photos/seed/panic/400/300.jpg",
                tags = listOf("panic", "grounding", "emergency", "support"),
                rating = 4.9f,
                totalRatings = 156,
                difficulty = Difficulty.BEGINNER
            ),
            
            AudioSession(
                id = "anxiety_003",
                title = "Worry Time Management",
                description = "Learn to contain your worries to specific times and reclaim your mental space throughout the day.",
                duration = 1080, // 18 minutes
                category = SessionCategories.ANXIETY,
                instructorName = "Prof. David Wilson",
                audioUrl = anxietyAudioUrls[2],
                thumbnailUrl = "https://picsum.photos/seed/worry/400/300.jpg",
                tags = listOf("worry", "time-management", "cognitive", "control"),
                rating = 4.5f,
                totalRatings = 98,
                difficulty = Difficulty.INTERMEDIATE,
                isPremium = true
            ),
            
            // Stress Sessions
            AudioSession(
                id = "stress_001",
                title = "Stress Reduction Body Scan",
                description = "Release physical tension and mental stress through this comprehensive body scan meditation.",
                duration = 1500, // 25 minutes
                category = SessionCategories.STRESS,
                instructorName = "Dr. Jennifer Taylor",
                audioUrl = stressAudioUrls[0],
                thumbnailUrl = "https://picsum.photos/seed/bodyscan/400/300.jpg",
                tags = listOf("body-scan", "relaxation", "tension", "stress"),
                rating = 4.7f,
                totalRatings = 289,
                difficulty = Difficulty.BEGINNER
            ),
            
            AudioSession(
                id = "stress_002",
                title = "Quick Stress Reset",
                description = "A 5-minute meditation to quickly reset your stress levels during busy days at work or home.",
                duration = 300, // 5 minutes
                category = SessionCategories.STRESS,
                instructorName = "Dr. Robert Martinez",
                audioUrl = stressAudioUrls[1],
                thumbnailUrl = "https://picsum.photos/seed/quick/400/300.jpg",
                tags = listOf("quick", "stress", "break", "reset"),
                rating = 4.4f,
                totalRatings = 445,
                difficulty = Difficulty.BEGINNER
            ),
            
            AudioSession(
                id = "stress_003",
                title = "Workplace Stress Relief",
                description = "Specific techniques for managing workplace stress and maintaining professional composure.",
                duration = 900, // 15 minutes
                category = SessionCategories.STRESS,
                instructorName = "Dr. Maria Garcia",
                audioUrl = stressAudioUrls[2],
                thumbnailUrl = "https://picsum.photos/seed/work/400/300.jpg",
                tags = listOf("work", "professional", "stress", "composure"),
                rating = 4.6f,
                totalRatings = 178,
                difficulty = Difficulty.INTERMEDIATE
            ),
            
            // Focus Sessions
            AudioSession(
                id = "focus_001",
                title = "Deep Focus Meditation",
                description = "Enhance your concentration and mental clarity for work, study, or creative projects.",
                duration = 900, // 15 minutes
                category = SessionCategories.FOCUS,
                instructorName = "Prof. Kevin Thompson",
                audioUrl = focusAudioUrls[0],
                thumbnailUrl = "https://picsum.photos/seed/focus1/400/300.jpg",
                tags = listOf("focus", "concentration", "work", "study"),
                rating = 4.8f,
                totalRatings = 367,
                difficulty = Difficulty.INTERMEDIATE,
                isPremium = true
            ),
            
            AudioSession(
                id = "focus_002",
                title = "Study Session Support",
                description = "Optimize your learning and retention with this study-focused meditation for students.",
                duration = 720, // 12 minutes
                category = SessionCategories.FOCUS,
                instructorName = "Dr. Amanda White",
                audioUrl = focusAudioUrls[1],
                thumbnailUrl = "https://picsum.photos/seed/study/400/300.jpg",
                tags = listOf("study", "learning", "memory", "students"),
                rating = 4.5f,
                totalRatings = 234,
                difficulty = Difficulty.BEGINNER
            ),
            
            AudioSession(
                id = "focus_003",
                title = "Creative Flow State",
                description = "Enter a state of creative flow and unlock your artistic or problem-solving potential.",
                duration = 1200, // 20 minutes
                category = SessionCategories.FOCUS,
                instructorName = "Dr. Christopher Lee",
                audioUrl = focusAudioUrls[2],
                thumbnailUrl = "https://picsum.photos/seed/creative/400/300.jpg",
                tags = listOf("creative", "flow", "artistic", "innovation"),
                rating = 4.7f,
                totalRatings = 145,
                difficulty = Difficulty.ADVANCED,
                isPremium = true
            ),
            
            // Breathing Sessions
            AudioSession(
                id = "breathing_001",
                title = "4-7-8 Breathing Technique",
                description = "Master the powerful 4-7-8 breathing technique for instant relaxation and anxiety relief.",
                duration = 480, // 8 minutes
                category = SessionCategories.BREATHING,
                instructorName = "Dr. Nancy Davis",
                audioUrl = breathingAudioUrls[0],
                thumbnailUrl = "https://picsum.photos/seed/breathing1/400/300.jpg",
                tags = listOf("4-7-8", "breathing", "relaxation", "technique"),
                rating = 4.9f,
                totalRatings = 523,
                difficulty = Difficulty.BEGINNER
            ),
            
            AudioSession(
                id = "breathing_002",
                title = "Box Breathing for Focus",
                description = "Use box breathing to improve focus and reduce stress in high-pressure situations.",
                duration = 600, // 10 minutes
                category = SessionCategories.BREATHING,
                instructorName = "Prof. Thomas Kumar",
                audioUrl = breathingAudioUrls[1],
                thumbnailUrl = "https://picsum.photos/seed/box/400/300.jpg",
                tags = listOf("box", "breathing", "focus", "pressure"),
                rating = 4.6f,
                totalRatings = 189,
                difficulty = Difficulty.BEGINNER
            ),
            
            AudioSession(
                id = "breathing_003",
                title = "Diaphragmatic Breathing",
                description = "Learn proper diaphragmatic breathing for deeper relaxation and better oxygen flow.",
                duration = 720, // 12 minutes
                category = SessionCategories.BREATHING,
                instructorName = "Dr. Patricia Brown",
                audioUrl = breathingAudioUrls[2],
                thumbnailUrl = "https://picsum.photos/seed/diaphragm/400/300.jpg",
                tags = listOf("diaphragmatic", "deep", "oxygen", "relaxation"),
                rating = 4.5f,
                totalRatings = 167,
                difficulty = Difficulty.BEGINNER
            ),
            
            // Mindfulness Sessions
            AudioSession(
                id = "mindfulness_001",
                title = "Morning Mindfulness",
                description = "Start your day with clarity and peace through this gentle morning meditation practice.",
                duration = 600, // 10 minutes
                category = SessionCategories.MINDFULNESS,
                instructorName = "Dr. Susan Miller",
                audioUrl = mindfulnessAudioUrls[0],
                thumbnailUrl = "https://picsum.photos/seed/morning/400/300.jpg",
                tags = listOf("morning", "clarity", "peace", "routine"),
                rating = 4.8f,
                totalRatings = 234,
                difficulty = Difficulty.BEGINNER
            ),
            
            AudioSession(
                id = "mindfulness_002",
                title = "Mindful Walking",
                description = "Transform your daily walks into mindful meditation practices with this guided session.",
                duration = 900, // 15 minutes
                category = SessionCategories.MINDFULNESS,
                instructorName = "Dr. Barbara Wilson",
                audioUrl = mindfulnessAudioUrls[1],
                thumbnailUrl = "https://picsum.photos/seed/walking/400/300.jpg",
                tags = listOf("walking", "movement", "outdoors", "mindful"),
                rating = 4.4f,
                totalRatings = 167,
                difficulty = Difficulty.BEGINNER
            ),
            
            AudioSession(
                id = "mindfulness_003",
                title = "Eating Mindfully",
                description = "Develop a healthier relationship with food through mindful eating practices.",
                duration = 1200, // 20 minutes
                category = SessionCategories.MINDFULNESS,
                instructorName = "Dr. Laura Smith",
                audioUrl = mindfulnessAudioUrls[2],
                thumbnailUrl = "https://picsum.photos/seed/eating/400/300.jpg",
                tags = listOf("eating", "food", "mindful", "health"),
                rating = 4.6f,
                totalRatings = 98,
                difficulty = Difficulty.INTERMEDIATE,
                isPremium = true
            ),
            
            // Yoga Sessions
            AudioSession(
                id = "yoga_001",
                title = "Gentle Yoga Flow",
                description = "Combine mindfulness with gentle movement in this beginner-friendly yoga session.",
                duration = 1200, // 20 minutes
                category = SessionCategories.YOGA,
                instructorName = "Rachel Johnson",
                audioUrl = yogaAudioUrls[0],
                thumbnailUrl = "https://picsum.photos/seed/yoga1/400/300.jpg",
                tags = listOf("yoga", "movement", "gentle", "beginner"),
                rating = 4.6f,
                totalRatings = 298,
                difficulty = Difficulty.BEGINNER
            ),
            
            AudioSession(
                id = "yoga_002",
                title = "Office Yoga Break",
                description = "Quick yoga stretches you can do at your desk to relieve tension and boost energy.",
                duration = 600, // 10 minutes
                category = SessionCategories.YOGA,
                instructorName = "Dr. Michelle Anderson",
                audioUrl = yogaAudioUrls[1],
                thumbnailUrl = "https://picsum.photos/seed/office/400/300.jpg",
                tags = listOf("office", "desk", "stretch", "energy"),
                rating = 4.5f,
                totalRatings = 445,
                difficulty = Difficulty.BEGINNER
            ),
            
            // Depression Support Sessions
            AudioSession(
                id = "depression_001",
                title = "Hope and Healing",
                description = "A gentle meditation for finding hope and managing depressive symptoms with compassion.",
                duration = 1080, // 18 minutes
                category = SessionCategories.DEPRESSION,
                instructorName = "Dr. Samantha Taylor",
                audioUrl = depressionAudioUrls[0],
                thumbnailUrl = "https://picsum.photos/seed/hope/400/300.jpg",
                tags = listOf("hope", "healing", "depression", "compassion"),
                rating = 4.8f,
                totalRatings = 234,
                difficulty = Difficulty.INTERMEDIATE
            ),
            
            AudioSession(
                id = "depression_002",
                title = "Self-Compassion Practice",
                description = "Cultivate kindness and compassion toward yourself during difficult times.",
                duration = 960, // 16 minutes
                category = SessionCategories.DEPRESSION,
                instructorName = "Dr. Barbara Wilson",
                audioUrl = depressionAudioUrls[1],
                thumbnailUrl = "https://picsum.photos/seed/compassion/400/300.jpg",
                tags = listOf("compassion", "self-care", "kindness", "support"),
                rating = 4.9f,
                totalRatings = 312,
                difficulty = Difficulty.BEGINNER
            ),
            
            AudioSession(
                id = "depression_003",
                title = "Morning Energy Boost",
                description = "Start your day with gentle motivation and positive energy for managing depression.",
                duration = 720, // 12 minutes
                category = SessionCategories.DEPRESSION,
                instructorName = "Dr. Jennifer Martinez",
                audioUrl = depressionAudioUrls[2],
                thumbnailUrl = "https://picsum.photos/seed/energy/400/300.jpg",
                tags = listOf("morning", "energy", "motivation", "positive"),
                rating = 4.7f,
                totalRatings = 189,
                difficulty = Difficulty.BEGINNER
            )
        )
    }
    
    fun getSessionsByCategory(categoryId: String): List<AudioSession> {
        return getSampleSessions().filter { it.category.id == categoryId }
    }
    
    fun getSessionById(sessionId: String): AudioSession? {
        return getSampleSessions().find { it.id == sessionId }
    }
    
    fun getFeaturedSessions(): List<AudioSession> {
        return getSampleSessions().filter { it.rating >= 4.7f }.shuffled().take(5)
    }
    
    fun getSessionOfDay(): AudioSession {
        val sessions = getSampleSessions()
        val today = java.time.LocalDate.now().dayOfYear
        return sessions[today % sessions.size]
    }
    
    fun getRecentlyPlayed(): List<AudioSession> {
        // In real app, this would come from user preferences/local storage
        return getSampleSessions().shuffled().take(3)
    }
    
    fun getContinueListening(): List<AudioSession> {
        // In real app, this would come from user progress
        return getSampleSessions().shuffled().take(4)
    }
}
