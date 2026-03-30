package com.ourmemories.OurMemoriesEduSmart.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "application", indexes = {
        @Index(name = "idx_application_status", columnList = "status"),
        @Index(name = "idx_application_user", columnList = "user_id"),
        @Index(name = "idx_application_type", columnList = "applicationType")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    private LocalDateTime submittedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApplicationStatus status;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"applications", "guardian"})
    private Applicant applicant;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("application")
    private List<Institution> institutions;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("application")
    private List<Document> documents;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("application")
    private List<Subject> subjects;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnoreProperties({"applications", "payments", "password"})
    private User user;

    @Column(nullable = false, length = 20)
    private String applicationType;

    @Column(nullable = false, length = 20)
    private String educationStatus;

    @Column(nullable = false)
    private Integer numberOfApplications = 1;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal applicationFee;

    @Column(length = 20)
    private String paymentMethod;

    @OneToOne(mappedBy = "application", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("application")
    private Payment payment;

    @PrePersist
    protected void onCreate() {
        if (submittedDate == null) {
            submittedDate = LocalDateTime.now();
        }
        if (status == null) {
            status = ApplicationStatus.PENDING_PAYMENT;
        }
        if (numberOfApplications == null) {
            numberOfApplications = 1;
        }
    }

    // Helper methods
    public boolean canBeEdited() {
        return status == ApplicationStatus.PENDING_PAYMENT ||
                status == ApplicationStatus.PAYMENT_VERIFIED;
    }

    public boolean isPaymentRequired() {
        return status == ApplicationStatus.PENDING_PAYMENT;
    }

    public boolean isComplete() {
        return status == ApplicationStatus.APPROVED ||
                status == ApplicationStatus.REJECTED;
    }

    public boolean isReadyForReview() {
        return status == ApplicationStatus.PAYMENT_VERIFIED;
    }

    public String getDisplayStatus() {
        switch (status) {
            case PENDING_PAYMENT:
                return "Awaiting Payment";
            case PAYMENT_VERIFIED:
                return "Payment Verified";
            case UNDER_REVIEW:
                return "Under Review";
            case APPROVED:
                return "Application Approved";
            case REJECTED:
                return "Application Rejected";
            case CANCELLED:
                return "Cancelled";
            default:
                return status != null ? status.name() : "Pending";
        }
    }

    public String getStatusColor() {
        switch (status) {
            case PENDING_PAYMENT:
                return "warning";
            case PAYMENT_VERIFIED:
                return "info";
            case UNDER_REVIEW:
                return "primary";
            case APPROVED:
                return "success";
            case REJECTED:
            case CANCELLED:
                return "danger";
            default:
                return "secondary";
        }
    }

    public String getStatusIcon() {
        switch (status) {
            case PENDING_PAYMENT:
                return "⏳";
            case PAYMENT_VERIFIED:
                return "✓";
            case UNDER_REVIEW:
                return "📋";
            case APPROVED:
                return "🎉";
            case REJECTED:
                return "✗";
            case CANCELLED:
                return "✗";
            default:
                return "📋";
        }
    }
}