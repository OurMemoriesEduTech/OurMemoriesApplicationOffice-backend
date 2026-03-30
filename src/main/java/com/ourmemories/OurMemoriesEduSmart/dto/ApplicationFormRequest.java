package com.ourmemories.OurMemoriesEduSmart.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ApplicationFormRequest {
    // === COMMON FIELDS (used by all application types) ===
    private String applicationType; // "UNIVERSITY", "TVET", "NSFAS"

    private String educationStatus;

    // Personal Info
    private String title;
    private String initials;
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

    // Guardian
    private String guardianRelationship;
    private String guardianTitle;
    private String guardianInitials;
    private String guardianFullNames;
    private String guardianSurname;
    private String guardianCellNumber;
    private String guardianEmail;

    // === NSFAS-SPECIFIC FIELDS ===
    private String fundingType; // "Bursary" or "Loan"
    private String isDisabled; // "Yes"/"No"
    private String householdIncomeUnder350k; // "Yes"/"No"
    private String highestGrade;
    private String studyCycleYears;
    private String hasOfferLetter; // "Yes"/"No"
    private String isMarried;
    private String fatherAlive;
    private String fatherTitle;
    private String fatherFirstName;
    private String fatherLastName;
    private String fatherIdNumber;
    private String motherAlive;
    private String motherTitle;
    private String motherFirstName;
    private String motherLastName;
    private String motherIdNumber;
    private String hasGuardian;
    private String guardianFirstName;
    private String guardianLastName;
    private String guardianIdNumber;

    // === EDUCATION & INSTITUTIONS (University/TVET) ===
    private List<InstitutionDto> selectedInstitutions;
    private List<SubjectDto> subjects;

    // === PAYMENT FIELDS ===
    private Integer numberOfApplications; // Number of universities selected
    private BigDecimal applicationFee; // Calculated fee
    private String paymentMethod; // "EFT" or "ONLINE"
    private String paymentStatus; // "PENDING", "PAID", "AWAITING_VERIFICATION"
    private String paymentReference; // User's reference for EFT
    private String paymentIntentId; // For Stripe payments

    // === DOCUMENTS ===
    private Documents documents;

    private boolean declaration;

    // Inner classes
    @Data
    public static class InstitutionDto {
        private String institutionName;
        private String institutionType; // "University", "TVET College"
        private List<String> courses;
    }

    @Data
    public static class SubjectDto {
        private String name;
        private String level;
        private String percentage;
    }

    @Data
    public static class Documents {
        private MultipartFile applicantIdCopy;
        private MultipartFile grade11_12Results;
        private MultipartFile grade9_10_11_12Results;
        private MultipartFile parentIdCopy;
        private MultipartFile proofOfResidence;
        private MultipartFile proofOfPayment;

        // NSFAS-specific
        private MultipartFile fatherIdCopy;
        private MultipartFile motherIdCopy;
        private MultipartFile guardianIdCopy;
        private MultipartFile nsfasConsentForm;
        private MultipartFile nsfasDeclarationForm;
        private MultipartFile proofOfIncome;
        private MultipartFile offerLetter;
    }
}