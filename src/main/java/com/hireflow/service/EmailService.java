package com.hireflow.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@hireflow.dev}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Async
    public void sendVerificationOtp(String toEmail, String otp) {
        String subject = "HireFlow - Verify Your Email";
        String content = buildOtpEmailHtml(otp);
        sendEmail(toEmail, subject, content);
    }

    @Async
    public void sendWelcomeEmail(String toEmail, String name) {
        String subject = "Welcome to HireFlow!";
        String content = buildWelcomeEmailHtml(name);
        sendEmail(toEmail, subject, content);
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        String resetUrl = baseUrl + "/api/v1/auth/reset-password?token=" + resetToken;
        String subject = "HireFlow - Reset Your Password";
        String content = buildPasswordResetEmailHtml(resetUrl);
        sendEmail(toEmail, subject, content);
    }

    @Async
    public void sendApplicationConfirmation(String toEmail, String jobTitle, String companyName) {
        String subject = "Application Submitted - " + jobTitle;
        String content = buildApplicationConfirmationHtml(jobTitle, companyName);
        sendEmail(toEmail, subject, content);
    }

    @Async
    public void sendStatusUpdateEmail(String toEmail, String jobTitle, String newStatus) {
        String subject = "Application Update - " + jobTitle;
        String content = buildStatusUpdateEmailHtml(jobTitle, newStatus);
        sendEmail(toEmail, subject, content);
    }

    @Async
    public void sendInterviewInvite(String toEmail, String jobTitle, String companyName,
                                     String scheduledAt, String type, String meetingLink) {
        String subject = "Interview Scheduled - " + jobTitle + " at " + companyName;
        String content = buildInterviewInviteHtml(jobTitle, companyName, scheduledAt, type, meetingLink);
        sendEmail(toEmail, subject, content);
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, "HireFlow");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Email sent to {} with subject: {}", to, subject);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String buildOtpEmailHtml(String otp) {
        return """
            <!DOCTYPE html>
            <html><body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
            <div style="max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; padding: 30px;">
                <h2 style="color: #6366f1;">Verify Your Email</h2>
                <p>Thank you for signing up for HireFlow! Use the OTP below to verify your email address.</p>
                <div style="background: #f0f0ff; border-radius: 8px; padding: 20px; text-align: center; margin: 20px 0;">
                    <h1 style="color: #6366f1; letter-spacing: 8px; font-size: 36px;">%s</h1>
                </div>
                <p style="color: #666;">This OTP expires in <strong>15 minutes</strong>. Do not share it with anyone.</p>
                <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                <p style="color: #999; font-size: 12px;">HireFlow — Your Dream Job Awaits</p>
            </div>
            </body></html>
            """.formatted(otp);
    }

    private String buildWelcomeEmailHtml(String name) {
        return """
            <!DOCTYPE html>
            <html><body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
            <div style="max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; padding: 30px;">
                <h2 style="color: #6366f1;">Welcome to HireFlow, %s! 🎉</h2>
                <p>Your account is now verified and ready to use.</p>
                <p>Start exploring thousands of job opportunities tailored for you.</p>
                <a href="%s" style="background: #6366f1; color: white; padding: 12px 24px; border-radius: 6px; text-decoration: none; display: inline-block; margin-top: 10px;">Explore Jobs</a>
                <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                <p style="color: #999; font-size: 12px;">HireFlow — Your Dream Job Awaits</p>
            </div>
            </body></html>
            """.formatted(name, baseUrl);
    }

    private String buildPasswordResetEmailHtml(String resetUrl) {
        return """
            <!DOCTYPE html>
            <html><body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
            <div style="max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; padding: 30px;">
                <h2 style="color: #6366f1;">Reset Your Password</h2>
                <p>We received a request to reset your password. Click the button below to proceed.</p>
                <a href="%s" style="background: #6366f1; color: white; padding: 12px 24px; border-radius: 6px; text-decoration: none; display: inline-block; margin-top: 10px;">Reset Password</a>
                <p style="color: #666; margin-top: 20px;">This link expires in <strong>30 minutes</strong>. If you didn't request this, ignore this email.</p>
                <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                <p style="color: #999; font-size: 12px;">HireFlow — Your Dream Job Awaits</p>
            </div>
            </body></html>
            """.formatted(resetUrl);
    }

    private String buildApplicationConfirmationHtml(String jobTitle, String companyName) {
        return """
            <!DOCTYPE html>
            <html><body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
            <div style="max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; padding: 30px;">
                <h2 style="color: #6366f1;">Application Submitted ✅</h2>
                <p>Your application for <strong>%s</strong> at <strong>%s</strong> has been successfully submitted!</p>
                <p>The recruiter will review your application and get back to you soon.</p>
                <p style="color: #666; margin-top: 20px;">Track your application status in your HireFlow dashboard.</p>
                <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                <p style="color: #999; font-size: 12px;">HireFlow — Your Dream Job Awaits</p>
            </div>
            </body></html>
            """.formatted(jobTitle, companyName);
    }

    private String buildStatusUpdateEmailHtml(String jobTitle, String status) {
        String statusColor = switch (status) {
            case "SHORTLISTED" -> "#10b981";
            case "HIRED" -> "#6366f1";
            case "REJECTED" -> "#ef4444";
            default -> "#6b7280";
        };

        return """
            <!DOCTYPE html>
            <html><body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
            <div style="max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; padding: 30px;">
                <h2 style="color: #6366f1;">Application Status Update</h2>
                <p>Your application for <strong>%s</strong> has been updated:</p>
                <div style="background: #f9fafb; border-left: 4px solid %s; padding: 15px; border-radius: 4px; margin: 20px 0;">
                    <strong style="color: %s; font-size: 18px;">%s</strong>
                </div>
                <p style="color: #666;">Log in to HireFlow to view more details.</p>
                <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                <p style="color: #999; font-size: 12px;">HireFlow — Your Dream Job Awaits</p>
            </div>
            </body></html>
            """.formatted(jobTitle, statusColor, statusColor, status);
    }

    private String buildInterviewInviteHtml(String jobTitle, String companyName,
                                              String scheduledAt, String type, String meetingLink) {
        return """
            <!DOCTYPE html>
            <html><body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
            <div style="max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; padding: 30px;">
                <h2 style="color: #6366f1;">Interview Scheduled 📅</h2>
                <p>Congratulations! You've been invited for an interview at <strong>%s</strong> for <strong>%s</strong>.</p>
                <div style="background: #f0f0ff; border-radius: 8px; padding: 20px; margin: 20px 0;">
                    <p><strong>📅 When:</strong> %s</p>
                    <p><strong>🎙 Type:</strong> %s</p>
                    %s
                </div>
                <p style="color: #666;">Best of luck! Prepare well and show your best self.</p>
                <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                <p style="color: #999; font-size: 12px;">HireFlow — Your Dream Job Awaits</p>
            </div>
            </body></html>
            """.formatted(
                companyName, jobTitle, scheduledAt, type,
                meetingLink != null ? "<p><strong>🔗 Link:</strong> <a href='" + meetingLink + "'>" + meetingLink + "</a></p>" : ""
            );
    }
}
