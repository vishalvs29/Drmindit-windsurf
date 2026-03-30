'use client'

import { useState, useEffect } from 'react'
import { useInView } from 'react-intersection-observer'
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

export default function LandingPage() {
  const [isClient, setIsClient] = useState(false)
  
  useEffect(() => {
    setIsClient(true)
  }, [])

  // Intersection Observer hooks for animations
  const [heroRef, heroInView] = useInView({ triggerOnce: true, threshold: 0.1 })
  const [featuresRef, featuresInView] = useInView({ triggerOnce: true, threshold: 0.1 })
  const [useCasesRef, useCasesInView] = useInView({ triggerOnce: true, threshold: 0.1 })
  const [showcaseRef, showcaseInView] = useInView({ triggerOnce: true, threshold: 0.1 })
  const [howItWorksRef, howItWorksInView] = useInView({ triggerOnce: true, threshold: 0.1 })
  const [benefitsRef, benefitsInView] = useInView({ triggerOnce: true, threshold: 0.1 })
  const [testimonialsRef, testimonialsInView] = useInView({ triggerOnce: true, threshold: 0.1 })
  const [ctaRef, ctaInView] = useInView({ triggerOnce: true, threshold: 0.1 })

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

  const howItWorks = [
    {
      step: 1,
      title: 'Choose Your Goal',
      description: 'Select from stress relief, sleep improvement, focus enhancement, or anxiety reduction'
    },
    {
      step: 2,
      title: 'Listen & Learn',
      description: 'Engage with audio sessions and AI-guided exercises tailored to your needs'
    },
    {
      step: 3,
      title: 'Track Progress',
      description: 'Monitor your mental wellness journey with insights and recommendations'
    }
  ]

  const benefits = [
    'Better sleep quality',
    'Reduced anxiety levels',
    'Improved focus and concentration',
    'Daily calm and relaxation',
    'Enhanced mental clarity',
    'Stress resilience'
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

  const scrollToSection = (sectionId: string) => {
    const element = document.getElementById(sectionId)
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' })
    }
  }

  const handleCTAClick = (action: string) => {
    // Analytics tracking would go here
    console.log(`CTA clicked: ${action}`)
    // For now, just scroll to top
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  if (!isClient) {
    return null // Prevent hydration issues
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-deep-blue via-purple to-deep-blue text-white">
      
      {/* Hero Section */}
      <section ref={heroRef} className="section-padding relative overflow-hidden">
        <div className="absolute inset-0 hero-gradient opacity-50"></div>
        <div className="container mx-auto px-6 relative z-10">
          <motion.div 
            className="text-center max-w-4xl mx-auto"
            initial={{ opacity: 0, y: 30 }}
            animate={heroInView ? { opacity: 1, y: 0 } : {}}
            transition={{ duration: 0.8 }}
          >
            <motion.h1 
              className="text-5xl md:text-7xl font-bold mb-6 leading-tight"
              initial={{ opacity: 0, y: 30 }}
              animate={heroInView ? { opacity: 1, y: 0 } : {}}
              transition={{ duration: 0.8, delay: 0.2 }}
            >
              Feel calm, focused, and
              <br />
              <span className="text-gradient">stress-free in minutes</span>
            </motion.h1>
            
            <motion.p 
              className="text-xl md:text-2xl mb-8 text-gray-300 max-w-2xl mx-auto"
              initial={{ opacity: 0, y: 30 }}
              animate={heroInView ? { opacity: 1, y: 0 } : {}}
              transition={{ duration: 0.8, delay: 0.4 }}
            >
              AI-powered mental wellness for students, professionals, and high-pressure roles
            </motion.p>
            
            <motion.div 
              className="flex flex-col sm:flex-row gap-4 justify-center items-center mb-12"
              initial={{ opacity: 0, y: 30 }}
              animate={heroInView ? { opacity: 1, y: 0 } : {}}
              transition={{ duration: 0.8, delay: 0.6 }}
            >
              <button 
                onClick={() => handleCTAClick('start-free')}
                className="pill-button pill-button-primary text-lg"
              >
                Start Free
                <ArrowRight className="ml-2 w-5 h-5" />
              </button>
              <button 
                onClick={() => handleCTAClick('explore-audio')}
                className="pill-button pill-button-secondary text-lg"
              >
                <Play className="mr-2 w-5 h-5" />
                Explore Audio
              </button>
            </motion.div>
            
            {/* App Mockup */}
            <motion.div 
              className="relative max-w-md mx-auto animate-float"
              initial={{ opacity: 0, scale: 0.9 }}
              animate={heroInView ? { opacity: 1, scale: 1 } : {}}
              transition={{ duration: 0.8, delay: 0.8 }}
            >
              <div className="glass-card rounded-3xl p-6">
                <div className="flex items-center justify-between mb-4">
                  <div className="flex items-center space-x-2">
                    <div className="w-8 h-8 bg-accent-teal rounded-full flex items-center justify-center">
                      <Music className="w-4 h-4 text-white" />
                    </div>
                    <div>
                      <div className="text-sm font-semibold">Sleep Therapy</div>
                      <div className="text-xs text-gray-400">432Hz • 30 min</div>
                    </div>
                  </div>
                  <button className="w-12 h-12 bg-accent-teal rounded-full flex items-center justify-center">
                    <Play className="w-5 h-5 text-white ml-1" />
                  </button>
                </div>
                
                <div className="space-y-3">
                  <div className="h-2 bg-gray-700 rounded-full overflow-hidden">
                    <div className="h-full w-3/4 bg-gradient-to-r from-accent-teal to-accent-teal-light rounded-full"></div>
                  </div>
                  <div className="flex justify-between text-xs text-gray-400">
                    <span>22:45</span>
                    <span>30:00</span>
                  </div>
                </div>
              </div>
            </motion.div>
          </motion.div>
        </div>
      </section>

      {/* Features Section */}
      <section ref={featuresRef} className="section-padding">
        <div className="container mx-auto px-6">
          <motion.div 
            className="text-center mb-16"
            initial={{ opacity: 0, y: 30 }}
            animate={featuresInView ? { opacity: 1, y: 0 } : {}}
            transition={{ duration: 0.8 }}
          >
            <h2 className="text-4xl md:text-5xl font-bold mb-4">
              Everything You Need for
              <span className="text-gradient"> Mental Wellness</span>
            </h2>
            <p className="text-xl text-gray-300 max-w-2xl mx-auto">
              Comprehensive tools designed to help you achieve optimal mental health
            </p>
          </motion.div>
          
          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-8">
            {features.map((feature, index) => (
              <motion.div
                key={index}
                className="glass-card glass-card-hover rounded-2xl p-8 text-center"
                initial={{ opacity: 0, y: 30 }}
                animate={featuresInView ? { opacity: 1, y: 0 } : {}}
                transition={{ duration: 0.8, delay: index * 0.1 }}
              >
                <div className="feature-icon mx-auto">
                  {feature.icon}
                </div>
                <h3 className="text-xl font-semibold mb-3">{feature.title}</h3>
                <p className="text-gray-300">{feature.description}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Use Cases Section */}
      <section ref={useCasesRef} className="section-padding">
        <div className="container mx-auto px-6">
          <motion.div 
            className="text-center mb-16"
            initial={{ opacity: 0, y: 30 }}
            animate={useCasesInView ? { opacity: 1, y: 0 } : {}}
            transition={{ duration: 0.8 }}
          >
            <h2 className="text-4xl md:text-5xl font-bold mb-4">
              Designed for
              <span className="text-gradient"> Every Journey</span>
            </h2>
            <p className="text-xl text-gray-300 max-w-2xl mx-auto">
              Tailored solutions for different life situations and challenges
            </p>
          </motion.div>
          
          <div className="grid md:grid-cols-3 gap-8">
            {useCases.map((useCase, index) => (
              <motion.div
                key={index}
                className="glass-card glass-card-hover rounded-2xl p-8"
                initial={{ opacity: 0, y: 30 }}
                animate={useCasesInView ? { opacity: 1, y: 0 } : {}}
                transition={{ duration: 0.8, delay: index * 0.1 }}
              >
                <div className="flex items-center mb-4">
                  <div className="w-12 h-12 bg-accent-teal rounded-full flex items-center justify-center mr-4">
                    {useCase.icon}
                  </div>
                  <h3 className="text-2xl font-semibold">{useCase.title}</h3>
                </div>
                <p className="text-gray-300 mb-6">{useCase.description}</p>
                <ul className="space-y-2">
                  {useCase.benefits.map((benefit, benefitIndex) => (
                    <li key={benefitIndex} className="flex items-center text-gray-300">
                      <CheckCircle className="w-4 h-4 text-accent-teal mr-2 flex-shrink-0" />
                      {benefit}
                    </li>
                  ))}
                </ul>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Product Showcase */}
      <section ref={showcaseRef} className="section-padding">
        <div className="container mx-auto px-6">
          <motion.div 
            className="text-center mb-16"
            initial={{ opacity: 0, y: 30 }}
            animate={showcaseInView ? { opacity: 1, y: 0 } : {}}
            transition={{ duration: 0.8 }}
          >
            <h2 className="text-4xl md:text-5xl font-bold mb-4">
              Beautiful
              <span className="text-gradient"> User Experience</span>
            </h2>
            <p className="text-xl text-gray-300 max-w-2xl mx-auto">
              Intuitive design that makes mental wellness accessible and enjoyable
            </p>
          </motion.div>
          
          <div className="grid md:grid-cols-3 gap-8">
            {[
              { device: 'mobile', title: 'Audio Player', icon: <Smartphone /> },
              { device: 'tablet', title: 'Session Screen', icon: <Monitor /> },
              { device: 'desktop', title: 'Dashboard', icon: <Monitor /> }
            ].map((item, index) => (
              <motion.div
                key={index}
                className="text-center"
                initial={{ opacity: 0, scale: 0.9 }}
                animate={showcaseInView ? { opacity: 1, scale: 1 } : {}}
                transition={{ duration: 0.8, delay: index * 0.2 }}
              >
                <div className="glass-card rounded-3xl p-8 mb-4 h-64 flex items-center justify-center">
                  <div className="text-6xl text-accent-teal">
                    {item.icon}
                  </div>
                </div>
                <h3 className="text-xl font-semibold">{item.title}</h3>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* How It Works */}
      <section ref={howItWorksRef} className="section-padding">
        <div className="container mx-auto px-6">
          <motion.div 
            className="text-center mb-16"
            initial={{ opacity: 0, y: 30 }}
            animate={howItWorksInView ? { opacity: 1, y: 0 } : {}}
            transition={{ duration: 0.8 }}
          >
            <h2 className="text-4xl md:text-5xl font-bold mb-4">
              How
              <span className="text-gradient"> DrMindit Works</span>
            </h2>
            <p className="text-xl text-gray-300 max-w-2xl mx-auto">
              Simple three-step process to achieve mental wellness
            </p>
          </motion.div>
          
          <div className="grid md:grid-cols-3 gap-8">
            {howItWorks.map((step, index) => (
              <motion.div
                key={index}
                className="text-center"
                initial={{ opacity: 0, y: 30 }}
                animate={howItWorksInView ? { opacity: 1, y: 0 } : {}}
                transition={{ duration: 0.8, delay: index * 0.2 }}
              >
                <div className="w-16 h-16 bg-accent-teal rounded-full flex items-center justify-center text-2xl font-bold mx-auto mb-4">
                  {step.step}
                </div>
                <h3 className="text-2xl font-semibold mb-3">{step.title}</h3>
                <p className="text-gray-300">{step.description}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Benefits Section */}
      <section ref={benefitsRef} className="section-padding">
        <div className="container mx-auto px-6">
          <motion.div 
            className="text-center mb-16"
            initial={{ opacity: 0, y: 30 }}
            animate={benefitsInView ? { opacity: 1, y: 0 } : {}}
            transition={{ duration: 0.8 }}
          >
            <h2 className="text-4xl md:text-5xl font-bold mb-4">
              Transform Your
              <span className="text-gradient"> Mental Health</span>
            </h2>
            <p className="text-xl text-gray-300 max-w-2xl mx-auto">
              Experience the benefits of consistent mental wellness practice
            </p>
          </motion.div>
          
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6 max-w-4xl mx-auto">
            {benefits.map((benefit, index) => (
              <motion.div
                key={index}
                className="flex items-center space-x-3"
                initial={{ opacity: 0, x: -20 }}
                animate={benefitsInView ? { opacity: 1, x: 0 } : {}}
                transition={{ duration: 0.8, delay: index * 0.1 }}
              >
                <CheckCircle className="w-6 h-6 text-accent-teal flex-shrink-0" />
                <span className="text-lg">{benefit}</span>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Testimonials */}
      <section ref={testimonialsRef} className="section-padding">
        <div className="container mx-auto px-6">
          <motion.div 
            className="text-center mb-16"
            initial={{ opacity: 0, y: 30 }}
            animate={testimonialsInView ? { opacity: 1, y: 0 } : {}}
            transition={{ duration: 0.8 }}
          >
            <h2 className="text-4xl md:text-5xl font-bold mb-4">
              Loved by
              <span className="text-gradient"> Real Users</span>
            </h2>
            <p className="text-xl text-gray-300 max-w-2xl mx-auto">
              Join thousands who have transformed their mental wellness
            </p>
          </motion.div>
          
          <div className="grid md:grid-cols-3 gap-8">
            {testimonials.map((testimonial, index) => (
              <motion.div
                key={index}
                className="glass-card glass-card-hover rounded-2xl p-8"
                initial={{ opacity: 0, y: 30 }}
                animate={testimonialsInView ? { opacity: 1, y: 0 } : {}}
                transition={{ duration: 0.8, delay: index * 0.2 }}
              >
                <div className="flex mb-4">
                  {[...Array(testimonial.rating)].map((_, i) => (
                    <Star key={i} className="w-5 h-5 text-yellow-400 fill-current" />
                  ))}
                </div>
                <p className="text-gray-300 mb-6 italic">"{testimonial.content}"</p>
                <div>
                  <div className="font-semibold">{testimonial.name}</div>
                  <div className="text-sm text-gray-400">{testimonial.role}</div>
                </div>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Final CTA Section */}
      <section ref={ctaRef} className="section-padding">
        <div className="container mx-auto px-6">
          <motion.div 
            className="glass-card rounded-3xl p-12 text-center max-w-4xl mx-auto"
            initial={{ opacity: 0, y: 30 }}
            animate={ctaInView ? { opacity: 1, y: 0 } : {}}
            transition={{ duration: 0.8 }}
          >
            <h2 className="text-4xl md:text-5xl font-bold mb-4">
              Start Your
              <span className="text-gradient"> Mental Wellness Journey</span>
              <br />
              Today
            </h2>
            <p className="text-xl text-gray-300 mb-8 max-w-2xl mx-auto">
              Join thousands who have found peace, focus, and better sleep with DrMindit
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <button 
                onClick={() => handleCTAClick('download')}
                className="pill-button pill-button-primary text-lg"
              >
                <Download className="mr-2 w-5 h-5" />
                Download App
              </button>
              <button 
                onClick={() => handleCTAClick('try-free')}
                className="pill-button pill-button-secondary text-lg"
              >
                Try Free
                <ArrowRight className="ml-2 w-5 h-5" />
              </button>
            </div>
          </motion.div>
        </div>
      </section>

      {/* Footer */}
      <footer className="py-12 border-t border-gray-800">
        <div className="container mx-auto px-6">
          <div className="grid md:grid-cols-4 gap-8 mb-8">
            <div>
              <h3 className="text-xl font-semibold mb-4 text-gradient">DrMindit</h3>
              <p className="text-gray-400">
                Your companion for mental wellness and stress relief.
              </p>
            </div>
            <div>
              <h4 className="font-semibold mb-4">Product</h4>
              <ul className="space-y-2 text-gray-400">
                <li><a href="#" className="hover:text-accent-teal transition-colors">Features</a></li>
                <li><a href="#" className="hover:text-accent-teal transition-colors">Pricing</a></li>
                <li><a href="#" className="hover:text-accent-teal transition-colors">Testimonials</a></li>
              </ul>
            </div>
            <div>
              <h4 className="font-semibold mb-4">Company</h4>
              <ul className="space-y-2 text-gray-400">
                <li><a href="#" className="hover:text-accent-teal transition-colors">About</a></li>
                <li><a href="#" className="hover:text-accent-teal transition-colors">Contact</a></li>
                <li><a href="#" className="hover:text-accent-teal transition-colors">Blog</a></li>
              </ul>
            </div>
            <div>
              <h4 className="font-semibold mb-4">Legal</h4>
              <ul className="space-y-2 text-gray-400">
                <li><a href="#" className="hover:text-accent-teal transition-colors">Privacy Policy</a></li>
                <li><a href="#" className="hover:text-accent-teal transition-colors">Terms of Service</a></li>
                <li><a href="#" className="hover:text-accent-teal transition-colors">Cookie Policy</a></li>
              </ul>
            </div>
          </div>
          <div className="text-center text-gray-400 pt-8 border-t border-gray-800">
            <p>&copy; 2024 DrMindit. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  )
}
