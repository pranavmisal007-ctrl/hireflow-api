package com.hireflow.repository;

import com.hireflow.entity.SeekerProfile;
import com.hireflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeekerProfileRepository extends JpaRepository<SeekerProfile, Long> {
    Optional<SeekerProfile> findByUser(User user);
    Optional<SeekerProfile> findByUserId(Long userId);
    boolean existsByUser(User user);
}
