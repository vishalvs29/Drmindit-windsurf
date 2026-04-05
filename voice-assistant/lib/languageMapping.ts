// Language mapping for STT with Indian language support
interface LanguageConfig {
  code: string
  name: string
  displayName: string
  whisperCode: string
}

export const SUPPORTED_LANGUAGES: Record<string, LanguageConfig> = {
  en: {
    code: 'en',
    name: 'english',
    displayName: 'English',
    whisperCode: 'en'
  },
  hi: {
    code: 'hi',
    name: 'hindi',
    displayName: 'हिन्दी (Hindi)',
    whisperCode: 'hi'
  },
  ta: {
    code: 'ta',
    name: 'tamil',
    displayName: 'தமிழ் (Tamil)',
    whisperCode: 'ta'
  },
  mr: {
    code: 'mr',
    name: 'marathi',
    displayName: 'मराठी (Marathi)',
    whisperCode: 'mr'
  },
  bn: {
    code: 'bn',
    name: 'bengali',
    displayName: 'বাংলা (Bengali)',
    whisperCode: 'bn'
  },
  gu: {
    code: 'gu',
    name: 'gujarati',
    displayName: 'ગુજરાતી (Gujarati)',
    whisperCode: 'gu'
  },
  te: {
    code: 'te',
    name: 'telugu',
    displayName: 'తెలుగు (Telugu)',
    whisperCode: 'te'
  },
  kn: {
    code: 'kn',
    name: 'kannada',
    displayName: 'ಕನ್ನಡ (Kannada)',
    whisperCode: 'kn'
  },
  ml: {
    code: 'ml',
    name: 'malayalam',
    displayName: 'മലയാളം (Malayalam)',
    whisperCode: 'ml'
  },
  pa: {
    code: 'pa',
    name: 'punjabi',
    displayName: 'ਪੰਜਾਬੀ (Punjabi)',
    whisperCode: 'pa'
  },
  ur: {
    code: 'ur',
    name: 'urdu',
    displayName: 'اردو (Urdu)',
    whisperCode: 'ur'
  },
  // Auto-detect option
  auto: {
    code: 'auto',
    name: 'auto',
    displayName: 'Auto-detect',
    whisperCode: '' // Empty string for auto-detection
  }
}

export function getLanguageConfig(languageCode: string): LanguageConfig {
  return SUPPORTED_LANGUAGES[languageCode] || SUPPORTED_LANGUAGES.en
}

export function getIndianLanguages(): LanguageConfig[] {
  return [
    SUPPORTED_LANGUAGES.hi,
    SUPPORTED_LANGUAGES.ta,
    SUPPORTED_LANGUAGES.mr,
    SUPPORTED_LANGUAGES.bn,
    SUPPORTED_LANGUAGES.gu,
    SUPPORTED_LANGUAGES.te,
    SUPPORTED_LANGUAGES.kn,
    SUPPORTED_LANGUAGES.ml,
    SUPPORTED_LANGUAGES.pa,
    SUPPORTED_LANGUAGES.ur
  ]
}

export function getAllLanguages(): LanguageConfig[] {
  return Object.values(SUPPORTED_LANGUAGES)
}

// Detect if language is Indian
export function isIndianLanguage(languageCode: string): boolean {
  const indianLanguages = ['hi', 'ta', 'mr', 'bn', 'gu', 'te', 'kn', 'ml', 'pa', 'ur']
  return indianLanguages.includes(languageCode)
}
