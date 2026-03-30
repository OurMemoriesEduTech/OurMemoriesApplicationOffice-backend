package com.ourmemories.OurMemoriesEduSmart.scheduler;

import com.ourmemories.OurMemoriesEduSmart.model.ApplicationStatus;
import com.ourmemories.OurMemoriesEduSmart.model.Payment;
import com.ourmemories.OurMemoriesEduSmart.model.PaymentStatus;
import com.ourmemories.OurMemoriesEduSmart.repository.PaymentRepository;
import com.ourmemories.OurMemoriesEduSmart.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentExpirationScheduler {

    private final PaymentRepository paymentRepository;
    private final EmailNotificationService emailService;

    /**
     * Run every hour to check for expired payments
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour at minute 0
    @Transactional
    public void expirePendingPayments() {
        log.info("Running payment expiration scheduler...");

        // FIXED: Use PaymentStatus enum instead of String
        List<Payment> expiredPayments = paymentRepository.findExpiredPayments(
                PaymentStatus.PENDING,
                LocalDateTime.now()
        );

        for (Payment payment : expiredPayments) {
            // FIXED: Set status to EXPIRED, not PENDING
            payment.setStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(payment);

            if (payment.getApplication() != null) {
                // FIXED: Set application status to CANCELLED (not PENDING_PAYMENT)
                payment.getApplication().setStatus(ApplicationStatus.CANCELLED);
                payment.getApplication().setPayment(null); // Clear payment reference

                // Send expiration email
                emailService.sendPaymentExpirationEmail(
                        payment.getUser().getEmail(),
                        payment.getApplication().getId(),
                        payment.getId()
                );
            }

            log.info("Expired payment: ID={}, User={}, Created={}",
                    payment.getId(),
                    payment.getUser().getEmail(),
                    payment.getCreatedAt());
        }

        if (!expiredPayments.isEmpty()) {
            log.info("Expired {} pending payments", expiredPayments.size());
        }
    }

    /**
     * Send reminders for payments expiring in 24 hours
     * Runs every 6 hours
     */
    @Scheduled(cron = "0 0 */6 * * *") // Every 6 hours
    @Transactional
    public void sendExpirationReminders() {
        log.info("Running payment reminder scheduler...");

        LocalDateTime reminderThreshold = LocalDateTime.now().plusHours(24);
        LocalDateTime endThreshold = LocalDateTime.now().plusHours(25);

        // FIXED: Use PaymentStatus enum instead of String
        List<Payment> paymentsAboutToExpire = paymentRepository.findByStatusAndExpiresAtBetween(
                PaymentStatus.PENDING,
                reminderThreshold,
                endThreshold
        );

        for (Payment payment : paymentsAboutToExpire) {
            emailService.sendPaymentReminderEmail(
                    payment.getUser().getEmail(),
                    payment.getApplication().getId(),
                    payment.getAmount().doubleValue(),
                    payment.getId(),
                    payment.getExpiresAt()
            );

            log.info("Sent reminder for payment: {}", payment.getId());
        }

        if (!paymentsAboutToExpire.isEmpty()) {
            log.info("Sent {} payment reminders", paymentsAboutToExpire.size());
        }
    }
}