package com.hireflow.service;

import com.hireflow.dto.response.dashboard.RecruiterDashboardResponse;
import com.hireflow.entity.RecruiterProfile;
import com.hireflow.exception.ResourceNotFoundException;
import com.hireflow.repository.*;
import com.hireflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecruiterDashboardService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;

    @Transactional(readOnly = true)
    public RecruiterDashboardResponse getDashboard(UserPrincipal principal) {
        RecruiterProfile recruiter = recruiterProfileRepository.findByUserId(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("RecruiterProfile", "userId", principal.getId()));

        // Active job postings
        var myJobs = jobRepository.findByRecruiter(recruiter, PageRequest.of(0, Integer.MAX_VALUE));
        long activeJobs = myJobs.stream().filter(j -> j.getIsActive()).count();

        // Total applicants
        long totalApplicants = applicationRepository.countByRecruiterId(principal.getId());

        // Applications today
        long applicationsToday = applicationRepository.countApplicationsSince(
            LocalDateTime.now().withHour(0).withMinute(0).withSecond(0)
        );

        // Jobs expiring soon (next 7 days)
        List<?> expiringSoon = jobRepository.findExpiringSoon(
            recruiter, LocalDate.now(), LocalDate.now().plusDays(7)
        );

        // Recent jobs as summary
        List<RecruiterDashboardResponse.JobSummary> recentJobs = myJobs.getContent().stream()
            .limit(5)
            .map(j -> RecruiterDashboardResponse.JobSummary.builder()
                .id(j.getId())
                .title(j.getTitle())
                .applicationCount(0) // Would need a separate count query
                .status(j.getIsActive() ? "ACTIVE" : "INACTIVE")
                .build())
            .toList();

        return RecruiterDashboardResponse.builder()
            .activeJobPostings(activeJobs)
            .totalApplicants(totalApplicants)
            .applicationsToday(applicationsToday)
            .jobsExpiringSoon(expiringSoon.size())
            .topJobTitle(recentJobs.isEmpty() ? "N/A" : recentJobs.get(0).getTitle())
            .topJobApplicationCount(0)
            .recentJobs(recentJobs)
            .build();
    }
}
