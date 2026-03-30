package com.ourmemories.OurMemoriesEduSmart.controller;

import com.ourmemories.OurMemoriesEduSmart.dto.PaymentInitiateRequest;
import com.ourmemories.OurMemoriesEduSmart.dto.PaymentResponse;
import com.ourmemories.OurMemoriesEduSmart.dto.PaymentVerificationRequest;
import com.ourmemories.OurMemoriesEduSmart.model.Document;
import com.ourmemories.OurMemoriesEduSmart.model.Payment;
import com.ourmemories.OurMemoriesEduSmart.repository.DocumentRepository;
import com.ourmemories.OurMemoriesEduSmart.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class EFTController {

    private final PaymentService paymentService;
    private final DocumentRepository documentRepository;

    // ==================== PUBLIC ENDPOINTS ====================

    @GetMapping("/eft-instructions")
    public ResponseEntity<?> getEFTInstructions() {
        return ResponseEntity.ok(paymentService.getEFTInstructions());
    }

    @GetMapping("/fee-structure")
    public ResponseEntity<?> getFeeStructure() {
        return ResponseEntity.ok(paymentService.getFeeStructure());
    }

    // ==================== USER ENDPOINTS ====================

    @PostMapping("/initiate-eft")
    public ResponseEntity<?> initiateEFTPayment(
            @Valid @RequestBody PaymentInitiateRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {
        try {
            request.setIpAddress(httpRequest.getRemoteAddr());
            request.setUserAgent(httpRequest.getHeader("User-Agent"));

            PaymentResponse response = paymentService.initiateEFTPayment(request, userDetails.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error initiating EFT payment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }

    @PostMapping("/upload-proof/{paymentId}")
    public ResponseEntity<?> uploadProofOfPayment(
            @PathVariable Long paymentId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {
        try {
            Payment payment = paymentService.uploadProofOfPayment(
                    paymentId,
                    file,
                    userDetails.getUsername(),
                    httpRequest.getRemoteAddr(),
                    httpRequest.getHeader("User-Agent")
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "paymentId", payment.getId(),
                    "status", payment.getStatus(),
                    "message", "Proof of payment uploaded successfully. Our team will verify your payment within 24-48 hours.",
                    "estimatedVerificationTime", "24-48 hours"
            ));
        } catch (Exception e) {
            log.error("Error uploading proof: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }

    @GetMapping("/my-eft-payments")
    public ResponseEntity<?> getUserEFTPayments(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            List<Payment> payments = paymentService.getUserEFTPayments(userDetails.getUsername());
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            log.error("Error fetching EFT payments: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/eft/{paymentId}")
    public ResponseEntity<?> getEFTPaymentDetails(
            @PathVariable Long paymentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Payment payment = paymentService.getEFTPaymentDetails(paymentId, userDetails.getUsername());
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            log.error("Error fetching EFT payment details: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== ADMIN ENDPOINTS ====================

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/pending-eft")
    public ResponseEntity<?> getPendingEFTVerifications() {
        try {
            List<Payment> payments = paymentService.getPendingEFTVerifications();
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            log.error("Error fetching pending EFT verifications: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/verify-eft/{paymentId}")
    public ResponseEntity<?> verifyEFTPayment(
            @PathVariable Long paymentId,
            @Valid @RequestBody PaymentVerificationRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {
        try {
            Payment payment = paymentService.verifyEFTPayment(paymentId, request, userDetails.getUsername());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "paymentId", payment.getId(),
                    "status", payment.getStatus(),
                    "message", "Payment " + request.getStatus().toLowerCase() + " successfully",
                    "userNotified", true,
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            log.error("Error verifying EFT payment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/eft-stats")
    public ResponseEntity<?> getEFTStatistics() {
        try {
            Map<String, Object> stats = paymentService.getEFTStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching EFT statistics: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/expire-pending")
    public ResponseEntity<?> expirePendingPayments() {
        try {
            int expiredCount = paymentService.expirePendingPayments();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "expiredCount", expiredCount,
                    "message", "Successfully expired " + expiredCount + " pending payments"
            ));
        } catch (Exception e) {
            log.error("Error expiring payments: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/payment/{paymentId}")
    public ResponseEntity<?> getPaymentById(@PathVariable Long paymentId) {
        try {
            Payment payment = paymentService.getPaymentById(paymentId);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            log.error("Error fetching payment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // FIXED: View proof endpoint - checks BOTH Payment table AND Document table
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/view-proof/{paymentId}")
    public ResponseEntity<byte[]> viewProofOfPayment(@PathVariable Long paymentId) {
        log.info("View proof request for payment: {}", paymentId);

        try {
            Payment payment = paymentService.getPaymentById(paymentId);

            // FIRST: Check if proof is in file system (Payment table)
            if (payment.getProofOfPaymentPath() != null) {
                log.info("Found proof in file system at: {}", payment.getProofOfPaymentPath());
                Path filePath = Paths.get(payment.getProofOfPaymentPath());
                if (Files.exists(filePath)) {
                    byte[] fileBytes = Files.readAllBytes(filePath);
                    String filename = getFileName(payment.getProofOfPaymentPath());
                    String contentType = getContentType(payment.getProofOfPaymentPath());

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.parseMediaType(contentType));
                    headers.setContentDispositionFormData("inline", filename);

                    log.info("Serving proof from file system for payment: {}", paymentId);
                    return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
                }
            }

            // SECOND: Check if proof is in Document table (uploaded later from dashboard)
            log.info("Checking Document table for proof of payment...");

            // Find document by application ID and document type
            List<Document> documents = documentRepository.findByApplicationId(payment.getApplication().getId());
            Document proofDoc = documents.stream()
                    .filter(doc -> "Proof of Payment".equalsIgnoreCase(doc.getDocumentType()))
                    .findFirst()
                    .orElse(null);

            if (proofDoc != null && proofDoc.getDocument() != null) {
                log.info("Found proof in Document table for payment: {}", paymentId);
                byte[] fileBytes = proofDoc.getDocument();
                String contentType = proofDoc.getContentType();
                String filename = proofDoc.getFileName() != null ? proofDoc.getFileName() : "proof_of_payment.pdf";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(contentType));
                headers.setContentDispositionFormData("inline", filename);

                log.info("Serving proof from Document table for payment: {}", paymentId);
                return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
            }

            // No proof found anywhere
            log.warn("No proof of payment found for payment: {} (checked both file system and Document table)", paymentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

        } catch (IOException e) {
            log.error("IO Error viewing proof for payment {}: {}", paymentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (Exception e) {
            log.error("Error viewing proof for payment {}: {}", paymentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // FIXED: Download proof endpoint - checks BOTH Payment table AND Document table
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/download-proof/{paymentId}")
    public ResponseEntity<byte[]> downloadProofOfPayment(@PathVariable Long paymentId) {
        log.info("Download proof request for payment: {}", paymentId);

        try {
            Payment payment = paymentService.getPaymentById(paymentId);

            // FIRST: Check if proof is in file system (Payment table)
            if (payment.getProofOfPaymentPath() != null) {
                log.info("Found proof in file system at: {}", payment.getProofOfPaymentPath());
                Path filePath = Paths.get(payment.getProofOfPaymentPath());
                if (Files.exists(filePath)) {
                    byte[] fileBytes = Files.readAllBytes(filePath);
                    String filename = getFileName(payment.getProofOfPaymentPath());
                    String contentType = getContentType(payment.getProofOfPaymentPath());

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.parseMediaType(contentType));
                    headers.setContentDispositionFormData("attachment", filename);

                    log.info("Serving download from file system for payment: {}", paymentId);
                    return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
                }
            }

            // SECOND: Check if proof is in Document table (uploaded later from dashboard)
            log.info("Checking Document table for proof of payment...");

            List<Document> documents = documentRepository.findByApplicationId(payment.getApplication().getId());
            Document proofDoc = documents.stream()
                    .filter(doc -> "Proof of Payment".equalsIgnoreCase(doc.getDocumentType()))
                    .findFirst()
                    .orElse(null);

            if (proofDoc != null && proofDoc.getDocument() != null) {
                log.info("Found proof in Document table for payment: {}", paymentId);
                byte[] fileBytes = proofDoc.getDocument();
                String contentType = proofDoc.getContentType();
                String filename = proofDoc.getFileName() != null ? proofDoc.getFileName() : "proof_of_payment.pdf";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(contentType));
                headers.setContentDispositionFormData("attachment", filename);

                log.info("Serving download from Document table for payment: {}", paymentId);
                return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
            }

            log.warn("No proof of payment found for payment: {} (checked both file system and Document table)", paymentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

        } catch (IOException e) {
            log.error("IO Error downloading proof for payment {}: {}", paymentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (Exception e) {
            log.error("Error downloading proof for payment {}: {}", paymentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private String getFileName(String filePath) {
        if (filePath == null) return "proof.pdf";
        int lastSlash = Math.max(filePath.lastIndexOf("/"), filePath.lastIndexOf("\\"));
        if (lastSlash >= 0) {
            return filePath.substring(lastSlash + 1);
        }
        return filePath;
    }

    private String getContentType(String filePath) {
        if (filePath == null) return "application/pdf";
        String path = filePath.toLowerCase();
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (path.endsWith(".png")) {
            return "image/png";
        }
        return "application/pdf";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all-payments")
    public ResponseEntity<?> getAllEFTPayments() {
        try {
            List<Payment> payments = paymentService.getAllEFTPayments();
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            log.error("Error fetching all EFT payments: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}