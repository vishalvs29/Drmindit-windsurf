const nodemailer = require('nodemailer');
const { logger } = require('./logger');

/**
 * Email Service Utility
 * Handles email sending functionality for the application
 */

class EmailService {
    constructor() {
        this.transporter = null;
        this.initializeTransporter();
    }
    
    /**
     * Initialize email transporter
     */
    initializeTransporter() {
        try {
            // Create transporter with environment variables
            this.transporter = nodemailer.createTransport({
                host: process.env.SMTP_HOST || 'smtp.gmail.com',
                port: process.env.SMTP_PORT || 587,
                secure: process.env.SMTP_SECURE === 'true',
                auth: {
                    user: process.env.SMTP_USER || '',
                    pass: process.env.SMTP_PASS || ''
                }
            });
            
            logger.info('Email transporter initialized');
            
        } catch (error) {
            logger.error('Failed to initialize email transporter', { error: error.message });
            this.transporter = null;
        }
    }
    
    /**
     * Send email
     */
    async sendEmail(to, subject, html, text = null) {
        try {
            if (!this.transporter) {
                logger.warn('Email transporter not available, skipping email send');
                return { success: false, message: 'Email service not available' };
            }
            
            const mailOptions = {
                from: process.env.EMAIL_FROM || 'noreply@drmindit.com',
                to: to,
                subject: subject,
                html: html,
                text: text || html.replace(/<[^>]*>/g, '') // Strip HTML for text version
            };
            
            const result = await this.transporter.sendMail(mailOptions);
            
            logger.info('Email sent successfully', { 
                to, 
                subject, 
                messageId: result.messageId 
            });
            
            return { success: true, messageId: result.messageId };
            
        } catch (error) {
            logger.error('Failed to send email', { 
                to, 
                subject, 
                error: error.message 
            });
            
            return { 
                success: false, 
                error: error.message 
            };
        }
    }
    
    /**
     * Send verification email
     */
    async sendVerificationEmail(email, verificationToken) {
        const subject = 'Verify your DrMindit account';
        const verificationUrl = `${process.env.FRONTEND_URL}/verify-email?token=${verificationToken}`;
        
        const html = `
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <h2 style="color: #4a90e2;">Welcome to DrMindit!</h2>
                <p>Thank you for signing up for DrMindit. Please click the button below to verify your email address:</p>
                <div style="text-align: center; margin: 30px 0;">
                    <a href="${verificationUrl}" 
                       style="background-color: #4a90e2; color: white; padding: 12px 30px; 
                              text-decoration: none; border-radius: 5px; display: inline-block;">
                        Verify Email Address
                    </a>
                </div>
                <p>Or copy and paste this link into your browser:</p>
                <p style="word-break: break-all; color: #666;">${verificationUrl}</p>
                <p style="color: #999; font-size: 12px; margin-top: 30px;">
                    This link will expire in 24 hours. If you didn't create an account with DrMindit, 
                    please ignore this email.
                </p>
            </div>
        `;
        
        return await this.sendEmail(email, subject, html);
    }
    
    /**
     * Send password reset email
     */
    async sendPasswordResetEmail(email, resetToken) {
        const subject = 'Reset your DrMindit password';
        const resetUrl = `${process.env.FRONTEND_URL}/reset-password?token=${resetToken}`;
        
        const html = `
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <h2 style="color: #4a90e2;">Reset Your Password</h2>
                <p>You requested to reset your password for your DrMindit account. Click the button below to reset it:</p>
                <div style="text-align: center; margin: 30px 0;">
                    <a href="${resetUrl}" 
                       style="background-color: #4a90e2; color: white; padding: 12px 30px; 
                              text-decoration: none; border-radius: 5px; display: inline-block;">
                        Reset Password
                    </a>
                </div>
                <p>Or copy and paste this link into your browser:</p>
                <p style="word-break: break-all; color: #666;">${resetUrl}</p>
                <p style="color: #999; font-size: 12px; margin-top: 30px;">
                    This link will expire in 1 hour. If you didn't request a password reset, 
                    please ignore this email.
                </p>
            </div>
        `;
        
        return await this.sendEmail(email, subject, html);
    }
    
    /**
     * Test email configuration
     */
    async testEmailConfiguration() {
        try {
            if (!this.transporter) {
                return { success: false, message: 'Email transporter not configured' };
            }
            
            await this.transporter.verify();
            return { success: true, message: 'Email configuration is valid' };
            
        } catch (error) {
            logger.error('Email configuration test failed', { error: error.message });
            return { success: false, error: error.message };
        }
    }
}

// Create singleton instance
const emailService = new EmailService();

module.exports = emailService;
