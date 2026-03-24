# DrMindit - Deployment Guide

## 🚀 Production Deployment Instructions

This guide covers deploying the DrMindit mental health platform with real Supabase backend integration and crisis escalation features.

---

## 📋 Prerequisites

### Required Accounts & Services
- **Supabase Account** - Database and authentication
- **Google Cloud Console** - OAuth configuration
- **Domain Name** - For production deployment
- **SSL Certificate** - For HTTPS

### Development Tools
- **Android Studio** - Latest version
- **Git** - Version control
- **Node.js** - For any web components
- **PostgreSQL Client** - For direct database access

---

## 🔧 Step 1: Supabase Setup

### 1.1 Create Project
1. Go to [supabase.com](https://supabase.com)
2. Create new project: `DrMindit-Production`
3. Choose region closest to your users
4. Generate database password (store securely)

### 1.2 Database Schema
```bash
# Apply the provided schema
psql -h db.swsqirdcmxotncibmgeb.supabase.co -U postgres -d postgres < supabase/schema.sql
```

### 1.3 Authentication Setup
1. Go to Authentication → Settings
2. Enable **Email/Password** auth
3. Enable **Google OAuth**:
   - Add Google OAuth provider
   - Configure redirect URLs:
     - Development: `http://localhost:3000/auth/callback`
     - Production: `https://drmindit.com/auth/callback`
4. Set site URL: `https://drmindit.com`

### 1.4 Storage Setup
1. Create storage buckets:
   - `audio-sessions` - For meditation audio files
   - `user-avatars` - For profile pictures
   - `program-images` - For program cover images

2. Set up storage policies:
   ```sql
   -- Audio files access
   CREATE POLICY "Users can upload audio" ON storage.objects
   FOR INSERT WITH CHECK (bucket_id = 'audio-sessions');

   -- Public read access for active sessions
   CREATE POLICY "Public audio access" ON storage.objects
   FOR SELECT USING (bucket_id = 'audio-sessions');
   ```

---

## 🔧 Step 2: Environment Configuration

### 2.1 Update Supabase Client
Replace placeholder values in `shared/src/commonMain/kotlin/com/drmindit/shared/data/supabase/SupabaseClient.kt`:

```kotlin
private const val SUPABASE_URL = "https://your-project-id.supabase.co"
private const val SUPABASE_ANON_KEY = "your-actual-anon-key"
```

### 2.2 Environment Variables
Create `.env.production`:

```bash
# Supabase Configuration
NEXT_PUBLIC_SUPABASE_URL=https://your-project-id.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=your-actual-anon-key
SUPABASE_SERVICE_ROLE_KEY=your-service-role-key

# App Configuration
APP_URL=https://drmindit.com
APP_NAME=DrMindit

# Security
JWT_SECRET=your-jwt-secret
SESSION_SECRET=your-session-secret

# Monitoring
SENTRY_DSN=your-sentry-dsn
LOG_LEVEL=warn
```

---

## 🔧 Step 3: Google OAuth Setup

### 3.1 Google Cloud Console
1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Create new project: `DrMindit`
3. Enable Google+ API and Google OAuth2 API

### 3.2 OAuth Credentials
1. Go to Credentials → Create Credentials → OAuth 2.0 Client IDs
2. Application type: **Web application**
3. Authorized redirect URIs:
   - Development: `http://localhost:3000/auth/callback`
   - Production: `https://drmindit.com/auth/callback`
4. Save Client ID and Client Secret

### 3.3 Supabase Google OAuth
1. In Supabase dashboard → Authentication → Providers
2. Enable Google provider
3. Add Client ID and Client Secret from Google Console
4. Save configuration

---

## 🔧 Step 4: Database Migration

### 4.1 Run Schema Migration
```bash
# Connect to your Supabase database
psql -h db.swsqirdcmxotncibmgeb.supabase.co -U postgres -d postgres

# Run the schema
\i supabase/schema.sql
```

### 4.2 Seed Initial Data
```sql
-- Insert sample sessions
INSERT INTO public.sessions (title, instructor, duration_minutes, audio_url, category, difficulty) VALUES
('Morning Meditation', 'Dr. Sarah Chen', 10, 'https://storage.googleapis.com/audio/morning-meditation.mp3', 'mindfulness', 'beginner'),
('Anxiety Relief', 'Prof. James Miller', 15, 'https://storage.googleapis.com/audio/anxiety-relief.mp3', 'anxiety', 'beginner'),
('Deep Sleep', 'Dr. Emily Brown', 20, 'https://storage.googleapis.com/audio/deep-sleep.mp3', 'sleep', 'beginner');

-- Insert sample programs
INSERT INTO public.programs (title, description, duration_days, category, difficulty) VALUES
('21-Day Anxiety Reset', 'Comprehensive program to reduce anxiety', 21, 'anxiety_reset', 'beginner'),
('Sleep Better Program', 'Improve your sleep quality in 3 weeks', 21, 'sleep_better', 'beginner');
```

---

## 🔧 Step 5: Android App Configuration

### 5.1 Update Build Configuration
Update `androidApp/build.gradle.kts`:

```kotlin
android {
    defaultConfig {
        applicationId = "com.drmindit.android"
        versionCode = 1
        versionName = "1.0.0"
        minSdk = 24
        targetSdk = 34
        
        // Build config fields
        buildConfigField("String", "SUPABASE_URL", "\"https://your-project-id.supabase.co\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"your-actual-anon-key\"")
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

### 5.2 Update AndroidManifest.xml
```xml
<application
    android:name=".DrMinditApplication"
    android:label="@string/app_name"
    android:theme="@style/Theme.DrMindit"
    android:usesCleartextTraffic="false">
    
    <!-- Production configuration -->
    <meta-data android:name="supabase_url" android:value="https://your-project-id.supabase.co" />
    <meta-data android:name="supabase_anon_key" android:value="your-actual-anon-key" />
</application>
```

---

## 🔧 Step 6: Crisis System Configuration

### 6.1 Emergency Helplines
Update helplines in the database:

```sql
-- Update with production helplines
INSERT INTO public.emergency_helplines (name, phone, description, country, priority) VALUES
('National Suicide Prevention Lifeline', '988', '24/7 crisis support', 'US', 1),
('Crisis Text Line', 'Text HOME to 741741', '24/7 crisis support via text', 'US', 2);
```

### 6.2 Crisis Detection Configuration
Update crisis keywords and thresholds in `CrisisRepository.kt`:

```kotlin
private val suicidalKeywords = setOf(
    "suicidal", "want to die", "kill myself", "end my life",
    "suicide", "no reason to live", "better off dead",
    "hopeless", "worthless", "burden", "give up"
)

private val CRISIS_MOOD_THRESHOLD = 2
private val REPEATED_NEGATIVE_LOGS_THRESHOLD = 5
private val NEGATIVE_LOGS_DAYS_WINDOW = 7
```

---

## 🚀 Step 7: Build & Deploy

### 7.1 Build Android App
```bash
# Clean build
./gradlew clean

# Build release APK
./gradlew assembleRelease

# Build release AAB (recommended for Play Store)
./gradlew bundleRelease
```

### 7.2 Upload to Google Play Store
1. Go to [Google Play Console](https://play.google.com/console)
2. Create new app: `DrMindit - Mental Health Platform`
3. Upload AAB file
4. Complete store listing:
   - App description
   - Screenshots
   - Content rating: Mental health
   - Privacy policy URL
   - Target audience: Teens and adults

### 7.3 Configure App Content
1. **Content Rating**: Mental health information
2. **Privacy Policy**: Include crisis handling procedures
3. **Data Safety**: Declare data collection practices
4. **Target Audience**: Ages 13+

---

## 🔒 Step 8: Security & Monitoring

### 8.1 Security Checklist
- [ ] Supabase RLS policies enabled
- [ ] API keys secured (no hardcoded values)
- [ ] HTTPS enforced
- [ ] Input validation implemented
- [ ] User data encryption
- [ ] Crisis data protected

### 8.2 Monitoring Setup
```bash
# Set up error tracking
# Add Sentry DSN to environment variables
SENTRY_DSN=https://your-sentry-dsn.ingest.sentry.io/project-id

# Configure logging
LOG_LEVEL=warn
```

### 8.3 Database Monitoring
1. Enable Supabase database logs
2. Set up query performance monitoring
3. Configure backup retention
4. Monitor crisis events table

---

## 🧪 Step 9: Testing

### 9.1 Pre-deployment Testing
```bash
# Run unit tests
./gradlew test

# Run integration tests
./gradlew connectedAndroidTest

# Manual testing checklist
- [ ] Authentication flow works
- [ ] Crisis detection triggers correctly
- [ ] Emergency helplines accessible
- [ ] Audio playback functional
- [ ] Data persistence works
- [ ] Error handling graceful
```

### 9.2 Crisis System Testing
Test all crisis triggers:
1. **Low mood score** (≤ 2/10)
2. **Suicidal keywords** in notes
3. **Repeated negative logs** (5+ in 7 days)
4. **Manual trigger** button
5. **AI detection** of distress

### 9.3 Emergency Helpline Testing
1. Verify all phone numbers are correct
2. Test click-to-call functionality
3. Confirm 24/7 availability
4. Test website links
5. Verify country-specific helplines

---

## 📊 Step 10: Post-deployment

### 10.1 Analytics Setup
1. Configure user analytics tracking
2. Set up crisis event monitoring
3. Monitor session completion rates
4. Track helpline usage

### 10.2 Performance Monitoring
1. Set up APM monitoring
2. Monitor API response times
3. Track error rates
4. Monitor database performance

### 10.3 User Support
1. Set up support email
2. Create crisis response protocol
3. Train support team
4. Establish escalation procedures

---

## 🚨 Crisis Response Protocol

### Immediate Actions
1. **High-priority alerts** for critical crisis events
2. **Automatic notifications** to designated responders
3. **Follow-up procedures** within 24 hours
4. **Documentation** of all crisis events

### Monitoring Dashboard
1. Real-time crisis event tracking
2. User risk status monitoring
3. Helpline usage analytics
4. Response time tracking

---

## 📞 Emergency Contacts

### Production Support
- **Technical Lead**: [Phone number]
- **Crisis Response Team**: [Phone number]
- **Database Admin**: [Phone number]
- **Legal/Compliance**: [Phone number]

### Backup Contacts
- **Supabase Support**: [Support portal]
- **Google Play Support**: [Support portal]
- **Hosting Provider**: [Support portal]

---

## 🔄 Maintenance

### Regular Tasks
- **Daily**: Monitor crisis events, check system health
- **Weekly**: Review analytics, update helplines
- **Monthly**: Security audit, performance review
- **Quarterly**: Update crisis protocols, user feedback review

### Update Procedures
1. **Database migrations** with version control
2. **App updates** through Play Store
3. **Crisis protocol updates** with team training
4. **Security patches** applied immediately

---

## ✅ Deployment Checklist

Before going live, verify:

### Security
- [ ] All API keys secured
- [ ] HTTPS enforced
- [ ] RLS policies enabled
- [ ] Input validation active
- [ ] Crisis data protected

### Functionality
- [ ] Authentication works
- [ ] Crisis detection functional
- [ ] Helplines accessible
- [ ] Audio playback works
- [ ] Data persists correctly

### Crisis System
- [ ] All triggers tested
- [ ] Emergency contacts verified
- [ ] Response protocol documented
- [ ] Monitoring active
- [ ] Support team trained

### Compliance
- [ ] Privacy policy published
- [ ] Terms of service updated
- [ ] Data safety declaration complete
- [ ] Content rating appropriate

---

## 🚀 Go Live!

Once all checks pass:
1. **Publish app** to Google Play Store
2. **Monitor initial users** closely
3. **Have crisis team on standby**
4. **Document launch** for post-mortem

Welcome to production! 🎉

---

## 📞 Support

For deployment issues:
- **Technical Support**: [Email/Phone]
- **Crisis Support**: [Emergency protocol]
- **Documentation**: [Link to internal docs]
- **Status Page**: [Link to status page]

Remember: User safety is the highest priority. When in doubt, err on the side of caution.
