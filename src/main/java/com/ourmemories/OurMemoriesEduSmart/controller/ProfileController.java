package com.ourmemories.OurMemoriesEduSmart.controller;

import com.ourmemories.OurMemoriesEduSmart.dto.ApiResponse;
import com.ourmemories.OurMemoriesEduSmart.dto.ChangePasswordRequest;
import com.ourmemories.OurMemoriesEduSmart.dto.UpdateProfileRequest;
import com.ourmemories.OurMemoriesEduSmart.dto.UserProfileDto;
import com.ourmemories.OurMemoriesEduSmart.model.User;
import com.ourmemories.OurMemoriesEduSmart.repository.UserRepository;
import com.ourmemories.OurMemoriesEduSmart.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class ProfileController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserService userService;

    public ProfileController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        return ResponseEntity.ok(new UserProfileDto(user.getEmail(), user.getFirstName(), user.getLastName()));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        userRepository.save(user);
        return ResponseEntity.ok(new ApiResponse(true, "Profile updated", null));
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Current password is incorrect", null));
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok(new ApiResponse(true, "Password changed", null));
    }

    @DeleteMapping("/account")
    public ResponseEntity<ApiResponse> deleteAccount() {
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            userService.deleteUserAccount(userEmail);
            return ResponseEntity.ok(new ApiResponse(true, "Account deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }
}
