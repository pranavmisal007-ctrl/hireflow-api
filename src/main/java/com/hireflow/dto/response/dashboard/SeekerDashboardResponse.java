package com.hireflow.dto.response.dashboard;

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
public class SeekerDashboardResponse {
    private long totalApplications;
    private Map<String, Long> applicationsByStatus;
    private int upcomingInterviewsCount;
    private int profileCompletionPercent;
    private long savedJobsCount;
    private List<RecentActivity> recentActivities;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentActivity {
        private String type;
        private String description;
        private String timestamp;
    }
}
