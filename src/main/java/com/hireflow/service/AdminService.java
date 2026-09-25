package com.hireflow.service;

import com.hireflow.dto.response.dashboard.AdminOverviewResponse;
import com.hireflow.entity.Role;
import com.hireflow.entity.User;
import com.hireflow.exception.ResourceNotFoundException;
import com.hireflow.repository.*;
import com.hireflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public AdminOverviewResponse getOverview() {
        long totalUsers = userRepository.count();
        long totalSeekers = userRepository.findAll().stream()
            .filter(u -> u.getRole() == Role.SEEKER).count();
        long totalRecruiters = userRepository.findAll().stream()
            .filter(u -> u.getRole() == Role.RECRUITER).count();
        long totalJobs = jobRepository.count();
        long activeJobs = jobRepository.countByIsActiveTrue();
        long totalApplications = applicationRepository.count();
        long totalCompanies = companyRepository.count();
        long newUsersThisWeek = userRepository.countByCreatedAtAfter(LocalDateTime.now().minusDays(7));
        long applicationsThisWeek = applicationRepository.countApplicationsSince(LocalDateTime.now().minusDays(7));

        return AdminOverviewResponse.builder()
            .totalUsers(totalUsers)
            .totalSeekers(totalSeekers)
            .totalRecruiters(totalRecruiters)
            .totalJobs(totalJobs)
            .activeJobs(activeJobs)
            .totalApplications(totalApplications)
            .totalCompanies(totalCompanies)
            .newUsersThisWeek(newUsersThisWeek)
            .applicationsThisWeek(applicationsThisWeek)
            .build();
    }

    @Transactional(readOnly = true)
    public Page<User> getAllUsers(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setIsActive(true);
        userRepository.save(user);
    }

    @Transactional
    public void forceDeleteJob(Long jobId) {
        if (!jobRepository.existsById(jobId)) {
            throw new ResourceNotFoundException("Job", "id", jobId);
        }
        jobRepository.deleteById(jobId);
    }
}
