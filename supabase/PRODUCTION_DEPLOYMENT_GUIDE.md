# DrMindit Production Deployment Guide
# Supabase Backend Production Setup

## Overview

This guide provides step-by-step instructions for deploying the DrMindit Supabase backend to production environment.

## Pre-Deployment Checklist

- [ ] Supabase project created and configured
- [ ] Database schema reviewed and tested
- [ ] Security policies validated
- [ ] Performance indexes added
- [ ] Storage buckets configured
- [ ] Kotlin app integration tested
- [ ] Backup strategy defined
- [ ] Monitoring setup planned

## Step 1: Supabase Project Setup

### 1.1 Create Production Project

1. Go to [Supabase Dashboard](https://supabase.com/dashboard)
2. Click "New Project"
3. Select organization
4. Configure project settings:
   - **Project Name**: `drmindit-production`
   - **Database Password**: Generate strong password
   - **Region**: Choose closest to your users
   - **Pricing Tier**: Start with Pro tier for production

### 1.2 Configure Database

1. Navigate to **Settings** > **Database**
2. Configure connection pooling:
   - **Pool Size**: 20 (adjust based on load)
   - **Timeout**: 30 seconds
3. Enable **Point-in-Time Recovery** (if needed)
4. Set up **Database Backups**:
   - Daily backups enabled
   - Point-in-time recovery: 7 days

### 1.3 API Settings

1. Navigate to **Settings** > **API**
2. Configure **JWT Settings**:
   - **JWT Expiry**: 1 hour (adjust as needed)
   - **Refresh Token Expiry**: 30 days
3. Enable **Rate Limiting**:
   - **Requests per minute**: 100
   - **Burst size**: 200

## Step 2: Database Deployment

### 2.1 Execute Schema

1. Navigate to **SQL Editor**
2. Execute `production_schema.sql`:
   ```sql
   -- Copy and paste the entire schema
   -- Execute step by step to monitor for errors
   ```

### 2.2 Insert Sample Data

1. Execute `production_sample_data.sql`
2. Verify data insertion:
   ```sql
   SELECT COUNT(*) FROM sessions;
   SELECT COUNT(*) FROM session_steps;
   ```

### 2.3 Configure Storage

1. Execute `storage_setup.sql`
2. Create storage buckets via dashboard:
   - **sessions-audio**: Public bucket for audio files
   - **user-avatars**: Private bucket for user avatars
   - **session-thumbnails**: Public bucket for thumbnails

### 2.4 Upload Audio Files

1. Using Supabase Storage UI or SDK:
   ```
   sessions-audio/public/morning-mindfulness/
   sessions-audio/public/evening-wind-down/
   sessions-audio/public/focus-enhancement/
   ```

2. Set proper permissions:
   - Public read access for audio files
   - Signed URLs for premium content if needed

## Step 3: Security Configuration

### 3.1 Row Level Security

Verify RLS policies are active:
```sql
SELECT schemaname, tablename, rowsecurity 
FROM pg_tables 
WHERE schemaname = 'public';
```

### 3.2 API Keys Management

1. **Anon Key**: Used in Kotlin app
2. **Service Role Key**: Server-side operations only
3. **Environment Variables**:
   ```bash
   SUPABASE_URL=https://your-project.supabase.co
   SUPABASE_ANON_KEY=your-anon-key
   SUPABASE_SERVICE_KEY=your-service-key
   ```

### 3.3 CORS Configuration

1. Navigate to **Settings** > **API**
2. Add your app domains to CORS:
   - `https://yourapp.com`
   - `https://www.yourapp.com`
   - `app://your-android-app` (for deep links)

## Step 4: Performance Optimization

### 4.1 Database Indexes

Verify indexes are created:
```sql
SELECT indexname, tablename 
FROM pg_indexes 
WHERE schemaname = 'public';
```

### 4.2 Query Performance

Monitor slow queries:
```sql
SELECT query, mean_time, calls 
FROM pg_stat_statements 
ORDER BY mean_time DESC 
LIMIT 10;
```

### 4.3 Connection Pooling

Monitor connection pool usage:
```sql
SELECT state, count(*) 
FROM pg_stat_activity 
GROUP BY state;
```

## Step 5: Monitoring Setup

### 5.1 Supabase Monitoring

1. Navigate to **Logs** > **Database Logs**
2. Set up log monitoring:
   - Error logs
   - Slow query logs
   - Connection logs

### 5.2 Custom Monitoring

Set up monitoring for:
- Database connection count
- Query performance
- Storage usage
- API request count
- Error rates

### 5.3 Alert Configuration

Configure alerts for:
- Database connection failures
- High error rates
- Storage capacity limits
- Unusual activity patterns

## Step 6: Kotlin App Configuration

### 6.1 Update App Configuration

```kotlin
// Update SupabaseClient.kt with production URL
supabaseUrl = "https://your-production-project.supabase.co"
supabaseKey = "your-production-anon-key"
```

### 6.2 Build Production APK

```bash
./gradlew assembleRelease
./gradlew bundleRelease
```

### 6.3 Test Integration

1. Install production build
2. Test all critical flows:
   - User authentication
   - Session loading
   - Progress tracking
   - Audio playback
   - Error handling

## Step 7: Backup Strategy

### 7.1 Database Backups

- **Daily automated backups** (enabled by default)
- **Point-in-time recovery** (7 days)
- **Manual backups** before major updates

### 7.2 Storage Backups

- **Audio files**: Use Supabase Storage replication
- **User avatars**: Implement client-side backup
- **Critical data**: Export to external storage

### 7.3 Recovery Testing

Test recovery procedures quarterly:
1. Restore from backup
2. Verify data integrity
3. Test application functionality

## Step 8: Security Hardening

### 8.1 Access Control

1. **Database Access**:
   - Limit to necessary IPs
   - Use SSL connections only
   - Implement connection pooling

2. **API Access**:
   - Use JWT tokens
   - Implement rate limiting
   - Monitor API usage

### 8.2 Data Protection

1. **Encryption**:
   - Data encrypted at rest
   - Data encrypted in transit
   - Sensitive data masked in logs

2. **Privacy Compliance**:
   - GDPR compliance
   - Data retention policies
   - User data deletion

## Step 9: Scaling Strategy

### 9.1 Database Scaling

- **Read Replicas**: For read-heavy operations
- **Connection Pooling**: Optimize connection usage
- **Query Optimization**: Continuously optimize slow queries

### 9.2 Storage Scaling

- **CDN Integration**: Use Supabase CDN
- **File Compression**: Compress audio files
- **Lazy Loading**: Load content as needed

### 9.3 App Scaling

- **Caching**: Implement client-side caching
- **Background Sync**: Sync data in background
- **Offline Support**: Support offline usage

## Step 10: Maintenance

### 10.1 Regular Tasks

- **Weekly**: Review logs and performance
- **Monthly**: Update dependencies
- **Quarterly**: Security audit
- **Yearly**: Architecture review

### 10.2 Update Procedures

1. **Test updates** in staging environment
2. **Backup production** before updates
3. **Rollback plan** prepared
4. **Monitor** after deployment

### 10.3 Documentation

- Maintain up-to-date documentation
- Document all changes
- Train team on procedures

## Production Verification Checklist

### Database
- [ ] Schema deployed correctly
- [ ] RLS policies active
- [ ] Indexes created
- [ ] Sample data inserted
- [ ] Backup strategy implemented

### Storage
- [ ] Buckets created
- [ ] Audio files uploaded
- [ ] Permissions set correctly
- [ ] CDN configured

### Security
- [ ] API keys configured
- [ ] CORS settings updated
- [ ] Rate limiting enabled
- [ ] Monitoring active

### Application
- [ ] Kotlin app updated
- [ ] Authentication working
- [ ] Sessions loading
- [ ] Progress tracking
- [ ] Error handling

### Performance
- [ ] Query performance optimized
- [ ] Connection pooling configured
- [ ] Caching implemented
- [ ] Monitoring setup

## Troubleshooting

### Common Issues

1. **Connection Errors**
   - Check network connectivity
   - Verify API keys
   - Review CORS settings

2. **Performance Issues**
   - Check query performance
   - Monitor connection pool
   - Review indexing

3. **Security Issues**
   - Review RLS policies
   - Check API permissions
   - Audit access logs

### Emergency Procedures

1. **Database Down**
   - Check Supabase status
   - Monitor error logs
   - Implement failover

2. **Security Breach**
   - Rotate API keys
   - Review access logs
   - Notify users

3. **Data Loss**
   - Restore from backup
   - Investigate cause
   - Implement prevention

## Support Contacts

- **Supabase Support**: https://supabase.com/support
- **Documentation**: https://supabase.com/docs
- **Community**: https://github.com/supabase/supabase/discussions

## Conclusion

Following this guide will ensure a successful production deployment of the DrMindit Supabase backend. Regular monitoring and maintenance will keep the system running smoothly and securely.

Remember to:
- Test thoroughly before deployment
- Monitor performance continuously
- Keep security updated
- Maintain regular backups
- Document all changes
