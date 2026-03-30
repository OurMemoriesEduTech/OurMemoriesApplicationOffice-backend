package com.ourmemories.OurMemoriesEduSmart.custom;

import com.ourmemories.OurMemoriesEduSmart.model.User;
import com.ourmemories.OurMemoriesEduSmart.repository.UserRepository;
import com.ourmemories.OurMemoriesEduSmart.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public OAuth2LoginSuccessHandler(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Generate JWT with email + role
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        // Set JWT in HttpOnly cookie
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set true in production (HTTPS)
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60); // 24 hours
        response.addCookie(cookie);

        // Determine redirect URL based on role
        String roleStr = user.getRole().name(); // "ADMIN" or "USER"
        String redirectUrl = "ADMIN".equals(roleStr) ? "/admin-dashboard" : "/application-portal";

        // Return JSON response for React frontend
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format("{\"redirect\": \"%s\"}", redirectUrl);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}