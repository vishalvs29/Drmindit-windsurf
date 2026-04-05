-- Migration 001: Create users table
-- DrMindit Backend Database Schema

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'user',
    is_email_verified BOOLEAN DEFAULT FALSE,
    email_verification_token VARCHAR(255),
    email_verification_token_expiry TIMESTAMP WITH TIME ZONE,
    password_reset_token VARCHAR(255),
    password_reset_token_expiry TIMESTAMP WITH TIME ZONE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);
CREATE INDEX IF NOT EXISTS idx_users_last_login_at ON users(last_login_at);
CREATE INDEX IF NOT EXISTS idx_users_email_verification_token ON users(email_verification_token);
CREATE INDEX IF NOT EXISTS idx_users_deleted_at ON users(deleted_at);

-- Add constraints
ALTER TABLE users ADD CONSTRAINT chk_users_role 
    CHECK (role IN ('user', 'admin', 'moderator'));

-- Add comments for documentation
COMMENT ON TABLE users IS 'User accounts for DrMindit platform';
COMMENT ON COLUMN users.id IS 'Primary key UUID for user identification';
COMMENT ON COLUMN users.email IS 'User email address (unique)';
COMMENT ON COLUMN users.password IS 'Hashed password using bcrypt';
COMMENT ON COLUMN users.first_name IS 'User first name';
COMMENT ON COLUMN users.last_name IS 'User last name';
COMMENT ON COLUMN users.role IS 'User role (user, admin, moderator)';
COMMENT ON COLUMN users.is_email_verified IS 'Email verification status';
COMMENT ON COLUMN users.email_verification_token IS 'Token for email verification';
COMMENT ON COLUMN users.email_verification_token_expiry IS 'Expiry time for email verification token';
COMMENT ON COLUMN users.password_reset_token IS 'Token for password reset';
COMMENT ON COLUMN users.password_reset_token_expiry IS 'Expiry time for password reset token';
COMMENT ON COLUMN users.last_login_at IS 'Last login timestamp';
COMMENT ON COLUMN users.created_at IS 'Account creation timestamp';
COMMENT ON COLUMN users.updated_at IS 'Last update timestamp';
COMMENT ON COLUMN users.deleted_at IS 'Soft delete timestamp (null means not deleted)';
