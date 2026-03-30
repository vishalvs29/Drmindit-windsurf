'use client'

import { useState, useRef, useEffect } from 'react'
import { motion } from 'framer-motion'
import { Play, Pause, SkipForward, Volume2, Headphones } from 'lucide-react'

interface VoiceSession {
  id: string
  title: string
  description: string
  duration: number
  category: string
  audioUrl?: string
  text: string
}

interface VoiceSessionPlayerProps {
  session: VoiceSession
  onSessionComplete?: () => void
  className?: string
}

export default function VoiceSessionPlayer({ session, onSessionComplete, className }: VoiceSessionPlayerProps) {
  const [isPlaying, setIsPlaying] = useState(false)
  const [currentTime, setCurrentTime] = useState(0)
  const [duration, setDuration] = useState(0)
  const [volume, setVolume] = useState(1)
  const [isLoading, setIsLoading] = useState(false)
  
  const audioRef = useRef<HTMLAudioElement | null>(null)
  const progressBarRef = useRef<HTMLDivElement | null>(null)

  // Initialize audio
  useEffect(() => {
    if (audioRef.current) {
      audioRef.current.volume = volume
      
      const updateTime = () => setCurrentTime(audioRef.current?.currentTime || 0)
      const updateDuration = () => setDuration(audioRef.current?.duration || 0)
      
      audioRef.current.addEventListener('timeupdate', updateTime)
      audioRef.current.addEventListener('loadedmetadata', updateDuration)
      audioRef.current.addEventListener('ended', handleSessionEnd)
      
      return () => {
        audioRef.current?.removeEventListener('timeupdate', updateTime)
        audioRef.current?.removeEventListener('loadedmetadata', updateDuration)
        audioRef.current?.removeEventListener('ended', handleSessionEnd)
      }
    }
  }, [volume])

  // Generate audio for session
  useEffect(() => {
    if (session.text && !session.audioUrl) {
      generateAudioForSession()
    }
  }, [session])

  const generateAudioForSession = async () => {
    setIsLoading(true)
    try {
      const response = await fetch('/api/tts', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          text: session.text,
          voice: 'calm-female',
          speed: 0.9
        })
      })

      const result = await response.json()
      
      if (result.audio) {
        const audioData = 'data:audio/mp3;base64,' + result.audio
        if (audioRef.current) {
          audioRef.current.src = audioData
        }
      }
    } catch (error) {
      console.error('Error generating audio:', error)
    } finally {
      setIsLoading(false)
    }
  }

  const togglePlayPause = () => {
    if (!audioRef.current) return

    if (isPlaying) {
      audioRef.current.pause()
    } else {
      audioRef.current.play()
    }
    setIsPlaying(!isPlaying)
  }

  const handleSessionEnd = () => {
    setIsPlaying(false)
    setCurrentTime(0)
    onSessionComplete?.()
  }

  const handleProgressClick = (e: React.MouseEvent<HTMLDivElement>) => {
    if (!audioRef.current || !progressBarRef.current) return

    const rect = progressBarRef.current.getBoundingClientRect()
    const clickX = e.clientX - rect.left
    const clickPercent = clickX / rect.width
    const newTime = clickPercent * duration

    audioRef.current.currentTime = newTime
    setCurrentTime(newTime)
  }

  const skipForward = () => {
    if (audioRef.current) {
      audioRef.current.currentTime = Math.min(currentTime + 15, duration)
    }
  }

  const handleVolumeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newVolume = parseFloat(e.target.value)
    setVolume(newVolume)
    if (audioRef.current) {
      audioRef.current.volume = newVolume
    }
  }

  const formatTime = (time: number) => {
    const minutes = Math.floor(time / 60)
    const seconds = Math.floor(time % 60)
    return `${minutes}:${seconds.toString().padStart(2, '0')}`
  }

  const progressPercent = duration > 0 ? (currentTime / duration) * 100 : 0

  return (
    <div className={`voice-session-player ${className}`}>
      <div className="glass-card rounded-3xl p-8 max-w-2xl mx-auto">
        {/* Session Header */}
        <div className="text-center mb-8">
          <div className="flex items-center justify-center mb-4">
            <Headphones className="w-8 h-8 text-accent-teal mr-3" />
            <h2 className="text-2xl font-bold">{session.title}</h2>
          </div>
          <p className="text-gray-300 mb-2">{session.description}</p>
          <div className="flex items-center justify-center gap-4 text-sm text-gray-400">
            <span className="px-3 py-1 bg-white/10 rounded-full">
              {session.category}
            </span>
            <span>{formatTime(session.duration)}</span>
          </div>
        </div>

        {/* Audio Player */}
        <div className="space-y-6">
          {/* Progress Bar */}
          <div className="space-y-2">
            <div 
              ref={progressBarRef}
              onClick={handleProgressClick}
              className="h-2 bg-gray-700 rounded-full cursor-pointer relative"
            >
              <motion.div
                className="h-full bg-gradient-to-r from-accent-teal to-accent-teal-light rounded-full"
                style={{ width: `${progressPercent}%` }}
                initial={{ width: 0 }}
                transition={{ duration: 0.3 }}
              />
            </div>
            <div className="flex justify-between text-sm text-gray-400">
              <span>{formatTime(currentTime)}</span>
              <span>{formatTime(duration)}</span>
            </div>
          </div>

          {/* Controls */}
          <div className="flex items-center justify-center gap-4">
            <button
              onClick={skipForward}
              className="p-3 glass-card rounded-full hover:bg-white/20 transition-colors"
              title="Skip forward 15 seconds"
            >
              <SkipForward className="w-5 h-5" />
            </button>
            
            <button
              onClick={togglePlayPause}
              disabled={isLoading}
              className="voice-button"
              title={isPlaying ? 'Pause' : 'Play'}
            >
              {isLoading ? (
                <div className="animate-spin">
                  <Volume2 className="w-8 h-8 text-white" />
                </div>
              ) : isPlaying ? (
                <Pause className="w-8 h-8 text-white" />
              ) : (
                <Play className="w-8 h-8 text-white ml-1" />
              )}
            </button>
            
            <div className="flex items-center gap-2">
              <Volume2 className="w-5 h-5 text-gray-400" />
              <input
                type="range"
                min="0"
                max="1"
                step="0.1"
                value={volume}
                onChange={handleVolumeChange}
                className="w-20 accent-accent-teal"
                title="Volume"
              />
            </div>
          </div>

          {/* Session Text Display */}
          <div className="transcript-container max-h-40 overflow-y-auto">
            <p className="text-sm leading-relaxed">{session.text}</p>
          </div>
        </div>

        {/* Hidden Audio Element */}
        <audio ref={audioRef} preload="metadata" />
      </div>
    </div>
  )
}
