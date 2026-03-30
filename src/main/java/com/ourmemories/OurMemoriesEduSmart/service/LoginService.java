package com.ourmemories.OurMemoriesEduSmart.service;

import com.ourmemories.OurMemoriesEduSmart.dto.LoginRequest;
import com.ourmemories.OurMemoriesEduSmart.dto.LoginResponse;
import com.ourmemories.OurMemoriesEduSmart.dto.UserInformation;
import com.ourmemories.OurMemoriesEduSmart.model.User;
import com.ourmemories.OurMemoriesEduSmart.repository.UserRepository;
import com.ourmemories.OurMemoriesEduSmart.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtUtil jwtUtil;

    public LoginService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        if (!bCryptPasswordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new UsernameNotFoundException("Incorrect password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        UserInformation userInformation = UserInformation.builder()
                .email(user.getEmail())
                .phone(user.getPhoneNumber())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .build();

        return LoginResponse.builder()
                .userInformation(userInformation)
                .token(token)
                .build();
    }

    public LoginResponse verifyToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String token = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token != null && jwtUtil.validateToken(token)) {
            String email = jwtUtil.getEmailFromToken(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

            UserInformation userInformation = UserInformation.builder()
                    .email(user.getEmail())
                    .phone(user.getPhoneNumber())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .role(user.getRole().name())  // ← .name() to get "ADMIN"
                    .build();

            return LoginResponse.builder()
                    .userInformation(userInformation)
                    .token(token)
                    .build();
        }
        return null;
    }
}