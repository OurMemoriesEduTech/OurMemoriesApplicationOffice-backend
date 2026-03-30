package com.ourmemories.OurMemoriesEduSmart.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PaymentInitiateRequest {

    @NotNull(message = "Application ID is required")
    private Long applicationId;

    @NotNull(message = "Payment method is required")
    @Pattern(regexp = "ONLINE|EFT", message = "Payment method must be ONLINE or EFT")
    private String paymentMethod;

    @Size(max = 100, message = "Payment reference cannot exceed 100 characters")
    private String paymentReference;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    // For tracking (optional)
    private String ipAddress;
    private String userAgent;
}