package com.ourmemories.OurMemoriesEduSmart.repository;

import com.ourmemories.OurMemoriesEduSmart.model.Application;
import com.ourmemories.OurMemoriesEduSmart.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    // Simple and efficient: find all documents for a given application
    List<Document> findByApplicationId(Long applicationId);

    // Optional: fetch a single document with its application (for extra safety)
    @Query("SELECT d FROM Document d JOIN FETCH d.application WHERE d.id = :id")
    Optional<Document> findByIdWithApplication(@Param("id") Long id);

    @Query("SELECT a FROM Application a LEFT JOIN FETCH a.documents WHERE a.id = :id")
    Optional<Application> findByIdWithDocuments(@Param("id") Long id);

    @Query("SELECT a FROM Application a LEFT JOIN FETCH a.institutions LEFT JOIN FETCH a.documents LEFT JOIN FETCH a.subjects WHERE a.id = :id")
    Optional<Application> findByIdWithDetails(@Param("id") Long id);
}