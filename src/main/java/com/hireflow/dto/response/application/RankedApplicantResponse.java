package com.hireflow.dto.response.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankedApplicantResponse {
    private Long applicationId;
    private Long seekerId;
    private String seekerName;
    private String seekerEmail;
    private Double matchScore;
    private String status;
    private Set<String> matchedSkills;
    private Set<String> missingSkills;
    private LocalDateTime appliedAt;
    private String coverLetter;
    private Long resumeFileId;
}
