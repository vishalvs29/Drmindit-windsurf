# DrMindit E2E Testing

End-to-end tests for the DrMindit mental health application using Playwright.

## 🚀 Quick Start

```bash
# Install dependencies
npm install

# Run all tests (web, android, ios)
npm test

# Run specific platform tests
npm run test:web     # Web application
npm run test:android  # Android application  
npm run test:ios      # iOS application

# Run tests with UI (headed mode)
npm run test:ui

# Generate test report
npm run report
```

## 📱 Test Coverage

### Authentication Flow
- ✅ User sign up with validation
- ✅ User sign in/out
- ✅ Password strength requirements
- ✅ Session persistence

### Session Player Flow
- ✅ Audio playback controls
- ✅ Mood rating before/after sessions
- ✅ Progress tracking
- ✅ Session completion flow

### Chat & Safety
- ✅ Message sending/receiving
- ✅ Real-time crisis detection
- ✅ Crisis banner/dialog triggers
- ✅ Safe messaging filter validation

### Privacy & Data
- ✅ Data category visibility
- ✅ Delete all data functionality
- ✅ Data transparency metrics
- ✅ GDPR compliance

## 🌐 Multi-Platform Testing

### Web Application
- **Base URL**: http://localhost:3000
- **Browser**: Chrome (Desktop)
- **Features**: Full web app functionality

### Android Application
- **Base URL**: http://10.0.2.2:8080
- **Device**: Pixel 5
- **Features**: Native Android app testing

### iOS Application
- **Base URL**: http://10.0.2.2:8081
- **Device**: iPhone 14
- **Features**: Native iOS app testing

## 🔧 Configuration

### Environment Variables
- `BASE_URL`: Web application URL (default: http://localhost:3000)
- `ANDROID_BASE_URL`: Android application URL
- `IOS_BASE_URL`: iOS application URL
- `CI`: Continuous integration flag

### Test Data
- **Email**: test@drmindit.com
- **Password**: TestPassword123!
- **User**: Test User

## 📊 Reports

Test reports are generated in `playwright-report/` directory:
- **HTML Report**: `playwright-report/index.html`
- **Screenshots**: Failed test screenshots
- **Videos**: Failed test recordings
- **Traces**: Execution traces for debugging

## 🎯 Critical User Journeys Tested

1. **New User Onboarding**: Sign up → Sign in → Dashboard
2. **Session Experience**: Browse → Select session → Mood rating → Audio playback → Completion
3. **Crisis Support**: Chat with crisis keywords → Banner/Dialog → Resources
4. **Data Control**: Privacy screen → View data → Delete all data → Sign out

## 🚨 Safety Testing

All crisis detection and safe messaging features are thoroughly tested:
- Real-time detection during typing
- Appropriate response levels (banner vs dialog)
- Safe message filtering and alternatives
- Resource availability and accessibility

## 📱 Device Testing

Tests cover both mobile and desktop experiences:
- Responsive design validation
- Touch interaction testing
- Cross-platform compatibility
- Performance under different network conditions
