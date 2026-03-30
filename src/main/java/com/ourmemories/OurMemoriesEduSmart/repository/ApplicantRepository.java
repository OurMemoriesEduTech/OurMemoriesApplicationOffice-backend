package com.ourmemories.OurMemoriesEduSmart.repository;

import com.ourmemories.OurMemoriesEduSmart.model.Applicant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicantRepository extends JpaRepository<Applicant, Long> {
}
