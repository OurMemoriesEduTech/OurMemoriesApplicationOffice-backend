package com.ourmemories.OurMemoriesEduSmart.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignUpRequest {
    @NotNull(message = "First name must not be null")
    private String firstName;

    @NotNull(message = "Last name must not be null")
    private String lastName;

    @NotNull(message = "Phone number must not be null")
    private String phoneNumber;

    @NotNull(message = "Email must not be null")
    @Email(message = "Invalid email")
    private String email;

    @NotNull(message = "Password must not be null")
    private String password;
}
