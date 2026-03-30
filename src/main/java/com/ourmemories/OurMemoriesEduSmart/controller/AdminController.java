package com.ourmemories.OurMemoriesEduSmart.controller;

import com.ourmemories.OurMemoriesEduSmart.dto.ApplicationSummaryDTO;
import com.ourmemories.OurMemoriesEduSmart.dto.ApplicationSummaryWithInstitutionsDTO;
import com.ourmemories.OurMemoriesEduSmart.model.*;
import com.ourmemories.OurMemoriesEduSmart.repository.AnnouncementRepository;
import com.ourmemories.OurMemoriesEduSmart.repository.ApplicationRepository;
import com.ourmemories.OurMemoriesEduSmart.repository.DocumentRepository;
import com.ourmemories.OurMemoriesEduSmart.repository.UserRepository;
import com.ourmemories.OurMemoriesEduSmart.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ApplicationRepository applicationRepository;
    private final DocumentRepository documentRepository;
    private final AnnouncementRepository announcementRepository;
    private final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);
    private final EmailService emailService;
    private final UserRepository userRepository;

    @GetMapping("/getAllApplications")
    public ResponseEntity<Map<String, Object>> getAllApplications() {
        Map<String, Object> response = new HashMap<>();
        try {
            // Use the new method that fetches institutions
            List<Application> applications = applicationRepository.findAllWithDetails();

            List<ApplicationSummaryWithInstitutionsDTO> appDTOs = applications.stream()
                    .map(ApplicationSummaryWithInstitutionsDTO::fromEntity)
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("applications", appDTOs);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    // 2. Get ONLY applicant details (currently used in View page)
    @GetMapping("/application/{id}/applicant")
    public ResponseEntity<Map<String, Object>> getApplicantDetails(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Application app = applicationRepository.findByIdWithDetails(id).orElse(null);
            if (app == null || app.getApplicant() == null) {
                response.put("success", false);
                response.put("message", "Application or applicant not found");
                return ResponseEntity.ok(response);
            }

            Applicant a = app.getApplicant();

            Map<String, Object> applicant = new HashMap<>();
            applicant.put("title", nvl(a.getTitle()));
            applicant.put("initials", nvl(a.getInitials()));
            applicant.put("fullNames", nvl(a.getFullNames()));
            applicant.put("lastName", nvl(a.getLastName()));
            applicant.put("gender", nvl(a.getGender()));
            applicant.put("dateOfBirth", a.getDateOfBirth() != null ? a.getDateOfBirth().toString() : "");
            applicant.put("idNumber", nvl(a.getIdNumber()));
            applicant.put("passportNumber", nvl(a.getPassportNumber()));
            applicant.put("isSouthAfrican", a.isSouthAfrican());
            applicant.put("countryOfResidence", nvl(a.getCountryOfResidence()));
            applicant.put("email", nvl(a.getEmail()));
            applicant.put("cellNumber", nvl(a.getCellNumber()));
            applicant.put("homeNumber", nvl(a.getHomeNumber()));
            applicant.put("physicalAddress", nvl(a.getPhysicalAddress()));
            applicant.put("physicalCity", nvl(a.getPhysicalCity()));
            applicant.put("physicalProvince", nvl(a.getPhysicalProvince()));
            applicant.put("physicalPostalCode", nvl(a.getPhysicalPostalCode()));

            response.put("success", true);
            response.put("applicant", applicant);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @DeleteMapping("/{applicationId}/deleteApplication")
    public ResponseEntity<Map<String, Object>> deleteApplication(@PathVariable Long applicationId) {
        Map<String, Object> response = new HashMap<>();
        if (!applicationRepository.existsById(applicationId)) {
            response.put("success", false);
            response.put("message", "Not found");
            return ResponseEntity.ok(response);
        }
        applicationRepository.deleteById(applicationId);
        response.put("success", true);
        response.put("message", "Deleted");
        return ResponseEntity.ok(response);
    }

    // GET Guardian (single) for an application
    @GetMapping("/application/{id}/guardian")
    public ResponseEntity<Map<String, Object>> getGuardian(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Application app = applicationRepository
                    .findByIdWithApplicantAndGuardian(id)
                    .orElse(null);

            if (app == null || app.getApplicant() == null || app.getApplicant().getGuardian() == null) {
                response.put("success", false);
                response.put("message", "Guardian not found");
                return ResponseEntity.ok(response);
            }

            Guardian g = app.getApplicant().getGuardian();

            Map<String, Object> guardian = Map.of(
                    "id", g.getId(),
                    "relationship", nvl(g.getRelationship()),
                    "title", nvl(g.getTitle()),
                    "initials", nvl(g.getInitials()),
                    "fullNames", nvl(g.getFullNames()),
                    "surname", nvl(g.getSurname()),
                    "cellNumber", nvl(g.getCellNumber()),
                    "email", nvl(g.getEmail())
            );

            response.put("success", true);
            response.put("guardian", guardian);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // GET Institutions (all chosen institutions + course choices) for an application
    @GetMapping("/application/{id}/institutions")
    public ResponseEntity<Map<String, Object>> getInstitutions(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Fetch the application with its institutions eagerly
            Application app = applicationRepository
                    .findByIdWithInstitutions(id)   // ← you need this query method (see below)
                    .orElse(null);

            if (app == null || app.getInstitutions() == null || app.getInstitutions().isEmpty()) {
                response.put("success", false);
                response.put("message", "No institutions found for this application");
                return ResponseEntity.ok(response);
            }

            List<Map<String, Object>> institutionList = app.getInstitutions().stream()
                    .map(inst -> Map.<String, Object>of(
                            "id", inst.getId(),
                            "institutionName", nvl(inst.getInstitutionName()),
                            "institutionType", nvl(inst.getInstitutionType()),
                            "firstCourse",   nvl(inst.getFirstCourse()),
                            "secondCourse",  nvl(inst.getSecondCourse()),
                            "thirdCourse",   nvl(inst.getThirdCourse())
                    ))
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("institutions", institutionList);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // GET all documents for a specific application
    @GetMapping("/application/{id}/documents")
    public ResponseEntity<Map<String, Object>> getDocuments(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Application app = applicationRepository.findByIdWithDocuments(id).orElse(null);

            if (app == null || app.getDocuments() == null || app.getDocuments().isEmpty()) {
                response.put("success", false);
                response.put("message", "No documents found for this application");
                return ResponseEntity.ok(response);
            }

            List<Map<String, Object>> docs = app.getDocuments().stream()
                    .map(doc -> Map.<String, Object>of(
                            "id", doc.getId(),
                            "documentType", nvl(doc.getDocumentType()),
                            "fileName", generateFileName(doc.getDocumentType()) // optional: make it nice
                    ))
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("documents", docs);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/document/{docId}/view")
    public ResponseEntity<byte[]> viewDocument(@PathVariable Long docId) {
        try {
            Document doc = documentRepository.findById(docId)
                    .orElseThrow(() -> new RuntimeException("Document not found"));
            byte[] content = doc.getDocument(); // the byte array
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(doc.getContentType()));
            headers.setContentDispositionFormData("inline", doc.getFileName());
            return new ResponseEntity<>(content, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // Download single document by ID
    @GetMapping("/document/{docId}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long docId) {
        try {
            Document doc = documentRepository.findById(docId)
                    .orElseThrow(() -> new RuntimeException("Document not found"));

            String fileName = generateFileName(doc.getDocumentType());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(doc.getDocument());

        } catch (Exception e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    // Helper: generate nice file names
    private String generateFileName(String type) {
        return switch (type != null ? type.toLowerCase() : "") {
            case "id document", "id copy", "id" -> "ID_Document.pdf";
            case "passport" -> "Passport.pdf";
            case "matric certificate", "grade 12 certificate" -> "Matric_Certificate.pdf";
            case "transcript", "academic record" -> "Academic_Transcript.pdf";
            case "cv", "resume" -> "CV_Resume.pdf";
            case "motivation letter" -> "Motivation_Letter.pdf";
            default -> type != null ? type.replaceAll("\\s+", "_") + ".pdf" : "document.pdf";
        };
    }

    // UPDATE Guardian saving guardian
    @PutMapping("/application/{applicationId}/guardian")
    public ResponseEntity<Map<String, Object>> updateGuardian(
            @PathVariable Long applicationId,
            @RequestBody Map<String, String> body) {

        Map<String, Object> response = new HashMap<>();
        try {
            Application app = applicationRepository
                    .findByIdWithApplicantAndGuardian(applicationId)
                    .orElse(null);

            if (app == null || app.getApplicant() == null || app.getApplicant().getGuardian() == null) {
                response.put("success", false);
                response.put("message", "Guardian not found");
                return ResponseEntity.notFound().build();
            }

            Guardian g = app.getApplicant().getGuardian();

            g.setRelationship(body.get("relationship"));
            g.setTitle(body.get("title"));
            g.setInitials(body.get("initials"));
            g.setFullNames(body.get("fullNames"));
            g.setSurname(body.get("surname"));
            g.setCellNumber(body.get("cellNumber"));
            g.setEmail(body.get("email"));

            applicationRepository.save(app); // cascades → saves guardian

            response.put("success", true);
            response.put("message", "Guardian updated successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // === ANNOUNCEMENTS ADMIN ENDPOINTS ===

    // GET all announcements (for admin management)
    @GetMapping("/announcements")
    public ResponseEntity<Map<String, Object>> getAllAnnouncements() {
        Map<String, Object> response = new HashMap<>();
        try {
            var announcements = announcementRepository.findAll()
                    .stream()
                    .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                    .map(this::mapAnnouncementToMap)
                    .toList();

            response.put("success", true);
            response.put("announcements", announcements);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // CREATE new announcement
    @PostMapping("/announcements")
    public ResponseEntity<Map<String, Object>> createAnnouncement(@RequestBody Announcement announcement) {
        Map<String, Object> response = new HashMap<>();
        try {
            Announcement saved = announcementRepository.save(announcement);
            response.put("success", true);
            response.put("message", "Announcement created");
            response.put("announcement", mapAnnouncementToMap(saved));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // UPDATE announcement
    @PutMapping("/announcements/{id}")
    public ResponseEntity<Map<String, Object>> updateAnnouncement(
            @PathVariable Long id,
            @RequestBody Announcement updated) {
        Map<String, Object> response = new HashMap<>();
        try {
            Announcement existing = announcementRepository.findById(id)
                    .orElse(null);
            if (existing == null) {
                response.put("success", false);
                response.put("message", "Announcement not found");
                return ResponseEntity.status(404).body(response);
            }

            existing.setTitle(updated.getTitle());
            existing.setMessage(updated.getMessage());
            existing.setType(updated.getType());
            existing.setImportant(updated.isImportant());
            existing.setDate(updated.getDate());

            Announcement saved = announcementRepository.save(existing);
            response.put("success", true);
            response.put("message", "Announcement updated");
            response.put("announcement", mapAnnouncementToMap(saved));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // DELETE announcement
    @DeleteMapping("/announcements/{id}")
    public ResponseEntity<Map<String, Object>> deleteAnnouncement(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!announcementRepository.existsById(id)) {
                response.put("success", false);
                response.put("message", "Not found");
                return ResponseEntity.status(404).body(response);
            }
            announcementRepository.deleteById(id);
            response.put("success", true);
            response.put("message", "Announcement deleted");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ADD THIS METHOD inside your controller class (e.g. AdminController or PublicController)
    private Map<String, Object> toMap(Announcement a) {
        return Map.of(
                "id", a.getId(),
                "title", a.getTitle() != null ? a.getTitle() : "",
                "message", a.getMessage() != null ? a.getMessage() : "",
                "date", a.getDate().toString(),
                "type", a.getType() != null ? a.getType() : "info",
                "important", a.isImportant()
        );
    }

    // Helper: Convert Announcement → Map (for JSON response)
    private Map<String, Object> mapAnnouncementToMap(Announcement a) {
        return Map.of(
                "id", a.getId(),
                "title", nvl(a.getTitle()),
                "message", nvl(a.getMessage()),
                "date", a.getDate().toString(),
                "type", nvl(a.getType()),
                "important", a.isImportant()
        );
    }

    // HELPER: Table summary
    private Map<String, Object> mapToSummary(Application a) {
        Applicant ap = a.getApplicant();
        String name = ap != null ? ap.getFullNames() + " " + ap.getLastName() : "Unknown";
        String type = a.getInstitutions().stream()
                .findFirst()
                .map(Institution::getInstitutionType)
                .orElse("University");

        return Map.of(
                "id", a.getId(),
                "learner", name,
                "email", ap != null ? ap.getEmail() : "",
                "type", type,
                "status", a.getStatus() != null ? a.getStatus() : "Pending",
                "dateSubmitted", a.getSubmittedDate()
        );
    }

    // Helper: null → empty string
    private String nvl(String s) {
        return s == null ? "" : s;
    }

    @GetMapping("/reports")
    public ResponseEntity<Map<String, Object>> getReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {

        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : null;

        if (type != null && type.trim().isEmpty()) type = null;
        if (status != null && status.trim().isEmpty()) status = null;

        // Fetch applications with all details
        List<Application> applications = applicationRepository.findByFilters(start, end, type, status);

        // Convert to DTOs with institutions
        List<ApplicationSummaryWithInstitutionsDTO> appDTOs = applications.stream()
                .map(ApplicationSummaryWithInstitutionsDTO::fromEntity)
                .collect(Collectors.toList());

        // Aggregate summary
        long total = appDTOs.size();
        long approved = appDTOs.stream().filter(a -> "APPROVED".equals(a.getStatus())).count();
        long pending = appDTOs.stream().filter(a ->
                "PENDING_PAYMENT".equals(a.getStatus()) ||
                        "PAYMENT_VERIFIED".equals(a.getStatus()) ||
                        "UNDER_REVIEW".equals(a.getStatus())
        ).count();
        long rejected = appDTOs.stream().filter(a -> "REJECTED".equals(a.getStatus())).count();

        Map<String, Long> byType = appDTOs.stream()
                .collect(Collectors.groupingBy(ApplicationSummaryWithInstitutionsDTO::getType, Collectors.counting()));

        // Timeline
        Map<LocalDate, Long> timelineMap = appDTOs.stream()
                .filter(a -> a.getDateSubmitted() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getDateSubmitted().toLocalDate(),
                        Collectors.counting()
                ));

        List<Map<String, Object>> timeline = timelineMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("date", e.getKey().toString());
                    entry.put("count", e.getValue());
                    return entry;
                })
                .collect(Collectors.toList());

        Map<String, Object> summary = new HashMap<>();
        summary.put("total", total);
        summary.put("approved", approved);
        summary.put("pending", pending);
        summary.put("rejected", rejected);
        summary.put("byType", byType);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("applications", appDTOs);
        response.put("summary", summary);
        response.put("timeline", timeline);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Count applications by status
            long totalApplications = applicationRepository.count();
            long pending = applicationRepository.countByStatus(ApplicationStatus.PENDING_PAYMENT);
            long paymentVerified = applicationRepository.countByStatus(ApplicationStatus.PAYMENT_VERIFIED);
            long underReview = applicationRepository.countByStatus(ApplicationStatus.UNDER_REVIEW);
            long approved = applicationRepository.countByStatus(ApplicationStatus.APPROVED);
            long rejected = applicationRepository.countByStatus(ApplicationStatus.REJECTED);
            long cancelled = applicationRepository.countByStatus(ApplicationStatus.CANCELLED);

            // Count users
            long totalUsers = userRepository.count();
            long admins = userRepository.countByRole(Role.ADMIN);
            long applicants = userRepository.countByRole(Role.USER);

            // Count courses (you may need to implement this based on your data)
            long totalCourses = 0;
            long universityCourses = 0;
            long tvetCourses = 0;

            // Count reports (you may need to implement this)
            long totalReports = 0;
            long reportsThisMonth = 0;

            response.put("success", true);
            response.put("totalApplications", totalApplications);
            response.put("pending", pending + paymentVerified); // Combined pending for display
            response.put("approved", approved);
            response.put("rejected", rejected);
            response.put("underReview", underReview);
            response.put("cancelled", cancelled);
            response.put("totalUsers", totalUsers);
            response.put("admins", admins);
            response.put("applicants", applicants);
            response.put("totalCourses", totalCourses);
            response.put("universityCourses", universityCourses);
            response.put("tvetCourses", tvetCourses);
            response.put("totalReports", totalReports);
            response.put("reportsThisMonth", reportsThisMonth);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("fail", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/application/{applicationId}/status")
    public ResponseEntity<Map<String, Object>> updateApplicationStatus(
            @PathVariable Long applicationId,
            @RequestBody Map<String, String> body) {

        Map<String, Object> response = new HashMap<>();
        String newStatus = body.get("status");

        try {
            ApplicationStatus status = ApplicationStatus.valueOf(newStatus);
            Application app = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new RuntimeException("Application not found"));

            ApplicationStatus currentStatus = app.getStatus();

            // Validate allowed transitions
            boolean isValidTransition = false;
            switch (currentStatus) {
                case PAYMENT_VERIFIED:
                    isValidTransition = status == ApplicationStatus.UNDER_REVIEW;
                    break;
                case UNDER_REVIEW:
                    isValidTransition = status == ApplicationStatus.APPROVED ||
                            status == ApplicationStatus.REJECTED;
                    break;
                default:
                    isValidTransition = false;
            }

            if (!isValidTransition) {
                response.put("success", false);
                response.put("message", "Invalid status transition from " + currentStatus + " to " + status);
                return ResponseEntity.badRequest().body(response);
            }

            app.setStatus(status);
            applicationRepository.save(app);

            // Send email notification for status change
            emailService.sendApplicationStatusUpdateEmail(
                    app.getApplicant().getEmail(),
                    applicationId,
                    currentStatus.getDisplayName(),
                    status.getDisplayName()
            );

            response.put("success", true);
            response.put("message", "Application status updated to " + status.getDisplayName());
            response.put("newStatus", status.name());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "Invalid status: " + newStatus);
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
