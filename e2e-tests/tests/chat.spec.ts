import { test, expect } from '@playwright/test';

test.describe('Chat & Crisis Detection', () => {
  test('user can send chat messages', async ({ page }) => {
    // Sign in
    await page.goto('/');
    await page.fill('[data-testid="email-input"]', 'test@drmindit.com');
    await page.fill('[data-testid="password-input"]', 'TestPassword123!');
    await page.click('[data-testid="sign-in-submit"]');
    
    // Navigate to chat
    await page.click('[data-testid="chat-button"]');
    
    // Type a message
    await page.fill('[data-testid="chat-input"]', 'I am feeling anxious today');
    await page.click('[data-testid="send-message-button"]');
    
    // Message should appear in chat
    await expect(page.locator('[data-testid="user-message"]').last()).toContainText('I am feeling anxious today');
    
    // Should receive AI response
    await expect(page.locator('[data-testid="ai-message"]').last()).toBeVisible();
    await expect(page.locator('[data-testid="ai-message"]').last()).toContainText('overwhelming');
  });

  test('crisis detection shows banner for high risk', async ({ page }) => {
    await page.goto('/');
    await page.fill('[data-testid="email-input"]', 'test@drmindit.com');
    await page.fill('[data-testid="password-input"]', 'TestPassword123!');
    await page.click('[data-testid="sign-in-submit"]');
    
    await page.click('[data-testid="chat-button"]');
    
    // Type crisis message
    await page.fill('[data-testid="chat-input"]', 'I want to hurt myself');
    
    // Should show crisis banner (not dialog for high level)
    await expect(page.locator('[data-testid="crisis-banner"]')).toBeVisible();
    await expect(page.locator('[data-testid="crisis-dialog"]')).not.toBeVisible();
    
    // Banner should have supportive message
    await expect(page.locator('[data-testid="crisis-banner"]')).toContainText('I am here with you');
  });

  test('crisis detection shows dialog for immediate risk', async ({ page }) => {
    await page.goto('/');
    await page.fill('[data-testid="email-input"]', 'test@drmindit.com');
    await page.fill('[data-testid="password-input"]', 'TestPassword123!');
    await page.click('[data-testid="sign-in-submit"]');
    
    await page.click('[data-testid="chat-button"]');
    
    // Type immediate crisis message
    await page.fill('[data-testid="chat-input"]', 'I want to kill myself');
    
    // Should show crisis dialog for immediate level
    await expect(page.locator('[data-testid="crisis-dialog"]')).toBeVisible();
    await expect(page.locator('[data-testid="crisis-banner"]')).toBeVisible();
    
    // Dialog should have resources
    await expect(page.locator('[data-testid="crisis-resources"]')).toBeVisible();
    await expect(page.locator('[data-testid="crisis-hotline"]')).toContainText('988');
  });

  test('safe messaging filter works correctly', async ({ page }) => {
    await page.goto('/');
    await page.fill('[data-testid="email-input"]', 'test@drmindit.com');
    await page.fill('[data-testid="password-input"]', 'TestPassword123!');
    await page.click('[data-testid="sign-in-submit"]');
    
    await page.click('[data-testid="chat-button"]');
    
    // Type message with harmful phrase
    await page.fill('[data-testid="chat-input"]', 'I want to end my life');
    
    // AI response should use safe alternative
    await page.click('[data-testid="send-message-button"]');
    
    // Should contain safe messaging
    await expect(page.locator('[data-testid="ai-message"]').last()).toContainText('end your suffering');
    await expect(page.locator('[data-testid="ai-message"]').last()).not.toContainText('end my life');
  });

  test('typing triggers real-time crisis detection', async ({ page }) => {
    await page.goto('/');
    await page.fill('[data-testid="email-input"]', 'test@drmindit.com');
    await page.fill('[data-testid="password-input"]', 'TestPassword123!');
    await page.click('[data-testid="sign-in-submit"]');
    
    await page.click('[data-testid="chat-button"]');
    
    // Start typing crisis message
    await page.fill('[data-testid="chat-input"]', 'I feel hopeless');
    await page.waitForTimeout(100);
    
    // Should show banner while typing (before sending)
    await expect(page.locator('[data-testid="crisis-banner"]')).toBeVisible();
    
    // Banner should dismiss after sending
    await page.click('[data-testid="send-message-button"]');
    await expect(page.locator('[data-testid="crisis-banner"]')).not.toBeVisible();
  });
});
