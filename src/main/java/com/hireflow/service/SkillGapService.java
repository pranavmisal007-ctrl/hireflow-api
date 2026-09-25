package com.hireflow.service;

import com.hireflow.dto.response.skill.SkillGapResponse;
import com.hireflow.entity.Job;
import com.hireflow.exception.ResourceNotFoundException;
import com.hireflow.repository.JobRepository;
import com.hireflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SkillGapService {

    private final MatchingService matchingService;
    private final JobRepository jobRepository;

    @Transactional(readOnly = true)
    public SkillGapResponse analyzeGap(Long jobId, UserPrincipal principal) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        MatchingService.MatchDetails details = matchingService.getMatchDetails(jobId, principal.getId());
        double matchPercent = details.score() * 100;

        String recommendation = generateRecommendation(matchPercent, details.missingRequired().size());

        return SkillGapResponse.builder()
            .jobId(jobId)
            .jobTitle(job.getTitle())
            .matchedSkills(details.matchedSkills())
            .missingRequired(details.missingRequired())
            .missingOptional(details.missingOptional())
            .matchPercent(Math.round(matchPercent * 100.0) / 100.0)
            .recommendation(recommendation)
            .build();
    }

    private String generateRecommendation(double matchPercent, int missingRequired) {
        if (matchPercent >= 80) {
            return "Excellent match! You are highly qualified for this role. Apply with confidence!";
        } else if (matchPercent >= 60) {
            return "Good match! You have most of the required skills. Consider highlighting your relevant experience.";
        } else if (matchPercent >= 40) {
            return String.format("Moderate match. You are missing %d required skill(s). Consider upskilling before applying.", missingRequired);
        } else if (matchPercent >= 20) {
            return "Low match. Significant skill gaps exist. Focus on learning the required technologies first.";
        } else {
            return "Very low match. This role requires skills you have not yet acquired. Consider this as a learning target.";
        }
    }
}
