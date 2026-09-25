package com.hireflow.dto.response.job;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSummaryResponse {
    private Long id;
    private String title;
    private String slug;
    private String location;
    private Boolean isRemote;
    private String jobType;
    private String experienceLevel;
    private Long salaryMin;
    private Long salaryMax;
    private String currency;
    private LocalDate applicationDeadline;
    private String companyName;
    private String companyLogoUrl;
    private String industry;
    private LocalDateTime postedAt;
}
