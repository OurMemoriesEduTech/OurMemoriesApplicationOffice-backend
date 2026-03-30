package com.ourmemories.OurMemoriesEduSmart.dto;

import com.ourmemories.OurMemoriesEduSmart.model.Applicant;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ApplicantDTO {
    private Long id;
    private String fullNames;
    private String email;
    // other fields you need

    public static ApplicantDTO fromEntity(Applicant applicant) {
        if (applicant == null) return null;
        return ApplicantDTO.builder()
                .id(applicant.getId())
                .fullNames(applicant.getFullNames())
                .email(applicant.getEmail())
                .build();
    }
}