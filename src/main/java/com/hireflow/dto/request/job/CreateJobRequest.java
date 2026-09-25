package com.hireflow.dto.request.job;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateJobRequest {

    @NotBlank(message = "Job title is required")
    private String title;

    @NotBlank(message = "Job description is required")
    private String description;

    private String requirements;

    @NotNull(message = "Company ID is required")
    private Long companyId;

    private String location;
    private Boolean isRemote = false;

    @NotBlank(message = "Job type is required")
    private String jobType; // FULL_TIME / PART_TIME / CONTRACT / INTERNSHIP

    @NotBlank(message = "Experience level is required")
    private String experienceLevel; // ENTRY / MID / SENIOR / LEAD

    private Long salaryMin;
    private Long salaryMax;
    private String currency = "INR";
    private LocalDate applicationDeadline;

    private List<JobSkillRequest> skills;

    @Data
    public static class JobSkillRequest {
        private String skillName;
        private Boolean isRequired = true;
    }
}
