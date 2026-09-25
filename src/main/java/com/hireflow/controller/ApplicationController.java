package com.hireflow.controller;

import com.hireflow.dto.request.application.ApplyJobRequest;
import com.hireflow.dto.request.application.UpdateApplicationStatusRequest;
import com.hireflow.dto.response.application.ApplicationResponse;
import com.hireflow.dto.response.application.RankedApplicantResponse;
import com.hireflow.dto.response.auth.MessageResponse;
import com.hireflow.security.UserPrincipal;
import com.hireflow.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Tag(name = "Applications", description = "Job application management")
@SecurityRequirement(name = "Bearer Authentication")
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    @Operation(summary = "Apply to a job (SEEKER only)")
    public ResponseEntity<ApplicationResponse> apply(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody ApplyJobRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.apply(principal, request));
    }

    @GetMapping("/my-applications")
    @Operation(summary = "Get seeker's all applications")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(applicationService.getMyApplications(principal));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get single application details")
    public ResponseEntity<ApplicationResponse> getApplicationById(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getApplicationById(id, principal));
    }

    @GetMapping("/job/{jobId}")
    @Operation(summary = "Get all applicants for a job, ranked by match score (RECRUITER only)")
    public ResponseEntity<List<RankedApplicantResponse>> getRankedApplicants(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long jobId) {
        return ResponseEntity.ok(applicationService.getRankedApplicants(jobId, principal));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update application status (RECRUITER only)")
    public ResponseEntity<ApplicationResponse> updateStatus(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long id,
        @Valid @RequestBody UpdateApplicationStatusRequest request) {
        return ResponseEntity.ok(applicationService.updateStatus(id, request, principal));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Withdraw application (SEEKER only)")
    public ResponseEntity<MessageResponse> withdraw(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long id) {
        applicationService.withdraw(id, principal);
        return ResponseEntity.ok(new MessageResponse("Application withdrawn successfully"));
    }
}
