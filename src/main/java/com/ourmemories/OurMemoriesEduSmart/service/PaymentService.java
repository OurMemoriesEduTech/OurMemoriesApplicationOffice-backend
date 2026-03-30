package com.ourmemories.OurMemoriesEduSmart.service;

import com.ourmemories.OurMemoriesEduSmart.dto.PaymentInitiateRequest;
import com.ourmemories.OurMemoriesEduSmart.dto.PaymentResponse;
import com.ourmemories.OurMemoriesEduSmart.dto.PaymentVerificationRequest;
import com.ourmemories.OurMemoriesEduSmart.model.Application;
import com.ourmemories.OurMemoriesEduSmart.model.ApplicationStatus;
import com.ourmemories.OurMemoriesEduSmart.model.Payment;
import com.ourmemories.OurMemoriesEduSmart.model.PaymentStatus;
import com.ourmemories.OurMemoriesEduSmart.model.User;
import com.ourmemories.OurMemoriesEduSmart.repository.ApplicationRepository;
import com.ourmemories.OurMemoriesEduSmart.repository.PaymentRepository;
import com.ourmemories.OurMemoriesEduSmart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${payment.proof.upload.path:./uploads/payment-proofs}")
    private String proofUploadPath;

    @Value("${payment.expiration.hours:48}")
    private int paymentExpirationHours;

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    private final PaymentRepository paymentRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final ApplicationFeeService feeService;
    private final EmailService emailService;

    // ==================== EFT PAYMENT METHODS ====================

    public Map<String, Object> getEFTInstructions() {
        Map<String, Object> instructions = new HashMap<>();
        instructions.put("bankName", "Standard Bank");
        instructions.put("accountName", "OurMemories EduSmart (Pty) Ltd");
        instructions.put("accountNumber", "123456789");
        instructions.put("accountType", "Cheque Account");
        instructions.put("branchCode", "052852");
        instructions.put("branchName", "Johannesburg");
        instructions.put("swiftCode", "SBZAZAJJ");
        instructions.put("reference", "Your Email Address or Application ID");
        instructions.put("amountNote", "Pay exactly the amount calculated for your applications");
        instructions.put("deadline", paymentExpirationHours + " hours after application submission");

        List<Map<String, String>> steps = new ArrayList<>();
        steps.add(Map.of("step", "1", "instruction", "Make an EFT payment using the bank details above"));
        steps.add(Map.of("step", "2", "instruction", "Use your registered email address as payment reference"));
        steps.add(Map.of("step", "3", "instruction", "Save the proof of payment (screenshot or bank statement)"));
        steps.add(Map.of("step", "4", "instruction", "Upload the proof of payment using the form below"));
        steps.add(Map.of("step", "5", "instruction", "Wait for verification (24-48 hours)"));
        instructions.put("steps", steps);

        instructions.put("importantNotes", List.of(
                "Please use your registered email as reference to help us identify your payment",
                "Keep your proof of payment until your application is processed",
                "Payments without proof will not be processed",
                "Contact support if you don't receive verification within 48 hours"
        ));

        return instructions;
    }

    public Map<String, BigDecimal> getFeeStructure() {
        return feeService.getFeeStructure();
    }

    @Transactional
    public PaymentResponse initiateEFTPayment(PaymentInitiateRequest request, String userEmail) {
        log.info("Initiating EFT payment for application: {}", request.getApplicationId());

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        Application application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new RuntimeException("Application not found with ID: " + request.getApplicationId()));

        if (!application.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: This application does not belong to the user");
        }

        if (paymentRepository.existsByApplicationIdAndStatus(application.getId(), PaymentStatus.VERIFIED)) {
            throw new RuntimeException("Payment already completed for this application");
        }

        Optional<Payment> existingPayment = paymentRepository.findByApplicationId(application.getId());
        if (existingPayment.isPresent()) {
            Payment existing = existingPayment.get();
            if (existing.getStatus() == PaymentStatus.PENDING ||
                    existing.getStatus() == PaymentStatus.AWAITING_VERIFICATION) {
                throw new RuntimeException("You already have a pending payment. Please upload proof or wait for verification.");
            }
        }

        Payment payment = Payment.builder()
                .user(user)
                .application(application)
                .amount(application.getApplicationFee())
                .paymentMethod("EFT")
                .paymentReference(request.getPaymentReference())
                .status(PaymentStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusHours(paymentExpirationHours))
                .build();

        payment = paymentRepository.save(payment);

        application.setPaymentMethod("EFT");
        application.setPayment(payment);
        applicationRepository.save(application);

        emailService.sendEFTPaymentInstructionsEmail(
                user.getEmail(),
                application.getId(),
                application.getApplicationFee().doubleValue(),
                payment.getId()
        );

        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getId());
        response.setStatus(payment.getStatus().name());
        response.setAmount(application.getApplicationFee().doubleValue());
        response.setPaymentMethod("EFT");
        response.setMessage("Please upload proof of payment for verification. You have " +
                paymentExpirationHours + " hours to complete this.");

        log.info("EFT Payment initiated successfully. Payment ID: {}", payment.getId());

        return response;
    }

    @Transactional
    public Payment uploadProofOfPayment(Long paymentId, MultipartFile file, String userEmail,
                                        String ipAddress, String userAgent) throws IOException {
        log.info("Uploading proof of payment for payment: {}", paymentId);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (!payment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: This payment does not belong to the user");
        }

        if (payment.getStatus() == PaymentStatus.VERIFIED) {
            throw new RuntimeException("Payment already completed. Cannot upload new proof.");
        }

        if (payment.isExpired()) {
            payment.setStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(payment);

            Application application = payment.getApplication();
            application.setStatus(ApplicationStatus.CANCELLED);
            applicationRepository.save(application);

            throw new RuntimeException("Payment has expired. Please start a new application.");
        }

        validateFile(file);

        Path uploadDir = Paths.get(proofUploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String fileName = generateFileName(file.getOriginalFilename());
        Path filePath = uploadDir.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        payment.setProofOfPaymentPath(filePath.toString());
        payment.setStatus(PaymentStatus.AWAITING_VERIFICATION);
        payment.setProofUploadedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        Application application = payment.getApplication();
        applicationRepository.save(application);

        emailService.sendProofUploadConfirmationEmail(
                user.getEmail(),
                paymentId,
                application.getId()
        );

        log.info("Proof of payment uploaded successfully for payment: {}", paymentId);

        return payment;
    }

    @Transactional
    public Payment verifyEFTPayment(Long paymentId, PaymentVerificationRequest request, String adminEmail) {

        log.info("Verifying EFT payment: {} with status: {} by admin: {}",
                paymentId, request.getStatus(), adminEmail);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        Application application = payment.getApplication();

        if ("VERIFIED".equals(request.getStatus())) {
            payment.setStatus(PaymentStatus.VERIFIED);
            payment.setVerificationDate(LocalDateTime.now());
            payment.setVerifiedBy(adminEmail);

            // Update application to PAYMENT_VERIFIED
            application.setStatus(ApplicationStatus.PAYMENT_VERIFIED);

            log.info("EFT Payment {} verified successfully", paymentId);

            emailService.sendPaymentVerifiedEmail(payment.getUser().getEmail(),application.getId(),paymentId);

        } else if ("REJECTED".equals(request.getStatus())) {
            payment.setStatus(PaymentStatus.REJECTED);
            payment.setVerificationDate(LocalDateTime.now());
            payment.setVerifiedBy(adminEmail);
            payment.setRejectionReason(request.getNotes());

            // Keep application status as PENDING_PAYMENT so user can retry
            application.setStatus(ApplicationStatus.PENDING_PAYMENT);

            log.warn("EFT Payment {} rejected. Reason: {}", paymentId, request.getNotes());

            emailService.sendPaymentVerifiedEmail(
                    payment.getUser().getEmail(),
                    paymentId,
                    application.getId());
        }

        paymentRepository.save(payment);
        applicationRepository.save(application);

        return payment;
    }

    public List<Payment> getUserEFTPayments(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return paymentRepository.findEFTPaymentsByUserId(user.getId());
    }

    public Payment getEFTPaymentDetails(Long paymentId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (!payment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to payment details");
        }

        if (!"EFT".equals(payment.getPaymentMethod())) {
            throw new RuntimeException("This is not an EFT payment");
        }

        return payment;
    }

    public List<Payment> getPendingEFTVerifications() {
        return paymentRepository.findByPaymentMethodAndStatusOrderByCreatedAtDesc("EFT", PaymentStatus.AWAITING_VERIFICATION);
    }
    public Map<String, Object> getEFTStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Use Enum values for counting
        long pendingCount = paymentRepository.countByPaymentMethodAndStatus("EFT", PaymentStatus.PENDING);
        long awaitingVerificationCount = paymentRepository.countByPaymentMethodAndStatus("EFT", PaymentStatus.AWAITING_VERIFICATION);
        long paidCount = paymentRepository.countByPaymentMethodAndStatus("EFT", PaymentStatus.VERIFIED);
        long rejectedCount = paymentRepository.countByPaymentMethodAndStatus("EFT", PaymentStatus.REJECTED);
        long expiredCount = paymentRepository.countByPaymentMethodAndStatus("EFT", PaymentStatus.EXPIRED);

        stats.put("pending", pendingCount);
        stats.put("awaitingVerification", awaitingVerificationCount);
        stats.put("paid", paidCount);
        stats.put("rejected", rejectedCount);
        stats.put("expired", expiredCount);
        stats.put("total", pendingCount + awaitingVerificationCount + paidCount + rejectedCount + expiredCount);

        // Use Enum for sum amount
        Double totalPaidAmount = paymentRepository.sumAmountByPaymentMethodAndStatus("EFT", PaymentStatus.VERIFIED);
        stats.put("totalPaidAmount", totalPaidAmount != null ? totalPaidAmount : 0.00);

        // Use Enum for average verification time
        Double avgVerificationTime = paymentRepository.getAverageVerificationTime(PaymentStatus.VERIFIED.name());
        stats.put("averageVerificationHours", avgVerificationTime != null ? avgVerificationTime : 0.00);

        // Use Enum for today's revenue
        try {
            Double todayRevenue = paymentRepository.getTodayTotalRevenue(PaymentStatus.VERIFIED);
            stats.put("todayRevenue", todayRevenue != null ? todayRevenue : 0.00);
        } catch (Exception e) {
            stats.put("todayRevenue", 0.00);
        }

        // Use Enum for this month's revenue
        try {
            Double thisMonthRevenue = paymentRepository.getThisMonthTotalRevenue(PaymentStatus.VERIFIED);
            stats.put("thisMonthRevenue", thisMonthRevenue != null ? thisMonthRevenue : 0.00);
        } catch (Exception e) {
            stats.put("thisMonthRevenue", 0.00);
        }

        return stats;
    }

    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    public byte[] downloadProofOfPaymentBytes(Long paymentId) throws IOException {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));

        if (payment.getProofOfPaymentPath() == null) {
            throw new RuntimeException("No proof of payment found for payment: " + paymentId);
        }

        Path filePath = Paths.get(payment.getProofOfPaymentPath());
        if (!Files.exists(filePath)) {
            throw new RuntimeException("Proof of payment file not found at: " + filePath);
        }

        log.info("Reading file from: {}", filePath);
        return Files.readAllBytes(filePath);
    }

    @Transactional
    public int expirePendingPayments() {
        List<Payment> expiredPayments = paymentRepository.findExpiredPayments(PaymentStatus.PENDING, LocalDateTime.now());

        for (Payment payment : expiredPayments) {
            payment.setStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(payment);

            Application application = payment.getApplication();
            if (application != null) {
                application.setStatus(ApplicationStatus.CANCELLED);
                applicationRepository.save(application);
            }

            log.info("Payment expired: {}", payment.getId());
        }

        return expiredPayments.size();
    }

    // ==================== HELPER METHODS ====================

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String contentType = file.getContentType();
        List<String> allowedTypes = Arrays.asList(
                "application/pdf",
                "image/jpeg",
                "image/jpg",
                "image/png"
        );

        if (contentType == null || !allowedTypes.contains(contentType.toLowerCase())) {
            throw new RuntimeException("Only PDF, JPG, and PNG files are allowed");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("File size must be less than 5MB");
        }
    }

    private String generateFileName(String originalFileName) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString();
        String extension = "";

        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        return "proof_" + uuid + "_" + timestamp + extension;
    }

    public List<Payment> getAllEFTPayments() {
        return paymentRepository.findByPaymentMethodOrderByCreatedAtDesc("EFT");
    }
}