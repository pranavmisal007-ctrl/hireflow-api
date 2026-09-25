package com.hireflow.service;

import com.hireflow.dto.response.dashboard.SeekerDashboardResponse;
import com.hireflow.entity.ApplicationStatus;
import com.hireflow.entity.User;
import com.hireflow.exception.ResourceNotFoundException;
import com.hireflow.repository.*;
import com.hireflow.security.UserPrincipal;
import com.hireflow.util.ProfileCompletionCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SeekerDashboardService {

    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;
    private final SavedJobRepository savedJobRepository;
    private final SeekerProfileRepository seekerProfileRepository;
    private final UserRepository userRepository;
    private final ProfileCompletionCalculator completionCalculator;

    @Transactional(readOnly = true)
    public SeekerDashboardResponse getDashboard(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        var profile = seekerProfileRepository.findByUserId(principal.getId()).orElse(null);

        // Applications count
        long totalApplications = applicationRepository.countBySeeker(user);

        // Applications by status
        Map<String, Long> byStatus = new HashMap<>();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            byStatus.put(status.name(), 0L);
        }
        List<Object[]> statusCounts = applicationRepository.countByStatusForSeeker(user);
        statusCounts.forEach(row -> byStatus.put(((ApplicationStatus) row[0]).name(), (Long) row[1]));

        // Upcoming interviews in next 7 days
        List<?> upcomingInterviews = interviewRepository.findUpcomingBySeekerIdAndScheduledAtAfter(
            principal.getId(), LocalDateTime.now()
        );

        // Profile completion
        int completionPercent = profile != null ? completionCalculator.calculate(profile) : 0;

        // Saved jobs count
        long savedJobsCount = savedJobRepository.findBySeeker(user,
            org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();

        return SeekerDashboardResponse.builder()
            .totalApplications(totalApplications)
            .applicationsByStatus(byStatus)
            .upcomingInterviewsCount(upcomingInterviews.size())
            .profileCompletionPercent(completionPercent)
            .savedJobsCount(savedJobsCount)
            .recentActivities(List.of())
            .build();
    }
}
