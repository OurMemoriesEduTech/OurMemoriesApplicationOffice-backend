package com.ourmemories.OurMemoriesEduSmart.service;

import com.ourmemories.OurMemoriesEduSmart.dto.SignUpRequest;
import com.ourmemories.OurMemoriesEduSmart.dto.SignUpResponse;
import com.ourmemories.OurMemoriesEduSmart.model.Role;
import com.ourmemories.OurMemoriesEduSmart.model.User;
import com.ourmemories.OurMemoriesEduSmart.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SignUpService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmailService emailService;

    public SignUpService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.emailService = emailService;
    }

    public SignUpResponse signUp(SignUpRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        User user = User.builder()
                .email(signUpRequest.getEmail())
                .password(bCryptPasswordEncoder.encode(signUpRequest.getPassword()))
                .firstName(signUpRequest.getFirstName())
                .lastName(signUpRequest.getLastName())
                .phoneNumber(signUpRequest.getPhoneNumber())
                .role(Role.USER)
                .status("ACTIVE")
                .build();

        User savedUser = userRepository.save(user);

        String dashboardLink = "http://localhost:5173/"; // or your base URL + /dashboard
        String fullName = savedUser.getFirstName() + " " + savedUser.getLastName();
        emailService.sendWelcomeEmail(savedUser.getEmail(), fullName, dashboardLink);

        return SignUpResponse.builder()
                .success(true)
                .message("Signup successful")
                .data(user)
                .build();
    }

    //https://code-with-me.global.jetbrains.com/0dMIU9cw6Pgp0ZKNXPR9PQ#p=IU&fp=F20921A1B54E29182B16E0463ABCC8691EDB23FC5020FF5461B61016969E944F&newUi=true
}
