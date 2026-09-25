package com.hireflow.controller;

import com.hireflow.dto.response.dashboard.RecruiterDashboardResponse;
import com.hireflow.dto.response.dashboard.SeekerDashboardResponse;
import com.hireflow.security.UserPrincipal;
import com.hireflow.service.RecruiterDashboardService;
import com.hireflow.service.SeekerDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Role-specific dashboard statistics")
@SecurityRequirement(name = "Bearer Authentication")
public class DashboardController {

    private final SeekerDashboardService seekerDashboardService;
    private final RecruiterDashboardService recruiterDashboardService;

    @GetMapping("/seeker")
    @Operation(summary = "Seeker dashboard: applications, interviews, profile completion")
    public ResponseEntity<SeekerDashboardResponse> getSeekerDashboard(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(seekerDashboardService.getDashboard(principal));
    }

    @GetMapping("/recruiter")
    @Operation(summary = "Recruiter dashboard: active jobs, total applicants, activity stats")
    public ResponseEntity<RecruiterDashboardResponse> getRecruiterDashboard(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(recruiterDashboardService.getDashboard(principal));
    }
}
