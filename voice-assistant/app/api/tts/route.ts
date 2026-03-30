import { NextRequest, NextResponse } from 'next/server'

// Text-to-Speech API endpoint
export async function POST(request: NextRequest) {
  try {
    const { text, voice = 'calm-female', speed = 1.0 } = await request.json()

    if (!text) {
      return NextResponse.json(
        { error: 'No text provided' },
        { status: 400 }
      )
    }

    // Validate text length
    if (text.length > 5000) {
      return NextResponse.json(
        { error: 'Text too long. Maximum 5000 characters allowed.' },
        { status: 400 }
      )
    }

    // Call text-to-speech service (using OpenAI TTS API as example)
    const response = await fetch('https://api.openai.com/v1/audio/speech', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${process.env.OPENAI_API_KEY}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        model: 'tts-1',
        input: text,
        voice: getVoiceModel(voice),
        speed: speed,
        response_format: 'mp3'
      })
    })

    if (!response.ok) {
      throw new Error('Text-to-speech service failed')
    }

    // Get audio data
    const audioBuffer = await response.arrayBuffer()
    const audioBase64 = Buffer.from(audioBuffer).toString('base64')

    return NextResponse.json({
      audio: audioBase64,
      format: 'mp3',
      voice: voice,
      speed: speed,
      duration: await getAudioDuration(audioBuffer)
    })

  } catch (error) {
    console.error('TTS Error:', error)
    return NextResponse.json(
      { error: 'Text-to-speech conversion failed' },
      { status: 500 }
    )
  }
}

// Map voice names to TTS model voices
function getVoiceModel(voiceName: string): string {
  const voiceMap: { [key: string]: string } = {
    'calm-female': 'alloy',
    'calm-male': 'echo',
    'gentle-female': 'fable',
    'professional-female': 'onyx',
    'soothing-male': 'nova',
    'default': 'alloy'
  }
  
  return voiceMap[voiceName] || voiceMap['default']
}

// Get audio duration (simplified)
async function getAudioDuration(audioBuffer: ArrayBuffer): Promise<number> {
  // This is a simplified version - in production, you'd use a proper audio library
  // For now, estimating based on text length and average speech rate
  return 0 // Placeholder
}

// Predefined mental health scripts for TTS
export async function GET() {
  const predefinedScripts = {
    quickCalm: [
      {
        id: 'breathing-exercise',
        title: 'Quick Breathing Exercise',
        text: "Let's take a moment to find your calm. Close your eyes if that feels comfortable. Take a deep breath in through your nose for four counts... hold for four counts... and exhale slowly through your mouth for six counts. Notice how your body feels with each breath. Let's do this three more times, allowing each breath to bring you deeper into relaxation.",
        duration: 120,
        category: 'stress-relief'
      },
      {
        id: 'mindful-minute',
        title: 'One-Minute Mindfulness',
        text: "For the next sixty seconds, let's simply be present. Notice the feeling of your feet on the ground. Feel the air moving in and out of your body. Hear the sounds around you without judgment. You are exactly where you need to be right now. You are safe. You are capable.",
        duration: 60,
        category: 'mindfulness'
      },
      {
        id: 'stress-release',
        title: 'Stress Release',
        text: "Imagine your stress as a cloud in your mind. With each breath, watch this cloud get smaller and smaller. Breathe in peace and breathe out tension. Your shoulders can relax. Your jaw can unclench. Your brow can smooth. You are releasing what no longer serves you.",
        duration: 90,
        category: 'stress-relief'
      }
    ],
    guidedSessions: [
      {
        id: 'sleep-preparation',
        title: 'Sleep Preparation',
        text: "As you prepare for sleep, let's release the day. Scan your body from head to toe, allowing each part to relax. Your thoughts can drift by like clouds in the sky. You don't need to hold onto anything. You are safe. You are supported. You deserve this rest.",
        duration: 300,
        category: 'sleep'
      },
      {
        id: 'focus-enhancement',
        title: 'Focus Enhancement',
        text: "Let's prepare your mind for focused work. Bring your attention to your breath. When your mind wanders, gently guide it back. You have the ability to concentrate. You have the clarity you need. Trust in your capacity to learn and grow.",
        duration: 180,
        category: 'focus'
      }
    ]
  }

  return NextResponse.json({
    scripts: predefinedScripts,
    timestamp: new Date().toISOString()
  })
}
