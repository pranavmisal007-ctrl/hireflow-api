package com.hireflow.service;

import com.hireflow.entity.SeekerProfile;
import com.hireflow.exception.ResourceNotFoundException;
import com.hireflow.repository.JobSkillRepository;
import com.hireflow.repository.SeekerProfileRepository;
import com.hireflow.repository.SeekerSkillRepository;
import com.hireflow.util.JaccardSimilarityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final JobSkillRepository jobSkillRepository;
    private final SeekerSkillRepository seekerSkillRepository;
    private final SeekerProfileRepository seekerProfileRepository;

    /**
     * Computes Jaccard similarity score between job required skills and seeker skills.
     * @return score 0.0 - 1.0
     */
    @Transactional(readOnly = true)
    public double computeMatchScore(Long jobId, Long seekerUserId) {
        SeekerProfile profile = seekerProfileRepository.findByUserId(seekerUserId)
            .orElseThrow(() -> new ResourceNotFoundException("SeekerProfile", "userId", seekerUserId));

        Set<String> jobSkills = jobSkillRepository.findAllSkillNamesByJobId(jobId);
        Set<String> seekerSkills = seekerSkillRepository.findSkillNamesBySeekerProfileId(profile.getId());

        return JaccardSimilarityUtil.compute(jobSkills, seekerSkills);
    }

    @Transactional(readOnly = true)
    public MatchDetails getMatchDetails(Long jobId, Long seekerUserId) {
        SeekerProfile profile = seekerProfileRepository.findByUserId(seekerUserId)
            .orElseThrow(() -> new ResourceNotFoundException("SeekerProfile", "userId", seekerUserId));

        Set<String> requiredSkills = jobSkillRepository.findRequiredSkillNamesByJobId(jobId);
        Set<String> allJobSkills = jobSkillRepository.findAllSkillNamesByJobId(jobId);
        Set<String> seekerSkills = seekerSkillRepository.findSkillNamesBySeekerProfileId(profile.getId());

        Set<String> matchedSkills = JaccardSimilarityUtil.intersection(allJobSkills, seekerSkills);
        Set<String> missingRequired = JaccardSimilarityUtil.difference(requiredSkills, seekerSkills);
        Set<String> optionalSkills = JaccardSimilarityUtil.difference(allJobSkills, requiredSkills);
        Set<String> missingOptional = JaccardSimilarityUtil.difference(optionalSkills, seekerSkills);

        double score = JaccardSimilarityUtil.compute(allJobSkills, seekerSkills);

        return new MatchDetails(matchedSkills, missingRequired, missingOptional, score);
    }

    public record MatchDetails(
        Set<String> matchedSkills,
        Set<String> missingRequired,
        Set<String> missingOptional,
        double score
    ) {}
}
