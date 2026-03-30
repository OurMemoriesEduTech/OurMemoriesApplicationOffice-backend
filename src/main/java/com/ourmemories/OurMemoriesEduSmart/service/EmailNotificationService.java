package com.ourmemories.OurMemoriesEduSmart.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.name:OurMemories EduSmart}")
    private String appName;

    @Value("${support.email:support@ourmemories.co.za}")
    private String supportEmail;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm");

    // ==================== EFT PAYMENT EMAILS ====================

    /**
     * Send EFT payment instructions email
     */
    public void sendEFTPaymentInstructionsEmail(String toEmail, Long applicationId,
                                                Double amount, Long paymentId) {
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

            // Bank details
            Map<String, String> bankDetails = new HashMap<>();
            bankDetails.put("bankName", "Standard Bank");
            bankDetails.put("accountName", "OurMemories EduSmart (Pty) Ltd");
            bankDetails.put("accountNumber", "123456789");
            bankDetails.put("branchCode", "052852");
            bankDetails.put("reference", toEmail);
            context.setVariable("bankDetails", bankDetails);

            String htmlContent = templateEngine.process("eft-instructions", context);

            sendEmail(toEmail, "EFT Payment Instructions - Application #" + applicationId, htmlContent);
            log.info("EFT instructions email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send EFT instructions email: {}", e.getMessage(), e);
        }
    }

    /**
     * Send proof of payment upload confirmation email
     */
    public void sendProofUploadConfirmationEmail(String toEmail, Long paymentId, Long applicationId) {
        try {
            Context context = new Context();
            context.setVariable("paymentId", paymentId);
            context.setVariable("applicationId", applicationId);
            context.setVariable("date", LocalDateTime.now().format(DATE_FORMATTER));
            context.setVariable("trackingUrl", frontendUrl + "/application-portal");
            context.setVariable("supportEmail", supportEmail);
            context.setVariable("appName", appName);
            context.setVariable("estimatedVerificationTime", "24-48 hours");

            String htmlContent = templateEngine.process("proof-upload-confirmation", context);

            sendEmail(toEmail, "Proof of Payment Received - Application #" + applicationId, htmlContent);
            log.info("Proof upload confirmation email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send proof upload confirmation email: {}", e.getMessage(), e);
        }
    }

    /**
     * Send payment verification email (for EFT payments)
     */
    public void sendPaymentVerificationEmail(String toEmail, Long paymentId, String status) {
        try {
            Context context = new Context();
            context.setVariable("paymentId", paymentId);
            context.setVariable("status", status);
            context.setVariable("date", LocalDateTime.now().format(DATE_FORMATTER));
            context.setVariable("portalUrl", frontendUrl + "/application-portal");
            context.setVariable("supportEmail", supportEmail);
            context.setVariable("appName", appName);

            String template = status.equals("VERIFIED") ? "payment-verified" : "payment-rejected";
            String htmlContent = templateEngine.process(template, context);

            String subject = status.equals("VERIFIED") ?
                    "Payment Verified - Your Application is Being Processed" :
                    "Payment Verification Failed - Action Required";

            sendEmail(toEmail, subject, htmlContent);
            log.info("Payment verification email sent to: {} - Status: {}", toEmail, status);

        } catch (Exception e) {
            log.error("Failed to send payment verification email: {}", e.getMessage(), e);
        }
    }

    /**
     * Send payment reminder email (for pending EFT payments)
     */
    public void sendPaymentReminderEmail(String toEmail, Long applicationId,
                                         Double amount, Long paymentId, LocalDateTime expiresAt) {
        try {
            Context context = new Context();
            context.setVariable("applicationId", applicationId);
            context.setVariable("paymentId", paymentId);
            context.setVariable("amount", String.format("R %.2f", amount));
            context.setVariable("expiryDate", expiresAt.format(DATE_FORMATTER));
            context.setVariable("uploadUrl", frontendUrl + "/payment/upload/" + paymentId);
            context.setVariable("paymentUrl", frontendUrl + "/payment/" + paymentId);
            context.setVariable("supportEmail", supportEmail);
            context.setVariable("appName", appName);

            String htmlContent = templateEngine.process("payment-reminder", context);

            sendEmail(toEmail, "Payment Reminder: Complete Your Application Payment", htmlContent);
            log.info("Payment reminder email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send payment reminder email: {}", e.getMessage(), e);
        }
    }

    /**
     * Send payment expiration email
     */
    public void sendPaymentExpirationEmail(String toEmail, Long applicationId, Long paymentId) {
        try {
            Context context = new Context();
            context.setVariable("applicationId", applicationId);
            context.setVariable("paymentId", paymentId);
            context.setVariable("date", LocalDateTime.now().format(DATE_FORMATTER));
            context.setVariable("restartUrl", frontendUrl + "/applynow");
            context.setVariable("supportEmail", supportEmail);
            context.setVariable("appName", appName);

            String htmlContent = templateEngine.process("payment-expired", context);

            sendEmail(toEmail, "Payment Expired - Please Restart Your Application", htmlContent);
            log.info("Payment expiration email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send payment expiration email: {}", e.getMessage(), e);
        }
    }

    // ==================== GENERAL PAYMENT EMAILS ====================

    /**
     * Send payment confirmation email (for successful payments)
     */
    public void sendPaymentConfirmationEmail(String toEmail, Long applicationId, Double amount) {
        try {
            Context context = new Context();
            context.setVariable("applicationId", applicationId);
            context.setVariable("amount", String.format("R %.2f", amount));
            context.setVariable("date", LocalDateTime.now().format(DATE_FORMATTER));
            context.setVariable("trackingUrl", frontendUrl + "/application-portal");
            context.setVariable("supportEmail", supportEmail);
            context.setVariable("appName", appName);

            String htmlContent = templateEngine.process("payment-confirmation", context);

            sendEmail(toEmail, "Payment Confirmation - Application #" + applicationId, htmlContent);
            log.info("Payment confirmation email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send payment confirmation email: {}", e.getMessage(), e);
        }
    }

    /**
     * Send payment failure email (for failed online payments)
     */
    public void sendPaymentFailureEmail(String toEmail, String failureReason) {
        try {
            Context context = new Context();
            context.setVariable("failureReason", failureReason);
            context.setVariable("retryUrl", frontendUrl + "/application-portal");
            context.setVariable("supportEmail", supportEmail);
            context.setVariable("appName", appName);

            String htmlContent = templateEngine.process("payment-failed", context);

            sendEmail(toEmail, "Payment Failed - Please Retry", htmlContent);
            log.info("Payment failure email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send payment failure email: {}", e.getMessage(), e);
        }
    }

    // ==================== APPLICATION EMAILS ====================

    /**
     * Send application submission confirmation email
     */
    public void sendApplicationSubmissionEmail(String toEmail, Long applicationId) {
        try {
            Context context = new Context();
            context.setVariable("applicationId", applicationId);
            context.setVariable("date", LocalDateTime.now().format(DATE_FORMATTER));
            context.setVariable("trackingUrl", frontendUrl + "/application-portal");
            context.setVariable("supportEmail", supportEmail);
            context.setVariable("appName", appName);

            String htmlContent = templateEngine.process("application-submitted", context);

            sendEmail(toEmail, "Application Submitted - Confirmation #" + applicationId, htmlContent);
            log.info("Application submission email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send application submission email: {}", e.getMessage(), e);
        }
    }

    /**
     * Send application status update email
     */
    public void sendApplicationStatusUpdateEmail(String toEmail, Long applicationId,
                                                 String oldStatus, String newStatus) {
        try {
            Context context = new Context();
            context.setVariable("applicationId", applicationId);
            context.setVariable("oldStatus", oldStatus);
            context.setVariable("newStatus", newStatus);
            context.setVariable("date", LocalDateTime.now().format(DATE_FORMATTER));
            context.setVariable("trackingUrl", frontendUrl + "/application-portal");
            context.setVariable("supportEmail", supportEmail);
            context.setVariable("appName", appName);

            String htmlContent = templateEngine.process("application-status-update", context);

            sendEmail(toEmail, "Application Status Update - #" + applicationId, htmlContent);
            log.info("Application status update email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send application status update email: {}", e.getMessage(), e);
        }
    }

    // ==================== ADMIN EMAILS ====================

    /**
     * Send admin notification for new EFT payment pending verification
     */
    public void sendAdminNewPaymentNotification(String adminEmail, Long paymentId,
                                                String userEmail, Double amount) {
        try {
            Context context = new Context();
            context.setVariable("paymentId", paymentId);
            context.setVariable("userEmail", userEmail);
            context.setVariable("amount", String.format("R %.2f", amount));
            context.setVariable("date", LocalDateTime.now().format(DATE_FORMATTER));
            context.setVariable("verificationUrl", frontendUrl + "/admin/payments/" + paymentId);

            String htmlContent = templateEngine.process("admin-new-payment", context);

            sendEmail(adminEmail, "New EFT Payment Awaiting Verification", htmlContent);
            log.info("Admin notification email sent to: {}", adminEmail);

        } catch (Exception e) {
            log.error("Failed to send admin notification email: {}", e.getMessage(), e);
        }
    }

    // ==================== GENERIC EMAIL SENDER ====================

    /**
     * Generic email sender
     */
    private void sendEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send email to " + toEmail, e);
        }
    }

    /**
     * Send email with attachment
     */
    public void sendEmailWithAttachment(String toEmail, String subject, String htmlContent,
                                        byte[] attachment, String attachmentName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.addAttachment(attachmentName, new ByteArrayResource(attachment));

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send email with attachment to " + toEmail, e);
        }
    }

    /**
     * Send bulk email (for admin announcements)
     */
    public void sendBulkEmail(List<String> recipients, String subject, String htmlContent) {
        for (String recipient : recipients) {
            try {
                sendEmail(recipient, subject, htmlContent);
            } catch (Exception e) {
                log.error("Failed to send bulk email to: {}", recipient, e);
            }
        }
        log.info("Bulk email sent to {} recipients", recipients.size());
    }
}
