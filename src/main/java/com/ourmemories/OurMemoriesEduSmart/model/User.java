package com.ourmemories.OurMemoriesEduSmart.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})  // Add this
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String phoneNumber;

    @Column(unique = true)
    private String email;

    @JsonIgnore  // Never serialize password
    private String password;

    private boolean enabled;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String status = "ACTIVE";

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnore  // Ignore to prevent circular reference
    private List<Application> applications;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnore  // Ignore to prevent circular reference
    private List<Payment> payments;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}