package com.ourmemories.OurMemoriesEduSmart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDetailsDTO {

    private Long id;
    private String type;
    private String status;
    private String displayStatus;
    private String statusColor;
    private String statusIcon;
    private LocalDateTime submittedDate;
    private LocalDateTime lastUpdatedDate;
    private String educationStatus;
    private boolean canEdit;
    private boolean canCancel;

    // Payment fields
    private BigDecimal applicationFee;
    private String paymentMethod;
    private PaymentInfo payment;

    // Applicant Information
    private ApplicantInfo applicant;

    // Guardian / Parent Information
    private GuardianInfo guardian;

    // Institutions & Courses
    private List<InstitutionInfo> institutions;

    // Supporting Documents
    private List<DocumentInfo> documents;

    // Progress / Missing Documents
    private List<String> documentsNeeded;

    // Applicant's subjects
    private List<SubjectInfo> subjects;

    @Data
    @Builder
    public static class PaymentInfo {
        private Long id;
        private BigDecimal amount;
        private String paymentMethod;
        private String status;
        private String displayStatus;
        private String statusColor;
        private String statusIcon;
        private String paymentReference;
        private String proofOfPaymentPath;
        private LocalDateTime proofUploadedAt;
        private LocalDateTime paymentDate;
        private LocalDateTime verificationDate;
        private String verifiedBy;
        private String rejectionReason;
        private LocalDateTime expiresAt;
    }

    @Data
    @Builder
    public static class ApplicantInfo {
        private String title;
        private String initials;
        private String firstName;
        private String lastName;
        private String fullNames;
        private LocalDate dateOfBirth;
        private String gender;
        private boolean isSouthAfrican;
        private String idNumber;
        private String passportNumber;
        private String countryOfResidence;
        private String physicalAddress;
        private String physicalCity;
        private String physicalProvince;
        private String physicalPostalCode;
        private String email;
        private String cellNumber;
        private String homeNumber;
        private String isMarried;
        private String isDisabled;
        private String householdIncomeUnder350k;
        private String highestGrade;
        private String studyCycleYears;
        private String hasOfferLetter;
    }

    @Data
    @Builder
    public static class GuardianInfo {
        private String relationship;
        private String title;
        private String initials;
        private String fullNames;
        private String surname;
        private String cellNumber;
        private String email;
    }

    @Data
    @Builder
    public static class InstitutionInfo {
        private Long id;
        private String institutionName;
        private String institutionType;
        private String firstCourse;
        private String secondCourse;
        private String thirdCourse;
    }

    @Data
    @Builder
    public static class DocumentInfo {
        private Long id;
        private String documentType;
        private String fileName;
        private String fileType;
        private Long fileSize;
        private LocalDateTime uploadedAt;
        private String downloadUrl;
    }

    @Data
    @Builder
    public static class SubjectInfo {
        private Long id;
        private String name;
        private String level;
        private String percentage;
    }
}