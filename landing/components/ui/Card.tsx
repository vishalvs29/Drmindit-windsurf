'use client'

import { motion } from 'framer-motion'
import { ReactNode } from 'react'

interface CardProps {
  children: ReactNode
  className?: string
  hover?: boolean
  glass?: boolean
  onClick?: () => void
}

export default function Card({ 
  children, 
  className = '', 
  hover = true, 
  glass = true,
  onClick 
}: CardProps) {
  const baseClasses = 'rounded-2xl p-6 transition-all duration-300'
  
  const glassClasses = glass ? 'glass-card' : ''
  const hoverClasses = hover ? 'glass-card-hover cursor-pointer' : ''
  const clickClasses = onClick ? 'cursor-pointer' : ''
  
  const cardClasses = `${baseClasses} ${glassClasses} ${hoverClasses} ${clickClasses} ${className}`
  
  return (
    <motion.div
      className={cardClasses}
      onClick={onClick}
      whileHover={hover ? { scale: 1.05 } : {}}
      whileTap={onClick ? { scale: 0.98 } : {}}
      transition={{ duration: 0.2 }}
    >
      {children}
    </motion.div>
  )
}
