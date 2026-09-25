package com.hireflow.controller;

import com.hireflow.dto.request.job.CreateJobRequest;
import com.hireflow.dto.request.job.JobFilterRequest;
import com.hireflow.dto.request.job.UpdateJobRequest;
import com.hireflow.dto.response.auth.MessageResponse;
import com.hireflow.dto.response.job.JobResponse;
import com.hireflow.dto.response.job.PagedJobResponse;
import com.hireflow.security.UserPrincipal;
import com.hireflow.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs", description = "Job posting management and search")
public class JobController {

    private final JobService jobService;

    @PostMapping
    @Operation(summary = "Create a new job posting", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<JobResponse> createJob(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody CreateJobRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.createJob(principal, request));
    }

    @GetMapping
    @Operation(summary = "Browse and search jobs (paginated + filtered)")
    public ResponseEntity<PagedJobResponse> getJobs(
        @Parameter(description = "Job title search") @RequestParam(required = false) String title,
        @Parameter(description = "Location filter") @RequestParam(required = false) String location,
        @Parameter(description = "Job type: FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP") @RequestParam(required = false) String jobType,
        @Parameter(description = "Experience level: ENTRY, MID, SENIOR, LEAD") @RequestParam(required = false) String experienceLevel,
        @RequestParam(required = false) Boolean isRemote,
        @RequestParam(required = false) Long salaryMin,
        @RequestParam(required = false) Long salaryMax,
        @RequestParam(required = false) Long companyId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir) {

        JobFilterRequest filter = new JobFilterRequest();
        filter.setTitle(title);
        filter.setLocation(location);
        filter.setJobType(jobType);
        filter.setExperienceLevel(experienceLevel);
        filter.setIsRemote(isRemote);
        filter.setSalaryMin(salaryMin);
        filter.setSalaryMax(salaryMax);
        filter.setCompanyId(companyId);
        filter.setPage(page);
        filter.setSize(size);
        filter.setSortBy(sortBy);
        filter.setSortDir(sortDir);

        return ResponseEntity.ok(jobService.getJobs(filter));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job details by ID")
    public ResponseEntity<JobResponse> getJobById(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get job details by slug (SEO-friendly URL)")
    public ResponseEntity<JobResponse> getJobBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(jobService.getJobBySlug(slug));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update job posting", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<JobResponse> updateJob(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long id,
        @RequestBody UpdateJobRequest request) {
        return ResponseEntity.ok(jobService.updateJob(principal, id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete (deactivate) a job posting", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<MessageResponse> deleteJob(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long id) {
        jobService.deleteJob(principal, id);
        return ResponseEntity.ok(new MessageResponse("Job deactivated successfully"));
    }

    @GetMapping("/my-posts")
    @Operation(summary = "Get recruiter's own job postings", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<PagedJobResponse> getMyPosts(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(jobService.getMyPosts(principal, page, size));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle job active/inactive status", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<MessageResponse> toggleStatus(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long id) {
        jobService.toggleJobStatus(principal, id);
        return ResponseEntity.ok(new MessageResponse("Job status toggled successfully"));
    }
}
