package com.ourmemories.OurMemoriesEduSmart.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PaymentVerificationRequest {

    @NotNull(message = "Payment ID is required")
    private Long paymentId;

    @NotNull(message = "Verification status is required")
    @Pattern(regexp = "VERIFIED|REJECTED", message = "Status must be VERIFIED or REJECTED")
    private String status;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    // Optional fields for detailed verification
    @Size(max = 500, message = "Rejection reason cannot exceed 500 characters")
    private String rejectionReason;

    private Double verifiedAmount; // In case amount paid doesn't match fee
    private String transactionReference; // Bank transaction reference

    private Boolean sendEmailNotification = true; // Whether to notify user
    private Boolean updateApplicationStatus = true; // Whether to auto-update application

    // For tracking
    private String verificationIpAddress;
    private String verificationUserAgent;
    private LocalDateTime verificationTimestamp;

    // Helper method to get effective rejection reason
    public String getEffectiveRejectionReason() {
        if (rejectionReason != null && !rejectionReason.isEmpty()) {
            return rejectionReason;
        }
        return notes;
    }

    // Helper method to check if verification is approved
    public boolean isApproved() {
        return "VERIFIED".equalsIgnoreCase(status);
    }

    // Helper method to check if verification is rejected
    public boolean isRejected() {
        return "REJECTED".equalsIgnoreCase(status);
    }
}