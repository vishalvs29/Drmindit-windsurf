# DrMindit AI Voice Assistant

A comprehensive AI-powered voice assistant for mental wellness, featuring natural voice conversations, guided sessions, and advanced safety features.

## 🎯 Features

### 🎤 Voice Chat Assistant
- **Natural Conversations**: Speak naturally with AI-powered responses
- **Real-time Transcription**: Advanced speech-to-text technology
- **Voice Synthesis**: Calming AI-generated voice responses
- **Session Persistence**: Conversation history and context tracking

### 🎧 Voice-Guided Sessions
- **Predefined Scripts**: Mental wellness exercises and meditations
- **Multiple Categories**: Stress relief, sleep, focus, mindfulness
- **Audio Generation**: High-quality text-to-speech conversion
- **Professional Player**: Full audio controls with progress tracking

### ⚡ Quick Calm Feature
- **One-Click Access**: Instant stress relief button
- **Emergency Support**: Immediate calming guidance
- **Smart Selection**: Context-aware technique recommendations
- **Rapid Response**: Instant audio playback

### 🛡️ Safety System
- **Distress Detection**: Automated keyword scanning for crisis situations
- **Emergency Resources**: Crisis hotline information and professional referrals
- **Safe Responses**: Professional mental health guidance with clear boundaries
- **Disclaimer System**: Clear usage limitations and professional care recommendations

## 🚀 Quick Start

### Prerequisites
- Node.js 18+ 
- OpenAI API key (for TTS/STT services)

### Installation

1. **Clone and navigate**:
```bash
cd voice-assistant
```

2. **Install dependencies**:
```bash
npm install
```

3. **Set up environment**:
```bash
cp .env.local.example .env.local
# Edit .env.local and add your OpenAI API key
```

4. **Run development server**:
```bash
npm run dev
```

5. **Open application**:
Navigate to [http://localhost:3000](http://localhost:3000)

## 🔧 Configuration

### Environment Variables

Create a `.env.local` file with:

```env
# OpenAI API Key (required for TTS/STT)
OPENAI_API_KEY=your_openai_api_key_here

# Environment
NODE_ENV=development

# Analytics (optional)
NEXT_PUBLIC_GA_ID=G-XXXXXXXXXX
```

### API Endpoints

#### `/api/stt` - Speech to Text
```typescript
POST /api/stt
Content-Type: multipart/form-data
Body: FormData with audio file

Response:
{
  text: string,
  confidence: number,
  duration: number
}
```

#### `/api/chat` - AI Chat
```typescript
POST /api/chat
Content-Type: application/json
Body: {
  message: string,
  context?: any,
  sessionId?: string
}

Response:
{
  response: string,
  sessionId: string,
  timestamp: string,
  distressDetected: boolean,
  emergencyResources?: Array
}
```

#### `/api/tts` - Text to Speech
```typescript
POST /api/tts
Content-Type: application/json
Body: {
  text: string,
  voice?: string,
  speed?: number
}

Response:
{
  audio: string, // base64 encoded MP3
  format: string,
  voice: string,
  duration: number
}

GET /api/tts
Response:
{
  scripts: {
    quickCalm: Array,
    guidedSessions: Array
  }
}
```

## 🎨 UI Components

### VoiceAssistant Component
Main voice chat interface with:
- Microphone button with visual feedback
- Wave animations during recording
- Real-time transcript display
- AI response playback
- Quick calm integration
- Safety disclaimers

### VoiceSessionPlayer Component
Professional audio player with:
- Play/pause controls
- Progress bar with seek functionality
- Volume control
- Skip forward capability
- Session information display

## 🛡️ Safety Features

### Distress Detection
The system automatically detects distress keywords including:
- Suicide, self-harm, death-related terms
- Hopeless, worthless statements
- Crisis indicators

### Emergency Resources
When distress is detected, the system provides:
- 988 Suicide & Crisis Lifeline
- Crisis Text Line (HOME to 741741)
- National Suicide Prevention Lifeline (1-800-273-8255)
- Professional healthcare recommendations

### Safe Response Guidelines
- Clear AI role boundaries
- Professional mental health focus
- No medical advice or diagnosis
- Crisis escalation procedures
- Professional referral recommendations

## 🎯 Voice Options

### Available Voices
- `calm-female` - Soothing female voice (default)
- `calm-male` - Gentle male voice
- `gentle-female` - Soft female tone
- `professional-female` - Clear professional voice
- `soothing-male` - Calming male voice

### Voice Parameters
- **Speed**: 0.7 to 1.3 (default: 0.9)
- **Format**: MP3 with base64 encoding
- **Quality**: High-fidelity audio generation

## 📱 Responsive Design

### Mobile Features
- Touch-optimized controls
- Large microphone button
- Swipe gesture support
- Compact layout for small screens
- Native audio integration

### Desktop Features
- Keyboard shortcuts support
- Hover state enhancements
- Multi-window capability
- Extended interface options

## 🚀 Performance

### Frontend Optimization
- Lazy loading with code splitting
- Hardware-accelerated animations
- Optimized bundle size
- Efficient audio processing
- Smart caching strategies

### Backend Performance
- Serverless API architecture
- Optimized response times
- Efficient audio streaming
- Memory and CPU optimization
- Error recovery mechanisms

## 🧪 Development

### Scripts
```bash
npm run dev      # Start development server
npm run build    # Build for production
npm run start    # Start production server
npm run lint     # Run ESLint
```

### Project Structure
```
voice-assistant/
├── app/
│   ├── api/
│   │   ├── stt/route.ts      # Speech-to-text endpoint
│   │   ├── chat/route.ts     # AI chat endpoint
│   │   └── tts/route.ts      # Text-to-speech endpoint
│   ├── layout.tsx              # Root layout
│   ├── page.tsx               # Main page
│   └── globals.css             # Global styles
├── components/
│   ├── VoiceAssistant.tsx       # Main voice chat component
│   └── VoiceSessionPlayer.tsx  # Audio session player
├── package.json
├── tailwind.config.js
├── next.config.js
├── tsconfig.json
└── README.md
```

## 🔒 Security

### Data Protection
- Input sanitization and validation
- Length limits to prevent abuse
- Secure error handling
- No unnecessary data storage
- Privacy-first design

### Safety Measures
- Distress keyword detection
- Emergency resource provision
- Professional response guidelines
- Clear usage disclaimers
- Crisis escalation protocols

## 📊 Analytics

### Tracking Events
- Voice chat interactions
- Session completions
- Quick calm usage
- Error events
- Performance metrics

### Privacy Compliance
- Anonymous usage data
- No personal information storage
- GDPR-compliant practices
- User consent mechanisms

## 🚀 Deployment

### Production Build
```bash
npm run build
```

### Environment Setup
1. Set production environment variables
2. Configure API keys
3. Set up analytics (optional)
4. Test all endpoints

### Deployment Options
- **Vercel**: Recommended for Next.js apps
- **Netlify**: Static hosting option
- **AWS Lambda**: Serverless deployment
- **Docker**: Containerized deployment

## 🤝 Contributing

### Development Guidelines
1. Fork the repository
2. Create feature branch
3. Implement changes with tests
4. Maintain code style and type safety
5. Submit pull request

### Code Style
- TypeScript for type safety
- ESLint for code quality
- Prettier for formatting
- Conventional commit messages
- Comprehensive error handling

## 📄 License

MIT License - see LICENSE file for details.

## 🆘 Support

### Documentation
- API documentation in endpoint files
- Component documentation with JSDoc
- Setup instructions in README
- Safety guidelines in code comments

### Issues
- Report bugs via GitHub Issues
- Feature requests via Discussions
- Security issues via private email
- Community support via Discussions

---

**🎉 The DrMindit AI Voice Assistant provides a comprehensive, safe, and professional voice interaction system for mental wellness support.**
