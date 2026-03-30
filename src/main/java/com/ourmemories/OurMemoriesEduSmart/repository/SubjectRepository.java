package com.ourmemories.OurMemoriesEduSmart.repository;

import com.ourmemories.OurMemoriesEduSmart.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
}
