// Jest Setup File
// Configures test environment for DrMindit backend

// Set test environment variables
process.env.NODE_ENV = 'test';
process.env.JWT_SECRET = 'test-jwt-secret-key-for-testing-purposes-only';
process.env.DB_NAME = 'drmindit_test';
process.env.REDIS_URL = 'redis://localhost:6379/1';

// Mock external services
jest.mock('nodemailer', () => ({
    createTransporter: jest.fn(() => ({
        sendMail: jest.fn().mockResolvedValue({ messageId: 'test-message-id' })
    }))
}));

jest.mock('../src/utils/email', () => ({
    sendEmail: jest.fn().mockResolvedValue({ messageId: 'test-message-id' })
}));

// Global test timeout
jest.setTimeout(10000);

// Global test utilities
global.testUser = {
    id: 'test-user-id',
    email: 'test@example.com',
    firstName: 'Test',
    lastName: 'User',
    role: 'user',
    isEmailVerified: true
};

global.testAdmin = {
    id: 'test-admin-id',
    email: 'admin@example.com',
    firstName: 'Admin',
    lastName: 'User',
    role: 'admin',
    isEmailVerified: true
};

// Clean up after tests
afterEach(() => {
    jest.clearAllMocks();
});

// Setup database connection before tests
beforeAll(async () => {
    // Note: In a real setup, you would initialize test database here
    console.log('Test environment initialized');
});

// Cleanup database after tests
afterAll(async () => {
    // Note: In a real setup, you would clean up test database here
    console.log('Test environment cleaned up');
});
