package com.ourmemories.OurMemoriesEduSmart.service;

import com.ourmemories.OurMemoriesEduSmart.dto.ApplicationDetailsDTO;
import com.ourmemories.OurMemoriesEduSmart.model.*;
import com.ourmemories.OurMemoriesEduSmart.repository.ApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationDetailsService.class);

    private final ApplicationRepository applicationRepository;

    public ApplicationDetailsService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public ApplicationDetailsDTO getApplicationDetails(Long applicationId) {
        logger.info("Fetching application details for ID: {}", applicationId);

        Application application = applicationRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found with ID: " + applicationId));

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!application.getUser().getEmail().equals(currentUserEmail)) {
            logger.warn("User {} attempted to access application {} without permission", currentUserEmail, applicationId);
            throw new SecurityException("You do not have permission to view this application");
        }

        // Get payment info if exists
        Payment payment = application.getPayment();

        // Determine if user can edit
        boolean canEdit = application.canBeEdited();

        // Determine if user can cancel
        boolean canCancel = application.getStatus().isCancellable();

        return ApplicationDetailsDTO.builder()
                .id(application.getId())
                .type(application.getApplicationType())
                .status(application.getStatus().name())
                .displayStatus(application.getDisplayStatus())
                .statusColor(application.getStatusColor())
                .statusIcon(application.getStatusIcon())
                .canEdit(canEdit)
                .canCancel(canCancel)
                .submittedDate(application.getSubmittedDate())
                .lastUpdatedDate(application.getSubmittedDate())
                .educationStatus(application.getEducationStatus())
                .applicationFee(application.getApplicationFee())
                .paymentMethod(application.getPaymentMethod())
                .payment(mapPaymentToInfo(payment))
                .applicant(mapApplicantToInfo(application.getApplicant()))
                .guardian(mapGuardianToInfo(application.getApplicant().getGuardian()))
                .institutions(application.getInstitutions().stream()
                        .map(this::mapInstitutionToInfo)
                        .collect(Collectors.toList()))
                .documents(application.getDocuments().stream()
                        .map(this::mapDocumentToInfo)
                        .collect(Collectors.toList()))
                .subjects(application.getSubjects().stream()
                        .map(this::mapSubjectToInfo)
                        .collect(Collectors.toList()))
                .documentsNeeded(getDocumentsNeeded(application))
                .build();
    }

    // Helper mappers
    private ApplicationDetailsDTO.ApplicantInfo mapApplicantToInfo(Applicant applicant) {
        if (applicant == null) return null;
        return ApplicationDetailsDTO.ApplicantInfo.builder()
                .title(applicant.getTitle())
                .initials(applicant.getInitials())
                .firstName(applicant.getFullNames())
                .lastName(applicant.getLastName())
                .fullNames(applicant.getFullNames())
                .dateOfBirth(applicant.getDateOfBirth())
                .gender(applicant.getGender())
                .isSouthAfrican(applicant.isSouthAfrican())
                .idNumber(applicant.getIdNumber())
                .passportNumber(applicant.getPassportNumber())
                .countryOfResidence(applicant.getCountryOfResidence())
                .physicalAddress(applicant.getPhysicalAddress())
                .physicalCity(applicant.getPhysicalCity())
                .physicalProvince(applicant.getPhysicalProvince())
                .physicalPostalCode(applicant.getPhysicalPostalCode())
                .email(applicant.getEmail())
                .cellNumber(applicant.getCellNumber())
                .homeNumber(applicant.getHomeNumber())
                .isMarried(applicant.getIsMarried())
                .isDisabled(applicant.getIsDisabled())
                .householdIncomeUnder350k(applicant.getHouseholdIncomeUnder350k())
                .highestGrade(applicant.getHighestGrade())
                .studyCycleYears(applicant.getStudyCycleYears())
                .hasOfferLetter(applicant.getHasOfferLetter())
                .build();
    }

    private ApplicationDetailsDTO.GuardianInfo mapGuardianToInfo(Guardian guardian) {
        if (guardian == null) return null;
        return ApplicationDetailsDTO.GuardianInfo.builder()
                .relationship(guardian.getRelationship())
                .title(guardian.getTitle())
                .initials(guardian.getInitials())
                .fullNames(guardian.getFullNames())
                .surname(guardian.getSurname())
                .cellNumber(guardian.getCellNumber())
                .email(guardian.getEmail())
                .build();
    }

    private ApplicationDetailsDTO.InstitutionInfo mapInstitutionToInfo(Institution inst) {
        if (inst == null) return null;
        return ApplicationDetailsDTO.InstitutionInfo.builder()
                .id(inst.getId())
                .institutionName(inst.getInstitutionName())
                .institutionType(inst.getInstitutionType())
                .firstCourse(inst.getFirstCourse())
                .secondCourse(inst.getSecondCourse())
                .thirdCourse(inst.getThirdCourse())
                .build();
    }

    private ApplicationDetailsDTO.DocumentInfo mapDocumentToInfo(Document doc) {
        if (doc == null) return null;
        return ApplicationDetailsDTO.DocumentInfo.builder()
                .id(doc.getId())
                .documentType(doc.getDocumentType())
                .fileName(doc.getFileName())
                .fileType(doc.getContentType())
                .fileSize(doc.getDocument() != null ? (long) doc.getDocument().length : 0L)
                .uploadedAt(null) // Add if you have upload date
                .downloadUrl("/api/user/document/" + doc.getId() + "/view")
                .build();
    }

    private ApplicationDetailsDTO.SubjectInfo mapSubjectToInfo(Subject subject) {
        if (subject == null) return null;
        return ApplicationDetailsDTO.SubjectInfo.builder()
                .id(subject.getId())
                .name(subject.getName())
                .level(subject.getLevel())
                .percentage(subject.getPercentage())
                .build();
    }

    private ApplicationDetailsDTO.PaymentInfo mapPaymentToInfo(Payment payment) {
        if (payment == null) return null;
        return ApplicationDetailsDTO.PaymentInfo.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus().name())
                .displayStatus(payment.getDisplayStatus())
                .statusColor(payment.getStatusColor())
                .statusIcon(payment.getStatusIcon())
                .paymentReference(payment.getPaymentReference())
                .proofOfPaymentPath(payment.getProofOfPaymentPath())
                .proofUploadedAt(payment.getProofUploadedAt())
                .paymentDate(payment.getPaymentDate())
                .verificationDate(payment.getVerificationDate())
                .verifiedBy(payment.getVerifiedBy())
                .rejectionReason(payment.getRejectionReason())
                .expiresAt(payment.getExpiresAt())
                .build();
    }

    private List<String> getDocumentsNeeded(Application application) {
        List<String> needed = new java.util.ArrayList<>();

        if (application.getStatus() != ApplicationStatus.PENDING_PAYMENT) {
            return needed;
        }

        String type = application.getApplicationType();
        List<String> existingDocs = application.getDocuments().stream()
                .map(Document::getDocumentType)
                .collect(Collectors.toList());

        // Common required documents
        if (!existingDocs.contains("Applicant ID Copy")) {
            needed.add("Applicant ID Copy");
        }

        // Type-specific documents
        if ("UNIVERSITY".equalsIgnoreCase(type)) {
            if (!existingDocs.contains("Grade 11/12 Results")) {
                needed.add("Grade 11/12 Results");
            }
        } else if ("TVET".equalsIgnoreCase(type)) {
            if (!existingDocs.contains("Grade 9/10/11/12 Results")) {
                needed.add("Grade 9/10/11/12 Results");
            }
            if (!existingDocs.contains("Proof of Residence")) {
                needed.add("Proof of Residence");
            }
            if (!existingDocs.contains("Parent ID Copy")) {
                needed.add("Parent ID Copy");
            }
        } else if ("NSFAS".equalsIgnoreCase(type) || "BURSARY".equalsIgnoreCase(type)) {
            if (!existingDocs.contains("Proof of Income")) {
                needed.add("Proof of Income");
            }
        }

        // Add Proof of Payment if payment is pending
        Payment payment = application.getPayment();
        if (payment != null && payment.getStatus() == PaymentStatus.PENDING &&
                !existingDocs.contains("Proof of Payment")) {
            needed.add("Proof of Payment");
        }

        return needed;
    }
}