package com.hireflow.controller;

import com.hireflow.dto.request.profile.UpdateRecruiterProfileRequest;
import com.hireflow.dto.response.auth.MessageResponse;
import com.hireflow.dto.response.profile.RecruiterProfileResponse;
import com.hireflow.security.UserPrincipal;
import com.hireflow.service.RecruiterProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/recruiter/profile")
@RequiredArgsConstructor
@Tag(name = "Recruiter Profile", description = "Manage recruiter profile and company information")
@SecurityRequirement(name = "Bearer Authentication")
public class RecruiterProfileController {

    private final RecruiterProfileService recruiterProfileService;

    @GetMapping
    @Operation(summary = "Get own recruiter profile")
    public ResponseEntity<RecruiterProfileResponse> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(recruiterProfileService.getProfile(principal));
    }

    @PutMapping
    @Operation(summary = "Update recruiter profile info")
    public ResponseEntity<RecruiterProfileResponse> updateProfile(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody UpdateRecruiterProfileRequest request) {
        return ResponseEntity.ok(recruiterProfileService.updateProfile(principal, request));
    }

    @PostMapping("/company")
    @Operation(summary = "Register / link a company")
    public ResponseEntity<RecruiterProfileResponse> registerCompany(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestBody Map<String, Object> companyData) {
        return ResponseEntity.ok(recruiterProfileService.registerCompany(principal, companyData));
    }

    @PutMapping("/company")
    @Operation(summary = "Update company details")
    public ResponseEntity<RecruiterProfileResponse> updateCompany(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestBody Map<String, Object> companyData) {
        return ResponseEntity.ok(recruiterProfileService.updateCompany(principal, companyData));
    }
}
