package com.ourmemories.OurMemoriesEduSmart.dto;

import com.ourmemories.OurMemoriesEduSmart.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInformation {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    // Change this:
    // private Role role;

    // To this:
    private String role;  // ← String, not Role enum
}