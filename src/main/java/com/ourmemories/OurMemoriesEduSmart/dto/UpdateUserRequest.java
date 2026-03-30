package com.ourmemories.OurMemoriesEduSmart.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String role;
    private String status; // "ACTIVE" or "SUSPENDED"
}