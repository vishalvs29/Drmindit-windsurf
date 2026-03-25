# DrMindit - Production Deployment Guide

## 🚀 Complete Production Setup

This guide walks you through deploying the DrMindit mental health platform to production with all backend integration, audio functionality, and Play Store release configuration.

---

## 📋 Prerequisites

### Required Accounts & Services
- **Supabase Account** with production project
- **Google Play Developer Account** ($25 one-time fee)
- **Google Cloud Console** for OAuth setup
- **Domain Name** (optional but recommended)
- **SSL Certificate** (for custom domains)

### Development Tools
- **Android Studio** Arctic Fox or later
- **Java 17** or **JDK 17**
- **Git** for version control
- **Keytool** for keystore generation

---

## 🔧 Step 1: Supabase Production Setup

### 1.1 Create Production Project
```bash
# 1. Go to supabase.com and create new project
# 2. Choose production region closest to your users
# 3. Generate strong database password
# 4. Save credentials securely
```

### 1.2 Apply Production Schema
```bash
# Connect to your production Supabase database
psql -h db.your-project-id.supabase.co -U postgres -d postgres < supabase/seed_data.sql
```

### 1.3 Configure Authentication
1. **Email/Password Auth**: Enable in Supabase Auth settings
2. **Google OAuth Setup**:
   - Go to Google Cloud Console → APIs & Services → Credentials
   - Create OAuth 2.0 Client ID
   - Add redirect URL: `https://your-project-id.supabase.co/auth/v1/callback`
   - Copy Client ID and Client Secret to Supabase Auth settings

### 1.4 Set Up Storage
```sql
-- Create storage buckets
INSERT INTO storage.buckets (id, name, public) VALUES 
('audio-sessions', 'audio-sessions', true),
('user-avatars', 'user-avatars', false),
('program-images', 'program-images', true);

-- Set up storage policies
CREATE POLICY "Public audio access" ON storage.objects
FOR SELECT USING (bucket_id = 'audio-sessions');

CREATE POLICY "Users can upload avatars" ON storage.objects
FOR INSERT WITH CHECK (bucket_id = 'user-avatars');
```

---

## 🔧 Step 2: Environment Configuration

### 2.1 Create Production Environment File
Create `local.properties` (DO NOT commit to version control):

```properties
# Supabase Production Configuration
SUPABASE_URL=https://your-production-project-id.supabase.co
SUPABASE_ANON_KEY=your-production-anon-key
SUPABASE_SERVICE_KEY=your-production-service-key

# App Signing Configuration
RELEASE_STORE_FILE=release.keystore
RELEASE_STORE_PASSWORD=your-keystore-password
RELEASE_KEY_ALIAS=release
RELEASE_KEY_PASSWORD=your-key-password

# Environment
APP_ENV=production
DEBUG_MODE=false
LOG_LEVEL=error

# Feature Flags
ENABLE_CRISIS_DETECTION=true
ENABLE_ANALYTICS=true
ENABLE_PUSH_NOTIFICATIONS=true

# Monitoring (Optional)
SENTRY_DSN=your-sentry-dsn
FIREBASE_PROJECT_ID=your-firebase-project-id
```

### 2.2 Generate Release Keystore
```bash
# Generate keystore (run once)
keytool -genkey -v -keystore release.keystore -alias release -keyalg RSA -keysize 2048 -validity 10000

# Store keystore securely:
# - Add to .gitignore
# - Store password in secure location
# - Consider using environment variables in CI/CD
```

---

## 🔧 Step 3: Build Configuration

### 3.1 Update Build Configuration
The `androidApp/build.gradle.kts` is already configured for production with:
- ✅ Release signing configuration
- ✅ ProGuard optimization
- ✅ Build variants (debug, release, staging)
- ✅ Environment-based configs

### 3.2 Build Release APK/AAB
```bash
# Clean build
./gradlew clean

# Build release APK (for testing)
./gradlew assembleRelease

# Build release AAB (for Play Store)
./gradlew bundleRelease

# Verify build
./gradlew lintRelease
./gradlew testReleaseUnitTest
```

---

## 🔧 Step 4: Audio Content Setup

### 4.1 Upload Audio Files to Supabase Storage
```bash
# Upload meditation audio files to storage/audio-sessions bucket
# Use the seed data URLs as examples or upload your own:
# - https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3
# - Or upload your own meditation audio files
```

### 4.2 Update Session Audio URLs
```sql
-- Update sessions with your audio URLs
UPDATE sessions 
SET audio_url = 'https://your-storage-url/audio/meditation-1.mp3'
WHERE id = 'session_001';
```

### 4.3 Test Audio Playback
1. Build debug version first
2. Test audio playback functionality
3. Verify streaming works on different network conditions
4. Test background playback

---

## 🔧 Step 5: Play Store Preparation

### 5.1 Create Google Play Console Account
```bash
# 1. Go to play.google.com/console
# 2. Pay $25 developer fee
# 3. Complete developer identity verification
# 4. Set up payment profile
```

### 5.2 Create App Listing
1. **App Details**:
   - App name: "DrMindit - Mental Health Platform"
   - Package name: `com.drmindit.android`
   - App category: Health & Fitness
   - Content rating: Everyone

2. **Store Listing**:
   - Description: Comprehensive mental health platform with guided meditations, anxiety management, and crisis support
   - Screenshots: Add 8-10 high-quality screenshots
   - Feature graphic: 1024x500px promotional graphic

3. **Content Rating**:
   - Answer content rating questionnaire
   - Mental health information category
   - No age restrictions

### 5.3 Upload App Bundle
```bash
# Upload the generated AAB file
# Location: androidApp/build/outputs/bundle/release/androidApp-release.aab
```

### 5.4 Configure Release
1. **Release Track**: Start with Internal Testing
2. **Rollout**: 100% of internal testers
3. **Release Notes**: "Initial release with real backend integration"

---

## 🔧 Step 6: Testing & Validation

### 6.1 Pre-deployment Testing Checklist
```bash
# Functional Testing
□ Authentication flow (email/password, Google OAuth)
□ Audio playback functionality
□ Session progress tracking
□ Crisis detection system
□ Emergency helpline integration
□ Offline functionality
□ Push notifications (if enabled)

# Performance Testing
□ App startup time < 3 seconds
□ Audio buffering < 5 seconds
□ Memory usage < 200MB
□ Battery optimization

# Security Testing
□ API key security
□ Data encryption
□ Authentication security
□ Network security
```

### 6.2 Backend Testing
```sql
-- Test database connections
SELECT COUNT(*) FROM sessions;
SELECT COUNT(*) FROM programs;
SELECT COUNT(*) FROM emergency_helplines;

-- Test API endpoints
□ /rest/v1/sessions
□ /rest/v1/auth/v1/token
□ /rest/v1/profiles
□ /rest/v1/emergency_helplines
```

### 6.3 Audio Testing
```bash
# Test different audio formats
□ MP3 streaming
□ Background playback
□ Network interruption handling
□ Audio quality verification
□ Playback speed controls
```

---

## 🔧 Step 7: Monitoring & Analytics

### 7.1 Set Up Error Tracking
```kotlin
// In your Application class
if (BuildConfig.BUILD_TYPE == "release") {
    // Initialize Sentry or crashlytics
    Sentry.init { options ->
        options.dsn = BuildConfig.SENTRY_DSN
    }
}
```

### 7.2 Configure Analytics
```kotlin
// Track user engagement
□ Session completion rates
□ User retention metrics
□ Audio playback analytics
□ Crisis event monitoring
□ Performance metrics
```

### 7.3 Health Monitoring
```bash
# Monitor key metrics
□ API response times
□ Database performance
□ Audio streaming quality
□ User activity levels
□ Error rates
```

---

## 🔧 Step 8: Launch Process

### 8.1 Internal Testing (1-2 weeks)
1. Upload to Internal Testing track
2. Invite 10-20 internal testers
3. Collect feedback and fix issues
4. Verify all functionality works

### 8.2 Closed Testing (2-3 weeks)
1. Move to Closed Testing track
2. Invite 100-500 beta testers
3. Monitor performance and crashes
4. Gather user feedback
5. Optimize based on usage data

### 8.3 Open Testing (1-2 weeks)
1. Move to Open Testing track
2. Open to all users
3. Monitor server load
4. Scale resources if needed
5. Final bug fixes

### 8.4 Production Launch
1. Move to Production track
2. 100% rollout
3. Monitor closely for 48 hours
4. Be ready to rollback if needed
5. Announce launch

---

## 🔒 Security Checklist

### 9.1 Pre-launch Security Review
```bash
# API Security
□ API keys stored securely
□ Rate limiting enabled
□ Input validation implemented
□ SQL injection protection
□ XSS protection

# Data Security
□ User data encryption
□ Secure authentication
□ Session management
□ Data backup procedures

# Network Security
□ HTTPS enforcement
□ Certificate pinning (production)
□ Network monitoring
□ DDoS protection
```

### 9.2 Compliance
```bash
# Privacy Policy
□ Published privacy policy
□ Data handling disclosure
□ User consent mechanisms
□ GDPR compliance (if applicable)
□ HIPAA considerations (if applicable)
```

---

## 📞 Support & Maintenance

### 10.1 Launch Support Plan
```bash
# First 48 hours
□ 24/7 monitoring team
□ Emergency response procedures
□ User support channels
□ Server scaling readiness

# Ongoing Support
□ Regular security updates
□ Performance optimization
□ Feature enhancements
□ User feedback incorporation
```

### 10.2 Maintenance Schedule
```bash
# Daily
□ Monitor error rates
□ Check server performance
□ Review user feedback

# Weekly
□ Security updates
□ Performance optimization
□ Analytics review

# Monthly
□ Feature updates
□ Security audits
□ User satisfaction surveys
```

---

## 🚀 Launch Day Checklist

### Final Pre-launch Verification
```bash
□ All tests passed
□ Backend services running
□ Audio content uploaded
□ Emergency helplines verified
□ Crisis system tested
□ Performance optimized
□ Security measures in place
□ Support team ready
□ Monitoring configured
□ Rollback plan prepared
```

### Launch Steps
```bash
1. Final backup of all systems
2. Deploy production build
3. Start monitoring
4. Announce launch
5. Monitor user feedback
6. Address issues promptly
7. Celebrate successful launch! 🎉
```

---

## 📊 Post-launch Metrics

### Key Performance Indicators
```bash
# User Metrics
□ Daily active users
□ Session completion rates
□ User retention (7-day, 30-day)
□ App store ratings

# Technical Metrics
□ App crash rate (< 1%)
□ API response time (< 500ms)
□ Audio buffering time (< 3s)
□ Server uptime (> 99.9%)

# Business Metrics
□ User acquisition cost
□ Lifetime value
□ Conversion rates
□ Revenue (if premium features)
```

---

## 🆘 Emergency Procedures

### Critical Issues
```bash
# App Crashes
□ Immediate rollback
□ Hotfix deployment
□ User notification

# Backend Issues
□ Database failover
□ API service restoration
□ Data backup recovery

# Security Issues
□ Immediate investigation
□ Security patch deployment
□ User notification
```

### Contact Information
```bash
# Technical Lead: [Phone/Email]
# Backend Engineer: [Phone/Email]
# Product Manager: [Phone/Email]
# Support Team: [Phone/Email]
# Legal/Compliance: [Phone/Email]
```

---

## ✅ Success Criteria

### Launch Success Metrics
```bash
□ App launches without critical issues
□ Backend services stable for 48 hours
□ User feedback positive (> 4.0 rating)
□ Key features working correctly
□ Crisis system functional and tested
□ Audio playback working smoothly
□ Authentication flow working
□ Performance within acceptable limits
```

---

## 🎯 Next Steps

### Post-launch Roadmap
```bash
# Month 1-2
□ User feedback analysis
□ Bug fixes and optimizations
□ Performance improvements

# Month 3-4
□ New features based on feedback
□ Additional audio content
□ Enhanced analytics

# Month 5-6
□ Platform expansion (iOS)
□ Advanced features
□ Partnerships and integrations
```

---

**🎉 Congratulations! You've successfully transformed DrMindit from a UI prototype into a production-ready mental health platform with real backend integration, functional audio player, and Play Store release configuration!**

The app is now ready to help users with:
- Real meditation sessions with audio playback
- Crisis detection and emergency support
- Progress tracking and analytics
- Secure authentication and data management
- Professional-grade user experience

Good luck with your launch! 🚀
