'use client'

import { motion } from 'framer-motion'
import { useInView } from 'react-intersection-observer'
import { GraduationCap, Building2, Shield, CheckCircle } from 'lucide-react'

interface UseCase {
  icon: React.ReactNode
  title: string
  description: string
  benefits: string[]
}

interface AudienceSectionProps {
  useCases: UseCase[]
}

export default function AudienceSection({ useCases }: AudienceSectionProps) {
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
            Designed for
            <span className="text-gradient"> Every Journey</span>
          </h2>
          <p className="text-xl text-white/80 max-w-2xl mx-auto">
            Tailored solutions for different life situations and challenges
          </p>
        </motion.div>
        
        <div className="grid md:grid-cols-3 gap-8">
          {useCases.map((useCase, index) => (
            <motion.div
              key={index}
              className="glass-card glass-card-hover rounded-2xl p-8"
              initial={{ opacity: 0, y: 30 }}
              animate={inView ? { opacity: 1, y: 0 } : {}}
              transition={{ duration: 0.8, delay: index * 0.1 }}
              whileHover={{ scale: 1.05 }}
            >
              <div className="flex items-center mb-4">
                <div className="w-12 h-12 bg-gradient-to-r from-purple-500 to-blue-500 rounded-full flex items-center justify-center mr-4">
                  {useCase.icon}
                </div>
                <h3 className="text-2xl font-semibold text-white">{useCase.title}</h3>
              </div>
              <p className="text-white/70 mb-6">{useCase.description}</p>
              <ul className="space-y-2">
                {useCase.benefits.map((benefit, benefitIndex) => (
                  <li key={benefitIndex} className="flex items-center text-white/70">
                    <CheckCircle className="w-4 h-4 text-purple-400 mr-2 flex-shrink-0" />
                    {benefit}
                  </li>
                ))}
              </ul>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  )
}
