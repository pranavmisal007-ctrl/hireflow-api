package com.hireflow.service;

import com.hireflow.dto.request.application.ApplyJobRequest;
import com.hireflow.dto.request.application.UpdateApplicationStatusRequest;
import com.hireflow.dto.response.application.ApplicationResponse;
import com.hireflow.dto.response.application.RankedApplicantResponse;
import com.hireflow.entity.*;
import com.hireflow.event.ApplicationStatusChangedEvent;
import com.hireflow.event.ApplicationSubmittedEvent;
import com.hireflow.exception.*;
import com.hireflow.repository.*;
import com.hireflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    private final MatchingService matchingService;
    private final ApplicationEventPublisher eventPublisher;
    private final SeekerProfileRepository seekerProfileRepository;

    @Transactional
    public ApplicationResponse apply(UserPrincipal principal, ApplyJobRequest request) {
        User seeker = userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        Job job = jobRepository.findById(request.getJobId())
            .orElseThrow(() -> new ResourceNotFoundException("Job", "id", request.getJobId()));

        if (!job.getIsActive()) {
            throw new ApiException("This job posting is no longer active.", org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        if (applicationRepository.existsBySeekerAndJob(seeker, job)) {
            throw new DuplicateResourceException("Application", "jobId", request.getJobId());
        }

        // Compute Jaccard match score
        double matchScore = 0.0;
        try {
            matchScore = matchingService.computeMatchScore(job.getId(), principal.getId());
        } catch (Exception e) {
            log.warn("Could not compute match score: {}", e.getMessage());
        }



        Application application = Application.builder()
            .job(job)
            .seeker(seeker)
            .coverLetter(request.getCoverLetter())
            .matchScore(matchScore)
            .appliedAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        application = applicationRepository.save(application);

        eventPublisher.publishEvent(new ApplicationSubmittedEvent(this, application));

        return toResponse(application);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getMyApplications(UserPrincipal principal) {
        User seeker = userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));
        return applicationRepository.findBySeeker(seeker).stream()
            .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(Long id, UserPrincipal principal) {
        Application app = applicationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Application", "id", id));

        boolean isSeeker = app.getSeeker().getId().equals(principal.getId());
        boolean isRecruiter = app.getJob().getRecruiter().getUser().getId().equals(principal.getId());

        if (!isSeeker && !isRecruiter) {
            throw new UnauthorizedException("Not authorized to view this application");
        }

        return toResponse(app);
    }

    @Transactional(readOnly = true)
    public List<RankedApplicantResponse> getRankedApplicants(Long jobId, UserPrincipal principal) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        if (!job.getRecruiter().getUser().getId().equals(principal.getId())) {
            throw new UnauthorizedException("Not authorized to view applicants for this job");
        }

        List<Application> applications = applicationRepository.findByJobOrderByMatchScoreDesc(job);

        return applications.stream().map(app -> {
            MatchingService.MatchDetails details;
            try {
                details = matchingService.getMatchDetails(jobId, app.getSeeker().getId());
            } catch (Exception e) {
                details = new MatchingService.MatchDetails(java.util.Set.of(), java.util.Set.of(), java.util.Set.of(), 0.0);
            }

            SeekerProfile profile = seekerProfileRepository.findByUserId(app.getSeeker().getId()).orElse(null);

            return RankedApplicantResponse.builder()
                .applicationId(app.getId())
                .seekerId(app.getSeeker().getId())
                .seekerName(profile != null ? profile.getFullName() : app.getSeeker().getEmail())
                .seekerEmail(app.getSeeker().getEmail())
                .matchScore(app.getMatchScore())
                .status(app.getStatus().name())
                .matchedSkills(details.matchedSkills())
                .missingSkills(details.missingRequired())
                .appliedAt(app.getAppliedAt())
                .coverLetter(app.getCoverLetter())
                .build();
        }).toList();
    }

    @Transactional
    public ApplicationResponse updateStatus(Long applicationId, UpdateApplicationStatusRequest request, UserPrincipal principal) {
        Application app = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        if (!app.getJob().getRecruiter().getUser().getId().equals(principal.getId())) {
            throw new UnauthorizedException("Not authorized to update this application");
        }

        String previousStatus = app.getStatus().name();
        app.setStatus(ApplicationStatus.valueOf(request.getStatus()));
        if (request.getRecruiterNotes() != null) {
            app.setRecruiterNotes(request.getRecruiterNotes());
        }
        app.setUpdatedAt(LocalDateTime.now());
        app = applicationRepository.save(app);

        eventPublisher.publishEvent(new ApplicationStatusChangedEvent(this, app, previousStatus));
        return toResponse(app);
    }

    @Transactional
    public void withdraw(Long applicationId, UserPrincipal principal) {
        Application app = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        if (!app.getSeeker().getId().equals(principal.getId())) {
            throw new UnauthorizedException("Not authorized to withdraw this application");
        }

        applicationRepository.delete(app);
    }

    private ApplicationResponse toResponse(Application app) {
        return ApplicationResponse.builder()
            .id(app.getId())
            .jobId(app.getJob().getId())
            .jobTitle(app.getJob().getTitle())
            .companyName(app.getJob().getCompany().getName())
            .status(app.getStatus().name())
            .coverLetter(app.getCoverLetter())
            .matchScore(app.getMatchScore())
            .recruiterNotes(app.getRecruiterNotes())
            .appliedAt(app.getAppliedAt())
            .updatedAt(app.getUpdatedAt())
            .build();
    }
}
