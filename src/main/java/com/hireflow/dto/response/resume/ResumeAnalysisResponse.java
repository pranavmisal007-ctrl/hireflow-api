package com.hireflow.dto.response.resume;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeAnalysisResponse {
    private Long resumeFileId;
    private int atsScore;
    private List<String> extractedSkills;
    private int keywordCount;
    private int actionVerbCount;
    private boolean hasContactInfo;
    private boolean hasExperienceSection;
    private boolean hasEducationSection;
    private boolean hasSkillsSection;
    private Map<String, String> sectionSummary;
    private List<String> recommendations;
}
