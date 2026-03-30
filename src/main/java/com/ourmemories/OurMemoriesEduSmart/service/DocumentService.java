package com.ourmemories.OurMemoriesEduSmart.service;

import com.ourmemories.OurMemoriesEduSmart.model.Application;
import com.ourmemories.OurMemoriesEduSmart.model.Document;
import com.ourmemories.OurMemoriesEduSmart.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public Document saveDocument(MultipartFile file, String documentType, Application application) throws IOException {
        Document doc = Document.builder()
                .documentType(documentType)
                .fileName(file.getOriginalFilename())
                .document(file.getBytes())
                .application(application)
                .build();
        return documentRepository.save(doc);
    }

    public Document getDocument(Long docId) {
        return documentRepository.findById(docId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
    }

    public List<Document> getDocumentsForApplication(Long appId) {
        return documentRepository.findByApplicationId(appId);
    }

    public Document replaceDocument(Long id, MultipartFile file) throws IOException {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        doc.setFileName(file.getOriginalFilename());
        doc.setDocument(file.getBytes());
        return documentRepository.save(doc);
    }

    public Document getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
    }

}
