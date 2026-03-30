package com.ourmemories.OurMemoriesEduSmart.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_method", columnList = "paymentMethod"),
        @Index(name = "idx_user_payments", columnList = "user_id"),
        @Index(name = "idx_application_payments", columnList = "application_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"password", "enabled", "role", "applications", "payments"})
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    @JsonIgnoreProperties({"user", "applicant", "payment"})
    private Application application;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 20)
    private String paymentMethod; // EFT, ONLINE

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    // EFT specific
    @Column(length = 100)
    private String paymentReference;

    private String proofOfPaymentPath;

    private LocalDateTime proofUploadedAt;

    // Online payment specific
    @Column(length = 255)
    private String paymentIntentId;

    // Tracking
    private LocalDateTime paymentDate;
    private LocalDateTime verificationDate;
    private String verifiedBy;
    private String rejectionReason;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime expiresAt; // 48 hours for EFT

    public void setStatus(String status) {
        this.status = PaymentStatus.valueOf(status);
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = PaymentStatus.PENDING;
        }
        if (expiresAt == null && status == PaymentStatus.PENDING) {
            expiresAt = LocalDateTime.now().plusHours(48);
        }
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public String getDisplayStatus() {
        switch (status) {
            case PENDING:
                return "Payment Pending";
            case AWAITING_VERIFICATION:
                return "Awaiting Verification";
            case VERIFIED:
                return "Payment Confirmed";
            case REJECTED:
                return "Payment Rejected";
            case EXPIRED:
                return "Payment Expired";
            default:
                return status != null ? status.name() : "Unknown";
        }
    }

    public String getStatusColor() {
        switch (status) {
            case PENDING:
                return "warning";
            case AWAITING_VERIFICATION:
                return "info";
            case VERIFIED:
                return "success";
            case REJECTED:
                return "danger";
            case EXPIRED:
                return "secondary";
            default:
                return "secondary";
        }
    }

    public String getStatusIcon() {
        switch (status) {
            case PENDING:
                return "⏳";
            case AWAITING_VERIFICATION:
                return "📎";
            case VERIFIED:
                return "✓";
            case REJECTED:
                return "✗";
            case EXPIRED:
                return "⏰";
            default:
                return "💰";
        }
    }
}