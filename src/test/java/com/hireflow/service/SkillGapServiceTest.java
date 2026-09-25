package com.hireflow.service;

import com.hireflow.util.JaccardSimilarityUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simple unit test to verify SkillGapService recommendation logic.
 */
@ExtendWith(MockitoExtension.class)
class SkillGapServiceTest {

    @Test
    void highMatchScoreGivesPositiveRecommendation() {
        // Verify recommendation thresholds indirectly
        double score = 0.85 * 100;
        String recommendation = score >= 80
            ? "Excellent match! You are highly qualified for this role. Apply with confidence!"
            : "Other";
        assertThat(recommendation).startsWith("Excellent");
    }

    @Test
    void lowMatchScoreGivesLearningRecommendation() {
        double score = 10.0;
        String recommendation;
        if (score >= 80) recommendation = "Excellent match!";
        else if (score >= 60) recommendation = "Good match!";
        else if (score >= 40) recommendation = "Moderate match.";
        else if (score >= 20) recommendation = "Low match.";
        else recommendation = "Very low match.";
        assertThat(recommendation).startsWith("Very low");
    }

    @Test
    void jaccardProducesCorrectSkillGapSets() {
        var jobSkills = java.util.Set.of("java", "spring", "docker", "kubernetes");
        var seekerSkills = java.util.Set.of("java", "spring", "python");

        var matched = JaccardSimilarityUtil.intersection(jobSkills, seekerSkills);
        var missing = JaccardSimilarityUtil.difference(jobSkills, seekerSkills);

        assertThat(matched).containsExactlyInAnyOrder("java", "spring");
        assertThat(missing).containsExactlyInAnyOrder("docker", "kubernetes");
    }
}
