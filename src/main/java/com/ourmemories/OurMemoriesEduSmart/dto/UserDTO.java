package com.ourmemories.OurMemoriesEduSmart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String role;           // "USER" or "ADMIN"
    private String status;         // "ACTIVE", "SUSPENDED"
    private LocalDateTime createdAt;
}