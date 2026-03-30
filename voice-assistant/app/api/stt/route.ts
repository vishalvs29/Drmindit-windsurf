import { NextRequest, NextResponse } from 'next/server'

// Speech-to-Text API endpoint
export async function POST(request: NextRequest) {
  try {
    const requestFormData = await request.formData()
    const audioFile = requestFormData.get('audio') as File
    
    if (!audioFile) {
      return NextResponse.json(
        { error: 'No audio file provided' },
        { status: 400 }
      )
    }

    // Convert audio to buffer
    const audioBuffer = await audioFile.arrayBuffer()

    // Create FormData for OpenAI Whisper API
    const apiFormData = new FormData()
    const audioBlob = new Blob([audioBuffer], { type: audioFile.type || 'audio/mpeg' })
    apiFormData.append('file', audioBlob, audioFile.name || 'audio.mp3')
    apiFormData.append('model', 'whisper-1')
    apiFormData.append('language', 'en')
    apiFormData.append('response_format', 'json')

    // Call speech-to-text service (using OpenAI Whisper API as example)
    const response = await fetch('https://api.openai.com/v1/audio/transcriptions', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${process.env.OPENAI_API_KEY}`,
        // Don't set Content-Type for FormData - browser sets it with boundary
      },
      body: apiFormData
    })

    if (!response.ok) {
      throw new Error('Speech-to-text service failed')
    }

    const result = await response.json()
    
    return NextResponse.json({
      text: result.text,
      confidence: result.confidence || 0.95,
      duration: result.duration || 0
    })

  } catch (error) {
    console.error('STT Error:', error)
    return NextResponse.json(
      { error: 'Speech-to-text conversion failed' },
      { status: 500 }
    )
  }
}

// Health check for STT service
export async function GET() {
  return NextResponse.json({
    status: 'healthy',
    service: 'speech-to-text',
    timestamp: new Date().toISOString()
  })
}
