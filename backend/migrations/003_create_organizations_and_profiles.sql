-- Migration 003: Create organizations table first, then profiles table
-- Fixes schema dependency issue where profiles references organizations before it's defined

-- Create organizations table first (must be created before profiles)
CREATE TABLE IF NOT EXISTS organizations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('educational', 'corporate', 'government', 'healthcare')),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    settings JSONB DEFAULT '{}',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- Create indexes for organizations table
CREATE INDEX IF NOT EXISTS idx_organizations_type ON organizations(type);
CREATE INDEX IF NOT EXISTS idx_organizations_is_active ON organizations(is_active);
CREATE INDEX IF NOT EXISTS idx_organizations_created_at ON organizations(created_at);

-- Add comments for organizations table
COMMENT ON TABLE organizations IS 'Organization accounts for enterprise clients';
COMMENT ON COLUMN organizations.id IS 'Primary key UUID for organization identification';
COMMENT ON COLUMN organizations.name IS 'Organization name';
COMMENT ON COLUMN organizations.type IS 'Organization type (educational, corporate, government, healthcare)';
COMMENT ON COLUMN organizations.contact_email IS 'Organization contact email';
COMMENT ON COLUMN organizations.contact_phone IS 'Organization contact phone';
COMMENT ON COLUMN organizations.settings IS 'Organization-specific settings as JSON';
COMMENT ON COLUMN organizations.is_active IS 'Organization active status';
COMMENT ON COLUMN organizations.created_at IS 'Organization creation timestamp';
COMMENT ON COLUMN organizations.updated_at IS 'Last update timestamp';
COMMENT ON COLUMN organizations.deleted_at IS 'Soft delete timestamp (null means not deleted)';

-- Create profiles table (now can safely reference organizations)
CREATE TABLE IF NOT EXISTS profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    email VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    avatar_url VARCHAR(500),
    role VARCHAR(50) NOT NULL CHECK (role IN ('student', 'corporate', 'government', 'police_military', 'general')),
    phone VARCHAR(50),
    organization_id UUID REFERENCES organizations(id) ON DELETE SET NULL,
    personal_goals TEXT[] DEFAULT '{}',
    stress_level INTEGER CHECK (stress_level >= 1 AND stress_level <= 10),
    preferences JSONB DEFAULT '{}',
    is_high_risk BOOLEAN DEFAULT FALSE,
    last_crisis_check TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- Create indexes for profiles table
CREATE INDEX IF NOT EXISTS idx_profiles_user_id ON profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_profiles_email ON profiles(email);
CREATE INDEX IF NOT EXISTS idx_profiles_role ON profiles(role);
CREATE INDEX IF NOT EXISTS idx_profiles_organization_id ON profiles(organization_id);
CREATE INDEX IF NOT EXISTS idx_profiles_is_high_risk ON profiles(is_high_risk);
CREATE INDEX IF NOT EXISTS idx_profiles_created_at ON profiles(created_at);
CREATE INDEX IF NOT EXISTS idx_profiles_last_crisis_check ON profiles(last_crisis_check);

-- Add comments for profiles table
COMMENT ON TABLE profiles IS 'Extended user profiles with organization and preferences';
COMMENT ON COLUMN profiles.id IS 'Primary key UUID for profile identification';
COMMENT ON COLUMN profiles.user_id IS 'Reference to users table';
COMMENT ON COLUMN profiles.email IS 'Profile email address';
COMMENT ON COLUMN profiles.full_name IS 'User full name';
COMMENT ON COLUMN profiles.avatar_url IS 'Profile avatar URL';
COMMENT ON COLUMN profiles.role IS 'User role within organization';
COMMENT ON COLUMN profiles.phone IS 'User phone number';
COMMENT ON COLUMN profiles.organization_id IS 'Reference to organizations table';
COMMENT ON COLUMN profiles.personal_goals IS 'Array of personal wellness goals';
COMMENT ON COLUMN profiles.stress_level IS 'Current stress level (1-10)';
COMMENT ON COLUMN profiles.preferences IS 'User preferences as JSON';
COMMENT ON COLUMN profiles.is_high_risk IS 'High risk flag for safety monitoring';
COMMENT ON COLUMN profiles.last_crisis_check IS 'Last crisis detection timestamp';
COMMENT ON COLUMN profiles.created_at IS 'Profile creation timestamp';
COMMENT ON COLUMN profiles.updated_at IS 'Last update timestamp';
COMMENT ON COLUMN profiles.deleted_at IS 'Soft delete timestamp (null means not deleted)';

-- Add constraints for profiles table
ALTER TABLE profiles ADD CONSTRAINT chk_profiles_role 
    CHECK (role IN ('student', 'corporate', 'government', 'police_military', 'general'));

-- Create trigger for automatic timestamp updates on profiles
CREATE OR REPLACE FUNCTION update_profiles_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_profiles_updated_at
    BEFORE UPDATE ON profiles
    FOR EACH ROW
    EXECUTE FUNCTION update_profiles_updated_at();

-- Create trigger for automatic timestamp updates on organizations
CREATE OR REPLACE FUNCTION update_organizations_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_organizations_updated_at
    BEFORE UPDATE ON organizations
    FOR EACH ROW
    EXECUTE FUNCTION update_organizations_updated_at();
