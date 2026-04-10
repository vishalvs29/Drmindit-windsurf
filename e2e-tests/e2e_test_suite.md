# DrMindit End-to-End Test Suite

## Overview
Comprehensive E2E testing for DrMindit application covering complete user flows from Kotlin app through Supabase backend.

## Test Environment Setup

### Prerequisites
- Supabase project deployed
- Kotlin app with Supabase integration
- Test data loaded
- Android emulator or device
- Test credentials ready

### Environment Configuration
```bash
# Supabase Test Environment
SUPABASE_URL=https://your-test-project.supabase.co
SUPABASE_ANON_KEY=your-test-anon-key
SUPABASE_SERVICE_KEY=your-test-service-key

# Test User Credentials
TEST_EMAIL=testuser@drmindit.com
TEST_PASSWORD=TestPassword123!
```

---

## Test Scenarios

### 1. User Authentication Flow

#### 1.1 User Registration
**Objective**: Test new user registration and profile creation

**Steps**:
1. Launch app
2. Click "Sign Up"
3. Enter test email and password
4. Submit registration
5. Verify email confirmation (if required)
6. Login with new credentials
7. Verify profile created in database

**Expected Results**:
- Registration successful
- User can login
- Profile created in Supabase
- JWT token received
- User redirected to home screen

**Validation**:
```sql
-- Verify user created
SELECT id, email, created_at FROM auth.users WHERE email = 'testuser@drmindit.com';

-- Verify profile created
SELECT id, first_name, last_name FROM public.profiles WHERE id = (user_id);
```

#### 1.2 User Login
**Objective**: Test existing user login flow

**Steps**:
1. Launch app
2. Click "Login"
3. Enter existing credentials
4. Submit login
5. Verify successful authentication

**Expected Results**:
- Login successful
- JWT token received
- User session established
- App loads user data

#### 1.3 User Logout
**Objective**: Test user logout and session cleanup

**Steps**:
1. User is logged in
2. Click logout
3. Verify session cleared
4. Verify redirected to login screen

**Expected Results**:
- JWT token cleared
- User session ended
- App returns to login screen

---

### 2. Session Discovery Flow

#### 2.1 Browse All Sessions
**Objective**: Test session browsing functionality

**Steps**:
1. User is logged in
2. Navigate to "Explore" tab
3. Verify sessions load
4. Check session data integrity
5. Test pagination/scrolling

**Expected Results**:
- All sessions displayed
- Session data complete (title, duration, category)
- Images load properly
- Categories filter works

**Validation**:
```sql
-- Verify sessions returned
SELECT COUNT(*) as total_sessions FROM public.sessions WHERE is_active = true;

-- Verify session data
SELECT title, total_duration, category, rating FROM public.sessions WHERE is_active = true LIMIT 5;
```

#### 2.2 Filter Sessions by Category
**Objective**: Test category filtering

**Steps**:
1. Navigate to "Explore" tab
2. Select "Meditation" category
3. Verify filtered results
4. Test other categories
5. Verify "All" category shows all sessions

**Expected Results**:
- Filter works correctly
- Only sessions from selected category shown
- Category count accurate

#### 2.3 Search Sessions
**Objective**: Test session search functionality

**Steps**:
1. Navigate to "Explore" tab
2. Enter search term (e.g., "stress")
3. Verify search results
4. Test different search terms
5. Verify no results handling

**Expected Results**:
- Search returns relevant sessions
- Search terms highlighted
- No results message shown when appropriate

---

### 3. Session Playback Flow

#### 3.1 Start Session
**Objective**: Test session initiation

**Steps**:
1. Select a session from explore
2. Click "Start Session"
3. Verify session detail screen loads
4. Check session steps loaded
5. Verify audio player ready

**Expected Results**:
- Session details displayed
- All steps loaded in order
- Audio player initialized
- Progress tracking starts

**Validation**:
```sql
-- Verify session steps
SELECT id, title, duration, order_index FROM public.session_steps WHERE session_id = 'session_id' ORDER BY order_index;

-- Verify progress tracking
SELECT * FROM public.user_session_progress WHERE user_id = 'user_id' AND session_id = 'session_id';
```

#### 3.2 Play Audio Steps
**Objective**: Test audio playback functionality

**Steps**:
1. Start session
2. Play first audio step
3. Verify audio plays
4. Test pause/resume
5. Test seek functionality
6. Play next step automatically
7. Complete all steps

**Expected Results**:
- Audio plays smoothly
- Pause/resume works
- Seek functionality works
- Steps progress automatically
- Audio URLs are valid

#### 3.3 Progress Tracking
**Objective**: Test real-time progress tracking

**Steps**:
1. Start session
2. Play through steps
3. Monitor progress updates
4. Verify progress saved to database
5. Test resume functionality

**Expected Results**:
- Progress updates in real-time
- Progress saved to database
- Resume works from last position
- Completion status updated

**Validation**:
```sql
-- Check progress updates
SELECT current_step_index, progress_seconds, is_completed 
FROM public.user_session_progress 
WHERE user_id = 'user_id' AND session_id = 'session_id';
```

---

### 4. User Profile Flow

#### 4.1 View Profile
**Objective**: Test profile viewing

**Steps**:
1. User is logged in
2. Navigate to profile section
3. Verify profile data displayed
4. Check statistics accuracy

**Expected Results**:
- Profile information displayed
- Statistics accurate
- Avatar displayed if set

#### 4.2 Update Profile
**Objective**: Test profile updates

**Steps**:
1. Navigate to profile
2. Click "Edit Profile"
3. Update name/avatar
4. Save changes
5. Verify updates reflected

**Expected Results**:
- Profile updates successfully
- Changes reflected in UI
- Database updated

**Validation**:
```sql
-- Verify profile updates
SELECT first_name, last_name, avatar_url FROM public.profiles WHERE id = 'user_id';
```

---

### 5. Analytics Flow

#### 5.1 Session Analytics
**Objective**: Test session completion analytics

**Steps**:
1. Complete a session
2. Navigate to analytics
3. Verify session counted
4. Check completion time
5. Verify mood ratings

**Expected Results**:
- Session appears in analytics
- Completion time accurate
- Mood ratings recorded

**Validation**:
```sql
-- Check analytics data
SELECT COUNT(*) as completed_sessions FROM public.user_session_progress WHERE user_id = 'user_id' AND is_completed = true;

-- Check mood ratings
SELECT mood_before, mood_after FROM public.user_mood_ratings WHERE user_id = 'user_id';
```

#### 5.2 Progress Statistics
**Objective**: Test progress statistics

**Steps**:
1. Complete multiple sessions
2. Navigate to progress section
3. Verify statistics accuracy
4. Check streaks and achievements

**Expected Results**:
- Statistics accurate
- Streaks calculated correctly
- Achievements unlocked appropriately

---

### 6. Offline Flow

#### 6.1 Offline Session Access
**Objective**: Test offline functionality

**Steps**:
1. Download session for offline
2. Disable network
3. Access downloaded session
4. Verify offline playback
5. Test progress sync when online

**Expected Results**:
- Downloaded sessions work offline
- Progress saved locally
- Sync works when online
- Graceful handling of network issues

---

### 7. Error Handling Flow

#### 7.1 Network Errors
**Objective**: Test network error handling

**Steps**:
1. Disable network
2. Try to load sessions
3. Verify error message
4. Test retry functionality
5. Restore network
6. Verify recovery

**Expected Results**:
- Clear error messages
- Retry functionality works
- Automatic recovery
- No app crashes

#### 7.2 Authentication Errors
**Objective**: Test auth error handling

**Steps**:
1. Use invalid credentials
2. Verify error handling
3. Test token expiry
4. Verify re-authentication

**Expected Results**:
- Clear error messages
- Token refresh works
- Re-authentication prompt
- Graceful error handling

---

### 8. Performance Flow

#### 8.1 Load Performance
**Objective**: Test app loading performance

**Steps**:
1. Launch app cold
2. Measure load times
3. Test with large datasets
4. Verify memory usage

**Expected Results**:
- App loads within 3 seconds
- Memory usage reasonable
- Smooth scrolling
- No lag

#### 8.2 Database Performance
**Objective**: Test database query performance

**Steps**:
1. Load all sessions
2. Search sessions
3. Filter categories
4. Update progress
5. Measure response times

**Expected Results**:
- Queries under 200ms
- Smooth filtering
- Real-time updates
- Efficient caching

---

## Test Automation

### Automated Test Scripts

#### 1. Setup Script
```bash
#!/bin/bash
# setup_e2e_tests.sh

# Clean test environment
./gradlew clean

# Install dependencies
npm install

# Setup test database
psql $DATABASE_URL -f supabase/production_schema.sql
psql $DATABASE_URL -f supabase/production_sample_data.sql

# Create test user
curl -X POST "$SUPABASE_URL/auth/v1/signup" \
  -H "apikey: $SUPABASE_ANON_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@drmindit.com",
    "password": "TestPassword123!",
    "data": {
      "first_name": "Test",
      "last_name": "User"
    }
  }'

echo "E2E test environment setup complete"
```

#### 2. Test Runner
```bash
#!/bin/bash
# run_e2e_tests.sh

# Start Supabase local if needed
supabase start

# Run Android tests
./gradlew :androidApp:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=DrMinditE2ETestSuite

# Generate test report
./gradlew :androidApp:connectedAndroidTestReport

# Cleanup
supabase stop

echo "E2E tests completed"
```

---

## Test Data Management

### Test Data Setup
```sql
-- Create test user
INSERT INTO auth.users (email, email_confirmed_at, phone, phone_confirmed_at, raw_user_meta_data, created_at, updated_at)
VALUES ('testuser@drmindit.com', NOW(), NULL, NULL, '{"first_name": "Test", "last_name": "User"}', NOW(), NOW());

-- Create test profile
INSERT INTO public.profiles (id, first_name, last_name, subscription_level)
VALUES ((SELECT id FROM auth.users WHERE email = 'testuser@drmindit.com'), 'Test', 'User', 'premium');

-- Create test progress
INSERT INTO public.user_session_progress (user_id, session_id, current_step_index, progress_seconds, is_completed)
VALUES ((SELECT id FROM auth.users WHERE email = 'testuser@drmindit.com'), 'test-session-id', 0, 0, false);
```

### Test Data Cleanup
```sql
-- Clean up test data
DELETE FROM public.user_session_progress WHERE user_id = (SELECT id FROM auth.users WHERE email = 'testuser@drmindit.com');
DELETE FROM public.profiles WHERE id = (SELECT id FROM auth.users WHERE email = 'testuser@drmindit.com');
DELETE FROM auth.users WHERE email = 'testuser@drmindit.com';
```

---

## Test Results Documentation

### Test Report Template
```markdown
# E2E Test Results - [Date]

## Test Environment
- Device: [Device Name]
- OS Version: [OS Version]
- App Version: [App Version]
- Supabase Project: [Project Name]

## Test Results Summary
- Total Tests: [Number]
- Passed: [Number]
- Failed: [Number]
- Skipped: [Number]
- Success Rate: [Percentage]%

## Detailed Results
### Authentication Tests
- User Registration: [PASS/FAIL]
- User Login: [PASS/FAIL]
- User Logout: [PASS/FAIL]

### Session Tests
- Browse Sessions: [PASS/FAIL]
- Filter Sessions: [PASS/FAIL]
- Search Sessions: [PASS/FAIL]
- Start Session: [PASS/FAIL]
- Play Audio: [PASS/FAIL]
- Progress Tracking: [PASS/FAIL]

### Profile Tests
- View Profile: [PASS/FAIL]
- Update Profile: [PASS/FAIL]

### Analytics Tests
- Session Analytics: [PASS/FAIL]
- Progress Statistics: [PASS/FAIL]

### Performance Tests
- Load Performance: [PASS/FAIL]
- Database Performance: [PASS/FAIL]

## Issues Found
### Critical Issues
- [Issue Description]
- [Steps to Reproduce]
- [Expected vs Actual]

### Minor Issues
- [Issue Description]
- [Steps to Reproduce]
- [Expected vs Actual]

## Recommendations
- [Recommendation 1]
- [Recommendation 2]
- [Recommendation 3]
```

---

## Continuous Integration

### GitHub Actions Workflow
```yaml
name: E2E Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]
  schedule:
    - cron: '0 2 * * *' # Daily at 2 AM

jobs:
  e2e-tests:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Setup Node.js
      uses: actions/setup-node@v3
      with:
        node-version: '18'
        
    - name: Setup Java
      uses: actions/setup-java@v3
      with:
        distribution: 'temurin'
        java-version: '11'
        
    - name: Start Supabase
      run: |
        npx supabase start
        
    - name: Setup Test Environment
      run: |
        chmod +x e2e-tests/setup_e2e_tests.sh
        ./e2e-tests/setup_e2e_tests.sh
        
    - name: Run E2E Tests
      run: |
        chmod +x e2e-tests/run_e2e_tests.sh
        ./e2e-tests/run_e2e_tests.sh
        
    - name: Upload Test Results
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: e2e-test-results
        path: androidApp/build/reports/androidTests/
        
    - name: Stop Supabase
      run: npx supabase stop
```

---

## Best Practices

### Test Design Principles
1. **Independent Tests**: Each test should be independent
2. **Repeatable**: Tests should produce same results
3. **Clear Assertions**: Explicit expected vs actual
4. **Proper Cleanup**: Clean up test data
5. **Error Scenarios**: Test both success and failure cases

### Test Data Management
1. **Isolated Environment**: Separate test database
2. **Consistent Data**: Same test data for each run
3. **Cleanup Strategy**: Clean up after each test
4. **Data Validation**: Verify data integrity

### Performance Considerations
1. **Test Parallelization**: Run tests in parallel
2. **Resource Management**: Proper resource cleanup
3. **Timeout Handling**: Appropriate timeouts
4. **Memory Management**: Monitor memory usage

---

## Conclusion

This comprehensive E2E test suite ensures the DrMindit application works end-to-end, providing confidence in:

- User authentication flows
- Session discovery and playback
- Progress tracking
- Analytics and reporting
- Error handling
- Performance optimization
- Offline functionality

Execute these tests regularly to maintain application quality and reliability.
