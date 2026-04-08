'use client'

import { motion } from 'framer-motion'
import { useInView } from 'react-intersection-observer'
import { Play, Music, ArrowRight } from 'lucide-react'

interface HeroSectionProps {
  onCTAClick: (action: string) => void
}

export default function HeroSection({ onCTAClick }: HeroSectionProps) {
  const [ref, inView] = useInView({ triggerOnce: true, threshold: 0.1 })

  return (
    <section ref={ref} className="section-padding relative overflow-hidden">
      <div className="absolute inset-0 hero-gradient opacity-50"></div>
      
      {/* Animated background particles */}
      <div className="absolute inset-0">
        {[...Array(20)].map((_, i) => (
          <motion.div
            key={i}
            className="absolute w-2 h-2 bg-white/20 rounded-full"
            style={{
              left: `${Math.random() * 100}%`,
              top: `${Math.random() * 100}%`,
            }}
            animate={{
              y: [0, -100, 0],
              opacity: [0, 1, 0],
            }}
            transition={{
              duration: 3 + Math.random() * 4,
              repeat: Infinity,
              delay: Math.random() * 2,
            }}
          />
        ))}
      </div>

      <div className="container mx-auto px-6 relative z-10">
        <motion.div 
          className="text-center max-w-4xl mx-auto"
          initial={{ opacity: 0, y: 30 }}
          animate={inView ? { opacity: 1, y: 0 } : {}}
          transition={{ duration: 0.8 }}
        >
          <motion.h1 
            className="text-5xl md:text-7xl font-bold mb-6 leading-tight"
            initial={{ opacity: 0, y: 30 }}
            animate={inView ? { opacity: 1, y: 0 } : {}}
            transition={{ duration: 0.8, delay: 0.2 }}
          >
            Feel calm, focused, and
            <br />
            <span className="text-gradient">stress-free in minutes</span>
          </motion.h1>
          
          <motion.p 
            className="text-xl md:text-2xl mb-8 text-white/80 max-w-2xl mx-auto"
            initial={{ opacity: 0, y: 30 }}
            animate={inView ? { opacity: 1, y: 0 } : {}}
            transition={{ duration: 0.8, delay: 0.4 }}
          >
            AI-powered mental wellness for students, professionals, and high-pressure roles
          </motion.p>
          
          <motion.div 
            className="flex flex-col sm:flex-row gap-4 justify-center items-center mb-12"
            initial={{ opacity: 0, y: 30 }}
            animate={inView ? { opacity: 1, y: 0 } : {}}
            transition={{ duration: 0.8, delay: 0.6 }}
          >
            <motion.button 
              onClick={() => onCTAClick('start-free')}
              className="pill-button pill-button-primary text-lg"
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
            >
              Start Free
              <ArrowRight className="ml-2 w-5 h-5" />
            </motion.button>
            <motion.button 
              onClick={() => onCTAClick('explore-audio')}
              className="pill-button pill-button-secondary text-lg"
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
            >
              <Play className="mr-2 w-5 h-5" />
              Explore Audio
            </motion.button>
          </motion.div>
          
          {/* Floating Phone Mockup */}
          <motion.div 
            className="relative max-w-md mx-auto animate-float"
            initial={{ opacity: 0, scale: 0.9 }}
            animate={inView ? { opacity: 1, scale: 1 } : {}}
            transition={{ duration: 0.8, delay: 0.8 }}
          >
            <div className="glass-card rounded-3xl p-6">
              <div className="flex items-center justify-between mb-4">
                <div className="flex items-center space-x-2">
                  <div className="w-8 h-8 bg-gradient-to-r from-purple-500 to-blue-500 rounded-full flex items-center justify-center">
                    <Music className="w-4 h-4 text-white" />
                  </div>
                  <div>
                    <div className="text-sm font-semibold">Sleep Therapy</div>
                    <div className="text-xs text-white/60">432Hz · 30 min</div>
                  </div>
                </div>
                <button className="w-12 h-12 bg-gradient-to-r from-purple-500 to-blue-500 rounded-full flex items-center justify-center">
                  <Play className="w-5 h-5 text-white ml-1" />
                </button>
              </div>
              
              <div className="space-y-3">
                <div className="h-2 bg-white/20 rounded-full overflow-hidden">
                  <div className="h-full w-3/4 bg-gradient-to-r from-purple-500 to-blue-500 rounded-full"></div>
                </div>
                <div className="flex justify-between text-xs text-white/60">
                  <span>22:45</span>
                  <span>30:00</span>
                </div>
              </div>
            </div>
            
            {/* Glow effect */}
            <div className="absolute inset-0 bg-gradient-to-r from-purple-500 to-blue-500 rounded-3xl blur-3xl opacity-20 -z-10" />
          </motion.div>
        </motion.div>
      </div>
    </section>
  )
}
