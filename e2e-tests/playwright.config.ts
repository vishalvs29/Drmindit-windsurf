import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright configuration for DrMindit E2E tests
 * Tests web, Android, and iOS platforms
 */
export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: 'html',
  use: {
    baseURL: process.env.BASE_URL || 'http://localhost:3000',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  projects: [
    {
      name: 'web',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'android',
      use: { 
        ...devices['Pixel 5'],
        baseURL: process.env.ANDROID_BASE_URL || 'http://10.0.2.2:8080',
      },
    },
    {
      name: 'ios',
      use: {
        ...devices['iPhone 14'],
        baseURL: process.env.IOS_BASE_URL || 'http://10.0.2.2:8081',
      },
    },
  ],
  webServer: {
    command: 'npm run dev',
    port: 3000,
    reuseExistingServer: !process.env.CI,
    timeout: 120 * 1000,
  },
});
