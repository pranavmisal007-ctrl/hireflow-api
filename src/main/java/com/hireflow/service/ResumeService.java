package com.hireflow.service;

import com.hireflow.entity.ResumeFile;
import com.hireflow.entity.SeekerProfile;
import com.hireflow.dto.response.resume.ResumeAnalysisResponse;
import com.hireflow.exception.ResourceNotFoundException;
import com.hireflow.exception.UnauthorizedException;
import com.hireflow.repository.ResumeFileRepository;
import com.hireflow.repository.SeekerProfileRepository;
import com.hireflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeFileRepository resumeFileRepository;
    private final SeekerProfileRepository seekerProfileRepository;
    private final S3FileService s3FileService;
    private final ResumeParserService resumeParserService;

    @Transactional
    public ResumeFile uploadResume(UserPrincipal principal, MultipartFile file) {
        SeekerProfile profile = seekerProfileRepository.findByUserId(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("SeekerProfile", "userId", principal.getId()));

        String s3Url = s3FileService.uploadFile(file, "resumes", principal.getId());
        // Extract S3 key from URL (simplified - in production parse properly)
        String s3Key = "resumes/" + principal.getId() + "/" + file.getOriginalFilename();

        boolean isFirst = resumeFileRepository.countBySeekerProfileId(profile.getId()) == 0;

        ResumeFile resumeFile = ResumeFile.builder()
            .seekerProfile(profile)
            .fileName(file.getOriginalFilename())
            .s3Key(s3Key)
            .s3Url(s3Url)
            .fileSizeBytes(file.getSize())
            .uploadedAt(LocalDateTime.now())
            .isPrimary(isFirst) // First upload is automatically primary
            .build();

        return resumeFileRepository.save(resumeFile);
    }

    @Transactional(readOnly = true)
    public List<ResumeFile> getMyResumes(UserPrincipal principal) {
        SeekerProfile profile = seekerProfileRepository.findByUserId(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("SeekerProfile", "userId", principal.getId()));
        return resumeFileRepository.findBySeekerProfileIdOrderByUploadedAtDesc(profile.getId());
    }

    @Transactional
    public void deleteResume(UserPrincipal principal, Long resumeId) {
        ResumeFile resumeFile = resumeFileRepository.findById(resumeId)
            .orElseThrow(() -> new ResourceNotFoundException("ResumeFile", "id", resumeId));

        if (!resumeFile.getSeekerProfile().getUser().getId().equals(principal.getId())) {
            throw new UnauthorizedException("Not authorized to delete this resume");
        }

        s3FileService.deleteFile(resumeFile.getS3Key());
        resumeFileRepository.delete(resumeFile);
    }

    @Transactional
    public void setPrimary(UserPrincipal principal, Long resumeId) {
        ResumeFile resumeFile = resumeFileRepository.findById(resumeId)
            .orElseThrow(() -> new ResourceNotFoundException("ResumeFile", "id", resumeId));

        if (!resumeFile.getSeekerProfile().getUser().getId().equals(principal.getId())) {
            throw new UnauthorizedException("Not authorized to update this resume");
        }

        resumeFileRepository.clearPrimaryForProfile(resumeFile.getSeekerProfile().getId());
        resumeFile.setIsPrimary(true);
        resumeFileRepository.save(resumeFile);
    }

    @Transactional
    public ResumeAnalysisResponse analyzeResume(UserPrincipal principal, Long resumeId) throws Exception {
        ResumeFile resumeFile = resumeFileRepository.findById(resumeId)
            .orElseThrow(() -> new ResourceNotFoundException("ResumeFile", "id", resumeId));

        if (!resumeFile.getSeekerProfile().getUser().getId().equals(principal.getId())) {
            throw new UnauthorizedException("Not authorized to analyze this resume");
        }

        // In production: download from S3. Here we do a minimal analysis.
        // The caller should pass the file bytes. This is a simplified version.
        // For now, return a placeholder analysis since we can't re-download from S3 easily here.
        return ResumeAnalysisResponse.builder()
            .resumeFileId(resumeId)
            .atsScore(0)
            .extractedSkills(List.of())
            .keywordCount(0)
            .actionVerbCount(0)
            .hasContactInfo(false)
            .hasExperienceSection(false)
            .hasEducationSection(false)
            .hasSkillsSection(false)
            .recommendations(List.of("Re-upload your resume to get a fresh ATS analysis."))
            .build();
    }
}
