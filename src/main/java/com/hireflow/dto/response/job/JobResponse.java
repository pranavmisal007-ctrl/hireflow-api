package com.hireflow.dto.response.job;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {
    private Long id;
    private String title;
    private String slug;
    private String description;
    private String requirements;
    private String location;
    private Boolean isRemote;
    private String jobType;
    private String experienceLevel;
    private Long salaryMin;
    private Long salaryMax;
    private String currency;
    private LocalDate applicationDeadline;
    private Boolean isActive;
    private Integer viewsCount;
    private CompanyInfo company;
    private RecruiterInfo recruiter;
    private List<SkillInfo> skills;
    private LocalDateTime postedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyInfo {
        private Long id;
        private String name;
        private String slug;
        private String logoUrl;
        private String industry;
        private String location;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecruiterInfo {
        private Long id;
        private String fullName;
        private String designation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillInfo {
        private String skillName;
        private Boolean isRequired;
    }
}
