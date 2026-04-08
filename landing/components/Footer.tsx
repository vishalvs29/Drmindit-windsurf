'use client'

import { motion } from 'framer-motion'
import { Facebook, Twitter, Instagram } from 'lucide-react'

export default function Footer() {
  const currentYear = new Date().getFullYear()

  return (
    <footer className="py-12 border-t border-white/10">
      <div className="container mx-auto px-6">
        <div className="grid md:grid-cols-4 gap-8 mb-8">
          <div>
            <h3 className="text-xl font-semibold mb-4 text-gradient">DrMindit</h3>
            <p className="text-white/60">
              Your companion for mental wellness and stress relief.
            </p>
          </div>
          
          <div>
            <h4 className="font-semibold mb-4 text-white">Product</h4>
            <ul className="space-y-2 text-white/60">
              <li>
                <motion.a 
                  href="#features" 
                  className="hover:text-purple-400 transition-colors"
                  whileHover={{ scale: 1.05 }}
                >
                  Features
                </motion.a>
              </li>
              <li>
                <motion.a 
                  href="#" 
                  className="hover:text-purple-400 transition-colors"
                  whileHover={{ scale: 1.05 }}
                >
                  Pricing
                </motion.a>
              </li>
              <li>
                <motion.a 
                  href="#testimonials" 
                  className="hover:text-purple-400 transition-colors"
                  whileHover={{ scale: 1.05 }}
                >
                  Testimonials
                </motion.a>
              </li>
            </ul>
          </div>
          
          <div>
            <h4 className="font-semibold mb-4 text-white">Company</h4>
            <ul className="space-y-2 text-white/60">
              <li>
                <motion.a 
                  href="#" 
                  className="hover:text-purple-400 transition-colors"
                  whileHover={{ scale: 1.05 }}
                >
                  About
                </motion.a>
              </li>
              <li>
                <motion.a 
                  href="#" 
                  className="hover:text-purple-400 transition-colors"
                  whileHover={{ scale: 1.05 }}
                >
                  Contact
                </motion.a>
              </li>
              <li>
                <motion.a 
                  href="#" 
                  className="hover:text-purple-400 transition-colors"
                  whileHover={{ scale: 1.05 }}
                >
                  Blog
                </motion.a>
              </li>
            </ul>
          </div>
          
          <div>
            <h4 className="font-semibold mb-4 text-white">Legal</h4>
            <ul className="space-y-2 text-white/60">
              <li>
                <motion.a 
                  href="/privacy-policy" 
                  className="hover:text-purple-400 transition-colors"
                  whileHover={{ scale: 1.05 }}
                >
                  Privacy Policy
                </motion.a>
              </li>
              <li>
                <motion.a 
                  href="/terms-of-service" 
                  className="hover:text-purple-400 transition-colors"
                  whileHover={{ scale: 1.05 }}
                >
                  Terms of Service
                </motion.a>
              </li>
              <li>
                <motion.a 
                  href="#" 
                  className="hover:text-purple-400 transition-colors"
                  whileHover={{ scale: 1.05 }}
                >
                  Cookie Policy
                </motion.a>
              </li>
            </ul>
          </div>
        </div>
        
        <div className="flex flex-col md:flex-row justify-between items-center pt-8 border-t border-white/10">
          <div className="text-white/60 mb-4 md:mb-0">
            <p>&copy; {currentYear} DrMindit. All rights reserved.</p>
          </div>
          
          <div className="flex gap-4">
            <motion.a 
              href="#" 
              className="text-white/60 hover:text-purple-400 transition-colors"
              whileHover={{ scale: 1.2 }}
            >
              <Facebook size={20} />
            </motion.a>
            <motion.a 
              href="#" 
              className="text-white/60 hover:text-purple-400 transition-colors"
              whileHover={{ scale: 1.2 }}
            >
              <Twitter size={20} />
            </motion.a>
            <motion.a 
              href="#" 
              className="text-white/60 hover:text-purple-400 transition-colors"
              whileHover={{ scale: 1.2 }}
            >
              <Instagram size={20} />
            </motion.a>
          </div>
        </div>
      </div>
    </footer>
  )
}
