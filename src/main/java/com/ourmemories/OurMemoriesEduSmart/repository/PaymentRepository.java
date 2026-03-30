package com.ourmemories.OurMemoriesEduSmart.repository;

import com.ourmemories.OurMemoriesEduSmart.model.Payment;
import com.ourmemories.OurMemoriesEduSmart.model.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // ==================== BASIC QUERIES ====================

    Optional<Payment> findByPaymentIntentId(String paymentIntentId);

    Optional<Payment> findByApplicationId(Long applicationId);

    // Use Enum instead of String
    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByPaymentMethod(String paymentMethod);

    // Use Enum for status parameter
    List<Payment> findByPaymentMethodAndStatus(String paymentMethod, PaymentStatus status);

    List<Payment> findByUserId(Long userId);

    Page<Payment> findByUserId(Long userId, Pageable pageable);

    // ==================== EFT SPECIFIC QUERIES ====================

    // Use Enum for status parameter
    @Query("SELECT p FROM Payment p WHERE p.paymentMethod = 'EFT' AND p.status = :status ORDER BY p.createdAt DESC")
    List<Payment> findEFTPaymentsByStatus(@Param("status") PaymentStatus status);

    // ==================== EXPIRATION QUERIES ====================

    // Use List<PaymentStatus> for statuses
    @Query("SELECT p FROM Payment p WHERE p.status IN :statuses AND p.expiresAt < :currentDate")
    List<Payment> findExpiredPaymentsByStatuses(@Param("statuses") List<PaymentStatus> statuses,
                                                @Param("currentDate") LocalDateTime currentDate);

    List<Payment> findByStatusAndExpiresAtBetween(PaymentStatus status, LocalDateTime start, LocalDateTime end);

    // ==================== COUNTING QUERIES ====================

    // Use Enum for status
    long countByPaymentMethodAndStatus(String paymentMethod, PaymentStatus status);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.application.id = :applicationId AND p.status IN :statuses")
    long countByApplicationIdAndStatusIn(@Param("applicationId") Long applicationId,
                                         @Param("statuses") List<PaymentStatus> statuses);

    boolean existsByApplicationIdAndStatus(Long applicationId, PaymentStatus status);

    boolean existsByPaymentIntentId(String paymentIntentId);

    // ==================== UPDATE QUERIES ====================

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.paymentMethod = 'EFT' AND p.status = :status AND p.verificationDate >= :startDate")
    long countVerifiedEFTPaymentsSince(@Param("status") PaymentStatus status, @Param("startDate") LocalDateTime startDate);

    /**
     * Get average verification time in hours for EFT payments
     */
    @Query(value = "SELECT AVG(TIMESTAMPDIFF(HOUR, p.proof_uploaded_at, p.verification_date)) " +
            "FROM payments p WHERE p.payment_method = 'EFT' AND p.status = :status " +
            "AND p.proof_uploaded_at IS NOT NULL AND p.verification_date IS NOT NULL",
            nativeQuery = true)
    Double getAverageVerificationTimeForStatus(@Param("status") String status);

    @Query(value = "SELECT AVG(TIMESTAMPDIFF(HOUR, p.proof_uploaded_at, p.verification_date)) " +
            "FROM payments p WHERE p.payment_method = 'EFT' AND p.status = :status " +
            "AND p.proof_uploaded_at IS NOT NULL AND p.verification_date IS NOT NULL " +
            "AND p.verification_date BETWEEN :startDate AND :endDate",
            nativeQuery = true)
    Double getAverageVerificationTimeForPeriod(@Param("status") String status,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    /**
     * Get payment method distribution
     */
    @Query("SELECT p.paymentMethod, COUNT(p) FROM Payment p GROUP BY p.paymentMethod")
    List<Object[]> getPaymentMethodDistribution();

    /**
     * Get daily payment statistics for successful/verified payments
     */
    @Query("SELECT FUNCTION('DATE', p.createdAt), COUNT(p), SUM(p.amount) " +
            "FROM Payment p WHERE p.status = :status " +
            "GROUP BY FUNCTION('DATE', p.createdAt) ORDER BY FUNCTION('DATE', p.createdAt) DESC")
    List<Object[]> getDailyPaymentStats(@Param("status") PaymentStatus status);

    /**
     * Get monthly payment statistics
     */
    @Query("SELECT FUNCTION('YEAR', p.createdAt), FUNCTION('MONTH', p.createdAt), " +
            "COUNT(p), SUM(p.amount) FROM Payment p " +
            "WHERE p.status = :status " +
            "GROUP BY FUNCTION('YEAR', p.createdAt), FUNCTION('MONTH', p.createdAt) " +
            "ORDER BY FUNCTION('YEAR', p.createdAt) DESC, FUNCTION('MONTH', p.createdAt) DESC")
    List<Object[]> getMonthlyPaymentStats(@Param("status") PaymentStatus status);

    /**
     * Get pending payments count by payment method
     */
    @Query("SELECT p.paymentMethod, COUNT(p) FROM Payment p WHERE p.status = :status GROUP BY p.paymentMethod")
    List<Object[]> getPendingPaymentsCount(@Param("status") PaymentStatus status);

    /**
     * Get total revenue by payment method
     */
    @Query("SELECT p.paymentMethod, SUM(p.amount) FROM Payment p " +
            "WHERE p.status = :status " +
            "GROUP BY p.paymentMethod")
    List<Object[]> getTotalRevenueByPaymentMethod(@Param("status") PaymentStatus status);

    /**
     * Get recent payments (last 30 days)
     */
    @Query("SELECT p FROM Payment p WHERE p.createdAt >= :since ORDER BY p.createdAt DESC")
    List<Payment> findRecentPayments(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * Find payments by status and date range
     */
    List<Payment> findByStatusAndCreatedAtBetween(PaymentStatus status, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find payments by user email (JPQL join)
     */
    @Query("SELECT p FROM Payment p WHERE p.user.email = :email ORDER BY p.createdAt DESC")
    List<Payment> findByUserEmail(@Param("email") String email);

    /**
     * Get total amount collected this year
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status " +
            "AND FUNCTION('YEAR', p.createdAt) = FUNCTION('YEAR', CURRENT_DATE)")
    Double getThisYearTotalRevenue(@Param("status") PaymentStatus status);

    /**
     * Get payment success rate
     */
    @Query("SELECT (COUNT(CASE WHEN p.status = :successStatus THEN 1 END) * 100.0 / COUNT(p)) " +
            "FROM Payment p WHERE p.createdAt >= :since")
    Double getSuccessRateSince(@Param("since") LocalDateTime since, @Param("successStatus") PaymentStatus successStatus);

    // Sum amount by payment method and status - using Enum
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.paymentMethod = :paymentMethod AND p.status = :status")
    Double sumAmountByPaymentMethodAndStatus(@Param("paymentMethod") String paymentMethod,
                                             @Param("status") PaymentStatus status);

    // Sum amount by payment method and status since a specific date
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.paymentMethod = :paymentMethod AND p.status = :status AND p.createdAt >= :since")
    Double sumAmountByPaymentMethodAndStatusSince(@Param("paymentMethod") String paymentMethod,
                                                  @Param("status") PaymentStatus status,
                                                  @Param("since") LocalDateTime since);

    // Sum amount for verified EFT payments between dates (for admin stats)
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.paymentMethod = 'EFT' AND p.status = :status AND p.verificationDate BETWEEN :startDate AND :endDate")
    Double sumVerifiedEFTAmountBetweenDates(@Param("status") PaymentStatus status,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    // Average verification time in hours
    @Query(value = "SELECT AVG(TIMESTAMPDIFF(HOUR, p.proof_uploaded_at, p.verification_date)) " +
            "FROM payments p WHERE p.payment_method = 'EFT' AND p.status = :status " +
            "AND p.proof_uploaded_at IS NOT NULL AND p.verification_date IS NOT NULL",
            nativeQuery = true)
    Double getAverageVerificationTime(@Param("status") String status);

    // Today's total revenue
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = :status AND DATE(p.createdAt) = CURRENT_DATE")
    Double getTodayTotalRevenue(@Param("status") PaymentStatus status);

    // This month's total revenue
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = :status AND " +
            "YEAR(p.createdAt) = YEAR(CURRENT_DATE) AND MONTH(p.createdAt) = MONTH(CURRENT_DATE)")
    Double getThisMonthTotalRevenue(@Param("status") PaymentStatus status);

    // Find payments by user ID and payment method
    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId AND p.paymentMethod = 'EFT' ORDER BY p.createdAt DESC")
    List<Payment> findEFTPaymentsByUserId(@Param("userId") Long userId);

    // Find payments by payment method and status ordered by creation date - using Enum
    List<Payment> findByPaymentMethodAndStatusOrderByCreatedAtDesc(String paymentMethod, PaymentStatus status);

    // Find expired payments - using Enum
    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.expiresAt < :currentDate")
    List<Payment> findExpiredPayments(@Param("status") PaymentStatus status, @Param("currentDate") LocalDateTime currentDate);

    List<Payment> findByPaymentMethodOrderByCreatedAtDesc(String eft);
}