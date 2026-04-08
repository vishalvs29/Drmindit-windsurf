'use client'

import { motion } from 'framer-motion'
import { useInView } from 'react-intersection-observer'
import { Smartphone, Monitor, Headphones } from 'lucide-react'

interface ShowcaseItem {
  device: string
  title: string
  icon: React.ReactNode
  description?: string
}

interface ShowcaseSectionProps {
  showcaseItems?: ShowcaseItem[]
}

export default function ShowcaseSection({ showcaseItems }: ShowcaseSectionProps) {
  const [ref, inView] = useInView({ triggerOnce: true, threshold: 0.1 })

  const defaultItems: ShowcaseItem[] = [
    { 
      device: 'mobile', 
      title: 'Audio Player', 
      icon: <Smartphone />,
      description: 'Immersive soundscapes and guided sessions'
    },
    { 
      device: 'tablet', 
      title: 'AI Session', 
      icon: <Monitor />,
      description: 'Intelligent conversations for mental wellness'
    },
    { 
      device: 'desktop', 
      title: 'Progress Dashboard', 
      icon: <Headphones />,
      description: 'Track your journey to better mental health'
    }
  ]

  const items = showcaseItems || defaultItems

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
            See DrMindit
            <span className="text-gradient"> in Action</span>
          </h2>
          <p className="text-xl text-white/80 max-w-2xl mx-auto">
            Beautiful user experience that makes mental wellness accessible and enjoyable
          </p>
        </motion.div>
        
        <div className="grid md:grid-cols-3 gap-8">
          {items.map((item, index) => (
            <motion.div
              key={index}
              className="text-center"
              initial={{ opacity: 0, scale: 0.9 }}
              animate={inView ? { opacity: 1, scale: 1 } : {}}
              transition={{ duration: 0.8, delay: index * 0.2 }}
              whileHover={{ scale: 1.05 }}
            >
              <div className="glass-card rounded-3xl p-8 mb-4 h-64 flex items-center justify-center">
                <div className="text-6xl bg-gradient-to-r from-purple-500 to-blue-500 bg-clip-text text-transparent">
                  {item.icon}
                </div>
              </div>
              <h3 className="text-xl font-semibold text-white mb-2">{item.title}</h3>
              {item.description && (
                <p className="text-white/70">{item.description}</p>
              )}
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  )
}
