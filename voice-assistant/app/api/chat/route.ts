import { NextRequest, NextResponse } from 'next/server'

// Mental health AI chat endpoint
export async function POST(request: NextRequest) {
  try {
    const { message, context, sessionId } = await request.json()

    if (!message) {
      return NextResponse.json(
        { error: 'No message provided' },
        { status: 400 }
      )
    }

    // Safety check for distress keywords
    const distressKeywords = [
      'suicide', 'kill myself', 'end my life', 'hurt myself',
      'self harm', 'die', 'death', 'hopeless', 'worthless'
    ]
    
    const messageLower = message.toLowerCase()
    const hasDistress = distressKeywords.some(keyword => 
      messageLower.includes(keyword)
    )

    if (hasDistress) {
      return NextResponse.json({
        response: "I notice you're going through a difficult time. Please reach out to a mental health professional or crisis hotline immediately. In India, you can call 112 for emergency services or 9152987821 for iCall mental health support. You're not alone, and help is available 24/7.",
        distressDetected: true,
        emergencyResources: [
          { name: 'Emergency Services', phone: '112' },
          { name: 'iCall Mental Health Helpline', phone: '9152987821' },
          { name: 'Vandrevala Foundation', phone: '1860-266-2600' },
          { name: 'AASRA Suicide Prevention', phone: '91-9820466726' }
        ]
      })
    }

    // Generate AI response for mental wellness
    const aiResponse = await generateMentalWellnessResponse(message, context)

    return NextResponse.json({
      response: aiResponse,
      sessionId: sessionId || generateSessionId(),
      timestamp: new Date().toISOString(),
      distressDetected: false
    })

  } catch (error) {
    console.error('Chat Error:', error)
    return NextResponse.json(
      { error: 'Chat service temporarily unavailable' },
      { status: 500 }
    )
  }
}

// Generate mental wellness response
async function generateMentalWellnessResponse(message: string, context?: any) {
  // This would integrate with a mental health AI model
  // For now, providing contextual responses based on message content
  
  const responses = {
    stress: [
      "I understand you're feeling stressed. Let's take a deep breath together. Inhale for 4 counts, hold for 4, and exhale for 6. This simple breathing exercise can help calm your nervous system.",
      "Stress is a natural response to challenges. Remember that you have the strength to handle this. Would you like to try a quick 5-minute relaxation exercise?"
    ],
    anxiety: [
      "Anxiety can feel overwhelming, but you have tools to manage it. Let's focus on the present moment. Notice 5 things you can see, 4 things you can touch, 3 things you can hear, 2 things you can smell, and 1 thing you can taste.",
      "Your feelings are valid. Anxiety is your body's way of protecting you. Let's work together to turn down the volume and find your calm center."
    ],
    sleep: [
      "Having trouble sleeping can be frustrating. Let's try a relaxation technique. Starting from your toes, gently tense and then release each muscle group, working your way up to your head.",
      "Good sleep is essential for mental wellness. Consider creating a calming bedtime routine. Would you like some guided meditation to help you relax?"
    ],
    focus: [
      "Finding focus can be challenging in our busy world. Let's try a simple mindfulness exercise. Bring your attention to your breath, and when your mind wanders, gently guide it back without judgment.",
      "Improving focus takes practice. Start with small, manageable periods of concentration and gradually increase them. Remember to take regular breaks to maintain productivity."
    ],
    general: [
      "I'm here to support your mental wellness journey. What specific aspect would you like to work on today - stress management, better sleep, improved focus, or something else?",
      "Thank you for reaching out. Taking care of your mental health is a sign of strength. How are you feeling right now?"
    ]
  }

  // Simple keyword matching for demo purposes
  const messageLower = message.toLowerCase()
  let category = 'general'
  
  if (messageLower.includes('stress') || messageLower.includes('overwhelm')) {
    category = 'stress'
  } else if (messageLower.includes('anxiety') || messageLower.includes('worry')) {
    category = 'anxiety'
  } else if (messageLower.includes('sleep') || messageLower.includes('insomnia')) {
    category = 'sleep'
  } else if (messageLower.includes('focus') || messageLower.includes('concentrate')) {
    category = 'focus'
  }

  const categoryResponses = responses[category as keyof typeof responses]
  return categoryResponses[Math.floor(Math.random() * categoryResponses.length)]
}

// Generate session ID
function generateSessionId(): string {
  return 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
}

// Health check for chat service
export async function GET() {
  return NextResponse.json({
    status: 'healthy',
    service: 'mental-wellness-chat',
    timestamp: new Date().toISOString()
  })
}
