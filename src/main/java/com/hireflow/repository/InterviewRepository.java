package com.hireflow.repository;

import com.hireflow.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    @Query("""
        SELECT i FROM Interview i
        WHERE i.application.seeker.id = :seekerId
        AND i.scheduledAt >= :from
        ORDER BY i.scheduledAt ASC
        """)
    List<Interview> findUpcomingBySeekerIdAndScheduledAtAfter(
        @Param("seekerId") Long seekerId,
        @Param("from") LocalDateTime from
    );

    @Query("""
        SELECT i FROM Interview i
        WHERE i.application.job.recruiter.id = :recruiterId
        ORDER BY i.scheduledAt ASC
        """)
    List<Interview> findByRecruiterId(@Param("recruiterId") Long recruiterId);
}
