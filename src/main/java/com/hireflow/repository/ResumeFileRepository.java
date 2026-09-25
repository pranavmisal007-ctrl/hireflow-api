package com.hireflow.repository;

import com.hireflow.entity.ResumeFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResumeFileRepository extends JpaRepository<ResumeFile, Long> {
    List<ResumeFile> findBySeekerProfileIdOrderByUploadedAtDesc(Long seekerProfileId);
    Optional<ResumeFile> findBySeekerProfileIdAndIsPrimaryTrue(Long seekerProfileId);
    long countBySeekerProfileId(Long seekerProfileId);

    @Modifying
    @Query("UPDATE ResumeFile rf SET rf.isPrimary = false WHERE rf.seekerProfile.id = :profileId")
    void clearPrimaryForProfile(@Param("profileId") Long profileId);
}
