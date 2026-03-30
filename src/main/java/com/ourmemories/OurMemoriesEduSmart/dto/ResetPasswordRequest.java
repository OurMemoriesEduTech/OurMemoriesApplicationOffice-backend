package com.ourmemories.OurMemoriesEduSmart.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResetPasswordRequest {
    @NotNull()
    private String email;

    @NotNull(message = "Enter your new password")
    private String newPassword;
}
