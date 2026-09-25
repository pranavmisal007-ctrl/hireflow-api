package com.hireflow.repository;

import com.hireflow.entity.JobSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface JobSkillRepository extends JpaRepository<JobSkill, Long> {
    List<JobSkill> findByJobId(Long jobId);
    void deleteByJobId(Long jobId);

    @Query("SELECT LOWER(js.skillName) FROM JobSkill js WHERE js.job.id = :jobId AND js.isRequired = true")
    Set<String> findRequiredSkillNamesByJobId(@Param("jobId") Long jobId);

    @Query("SELECT LOWER(js.skillName) FROM JobSkill js WHERE js.job.id = :jobId")
    Set<String> findAllSkillNamesByJobId(@Param("jobId") Long jobId);
}
