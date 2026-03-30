package com.ourmemories.OurMemoriesEduSmart.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
}