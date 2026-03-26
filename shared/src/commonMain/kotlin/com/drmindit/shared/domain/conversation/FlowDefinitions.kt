package com.drmindit.shared.domain.conversation

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Flow Definitions
 * Defines structured conversation flows for different mental health contexts
 */
@Singleton
class FlowDefinitions @Inject constructor() {
    
    /**
     * Get anxiety management flow
     */
    fun getAnxietyFlow(): ConversationFlow {
        return ConversationFlow(
            id = "anxiety_flow",
            name = "Anxiety Management",
            state = ConversationState.ANXIETY,
            description = "Structured flow for anxiety management and coping",
            steps = listOf(
                FlowStep(
                    id = "anxiety_validation",
                    step = 0,
                    title = "Understanding Your Anxiety",
                    prompt = "I can hear that you're feeling anxious. That's completely understandable. Anxiety is your body's natural response to stress. Before we work on managing it, can you tell me what specific thoughts or feelings are coming up for you right now?",
                    expectedInputType = InputType.TEXT,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Please share what you're experiencing so I can help you better.")
                    ),
                    aiResponseEnabled = true,
                    exercises = listOf(
                        Exercise(
                            id = "box_breathing",
                            name = "Box Breathing",
                            description = "A simple breathing technique to calm anxiety",
                            type = ExerciseType.BREATHING,
                            duration = 300,
                            instructions = listOf(
                                "Find a comfortable position and close your eyes",
                                "Inhale slowly for 4 counts",
                                "Hold your breath for 4 counts",
                                "Exhale slowly for 4 counts",
                                "Hold for 4 counts and repeat"
                            ),
                            benefits = listOf("Reduces physical anxiety symptoms", "Calms nervous system", "Improves focus"),
                            difficulty = Difficulty.EASY
                        )
                    ),
                    nextSteps = listOf("anxiety_trigger_identification", "breathing_exercise")
                ),
                
                FlowStep(
                    id = "anxiety_trigger_identification",
                    step = 1,
                    title = "Identifying Anxiety Triggers",
                    prompt = "Thank you for sharing that with me. Understanding what triggers your anxiety is the first step to managing it. Can you identify what might have triggered these feelings? Was it a specific situation, thought, or physical sensation?",
                    expectedInputType = InputType.TEXT,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Help me understand what triggered your anxiety so we can address it together.")
                    ),
                    aiResponseEnabled = true,
                    predefinedResponses = listOf(
                        "That makes sense. Triggers can be situations, thoughts, or even physical sensations. Let's work on understanding this trigger better.",
                        "I understand how that could trigger anxiety. Let's explore some strategies to handle this specific trigger."
                    ),
                    nextSteps = listOf("anxiety_cognitive_challenge", "grounding_technique")
                ),
                
                FlowStep(
                    id = "anxiety_cognitive_challenge",
                    step = 2,
                    title = "Challenging Anxious Thoughts",
                    prompt = "Anxiety often comes with worried thoughts that feel very real and threatening. Let's gently challenge these thoughts. What worried thoughts are coming up for you right now?",
                    expectedInputType = InputType.TEXT,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Sharing your worried thoughts will help us work through them together.")
                    ),
                    aiResponseEnabled = true,
                    exercises = listOf(
                        Exercise(
                            id = "thought_challenge",
                            name = "Thought Challenging",
                            description = "Cognitive technique to challenge anxious thoughts",
                            type = ExerciseType.COGNITIVE,
                            duration = 600,
                            instructions = listOf(
                                "Write down the anxious thought",
                                "Ask: Is this thought 100% true?",
                                "Ask: What's a more balanced perspective?",
                                "Ask: What would I tell a friend with this thought?"
                            ),
                            benefits = listOf("Reduces thought intensity", "Improves perspective", "Builds cognitive flexibility"),
                            difficulty = Difficulty.MODERATE
                        )
                    ),
                    nextSteps = listOf("anxiety_coping_strategies", "relaxation_response")
                ),
                
                FlowStep(
                    id = "anxiety_coping_strategies",
                    step = 3,
                    title = "Developing Coping Strategies",
                    prompt = "You're doing great work understanding and challenging your anxiety. Now let's identify some practical strategies you can use when anxiety arises. What coping strategies have worked for you in the past?",
                    expectedInputType = InputType.MULTIPLE_CHOICE,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Please select at least one strategy that has helped you.")
                    ),
                    aiResponseEnabled = true,
                    predefinedResponses = listOf(
                        "Those are excellent strategies. Let's build on what's worked for you and add some new tools to your toolkit.",
                        "It's okay if you're not sure what's worked before. We'll discover effective strategies together."
                    ),
                    resources = listOf(
                        Resource(
                            id = "anxiety_toolkit",
                            name = "Anxiety Management Toolkit",
                            description = "Comprehensive guide with anxiety management techniques",
                            type = ResourceType.TOOL,
                            category = ResourceCategory.ANXIETY,
                            tags = listOf("anxiety", "coping", "tools", "techniques"),
                            estimatedTime = 15
                        )
                    ),
                    nextSteps = listOf("anxiety_relaxation", "progress_review")
                ),
                
                FlowStep(
                    id = "anxiety_relaxation",
                    step = 4,
                    title = "Relaxation and Grounding",
                    prompt = "Let's practice a relaxation technique right now to help your body and mind calm down. Would you like to try a breathing exercise, a grounding technique, or a body scan?",
                    expectedInputType = InputType.MULTIPLE_CHOICE,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Please choose a relaxation technique to practice.")
                    ),
                    aiResponseEnabled = false,
                    exercises = listOf(
                        Exercise(
                            id = "54321_grounding",
                            name = "5-4-3-2-1 Grounding",
                            description = "Grounding technique using your five senses",
                            type = ExerciseType.GROUNDING,
                            duration = 300,
                            instructions = listOf(
                                "Name 5 things you can see around you",
                                "Name 4 things you can touch",
                                "Name 3 things you can hear",
                                "Name 2 things you can smell",
                                "Name 1 thing you can taste"
                            ),
                            benefits = listOf("Reduces dissociation", "Brings you to present moment", "Calms nervous system"),
                            difficulty = Difficulty.EASY
                        )
                    ),
                    nextSteps = listOf("anxiety_progress_review")
                ),
                
                FlowStep(
                    id = "anxiety_progress_review",
                    step = 5,
                    title = "Progress Review and Next Steps",
                    prompt = "You've done excellent work managing your anxiety today. Let's review what we've covered and create a plan for moving forward. On a scale of 1-10, how much less anxious are you feeling now compared to when we started?",
                    expectedInputType = InputType.SCALE,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Please rate your anxiety level from 1 (very low) to 10 (very high).")
                    ),
                    aiResponseEnabled = true,
                    predefinedResponses = listOf(
                        "That's wonderful progress! You've learned valuable skills to manage anxiety. Remember to practice these techniques regularly.",
                        "Even small progress is still progress. You're building important skills that will get stronger with practice."
                    ),
                    nextSteps = listOf()
                )
            )
        )
    }
    
    /**
     * Get overthinking management flow
     */
    fun getOverthinkingFlow(): ConversationFlow {
        return ConversationFlow(
            id = "overthinking_flow",
            name = "Overthinking Management",
            state = ConversationState.OVERTHINKING,
            description = "Structured flow for managing rumination and overthinking",
            steps = listOf(
                FlowStep(
                    id = "overthinking_capture",
                    step = 0,
                    title = "Capturing Overthinking Patterns",
                    prompt = "I hear you're caught in overthinking. That mental loop can be exhausting. Let's gently capture what's going on in your mind right now. What specific thoughts are repeating in your head?",
                    expectedInputType = InputType.TEXT,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Help me understand what thoughts are looping so we can work with them.")
                    ),
                    aiResponseEnabled = true,
                    exercises = listOf(
                        Exercise(
                            id = "thought_capture",
                            name = "Thought Capture Journal",
                            description = "Externalize repeating thoughts to break the cycle",
                            type = ExerciseType.JOURNALING,
                            duration = 300,
                            instructions = listOf(
                                "Write down the repeating thought",
                                "Notice how it makes you feel physically",
                                "Ask: Is this thought helpful right now?",
                                "Set a timer for 2 minutes to just observe"
                            ),
                            benefits = listOf("Breaks rumination cycle", "Creates distance from thoughts", "Reduces mental exhaustion"),
                            difficulty = Difficulty.EASY
                        )
                    ),
                    nextSteps = listOf("overthinking_challenge", "pattern_interruption")
                ),
                
                FlowStep(
                    id = "overthinking_challenge",
                    step = 1,
                    title = "Challenging Overthinking Patterns",
                    prompt = "Thank you for sharing those thoughts with me. Overthinking often involves patterns that aren't actually helpful. Let's gently challenge this pattern. Is there any evidence that what you're worrying about will actually happen?",
                    expectedInputType = InputType.YES_NO,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Please answer yes or no to help us examine this thought pattern.")
                    ),
                    aiResponseEnabled = true,
                    exercises = listOf(
                        Exercise(
                            id = "reality_check",
                            name = "Reality Check Technique",
                            description = "Test thoughts against reality",
                            type = ExerciseType.COGNITIVE,
                            duration = 600,
                            instructions = listOf(
                                "What evidence supports this thought?",
                                "What evidence contradicts this thought?",
                                "What's a more balanced perspective?",
                                "What would happen if you let this thought go?"
                            ),
                            benefits = listOf("Reduces catastrophic thinking", "Improves perspective", "Builds rational thinking"),
                            difficulty = Difficulty.MODERATE
                        )
                    ),
                    nextSteps = listOf("overthinking_reframe", "behavioral_activation")
                ),
                
                FlowStep(
                    id = "overthinking_reframe",
                    step = 2,
                    title = "Reframing and Redirecting",
                    prompt = "You're doing great work challenging those thoughts. Now let's practice reframing them into something more helpful or neutral. What's a more balanced or compassionate way to think about this situation?",
                    expectedInputType = InputType.TEXT,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Help me find a more balanced perspective on this situation.")
                    ),
                    aiResponseEnabled = true,
                    exercises = listOf(
                        Exercise(
                            id = "thought_reframing",
                            name = "Thought Reframing",
                            description = "Transform negative thoughts into balanced perspectives",
                            type = ExerciseType.COGNITIVE,
                            duration = 450,
                            instructions = listOf(
                                "Identify the negative thought",
                                "Ask: What's a more realistic perspective?",
                                "Ask: What would I tell a friend?",
                                "Create a balanced alternative thought"
                            ),
                            benefits = listOf("Reduces negative thinking", "Improves mental flexibility", "Builds self-compassion"),
                            difficulty = Difficulty.MODERATE
                        )
                    ),
                    nextSteps = listOf("overthinking_action", "mindful_redirect")
                ),
                
                FlowStep(
                    id = "overthinking_action",
                    step = 3,
                    title = "Taking Action Instead of Thinking",
                    prompt = "Sometimes the best way to stop overthinking is to take meaningful action. What small, concrete action could you take right now that would be more helpful than continuing to think about this?",
                    expectedInputType = InputType.TEXT,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Let's find a helpful action you can take instead of continuing to think.")
                    ),
                    aiResponseEnabled = true,
                    exercises = listOf(
                        Exercise(
                            id = "behavioral_activation",
                            name = "Behavioral Activation",
                            description = "Take action to break overthinking cycles",
                            type = ExerciseType.BEHAVIORAL,
                            duration = 900,
                            instructions = listOf(
                                "Choose one small, meaningful action",
                                "Focus on the action, not the thoughts",
                                "Notice how it feels to do something",
                                "Acknowledge any thoughts that arise but return to action"
                            ),
                            benefits = listOf("Breaks rumination cycle", "Builds confidence", "Creates positive momentum"),
                            difficulty = Difficulty.MODERATE
                        )
                    ),
                    nextSteps = listOf("overthinking_acceptance", "progress_review")
                ),
                
                FlowStep(
                    id = "overthinking_acceptance",
                    step = 4,
                    title = "Acceptance and Letting Go",
                    prompt = "You've made excellent progress managing your overthinking. Sometimes the most powerful step is accepting that some thoughts will come and go, and that's okay. How does it feel to consider letting these thoughts pass through without needing to solve them?",
                    expectedInputType = InputType.TEXT,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Share how acceptance feels to you.")
                    ),
                    aiResponseEnabled = true,
                    exercises = listOf(
                        Exercise(
                            id = "thought_acceptance",
                            name = "Thought Acceptance",
                            description = "Practice letting thoughts come and go without engagement",
                            type = ExerciseType.MINDFULNESS,
                            duration = 600,
                            instructions = listOf(
                                "Sit comfortably and close your eyes",
                                "Notice thoughts as clouds passing in the sky",
                                "Don't try to change or stop them",
                                "Return focus to your breath when you notice you're thinking"
                            ),
                            benefits = listOf("Reduces thought struggle", "Builds mental flexibility", "Promotes inner peace"),
                            difficulty = Difficulty.MODERATE
                        )
                    ),
                    nextSteps = listOf("overthinking_progress_review")
                ),
                
                FlowStep(
                    id = "overthinking_progress_review",
                    step = 5,
                    title = "Progress Review and Maintenance",
                    prompt = "You've done wonderful work managing your overthinking patterns. Let's review what you've learned and create a maintenance plan. What technique worked best for you today?",
                    expectedInputType = InputType.MULTIPLE_CHOICE,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Please select which technique was most helpful for you.")
                    ),
                    aiResponseEnabled = true,
                    predefinedResponses = listOf(
                        "Excellent! You've built a toolkit of effective strategies. Remember to use these techniques regularly to keep overthinking in check.",
                        "Great job identifying what works for you. Practice makes these techniques more automatic and effective."
                    ),
                    resources = listOf(
                        Resource(
                            id = "overthinking_toolkit",
                            name = "Overthinking Management Toolkit",
                            description = "Comprehensive guide for managing rumination and overthinking",
                            type = ResourceType.TOOL,
                            category = ResourceCategory.ANXIETY,
                            tags = listOf("overthinking", "rumination", "cognitive", "mindfulness"),
                            estimatedTime = 20
                        )
                    ),
                    nextSteps = listOf()
                )
            )
        )
    }
    
    /**
     * Get low mood management flow
     */
    fun getLowMoodFlow(): ConversationFlow {
        return ConversationFlow(
            id = "low_mood_flow",
            name = "Low Mood Support",
            state = ConversationState.LOW_MOOD,
            description = "Structured flow for supporting low mood and depression",
            steps = listOf(
                FlowStep(
                    id = "low_mood_validation",
                    step = 0,
                    title = "Validating Your Feelings",
                    prompt = "I hear that you're feeling down right now, and I want you to know that your feelings are completely valid. It's okay to feel this way. Can you tell me more about what this low mood feels like for you?",
                    expectedInputType = InputType.TEXT,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Your feelings matter. Please share what this low mood feels like.")
                    ),
                    aiResponseEnabled = true,
                    predefinedResponses = listOf(
                        "Thank you for sharing that with me. It takes courage to acknowledge when you're feeling down, and I'm here to support you through this.",
                        "I understand how heavy low mood can feel. You're not alone in this, and we'll work through it together."
                    ),
                    nextSteps = listOf("low_mood_gentle_activity", "self_compassion")
                ),
                
                FlowStep(
                    id = "low_mood_gentle_activity",
                    step = 1,
                    title = "Gentle Activity Scheduling",
                    prompt = "When we're feeling down, even small activities can help lift our mood. What's one gentle, manageable activity you could try today? It doesn't need to be big - just something small and kind to yourself.",
                    expectedInputType = InputType.TEXT,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Let's find one small activity that feels manageable for you.")
                    ),
                    aiResponseEnabled = true,
                    exercises = listOf(
                        Exercise(
                            id = "behavioral_activation_gentle",
                            name = "Gentle Behavioral Activation",
                            description = "Small activities to gently lift mood",
                            type = ExerciseType.BEHAVIORAL,
                            duration = 300,
                            instructions = listOf(
                                "Choose one small activity (5-15 minutes)",
                                "Focus on the activity, not the outcome",
                                "Notice any small positive feelings",
                                "Be kind to yourself regardless of results"
                            ),
                            benefits = listOf("Gently lifts mood", "Builds momentum", "Reduces isolation"),
                            difficulty = Difficulty.EASY
                        )
                    ),
                    nextSteps = listOf("low_mood_self_compassion", "gratitude_practice")
                ),
                
                FlowStep(
                    id = "low_mood_self_compassion",
                    step = 2,
                    title = "Practicing Self-Compassion",
                    prompt = "When we're feeling down, it's easy to be hard on ourselves. Let's practice treating yourself with the same kindness you'd offer a good friend. What kind words would you say to a friend feeling this way?",
                    expectedInputType = InputType.TEXT,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Imagine what you'd say to a friend - now try saying those words to yourself.")
                    ),
                    aiResponseEnabled = true,
                    exercises = listOf(
                        Exercise(
                            id = "self_compassion_meditation",
                            name = "Self-Compassion Meditation",
                            description = "Practice kindness toward yourself",
                            type = ExerciseType.MINDFULNESS,
                            duration = 600,
                            instructions = listOf(
                                "Place a hand over your heart",
                                "Acknowledge your suffering: 'This is hard right now'",
                                "Offer kindness: 'May I be kind to myself'",
                                "Remember you're not alone in this feeling"
                            ),
                            benefits = listOf("Reduces self-criticism", "Builds emotional resilience", "Promotes healing"),
                            difficulty = Difficulty.MODERATE
                        )
                    ),
                    nextSteps = listOf("low_mood_gratitude", "meaningful_connection")
                ),
                
                FlowStep(
                    id = "low_mood_gratitude",
                    step = 3,
                    title = "Finding Gratitude",
                    prompt = "Even when we're feeling down, there are usually small things we can be grateful for. Can you identify three small things you're grateful for right now? They don't need to be big - just authentic.",
                    expectedInputType = InputType.TEXT,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Gratitude can be found in small things. What three things come to mind?")
                    ),
                    aiResponseEnabled = true,
                    exercises = listOf(
                        Exercise(
                            id = "gratitude_practice",
                            name = "Gratitude Practice",
                            description = "Find and appreciate small things to be grateful for",
                            type = ExerciseType.JOURNALING,
                            duration = 300,
                            instructions = listOf(
                                "Write down 3 things you're grateful for",
                                "Notice how each one makes you feel",
                                "Appreciate the small details",
                                "Allow yourself to feel the gratitude"
                            ),
                            benefits = listOf("Lifts mood naturally", "Shifts perspective", "Builds resilience"),
                            difficulty = Difficulty.EASY
                        )
                    ),
                    nextSteps = listOf("low_mood_connection", "progress_assessment")
                ),
                
                FlowStep(
                    id = "low_mood_connection",
                    step = 4,
                    title = "Meaningful Connection",
                    prompt = "Low mood can make us feel isolated, but connection can help. Is there someone you could reach out to - even just to say hello? Or would you prefer to connect with me more?",
                    expectedInputType = InputType.MULTIPLE_CHOICE,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Connection is important. Please choose one option.")
                    ),
                    aiResponseEnabled = true,
                    exercises = listOf(
                        Exercise(
                            id = "social_connection",
                            name = "Social Connection Practice",
                            description = "Gentle ways to connect with others",
                            type = ExerciseType.SOCIAL,
                            duration = 300,
                            instructions = listOf(
                                "Send a simple message to someone",
                                "Share something positive with a friend",
                                "Ask someone how they're doing",
                                "Practice active listening"
                            ),
                            benefits = listOf("Reduces isolation", "Provides support", "Builds relationships"),
                            difficulty = Difficulty.EASY
                        )
                    ),
                    nextSteps = listOf("low_mood_progress_review")
                ),
                
                FlowStep(
                    id = "low_mood_progress_review",
                    step = 5,
                    title = "Progress Review and Hope",
                    prompt = "You've been working thoughtfully with your low mood. I want to acknowledge your courage and effort. On a scale of 1-10, how are you feeling now compared to when we started this conversation?",
                    expectedInputType = InputType.SCALE,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Please rate how you're feeling from 1 (very low) to 10 (much better).")
                    ),
                    aiResponseEnabled = true,
                    predefinedResponses = listOf(
                        "Every step forward matters, no matter how small. You're building important skills for managing difficult emotions.",
                        "Even small improvement is progress. You're learning to be compassionate with yourself during difficult times."
                    ),
                    resources = listOf(
                        Resource(
                            id = "low_mood_support",
                            name = "Low Mood Support Resources",
                            description = "Comprehensive resources for managing depression and low mood",
                            type = ResourceType.RESOURCE_LINK,
                            category = ResourceCategory.DEPRESSION,
                            tags = listOf("depression", "low mood", "support", "resources"),
                            estimatedTime = 30
                        )
                    ),
                    nextSteps = listOf()
                )
            )
        )
    }
    
    /**
     * Get sleep management flow
     */
    fun getSleepFlow(): ConversationFlow {
        return ConversationFlow(
            id = "sleep_flow",
            name = "Sleep Management",
            state = ConversationState.SLEEP,
            description = "Structured flow for improving sleep quality and managing insomnia",
            steps = listOf(
                FlowStep(
                    id = "sleep_assessment",
                    step = 0,
                    title = "Sleep Assessment",
                    prompt = "I understand you're having trouble with sleep. Let me help you work on that. Can you tell me more about what's been happening with your sleep? Are you having trouble falling asleep, staying asleep, or both?",
                    expectedInputType = InputType.MULTIPLE_CHOICE,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Please share what's happening with your sleep so I can help you better.")
                    ),
                    aiResponseEnabled = true,
                    nextSteps = listOf("sleep_environment", "sleep_routine")
                ),
                
                FlowStep(
                    id = "sleep_environment",
                    step = 1,
                    title = "Optimizing Sleep Environment",
                    prompt = "Our sleep environment has a big impact on our ability to rest. Let's optimize yours. What's your bedroom like right now? Is it dark, quiet, and cool?",
                    expectedInputType = InputType.MULTIPLE_CHOICE,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Help me understand your sleep environment so we can improve it.")
                    ),
                    aiResponseEnabled = true,
                    exercises = listOf(
                        Exercise(
                            id = "sleep_environment_optimization",
                            name = "Sleep Environment Setup",
                            description = "Create ideal conditions for sleep",
                            type = ExerciseType.BEHAVIORAL,
                            duration = 600,
                            instructions = listOf(
                                "Make room completely dark (blackout curtains)",
                                "Keep temperature cool (65-68°F)",
                                "Eliminate noise (earplugs, white noise)",
                                "Remove screens from bedroom",
                                "Use bed only for sleep"
                            ),
                            benefits = listOf("Improves sleep quality", "Reduces night awakenings", "Promotes deep sleep"),
                            difficulty = Difficulty.EASY
                        )
                    ),
                    nextSteps = listOf("sleep_routine", "relaxation_techniques")
                ),
                
                FlowStep(
                    id = "sleep_routine",
                    step = 2,
                    title = "Establishing Sleep Routine",
                    prompt = "A consistent bedtime routine signals to your body that it's time to sleep. What does your current bedtime routine look like, if you have one?",
                    expectedInputType = InputType.TEXT,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Tell me about your current bedtime routine so we can improve it.")
                    ),
                    aiResponseEnabled = true,
                    exercises = listOf(
                        Exercise(
                            id = "bedtime_routine",
                            name = "Bedtime Routine",
                            description = "Consistent routine to prepare for sleep",
                            type = ExerciseType.BEHAVIORAL,
                            duration = 900,
                            instructions = listOf(
                                "Start 30-60 minutes before bedtime",
                                "Dim lights and eliminate screens",
                                "Practice gentle stretching or yoga",
                                "Do relaxation breathing",
                                "Read something calming (no screens)"
                            ),
                            benefits = listOf("Signals bedtime to body", "Reduces sleep latency", "Improves sleep quality"),
                            difficulty = Difficulty.EASY
                        )
                    ),
                    nextSteps = listOf("sleep_relaxation", "thought_management")
                ),
                
                FlowStep(
                    id = "sleep_relaxation",
                    step = 3,
                    title = "Sleep Relaxation Techniques",
                    prompt = "When our minds are racing, it can be hard to sleep. Let's practice a relaxation technique to calm your body and mind. Would you like to try progressive muscle relaxation, body scan, or breathing exercises?",
                    expectedInputType = InputType.MULTIPLE_CHOICE,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Choose a relaxation technique to practice.")
                    ),
                    aiResponseEnabled = false,
                    exercises = listOf(
                        Exercise(
                            id = "progressive_muscle_relaxation",
                            name = "Progressive Muscle Relaxation",
                            description = "Systematic muscle relaxation for sleep",
                            type = ExerciseType.PROGRESSIVE_RELAXATION,
                            duration = 900,
                            instructions = listOf(
                                "Lie comfortably in bed",
                                "Tense and relax muscle groups systematically",
                                "Focus on the difference between tension and relaxation",
                                "Breathe slowly and deeply throughout"
                            ),
                            benefits = listOf("Reduces physical tension", "Calms nervous system", "Promotes sleep onset"),
                            difficulty = Difficulty.MODERATE
                        )
                    ),
                    nextSteps = listOf("sleep_thought_management", "progress_review")
                ),
                
                FlowStep(
                    id = "sleep_thought_management",
                    step = 4,
                    title = "Managing Sleep Thoughts",
                    prompt = "Racing thoughts can keep us awake. Let's practice a technique to manage them gently. What thoughts typically keep you awake when you're trying to sleep?",
                    expectedInputType = InputType.TEXT,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Sharing your sleep thoughts will help us work with them.")
                    ),
                    aiResponseEnabled = true,
                    exercises = listOf(
                        Exercise(
                            id = "thought_dump",
                            name = "Sleep Thought Dump",
                            description = "Externalize racing thoughts before sleep",
                            type = ExerciseType.JOURNALING,
                            duration = 300,
                            instructions = listOf(
                                "Keep a notepad by your bed",
                                "Write down any racing thoughts",
                                "Tell yourself you'll deal with them tomorrow",
                                "Focus on your breath instead"
                            ),
                            benefits = listOf("Reduces mental chatter", "Provides closure", "Promotes sleep onset"),
                            difficulty = Difficulty.EASY
                        )
                    ),
                    nextSteps = listOf("sleep_progress_review")
                ),
                
                FlowStep(
                    id = "sleep_progress_review",
                    step = 5,
                    title = "Sleep Progress Review",
                    prompt = "You've learned several techniques to improve your sleep. Let's create a plan for implementing them. Which technique feels most doable for you to start with?",
                    expectedInputType = InputType.MULTIPLE_CHOICE,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Choose one technique to start with.")
                    ),
                    aiResponseEnabled = true,
                    predefinedResponses = listOf(
                        "Great choice! Start with that technique for a week, then add another. Small consistent changes make the biggest difference.",
                        "Perfect! The best technique is the one you'll actually use. Be patient and kind to yourself as you build new habits."
                    ),
                    resources = listOf(
                        Resource(
                            id = "sleep_improvement_guide",
                            name = "Sleep Improvement Guide",
                            description = "Comprehensive guide for better sleep",
                            type = ResourceType.RESOURCE_LINK,
                            category = ResourceCategory.SLEEP,
                            tags = listOf("sleep", "insomnia", "sleep hygiene", "relaxation"),
                            estimatedTime = 45
                        )
                    ),
                    nextSteps = listOf()
                )
            )
        )
    }
    
    /**
     * Get general chat flow
     */
    fun getGeneralChatFlow(): ConversationFlow {
        return ConversationFlow(
            id = "general_chat_flow",
            name = "General Chat",
            state = ConversationState.GENERAL_CHAT,
            description = "General conversation and support",
            steps = listOf(
                FlowStep(
                    id = "general_checkin",
                    step = 0,
                    title = "General Check-in",
                    prompt = "Hello! I'm here to support you. How are you feeling today, and what would you like to talk about?",
                    expectedInputType = InputType.TEXT,
                    validationRules = emptyList(),
                    aiResponseEnabled = true,
                    nextSteps = listOf()
                )
            )
        )
    }
    
    /**
     * Get check-in flow
     */
    fun getCheckinFlow(): ConversationFlow {
        return ConversationFlow(
            id = "checkin_flow",
            name = "Daily Check-in",
            state = ConversationState.CHECKIN,
            description = "Daily mood and wellbeing check-in",
            steps = listOf(
                FlowStep(
                    id = "mood_rating",
                    step = 0,
                    title = "Mood Rating",
                    prompt = "Let's start with how you're feeling today. On a scale of 1-10, how would you rate your current mood?",
                    expectedInputType = InputType.SCALE,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Please rate your mood from 1 (very low) to 10 (very high).")
                    ),
                    aiResponseEnabled = true,
                    nextSteps = listOf("energy_level", "stress_level")
                ),
                
                FlowStep(
                    id = "energy_level",
                    step = 1,
                    title = "Energy Level",
                    prompt = "Thank you for sharing that. How is your energy level today? Are you feeling energized, tired, or somewhere in between?",
                    expectedInputType = InputType.MULTIPLE_CHOICE,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Please select your energy level.")
                    ),
                    aiResponseEnabled = true,
                    nextSteps = listOf("stress_level", "sleep_quality")
                ),
                
                FlowStep(
                    id = "stress_level",
                    step = 2,
                    title = "Stress Level",
                    prompt = "How would you rate your stress level today? Are you feeling relaxed, moderately stressed, or highly stressed?",
                    expectedInputType = InputType.MULTIPLE_CHOICE,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Please select your stress level.")
                    ),
                    aiResponseEnabled = true,
                    nextSteps = listOf("checkin_summary", "recommendations")
                ),
                
                FlowStep(
                    id = "checkin_summary",
                    step = 3,
                    title = "Check-in Summary",
                    prompt = "Thank you for sharing all that with me. Based on your check-in, is there anything specific you'd like support with today?",
                    expectedInputType = InputType.TEXT,
                    validationRules = emptyList(),
                    aiResponseEnabled = true,
                    nextSteps = listOf()
                )
            )
        )
    }
    
    /**
     * Get crisis flow
     */
    fun getCrisisFlow(): ConversationFlow {
        return ConversationFlow(
            id = "crisis_flow",
            name = "Crisis Intervention",
            state = ConversationState.CRISIS,
            description = "Immediate crisis intervention and support",
            steps = listOf(
                FlowStep(
                    id = "crisis_immediate",
                    step = 0,
                    title = "Immediate Support",
                    prompt = "I'm here with you and you're not alone. Your safety is the most important thing right now. Can you tell me if you're in a safe place?",
                    expectedInputType = InputType.YES_NO,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Please let me know if you're safe so I can help you properly.")
                    ),
                    aiResponseEnabled = true,
                    resources = listOf(
                        Resource(
                            id = "crisis_hotline",
                            name = "Crisis Hotline",
                            description = "24/7 crisis support hotline",
                            type = ResourceType.HOTLINE,
                            category = ResourceCategory.CRISIS,
                            tags = listOf("crisis", "emergency", "support", "hotline"),
                            url = "tel:988"
                        )
                    ),
                    nextSteps = listOf("crisis_safety", "crisis_resources")
                )
            )
        )
    }
    
    /**
     * Get onboarding flow
     */
    fun getOnboardingFlow(): ConversationFlow {
        return ConversationFlow(
            id = "onboarding_flow",
            name = "User Onboarding",
            state = ConversationState.ONBOARDING,
            description = "Initial user onboarding and setup",
            steps = listOf(
                FlowStep(
                    id = "welcome_intro",
                    step = 0,
                    title = "Welcome to DrMindit",
                    prompt = "Welcome to DrMindit! I'm your mental health companion, here to support you 24/7. To get started, could you tell me your name and what brought you here today?",
                    expectedInputType = InputType.TEXT,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Please share your name so I can personalize our conversations.")
                    ),
                    aiResponseEnabled = true,
                    nextSteps = listOf("goals_assessment", "preferences_setup")
                )
            )
        )
    }
    
    /**
     * Get stress relief flow
     */
    fun getStressReliefFlow(): ConversationFlow {
        return ConversationFlow(
            id = "stress_relief_flow",
            name = "Stress Relief",
            state = ConversationState.GENERAL_CHAT,
            description = "Quick stress relief techniques",
            steps = listOf(
                FlowStep(
                    id = "stress_assessment",
                    step = 0,
                    title = "Stress Assessment",
                    prompt = "I can help you manage stress. What's causing you stress right now, and how intense is it on a scale of 1-10?",
                    expectedInputType = InputType.TEXT,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Please share what's stressing you so I can help.")
                    ),
                    aiResponseEnabled = true,
                    exercises = listOf(
                        Exercise(
                            id = "quick_stress_relief",
                            name = "Quick Stress Relief",
                            description = "Immediate stress reduction technique",
                            type = ExerciseType.BREATHING,
                            duration = 180,
                            instructions = listOf(
                                "Take a deep breath in through your nose",
                                "Hold for 4 counts",
                                "Exhale slowly through your mouth",
                                "Repeat 3-5 times"
                            ),
                            benefits = listOf("Immediate stress reduction", "Calms nervous system", "Quick and easy"),
                            difficulty = Difficulty.EASY
                        )
                    ),
                    nextSteps = listOf()
                )
            )
        )
    
    /**
     * Get resource flow
     */
    fun getResourceFlow(): ConversationFlow {
        return ConversationFlow(
            id = "resource_flow",
            name = "Resource Recommendations",
            state = ConversationState.RESOURCE_RECOMMENDATION,
            description = "Resource and tool recommendations",
            steps = listOf(
                FlowStep(
                    id = "resource_assessment",
                    step = 0,
                    title = "Resource Assessment",
                    prompt = "I can recommend helpful resources for you. What type of support are you looking for right now?",
                    expectedInputType = InputType.MULTIPLE_CHOICE,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Please select what type of resource would be most helpful.")
                    ),
                    aiResponseEnabled = true,
                    nextSteps = listOf()
                )
            )
        )
    }
    
    /**
     * Get progress flow
     */
    fun getProgressFlow(): ConversationFlow {
        return ConversationFlow(
            id = "progress_flow",
            name = "Progress Tracking",
            state = ConversationState.PROGRESS_TRACKING,
            description = "Progress tracking and goal setting",
            steps = listOf(
                FlowStep(
                    id = "progress_assessment",
                    step = 0,
                    title = "Progress Assessment",
                    prompt = "I'd love to hear about your progress! What's been going well in your mental health journey, and what challenges are you facing?",
                    expectedInputType = InputType.TEXT,
                    validationRules = listOf(
                        ValidationRule(ValidationType.REQUIRED, errorMessage = "Please share your progress so I can support you better.")
                    ),
                    aiResponseEnabled = true,
                    nextSteps = listOf()
                )
            )
        )
    }
}

/**
 * Conversation flow definition
 */
data class ConversationFlow(
    val id: String,
    val name: String,
    val state: ConversationState,
    val description: String,
    val steps: List<FlowStep>,
    val estimatedDuration: Int = 0, // in minutes
    val difficulty: Difficulty = Difficulty.EASY,
    val prerequisites: List<String> = emptyList(),
    val outcomes: List<String> = emptyList()
)
