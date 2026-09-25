package com.hireflow.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruiterDashboardResponse {
    private long activeJobPostings;
    private long totalApplicants;
    private long applicationsToday;
    private long jobsExpiringSoon;
    private String topJobTitle;
    private long topJobApplicationCount;
    private List<JobSummary> recentJobs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobSummary {
        private Long id;
        private String title;
        private long applicationCount;
        private String status;
    }
}
