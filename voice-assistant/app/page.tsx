'use client'

import { useState, useEffect } from 'react'
import { motion } from 'framer-motion'
import { 
  Mic, 
  Headphones, 
  PlayCircle, 
  Shield, 
  Phone, 
  Volume2,
  AlertCircle,
  CheckCircle,
  Clock,
  Users
} from 'lucide-react'
import VoiceAssistant from '../components/VoiceAssistant'
import VoiceSessionPlayer from '../components/VoiceSessionPlayer'

interface VoiceSession {
  id: string
  title: string
  description: string
  duration: number
  category: string
  text: string
}

export default function VoiceAssistantPage() {
  const [activeTab, setActiveTab] = useState<'chat' | 'sessions'>('chat')
  const [sessions, setSessions] = useState<VoiceSession[]>([])
  const [selectedSession, setSelectedSession] = useState<VoiceSession | null>(null)
  const [transcript, setTranscript] = useState('')
  const [aiResponse, setAiResponse] = useState('')
  const [isLoading, setIsLoading] = useState(true)

  // Load predefined sessions
  useEffect(() => {
    loadSessions()
  }, [])

  const loadSessions = async () => {
    try {
      const response = await fetch('/api/tts')
      const result = await response.json()
      
      if (result.scripts) {
        const allSessions = [
          ...result.scripts.quickCalm,
          ...result.scripts.guidedSessions
        ]
        setSessions(allSessions)
      }
    } catch (error) {
      console.error('Error loading sessions:', error)
    } finally {
      setIsLoading(false)
    }
  }

  const handleSessionSelect = (session: VoiceSession) => {
    setSelectedSession(session)
    setActiveTab('sessions')
  }

  const handleTranscript = (text: string) => {
    setTranscript(text)
  }

  const handleAIResponse = (response: string) => {
    setAiResponse(response)
  }

  const handleSessionComplete = () => {
    setSelectedSession(null)
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-deep-blue via-purple to-deep-blue text-white">
      
      {/* Header */}
      <header className="section-padding">
        <div className="container mx-auto px-6">
          <motion.div 
            className="text-center"
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8 }}
          >
            <h1 className="text-4xl md:text-6xl font-bold mb-4">
              AI Voice
              <span className="text-gradient"> Assistant</span>
            </h1>
            <p className="text-xl text-gray-300 max-w-2xl mx-auto">
              Experience natural, calming AI voice guidance for mental wellness
            </p>
          </motion.div>
        </div>
      </header>

      {/* Tab Navigation */}
      <section className="container mx-auto px-6">
        <div className="flex justify-center mb-8">
          <div className="glass-card rounded-full p-1 inline-flex">
            <button
              onClick={() => setActiveTab('chat')}
              className={`px-6 py-3 rounded-full transition-all duration-300 ${
                activeTab === 'chat' 
                  ? 'bg-accent-teal text-white' 
                  : 'text-gray-300 hover:text-white'
              }`}
            >
              <Mic className="w-4 h-4 mr-2" />
              Voice Chat
            </button>
            <button
              onClick={() => setActiveTab('sessions')}
              className={`px-6 py-3 rounded-full transition-all duration-300 ${
                activeTab === 'sessions' 
                  ? 'bg-accent-teal text-white' 
                  : 'text-gray-300 hover:text-white'
              }`}
            >
              <Headphones className="w-4 h-4 mr-2" />
              Guided Sessions
            </button>
          </div>
        </div>
      </section>

      {/* Main Content */}
      <main className="section-padding">
        <div className="container mx-auto px-6">
          
          {/* Voice Chat Tab */}
          {activeTab === 'chat' && (
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.5 }}
            >
              <VoiceAssistant 
                onTranscript={handleTranscript}
                onAIResponse={handleAIResponse}
              />
              
              {/* Recent Conversation */}
              {(transcript || aiResponse) && (
                <motion.div
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.5, delay: 0.3 }}
                  className="mt-8 max-w-2xl mx-auto"
                >
                  <div className="glass-card rounded-2xl p-6">
                    <h3 className="text-lg font-semibold mb-4 text-gradient">Recent Conversation</h3>
                    
                    {transcript && (
                      <div className="mb-4">
                        <div className="flex items-center mb-2">
                          <Users className="w-4 h-4 text-accent-teal mr-2" />
                          <span className="text-sm font-medium">You:</span>
                        </div>
                        <p className="text-gray-300">{transcript}</p>
                      </div>
                    )}
                    
                    {aiResponse && (
                      <div>
                        <div className="flex items-center mb-2">
                          <Volume2 className="w-4 h-4 text-green-400 mr-2" />
                          <span className="text-sm font-medium">AI Assistant:</span>
                        </div>
                        <p className="text-gray-300">{aiResponse}</p>
                      </div>
                    )}
                  </div>
                </motion.div>
              )}
            </motion.div>
          )}

          {/* Guided Sessions Tab */}
          {activeTab === 'sessions' && (
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.5 }}
            >
              {selectedSession ? (
                <VoiceSessionPlayer
                  session={selectedSession}
                  onSessionComplete={handleSessionComplete}
                />
              ) : (
                <div>
                  <h2 className="text-3xl font-bold text-center mb-8">
                    Choose a
                    <span className="text-gradient"> Guided Session</span>
                  </h2>
                  
                  {isLoading ? (
                    <div className="text-center py-12">
                      <div className="animate-spin w-8 h-8 border-2 border-accent-teal border-t-transparent rounded-full mx-auto mb-4"></div>
                      <p className="text-gray-300">Loading sessions...</p>
                    </div>
                  ) : (
                    <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
                      {sessions.map((session, index) => (
                        <motion.div
                          key={session.id}
                          initial={{ opacity: 0, y: 30 }}
                          animate={{ opacity: 1, y: 0 }}
                          transition={{ duration: 0.5, delay: index * 0.1 }}
                          onClick={() => handleSessionSelect(session)}
                          className="session-card cursor-pointer"
                        >
                          <div className="flex items-center mb-3">
                            <div className="w-12 h-12 bg-accent-teal rounded-full flex items-center justify-center mr-3">
                              <PlayCircle className="w-6 h-6 text-white" />
                            </div>
                            <div>
                              <h3 className="font-semibold">{session.title}</h3>
                              <div className="flex items-center text-sm text-gray-400">
                                <Clock className="w-3 h-3 mr-1" />
                                {Math.floor(session.duration / 60)}:{(session.duration % 60).toString().padStart(2, '0')}
                              </div>
                            </div>
                          </div>
                          
                          <p className="text-gray-300 mb-4">{session.description}</p>
                          
                          <div className="flex items-center justify-between">
                            <span className="px-3 py-1 bg-white/10 rounded-full text-sm">
                              {session.category}
                            </span>
                            <button className="text-accent-teal hover:text-accent-teal-light transition-colors">
                              Start Session →
                            </button>
                          </div>
                        </motion.div>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </motion.div>
          )}
        </div>
      </main>

      {/* Features Section */}
      <section className="section-padding">
        <div className="container mx-auto px-6">
          <motion.div 
            className="text-center mb-12"
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8 }}
          >
            <h2 className="text-3xl md:text-4xl font-bold mb-4">
              Voice-Powered
              <span className="text-gradient"> Mental Wellness</span>
            </h2>
            <p className="text-xl text-gray-300 max-w-2xl mx-auto">
              Advanced AI technology for natural, calming voice interactions
            </p>
          </motion.div>
          
          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-8">
            {[
              {
                icon: <Mic />,
                title: 'Voice Recognition',
                description: 'Advanced speech-to-text technology accurately understands your needs'
              },
              {
                icon: <Volume2 />,
                title: 'Natural Voice',
                description: 'Calming AI-generated voice with multiple voice options'
              },
              {
                icon: <Shield />,
                title: 'Safety First',
                description: 'Built-in distress detection and emergency resource connections'
              },
              {
                icon: <Phone />,
                title: 'Quick Calm',
                description: 'One-click instant stress relief with calming guidance'
              }
            ].map((feature, index) => (
              <motion.div
                key={index}
                className="glass-card glass-card-hover rounded-2xl p-6 text-center"
                initial={{ opacity: 0, y: 30 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.8, delay: index * 0.1 }}
              >
                <div className="w-16 h-16 bg-accent-teal rounded-2xl flex items-center justify-center mx-auto mb-4">
                  {feature.icon}
                </div>
                <h3 className="text-xl font-semibold mb-3">{feature.title}</h3>
                <p className="text-gray-300">{feature.description}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Safety Notice */}
      <section className="section-padding">
        <div className="container mx-auto px-6">
          <motion.div
            className="safety-disclaimer max-w-4xl mx-auto"
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8, delay: 0.5 }}
          >
            <div className="flex items-start">
              <AlertCircle className="w-6 h-6 text-yellow-400 mr-3 mt-1 flex-shrink-0" />
              <div>
                <h3 className="text-lg font-semibold mb-2">Important Safety Notice</h3>
                <p className="text-sm leading-relaxed">
                  This AI voice assistant is designed to support mental wellness but is not a substitute for professional mental health care. 
                  If you're experiencing a mental health crisis, having thoughts of self-harm, or need immediate support, 
                  please contact a healthcare professional or crisis hotline immediately.
                </p>
                <div className="mt-4 space-y-2 text-sm">
                  <div><strong>988 Suicide & Crisis Lifeline:</strong> Call or text 988 (24/7)</div>
                  <div><strong>Crisis Text Line:</strong> Text HOME to 741741</div>
                  <div><strong>National Suicide Prevention Lifeline:</strong> 1-800-273-8255</div>
                </div>
              </div>
            </div>
          </motion.div>
        </div>
      </section>
    </div>
  )
}
