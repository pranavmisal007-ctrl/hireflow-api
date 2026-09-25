package com.hireflow.dto.response.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeekerProfileResponse {
    private Long id;
    private Long userId;
    private String email;
    private String fullName;
    private String phone;
    private String location;
    private String bio;
    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;
    private String headline;
    private Integer yearsOfExperience;
    private String jobTypePreference;
    private String avatarUrl;
    private List<ExperienceInfo> experiences;
    private List<EducationInfo> educations;
    private List<ProjectInfo> projects;
    private List<SkillInfo> skills;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExperienceInfo {
        private Long id;
        private String companyName;
        private String title;
        private String startDate;
        private String endDate;
        private Boolean isCurrent;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EducationInfo {
        private Long id;
        private String institution;
        private String degree;
        private String fieldOfStudy;
        private Integer startYear;
        private Integer endYear;
        private String grade;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectInfo {
        private Long id;
        private String title;
        private String description;
        private String techStack;
        private String projectUrl;
        private String repoUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillInfo {
        private Long id;
        private String skillName;
        private String proficiency;
        private Integer yearsUsed;
    }
}
