package com.ourmemories.OurMemoriesEduSmart.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Applicant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Personal info (existing)
    private String gender;
    private String title;
    private String initials;
    private String lastName;
    private String fullNames;
    private LocalDate dateOfBirth;

    @Column(name = "is_south_african")
    private boolean isSouthAfrican;
    private String idNumber;
    private String passportNumber;
    private String countryOfResidence;

    // Contact info
    private String physicalAddress;
    private String physicalCity;
    private String physicalProvince;
    private String physicalPostalCode;
    private String email;
    private String cellNumber;
    private String homeNumber;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Guardian guardian;

    @OneToMany(mappedBy = "applicant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Application> applications;

    // ==================== NSFAS-SPECIFIC FIELDS (NEW) ====================
    private String fundingType;                    // "Bursary" or "Loan"
    private String isDisabled;                     // "Yes"/"No"
    private String householdIncomeUnder350k;       // "Yes"/"No"
    private String highestGrade;                   // e.g., "Grade 12"
    private String studyCycleYears;                // e.g., "3
    private String hasOfferLetter;                 // "Yes"/"No"
    private String isMarried;                      // "Yes"/"No"

    // Father
    private String fatherAlive;                    // "Yes"/"No"
    private String fatherTitle;
    private String fatherFirstName;
    private String fatherLastName;
    private String fatherIdNumber;

    // Mother
    private String motherAlive;                    // "Yes"/"No"
    private String motherTitle;
    private String motherFirstName;
    private String motherLastName;
    private String motherIdNumber;

    // Guardian (extra fields — reuse same guardian or separate?)
    private String hasGuardian;                    // "Yes"/"No"
    private String guardianTitle;
    private String guardianFirstName;
    private String guardianLastName;
    private String guardianIdNumber;
    private String guardianRelationship;

    // Declaration
    private boolean declaration;
}