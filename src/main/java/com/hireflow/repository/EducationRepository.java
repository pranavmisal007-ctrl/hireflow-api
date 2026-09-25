package com.hireflow.repository;

import com.hireflow.entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EducationRepository extends JpaRepository<Education, Long> {
    List<Education> findBySeekerProfileIdOrderByEndYearDesc(Long seekerProfileId);
    long countBySeekerProfileId(Long seekerProfileId);
}
