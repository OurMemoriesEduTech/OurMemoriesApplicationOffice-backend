package com.ourmemories.OurMemoriesEduSmart.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.name:OurMemories EduSmart}")
    private String appName;

    @Value("${support.email:support@ourmemories.co.za}")
    private String supportEmail;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    // ==================== APPLICATION STATUS EMAILS ====================

    @Async
    public void sendApplicationStatusUpdateEmail(String to, Long applicationId,
                                                 String oldStatus, String newStatus) {
        logger.info("Sending status update email for application {} to {}", applicationId, to);

        try {
            Context context = new Context();
            context.setVariable("applicationId", applicationId);
            context.setVariable("oldStatus", oldStatus);
            context.setVariable("newStatus", newStatus);
            context.setVariable("date", LocalDateTime.now().format(DATE_FORMATTER));
            context.setVariable("trackingUrl", frontendUrl + "/application-portal");
            context.setVariable("supportEmail", supportEmail);
            context.setVariable("appName", appName);
            context.setVariable("logoUrl", baseUrl + "/images/logo.png");

            String template = getStatusUpdateTemplate(newStatus);
            String htmlContent = templateEngine.process(template, context);

            String subject = getStatusUpdateSubject(applicationId, newStatus);
            sendEmail(to, subject, htmlContent);

            logger.info("Status update email sent to {} for application {}", to, applicationId);
        } catch (Exception e) {
            logger.error("Failed to send status update email to {}: {}", to, e.getMessage(), e);
        }
    }

    private String getStatusUpdateTemplate(String newStatus) {
        switch (newStatus) {
            case "PAYMENT_VERIFIED":
                return "email/payment-verified";
            case "UNDER_REVIEW":
                return "email/under-review";
            case "APPROVED":
                return "email/application-approved";
            case "REJECTED":
                return "email/application-rejected";
            default:
                return "email/application-status-update";
        }
    }

    private String getStatusUpdateSubject(Long applicationId, String newStatus) {
        switch (newStatus) {
            case "PAYMENT_VERIFIED":
                return "Payment Verified - Application #" + applicationId;
            case "UNDER_REVIEW":
                return "Application Under Review - #" + applicationId;
            case "APPROVED":
                return "Congratulations! Your Application #" + applicationId + " Has Been Approved!";
            case "REJECTED":
                return "Update on Your Application #" + applicationId;
            default:
                return "Application Status Update - #" + applicationId;
        }
    }

    // ==================== PAYMENT EMAILS ====================

    @Async
    public void sendPaymentVerifiedEmail(String to, Long applicationId, Long paymentId) {
        logger.info("Sending payment verified email for application {} to {}", applicationId, to);

        try {
            Context context = new Context();
            context.setVariable("applicationId", applicationId);
            context.setVariable("paymentId", paymentId);
            context.setVariable("date", LocalDateTime.now().format(DATE_FORMATTER));
            context.setVariable("trackingUrl", frontendUrl + "/application-portal");
            context.setVariable("supportEmail", supportEmail);
            context.setVariable("appName", appName);
            context.setVariable("logoUrl", baseUrl + "/images/logo.png");

            String htmlContent = templateEngine.process("email/payment-verified", context);

            sendEmail(to, "Payment Verified - Application #" + applicationId, htmlContent);
            logger.info("Payment verified email sent to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send payment verified email to {}: {}", to, e.getMessage(), e);
        }
    }

    @Async
    public void sendPaymentRejectedEmail(String to, Long applicationId, Long paymentId, String reason) {
        logger.info("Sending payment rejected email for application {} to {}", applicationId, to);

        try {
            Context context = new Context();
            context.setVariable("applicationId", applicationId);
            context.setVariable("paymentId", paymentId);
            context.setVariable("reason", reason);
            context.setVariable("date", LocalDateTime.now().format(DATE_FORMATTER));
            context.setVariable("uploadUrl", frontendUrl + "/application-portal");
            context.setVariable("supportEmail", supportEmail);
            context.setVariable("appName", appName);
            context.setVariable("logoUrl", baseUrl + "/images/logo.png");

            String htmlContent = templateEngine.process("email/payment-rejected", context);

            sendEmail(to, "Action Required: Payment Verification Failed - Application #" + applicationId, htmlContent);
            logger.info("Payment rejected email sent to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send payment rejected email to {}: {}", to, e.getMessage(), e);
        }
    }

    @Async
    public void sendEFTPaymentInstructionsEmail(String to, Long applicationId,
                                                Double amount, Long paymentId) {
        logger.info("Sending EFT instructions email for application {} to {}", applicationId, to);

        try {
            Context context = new Context();
            context.setVariable("applicationId", applicationId);
            context.setVariable("paymentId", paymentId);
            context.setVariable("amount", String.format("R %.2f", amount));
            context.setVariable("date", LocalDateTime.now().format(DATE_FORMATTER));
            context.setVariable("uploadUrl", frontendUrl + "/payment/upload/" + paymentId);
            context.setVariable("trackingUrl", frontendUrl + "/application-portal");
            context.setVariable("supportEmail", supportEmail);
            context.setVariable("appName", appName);
            context.setVariable("logoUrl", baseUrl + "/images/logo.png");

            // Bank details
            Map<String, String> bankDetails = new HashMap<>();
            bankDetails.put("bankName", "Standard Bank");
            bankDetails.put("accountName", appName + " (Pty) Ltd");
            bankDetails.put("accountNumber", "123456789");
            bankDetails.put("branchCode", "052852");
            bankDetails.put("reference", to);
            context.setVariable("bankDetails", bankDetails);

            // Steps
            List<Map<String, String>> steps = List.of(
                    Map.of("step", "1", "instruction", "Make an EFT payment using the bank details above"),
                    Map.of("step", "2", "instruction", "Use your registered email address as payment reference"),
                    Map.of("step", "3", "instruction", "Save the proof of payment (screenshot or bank statement)"),
                    Map.of("step", "4", "instruction", "Upload the proof of payment using the link below"),
                    Map.of("step", "5", "instruction", "Wait for verification (24-48 hours)")
            );
            context.setVariable("steps", steps);

            String htmlContent = templateEngine.process("email/eft-instructions", context);

            sendEmail(to, "EFT Payment Instructions - Application #" + applicationId, htmlContent);
            logger.info("EFT instructions email sent to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send EFT instructions email to {}: {}", to, e.getMessage(), e);
        }
    }

    @Async
    public void sendProofUploadConfirmationEmail(String to, Long paymentId, Long applicationId) {
        logger.info("Sending proof upload confirmation email for payment {} to {}", paymentId, to);

        try {
            Context context = new Context();
            context.setVariable("paymentId", paymentId);
            context.setVariable("applicationId", applicationId);
            context.setVariable("date", LocalDateTime.now().format(DATE_FORMATTER));
            context.setVariable("trackingUrl", frontendUrl + "/application-portal");
            context.setVariable("supportEmail", supportEmail);
            context.setVariable("appName", appName);
            context.setVariable("estimatedVerificationTime", "24-48 hours");
            context.setVariable("logoUrl", baseUrl + "/images/logo.png");

            String htmlContent = templateEngine.process("email/proof-upload-confirmation", context);

            sendEmail(to, "Proof of Payment Received - Application #" + applicationId, htmlContent);
            logger.info("Proof upload confirmation email sent to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send proof upload confirmation email to {}: {}", to, e.getMessage(), e);
        }
    }

    @Async
    public void sendPaymentConfirmationEmail(String to, Long applicationId, Double amount) {
        logger.info("Sending payment confirmation email for application {} to {}", applicationId, to);

        try {
            Context context = new Context();
            context.setVariable("applicationId", applicationId);
            context.setVariable("amount", String.format("R %.2f", amount));
            context.setVariable("date", LocalDateTime.now().format(DATE_FORMATTER));
            context.setVariable("trackingUrl", frontendUrl + "/application-portal");
            context.setVariable("supportEmail", supportEmail);
            context.setVariable("appName", appName);
            context.setVariable("logoUrl", baseUrl + "/images/logo.png");

            String htmlContent = templateEngine.process("email/payment-confirmation", context);

            sendEmail(to, "Payment Confirmation - Application #" + applicationId, htmlContent);
            logger.info("Payment confirmation email sent to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send payment confirmation email to {}: {}", to, e.getMessage(), e);
        }
    }

    // ==================== APPLICATION SUBMISSION EMAILS ====================

    @Async
    public void sendApplicationConfirmation(String to, String applicantName, String referenceNumber,
                                            String submissionDate, List<Map<String, String>> institutions,
                                            String applicationType, String portalLink) {
        logger.info("Sending application confirmation email to: {}", to);

        try {
            Context context = new Context();
            context.setVariable("applicantName", applicantName);
            context.setVariable("referenceNumber", referenceNumber);
            context.setVariable("submissionDate", submissionDate);
            context.setVariable("institutions", institutions);
            context.setVariable("portalLink", portalLink);
            context.setVariable("applicantEmail", to);
            context.setVariable("applicationType", applicationType);
            context.setVariable("supportEmail", supportEmail);
            context.setVariable("appName", appName);
            context.setVariable("logoUrl", baseUrl + "/images/logo.png");

            String htmlContent = templateEngine.process("email/application-confirmation", context);

            sendEmail(to, "Your Application Has Been Submitted Successfully", htmlContent);
            logger.info("Confirmation email successfully sent to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send confirmation email to: {}", to, e);
        }
    }

    @Async
    public void sendApplicationUpdatedEmail(String to, String fullName, String referenceNumber, String applicationLink) {
        logger.info("Sending application updated email to: {}", to);

        try {
            Context context = new Context();
            context.setVariable("fullName", fullName);
            context.setVariable("referenceNumber", referenceNumber);
            context.setVariable("applicationLink", applicationLink);
            context.setVariable("appName", appName);
            context.setVariable("supportEmail", supportEmail);
            context.setVariable("logoUrl", baseUrl + "/images/logo.png");

            String htmlContent = templateEngine.process("email/application-updated", context);

            sendEmail(to, "Your Application Has Been Updated", htmlContent);
            logger.info("Application updated email sent to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send application updated email to: {}", to, e);
        }
    }

    @Async
    public void sendApplicationCancelledEmail(String to, String fullName, String referenceNumber, String dashboardLink) {
        logger.info("Sending application cancelled email to: {}", to);

        try {
            Context context = new Context();
            context.setVariable("fullName", fullName);
            context.setVariable("referenceNumber", referenceNumber);
            context.setVariable("dashboardLink", dashboardLink);
            context.setVariable("appName", appName);
            context.setVariable("supportEmail", supportEmail);
            context.setVariable("logoUrl", baseUrl + "/images/logo.png");

            String htmlContent = templateEngine.process("email/application-cancelled", context);

            sendEmail(to, "Your Application Has Been Cancelled", htmlContent);
            logger.info("Application cancelled email sent to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send application cancelled email to: {}", to, e);
        }
    }

    // ==================== USER ACCOUNT EMAILS ====================

    @Async
    public void sendWelcomeEmail(String to, String fullName, String dashboardLink) {
        logger.info("Sending welcome email to: {}", to);

        try {
            Context context = new Context();
            context.setVariable("fullName", fullName);
            context.setVariable("email", to);
            context.setVariable("dashboardLink", dashboardLink);
            context.setVariable("appName", appName);
            context.setVariable("supportEmail", supportEmail);
            context.setVariable("logoUrl", baseUrl + "/images/logo.png");

            String htmlContent = templateEngine.process("email/signup-confirmation", context);

            sendEmail(to, "Welcome to " + appName + "!", htmlContent);
            logger.info("Welcome email sent to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send welcome email to: {}", to, e);
        }
    }

    @Async
    public void sendPasswordResetOtp(String to, String fullName, String otp, String resetLink) {
        logger.info("Sending password reset OTP email to: {}", to);

        try {
            Context context = new Context();
            context.setVariable("fullName", fullName);
            context.setVariable("otp", otp);
            context.setVariable("resetLink", resetLink);
            context.setVariable("appName", appName);
            context.setVariable("supportEmail", supportEmail);
            context.setVariable("logoUrl", baseUrl + "/images/logo.png");
            context.setVariable("expiryMinutes", 10);

            String htmlContent = templateEngine.process("email/password-reset-otp", context);

            sendEmail(to, "Password Reset Request - " + appName, htmlContent);
            logger.info("Password reset OTP email sent to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send password reset OTP email to: {}", to, e);
        }
    }

    @Async
    public void sendAccountDeletedEmail(String to, String fullName) {
        logger.info("Sending account deleted email to: {}", to);

        try {
            Context context = new Context();
            context.setVariable("fullName", fullName);
            context.setVariable("appName", appName);
            context.setVariable("supportEmail", supportEmail);
            context.setVariable("logoUrl", baseUrl + "/images/logo.png");

            String htmlContent = templateEngine.process("email/account-deleted", context);

            sendEmail(to, "Your Account Has Been Deleted", htmlContent);
            logger.info("Account deleted email sent to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send account deleted email to: {}", to, e);
        }
    }

    // ==================== ADMIN EMAILS ====================

    @Async
    public void sendAdminNewPaymentNotification(String adminEmail, Long paymentId, String userEmail, Double amount) {
        logger.info("Sending admin notification for payment: {}", paymentId);

        try {
            Context context = new Context();
            context.setVariable("paymentId", paymentId);
            context.setVariable("userEmail", userEmail);
            context.setVariable("amount", String.format("R %.2f", amount));
            context.setVariable("date", LocalDateTime.now().format(DATE_FORMATTER));
            context.setVariable("verificationUrl", frontendUrl + "/admin/payments/" + paymentId);
            context.setVariable("appName", appName);
            context.setVariable("logoUrl", baseUrl + "/images/logo.png");

            String htmlContent = templateEngine.process("email/admin-new-payment", context);

            sendEmail(adminEmail, "New EFT Payment Awaiting Verification - #" + paymentId, htmlContent);
            logger.info("Admin notification sent for payment: {}", paymentId);
        } catch (Exception e) {
            logger.error("Failed to send admin notification for payment: {}", paymentId, e);
        }
    }

    // ==================== GENERIC EMAIL SENDER ====================

    private void sendEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.debug("Email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send email to " + toEmail, e);
        }
    }
}