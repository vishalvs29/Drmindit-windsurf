# DrMindit Supabase Migration Guide

## Overview

Complete migration from custom backend to pure Supabase architecture.

## New Supabase Project

- **Project URL**: https://supabase.com/dashboard/project/nlheesoshtczdhsqzjid
- **Database**: PostgreSQL with RLS enabled
- **Authentication**: Supabase Auth
- **Storage**: Supabase Storage (for audio files)

## Migration Steps

### 1. Database Schema Setup

Execute the schema in Supabase SQL Editor:

```sql
-- Run: supabase/new_schema.sql
```

### 2. Sample Data Insertion

```sql
-- Run: supabase/sample_data.sql
```

### 3. Get Supabase Credentials

From your Supabase dashboard:

1. Go to Project Settings > API
2. Copy:
   - **Project URL**: `https://nlheesoshtczdhsqzjid.supabase.co`
   - **Anon Key**: `your-anon-key-here`
   - **Service Role Key**: `your-service-key-here`

### 4. Update Local Configuration

Update `local.properties`:

```properties
SUPABASE_URL=https://nlheesoshtczdhsqzjid.supabase.co
SUPABASE_ANON_KEY=your-actual-anon-key
SUPABASE_SERVICE_KEY=your-actual-service-key
```

### 5. Android App Configuration

The app now uses direct Supabase client:

- **No custom backend required**
- **Direct database access**
- **Built-in authentication**
- **RLS for security**

## Architecture

### Before (Custom Backend)
```
Kotlin App -> Custom Node.js Backend -> Database
```

### After (Supabase Native)
```
Kotlin App -> Supabase (Direct)
```

## Database Tables

### Core Tables

1. **sessions** - Meditation/audio content
2. **session_steps** - Individual audio steps
3. **user_session_progress** - User progress tracking
4. **profiles** - User profiles (extends auth.users)
5. **user_preferences** - User settings
6. **user_mood_ratings** - Mood analytics
7. **session_events** - Analytics events

### RLS Policies

- **sessions**: Public read access
- **session_steps**: Public read access
- **user_session_progress**: User's own data only
- **profiles**: User's own data only
- **user_preferences**: User's own data only
- **user_mood_ratings**: User's own data only
- **session_events**: User's own data only

## API Usage Examples

### Fetch Sessions
```kotlin
val repository = SupabaseSessionRepository()
val sessions = repository.getSessions()
```

### Get Session Steps
```kotlin
val steps = repository.getSessionSteps(sessionId)
```

### Update Progress
```kotlin
val progress = UserSessionProgress(...)
repository.updateSessionProgress(progress)
```

## Audio Storage

### Option 1: External CDN
Keep using existing CDN URLs in database.

### Option 2: Supabase Storage
1. Create storage bucket: `audio-files`
2. Upload audio files
3. Update audio_url fields with storage URLs

## Authentication Flow

### 1. User Sign Up
```kotlin
val auth = SupabaseClient.auth()
auth.signUpWith(email, password)
```

### 2. User Login
```kotlin
auth.signInWith(email, password)
```

### 3. Get Current User
```kotlin
val user = auth.currentUserOrNull()
```

## Security Features

### Row Level Security (RLS)
- Users can only access their own data
- Public content is publicly readable
- Automatic user ID filtering

### Data Validation
- Database constraints
- Check constraints for mood scores
- Foreign key relationships

## Testing

### 1. Database Connection
```kotlin
// Test basic database access
val sessions = SupabaseSessionRepository().getSessions()
```

### 2. Authentication
```kotlin
// Test user authentication
val auth = SupabaseClient.auth()
val user = auth.currentUserOrNull()
```

### 3. Progress Tracking
```kotlin
// Test progress updates
val progress = UserSessionProgress(...)
val result = repository.updateSessionProgress(progress)
```

## Deployment

### 1. Update Build Configuration
```kotlin
// build.gradle.kts already configured
buildConfigField("String", "SUPABASE_URL", "\"${System.getenv("SUPABASE_URL")}\"")
buildConfigField("String", "SUPABASE_ANON_KEY", "\"${System.getenv("SUPABASE_ANON_KEY")}\"")
```

### 2. Environment Variables
Set in your CI/CD or build environment:
```bash
SUPABASE_URL=https://nlheesoshtczdhsqzjid.supabase.co
SUPABASE_ANON_KEY=your-anon-key
```

## Benefits

### Simplified Architecture
- No backend server to maintain
- Direct database access
- Built-in authentication
- Automatic scaling

### Better Security
- Row Level Security
- No custom authentication code
- Built-in user management
- Secure by default

### Cost Effective
- No server costs
- Pay per use
- Automatic scaling
- Managed infrastructure

## Migration Checklist

- [ ] Execute schema in Supabase SQL Editor
- [ ] Insert sample data
- [ ] Get Supabase credentials
- [ ] Update local.properties
- [ ] Test database connection
- [ ] Test authentication
- [ ] Test session fetching
- [ ] Test progress tracking
- [ ] Test audio playback
- [ ] Deploy to production

## Troubleshooting

### Common Issues

1. **Authentication Errors**
   - Check anon key is correct
   - Ensure user is logged in

2. **Database Access Errors**
   - Verify RLS policies
   - Check user permissions

3. **Network Issues**
   - Check Supabase URL
   - Verify internet connection

4. **Build Errors**
   - Clean and rebuild project
   - Check dependencies

### Debug Tips

```kotlin
// Enable logging in development
if (BuildConfig.DEBUG_MODE) {
    // Add debug logging
}
```

## Support

- Supabase Documentation: https://supabase.com/docs
- Android Client: https://github.com/supabase-community/supabase-kt
- Project Dashboard: https://supabase.com/dashboard/project/nlheesoshtczdhsqzjid
