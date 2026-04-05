// Conversation history management for voice assistant
interface ChatMessage {
  id: string
  message: string
  response: string
  timestamp: number
  distressDetected?: boolean
  context?: any
}

interface ConversationSession {
  id: string
  userId: string
  messages: ChatMessage[]
  createdAt: number
  lastActive: number
}

class ConversationHistory {
  private static instance: ConversationHistory
  private sessions = new Map<string, ConversationSession>()
  
  // Keep sessions for 24 hours
  private readonly SESSION_TTL = 24 * 60 * 60 * 1000

  static getInstance(): ConversationHistory {
    if (!ConversationHistory.instance) {
      ConversationHistory.instance = new ConversationHistory()
    }
    return ConversationHistory.instance
  }

  addMessage(sessionId: string, userId: string, message: string, response: string, distressDetected?: boolean, context?: any): void {
    let session = this.sessions.get(sessionId)
    
    if (!session) {
      session = {
        id: sessionId,
        userId,
        messages: [],
        createdAt: Date.now(),
        lastActive: Date.now()
      }
      this.sessions.set(sessionId, session)
    }

    const chatMessage: ChatMessage = {
      id: `${sessionId}-${Date.now()}`,
      message,
      response,
      timestamp: Date.now(),
      distressDetected,
      context
    }

    session.messages.push(chatMessage)
    session.lastActive = Date.now()

    // Keep only last 10 messages per session to manage memory
    if (session.messages.length > 10) {
      session.messages = session.messages.slice(-10)
    }
  }

  getSession(sessionId: string): ConversationSession | null {
    const session = this.sessions.get(sessionId)
    if (!session) return null

    // Check if session has expired
    if (Date.now() - session.lastActive > this.SESSION_TTL) {
      this.sessions.delete(sessionId)
      return null
    }

    return session
  }

  getRecentMessages(sessionId: string, limit: number = 5): ChatMessage[] {
    const session = this.getSession(sessionId)
    if (!session) return []

    return session.messages.slice(-limit)
  }

  getContextSummary(sessionId: string): string {
    const recentMessages = this.getRecentMessages(sessionId, 3)
    if (recentMessages.length === 0) return ""

    const topics = recentMessages
      .map(msg => msg.message.toLowerCase())
      .join(' ')

    // Extract key themes from recent conversation
    const themes = []
    if (topics.includes('stress') || topics.includes('anxious')) themes.push('stress/anxiety')
    if (topics.includes('sleep') || topics.includes('tired')) themes.push('sleep issues')
    if (topics.includes('work') || topics.includes('job')) themes.push('work-related')
    if (topics.includes('relationship') || topics.includes('family')) themes.push('relationships')

    return themes.length > 0 ? `Recent topics: ${themes.join(', ')}` : ""
  }

  // Cleanup expired sessions
  cleanup() {
    const now = Date.now()
    for (const [sessionId, session] of this.sessions.entries()) {
      if (now - session.lastActive > this.SESSION_TTL) {
        this.sessions.delete(sessionId)
      }
    }
  }

  getUserSessions(userId: string): ConversationSession[] {
    const sessions: ConversationSession[] = []
    for (const session of this.sessions.values()) {
      if (session.userId === userId) {
        sessions.push(session)
      }
    }
    return sessions.sort((a, b) => b.lastActive - a.lastActive)
  }
}

export default ConversationHistory
