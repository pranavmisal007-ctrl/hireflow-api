package com.hireflow.util;

import com.hireflow.entity.SeekerProfile;
import com.hireflow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProfileCompletionCalculator {

    private final ExperienceRepository experienceRepository;
    private final EducationRepository educationRepository;
    private final SeekerSkillRepository skillRepository;
    private final ResumeFileRepository resumeFileRepository;

    /**
     * Calculates profile completion % based on:
     * - Basic info (name, location, phone): 20%
     * - Avatar uploaded: 10%
     * - At least 1 experience: 20%
     * - At least 1 education: 15%
     * - At least 3 skills: 15%
     * - At least 1 resume uploaded: 10%
     * - Bio filled: 5%
     * - Social links added: 5%
     */
    public int calculate(SeekerProfile profile) {
        int score = 0;

        // Basic info (20%)
        if (isNotBlank(profile.getFullName())
            && isNotBlank(profile.getPhone())
            && isNotBlank(profile.getLocation())) {
            score += 20;
        }

        // Avatar (10%)
        if (isNotBlank(profile.getAvatarUrl())) {
            score += 10;
        }

        // Experience (20%)
        if (experienceRepository.countBySeekerProfileId(profile.getId()) >= 1) {
            score += 20;
        }

        // Education (15%)
        if (educationRepository.countBySeekerProfileId(profile.getId()) >= 1) {
            score += 15;
        }

        // Skills (15%)
        long skillCount = skillRepository.findBySeekerProfileId(profile.getId()).size();
        if (skillCount >= 3) {
            score += 15;
        }

        // Resume (10%)
        if (resumeFileRepository.countBySeekerProfileId(profile.getId()) >= 1) {
            score += 10;
        }

        // Bio (5%)
        if (isNotBlank(profile.getBio())) {
            score += 5;
        }

        // Social links (5%)
        if (isNotBlank(profile.getLinkedinUrl()) || isNotBlank(profile.getGithubUrl())) {
            score += 5;
        }

        return score;
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
