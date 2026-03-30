'use client'

import { useState, useRef, useEffect } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Mic, MicOff, Play, Pause, Volume2, AlertCircle, Phone } from 'lucide-react'

interface VoiceAssistantProps {
  onTranscript?: (text: string) => void
  onAIResponse?: (response: string) => void
  className?: string
}

export default function VoiceAssistant({ onTranscript, onAIResponse, className }: VoiceAssistantProps) {
  const [isListening, setIsListening] = useState(false)
  const [isProcessing, setIsProcessing] = useState(false)
  const [isPlaying, setIsPlaying] = useState(false)
  const [transcript, setTranscript] = useState('')
  const [aiResponse, setAiResponse] = useState('')
  const [showTranscript, setShowTranscript] = useState(false)
  const [distressAlert, setDistressAlert] = useState(false)
  
  const mediaRecorderRef = useRef<MediaRecorder | null>(null)
  const audioChunksRef = useRef<Blob[]>([])
  const audioRef = useRef<HTMLAudioElement | null>(null)

  // Start/Stop listening
  const toggleListening = async () => {
    if (isListening) {
      stopListening()
    } else {
      await startListening()
    }
  }

  // Start voice recording
  const startListening = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
      const mediaRecorder = new MediaRecorder(stream)
      
      mediaRecorderRef.current = mediaRecorder
      audioChunksRef.current = []

      mediaRecorder.ondataavailable = (event) => {
        if (event.data.size > 0) {
          audioChunksRef.current.push(event.data)
        }
      }

      mediaRecorder.onstop = async () => {
        const audioBlob = new Blob(audioChunksRef.current, { type: 'audio/wav' })
        await transcribeAudio(audioBlob)
      }

      mediaRecorder.start()
      setIsListening(true)
      setTranscript('')
      
    } catch (error) {
      console.error('Error accessing microphone:', error)
      alert('Please allow microphone access to use voice features')
    }
  }

  // Stop voice recording
  const stopListening = () => {
    if (mediaRecorderRef.current && isListening) {
      mediaRecorderRef.current.stop()
      mediaRecorderRef.current.stream.getTracks().forEach(track => track.stop())
      setIsListening(false)
      setIsProcessing(true)
    }
  }

  // Transcribe audio using API
  const transcribeAudio = async (audioBlob: Blob) => {
    try {
      const formData = new FormData()
      formData.append('audio', audioBlob, 'recording.wav')

      const response = await fetch('/api/stt', {
        method: 'POST',
        body: formData
      })

      const result = await response.json()
      
      if (result.text) {
        setTranscript(result.text)
        onTranscript?.(result.text)
        await getAIResponse(result.text)
      }
      
    } catch (error) {
      console.error('Transcription error:', error)
      setTranscript('Sorry, I had trouble understanding that. Please try again.')
    } finally {
      setIsProcessing(false)
    }
  }

  // Get AI response
  const getAIResponse = async (userMessage: string) => {
    try {
      const response = await fetch('/api/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          message: userMessage,
          sessionId: 'voice-session-' + Date.now()
        })
      })

      const result = await response.json()
      
      if (result.response) {
        setAiResponse(result.response)
        onAIResponse?.(result.response)
        
        // Check for distress
        if (result.distressDetected) {
          setDistressAlert(true)
        }
        
        // Convert to speech
        await speakText(result.response)
      }
      
    } catch (error) {
      console.error('AI response error:', error)
      setAiResponse('I apologize, but I\'m having trouble responding right now. Please try again.')
    }
  }

  // Text-to-speech
  const speakText = async (text: string) => {
    try {
      const response = await fetch('/api/tts', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          text: text,
          voice: 'calm-female',
          speed: 0.9
        })
      })

      const result = await response.json()
      
      if (result.audio) {
        const audioData = 'data:audio/mp3;base64,' + result.audio
        const audio = new Audio(audioData)
        audioRef.current = audio
        
        audio.onplay = () => setIsPlaying(true)
        audio.onended = () => setIsPlaying(false)
        
        await audio.play()
      }
      
    } catch (error) {
      console.error('TTS error:', error)
    }
  }

  // Toggle audio playback
  const togglePlayback = () => {
    if (audioRef.current) {
      if (isPlaying) {
        audioRef.current.pause()
        setIsPlaying(false)
      } else {
        audioRef.current.play()
        setIsPlaying(true)
      }
    }
  }

  // Quick calm feature
  const quickCalm = async () => {
    try {
      const response = await fetch('/api/tts')
      const result = await response.json()
      
      if (result.scripts?.quickCalm?.length > 0) {
        const calmScript = result.scripts.quickCalm[0]
        setAiResponse(calmScript.text)
        await speakText(calmScript.text)
      }
      
    } catch (error) {
      console.error('Quick calm error:', error)
    }
  }

  return (
    <div className={`voice-assistant ${className}`}>
      {/* Distress Alert */}
      <AnimatePresence>
        {distressAlert && (
          <motion.div
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.9 }}
            className="distress-alert mb-6"
          >
            <AlertCircle className="w-6 h-6 mb-2 mx-auto" />
            <h3 className="text-lg font-semibold mb-2">Support Available</h3>
            <p className="mb-4">I notice you may be going through a difficult time. Help is available 24/7.</p>
            <div className="space-y-2 text-sm">
              <div><strong>988 Suicide & Crisis Lifeline:</strong> Call or text 988</div>
              <div><strong>Crisis Text Line:</strong> Text HOME to 741741</div>
            </div>
            <button
              onClick={() => setDistressAlert(false)}
              className="mt-4 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
            >
              I Understand
            </button>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Voice Interface */}
      <div className="glass-card rounded-3xl p-8 max-w-md mx-auto">
        <div className="text-center mb-6">
          <h2 className="text-2xl font-bold mb-2 text-gradient">AI Voice Assistant</h2>
          <p className="text-gray-300">Click the microphone to speak with me</p>
        </div>

        {/* Voice Button */}
        <div className="flex justify-center mb-6">
          <button
            onClick={toggleListening}
            disabled={isProcessing}
            className={`voice-button ${isListening ? 'listening' : ''} ${isProcessing ? 'processing' : ''}`}
          >
            {isListening && <div className="wave-animation" />}
            {isProcessing ? (
              <div className="animate-spin">
                <Volume2 className="w-8 h-8 text-white" />
              </div>
            ) : isListening ? (
              <MicOff className="w-8 h-8 text-white" />
            ) : (
              <Mic className="w-8 h-8 text-white" />
            )}
          </button>
        </div>

        {/* Status Text */}
        <div className="text-center mb-6">
          {isListening && (
            <motion.p
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              className="text-accent-teal font-medium"
            >
              Listening... Speak now
            </motion.p>
          )}
          {isProcessing && (
            <motion.p
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              className="text-yellow-400 font-medium"
            >
              Processing your request...
            </motion.p>
          )}
          {isPlaying && (
            <motion.p
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              className="text-green-400 font-medium"
            >
              Playing response...
            </motion.p>
          )}
        </div>

        {/* Quick Actions */}
        <div className="flex gap-3 justify-center mb-6">
          <button
            onClick={quickCalm}
            className="quick-calm-button flex items-center gap-2"
          >
            <Phone className="w-4 h-4" />
            Quick Calm
          </button>
          {audioRef.current && (
            <button
              onClick={togglePlayback}
              className="glass-card px-4 py-2 rounded-full flex items-center gap-2 hover:bg-white/20 transition-colors"
            >
              {isPlaying ? <Pause className="w-4 h-4" /> : <Play className="w-4 h-4" />}
              {isPlaying ? 'Pause' : 'Replay'}
            </button>
          )}
        </div>

        {/* Transcript Toggle */}
        <button
          onClick={() => setShowTranscript(!showTranscript)}
          className="w-full text-center text-gray-400 hover:text-white transition-colors mb-4"
        >
          {showTranscript ? 'Hide' : 'Show'} Transcript
        </button>

        {/* Transcript Display */}
        <AnimatePresence>
          {showTranscript && (
            <motion.div
              initial={{ opacity: 0, height: 0 }}
              animate={{ opacity: 1, height: 'auto' }}
              exit={{ opacity: 0, height: 0 }}
              className="space-y-3"
            >
              {transcript && (
                <div>
                  <h4 className="text-sm font-semibold text-accent-teal mb-1">You:</h4>
                  <div className="transcript-container">
                    {transcript}
                  </div>
                </div>
              )}
              
              {aiResponse && (
                <div>
                  <h4 className="text-sm font-semibold text-green-400 mb-1">AI Assistant:</h4>
                  <div className="transcript-container">
                    {aiResponse}
                  </div>
                </div>
              )}
            </motion.div>
          )}
        </AnimatePresence>

        {/* Safety Disclaimer */}
        <div className="safety-disclaimer mt-6 text-xs">
          <p>
            <strong>Disclaimer:</strong> This AI assistant is not a substitute for professional mental health care. 
            If you're experiencing a mental health crisis, please contact a healthcare professional or crisis hotline immediately.
          </p>
        </div>
      </div>
    </div>
  )
}
