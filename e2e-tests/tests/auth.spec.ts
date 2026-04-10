import { test, expect } from '@playwright/test';

test.describe('Authentication Flow', () => {
  test('user can sign up and sign in', async ({ page }) => {
    // Navigate to landing page
    await page.goto('/');
    
    // Click sign up button
    await page.click('[data-testid="sign-up-button"]');
    
    // Fill sign up form
    await page.fill('[data-testid="email-input"]', 'test@drmindit.com');
    await page.fill('[data-testid="password-input"]', 'TestPassword123!');
    await page.fill('[data-testid="first-name-input"]', 'Test');
    await page.fill('[data-testid="last-name-input"]', 'User');
    
    // Submit form
    await page.click('[data-testid="sign-up-submit"]');
    
    // Wait for success message
    await expect(page.locator('[data-testid="success-message"]')).toBeVisible();
    
    // Sign in
    await page.goto('/');
    await page.click('[data-testid="sign-in-button"]');
    
    await page.fill('[data-testid="email-input"]', 'test@drmindit.com');
    await page.fill('[data-testid="password-input"]', 'TestPassword123!');
    
    await page.click('[data-testid="sign-in-submit"]');
    
    // Verify successful login - should redirect to dashboard
    await expect(page).toHaveURL(/dashboard|home/);
  });

  test('user can sign out', async ({ page }) => {
    // Sign in first
    await page.goto('/');
    await page.fill('[data-testid="email-input"]', 'test@drmindit.com');
    await page.fill('[data-testid="password-input"]', 'TestPassword123!');
    await page.click('[data-testid="sign-in-submit"]');
    
    // Wait for dashboard
    await expect(page).toHaveURL(/dashboard|home/);
    
    // Find and click sign out
    await page.click('[data-testid="user-menu"]');
    await page.click('[data-testid="sign-out-button"]');
    
    // Verify redirected to landing page
    await expect(page).toHaveURL('/');
  });

  test('password validation works', async ({ page }) => {
    await page.goto('/');
    await page.click('[data-testid="sign-up-button"]');
    
    // Test weak password
    await page.fill('[data-testid="email-input"]', 'test@drmindit.com');
    await page.fill('[data-testid="password-input"]', '123');
    await page.click('[data-testid="sign-up-submit"]');
    
    // Should show password error
    await expect(page.locator('[data-testid="password-error"]')).toBeVisible();
    
    // Test strong password
    await page.fill('[data-testid="password-input"]', 'StrongPassword123!');
    await page.click('[data-testid="sign-up-submit"]');
    
    // Should proceed
    await expect(page.locator('[data-testid="success-message"]')).toBeVisible();
  });
});
