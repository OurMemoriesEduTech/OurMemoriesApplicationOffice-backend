package com.ourmemories.OurMemoriesEduSmart.service;

import com.ourmemories.OurMemoriesEduSmart.dto.*;
import com.ourmemories.OurMemoriesEduSmart.model.Application;
import com.ourmemories.OurMemoriesEduSmart.model.Role;
import com.ourmemories.OurMemoriesEduSmart.model.User;
import com.ourmemories.OurMemoriesEduSmart.repository.ApplicationRepository;
import com.ourmemories.OurMemoriesEduSmart.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final ApplicationRepository applicationRepository;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, EmailService emailService, ApplicationRepository applicationRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.applicationRepository = applicationRepository;
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        return mapToDTO(user);
    }

    public UserDTO createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.valueOf(request.getRole().toUpperCase()))
                .status("ACTIVE")
                .build();

        User saved = userRepository.save(user);
        return mapToDTO(saved);
    }

    public UserDTO updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getRole() != null) user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        if (request.getStatus() != null) user.setStatus(request.getStatus());

        User saved = userRepository.save(user);
        return mapToDTO(saved);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }

    public UserDTO toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        String newStatus = user.getStatus().equals("ACTIVE") ? "SUSPENDED" : "ACTIVE";
        user.setStatus(newStatus);

        User saved = userRepository.save(user);
        return mapToDTO(saved);
    }

    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .role(user.getRole().name())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt() : null)
                .build();
    }

    @Transactional
    public void deleteUserAccount(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Delete all applications of this user (cascades to institutions, subjects, documents)
        List<Application> applications = applicationRepository.findByUserId(user.getId());
        applicationRepository.deleteAll(applications);

        String fullName = user.getFirstName() + " " + user.getLastName();
        String email = user.getEmail();

        userRepository.delete(user);

        // Send confirmation email (async)
        emailService.sendAccountDeletedEmail(email, fullName);
    }
}