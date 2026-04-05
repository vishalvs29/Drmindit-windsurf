import { NextRequest, NextResponse } from 'next/server'
import { getLanguageConfig, SUPPORTED_LANGUAGES } from '../../lib/languageMapping'

// Speech-to-Text API endpoint
export async function POST(request: NextRequest) {
  try {
    const requestFormData = await request.formData()
    const audioFile = requestFormData.get('audio') as File
    const languageCode = requestFormData.get('language') as string || 'en'
    
    if (!audioFile) {
      return NextResponse.json(
        { error: 'No audio file provided' },
        { status: 400 }
      )
    }

    // Validate language code
    const languageConfig = getLanguageConfig(languageCode)
    if (!languageConfig) {
      return NextResponse.json(
        { 
          error: 'Unsupported language',
          supportedLanguages: Object.keys(SUPPORTED_LANGUAGES)
        },
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
    
    // Use language code for Whisper API (empty string for auto-detect)
    apiFormData.append('language', languageConfig.whisperCode)
    apiFormData.append('response_format', 'json')

    // Call speech-to-text service (using OpenAI Whisper API as example)
    const response = await fetch('https://api.openai.com/v1/audio/transcriptions', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${process.env.OPENAI_API_KEY}`,
      },
      body: apiFormData
    })

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      console.error('STT API Error:', errorData)
      return NextResponse.json(
        { error: 'Speech-to-text service temporarily unavailable' },
        { status: 500 }
      )
    }

    const transcriptionData = await response.json()
    
    return NextResponse.json({
      text: transcriptionData.text,
      language: languageConfig.code,
      languageName: languageConfig.displayName,
      confidence: transcriptionData.confidence || null,
      duration: transcriptionData.duration || null
    })

  } catch (error) {
    console.error('STT Error:', error)
    return NextResponse.json(
      { error: 'Speech-to-text service temporarily unavailable' },
      { status: 500 }
    )
  }
}

// GET endpoint to retrieve supported languages
export async function GET() {
  try {
    return NextResponse.json({
      supportedLanguages: SUPPORTED_LANGUAGES,
      defaultLanguage: 'en',
      indianLanguages: ['hi', 'ta', 'mr', 'bn', 'gu', 'te', 'kn', 'ml', 'pa', 'ur'],
      status: 'healthy',
      service: 'speech-to-text',
      timestamp: new Date().toISOString()
    })
  } catch (error) {
    console.error('Language List Error:', error)
    return NextResponse.json(
      { error: 'Unable to retrieve supported languages' },
      { status: 500 }
    )
  }
}
