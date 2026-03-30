package com.ourmemories.OurMemoriesEduSmart.controller;

import com.ourmemories.OurMemoriesEduSmart.dto.LoginRequest;
import com.ourmemories.OurMemoriesEduSmart.dto.LoginResponse;
import com.ourmemories.OurMemoriesEduSmart.model.User;
import com.ourmemories.OurMemoriesEduSmart.service.LoginService;
import com.ourmemories.OurMemoriesEduSmart.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class LoginController {
    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest,HttpServletResponse httpServletResponse) {
        try{
            LoginResponse loginResponse = loginService.login(loginRequest);

            Cookie cookie = new Cookie("jwt", loginResponse.getToken());
            cookie.setHttpOnly(true);
            cookie.setSecure(false); // set true in production with HTTPS
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60); // 1 day
            httpServletResponse.addCookie(cookie);

            loginResponse.setToken(null);

            Map<String, Object> response = new HashMap<>();
            response.put("success", loginResponse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("fail", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(response);
        }
    }

    @GetMapping("/verify-token")
    public ResponseEntity<Map<String, Object>> verifyToken(HttpServletRequest request) {
      try {
          Map<String, Object> userInfo = new HashMap<>();
          userInfo.put("success", loginService.verifyToken(request));
          return ResponseEntity.ok(userInfo);
      }catch (Exception e){
          Map<String, Object> response = new HashMap<>();
          response.put("fail", e.getMessage());
          return ResponseEntity.badRequest()
                  .body(response);
      }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // False for localhost
        cookie.setPath("/"); // Ensure path matches cookie set in login/signup
        cookie.setMaxAge(0); // Expire immediately
        response.addCookie(cookie);
        return ResponseEntity.ok(Map.of("success", "Logout successful"));
    }
}
