package com.hireflow.repository;

import com.hireflow.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findBySeekerProfileId(Long seekerProfileId);
}
