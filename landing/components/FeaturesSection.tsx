'use client'

import { motion } from 'framer-motion'
import { useInView } from 'react-intersection-observer'
import { Headphones, Brain, BarChart3, Moon } from 'lucide-react'

interface Feature {
  icon: React.ReactNode
  title: string
  description: string
}

interface FeaturesSectionProps {
  features: Feature[]
}

export default function FeaturesSection({ features }: FeaturesSectionProps) {
  const [ref, inView] = useInView({ triggerOnce: true, threshold: 0.1 })

  return (
    <section ref={ref} className="section-padding">
      <div className="container mx-auto px-6">
        <motion.div 
          className="text-center mb-16"
          initial={{ opacity: 0, y: 30 }}
          animate={inView ? { opacity: 1, y: 0 } : {}}
          transition={{ duration: 0.8 }}
        >
          <h2 className="text-4xl md:text-5xl font-bold mb-4">
            Everything You Need for
            <span className="text-gradient"> Mental Wellness</span>
          </h2>
          <p className="text-xl text-white/80 max-w-2xl mx-auto">
            Comprehensive tools designed to help you achieve optimal mental health
          </p>
        </motion.div>
        
        <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-8">
          {features.map((feature, index) => (
            <motion.div
              key={index}
              className="glass-card glass-card-hover rounded-2xl p-8 text-center"
              initial={{ opacity: 0, y: 30 }}
              animate={inView ? { opacity: 1, y: 0 } : {}}
              transition={{ duration: 0.8, delay: index * 0.1 }}
              whileHover={{ scale: 1.05 }}
            >
              <div className="feature-icon mx-auto">
                {feature.icon}
              </div>
              <h3 className="text-xl font-semibold mb-3 text-white">{feature.title}</h3>
              <p className="text-white/70">{feature.description}</p>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  )
}
