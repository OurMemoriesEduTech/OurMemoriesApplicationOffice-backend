package com.ourmemories.OurMemoriesEduSmart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private String email;
    private String firstName;
    private String lastName;
}
