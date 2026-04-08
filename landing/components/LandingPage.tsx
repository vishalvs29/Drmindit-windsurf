import React, { useState, useEffect, useRef } from 'react';
import { motion, useScroll, useTransform, useInView } from 'framer-motion';
import { Play, Pause, Volume2, Brain, BarChart3, Moon, Users, Briefcase, Shield, Download, ArrowRight, Menu, X, Star, Quote, Facebook, Twitter, Instagram, ChevronRight } from 'lucide-react';

// Animation variants
const fadeInUp = {
  hidden: { opacity: 0, y: 30 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.6, ease: "easeOut" } }
};

const fadeInLeft = {
  hidden: { opacity: 0, x: -50 },
  visible: { opacity: 1, x: 0, transition: { duration: 0.6, ease: "easeOut" } }
};

const fadeInRight = {
  hidden: { opacity: 0, x: 50 },
  visible: { opacity: 1, x: 0, transition: { duration: 0.6, ease: "easeOut" } }
};

const scaleOnHover = {
  hover: { scale: 1.05, transition: { duration: 0.3, ease: "easeOut" } }
};

const floatAnimation = {
  initial: { y: 0 },
  animate: {
    y: [-10, 10, -10],
    transition: {
      duration: 4,
      repeat: Infinity,
      ease: "easeInOut"
    }
  }
};

const Navbar = () => {
  const [isScrolled, setIsScrolled] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 20);
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  return (
    <motion.nav
      className={`fixed top-0 w-full z-50 transition-all duration-300 ${
        isScrolled ? 'bg-white/10 backdrop-blur-md border-b border-white/10' : 'bg-transparent'
      }`}
      initial={{ y: -100 }}
      animate={{ y: 0 }}
      transition={{ duration: 0.6 }}
    >
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <motion.div
            className="text-2xl font-bold text-white"
            whileHover={{ scale: 1.05 }}
          >
            DrMindit
          </motion.div>

          {/* Desktop Navigation */}
          <div className="hidden md:flex items-center space-x-8">
            {['Features', 'For You', 'Testimonials', 'Login'].map((item) => (
              <motion.a
                key={item}
                href="#"
                className="text-white/80 hover:text-white transition-colors"
                whileHover={{ scale: 1.05 }}
              >
                {item}
              </motion.a>
            ))}
            <motion.button
              className="bg-white text-purple-600 px-6 py-2 rounded-full font-semibold hover:bg-white/90 transition-colors"
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
            >
              Start Free
            </motion.button>
          </div>

          {/* Mobile Menu Button */}
          <motion.button
            className="md:hidden text-white"
            onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
            whileTap={{ scale: 0.9 }}
          >
            {isMobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
          </motion.button>
        </div>

        {/* Mobile Menu */}
        {isMobileMenuOpen && (
          <motion.div
            className="md:hidden bg-white/10 backdrop-blur-md rounded-lg mt-2 p-4"
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
          >
            {['Features', 'For You', 'Testimonials', 'Login'].map((item) => (
              <a
                key={item}
                href="#"
                className="block text-white/80 hover:text-white py-2 transition-colors"
              >
                {item}
              </a>
            ))}
            <button className="bg-white text-purple-600 px-6 py-2 rounded-full font-semibold w-full mt-4">
              Start Free
            </button>
          </motion.div>
        )}
      </div>
    </motion.nav>
  );
};

const HeroSection = () => {
  const ref = useRef(null);
  const isInView = useInView(ref, { once: true });

  return (
    <section className="min-h-screen flex items-center justify-center relative overflow-hidden">
      {/* Background gradient and particles */}
      <div className="absolute inset-0 bg-gradient-to-br from-blue-600 via-purple-600 to-pink-600" />
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

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        <div className="grid lg:grid-cols-2 gap-12 items-center">
          <motion.div
            ref={ref}
            initial="hidden"
            animate={isInView ? "visible" : "hidden"}
            variants={fadeInLeft}
          >
            <h1 className="text-4xl md:text-6xl font-bold text-white mb-6 leading-tight">
              Feel calm, focused, and stress-free in minutes
            </h1>
            <p className="text-xl text-white/80 mb-8">
              AI-powered mental wellness for students, professionals, and high-pressure roles.
            </p>
            <div className="flex flex-col sm:flex-row gap-4">
              <motion.button
                className="bg-white text-purple-600 px-8 py-4 rounded-full font-semibold text-lg hover:bg-white/90 transition-colors flex items-center justify-center gap-2"
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
              >
                Start Free
                <ArrowRight size={20} />
              </motion.button>
              <motion.button
                className="bg-white/20 backdrop-blur-md text-white px-8 py-4 rounded-full font-semibold text-lg hover:bg-white/30 transition-colors flex items-center justify-center gap-2"
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
              >
                <Volume2 size={20} />
                Explore Audio
              </motion.button>
            </div>
          </motion.div>

          <motion.div
            className="relative"
            variants={floatAnimation}
            initial="initial"
            animate="animate"
          >
            <div className="relative z-10">
              {/* Phone mockup */}
              <div className="bg-black/20 backdrop-blur-md rounded-3xl p-2 max-w-sm mx-auto">
                <div className="bg-gradient-to-br from-purple-900/50 to-blue-900/50 rounded-2xl p-6">
                  {/* Audio player UI */}
                  <div className="text-center mb-6">
                    <div className="w-32 h-32 mx-auto bg-gradient-to-br from-purple-500 to-blue-500 rounded-full flex items-center justify-center mb-4">
                      <Play className="text-white w-12 h-12" />
                    </div>
                    <h3 className="text-white font-semibold mb-2">Mindful Breathing</h3>
                    <p className="text-white/60 text-sm">Dr. Sarah Johnson</p>
                  </div>
                  
                  <div className="bg-white/10 rounded-full h-2 mb-4">
                    <div className="bg-white rounded-full h-2 w-1/3"></div>
                  </div>
                  
                  <div className="flex justify-between text-white/60 text-sm mb-6">
                    <span>3:24</span>
                    <span>10:00</span>
                  </div>
                  
                  <div className="flex justify-center gap-6">
                    <button className="text-white/60 hover:text-white">
                      <Volume2 size={20} />
                    </button>
                    <button className="bg-white text-purple-600 rounded-full p-4">
                      <Pause size={24} />
                    </button>
                    <button className="text-white/60 hover:text-white">
                      <BarChart3 size={20} />
                    </button>
                  </div>
                </div>
              </div>
            </div>
            
            {/* Glow effect */}
            <div className="absolute inset-0 bg-gradient-to-r from-purple-500 to-blue-500 rounded-3xl blur-3xl opacity-30 -z-10" />
          </motion.div>
        </div>
      </div>
    </section>
  );
};

const TestimonialsSection = () => {
  const ref = useRef(null);
  const isInView = useInView(ref, { once: true });

  const testimonials = [
    {
      quote: "DrMindit helped me manage exam stress and improve my focus. I feel more confident than ever!",
      name: "Raj",
      role: "Engineering Student"
    },
    {
      quote: "The AI sessions are incredibly insightful. It's like having a therapist in my pocket.",
      name: "Priya",
      role: "Software Engineer"
    },
    {
      quote: "This app has been a lifeline during high-pressure situations. Truly life-changing.",
      name: "Officer K",
      role: "Police Officer"
    }
  ];

  return (
    <section className="py-20 relative">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <motion.h2
          ref={ref}
          initial="hidden"
          animate={isInView ? "visible" : "hidden"}
          variants={fadeInUp}
          className="text-4xl font-bold text-center mb-16 text-white"
        >
          Trusted by students & professionals
        </motion.h2>

        <div className="grid md:grid-cols-3 gap-8">
          {testimonials.map((testimonial, index) => (
            <motion.div
              key={index}
              initial="hidden"
              animate={isInView ? "visible" : "hidden"}
              variants={fadeInUp}
              transition={{ delay: index * 0.2 }}
              whileHover="hover"
              className="bg-white/10 backdrop-blur-md rounded-2xl p-8 border border-white/20"
            >
              <Quote className="text-white/40 mb-4" size={32} />
              <p className="text-white/90 mb-6 italic">"{testimonial.quote}"</p>
              <div>
                <p className="text-white font-semibold">{testimonial.name}</p>
                <p className="text-white/60 text-sm">{testimonial.role}</p>
              </div>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
};

const FeaturesSection = () => {
  const ref = useRef(null);
  const isInView = useInView(ref, { once: true });

  const features = [
    {
      icon: Volume2,
      title: "Audio Therapy",
      description: "Guided meditations and breathing exercises for instant calm"
    },
    {
      icon: Brain,
      title: "AI Sessions",
      description: "Personalized AI conversations for mental wellness support"
    },
    {
      icon: BarChart3,
      title: "Mood Insights",
      description: "Track your emotional patterns and progress over time"
    },
    {
      icon: Moon,
      title: "Sleep Mode",
      description: "Sleep stories and sounds for better rest quality"
    }
  ];

  return (
    <section className="py-20">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <motion.h2
          ref={ref}
          initial="hidden"
          animate={isInView ? "visible" : "hidden"}
          variants={fadeInUp}
          className="text-4xl font-bold text-center mb-16 text-white"
        >
          Your Path to Calm
        </motion.h2>

        <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-8">
          {features.map((feature, index) => (
            <motion.div
              key={index}
              initial="hidden"
              animate={isInView ? "visible" : "hidden"}
              variants={fadeInUp}
              transition={{ delay: index * 0.1 }}
              whileHover="hover"
              className="bg-white/10 backdrop-blur-md rounded-2xl p-8 border border-white/20 text-center"
            >
              <div className="w-16 h-16 mx-auto mb-6 bg-gradient-to-br from-purple-500 to-blue-500 rounded-2xl flex items-center justify-center">
                <feature.icon className="text-white w-8 h-8" />
              </div>
              <h3 className="text-xl font-semibold text-white mb-3">{feature.title}</h3>
              <p className="text-white/70">{feature.description}</p>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
};

const TargetAudienceSection = () => {
  const ref = useRef(null);
  const isInView = useInView(ref, { once: true });

  const audiences = [
    {
      icon: Users,
      title: "Students",
      subtitle: "Beat Exam Stress",
      description: "Manage academic pressure and improve focus"
    },
    {
      icon: Briefcase,
      title: "Professionals",
      subtitle: "Reduce Burnout",
      description: "Balance work stress and maintain productivity"
    },
    {
      icon: Shield,
      title: "Police & Military",
      subtitle: "Recover & Recharge",
      description: "Build resilience after high-pressure situations"
    }
  ];

  return (
    <section className="py-20">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <motion.h2
          ref={ref}
          initial="hidden"
          animate={isInView ? "visible" : "hidden"}
          variants={fadeInUp}
          className="text-4xl font-bold text-center mb-16 text-white"
        >
          Designed For You
        </motion.h2>

        <div className="grid md:grid-cols-3 gap-8">
          {audiences.map((audience, index) => (
            <motion.div
              key={index}
              initial="hidden"
              animate={isInView ? "visible" : "hidden"}
              variants={fadeInUp}
              transition={{ delay: index * 0.2 }}
              whileHover={{ scale: 1.05 }}
              className="text-center"
            >
              <div className="w-24 h-24 mx-auto mb-6 bg-gradient-to-br from-purple-500 to-blue-500 rounded-full flex items-center justify-center">
                <audience.icon className="text-white w-12 h-12" />
              </div>
              <h3 className="text-2xl font-bold text-white mb-2">{audience.title}</h3>
              <p className="text-xl text-purple-200 font-semibold mb-3">{audience.subtitle}</p>
              <p className="text-white/70">{audience.description}</p>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
};

const ProductShowcaseSection = () => {
  const ref = useRef(null);
  const isInView = useInView(ref, { once: true });

  const showcases = [
    {
      title: "Audio Player",
      description: "Immersive soundscapes and guided sessions",
      image: "🎵"
    },
    {
      title: "AI Session",
      description: "Intelligent conversations for mental wellness",
      image: "🤖"
    },
    {
      title: "Progress Dashboard",
      description: "Track your journey to better mental health",
      image: "📊"
    }
  ];

  return (
    <section className="py-20">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <motion.h2
          ref={ref}
          initial="hidden"
          animate={isInView ? "visible" : "hidden"}
          variants={fadeInUp}
          className="text-4xl font-bold text-center mb-16 text-white"
        >
          See DrMindit in Action
        </motion.h2>

        <div className="grid md:grid-cols-3 gap-8">
          {showcases.map((showcase, index) => (
            <motion.div
              key={index}
              initial="hidden"
              animate={isInView ? "visible" : "hidden"}
              variants={fadeInUp}
              transition={{ delay: index * 0.2 }}
              whileHover={{ scale: 1.05 }}
              className="bg-white/10 backdrop-blur-md rounded-2xl overflow-hidden border border-white/20"
            >
              <div className="h-48 bg-gradient-to-br from-purple-500/20 to-blue-500/20 flex items-center justify-center text-6xl">
                {showcase.image}
              </div>
              <div className="p-6">
                <h3 className="text-xl font-semibold text-white mb-2">{showcase.title}</h3>
                <p className="text-white/70">{showcase.description}</p>
              </div>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
};

const FinalCTASection = () => {
  const ref = useRef(null);
  const isInView = useInView(ref, { once: true });

  return (
    <section className="py-20 relative">
      <div className="absolute inset-0 bg-gradient-to-r from-purple-600 to-blue-600" />
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10 text-center">
        <motion.h2
          ref={ref}
          initial="hidden"
          animate={isInView ? "visible" : "hidden"}
          variants={fadeInUp}
          className="text-4xl md:text-5xl font-bold text-white mb-8"
        >
          Start Your Mental Wellness Journey Today
        </motion.h2>
        
        <motion.div
          initial="hidden"
          animate={isInView ? "visible" : "hidden"}
          variants={fadeInUp}
          transition={{ delay: 0.2 }}
          className="flex flex-col sm:flex-row gap-4 justify-center"
        >
          <motion.button
            className="bg-white text-purple-600 px-8 py-4 rounded-full font-semibold text-lg hover:bg-white/90 transition-colors flex items-center justify-center gap-2"
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
          >
            <Download size={20} />
            Download Now
          </motion.button>
          <motion.button
            className="bg-transparent text-white px-8 py-4 rounded-full font-semibold text-lg border-2 border-white hover:bg-white/10 transition-colors"
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
          >
            Try Free
          </motion.button>
        </motion.div>
      </div>
    </section>
  );
};

const Footer = () => {
  return (
    <footer className="bg-black/20 backdrop-blur-md py-12">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid md:grid-cols-4 gap-8">
          <div>
            <h3 className="text-xl font-bold text-white mb-4">DrMindit</h3>
            <p className="text-white/60">Your mental wellness companion</p>
          </div>
          
          <div>
            <h4 className="text-white font-semibold mb-4">Company</h4>
            <ul className="space-y-2">
              <li><a href="#" className="text-white/60 hover:text-white transition-colors">About</a></li>
              <li><a href="#" className="text-white/60 hover:text-white transition-colors">Contact</a></li>
            </ul>
          </div>
          
          <div>
            <h4 className="text-white font-semibold mb-4">Legal</h4>
            <ul className="space-y-2">
              <li><a href="#" className="text-white/60 hover:text-white transition-colors">Privacy Policy</a></li>
              <li><a href="#" className="text-white/60 hover:text-white transition-colors">Terms</a></li>
            </ul>
          </div>
          
          <div>
            <h4 className="text-white font-semibold mb-4">Follow Us</h4>
            <div className="flex gap-4">
              <motion.a href="#" whileHover={{ scale: 1.2 }}>
                <Facebook className="text-white/60 hover:text-white" size={20} />
              </motion.a>
              <motion.a href="#" whileHover={{ scale: 1.2 }}>
                <Twitter className="text-white/60 hover:text-white" size={20} />
              </motion.a>
              <motion.a href="#" whileHover={{ scale: 1.2 }}>
                <Instagram className="text-white/60 hover:text-white" size={20} />
              </motion.a>
            </div>
          </div>
        </div>
        
        <div className="border-t border-white/10 mt-8 pt-8 text-center">
          <p className="text-white/60">© 2024 DrMindit. All rights reserved.</p>
        </div>
      </div>
    </footer>
  );
};

const LandingPage = () => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-600 via-purple-600 to-pink-600">
      <Navbar />
      <HeroSection />
      <TestimonialsSection />
      <FeaturesSection />
      <TargetAudienceSection />
      <ProductShowcaseSection />
      <FinalCTASection />
      <Footer />
    </div>
  );
};

export default LandingPage;
