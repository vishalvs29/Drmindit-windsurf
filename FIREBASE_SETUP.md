# Firebase Setup Guide

This guide explains how to set up Firebase for the DrMindit Android application.

## Prerequisites

1. **Firebase Project**: Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. **Android Package**: The app package is `com.drmindit.android`

## Setup Steps

### 1. Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project"
3. Enter project name (e.g., "drmindit-production")
4. Choose your preferred analytics location
5. Click "Create project"

### 2. Add Android App

1. In Firebase Console, click "Add app" → "Android"
2. Enter package name: `com.drmindit.android`
3. Download `google-services.json`
4. Place the file in `androidApp/src/` directory

### 3. Enable Required Services

#### Firebase Cloud Messaging (FCM)
1. Go to Project Settings → Cloud Messaging
2. Enable Cloud Messaging API
3. Note your Server Key and Sender ID

#### Authentication
1. Go to Authentication → Sign-in method
2. Enable Email/Password authentication
3. Configure email templates as needed

#### Analytics (Optional)
1. Go to Analytics → Set up
2. Enable Firebase Analytics

### 4. Configure Android App

1. Copy `google-services.json` to `androidApp/src/`
2. Ensure these dependencies are in `androidApp/build.gradle`:

```kotlin
// In your app-level build.gradle
implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
implementation("com.google.firebase:firebase-messaging-ktx")
implementation("com.google.firebase:firebase-analytics-ktx")
```

3. Add Firebase plugin to project-level build.gradle:

```kotlin
// In your project-level build.gradle
classpath("com.google.gms:google-services:4.4.0")
```

4. Apply plugin in app-level build.gradle:

```kotlin
apply(plugin = "com.google.gms.google-services")
```

### 5. Environment Variables

Add these to your environment or `.env` file:

```bash
# Firebase Configuration
FIREBASE_PROJECT_ID=your-project-id
FIREBASE_SERVER_KEY=your-server-key
FIREBASE_SENDER_ID=your-sender-id
```

## Production Deployment

### Security Checklist

- [ ] Replace placeholder values in `google-services.json`
- [ ] Set up proper API key restrictions
- [ ] Enable app signing in production
- [ ] Configure proper SHA-256 certificate fingerprints
- [ ] Test push notifications in staging

### Testing

1. **Push Notifications**: Test with Firebase Console → Cloud Messaging → Send test message
2. **Authentication**: Test user registration and login flows
3. **Analytics**: Verify events are being tracked

## Troubleshooting

### Common Issues

1. **"google-services.json not found"**
   - Ensure file is in `androidApp/src/` directory
   - Check file permissions

2. **Push notifications not working**
   - Verify FCM is enabled in Firebase Console
   - Check device token registration
   - Review notification permissions in AndroidManifest.xml

3. **Build errors**
   - Ensure all Firebase dependencies are correctly added
   - Check for version conflicts
   - Verify Gradle sync is successful

## Support

- [Firebase Documentation](https://firebase.google.com/docs)
- [Android Setup Guide](https://firebase.google.com/docs/android/setup)
- [FCM Documentation](https://firebase.google.com/docs/cloud-messaging)

## Next Steps

After Firebase setup:

1. Test all authentication flows
2. Verify push notification delivery
3. Set up analytics event tracking
4. Configure crash reporting (optional)
5. Set up remote config (optional)
