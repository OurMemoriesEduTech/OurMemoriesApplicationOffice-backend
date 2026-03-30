package com.ourmemories.OurMemoriesEduSmart.controller;

import com.ourmemories.OurMemoriesEduSmart.dto.SignUpRequest;
import com.ourmemories.OurMemoriesEduSmart.repository.UserRepository;
import com.ourmemories.OurMemoriesEduSmart.service.SignUpService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class SignUpController {
    private final SignUpService signUpService;

    public SignUpController(SignUpService signUpService) {
        this.signUpService = signUpService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        try{
            Map<String, Object> response = new HashMap<>();
            response.put("success", signUpService.signUp(signUpRequest));
            return ResponseEntity.ok(response);
        } catch (Exception e){
            Map<String, Object> response = new HashMap<>();
            response.put("fail", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(response);
        }
    }
}
