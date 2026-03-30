package com.ourmemories.OurMemoriesEduSmart.controller;

import com.ourmemories.OurMemoriesEduSmart.dto.*;
import com.ourmemories.OurMemoriesEduSmart.service.PasswordResetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {
    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot-password/request-otp")
    public ResponseEntity<Map<String, Object>> getOpt(@RequestBody OTPRequest otpRequest) {
        try{
            Map<String, Object> response = new HashMap<>();
            response.put("success", passwordResetService.requestPasswordResetOTP(otpRequest));
            return ResponseEntity.ok(response);
        } catch (Exception e){
            Map<String, Object> response = new HashMap<>();
            response.put("fail", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(response);
        }
    }

    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<Map<String,Object>> verifyOTP(@RequestBody VerifyOTPRequest verifyOtpRequest) {
        try{
            Map<String, Object> response = new HashMap<>();
            response.put("success", passwordResetService.verifyOTP(verifyOtpRequest));
            return ResponseEntity.ok(response);
        }catch (Exception e){
            Map<String, Object> response = new HashMap<>();
            response.put("fail", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/forgot-password/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        try{
            Map<String, Object> response = new HashMap<>();
            response.put("success",  passwordResetService.resetPassword(resetPasswordRequest));
            return ResponseEntity.ok(response);
        } catch (Exception e){
            Map<String, Object> response = new HashMap<>();
            response.put("fail", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(response);
        }
    }
}
