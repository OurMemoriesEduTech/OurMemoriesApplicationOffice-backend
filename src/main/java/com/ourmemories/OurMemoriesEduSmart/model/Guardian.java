package com.ourmemories.OurMemoriesEduSmart.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Guardian {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String relationship;
    private String title;
    private String initials;
    private String fullNames;
    private String surname;
    private String cellNumber;
    private String email;
}
