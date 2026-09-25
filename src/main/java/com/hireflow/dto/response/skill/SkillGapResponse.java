package com.hireflow.dto.response.skill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillGapResponse {
    private Long jobId;
    private String jobTitle;
    private Set<String> matchedSkills;
    private Set<String> missingRequired;
    private Set<String> missingOptional;
    private double matchPercent;
    private String recommendation;
}
