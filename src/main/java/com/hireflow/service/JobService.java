package com.hireflow.service;

import com.hireflow.dto.request.job.*;
import com.hireflow.dto.response.job.*;
import com.hireflow.entity.*;
import com.hireflow.exception.*;
import com.hireflow.repository.*;
import com.hireflow.security.UserPrincipal;
import com.hireflow.util.SlugUtil;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final JobRepository jobRepository;
    private final JobSkillRepository jobSkillRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final CompanyRepository companyRepository;
    private final NotificationService notificationService;
    private final ApplicationRepository applicationRepository;

    @Transactional
    @CacheEvict(value = "jobs", allEntries = true)
    public JobResponse createJob(UserPrincipal principal, CreateJobRequest request) {
        RecruiterProfile recruiter = recruiterProfileRepository.findByUserId(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("RecruiterProfile", "userId", principal.getId()));

        Company company = companyRepository.findById(request.getCompanyId())
            .orElseThrow(() -> new ResourceNotFoundException("Company", "id", request.getCompanyId()));

        String slug = SlugUtil.generateUniqueSlug(request.getTitle(), jobRepository::existsBySlug);

        Job job = Job.builder()
            .recruiter(recruiter)
            .company(company)
            .title(request.getTitle())
            .slug(slug)
            .description(request.getDescription())
            .requirements(request.getRequirements())
            .location(request.getLocation())
            .isRemote(request.getIsRemote())
            .jobType(request.getJobType())
            .experienceLevel(request.getExperienceLevel())
            .salaryMin(request.getSalaryMin())
            .salaryMax(request.getSalaryMax())
            .currency(request.getCurrency())
            .applicationDeadline(request.getApplicationDeadline())
            .build();

        job = jobRepository.save(job);

        // Save skills
        if (request.getSkills() != null) {
            Job finalJob = job;
            List<JobSkill> skills = request.getSkills().stream()
                .map(s -> JobSkill.builder()
                    .job(finalJob)
                    .skillName(s.getSkillName())
                    .isRequired(s.getIsRequired())
                    .build())
                .toList();
            jobSkillRepository.saveAll(skills);
        }

        log.info("Job created: {} by recruiterId={}", job.getId(), principal.getId());
        return toJobResponse(job);
    }

    @Transactional(readOnly = true)
    public PagedJobResponse getJobs(JobFilterRequest filter) {
        Specification<Job> spec = buildSpecification(filter);
        Sort sort = Sort.by(
            "desc".equalsIgnoreCase(filter.getSortDir()) ? Sort.Direction.DESC : Sort.Direction.ASC,
            filter.getSortBy()
        );
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);
        Page<Job> page = jobRepository.findAll(spec, pageable);

        return PagedJobResponse.builder()
            .content(page.getContent().stream().map(this::toJobSummaryResponse).toList())
            .currentPage(page.getNumber())
            .totalPages(page.getTotalPages())
            .totalElements(page.getTotalElements())
            .pageSize(page.getSize())
            .last(page.isLast())
            .first(page.isFirst())
            .build();
    }

    @Transactional
    public JobResponse getJobById(Long id) {
        Job job = jobRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Job", "id", id));
        incrementViewsAsync(id);
        return toJobResponse(job);
    }

    @Transactional(readOnly = true)
    public JobResponse getJobBySlug(String slug) {
        Job job = jobRepository.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Job", "slug", slug));
        incrementViewsAsync(job.getId());
        return toJobResponse(job);
    }

    @Transactional
    @CacheEvict(value = {"jobs", "job"}, allEntries = true)
    public JobResponse updateJob(UserPrincipal principal, Long jobId, UpdateJobRequest request) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        validateRecruiterOwnership(job, principal.getId());

        if (request.getTitle() != null) job.setTitle(request.getTitle());
        if (request.getDescription() != null) job.setDescription(request.getDescription());
        if (request.getRequirements() != null) job.setRequirements(request.getRequirements());
        if (request.getLocation() != null) job.setLocation(request.getLocation());
        if (request.getIsRemote() != null) job.setIsRemote(request.getIsRemote());
        if (request.getJobType() != null) job.setJobType(request.getJobType());
        if (request.getExperienceLevel() != null) job.setExperienceLevel(request.getExperienceLevel());
        if (request.getSalaryMin() != null) job.setSalaryMin(request.getSalaryMin());
        if (request.getSalaryMax() != null) job.setSalaryMax(request.getSalaryMax());
        if (request.getCurrency() != null) job.setCurrency(request.getCurrency());
        if (request.getApplicationDeadline() != null) job.setApplicationDeadline(request.getApplicationDeadline());

        job = jobRepository.save(job);

        // Replace skills
        if (request.getSkills() != null) {
            jobSkillRepository.deleteByJobId(jobId);
            Job finalJob = job;
            List<JobSkill> skills = request.getSkills().stream()
                .map(s -> JobSkill.builder()
                    .job(finalJob)
                    .skillName(s.getSkillName())
                    .isRequired(s.getIsRequired())
                    .build())
                .toList();
            jobSkillRepository.saveAll(skills);
        }

        return toJobResponse(job);
    }

    @Transactional
    @CacheEvict(value = {"jobs", "job"}, allEntries = true)
    public void deleteJob(UserPrincipal principal, Long jobId) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));
        validateRecruiterOwnership(job, principal.getId());

        job.setIsActive(false);
        jobRepository.save(job);
        log.info("Job {} soft-deleted by recruiterId={}", jobId, principal.getId());
    }

    @Transactional(readOnly = true)
    public PagedJobResponse getMyPosts(UserPrincipal principal, int page, int size) {
        RecruiterProfile recruiter = recruiterProfileRepository.findByUserId(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("RecruiterProfile", "userId", principal.getId()));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Job> jobs = jobRepository.findByRecruiter(recruiter, pageable);

        return PagedJobResponse.builder()
            .content(jobs.getContent().stream().map(this::toJobSummaryResponse).toList())
            .currentPage(jobs.getNumber())
            .totalPages(jobs.getTotalPages())
            .totalElements(jobs.getTotalElements())
            .pageSize(jobs.getSize())
            .last(jobs.isLast())
            .first(jobs.isFirst())
            .build();
    }

    @Transactional
    @CacheEvict(value = {"jobs", "job"}, allEntries = true)
    public void toggleJobStatus(UserPrincipal principal, Long jobId) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));
        validateRecruiterOwnership(job, principal.getId());
        job.setIsActive(!job.getIsActive());
        jobRepository.save(job);
    }

    @Async
    public void incrementViewsAsync(Long jobId) {
        try {
            jobRepository.incrementViewsCount(jobId);
        } catch (Exception e) {
            log.warn("Failed to increment view count for jobId={}: {}", jobId, e.getMessage());
        }
    }

    private void validateRecruiterOwnership(Job job, Long userId) {
        if (!job.getRecruiter().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission to modify this job posting");
        }
    }

    private Specification<Job> buildSpecification(JobFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter active jobs for public browse
            predicates.add(cb.isTrue(root.get("isActive")));

            if (filter.getTitle() != null && !filter.getTitle().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")),
                    "%" + filter.getTitle().toLowerCase() + "%"));
            }
            if (filter.getLocation() != null && !filter.getLocation().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("location")),
                    "%" + filter.getLocation().toLowerCase() + "%"));
            }
            if (filter.getJobType() != null && !filter.getJobType().isBlank()) {
                predicates.add(cb.equal(root.get("jobType"), filter.getJobType()));
            }
            if (filter.getExperienceLevel() != null && !filter.getExperienceLevel().isBlank()) {
                predicates.add(cb.equal(root.get("experienceLevel"), filter.getExperienceLevel()));
            }
            if (filter.getIsRemote() != null) {
                predicates.add(cb.equal(root.get("isRemote"), filter.getIsRemote()));
            }
            if (filter.getSalaryMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("salaryMin"), filter.getSalaryMin()));
            }
            if (filter.getSalaryMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("salaryMax"), filter.getSalaryMax()));
            }
            if (filter.getCompanyId() != null) {
                predicates.add(cb.equal(root.get("company").get("id"), filter.getCompanyId()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private JobResponse toJobResponse(Job job) {
        List<JobSkill> skills = jobSkillRepository.findByJobId(job.getId());

        return JobResponse.builder()
            .id(job.getId())
            .title(job.getTitle())
            .slug(job.getSlug())
            .description(job.getDescription())
            .requirements(job.getRequirements())
            .location(job.getLocation())
            .isRemote(job.getIsRemote())
            .jobType(job.getJobType())
            .experienceLevel(job.getExperienceLevel())
            .salaryMin(job.getSalaryMin())
            .salaryMax(job.getSalaryMax())
            .currency(job.getCurrency())
            .applicationDeadline(job.getApplicationDeadline())
            .isActive(job.getIsActive())
            .viewsCount(job.getViewsCount())
            .company(JobResponse.CompanyInfo.builder()
                .id(job.getCompany().getId())
                .name(job.getCompany().getName())
                .slug(job.getCompany().getSlug())
                .logoUrl(job.getCompany().getLogoUrl())
                .industry(job.getCompany().getIndustry())
                .location(job.getCompany().getLocation())
                .build())
            .recruiter(JobResponse.RecruiterInfo.builder()
                .id(job.getRecruiter().getId())
                .fullName(job.getRecruiter().getFullName())
                .designation(job.getRecruiter().getDesignation())
                .build())
            .skills(skills.stream().map(s -> JobResponse.SkillInfo.builder()
                .skillName(s.getSkillName())
                .isRequired(s.getIsRequired())
                .build()).toList())
            .postedAt(job.getCreatedAt())
            .build();
    }

    private JobSummaryResponse toJobSummaryResponse(Job job) {
        return JobSummaryResponse.builder()
            .id(job.getId())
            .title(job.getTitle())
            .slug(job.getSlug())
            .location(job.getLocation())
            .isRemote(job.getIsRemote())
            .jobType(job.getJobType())
            .experienceLevel(job.getExperienceLevel())
            .salaryMin(job.getSalaryMin())
            .salaryMax(job.getSalaryMax())
            .currency(job.getCurrency())
            .applicationDeadline(job.getApplicationDeadline())
            .companyName(job.getCompany().getName())
            .companyLogoUrl(job.getCompany().getLogoUrl())
            .industry(job.getCompany().getIndustry())
            .postedAt(job.getCreatedAt())
            .build();
    }
}
