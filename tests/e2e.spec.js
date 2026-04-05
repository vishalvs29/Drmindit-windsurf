const { test, expect } = require('@playwright/test');

// DrMindit End-to-End Test Suite
test.describe('DrMindit Application', () => {
  
  test.beforeEach(async ({ page }) => {
    // Set up test environment
    await page.goto('http://localhost:3000');
  });

  test('Landing page loads correctly', async ({ page }) => {
    // Test landing page elements
    await expect(page).toHaveTitle(/DrMindit/);
    
    // Check main navigation
    await expect(page.locator('nav')).toBeVisible();
    
    // Check hero section
    await expect(page.locator('h1')).toContainText('DrMindit');
    
    // Test responsive design
    await page.setViewportSize({ width: 375, height: 812 }); // Mobile
    await expect(page.locator('nav')).toBeVisible();
  });

  test('Authentication flow', async ({ page }) => {
    // Navigate to login
    await page.click('[data-testid="login-button"]');
    
    // Check login form elements
    await expect(page.locator('input[type="email"]')).toBeVisible();
    await expect(page.locator('input[type="password"]')).toBeVisible();
    await expect(page.locator('button[type="submit"]')).toBeVisible();
    
    // Test form validation
    await page.click('button[type="submit"]');
    await expect(page.locator('.error-message')).toBeVisible();
    
    // Test successful login (mock)
    await page.fill('input[type="email"]', 'test@example.com');
    await page.fill('input[type="password"]', 'password123');
    await page.click('button[type="submit"]');
    
    // Should redirect to dashboard
    await expect(page).toHaveURL(/dashboard/);
  });

  test('Dashboard functionality', async ({ page }) => {
    // Mock authentication
    await page.goto('http://localhost:3000/dashboard');
    
    // Check dashboard elements
    await expect(page.locator('[data-testid="mood-chart"]')).toBeVisible();
    await expect(page.locator('[data-testid="session-stats"]')).toBeVisible();
    await expect(page.locator('[data-testid="progress-insights"]')).toBeVisible();
    
    // Test mood tracking
    await page.click('[data-testid="mood-input"]');
    await page.click('[data-testid="mood-option-8"]');
    await page.click('[data-testid="save-mood"]');
    
    // Verify mood saved
    await expect(page.locator('.success-message')).toBeVisible();
  });

  test('Audio session player', async ({ page }) => {
    // Navigate to session player
    await page.goto('http://localhost:3000/session/breathing-exercise');
    
    // Check player elements
    await expect(page.locator('[data-testid="audio-player"]')).toBeVisible();
    await expect(page.locator('[data-testid="play-button"]')).toBeVisible();
    await expect(page.locator('[data-testid="progress-bar"]')).toBeVisible();
    
    // Test play/pause
    await page.click('[data-testid="play-button"]');
    await expect(page.locator('[data-testid="pause-button"]')).toBeVisible();
    
    // Test seek
    await page.click('[data-testid="progress-bar"]');
    // Verify time updated
    const currentTime = await page.locator('[data-testid="current-time"]').textContent();
    expect(currentTime).toMatch(/\d+:\d+/);
  });

  test('AI Chat functionality', async ({ page }) => {
    // Navigate to chat
    await page.goto('http://localhost:3000/chat');
    
    // Check chat interface
    await expect(page.locator('[data-testid="chat-messages"]')).toBeVisible();
    await expect(page.locator('[data-testid="message-input"]')).toBeVisible();
    await expect(page.locator('[data-testid="send-button"]')).toBeVisible();
    
    // Test message sending
    await page.fill('[data-testid="message-input"]', 'I feel stressed today');
    await page.click('[data-testid="send-button"]');
    
    // Verify message appears
    await expect(page.locator('.user-message')).toContainText('I feel stressed today');
    
    // Wait for AI response (mock)
    await page.waitForTimeout(2000);
    await expect(page.locator('.ai-message')).toBeVisible();
    
    // Test voice input
    await page.click('[data-testid="voice-button"]');
    await expect(page.locator('.recording-indicator')).toBeVisible();
  });

  test('Profile and settings', async ({ page }) => {
    // Navigate to profile
    await page.goto('http://localhost:3000/profile');
    
    // Check profile elements
    await expect(page.locator('[data-testid="user-info"]')).toBeVisible();
    await expect(page.locator('[data-testid="stress-level"]')).toBeVisible();
    await expect(page.locator('[data-testid="personal-goals"]')).toBeVisible();
    
    // Test stress level adjustment
    await page.click('[data-testid="stress-slider"]');
    await expect(page.locator('[data-testid="stress-value"]')).toContainText('7');
    
    // Test dark mode toggle
    await page.click('[data-testid="dark-mode-toggle"]');
    await expect(page.locator('body')).toHaveClass(/dark/);
    
    // Test notification settings
    await page.click('[data-testid="notification-settings"]');
    await expect(page.locator('[data-testid="notification-preferences"]')).toBeVisible();
  });

  test('Offline functionality', async ({ page }) => {
    // Simulate offline mode
    await page.context().setOffline(true);
    
    // Navigate to app
    await page.goto('http://localhost:3000');
    
    // Check offline banner
    await expect(page.locator('[data-testid="offline-banner"]')).toBeVisible();
    
    // Test cached content access
    await page.goto('http://localhost:3000/session/breathing-exercise');
    await expect(page.locator('[data-testid="audio-player"]')).toBeVisible();
    
    // Restore online
    await page.context().setOffline(false);
    await expect(page.locator('[data-testid="offline-banner"]')).not.toBeVisible();
  });

  test('Responsive design', async ({ page }) => {
    // Test mobile view
    await page.setViewportSize({ width: 375, height: 812 });
    await expect(page.locator('[data-testid="mobile-nav"]')).toBeVisible();
    
    // Test tablet view
    await page.setViewportSize({ width: 768, height: 1024 });
    await expect(page.locator('[data-testid="tablet-layout"]')).toBeVisible();
    
    // Test desktop view
    await page.setViewportSize({ width: 1920, height: 1080 });
    await expect(page.locator('[data-testid="desktop-layout"]')).toBeVisible();
  });

  test('Performance metrics', async ({ page }) => {
    // Measure page load performance
    const startTime = Date.now();
    await page.goto('http://localhost:3000');
    const loadTime = Date.now() - startTime;
    
    // Should load within 3 seconds
    expect(loadTime).toBeLessThan(3000);
    
    // Check Core Web Vitals
    const metrics = await page.evaluate(() => {
      return performance.getEntriesByType('navigation')[0];
    });
    
    expect(metrics.loadEventEnd - metrics.loadEventStart).toBeLessThan(1000);
  });

  test('Accessibility compliance', async ({ page }) => {
    // Check ARIA labels
    const buttons = await page.locator('button').all();
    for (const button of buttons) {
      const hasAria = await button.getAttribute('aria-label');
      const hasText = await button.textContent();
      expect(hasAria || hasText).toBeTruthy();
    }
    
    // Check keyboard navigation
    await page.keyboard.press('Tab');
    await expect(page.locator(':focus')).toBeVisible();
    
    // Test screen reader compatibility
    const headings = await page.locator('h1, h2, h3, h4, h5, h6').all();
    expect(headings.length).toBeGreaterThan(0);
  });

  test('Error handling', async ({ page }) => {
    // Test network error
    await page.route('**/api/**', route => route.abort());
    
    await page.goto('http://localhost:3000/dashboard');
    await expect(page.locator('[data-testid="error-message"]')).toBeVisible();
    
    // Test recovery
    await page.unroute('**/api/**');
    await page.click('[data-testid="retry-button"]');
    await expect(page.locator('[data-testid="error-message"]')).not.toBeVisible();
  });

  test('Security features', async ({ page }) => {
    // Test XSS prevention
    await page.goto('http://localhost:3000/chat');
    await page.fill('[data-testid="message-input"]', '<script>alert("XSS")</script>');
    await page.click('[data-testid="send-button"]');
    
    // Verify script not executed
    await expect(page.locator('.user-message')).not.toContainText('<script>');
    
    // Test CSRF protection
    const response = await page.goto('http://localhost:3000/api/chat');
    expect(response?.status()).toBe(403); // Should require CSRF token
  });
});

// Test configuration
test.describe.configure({
  retries: 2,
  timeout: 30000,
});
