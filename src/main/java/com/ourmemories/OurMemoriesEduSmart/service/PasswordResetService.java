package com.ourmemories.OurMemoriesEduSmart.service;

import com.ourmemories.OurMemoriesEduSmart.dto.*;
import com.ourmemories.OurMemoriesEduSmart.model.PasswordReset;
import com.ourmemories.OurMemoriesEduSmart.model.User;
import com.ourmemories.OurMemoriesEduSmart.repository.PasswordResetRepository;
import com.ourmemories.OurMemoriesEduSmart.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class PasswordResetService {

    private final PasswordResetRepository passwordResetRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmailService emailService;

    private static final int OTP_EXPIRY_MINUTES = 10;

    public PasswordResetService(PasswordResetRepository passwordResetRepository,
                                UserRepository userRepository,
                                BCryptPasswordEncoder bCryptPasswordEncoder, EmailService emailService) {
        this.passwordResetRepository = passwordResetRepository;
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.emailService = emailService;
    }

    // Step 1: Request OTP
    @Transactional
    public ResetPasswordResponse requestPasswordResetOTP(OTPRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email not found"));


        // This deletes the old record in the same transaction
//        From line 43 to 45 this is confusing if you have any solution,
//        please yenta nyalo coz if i remove one deletion the method stops working
        passwordResetRepository.findByUserEmail(user.getEmail())
                .ifPresent(passwordResetRepository::delete);
        passwordResetRepository.deleteByUser(user);

        String otp = generateSecureOTP();

        PasswordReset passwordReset = PasswordReset.builder()
                .otp(otp)
                .otpExpiryDate(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .user(user)
                .build();

        passwordResetRepository.save(passwordReset);

        String fullName = user.getFirstName() + " " + user.getLastName();
        // Build reset link (frontend page where user enters OTP and new password)
        String resetLink = "https://localhost:5173/reset-password";
        emailService.sendPasswordResetOtp(user.getEmail(),fullName,otp, resetLink);

        return ResetPasswordResponse.builder()
                .message("OTP sent successfully")
                .build();
    }

    // Step 2: Verify OTP
    @Transactional
    public ResetPasswordResponse verifyOTP(VerifyOTPRequest request) {
        PasswordReset passwordReset = passwordResetRepository.findByOtp(request.getOtp())
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        if (LocalDateTime.now().isAfter(passwordReset.getOtpExpiryDate())) {
            passwordResetRepository.delete(passwordReset);
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }

        return ResetPasswordResponse.builder()
                .message("OTP verified successfully")
                .build();
    }

    // Step 3: Reset Password
    @Transactional
    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
        PasswordReset passwordReset = passwordResetRepository.findByUserEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP"));

        if (LocalDateTime.now().isAfter(passwordReset.getOtpExpiryDate())) {
            passwordResetRepository.delete(passwordReset);
            throw new RuntimeException("OTP has expired");
        }

        User user = passwordReset.getUser();
        user.setPassword(bCryptPasswordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Clean up after successful reset
        passwordResetRepository.delete(passwordReset);

        return ResetPasswordResponse.builder()
                .message("Password reset successfully")
                .build();
    }

    // Secure 6-digit OTP
    private String generateSecureOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Ensures 6 digits
        return String.valueOf(otp);
    }
}