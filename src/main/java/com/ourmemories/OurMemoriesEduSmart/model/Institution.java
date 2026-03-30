package com.ourmemories.OurMemoriesEduSmart.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Institution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String institutionName;
    private String institutionType;

    private String firstCourse;
    private String secondCourse;
    private String thirdCourse;

    @ManyToOne
    @JoinColumn(name = "application_id")
    private Application application;
}
