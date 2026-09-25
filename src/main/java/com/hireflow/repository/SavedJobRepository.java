package com.hireflow.repository;

import com.hireflow.entity.SavedJob;
import com.hireflow.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    Optional<SavedJob> findBySeekerAndJobId(User seeker, Long jobId);
    boolean existsBySeekerAndJobId(User seeker, Long jobId);
    Page<SavedJob> findBySeeker(User seeker, Pageable pageable);
    void deleteBySeekerAndJobId(User seeker, Long jobId);
}
