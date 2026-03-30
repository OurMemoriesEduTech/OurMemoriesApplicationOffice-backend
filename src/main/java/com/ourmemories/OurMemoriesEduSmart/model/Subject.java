package com.ourmemories.OurMemoriesEduSmart.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String level;
    private String percentage;

    @ManyToOne
    @JoinColumn(name = "application_id")
    private Application application;
}