package com.ourmemories.OurMemoriesEduSmart.dto;

import com.ourmemories.OurMemoriesEduSmart.model.Application;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class ApplicationSummaryDTO {

    private Long id;
    private LocalDateTime submittedDate;
    private String status;
    private String applicationType;
    private ApplicantDTO applicant;  // a flattened version without the back‑reference
    // add only fields you need for the report

    // constructor/mapping method
    public static ApplicationSummaryDTO fromEntity(Application app) {
        return ApplicationSummaryDTO.builder()
                .id(app.getId())
                .submittedDate(app.getSubmittedDate())
                .status(String.valueOf(app.getStatus()))
                .applicationType(app.getApplicationType())
                .applicant(ApplicantDTO.fromEntity(app.getApplicant()))
                .build();
    }
}