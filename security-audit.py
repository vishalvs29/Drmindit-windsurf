#!/usr/bin/env python3
"""
DrMindit Security Audit Script

This script performs comprehensive security checks on the DrMindit codebase
to ensure no secrets are exposed and security best practices are followed.
"""

import os
import re
import json
import sys
from pathlib import Path
from typing import List, Dict, Any

class SecurityAudit:
    def __init__(self, repo_path: str):
        self.repo_path = Path(repo_path)
        self.issues = []
        self.warnings = []
        self.passed = []
        
    def run_full_audit(self):
        """Run complete security audit"""
        print("🔍 Starting DrMindit Security Audit...")
        print("=" * 50)
        
        # 1. Secret detection
        self.check_for_secrets()
        
        # 2. Git security
        self.check_git_security()
        
        # 3. Configuration security
        self.check_configuration_security()
        
        # 4. File permissions
        self.check_file_permissions()
        
        # 5. Dependencies
        self.check_dependencies()
        
        # 6. Code patterns
        self.check_code_patterns()
        
        # Generate report
        self.generate_report()
        
    def check_for_secrets(self):
        """Check for exposed secrets in source code"""
        print("\n🔐 Checking for exposed secrets...")
        
        secret_patterns = [
            # OpenAI API keys
            (r'sk-[a-zA-Z0-9]{20,}', 'OpenAI API Key'),
            (r'AIza[0-9A-Za-z_-]{35}', 'OpenAI API Key'),
            
            # Supabase keys
            (r'supabase_[a-zA-Z0-9_-]{40,}', 'Supabase Key'),
            (r'eyJ[a-zA-Z0-9._-]+\.eyJ[a-zA-Z0-9._-]+', 'JWT Token'),
            
            # Firebase keys
            (r'AAAA[a-zA-Z0-9_-]{35}', 'Firebase Key'),
            (r'[a-zA-Z0-9_-]{32}@firebaseio\.com', 'Firebase ID'),
            
            # AWS keys
            (r'AKIA[0-9A-Z]{16}', 'AWS Access Key'),
            (r'[a-zA-Z0-9/+]{40}', 'AWS Secret Key'),
            
            # Generic patterns
            (r'[a-zA-Z0-9_-]{32,}@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}', 'Email Address'),
            (r'api[_-]?key["\']?\s*[:=]\s*["\']?[a-zA-Z0-9_-]+', 'API Key'),
            (r'secret[_-]?key["\']?\s*[:=]\s*["\']?[a-zA-Z0-9_-]+', 'Secret Key'),
            (r'private[_-]?key["\']?\s*[:=]\s*["\']?[a-zA-Z0-9_-]+', 'Private Key'),
            (r'password["\']?\s*[:=]\s*["\']?[^\s"\']{8,}', 'Password'),
            (r'token["\']?\s*[:=]\s*["\']?[a-zA-Z0-9._-]+', 'Token'),
        ]
        
        files_to_check = []
        for pattern in ['*.kt', '*.java', '*.js', '*.ts', '*.json', '*.properties', '*.xml']:
            files_to_check.extend(self.repo_path.rglob(pattern))
        
        secrets_found = False
        for file_path in files_to_check:
            try:
                with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                    content = f.read()
                    line_number = 0
                    
                    for line in content.split('\n'):
                        line_number += 1
                        for pattern, description in secret_patterns:
                            matches = re.findall(pattern, line, re.IGNORECASE)
                            if matches:
                                for match in matches:
                                    self.issues.append({
                                        'type': 'SECRET_EXPOSED',
                                        'severity': 'CRITICAL',
                                        'file': str(file_path.relative_to(self.repo_path)),
                                        'line': line_number,
                                        'pattern': description,
                                        'match': match[:20] + '...' if len(match) > 20 else match,
                                        'recommendation': 'Remove secret from code and use environment variables'
                                    })
                                    secrets_found = True
            except Exception as e:
                self.warnings.append({
                    'type': 'FILE_READ_ERROR',
                    'severity': 'LOW',
                    'file': str(file_path.relative_to(self.repo_path)),
                    'error': str(e)
                })
        
        if not secrets_found:
            self.passed.append({
                'type': 'SECRET_SCAN',
                'message': 'No exposed secrets found in source code'
            })
    
    def check_git_security(self):
        """Check git security configuration"""
        print("\n📁 Checking git security...")
        
        gitignore_path = self.repo_path / '.gitignore'
        if not gitignore_path.exists():
            self.issues.append({
                'type': 'GIT_SECURITY',
                'severity': 'HIGH',
                'message': '.gitignore file not found'
            })
            return
        
        with open(gitignore_path, 'r') as f:
            gitignore_content = f.read()
        
        # Check for critical patterns in .gitignore
        critical_patterns = [
            '.env',
            '*.env',
            'api-keys.txt',
            'secrets.txt',
            'private-keys.txt',
            '*.keystore',
            '*.jks',
            'google-services.json',
            'firebase.json'
        ]
        
        missing_patterns = []
        for pattern in critical_patterns:
            if pattern not in gitignore_content:
                missing_patterns.append(pattern)
        
        if missing_patterns:
            self.issues.append({
                'type': 'GIT_SECURITY',
                'severity': 'HIGH',
                'message': f'Critical patterns missing from .gitignore: {", ".join(missing_patterns)}'
            })
        else:
            self.passed.append({
                'type': 'GIT_SECURITY',
                'message': 'Git security properly configured'
            })
    
    def check_configuration_security(self):
        """Check configuration files for security"""
        print("\n⚙️ Checking configuration security...")
        
        config_files = [
            'local.properties',
            'build.gradle',
            'gradle.properties'
        ]
        
        for config_file in config_files:
            config_path = self.repo_path / config_file
            if config_path.exists():
                with open(config_path, 'r') as f:
                    content = f.read()
                    
                # Check for potential secrets
                if any(keyword in content.lower() for keyword in ['password', 'secret', 'key', 'token']):
                    self.warnings.append({
                        'type': 'CONFIG_SECURITY',
                        'severity': 'MEDIUM',
                        'file': config_file,
                        'message': f'Potential sensitive data in {config_file}'
                    })
    
    def check_file_permissions(self):
        """Check file permissions for sensitive files"""
        print("\n🔒 Checking file permissions...")
        
        sensitive_files = [
            '.env',
            '*.keystore',
            '*.jks',
            'google-services.json',
            'firebase.json'
        ]
        
        for pattern in sensitive_files:
            for file_path in self.repo_path.glob(pattern):
                if file_path.exists():
                    stat = file_path.stat()
                    mode = oct(stat.st_mode)[-3:]
                    
                    # Check if file is readable by others
                    if mode[2] != '0':
                        self.warnings.append({
                            'type': 'FILE_PERMISSIONS',
                            'severity': 'MEDIUM',
                            'file': str(file_path.relative_to(self.repo_path)),
                            'message': f'File has permissions {mode}'
                        })
    
    def check_dependencies(self):
        """Check for known vulnerable dependencies"""
        print("\n📦 Checking dependencies...")
        
        package_files = [
            'package.json',
            'build.gradle.kts',
            'build.gradle'
        ]
        
        for package_file in package_files:
            package_path = self.repo_path / package_file
            if package_path.exists():
                with open(package_path, 'r') as f:
                    content = f.read()
                    
                # Check for obvious vulnerable versions (basic check)
                vulnerable_patterns = [
                    (r'"express":\s*"4\.', 'Express.js < 5'),
                    (r'"axios":\s*"[0-9]\.', 'Axios < 1.0'),
                    (r'"lodash":\s*"4\.', 'Lodash < 4.17'),
                ]
                
                for pattern, description in vulnerable_patterns:
                    if re.search(pattern, content):
                        self.warnings.append({
                            'type': 'DEPENDENCY_SECURITY',
                            'severity': 'HIGH',
                            'file': package_file,
                            'message': f'Potentially vulnerable dependency: {description}'
                        })
        
        self.passed.append({
            'type': 'DEPENDENCY_CHECK',
            'message': 'No obviously vulnerable dependencies found'
        })
    
    def check_code_patterns(self):
        """Check for insecure coding patterns"""
        print("\n🔍 Checking code patterns...")
        
        files_to_check = []
        for pattern in ['*.kt', '*.java', '*.js', '*.ts']:
            files_to_check.extend(self.repo_path.rglob(pattern))
        
        insecure_patterns = [
            # Hardcoded URLs
            (r'http://[^"\s]+', 'Hardcoded HTTP URL'),
            (r'https?://[^"\s]*api\.key', 'Hardcoded API URL'),
            
            # Debug code in production
            (r'console\.log', 'Debug logging'),
            (r'System\.out\.print', 'Debug printing'),
            (r'Timber\.d\(', 'Debug logging'),
            
            # Weak cryptography
            (r'MD5', 'Weak hash algorithm'),
            (r'SHA1', 'Weak hash algorithm'),
            
            # SQL injection patterns
            (r'\+.*\+.*\+', 'Potential SQL injection'),
            (r'"SELECT.*FROM', 'Potential SQL injection'),
        ]
        
        for file_path in files_to_check:
            try:
                with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                    content = f.read()
                    line_number = 0
                    
                    for line in content.split('\n'):
                        line_number += 1
                        for pattern, description in insecure_patterns:
                            if re.search(pattern, line, re.IGNORECASE):
                                self.warnings.append({
                                    'type': 'CODE_PATTERN',
                                    'severity': 'MEDIUM',
                                    'file': str(file_path.relative_to(self.repo_path)),
                                    'line': line_number,
                                    'pattern': description,
                                    'code': line.strip()[:100] + '...' if len(line.strip()) > 100 else line.strip()
                                })
            except Exception as e:
                self.warnings.append({
                    'type': 'FILE_READ_ERROR',
                    'severity': 'LOW',
                    'file': str(file_path.relative_to(self.repo_path)),
                    'error': str(e)
                })
    
    def generate_report(self):
        """Generate security audit report"""
        print("\n" + "=" * 50)
        print("🔍 SECURITY AUDIT REPORT")
        print("=" * 50)
        
        # Critical issues
        critical_issues = [issue for issue in self.issues if issue['severity'] == 'CRITICAL']
        if critical_issues:
            print(f"\n🚨 CRITICAL ISSUES ({len(critical_issues)}):")
            for issue in critical_issues:
                print(f"  ❌ {issue['type']}: {issue['message']}")
                print(f"     File: {issue.get('file', 'N/A')}")
                print(f"     Line: {issue.get('line', 'N/A')}")
                print(f"     Recommendation: {issue.get('recommendation', 'Review and fix')}")
        
        # High severity issues
        high_issues = [issue for issue in self.issues if issue['severity'] == 'HIGH']
        if high_issues:
            print(f"\n⚠️  HIGH SEVERITY ISSUES ({len(high_issues)}):")
            for issue in high_issues:
                print(f"  ❌ {issue['type']}: {issue['message']}")
                if 'file' in issue:
                    print(f"     File: {issue['file']}")
        
        # Medium severity issues
        medium_issues = [issue for issue in self.issues if issue['severity'] == 'MEDIUM']
        if medium_issues:
            print(f"\n⚠️  MEDIUM SEVERITY ISSUES ({len(medium_issues)}):")
            for issue in medium_issues:
                print(f"  ⚠️  {issue['type']}: {issue['message']}")
                if 'file' in issue:
                    print(f"     File: {issue['file']}")
        
        # Warnings
        if self.warnings:
            print(f"\n⚠️  WARNINGS ({len(self.warnings)}):")
            for warning in self.warnings:
                print(f"  ⚠️  {warning['type']}: {warning['message']}")
                if 'file' in warning:
                    print(f"     File: {warning['file']}")
        
        # Passed checks
        if self.passed:
            print(f"\n✅ PASSED CHECKS ({len(self.passed)}):")
            for passed in self.passed:
                print(f"  ✅ {passed['message']}")
        
        # Summary
        total_issues = len(self.issues) + len(self.warnings)
        if total_issues == 0:
            print(f"\n🎉 SECURITY AUDIT PASSED!")
            print("✅ No critical security issues found")
            print("✅ Repository is secure for production")
        else:
            print(f"\n⚠️  SECURITY AUDIT SUMMARY:")
            print(f"   Critical Issues: {len(critical_issues)}")
            print(f"   High Issues: {len(high_issues)}")
            print(f"   Medium Issues: {len(medium_issues)}")
            print(f"   Warnings: {len(self.warnings)}")
            print(f"   Total Issues: {total_issues}")
            
            if critical_issues:
                print(f"\n🚨 CRITICAL: Fix all critical issues before production deployment!")
            elif high_issues:
                print(f"\n⚠️  WARNING: Address high severity issues before production!")
        
        print("\n" + "=" * 50)
        print("📋 NEXT STEPS:")
        print("1. Fix all CRITICAL and HIGH severity issues")
        print("2. Review and address MEDIUM severity issues")
        print("3. Run security audit after fixes")
        print("4. Set up regular security scanning in CI/CD")
        print("5. Implement security monitoring in production")
        print("=" * 50)

def main():
    if len(sys.argv) != 2:
        print("Usage: python security-audit.py <repository-path>")
        sys.exit(1)
    
    repo_path = sys.argv[1]
    if not os.path.exists(repo_path):
        print(f"Error: Repository path '{repo_path}' does not exist")
        sys.exit(1)
    
    audit = SecurityAudit(repo_path)
    audit.run_full_audit()

if __name__ == "__main__":
    main()
