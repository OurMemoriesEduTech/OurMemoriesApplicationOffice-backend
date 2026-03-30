package com.ourmemories.OurMemoriesEduSmart.service;

import com.ourmemories.OurMemoriesEduSmart.dto.*;
import com.ourmemories.OurMemoriesEduSmart.model.*;
import com.ourmemories.OurMemoriesEduSmart.repository.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    private final Logger LOGGER = LoggerFactory.getLogger(ApplicationService.class);

    private final EmailService emailService;
    private final PaymentRepository paymentRepository;
    private final ApplicantRepository applicantRepository;
    private final ApplicationRepository applicationRepository;
    private final InstitutionRepository institutionRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;

    public ApplicationService(EmailService emailService,
                              PaymentRepository paymentRepository,
                              ApplicantRepository applicantRepository,
                              ApplicationRepository applicationRepository,
                              InstitutionRepository institutionRepository,
                              DocumentRepository documentRepository,
                              UserRepository userRepository,
                              SubjectRepository subjectRepository) {
        this.emailService = emailService;
        this.paymentRepository = paymentRepository;
        this.applicantRepository = applicantRepository;
        this.applicationRepository = applicationRepository;
        this.institutionRepository = institutionRepository;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
    }

    @Transactional
    public Map<String, Object> submitApplication(ApplicationFormRequest request,
                                                 MultipartFile proofOfPayment,
                                                 MultipartFile proofOfResidence,
                                                 MultipartFile grade11_12Results,
                                                 MultipartFile grade9_10_11_12Results,
                                                 MultipartFile parentIdCopy,
                                                 MultipartFile applicantIdCopy,
                                                 MultipartFile fatherIdCopy,
                                                 MultipartFile motherIdCopy,
                                                 MultipartFile guardianIdCopy,
                                                 MultipartFile nsfasConsentForm,
                                                 MultipartFile nsfasDeclarationForm,
                                                 MultipartFile proofOfIncome,
                                                 MultipartFile offerLetter) {

        Map<String, Object> result = new java.util.HashMap<>();

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Save Guardian
        Guardian guardian = Guardian.builder()
                .relationship(request.getGuardianRelationship())
                .title(request.getGuardianTitle())
                .initials(request.getGuardianInitials())
                .fullNames(request.getGuardianFullNames())
                .surname(request.getGuardianSurname())
                .cellNumber(request.getGuardianCellNumber())
                .email(request.getGuardianEmail())
                .build();

        // Save Applicant
        Applicant applicant = Applicant.builder()
                .gender(request.getGender())
                .title(request.getTitle())
                .initials(request.getInitials())
                .lastName(request.getLastName())
                .fullNames(request.getFullNames())
                .dateOfBirth(request.getDateOfBirth())
                .isSouthAfrican(request.isSouthAfrican())
                .idNumber(request.getIdNumber())
                .passportNumber(request.getPassportNumber())
                .countryOfResidence(request.getCountryOfResidence())
                .physicalAddress(request.getPhysicalAddress())
                .physicalCity(request.getPhysicalCity())
                .physicalProvince(request.getPhysicalProvince())
                .physicalPostalCode(request.getPhysicalPostalCode())
                .email(request.getEmail())
                .cellNumber(request.getCellNumber())
                .homeNumber(request.getHomeNumber())
                .guardian(guardian)
                .isMarried(request.getIsMarried())
                .isDisabled(request.getIsDisabled())
                .householdIncomeUnder350k(request.getHouseholdIncomeUnder350k())
                .highestGrade(request.getHighestGrade())
                .studyCycleYears(request.getStudyCycleYears())
                .hasOfferLetter(request.getHasOfferLetter())
                .fatherAlive(request.getFatherAlive())
                .fatherTitle(request.getFatherTitle())
                .fatherFirstName(request.getFatherFirstName())
                .fatherLastName(request.getFatherLastName())
                .fatherIdNumber(request.getFatherIdNumber())
                .motherAlive(request.getMotherAlive())
                .motherTitle(request.getMotherTitle())
                .motherFirstName(request.getMotherFirstName())
                .motherLastName(request.getMotherLastName())
                .motherIdNumber(request.getMotherIdNumber())
                .hasGuardian(request.getHasGuardian())
                .guardianTitle(request.getGuardianTitle())
                .guardianFirstName(request.getGuardianFirstName())
                .guardianLastName(request.getGuardianLastName())
                .guardianIdNumber(request.getGuardianIdNumber())
                .guardianRelationship(request.getGuardianRelationship())
                .build();

        applicant = applicantRepository.save(applicant);

        // Build the application with new status
        Application application = Application.builder()
                .applicant(applicant)
                .submittedDate(LocalDateTime.now())
                .status(ApplicationStatus.PENDING_PAYMENT)
                .user(user)
                .educationStatus(request.getEducationStatus())
                .applicationType(request.getApplicationType())
                .numberOfApplications(request.getNumberOfApplications() != null ? request.getNumberOfApplications() : 0)
                .applicationFee(request.getApplicationFee() != null ? request.getApplicationFee() : BigDecimal.ZERO)
                .paymentMethod(request.getPaymentMethod())
                .build();

        // Prepare institutions list
        List<Institution> institutionList = new ArrayList<>();

        if ("NSFAS".equals(request.getApplicationType())) {
            Institution nsfas = Institution.builder()
                    .institutionName("NSFAS")
                    .institutionType("Bursary")
                    .application(application)
                    .build();
            institutionList.add(nsfas);
        } else {
            if (request.getSelectedInstitutions() != null) {
                institutionList = request.getSelectedInstitutions().stream()
                        .map(dto -> Institution.builder()
                                .institutionName(dto.getInstitutionName())
                                .institutionType(dto.getInstitutionType())
                                .firstCourse(dto.getCourses().size() > 0 ? dto.getCourses().get(0) : null)
                                .secondCourse(dto.getCourses().size() > 1 ? dto.getCourses().get(1) : null)
                                .thirdCourse(dto.getCourses().size() > 2 ? dto.getCourses().get(2) : null)
                                .application(application)
                                .build())
                        .collect(Collectors.toList());
            }
        }

        application.setInstitutions(institutionList);
        Application savedApplication = applicationRepository.save(application);

        // Save Subjects
        if (request.getSubjects() != null && !"NSFAS".equals(request.getApplicationType())) {
            List<Subject> subjects = request.getSubjects().stream()
                    .map(dto -> Subject.builder()
                            .name(dto.getName())
                            .level(dto.getLevel())
                            .percentage(dto.getPercentage())
                            .application(savedApplication)
                            .build())
                    .collect(Collectors.toList());
            subjectRepository.saveAll(subjects);
        }

        // Save Documents
        saveDocument(applicantIdCopy, "Applicant ID Copy", savedApplication);
        saveDocument(grade11_12Results, "Grade 11/12 Results", savedApplication);
        saveDocument(grade9_10_11_12Results, "Grade 9/10/11/12 Results", savedApplication);
        saveDocument(proofOfResidence, "Proof of Residence", savedApplication);
        saveDocument(parentIdCopy, "Parent ID Copy", savedApplication);
        saveDocument(fatherIdCopy, "Father ID Copy", savedApplication);
        saveDocument(motherIdCopy, "Mother ID Copy", savedApplication);
        saveDocument(guardianIdCopy, "Guardian ID Copy", savedApplication);
        saveDocument(nsfasConsentForm, "NSFAS Consent Form", savedApplication);
        saveDocument(nsfasDeclarationForm, "NSFAS Declaration Form", savedApplication);
        saveDocument(proofOfIncome, "Proof of Income", savedApplication);
        saveDocument(offerLetter, "Offer Letter", savedApplication);

        // Create payment record
        Payment payment = Payment.builder()
                .user(user)
                .application(savedApplication)
                .amount(savedApplication.getApplicationFee())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .paymentReference(request.getPaymentReference())
                .expiresAt(LocalDateTime.now().plusHours(48))
                .build();

        if ("EFT".equals(request.getPaymentMethod()) && proofOfPayment != null && !proofOfPayment.isEmpty()) {
            payment.setStatus(PaymentStatus.AWAITING_VERIFICATION);
            payment.setProofUploadedAt(LocalDateTime.now());
            saveDocument(proofOfPayment, "Proof of Payment", savedApplication);
        }

        if ("ONLINE".equals(request.getPaymentMethod())) {
            payment.setStatus(PaymentStatus.PENDING);
        }

        payment = paymentRepository.save(payment);
        savedApplication.setPayment(payment);
        applicationRepository.save(savedApplication);

        LOGGER.info("Payment record created for application {}: {} - Amount: {}",
                savedApplication.getId(), payment.getPaymentMethod(), payment.getAmount());

        result.put("paymentId", payment.getId());
        result.put("paymentStatus", payment.getStatus().name());

        // Send confirmation email
        String applicantName = savedApplication.getApplicant().getFullNames();
        String applicationType = savedApplication.getApplicationType();
        String reference = savedApplication.getId().toString();
        String submissionDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));

        List<Map<String, String>> institutions = savedApplication.getInstitutions().stream()
                .map(inst -> Map.of(
                        "name", inst.getInstitutionName(),
                        "course1", inst.getFirstCourse() != null ? inst.getFirstCourse() : "",
                        "course2", inst.getSecondCourse() != null ? inst.getSecondCourse() : "",
                        "course3", inst.getThirdCourse() != null ? inst.getThirdCourse() : ""
                ))
                .collect(Collectors.toList());

        String portalLink = "http://localhost:5173/application-portal";

        emailService.sendApplicationConfirmation(
                savedApplication.getApplicant().getEmail(),
                applicantName,
                reference,
                submissionDate,
                institutions,
                applicationType,
                portalLink
        );

        if ("EFT".equals(request.getPaymentMethod())) {
            emailService.sendEFTPaymentInstructionsEmail(
                    savedApplication.getApplicant().getEmail(),
                    savedApplication.getId(),
                    savedApplication.getApplicationFee().doubleValue(),
                    payment.getId()
            );
        }

        result.put("success", true);
        result.put("applicationId", savedApplication.getId());
        result.put("paymentId", payment.getId());
        result.put("paymentStatus", payment.getStatus().getDisplayName());
        result.put("message", "Application submitted successfully");

        return result;
    }

    private void saveDocument(MultipartFile file, String type, Application application) {
        if (file != null && !file.isEmpty()) {
            try {
                Document document = Document.builder()
                        .document(file.getBytes())
                        .documentType(type)
                        .application(application)
                        .fileName(file.getOriginalFilename())
                        .build();
                document = documentRepository.save(document);
                LOGGER.info("Saved document: {} for application: {}", type, application.getId());

                // CRITICAL: Update payment status if this is Proof of Payment
                if ("Proof of Payment".equalsIgnoreCase(type)) {
                    Payment payment = application.getPayment();
                    if (payment != null && payment.getStatus() == PaymentStatus.PENDING) {
                        payment.setStatus(PaymentStatus.AWAITING_VERIFICATION);
                        payment.setProofUploadedAt(LocalDateTime.now());
                        paymentRepository.save(payment);
                        LOGGER.info("Payment status updated to AWAITING_VERIFICATION for application: {}",
                                application.getId());
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Failed to save document: {}", type, e);
                throw new RuntimeException("Failed to save document: " + type, e);
            }
        }
    }

    public List<ApplicationDTO> getUserApplications() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Application> applications = applicationRepository.findByUserEmail(email);

        return applications.stream().map(app -> {
            List<Institution> institutions = institutionRepository.findByApplicationId(app.getId());
            List<Document> documents = documentRepository.findByApplicationId(app.getId());

            List<String> institutionNames = institutions.stream()
                    .map(Institution::getInstitutionName)
                    .collect(Collectors.toList());

            String type = institutions.isEmpty() ? "Unknown" : institutions.get(0).getInstitutionType();

            // Define required documents
            List<String> requiredDocuments = new ArrayList<>();
            if ("University".equalsIgnoreCase(type)) {
                requiredDocuments.add("Applicant ID Copy");
                requiredDocuments.add("Grade 11/12 Results");
            } else if ("TVET College".equalsIgnoreCase(type)) {
                requiredDocuments.add("Applicant ID Copy");
                requiredDocuments.add("Grade 9/10/11/12 Results");
                requiredDocuments.add("Proof of Residence");
                requiredDocuments.add("Parent ID Copy");
            } else if ("Bursary".equalsIgnoreCase(type) || "NSFAS".equalsIgnoreCase(type)) {
                requiredDocuments.add("Applicant ID Copy");
                requiredDocuments.add("Proof of Income");
            } else {
                requiredDocuments.add("Applicant ID Copy");
            }

            // Add Proof of Payment if payment is pending
            if (app.getStatus() == ApplicationStatus.PENDING_PAYMENT && app.getPayment() != null) {
                if (app.getPayment().getStatus() == PaymentStatus.PENDING ||
                        app.getPayment().getStatus() == PaymentStatus.AWAITING_VERIFICATION) {
                    requiredDocuments.add("Proof of Payment");
                }
            }

            // Find missing documents
            List<String> existingDocumentTypes = documents.stream()
                    .map(Document::getDocumentType)
                    .collect(Collectors.toList());

            List<String> documentsNeeded = requiredDocuments.stream()
                    .filter(docType -> !existingDocumentTypes.contains(docType))
                    .collect(Collectors.toList());

            if (app.getStatus() != ApplicationStatus.PENDING_PAYMENT) {
                documentsNeeded.clear();
            }

            String paymentStatus = app.getPayment() != null ? app.getPayment().getStatus().name() : null;

            return new ApplicationDTO(
                    app.getId(),
                    type,
                    institutionNames,
                    app.getStatus().name(),
                    app.getSubmittedDate(),
                    documentsNeeded,
                    app.getApplicationFee(),
                    app.getPaymentMethod(),
                    paymentStatus,
                    app.getDisplayStatus(),
                    app.getStatusColor()
            );
        }).collect(Collectors.toList());
    }

    // ApplicationService.java - The uploadDocument method
    @Transactional
    public void uploadDocument(Long appId, String docName, MultipartFile file) throws IOException {
        LOGGER.info("Uploading document: '{}' for application: {}", docName, appId);

        Application application = applicationRepository.findById(appId)
                .orElseThrow(() -> new RuntimeException("Application not found with id: " + appId));

        // Check if document already exists
        List<Document> existingDocs = documentRepository.findByApplicationId(appId);
        Optional<Document> existingDoc = existingDocs.stream()
                .filter(doc -> doc.getDocumentType().equalsIgnoreCase(docName))
                .findFirst();

        Document doc;
        if (existingDoc.isPresent()) {
            doc = existingDoc.get();
            doc.setDocument(file.getBytes());
            doc.setFileName(file.getOriginalFilename());
            LOGGER.info("Updated existing document: {}", docName);
        } else {
            doc = Document.builder()
                    .documentType(docName)
                    .document(file.getBytes())
                    .fileName(file.getOriginalFilename())
                    .application(application)
                    .build();
            LOGGER.info("Created new document: {}", docName);
        }

        documentRepository.save(doc);

        // Update payment status if this is Proof of Payment
        if ("Proof of Payment".equalsIgnoreCase(docName)) {
            Payment payment = application.getPayment();
            if (payment != null && payment.getStatus() == PaymentStatus.PENDING) {
                payment.setStatus(PaymentStatus.AWAITING_VERIFICATION);
                payment.setProofUploadedAt(LocalDateTime.now());
                paymentRepository.save(payment);
                LOGGER.info("Payment status updated to AWAITING_VERIFICATION for application: {}", appId);
            } else if (payment != null) {
                LOGGER.info("Payment status is {} - no update needed", payment.getStatus());
            }
        }
    }

    @Transactional
    public Application updateApplication(Long id, ApplicationUpdateRequest request) {
        Application existing = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found with id: " + id));

        // Check if application can be edited
        if (!existing.canBeEdited()) {
            throw new RuntimeException("Application cannot be edited in its current state: " + existing.getStatus().getDisplayName());
        }

        // Update simple fields
        existing.setEducationStatus(request.getEducationStatus());
        existing.setApplicationType(request.getApplicationType());

        // Update Applicant
        ApplicationUpdateRequest.ApplicantDto applicantDto = request.getApplicant();
        if (applicantDto != null) {
            Applicant applicant = existing.getApplicant();
            if (applicant == null) {
                throw new RuntimeException("Applicant not found for application");
            }

            applicant.setTitle(applicantDto.getTitle());
            applicant.setInitials(applicantDto.getInitials());
            applicant.setFullNames(applicantDto.getFullNames());
            applicant.setLastName(applicantDto.getLastName());
            applicant.setGender(applicantDto.getGender());
            if (applicantDto.getDateOfBirth() != null) {
                applicant.setDateOfBirth(LocalDate.parse(applicantDto.getDateOfBirth()));
            }
            applicant.setSouthAfrican(applicantDto.getIsSouthAfrican());
            applicant.setIdNumber(applicantDto.getIdNumber());
            applicant.setPassportNumber(applicantDto.getPassportNumber());
            applicant.setCountryOfResidence(applicantDto.getCountryOfResidence());
            applicant.setPhysicalAddress(applicantDto.getPhysicalAddress());
            applicant.setPhysicalCity(applicantDto.getPhysicalCity());
            applicant.setPhysicalProvince(applicantDto.getPhysicalProvince());
            applicant.setPhysicalPostalCode(applicantDto.getPhysicalPostalCode());
            applicant.setEmail(applicantDto.getEmail());
            applicant.setCellNumber(applicantDto.getCellNumber());
            applicant.setHomeNumber(applicantDto.getHomeNumber());

            // Update Guardian
            ApplicationUpdateRequest.GuardianDto guardianDto = request.getGuardian();
            if (guardianDto != null) {
                Guardian guardian = applicant.getGuardian();
                if (guardian == null) {
                    guardian = new Guardian();
                    applicant.setGuardian(guardian);
                }
                guardian.setRelationship(guardianDto.getRelationship());
                guardian.setTitle(guardianDto.getTitle());
                guardian.setInitials(guardianDto.getInitials());
                guardian.setFullNames(guardianDto.getFullNames());
                guardian.setSurname(guardianDto.getSurname());
                guardian.setCellNumber(guardianDto.getCellNumber());
                guardian.setEmail(guardianDto.getEmail());
            }

            applicant = applicantRepository.save(applicant);
        }

        // Update Subjects
        existing.getSubjects().clear();
        if (request.getSubjects() != null) {
            List<Subject> subjects = new ArrayList<>();
            for (ApplicationUpdateRequest.SubjectDto dto : request.getSubjects()) {
                Subject subject = Subject.builder()
                        .name(dto.getName())
                        .level(dto.getLevel())
                        .percentage(String.valueOf(dto.getPercentage()))
                        .application(existing)
                        .build();
                subjects.add(subject);
            }
            existing.getSubjects().addAll(subjects);
        }

        // Update Institutions
        existing.getInstitutions().clear();
        if (request.getInstitutions() != null) {
            List<Institution> institutions = new ArrayList<>();
            for (ApplicationUpdateRequest.InstitutionDto dto : request.getInstitutions()) {
                Institution institution = Institution.builder()
                        .institutionName(dto.getInstitutionName())
                        .institutionType(dto.getInstitutionType())
                        .firstCourse(dto.getFirstCourse())
                        .secondCourse(dto.getSecondCourse())
                        .thirdCourse(dto.getThirdCourse())
                        .application(existing)
                        .build();
                institutions.add(institution);
            }
            existing.getInstitutions().addAll(institutions);
        }

        Application savedApplication = applicationRepository.save(existing);

        emailService.sendApplicationUpdatedEmail(
                savedApplication.getApplicant().getEmail(),
                savedApplication.getApplicant().getFullNames(),
                savedApplication.getId().toString(),
                "https://yourapp.com/application/" + savedApplication.getId()
        );

        return savedApplication;
    }

    @Transactional
    public void cancelApplication(Long id, String userEmail) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found with id: " + id));

        // Verify ownership
        if (!application.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("You are not authorized to cancel this application");
        }

        // Check if application can be cancelled
        if (!application.getStatus().isCancellable()) {
            throw new RuntimeException("Application cannot be cancelled in its current state: " + application.getStatus().getDisplayName());
        }

        application.setStatus(ApplicationStatus.CANCELLED);
        applicationRepository.save(application);

        // Send cancellation confirmation email
        String fullName = application.getApplicant().getFullNames();
        String reference = application.getId().toString();
        String dashboardLink = "https://yourapp.com/application-portal";

        emailService.sendApplicationCancelledEmail(
                application.getApplicant().getEmail(),
                fullName,
                reference,
                dashboardLink
        );
    }
}