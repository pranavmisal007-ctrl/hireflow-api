package com.hireflow.controller;

import com.hireflow.dto.response.auth.MessageResponse;
import com.hireflow.dto.response.resume.ResumeAnalysisResponse;
import com.hireflow.entity.ResumeFile;
import com.hireflow.security.UserPrincipal;
import com.hireflow.service.ResumeParserService;
import com.hireflow.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/resume")
@RequiredArgsConstructor
@Tag(name = "Resume", description = "PDF resume upload, management, and ATS analysis")
@SecurityRequirement(name = "Bearer Authentication")
public class ResumeController {

    private final ResumeService resumeService;
    private final ResumeParserService resumeParserService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload PDF resume to S3 (max 5MB)")
    public ResponseEntity<Map<String, Object>> uploadResume(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam("file") MultipartFile file) {
        ResumeFile resumeFile = resumeService.uploadResume(principal, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "id", resumeFile.getId(),
            "fileName", resumeFile.getFileName(),
            "s3Url", resumeFile.getS3Url(),
            "fileSizeBytes", resumeFile.getFileSizeBytes(),
            "isPrimary", resumeFile.getIsPrimary(),
            "uploadedAt", resumeFile.getUploadedAt().toString()
        ));
    }

    @GetMapping
    @Operation(summary = "Get all uploaded resumes")
    public ResponseEntity<List<Map<String, Object>>> getMyResumes(@AuthenticationPrincipal UserPrincipal principal) {
        List<ResumeFile> resumes = resumeService.getMyResumes(principal);
        List<Map<String, Object>> response = resumes.stream().map(r -> Map.<String, Object>of(
            "id", r.getId(),
            "fileName", r.getFileName(),
            "s3Url", r.getS3Url(),
            "fileSizeBytes", r.getFileSizeBytes(),
            "isPrimary", r.getIsPrimary(),
            "uploadedAt", r.getUploadedAt().toString()
        )).toList();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a resume")
    public ResponseEntity<MessageResponse> deleteResume(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long id) {
        resumeService.deleteResume(principal, id);
        return ResponseEntity.ok(new MessageResponse("Resume deleted successfully"));
    }

    @PatchMapping("/{id}/set-primary")
    @Operation(summary = "Set resume as primary")
    public ResponseEntity<MessageResponse> setPrimary(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long id) {
        resumeService.setPrimary(principal, id);
        return ResponseEntity.ok(new MessageResponse("Primary resume updated"));
    }

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload & instantly parse/analyze a PDF resume via PDFBox (returns ATS score)")
    public ResponseEntity<ResumeAnalysisResponse> analyzeResumeDirect(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam("file") MultipartFile file) throws Exception {
        byte[] fileBytes = file.getBytes();
        // Upload first
        ResumeFile saved = resumeService.uploadResume(principal, file);
        // Then analyze with the bytes we already have
        ResumeAnalysisResponse analysis = resumeParserService.parsePdf(fileBytes, saved.getId());
        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/analyze/{id}")
    @Operation(summary = "Get last analysis result for a specific resume ID")
    public ResponseEntity<ResumeAnalysisResponse> getAnalysis(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long id) throws Exception {
        return ResponseEntity.ok(resumeService.analyzeResume(principal, id));
    }
}
