package com.hireflow.repository;

import com.hireflow.entity.SeekerSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface SeekerSkillRepository extends JpaRepository<SeekerSkill, Long> {
    List<SeekerSkill> findBySeekerProfileId(Long seekerProfileId);

    @Query("SELECT LOWER(ss.skill.name) FROM SeekerSkill ss WHERE ss.seekerProfile.id = :profileId")
    Set<String> findSkillNamesBySeekerProfileId(@Param("profileId") Long profileId);

    boolean existsBySeekerProfileIdAndSkillId(Long seekerProfileId, Long skillId);
    void deleteBySeekerProfileIdAndSkillId(Long seekerProfileId, Long skillId);
}
