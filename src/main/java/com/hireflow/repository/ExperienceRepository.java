package com.hireflow.repository;

import com.hireflow.entity.Experience;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExperienceRepository extends JpaRepository<Experience, Long> {
    List<Experience> findBySeekerProfileIdOrderByStartDateDesc(Long seekerProfileId);
    long countBySeekerProfileId(Long seekerProfileId);
}
