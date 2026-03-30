package com.ourmemories.OurMemoriesEduSmart.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordReset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String otp;
    private LocalDateTime otpExpiryDate;
    @OneToOne
    @JoinColumn(name = "user_email", referencedColumnName = "email")
    private User user;
}
