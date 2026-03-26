# DrMindit Secure Backend Proxy

## 🛡️ **Secure API Proxy for DrMindit Mental Health App**

This backend proxy handles all sensitive API calls securely, ensuring no API keys are exposed in the client application.

---

## 🚀 **Quick Start**

### **1. Prerequisites**
- Node.js 18.0.0 or later
- npm 8.0.0 or later
- Valid OpenAI API key
- Supabase project credentials

### **2. Installation**
```bash
# Clone the repository
git clone https://github.com/your-org/drmindit.git
cd drmindit/backend/secure-proxy

# Install dependencies
npm install

# Copy environment template
cp .env.example .env
```

### **3. Environment Configuration**
Edit `.env` file with your actual credentials:

```bash
# OpenAI Configuration
OPENAI_API_KEY=sk-your-actual-openai-api-key
OPENAI_MODEL=gpt-4
OPENAI_MAX_TOKENS=150
OPENAI_TEMPERATURE=0.7

# Supabase Configuration
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-supabase-anon-key
SUPABASE_SERVICE_ROLE_KEY=your-supabase-service-role-key

# Security Configuration
JWT_SECRET=your-super-secret-jwt-key-min-32-chars
CORS_ORIGIN=https://yourdomain.com
```

### **4. Start Server**
```bash
# Development
npm run dev

# Production
npm start
```

Server will start on port 3001 with all security features enabled.

---

## 🔐 **Security Features**

### **Authentication & Authorization**
- **JWT-based authentication** with secure token generation
- **Token expiration** (24 hours by default)
- **Secure password validation** (minimum 8 characters)
- **Session management** with token validation

### **Rate Limiting**
- **20 requests per minute** per user
- **Sliding window** implementation
- **IP-based tracking**
- **Graceful error handling** for rate limit exceeded

### **Input Validation**
- **Message length limits** (max 1000 characters)
- **XSS protection** with script tag removal
- **Email validation** with proper format checking
- **Request sanitization** for all inputs

### **Security Headers**
- **HSTS** for HTTPS enforcement
- **Content Security Policy** for XSS prevention
- **Frameguard** for clickjacking protection
- **No-Cache headers** for sensitive data

---

## 📡 **API Endpoints**

### **Authentication**
```http
POST /api/auth/login
```

**Request:**
```json
{
  "email": "user@example.com",
  "password": "securepassword123"
}
```

**Response:**
```json
{
  "success": true,
  "token": "jwt-token-here",
  "user": {
    "id": "user-id",
    "email": "user@example.com",
    "name": "User Name"
  },
  "expiresAt": 1640995200000
}
```

### **Chat API**
```http
POST /api/chat
```

**Headers:**
```
Authorization: Bearer jwt-token-here
Content-Type: application/json
```

**Request:**
```json
{
  "message": "I'm feeling anxious today",
  "userId": "user-id",
  "sessionId": "optional-session-id"
}
```

**Response:**
```json
{
  "success": true,
  "response": "I understand you're feeling anxious. Let's work through this together...",
  "usage": {
    "promptTokens": 25,
    "completionTokens": 75,
    "totalTokens": 100
  },
  "sessionId": "session-id",
  "timestamp": 1640995200000
}
```

### **Session Validation**
```http
GET /api/session/validate
```

**Response:**
```json
{
  "valid": true,
  "user": {
    "id": "user-id",
    "email": "user@example.com"
  },
  "timestamp": 1640995200000,
  "expiresAt": 1640995200000
}
```

### **Health Check**
```http
GET /health
```

**Response:**
```json
{
  "status": "healthy",
  "timestamp": "2024-01-01T12:00:00.000Z",
  "uptime": 3600,
  "version": "v1"
}
```

---

## 🛠️ **Configuration Options**

### **Environment Variables**
| Variable | Required | Default | Description |
|----------|-----------|---------|-------------|
| `PORT` | No | 3001 | Server port |
| `NODE_ENV` | No | development | Environment (development/production) |
| `OPENAI_API_KEY` | Yes | - | OpenAI API key |
| `OPENAI_MODEL` | No | gpt-4 | OpenAI model |
| `OPENAI_MAX_TOKENS` | No | 150 | Max tokens per request |
| `OPENAI_TEMPERATURE` | No | 0.7 | Response creativity |
| `SUPABASE_URL` | Yes | - | Supabase project URL |
| `SUPABASE_ANON_KEY` | Yes | - | Supabase anonymous key |
| `JWT_SECRET` | Yes | - | JWT signing secret (min 32 chars) |
| `CORS_ORIGIN` | No | * | CORS allowed origin |

### **Rate Limiting**
| Variable | Default | Description |
|----------|-----------|-------------|
| `RATE_LIMIT_WINDOW_MS` | 60000 | Rate limit window in milliseconds |
| `RATE_LIMIT_MAX_REQUESTS` | 20 | Max requests per window |
| `RATE_LIMIT_SKIP_SUCCESSFUL` | false | Skip successful requests in rate limit |

### **Security**
| Variable | Default | Description |
|----------|-----------|-------------|
| `BCRYPT_ROUNDS` | 12 | Password hashing rounds |
| `HELMET_CSP_DIRECTIVE` | default-src 'self' | Content Security Policy |
| `HELMET_HSTS_MAX_AGE` | 31536000 | HSTS max age in seconds |
| `HELMET_NO_CACHE` | true | Disable client-side caching |
| `HELMET_FRAMEGUARD` | true | Enable frameguard protection |

---

## 🚨 **Security Considerations**

### **Production Deployment**
1. **Use HTTPS** with valid SSL certificate
2. **Set strong JWT secret** (minimum 32 characters)
3. **Configure firewall** to only allow necessary ports
4. **Enable monitoring** for security events
5. **Regular secret rotation** for API keys

### **Rate Limiting**
- Monitor rate limit violations for potential abuse
- Adjust limits based on your usage patterns
- Consider implementing tiered rate limits for different user types

### **Database Security**
If using a database:
1. Use connection pooling
2. Enable SSL for database connections
3. Implement proper indexing
4. Regular security updates
5. Backup encryption

---

## 📊 **Monitoring & Logging**

### **Log Levels**
- `error`: Security events and errors
- `warn`: Rate limit violations, auth failures
- `info`: General API calls, health checks
- `debug`: Detailed debugging (development only)

### **Security Events Logged**
- Authentication failures
- Rate limit exceeded
- Invalid tokens
- Input validation failures
- Suspicious request patterns

### **Health Monitoring**
- API response times
- Error rates
- Memory usage
- CPU usage
- Database connection status

---

## 🔧 **Development**

### **Local Development**
```bash
# Start with hot reload
npm run dev

# View logs
tail -f logs/app.log

# Test endpoints
curl http://localhost:3001/health
```

### **Testing**
```bash
# Run tests
npm test

# Run with coverage
npm run test:coverage

# Integration tests
npm run test:integration
```

### **Debug Mode**
Set `SKIP_AUTH_FOR_DEV=true` in `.env` to skip authentication during development.

---

## 🚀 **Production Deployment**

### **Using PM2**
```bash
# Install PM2
npm install -g pm2

# Start with PM2
pm2 start ecosystem.config.js

# Monitor
pm2 monit
```

### **Using Docker**
```bash
# Build image
docker build -t drmindit-proxy .

# Run container
docker run -p 3001:3001 --env-file .env drmindit-proxy
```

### **Using Systemd**
```bash
# Create service file
sudo nano /etc/systemd/system/drmindit-proxy.service

# Enable and start
sudo systemctl enable drmindit-proxy
sudo systemctl start drmindit-proxy
```

---

## 🔐 **Security Checklist**

### **Before Deployment**
- [ ] All secrets in environment variables
- [ ] Strong JWT secret (32+ characters)
- [ ] HTTPS certificates valid
- [ ] Firewall rules configured
- [ ] Rate limiting tested
- [ ] Input validation tested
- [ ] Error handling reviewed
- [ ] Logging configured
- [ ] Monitoring set up

### **After Deployment**
- [ ] SSL certificate valid
- [ ] HTTPS redirects working
- [ ] Rate limiting active
- [ ] Authentication working
- [ ] Logs being generated
- [ ] Health checks passing
- [ ] Monitoring alerts configured

---

## 📞 **Support**

### **Security Issues**
For security concerns or vulnerabilities:
1. **Do NOT** report in public issues
2. Email: security@drmindit.com
3. Include detailed reproduction steps
4. Provide environment details

### **General Support**
For technical support:
- Email: support@drmindit.com
- Documentation: [Link to docs]
- Status: [Link to status page]

---

## 📄 **License**

This secure backend proxy is part of the DrMindit project and is subject to the same licensing terms.

---

**🔐 This secure backend ensures that no API keys are ever exposed in client code while providing enterprise-grade security for the DrMindit mental health application.**
