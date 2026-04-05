// Rate limiting for voice assistant API calls
interface RateLimitEntry {
  count: number
  resetTime: number
}

class RateLimiter {
  private static instance: RateLimiter
  private userLimits = new Map<string, RateLimitEntry>()
  
  // Rate limits: 10 requests per minute, 100 per hour
  private readonly LIMITS = {
    perMinute: 10,
    perHour: 100,
    minuteWindow: 60 * 1000, // 1 minute in ms
    hourWindow: 60 * 60 * 1000 // 1 hour in ms
  }

  static getInstance(): RateLimiter {
    if (!RateLimiter.instance) {
      RateLimiter.instance = new RateLimiter()
    }
    return RateLimiter.instance
  }

  isAllowed(userId: string): { allowed: boolean; resetTime?: number } {
    const now = Date.now()
    const existing = this.userLimits.get(userId)

    if (!existing) {
      this.userLimits.set(userId, {
        count: 1,
        resetTime: now + this.LIMITS.minuteWindow
      })
      return { allowed: true }
    }

    // Check if window has expired
    if (now > existing.resetTime) {
      this.userLimits.set(userId, {
        count: 1,
        resetTime: now + this.LIMITS.minuteWindow
      })
      return { allowed: true }
    }

    // Check if under limit
    if (existing.count < this.LIMITS.perMinute) {
      existing.count++
      return { allowed: true }
    }

    return { 
      allowed: false, 
      resetTime: existing.resetTime 
    }
  }

  // Cleanup old entries periodically
  cleanup() {
    const now = Date.now()
    for (const [userId, entry] of this.userLimits.entries()) {
      if (now > entry.resetTime) {
        this.userLimits.delete(userId)
      }
    }
  }
}

export default RateLimiter
