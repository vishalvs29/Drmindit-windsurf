import { test, expect, type Page } from '@playwright/test';

test.describe('Session Player Flow', () => {
  test('user can start and control audio session', async ({ page }) => {
    // Sign in first
    await page.goto('/');
    await page.fill('[data-testid="email-input"]', 'test@drmindit.com');
    await page.fill('[data-testid="password-input"]', 'TestPassword123!');
    await page.click('[data-testid="sign-in-submit"]');
    
    // Navigate to session player
    await page.click('[data-testid="session-player-button"]');
    
    // Verify session player loads
    await expect(page.locator('[data-testid="session-title"]')).toBeVisible();
    await expect(page.locator('[data-testid="breathing-orb"]')).toBeVisible();
    
    // Test play/pause functionality
    await page.click('[data-testid="play-pause-button"]');
    await expect(page.locator('[data-testid="play-pause-button"]')).toHaveAttribute('data-state', 'playing');
    
    await page.click('[data-testid="play-pause-button"]');
    await expect(page.locator('[data-testid="play-pause-button"]')).toHaveAttribute('data-state', 'paused');
  });

  test('mood rating before session works', async ({ page }) => {
    await page.goto('/');
    await page.fill('[data-testid="email-input"]', 'test@drmindit.com');
    await page.fill('[data-testid="password-input"]', 'TestPassword123!');
    await page.click('[data-testid="sign-in-submit"]');
    
    await page.click('[data-testid="session-player-button"]');
    
    // Should show mood rating dialog
    await expect(page.locator('[data-testid="mood-rating-dialog"]')).toBeVisible();
    
    // Select mood
    await page.click('[data-testid="mood-option-7"]');
    await page.click('[data-testid="mood-continue-button"]');
    
    // Mood dialog should close, session should start
    await expect(page.locator('[data-testid="mood-rating-dialog"]')).not.toBeVisible();
    await expect(page.locator('[data-testid="play-pause-button"]')).toBeVisible();
  });

  test('session progress tracking works', async ({ page }) => {
    await page.goto('/');
    await page.fill('[data-testid="email-input"]', 'test@drmindit.com');
    await page.fill('[data-testid="password-input"]', 'TestPassword123!');
    await page.click('[data-testid="sign-in-submit"]');
    
    await page.click('[data-testid="session-player-button"]');
    
    // Start session
    await page.click('[data-testid="play-pause-button"]');
    
    // Wait for progress
    await page.waitForTimeout(2000);
    
    // Verify progress elements
    await expect(page.locator('[data-testid="progress-bar"]')).toBeVisible();
    await expect(page.locator('[data-testid="timer-display"]')).toBeVisible();
    
    // Progress should update
    const initialProgress = await page.getAttribute('[data-testid="progress-bar"]', 'value');
    await page.waitForTimeout(3000);
    const updatedProgress = await page.getAttribute('[data-testid="progress-bar"]', 'value');
    
    expect(parseFloat(updatedProgress)).toBeGreaterThan(parseFloat(initialProgress || '0'));
  });

  test('session completion triggers mood rating', async ({ page }) => {
    await page.goto('/');
    await page.fill('[data-testid="email-input"]', 'test@drmindit.com');
    await page.fill('[data-testid="password-input"]', 'TestPassword123!');
    await page.click('[data-testid="sign-in-submit"]');
    
    await page.click('[data-testid="session-player-button"]');
    
    // Mock session completion (in real test, would wait for full duration)
    await page.evaluate(() => {
      // Simulate session completion
      window.dispatchEvent(new CustomEvent('session-completed'));
    });
    
    // Should show mood rating after dialog
    await expect(page.locator('[data-testid="mood-rating-after-dialog"]')).toBeVisible();
    
    // Complete mood rating
    await page.click('[data-testid="mood-option-8"]');
    await page.click('[data-testid="mood-complete-button"]');
    
    // Should show completion message
    await expect(page.locator('[data-testid="session-complete-message"]')).toBeVisible();
  });
});
