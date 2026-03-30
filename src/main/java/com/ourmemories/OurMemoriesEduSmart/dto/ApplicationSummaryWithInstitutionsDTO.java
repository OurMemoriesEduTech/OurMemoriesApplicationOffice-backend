package com.ourmemories.OurMemoriesEduSmart.dto;

import com.ourmemories.OurMemoriesEduSmart.model.Applicant;
import com.ourmemories.OurMemoriesEduSmart.model.Application;
import com.ourmemories.OurMemoriesEduSmart.model.Institution;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
public class ApplicationSummaryWithInstitutionsDTO {
    private Long id;
    private String applicant;
    private String email;
    private String type;
    private String status;
    private LocalDateTime dateSubmitted;
    private List<InstitutionDTO> institutions;

    @Builder
    @Getter
    public static class InstitutionDTO {
        private Long id;
        private String institutionName;
        private String institutionType;
        private String firstCourse;
        private String secondCourse;
        private String thirdCourse;

        public static InstitutionDTO fromEntity(Institution inst) {
            if (inst == null) return null;
            return InstitutionDTO.builder()
                    .id(inst.getId())
                    .institutionName(inst.getInstitutionName())
                    .institutionType(inst.getInstitutionType())
                    .firstCourse(inst.getFirstCourse())
                    .secondCourse(inst.getSecondCourse())
                    .thirdCourse(inst.getThirdCourse())
                    .build();
        }
    }

    public static ApplicationSummaryWithInstitutionsDTO fromEntity(Application app) {
        Applicant ap = app.getApplicant();
        String name = ap != null ? ap.getFullNames() + " " + ap.getLastName() : "Unknown";
        String type = app.getApplicationType() != null ? app.getApplicationType() : "Unknown";

        List<InstitutionDTO> institutions = app.getInstitutions().stream()
                .map(InstitutionDTO::fromEntity)
                .collect(Collectors.toList());

        return ApplicationSummaryWithInstitutionsDTO.builder()
                .id(app.getId())
                .applicant(name)
                .email(ap != null ? ap.getEmail() : "")
                .type(type)
                .status(app.getStatus() != null ? app.getStatus().name() : "PENDING_PAYMENT")
                .dateSubmitted(app.getSubmittedDate())
                .institutions(institutions)
                .build();
    }
}