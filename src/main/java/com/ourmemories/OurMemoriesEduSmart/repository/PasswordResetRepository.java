package com.ourmemories.OurMemoriesEduSmart.repository;

import com.ourmemories.OurMemoriesEduSmart.model.PasswordReset;
import com.ourmemories.OurMemoriesEduSmart.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {
    Optional<PasswordReset> findByUser(User user);
    Optional<PasswordReset> findByOtp(String otp);
    Optional<PasswordReset> findByOtpAndUserEmail(String otp, String email);
    Optional<PasswordReset> findByUserEmail(String email);
    void deleteByUser(User user);
}
