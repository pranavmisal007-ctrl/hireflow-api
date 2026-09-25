package com.hireflow.repository;

import com.hireflow.entity.Application;
import com.hireflow.entity.ApplicationStatus;
import com.hireflow.entity.Job;
import com.hireflow.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findBySeeker(User seeker);
    Page<Application> findBySeeker(User seeker, Pageable pageable);
    List<Application> findByJobOrderByMatchScoreDesc(Job job);
    boolean existsBySeekerAndJob(User seeker, Job job);
    Optional<Application> findBySeekerAndJob(User seeker, Job job);
    long countByStatus(ApplicationStatus status);
    long countBySeeker(User seeker);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.appliedAt >= :since")
    long countApplicationsSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.recruiter.id = :recruiterId")
    long countByRecruiterId(@Param("recruiterId") Long recruiterId);

    @Query("SELECT a.status, COUNT(a) FROM Application a WHERE a.seeker = :seeker GROUP BY a.status")
    List<Object[]> countByStatusForSeeker(@Param("seeker") User seeker);

    @Query("SELECT a FROM Application a WHERE a.job.recruiter.id = :recruiterId AND a.appliedAt >= :since")
    List<Application> findRecentByRecruiterId(@Param("recruiterId") Long recruiterId, @Param("since") LocalDateTime since);
}
