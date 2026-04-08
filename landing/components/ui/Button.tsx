'use client'

import { motion } from 'framer-motion'
import { ReactNode } from 'react'

interface ButtonProps {
  children: ReactNode
  variant?: 'primary' | 'secondary'
  size?: 'sm' | 'md' | 'lg'
  onClick?: () => void
  disabled?: boolean
  className?: string
  icon?: ReactNode
  iconPosition?: 'left' | 'right'
}

export default function Button({
  children,
  variant = 'primary',
  size = 'md',
  onClick,
  disabled = false,
  className = '',
  icon,
  iconPosition = 'left'
}: ButtonProps) {
  const baseClasses = 'pill-button font-semibold transition-all duration-300 flex items-center justify-center gap-2'
  
  const sizeClasses = {
    sm: 'px-4 py-2 text-sm',
    md: 'px-6 py-3 text-base',
    lg: 'px-8 py-4 text-lg'
  }
  
  const variantClasses = {
    primary: 'pill-button-primary',
    secondary: 'pill-button-secondary'
  }
  
  const buttonClasses = `${baseClasses} ${sizeClasses[size]} ${variantClasses[variant]} ${className}`
  
  return (
    <motion.button
      className={buttonClasses}
      onClick={onClick}
      disabled={disabled}
      whileHover={{ scale: disabled ? 1 : 1.05 }}
      whileTap={{ scale: disabled ? 1 : 0.95 }}
      transition={{ duration: 0.2 }}
    >
      {icon && iconPosition === 'left' && icon}
      {children}
      {icon && iconPosition === 'right' && icon}
    </motion.button>
  )
}
