package com.hireflow.controller;

import com.hireflow.dto.request.profile.*;
import com.hireflow.dto.response.auth.MessageResponse;
import com.hireflow.dto.response.profile.*;
import com.hireflow.security.UserPrincipal;
import com.hireflow.service.SeekerProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/seeker/profile")
@RequiredArgsConstructor
@Tag(name = "Seeker Profile", description = "Manage seeker profile, experience, education, projects, and skills")
@SecurityRequirement(name = "Bearer Authentication")
public class SeekerProfileController {

    private final SeekerProfileService seekerProfileService;

    @GetMapping
    @Operation(summary = "Get own seeker profile")
    public ResponseEntity<SeekerProfileResponse> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(seekerProfileService.getProfile(principal));
    }

    @PutMapping
    @Operation(summary = "Update seeker profile info")
    public ResponseEntity<SeekerProfileResponse> updateProfile(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody UpdateSeekerProfileRequest request) {
        return ResponseEntity.ok(seekerProfileService.updateProfile(principal, request));
    }

    @GetMapping("/completion")
    @Operation(summary = "Get profile completion percentage and checklist")
    public ResponseEntity<ProfileCompletionResponse> getCompletion(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(seekerProfileService.getCompletion(principal));
    }

    // --- Experience ---
    @PostMapping("/experience")
    @Operation(summary = "Add work experience entry")
    public ResponseEntity<MessageResponse> addExperience(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody AddExperienceRequest request) {
        seekerProfileService.addExperience(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Experience added successfully"));
    }

    @PutMapping("/experience/{id}")
    @Operation(summary = "Update work experience entry")
    public ResponseEntity<MessageResponse> updateExperience(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long id,
        @Valid @RequestBody AddExperienceRequest request) {
        seekerProfileService.updateExperience(principal, id, request);
        return ResponseEntity.ok(new MessageResponse("Experience updated successfully"));
    }

    @DeleteMapping("/experience/{id}")
    @Operation(summary = "Delete work experience entry")
    public ResponseEntity<MessageResponse> deleteExperience(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long id) {
        seekerProfileService.deleteExperience(principal, id);
        return ResponseEntity.ok(new MessageResponse("Experience deleted successfully"));
    }

    // --- Education ---
    @PostMapping("/education")
    @Operation(summary = "Add education entry")
    public ResponseEntity<MessageResponse> addEducation(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody AddEducationRequest request) {
        seekerProfileService.addEducation(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Education added successfully"));
    }

    @PutMapping("/education/{id}")
    @Operation(summary = "Update education entry")
    public ResponseEntity<MessageResponse> updateEducation(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long id,
        @Valid @RequestBody AddEducationRequest request) {
        seekerProfileService.updateEducation(principal, id, request);
        return ResponseEntity.ok(new MessageResponse("Education updated successfully"));
    }

    @DeleteMapping("/education/{id}")
    @Operation(summary = "Delete education entry")
    public ResponseEntity<MessageResponse> deleteEducation(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long id) {
        seekerProfileService.deleteEducation(principal, id);
        return ResponseEntity.ok(new MessageResponse("Education deleted successfully"));
    }

    // --- Projects ---
    @PostMapping("/project")
    @Operation(summary = "Add portfolio project")
    public ResponseEntity<MessageResponse> addProject(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody AddProjectRequest request) {
        seekerProfileService.addProject(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Project added successfully"));
    }

    @PutMapping("/project/{id}")
    @Operation(summary = "Update portfolio project")
    public ResponseEntity<MessageResponse> updateProject(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long id,
        @Valid @RequestBody AddProjectRequest request) {
        seekerProfileService.updateProject(principal, id, request);
        return ResponseEntity.ok(new MessageResponse("Project updated successfully"));
    }

    @DeleteMapping("/project/{id}")
    @Operation(summary = "Delete portfolio project")
    public ResponseEntity<MessageResponse> deleteProject(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long id) {
        seekerProfileService.deleteProject(principal, id);
        return ResponseEntity.ok(new MessageResponse("Project deleted successfully"));
    }

    // --- Skills ---
    @PostMapping("/skills")
    @Operation(summary = "Add skill to profile")
    public ResponseEntity<MessageResponse> addSkill(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestBody Map<String, Object> body) {
        Long skillId = Long.parseLong(body.get("skillId").toString());
        String proficiency = (String) body.getOrDefault("proficiency", "INTERMEDIATE");
        Integer yearsUsed = body.containsKey("yearsUsed") ? Integer.parseInt(body.get("yearsUsed").toString()) : null;
        seekerProfileService.addSkill(principal, skillId, proficiency, yearsUsed);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Skill added successfully"));
    }

    @DeleteMapping("/skills/{skillId}")
    @Operation(summary = "Remove skill from profile")
    public ResponseEntity<MessageResponse> removeSkill(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long skillId) {
        seekerProfileService.removeSkill(principal, skillId);
        return ResponseEntity.ok(new MessageResponse("Skill removed successfully"));
    }
}
