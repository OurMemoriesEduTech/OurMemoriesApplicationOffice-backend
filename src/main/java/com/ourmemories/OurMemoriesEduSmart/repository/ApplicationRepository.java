package com.ourmemories.OurMemoriesEduSmart.repository;

import com.ourmemories.OurMemoriesEduSmart.model.Application;
import com.ourmemories.OurMemoriesEduSmart.model.ApplicationStatus;
import com.ourmemories.OurMemoriesEduSmart.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application,Long> {
    List<Application> findByUserEmail(String email);

    // ApplicationRepository.java
    @Query("SELECT a FROM Application a " +
            "LEFT JOIN FETCH a.applicant ap " +
            "LEFT JOIN FETCH ap.guardian " +
            "WHERE a.id = :id")
    Optional<Application> findByIdWithDetails(@Param("id") Long id);
    @Query("SELECT a FROM Application a " +
            "JOIN FETCH a.applicant ap " +
            "LEFT JOIN FETCH ap.guardian " +
            "WHERE a.id = :id")
    Optional<Application> findByIdWithApplicantAndGuardian(@Param("id") Long id);

    @Query("SELECT a FROM Application a LEFT JOIN FETCH a.institutions WHERE a.id = :id")
    Optional<Application> findByIdWithInstitutions(@Param("id") Long id);

    // In ApplicationRepository.java
    @Query("SELECT a FROM Application a LEFT JOIN FETCH a.documents WHERE a.id = :id")
    Optional<Application> findByIdWithDocuments(@Param("id") Long id);

    int countApplicationByStatus(String status);

    List<Application> findByUserId(Long userId);

    @Query("SELECT a FROM Application a WHERE " +
            "(:startDate IS NULL OR a.submittedDate >= :startDate) AND " +
            "(:endDate IS NULL OR a.submittedDate <= :endDate) AND " +
            "(:type IS NULL OR :type = '' OR a.applicationType = :type) AND " +
            "(:status IS NULL OR :status = '' OR a.status = :status)")
    List<Application> findByFilters(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate,
                                    @Param("type") String type,
                                    @Param("status") String status);


    @Query("SELECT COUNT(a) FROM Application a WHERE a.status = :status")
    long countByStatus(@Param("status") ApplicationStatus status);

    @Query("SELECT a FROM Application a WHERE a.status = :status ORDER BY a.submittedDate DESC")
    List<Application> findByStatus(@Param("status") ApplicationStatus status);

    @Query("SELECT a FROM Application a WHERE a.status IN :statuses ORDER BY a.submittedDate DESC")
    List<Application> findByStatusIn(@Param("statuses") List<ApplicationStatus> statuses);

    @Query("SELECT a FROM Application a WHERE a.status = :status AND a.payment.status = :paymentStatus")
    List<Application> findByStatusAndPaymentStatus(@Param("status") ApplicationStatus status,
                                                   @Param("paymentStatus") PaymentStatus paymentStatus);

    @Query("SELECT a FROM Application a " +
            "LEFT JOIN FETCH a.applicant " +
            "LEFT JOIN FETCH a.institutions " +
            "LEFT JOIN FETCH a.payment")
    List<Application> findAllWithDetails();

    @Query("SELECT a FROM Application a " +
            "LEFT JOIN FETCH a.applicant " +
            "LEFT JOIN FETCH a.institutions " +
            "LEFT JOIN FETCH a.payment " +
            "WHERE a.id = :id")
    Optional<Application> findByIdWithAllDetails(@Param("id") Long id);
}
