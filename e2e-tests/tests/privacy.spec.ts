import { test, expect } from '@playwright/test';

test.describe('Privacy & Data Management', () => {
  test('user can view all collected data categories', async ({ page }) => {
    // Sign in
    await page.goto('/');
    await page.fill('[data-testid="email-input"]', 'test@drmindit.com');
    await page.fill('[data-testid="password-input"]', 'TestPassword123!');
    await page.click('[data-testid="sign-in-submit"]');
    
    // Navigate to privacy screen
    await page.click('[data-testid="privacy-button"]');
    
    // Should show data categories
    await expect(page.locator('[data-testid="personal-info-category"]')).toBeVisible();
    await expect(page.locator('[data-testid="mood-data-category"]')).toBeVisible();
    await expect(page.locator('[data-testid="chat-history-category"]')).toBeVisible();
    await expect(page.locator('[data-testid="usage-data-category"]')).toBeVisible();
    
    // Should show retention periods
    await expect(page.locator('[data-testid="retention-periods"]')).toBeVisible();
  });

  test('user can delete all data', async ({ page }) => {
    // Sign in
    await page.goto('/');
    await page.fill('[data-testid="email-input"]', 'test@drmindit.com');
    await page.fill('[data-testid="password-input"]', 'TestPassword123!');
    await page.click('[data-testid="sign-in-submit"]');
    
    await page.click('[data-testid="privacy-button"]');
    
    // Click delete all data button
    await page.click('[data-testid="delete-all-data-button"]');
    
    // Should show confirmation dialog
    await expect(page.locator('[data-testid="delete-confirmation-dialog"]')).toBeVisible();
    
    // Confirm deletion
    await page.click('[data-testid="confirm-delete-button"]');
    
    // Should show loading state
    await expect(page.locator('[data-testid="delete-loading"]')).toBeVisible();
    
    // Should redirect to login/home after deletion
    await page.waitForTimeout(2000);
    await expect(page).toHaveURL(/login|home/);
  });

  test('data transparency shows file counts and sizes', async ({ page }) => {
    await page.goto('/');
    await page.fill('[data-testid="email-input"]', 'test@drmindit.com');
    await page.fill('[data-testid="password-input"]', 'TestPassword123!');
    await page.click('[data-testid="sign-in-submit"]');
    
    await page.click('[data-testid="privacy-button"]');
    
    // Should show data metrics
    await expect(page.locator('[data-testid="total-sessions"]')).toBeVisible();
    await expect(page.locator('[data-testid="total-storage-used"]')).toBeVisible();
    await expect(page.locator('[data-testid="data-collection-date"]')).toBeVisible();
    
    // Metrics should be realistic
    const sessionCount = await page.textContent('[data-testid="total-sessions"]');
    expect(parseInt(sessionCount || '0')).toBeGreaterThan(0);
    
    const storageUsed = await page.textContent('[data-testid="total-storage-used"]');
    expect(storageUsed).toMatch(/\d+ MB/);
  });

  test('cancel deletion works correctly', async ({ page }) => {
    await page.goto('/');
    await page.fill('[data-testid="email-input"]', 'test@drmindit.com');
    await page.fill('[data-testid="password-input"]', 'TestPassword123!');
    await page.click('[data-testid="sign-in-submit"]');
    
    await page.click('[data-testid="privacy-button"]');
    
    await page.click('[data-testid="delete-all-data-button"]');
    await expect(page.locator('[data-testid="delete-confirmation-dialog"]')).toBeVisible();
    
    // Cancel deletion
    await page.click('[data-testid="cancel-delete-button"]');
    
    // Dialog should close, no deletion should occur
    await expect(page.locator('[data-testid="delete-confirmation-dialog"]')).not.toBeVisible();
    
    // Should still be on privacy screen
    await expect(page).toHaveURL(/privacy/);
  });
});
