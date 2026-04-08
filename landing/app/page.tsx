'use client'

import { useState, useEffect } from 'react'
import { motion } from 'framer-motion'
import { 
  Play, 
  Music, 
  Brain, 
  BarChart3, 
  Moon, 
  GraduationCap, 
  Building2, 
  Shield, 
  Download,
  ArrowRight,
  Star,
  CheckCircle,
  Headphones,
  Smartphone,
  Monitor
} from 'lucide-react'

// Import reusable components
import HeroSection from '../components/HeroSection'
import FeaturesSection from '../components/FeaturesSection'
import TestimonialsSection from '../components/TestimonialsSection'
import AudienceSection from '../components/AudienceSection'
import ShowcaseSection from '../components/ShowcaseSection'
import CTASection from '../components/CTASection'
import Footer from '../components/Footer'

export default function LandingPage() {
  const [isClient, setIsClient] = useState(false)
  
  useEffect(() => {
    setIsClient(true)
  }, [])

  // Data for components
  const features = [
    {
      icon: <Headphones />,
      title: 'Audio Therapy',
      description: 'Relaxing sounds including rain, river, 432Hz frequency, and sleep music'
    },
    {
      icon: <Brain />,
      title: 'AI-Guided Sessions',
      description: 'Personalized mental wellness sessions powered by artificial intelligence'
    },
    {
      icon: <BarChart3 />,
      title: 'Mental Health Insights',
      description: 'Track your progress with detailed analytics and personalized recommendations'
    },
    {
      icon: <Moon />,
      title: 'Sleep Mode',
      description: 'Dedicated sleep programs to improve your rest and recovery'
    }
  ]

  const useCases = [
    {
      icon: <GraduationCap />,
      title: 'Students',
      description: 'Exam anxiety relief and focus improvement for academic success',
      benefits: ['Reduce exam stress', 'Improve concentration', 'Better study habits']
    },
    {
      icon: <Building2 />,
      title: 'Corporate',
      description: 'Burnout reduction and productivity enhancement for professionals',
      benefits: ['Prevent burnout', 'Boost productivity', 'Work-life balance']
    },
    {
      icon: <Shield />,
      title: 'Police/Military',
      description: 'Stress recovery and sleep support for high-pressure roles',
      benefits: ['Stress recovery', 'Better sleep', 'Mental resilience']
    }
  ]

  const testimonials = [
    {
      name: 'Sarah Chen',
      role: 'Graduate Student',
      content: 'DrMindit helped me manage exam anxiety and improve my focus. The audio sessions are incredibly calming.',
      rating: 5
    },
    {
      name: 'Michael Rodriguez',
      role: 'Software Engineer',
      content: 'As someone working in a high-pressure environment, this app has been a game-changer for my mental wellness.',
      rating: 5
    },
    {
      name: 'James Wilson',
      role: 'Police Officer',
      content: 'The sleep programs have significantly improved my rest. I feel more alert and resilient during my shifts.',
      rating: 5
    }
  ]

  const handleCTAClick = (action: string) => {
    // Analytics tracking would go here
    console.log(`CTA clicked: ${action}`)
    
    // Handle navigation based on action
    switch (action) {
      case 'start-free':
        window.location.href = '/signup'
        break
      case 'explore-audio':
        document.getElementById('features')?.scrollIntoView({ behavior: 'smooth' })
        break
      case 'download':
        window.location.href = '/download'
        break
      case 'try-free':
        window.location.href = '/signup'
        break
      default:
        console.log('Unknown action:', action)
    }
  }

  if (!isClient) {
    return null // Prevent hydration issues
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-600 via-purple-600 to-pink-600 text-white">
      <HeroSection onCTAClick={handleCTAClick} />
      <FeaturesSection features={features} />
      <AudienceSection useCases={useCases} />
      <ShowcaseSection />
      <TestimonialsSection testimonials={testimonials} />
      <CTASection onCTAClick={handleCTAClick} />
      <Footer />
    </div>
  )
}
