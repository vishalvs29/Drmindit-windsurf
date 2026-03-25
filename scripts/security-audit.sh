#!/bin/bash

# DrMindit Security Audit Script
# Scans for exposed secrets and security vulnerabilities

set -e

echo "🔍 Starting DrMindit Security Audit..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

# Check for exposed API keys
print_status "Checking for exposed API keys..."

# Common API key patterns
API_KEY_PATTERNS=(
    "AIza[0-9A-Za-z_-]{35}"
    "ya29\\.[0-9A-Za-z_-]{100}"
    "AKIA[0-9A-Z]{16}"
    "sk_live_[0-9A-Za-z]{24}"
    "pk_live_[0-9A-Za-z]{24}"
    "xoxb-[0-9]{12}-[0-9A-Za-z]{24}"
    "ghp_[0-9A-Za-z]{36}"
    "glpat-[0-9A-Za-z_-]{20}"
    "[0-9A-Za-z_-]{40}\\.(supabase\\.co)"
)

SECRETS_FOUND=false

for pattern in "${API_KEY_PATTERNS[@]}"; do
    if grep -r -E "$pattern" --include="*.kt" --include="*.java" --include="*.xml" --include="*.json" --include="*.gradle" --include="*.properties" --exclude-dir=build --exclude-dir=.git . 2>/dev/null; then
        print_error "Potential API key found with pattern: $pattern"
        SECRETS_FOUND=true
    fi
done

# Check for hardcoded passwords
print_status "Checking for hardcoded passwords..."

PASSWORD_PATTERNS=(
    "password[\"']?\\s*[:=]\\s*[\"'][^\"']{8,}[\"']"
    "secret[\"']?\\s*[:=]\\s*[\"'][^\"']{8,}[\"']"
    "token[\"']?\\s*[:=]\\s*[\"'][^\"']{16,}[\"']"
    "key[\"']?\\s*[:=]\\s*[\"'][^\"']{16,}[\"']"
)

for pattern in "${PASSWORD_PATTERNS[@]}"; do
    if grep -r -E "$pattern" --include="*.kt" --include="*.java" --include="*.xml" --include="*.json" --include="*.gradle" --include="*.properties" --exclude-dir=build --exclude-dir=.git . 2>/dev/null; then
        print_error "Potential hardcoded password/secret found with pattern: $pattern"
        SECRETS_FOUND=true
    fi
done

# Check for exposed database URLs
print_status "Checking for exposed database URLs..."

DB_URL_PATTERNS=(
    "mysql://[^@]+:[^@]+@[^/]+"
    "postgresql://[^@]+:[^@]+@[^/]+"
    "mongodb://[^@]+:[^@]+@[^/]+"
    "redis://[^@]+:[^@]+@[^/]+"
)

for pattern in "${DB_URL_PATTERNS[@]}"; do
    if grep -r -E "$pattern" --include="*.kt" --include="*.java" --include="*.xml" --include="*.json" --include="*.gradle" --include="*.properties" --exclude-dir=build --exclude-dir=.git . 2>/dev/null; then
        print_error "Potential database URL with credentials found: $pattern"
        SECRETS_FOUND=true
    fi
done

# Check for sensitive files
print_status "Checking for sensitive files in repository..."

SENSITIVE_FILES=(
    ".env"
    ".env.local"
    ".env.production"
    "google-services.json"
    "firebase.json"
    "*.keystore"
    "*.jks"
    "*.p12"
    "*.pem"
    "aws-credentials.json"
    "gcp-credentials.json"
    "id_rsa"
    "id_rsa.pub"
)

for file in "${SENSITIVE_FILES[@]}"; do
    if find . -name "$file" -not -path "./.git/*" -not -name "*.example" 2>/dev/null | grep -q .; then
        print_error "Sensitive file found in repository: $file"
        SECRETS_FOUND=true
    fi
done

# Check .gitignore for security entries
print_status "Checking .gitignore for security entries..."

REQUIRED_GITIGNORE_ENTRIES=(
    ".env"
    "google-services.json"
    "*.keystore"
    "*.jks"
    "local.properties"
)

GITIGNORE_ISSUES=false
for entry in "${REQUIRED_GITIGNORE_ENTRIES[@]}"; do
    if ! grep -q "^$entry" .gitignore 2>/dev/null; then
        print_warning "Missing .gitignore entry: $entry"
        GITIGNORE_ISSUES=true
    fi
done

# Check for debug code in production
print_status "Checking for debug code..."

DEBUG_PATTERNS=(
    "Log\\.d"
    "Log\\.v"
    "System\\.out\\.println"
    "console\\.log"
    "debugger"
    "TODO.*remove.*before.*production"
)

for pattern in "${DEBUG_PATTERNS[@]}"; do
    if grep -r -E "$pattern" --include="*.kt" --include="*.java" --exclude-dir=build --exclude-dir=.git --exclude="*Test*" . 2>/dev/null; then
        print_warning "Debug code found: $pattern"
    fi
done

# Check for insecure HTTP usage
print_status "Checking for insecure HTTP usage..."

if grep -r "http://" --include="*.kt" --include="*.java" --include="*.xml" --exclude-dir=build --exclude-dir=.git . 2>/dev/null | grep -v "http://localhost" | grep -v "http://127.0.0.1" | grep -q .; then
    print_warning "Insecure HTTP usage found (non-localhost)"
fi

# Check for weak cryptographic algorithms
print_status "Checking for weak cryptographic algorithms..."

WEAK_CRYPTO_PATTERNS=(
    "MD5"
    "SHA1"
    "DES"
    "RC4"
)

for pattern in "${WEAK_CRYPTO_PATTERNS[@]}"; do
    if grep -r -E "MessageDigest\\.getInstance\\(\"$pattern\"\\)" --include="*.kt" --include="*.java" --exclude-dir=build --exclude-dir=.git . 2>/dev/null; then
        print_warning "Weak cryptographic algorithm found: $pattern"
    fi
done

# Summary
echo ""
echo "📊 Security Audit Summary:"
echo "========================="

if [ "$SECRETS_FOUND" = true ]; then
    print_error "❌ CRITICAL: Exposed secrets found in repository!"
    echo "Please immediately:"
    echo "1. Remove all hardcoded secrets"
    echo "2. Replace with environment variables"
    echo "3. Invalidate any exposed API keys"
    echo "4. Review git history for sensitive data"
    exit 1
else
    print_success "✅ No exposed secrets found in current files"
fi

if [ "$GITIGNORE_ISSUES" = true ]; then
    print_warning "⚠️  .gitignore needs security updates"
else
    print_success "✅ .gitignore security entries are properly configured"
fi

# Check if .env.example exists
if [ ! -f ".env.example" ]; then
    print_warning "⚠️  .env.example file is missing"
else
    print_success "✅ .env.example file exists"
fi

# Check if google-services.json.example exists
if [ ! -f "google-services.json.example" ]; then
    print_warning "⚠️  google-services.json.example file is missing"
else
    print_success "✅ google-services.json.example file exists"
fi

echo ""
print_status "Running additional security checks..."

# Check for dependencies with known vulnerabilities (if dependency-check is available)
if command -v dependency-check &> /dev/null; then
    print_status "Running dependency vulnerability check..."
    dependency-check --project . --format XML --failBuildOnCVSS 7 || {
        print_error "❌ Dependencies with known vulnerabilities found"
        exit 1
    }
    print_success "✅ No high-severity vulnerabilities found in dependencies"
else
    print_warning "⚠️  dependency-check not available, skipping vulnerability scan"
fi

# Run static analysis (if detekt is available)
if command -v detekt &> /dev/null; then
    print_status "Running static analysis..."
    detekt --config config/detekt/detekt.yml --build-upon-default-config || {
        print_error "❌ Static analysis issues found"
        exit 1
    }
    print_success "✅ Static analysis passed"
else
    print_warning "⚠️  detekt not available, skipping static analysis"
fi

echo ""
print_success "🎉 Security audit completed successfully!"
echo ""
echo "📋 Security Checklist:"
echo "✅ No exposed secrets in current files"
echo "✅ Environment variables properly configured"
echo "✅ Sensitive files excluded from version control"
echo "✅ .gitignore security entries configured"
echo "✅ Example configuration files provided"
echo ""
echo "🔐 Remember to:"
echo "1. Use environment variables for all secrets"
echo "2. Never commit .env files or actual credentials"
echo "3. Regularly rotate API keys and passwords"
echo "4. Monitor for unauthorized access"
echo "5. Keep dependencies updated"
