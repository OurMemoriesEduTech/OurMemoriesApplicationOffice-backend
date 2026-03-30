package com.ourmemories.OurMemoriesEduSmart.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationUpdateRequest {
    private Long id;
    private ApplicantDto applicant;
    private GuardianDto guardian;
    private List<SubjectDto> subjects;
    private String educationStatus;
    private String applicationType;
    private List<InstitutionDto> institutions;

    @Data
    public static class ApplicantDto {
        private String title, initials, fullNames, lastName, gender, dateOfBirth;
        private boolean isSouthAfrican;
        private String idNumber, passportNumber, countryOfResidence;
        private String physicalAddress, physicalCity, physicalProvince, physicalPostalCode;
        private String email, cellNumber, homeNumber;

        public boolean getIsSouthAfrican() {
            return isSouthAfrican;
        }
    }

    @Data
    public static class GuardianDto {
        private String relationship, title, fullNames, surname, initials, cellNumber, email;
    }

    @Data
    public static class SubjectDto {
        private String name;
        private String level;
        private int percentage;
    }

    @Data
    public static class InstitutionDto {
        private String institutionName, institutionType;
        private String firstCourse, secondCourse, thirdCourse;
    }
}
