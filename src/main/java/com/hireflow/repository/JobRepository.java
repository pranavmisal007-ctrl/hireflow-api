package com.hireflow.repository;

import com.hireflow.entity.Job;
import com.hireflow.entity.RecruiterProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    Optional<Job> findBySlug(String slug);
    boolean existsBySlug(String slug);
    Page<Job> findByRecruiter(RecruiterProfile recruiter, Pageable pageable);
    Page<Job> findByIsActiveTrue(Pageable pageable);

    @Modifying
    @Query("UPDATE Job j SET j.viewsCount = j.viewsCount + 1 WHERE j.id = :jobId")
    void incrementViewsCount(@Param("jobId") Long jobId);

    @Query("SELECT j FROM Job j WHERE j.recruiter = :recruiter AND j.applicationDeadline BETWEEN :now AND :soon")
    List<Job> findExpiringSoon(
        @Param("recruiter") RecruiterProfile recruiter,
        @Param("now") LocalDate now,
        @Param("soon") LocalDate soon
    );

    long countByIsActiveTrue();

    @Query("SELECT j FROM Job j WHERE j.recruiter = :recruiter ORDER BY SIZE(j.recruiter.designation) DESC")
    List<Job> findTopJobByApplicationCount(@Param("recruiter") RecruiterProfile recruiter, Pageable pageable);
}
