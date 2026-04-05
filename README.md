# DrMindit - Mental Health Application

A production-ready mental health application built with Kotlin Multiplatform (KMP) and Jetpack Compose, targeting schools, colleges, corporate employees, government employees, and police/military personnel.

## 🎯 Features

### Core Features
- **Personal Dashboard** - Mental health score, daily check-ins, recommended sessions, weekly insights
- **Session Player** - Modern audio interface with visualization, playback controls, progress tracking
- **21-Day Programs** - Structured programs with day-by-day unlock system and progress tracking
- **Guided Library** - Categorized sessions with search, favorites, and session of the day
- **Multi-step Onboarding** - User type selection, personal goals, stress level assessment
- **Organization Dashboard** - Analytics view for organizations with department insights

### Target Areas
- Stress management
- Anxiety reduction
- Sleep improvement
- Depression support
- Focus & productivity

## 📸 Application Screens

### 🏠 Home
![Home](docs/screens/home.png)
*Personal dashboard with mood tracking, daily goals, session recommendations, and progress insights.*

### � Login
![Login](docs/screens/login.png)
*Secure authentication with social login options and professional healthcare interface.*

### 🎧 Session
![Session](docs/screens/session.png)
*Immersive audio player with breathing exercises, progress tracking, and session notes.*

### 💬 Chat
![Chat](docs/screens/chat.png)
*AI-powered mental health assistant with voice input and contextual conversation history.*

### 📊 Dashboard
![Dashboard](docs/screens/dashboard.png)
*Comprehensive analytics with mood trends, session statistics, and personalized insights.*

### � Profile
![Profile](docs/screens/profile.png)
*User profile with stress management, personal goals, session statistics, and app settings.*

## 🛡️ Security

### 🔐 **Enterprise-Grade Security**
- **Zero Secret Exposure**: No API keys in source code
- **Secure Backend Proxy**: All sensitive API calls through secure server
- **Rate Limiting**: 20 requests/minute per user
- **JWT Authentication**: Secure token-based authentication
- **Input Validation**: Comprehensive XSS and injection prevention
- **HTTPS Only**: Production endpoints use HTTPS exclusively

### 🚀 **Quick Setup**
```bash
# Backend Setup
cd backend/secure-proxy
cp .env.example .env
# Edit .env with your API keys
npm install
npm start

# Client Configuration
# No API keys needed - uses secure backend proxy
# Authentication handled through secure endpoints
```

### 📋 **Security Checklist**
- ✅ No hardcoded secrets in source code
- ✅ Environment variables for all sensitive data
- ✅ Rate limiting and abuse prevention
- ✅ Input validation and sanitization
- ✅ Secure authentication with JWT tokens
- ✅ HTTPS enforcement and security headers
- ✅ Comprehensive logging and monitoring

[📖 **Security Documentation**](SECURITY_HARDENING.md) | [🔧 **Backend Setup**](backend/secure-proxy/README.md)

## 🧱 Tech Stack

### Architecture
- **Kotlin Multiplatform (KMP)** - Shared business logic
- **Jetpack Compose** - Modern UI framework
- **Clean Architecture** - Separation of concerns
- **MVVM Pattern** - ViewModels with StateFlow
- **Repository Pattern** - Data access layer

### Dependencies
- **Koin** - Dependency injection
- **Ktor** - Networking
- **Kotlinx Serialization** - JSON parsing
- **Kotlinx Coroutines** - Async operations
- **Media3 ExoPlayer** - Audio playback

## 📱 Modules

### Shared Module (KMP)
- Domain models (User, Session, Program, Analytics)
- Repository interfaces
- Use cases
- Business logic

### Android App Module
- UI screens with Jetpack Compose
- ViewModels
- Navigation
- Audio player implementation
- Repository implementations

## 🎨 UI Design

### Design Principles
- Clean, modern, calming UI inspired by meditation apps
- Soft colors (blue, purple, gradients)
- Rounded cards (16dp+)
- Smooth animations
- Minimalistic typography
- Dark mode support

### Key Screens
1. **Personal Dashboard**
   - Dynamic greeting
   - Mental health score (circular progress)
   - Daily mood check-in
   - Recommended sessions
   - Weekly insights
   - Focus card
   - Mindful minutes tracker

2. **Session Player**
   - Full-screen immersive design
   - Audio visualization
   - Playback controls
   - Progress bar
   - Speed control
   - Timer functionality

3. **21-Day Program Flow**
   - Day-by-day unlock system
   - Progress tracking
   - Session cards
   - Completion stats

4. **Guided Library**
   - Category browsing
   - Search functionality
   - Session of the day
   - Favorites system

5. **Onboarding**
   - User type selection
   - Personal goals
   - Stress level assessment
   - Progress indicators

6. **Organization Dashboard**
   - Analytics charts
   - Department insights
   - Completion statistics
   - Export functionality

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog | 2023.1.1 or later
- JDK 17
- Android SDK API 34

### Setup
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run the Android app

### Building
```bash
./gradlew assembleDebug
```

### Running Tests
```bash
./gradlew test
```

## 📁 Project Structure

```
DrMindit/
├── shared/                          # KMP shared module
│   └── src/commonMain/kotlin/
│       ├── domain/
│       │   ├── model/              # Data models
│       │   ├── repository/         # Repository interfaces
│       │   └── usecase/           # Use cases
│       ├── data/
│       │   ├── remote/            # Remote data sources
│       │   └── local/             # Local data sources
│       └── build.gradle.kts       # KMP configuration
├── androidApp/                      # Android application
│   └── src/main/kotlin/
│       ├── di/                    # Dependency injection
│       ├── data/                  # Android-specific data
│       ├── navigation/            # Navigation setup
│       ├── player/                # Audio player
│       ├── ui/
│       │   ├── components/        # Reusable UI components
│       │   ├── screens/           # Main screens
│       │   └── viewmodel/         # ViewModels
│       ├── DrMinditApplication.kt
│       └── MainActivity.kt
├── build.gradle.kts                # Root build configuration
├── gradle.properties              # Gradle properties
└── settings.gradle.kts           # Project settings
```

## 🔧 Configuration

### Dependencies
- Kotlin 1.9.20
- Compose 1.5.10
- Android Gradle Plugin 8.1.2
- Koin 3.5.3
- Ktor 2.3.6

### Build Variants
- `debug` - Development build with logging
- `release` - Production build with optimizations

## 📊 Data Models

### User
- Personal information
- User type (Student, Corporate, Government, Military)
- Personal goals
- Stress level
- Preferences

### Session
- Audio content
- Categories (Sleep, Anxiety, Stress, Focus)
- Ratings and metadata
- Progress tracking

### Program
- 21-day structured programs
- Session sequences
- Progress tracking
- Completion stats

### Analytics
- User mental health metrics
- Organization insights
- Engagement statistics
- Progress analytics

## 🎵 Audio Player

### Features
- Background playback
- Notification controls
- Progress tracking
- Speed control
- Session completion
- Offline support (structure ready)

### Implementation
- Media3 ExoPlayer integration
- Foreground service
- State management with StateFlow
- Audio visualization

## 🔐 Authentication

### Supported Methods
- Email/Password
- Google Sign-In (structure ready)
- Session management

### Security
- Token-based authentication
- Secure storage
- Session persistence

## 📈 Analytics & Tracking

### User Analytics
- Mental health score tracking
- Session completion rates
- Mood patterns
- Progress insights

### Organization Analytics
- Department-level insights
- Engagement metrics
- Stress level trends
- Program completion stats

## 🎯 Target Organizations

### Education
- Schools & Colleges
- Student wellness programs
- Stress management for exams

### Corporate
- Employee wellness programs
- Stress reduction initiatives
- Productivity enhancement

### Government
- Government employee wellness
- Stress management programs
- Mental health support

### Defense & Security
- Police personnel
- Military personnel
- High-stress environment support

## 🚀 Future Enhancements

### Planned Features
- AI-based recommendations
- Offline download support
- Advanced analytics
- Multi-language support
- Wear OS integration
- Web dashboard for organizations

### Technical Improvements
- Real backend integration
- Advanced audio features
- Enhanced animations
- Accessibility improvements
- Performance optimizations

## 📄 License

This project is proprietary and confidential.

## 🤝 Contributing

This is an internal project. Please follow the established coding standards and review processes.

## 📞 Support

For technical support or questions, please contact the development team.
