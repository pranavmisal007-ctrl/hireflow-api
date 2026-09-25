package com.hireflow.dto.request.job;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateJobRequest {
    private String title;
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
    private List<CreateJobRequest.JobSkillRequest> skills;
}
