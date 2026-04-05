'use client'

import { useState, useEffect } from 'react'
import { Globe } from 'lucide-react'

interface LanguageSelectorProps {
  selectedLanguage: string
  onLanguageChange: (language: string) => void
}

const languages = [
  { code: 'en', name: 'english', displayName: 'English' },
  { code: 'hi', name: 'hindi', displayName: 'हिन्दी' },
  { code: 'ta', name: 'tamil', displayName: 'தமிழ்' },
  { code: 'mr', name: 'marathi', displayName: 'मराठी' },
  { code: 'bn', name: 'bengali', displayName: 'বাংলা' },
  { code: 'gu', name: 'gujarati', displayName: 'ગુજરાતી' },
  { code: 'te', name: 'telugu', displayName: 'తెలుగు' },
  { code: 'kn', name: 'kannada', displayName: 'ಕನ್ನಡ' },
  { code: 'ml', name: 'malayalam', displayName: 'മലയാളം' },
  { code: 'pa', name: 'punjabi', displayName: 'ਪੰਜਾਬੀ' },
  { code: 'ur', name: 'urdu', displayName: 'اردو' }
]

export default function LanguageSelector({ selectedLanguage, onLanguageChange }: LanguageSelectorProps) {
  return (
    <div className="flex items-center gap-2 p-2">
      <Globe className="w-4 h-4 text-gray-600" />
      <select
        value={selectedLanguage}
        onChange={(e) => onLanguageChange(e.target.value)}
        className="px-3 py-2 border border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
      >
        {languages.map((lang) => (
          <option key={lang.code} value={lang.code}>
            {lang.displayName}
          </option>
        ))}
      </select>
    </div>
  )
}
