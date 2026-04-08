'use client'

import { motion } from 'framer-motion'
import { useInView } from 'react-intersection-observer'
import { Download, ArrowRight } from 'lucide-react'

interface CTASectionProps {
  onCTAClick: (action: string) => void
}

export default function CTASection({ onCTAClick }: CTASectionProps) {
  const [ref, inView] = useInView({ triggerOnce: true, threshold: 0.1 })

  return (
    <section ref={ref} className="section-padding relative">
      <div className="absolute inset-0 bg-gradient-to-r from-purple-600 to-blue-600 opacity-80"></div>
      
      <div className="container mx-auto px-6 relative z-10">
        <motion.div 
          className="glass-card rounded-3xl p-12 text-center max-w-4xl mx-auto"
          initial={{ opacity: 0, y: 30 }}
          animate={inView ? { opacity: 1, y: 0 } : {}}
          transition={{ duration: 0.8 }}
        >
          <h2 className="text-4xl md:text-5xl font-bold mb-4">
            Start Your
            <span className="text-gradient"> Mental Wellness Journey</span>
            <br />
            Today
          </h2>
          <p className="text-xl text-white/80 mb-8 max-w-2xl mx-auto">
            Join thousands who have found peace, focus, and better sleep with DrMindit
          </p>
          
          <motion.div 
            className="flex flex-col sm:flex-row gap-4 justify-center"
            initial={{ opacity: 0, y: 30 }}
            animate={inView ? { opacity: 1, y: 0 } : {}}
            transition={{ duration: 0.8, delay: 0.2 }}
          >
            <motion.button 
              onClick={() => onCTAClick('download')}
              className="pill-button pill-button-primary text-lg"
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
            >
              <Download className="mr-2 w-5 h-5" />
              Download App
            </motion.button>
            <motion.button 
              onClick={() => onCTAClick('try-free')}
              className="pill-button pill-button-secondary text-lg"
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
            >
              Try Free
              <ArrowRight className="ml-2 w-5 h-5" />
            </motion.button>
          </motion.div>
        </motion.div>
      </div>
    </section>
  )
}
