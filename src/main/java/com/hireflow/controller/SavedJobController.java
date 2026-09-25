package com.hireflow.controller;

import com.hireflow.dto.response.auth.MessageResponse;
import com.hireflow.dto.response.job.JobSummaryResponse;
import com.hireflow.entity.SavedJob;
import com.hireflow.security.UserPrincipal;
import com.hireflow.service.SavedJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/saved-jobs")
@RequiredArgsConstructor
@Tag(name = "Saved Jobs", description = "Bookmark/save job postings")
@SecurityRequirement(name = "Bearer Authentication")
public class SavedJobController {

    private final SavedJobService savedJobService;

    @PostMapping("/{jobId}")
    @Operation(summary = "Save / bookmark a job")
    public ResponseEntity<MessageResponse> saveJob(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long jobId) {
        savedJobService.saveJob(principal, jobId);
        return ResponseEntity.ok(new MessageResponse("Job saved successfully"));
    }

    @DeleteMapping("/{jobId}")
    @Operation(summary = "Remove saved job")
    public ResponseEntity<MessageResponse> unsaveJob(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long jobId) {
        savedJobService.unsaveJob(principal, jobId);
        return ResponseEntity.ok(new MessageResponse("Job removed from saved list"));
    }

    @GetMapping
    @Operation(summary = "Get all saved jobs")
    public ResponseEntity<Map<String, Object>> getSavedJobs(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        Page<SavedJob> savedJobs = savedJobService.getSavedJobs(principal, page, size);
        List<Map<String, Object>> content = savedJobs.getContent().stream().map(sj -> Map.of(
            "id", sj.getId(),
            "jobId", sj.getJob().getId(),
            "jobTitle", sj.getJob().getTitle(),
            "companyName", sj.getJob().getCompany().getName(),
            "savedAt", sj.getSavedAt().toString()
        )).toList();

        return ResponseEntity.ok(Map.of(
            "content", content,
            "totalElements", savedJobs.getTotalElements(),
            "totalPages", savedJobs.getTotalPages(),
            "currentPage", savedJobs.getNumber()
        ));
    }
}
