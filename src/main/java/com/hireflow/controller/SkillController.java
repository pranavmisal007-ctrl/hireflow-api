package com.hireflow.controller;

import com.hireflow.dto.response.skill.SkillGapResponse;
import com.hireflow.dto.response.skill.SkillResponse;
import com.hireflow.security.UserPrincipal;
import com.hireflow.service.SkillGapService;
import com.hireflow.service.SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/skills")
@RequiredArgsConstructor
@Tag(name = "Skills", description = "Skill search and gap analysis")
public class SkillController {

    private final SkillService skillService;
    private final SkillGapService skillGapService;

    @GetMapping
    @Operation(summary = "Search skills by name (autocomplete)")
    public ResponseEntity<List<SkillResponse>> searchSkills(
        @RequestParam(defaultValue = "") String query,
        @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(skillService.searchSkills(query, limit));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all skill categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(skillService.getCategories());
    }

    @GetMapping("/gap/{jobId}")
    @Operation(summary = "Get skill gap analysis for a job (SEEKER only)",
               security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<SkillGapResponse> getSkillGap(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long jobId) {
        return ResponseEntity.ok(skillGapService.analyzeGap(jobId, principal));
    }
}
