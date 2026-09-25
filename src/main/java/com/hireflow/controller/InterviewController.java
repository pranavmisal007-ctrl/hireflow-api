package com.hireflow.controller;

import com.hireflow.dto.request.interview.ScheduleInterviewRequest;
import com.hireflow.dto.response.auth.MessageResponse;
import com.hireflow.dto.response.interview.InterviewResponse;
import com.hireflow.security.UserPrincipal;
import com.hireflow.service.InterviewService;
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
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
@Tag(name = "Interviews", description = "Interview scheduling and management")
@SecurityRequirement(name = "Bearer Authentication")
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping
    @Operation(summary = "Schedule an interview for an application (RECRUITER only)")
    public ResponseEntity<InterviewResponse> scheduleInterview(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody ScheduleInterviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(interviewService.scheduleInterview(principal, request));
    }

    @GetMapping("/my-interviews")
    @Operation(summary = "Get seeker's upcoming interviews")
    public ResponseEntity<List<InterviewResponse>> getMyInterviews(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(interviewService.getMyInterviews(principal));
    }

    @GetMapping("/scheduled")
    @Operation(summary = "Get recruiter's scheduled interviews")
    public ResponseEntity<List<InterviewResponse>> getScheduledInterviews(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(interviewService.getScheduledInterviews(principal));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get interview detail")
    public ResponseEntity<InterviewResponse> getInterviewById(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long id) {
        return ResponseEntity.ok(interviewService.getInterviewById(id, principal));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Reschedule / update interview (RECRUITER only)")
    public ResponseEntity<InterviewResponse> updateInterview(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long id,
        @RequestBody ScheduleInterviewRequest request) {
        return ResponseEntity.ok(interviewService.updateInterview(id, principal, request));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel interview (RECRUITER only)")
    public ResponseEntity<MessageResponse> cancelInterview(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long id) {
        interviewService.cancelInterview(id, principal);
        return ResponseEntity.ok(new MessageResponse("Interview cancelled successfully"));
    }
}
