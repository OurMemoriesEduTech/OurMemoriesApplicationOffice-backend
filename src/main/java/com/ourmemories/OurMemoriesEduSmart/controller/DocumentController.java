package com.ourmemories.OurMemoriesEduSmart.controller;

import com.ourmemories.OurMemoriesEduSmart.dto.ApiResponse;
import com.ourmemories.OurMemoriesEduSmart.model.Document;
import com.ourmemories.OurMemoriesEduSmart.service.DocumentService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/user/document")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<byte[]> viewDocument(@PathVariable Long id) {
        try {
            Document doc = documentService.getDocumentById(id);
            byte[] content = doc.getDocument(); // the byte array
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(doc.getContentType()));
            headers.setContentDispositionFormData("inline", doc.getFileName());
            return new ResponseEntity<>(content, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PutMapping("/{id}/replace")
    public ResponseEntity<ApiResponse> replaceDocument(@PathVariable Long id,
                                                       @RequestParam("file") MultipartFile file) {
        try {
            Document updated = documentService.replaceDocument(id, file);
            // Return minimal document info (id, documentType, fileName)
            Map<String, Object> responseData = Map.of(
                    "id", updated.getId(),
                    "documentType", updated.getDocumentType(),
                    "fileName", updated.getFileName()
            );
            return ResponseEntity.ok(new ApiResponse(true, "Document replaced successfully", responseData));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }
}
