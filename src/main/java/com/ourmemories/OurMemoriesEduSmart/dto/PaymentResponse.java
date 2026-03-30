package com.ourmemories.OurMemoriesEduSmart.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private Long paymentId;
    private String clientSecret;
    private String paymentIntentId;
    private String status;
    private String message;
    private Double amount;
    private String paymentMethod;
    private String redirectUrl;
    private LocalDateTime expiresAt;
    private String paymentReference;
}