package com.ourmemories.OurMemoriesEduSmart.controller;

import com.ourmemories.OurMemoriesEduSmart.dto.*;
import com.ourmemories.OurMemoriesEduSmart.model.Application;
import com.ourmemories.OurMemoriesEduSmart.model.Document;
import com.ourmemories.OurMemoriesEduSmart.repository.DocumentRepository;
import com.ourmemories.OurMemoriesEduSmart.service.ApplicationDetailsService;
import com.ourmemories.OurMemoriesEduSmart.service.ApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class ApplicationController {

    private final Logger LOGGER = LoggerFactory.getLogger(ApplicationController.class);
    private final ApplicationService applicationService;
    private final ApplicationDetailsService applicationDetailsService;
    private final DocumentRepository documentRepository;

    public ApplicationController(ApplicationService applicationService, ApplicationDetailsService applicationDetailsService, DocumentRepository documentRepository) {
        this.applicationService = applicationService;
        this.applicationDetailsService = applicationDetailsService;
        this.documentRepository = documentRepository;
    }

    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> submitApplication(
            @Valid @RequestPart("data") ApplicationFormRequest request,
            BindingResult bindingResult,

            // === Common Files ===
            @RequestPart(value = "applicantIdCopy", required = false) MultipartFile applicantIdCopy,
            @RequestPart(value = "proofOfPayment", required = false) MultipartFile proofOfPayment,
            @RequestPart(value = "proofOfResidence", required = false) MultipartFile proofOfResidence,

            // === University/TVET Files ===
            @RequestPart(value = "grade11_12Results", required = false) MultipartFile grade11_12Results,
            @RequestPart(value = "grade9_10_11_12Results", required = false) MultipartFile grade9_10_11_12Results,
            @RequestPart(value = "parentIdCopy", required = false) MultipartFile parentIdCopy,

            // === NSFAS-Specific Files ===
            @RequestPart(value = "fatherIdCopy", required = false) MultipartFile fatherIdCopy,
            @RequestPart(value = "motherIdCopy", required = false) MultipartFile motherIdCopy,
            @RequestPart(value = "guardianIdCopy", required = false) MultipartFile guardianIdCopy,
            @RequestPart(value = "nsfasConsentForm", required = false) MultipartFile nsfasConsentForm,
            @RequestPart(value = "nsfasDeclarationForm", required = false) MultipartFile nsfasDeclarationForm,
            @RequestPart(value = "proofOfIncome", required = false) MultipartFile proofOfIncome,
            @RequestPart(value = "offerLetter", required = false) MultipartFile offerLetter
    ) {

        Map<String, Object> response = new HashMap<>();

        // Validation
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors().stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .collect(Collectors.joining("; "));
            response.put("fail", "Please fix the following: " + errors);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Map<String, Object> result = applicationService.submitApplication(
                    request,
                    proofOfPayment, proofOfResidence,
                    grade11_12Results, grade9_10_11_12Results, parentIdCopy,
                    applicantIdCopy, fatherIdCopy, motherIdCopy, guardianIdCopy,
                    nsfasConsentForm, nsfasDeclarationForm, proofOfIncome, offerLetter
            );

            response.put("success", true);
            response.put("applicationId", result.get("applicationId"));
            response.put("paymentId", result.get("paymentId"));
            response.put("paymentStatus", result.get("paymentStatus"));
            response.put("message", result.get("message"));

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("fail", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            LOGGER.error("Submission failed", e);
            response.put("fail", "Submission failed. Please try again.");
            return ResponseEntity.status(500).body(response);
        }
    }

    // ApplicationController.java - Change to use @RequestParam instead of @PathVariable
    @PostMapping("/applications/{appId}/upload")
    public ResponseEntity<Map<String, Object>> uploadDocument(
            @PathVariable Long appId,
            @RequestParam("docName") String docName,  // ← Now a query parameter
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> response = new HashMap<>();

        if (file.isEmpty()) {
            response.put("fail", "No file uploaded");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            LOGGER.info("Uploading document: {} for application: {}", docName, appId);
            applicationService.uploadDocument(appId, docName, file);
            response.put("success", true);
            response.put("message", docName + " uploaded successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LOGGER.error("Upload failed", e);
            response.put("fail", "Upload failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/applications")
    public ResponseEntity<List<ApplicationDTO>> getUserApplications() {
        try {
            List<ApplicationDTO> applications = applicationService.getUserApplications();
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/applications/{appId}")
    public ResponseEntity<ApplicationDetailsDTO> getApplication(@PathVariable Long appId) {
        try{
            ApplicationDetailsDTO details = applicationDetailsService.getApplicationDetails(appId);
            return ResponseEntity.ok(details);
        } catch (Exception e){
            return ResponseEntity.status(500).body(null);
        }
    }

    @PutMapping("/applications/{id}")
    public ResponseEntity<ApiResponse> updateApplication(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationUpdateRequest request) {
        try {
            LOGGER.warn(String.valueOf("Before updated, " + request.getApplicant().getIsSouthAfrican()));
            Application updated = applicationService.updateApplication(id, request);
            LOGGER.warn(String.valueOf("After updated, " + updated.getApplicant().isSouthAfrican()));
            return ResponseEntity.ok(new ApiResponse(true, "Application updated successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @PutMapping("/applications/{id}/cancel")
    public ResponseEntity<ApiResponse> cancelApplication(@PathVariable Long id) {
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            applicationService.cancelApplication(id, userEmail);
            return ResponseEntity.ok(new ApiResponse(true, "Application cancelled successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

//    @GetMapping("/documentN/{docId}/view")
//    public ResponseEntity<byte[]> viewDocument(@PathVariable Long docId) {
//        try {
//            Document document = documentRepository.findById(docId)
//                    .orElseThrow(() -> new RuntimeException("Document not found"));
//
//            // Verify ownership
//            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
//            if (!document.getApplication().getUser().getEmail().equals(userEmail)) {
//                throw new SecurityException("You don't have permission to view this document");
//            }
//
//            return ResponseEntity.ok()
//                    .contentType(MediaType.parseMediaType(document.getContentType()))
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.getFileName() + "\"")
//                    .body(document.getDocument());
//        } catch (Exception e)
//            LOGGER.error("Error viewing document: {}", e.getMessage(), e);
//            return ResponseEntity.status(500).body(null);
//        }
//    }s
}
