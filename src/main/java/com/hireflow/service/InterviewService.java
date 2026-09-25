package com.hireflow.service;

import com.hireflow.dto.request.interview.ScheduleInterviewRequest;
import com.hireflow.dto.response.interview.InterviewResponse;
import com.hireflow.entity.*;
import com.hireflow.event.InterviewScheduledEvent;
import com.hireflow.exception.*;
import com.hireflow.repository.*;
import com.hireflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SeekerProfileRepository seekerProfileRepository;

    @Transactional
    public InterviewResponse scheduleInterview(UserPrincipal principal, ScheduleInterviewRequest request) {
        Application application = applicationRepository.findById(request.getApplicationId())
            .orElseThrow(() -> new ResourceNotFoundException("Application", "id", request.getApplicationId()));

        if (!application.getJob().getRecruiter().getUser().getId().equals(principal.getId())) {
            throw new UnauthorizedException("Not authorized to schedule interview for this application");
        }

        Interview interview = Interview.builder()
            .application(application)
            .scheduledAt(request.getScheduledAt())
            .durationMinutes(request.getDurationMinutes())
            .type(InterviewType.valueOf(request.getType()))
            .meetingLink(request.getMeetingLink())
            .venue(request.getVenue())
            .notes(request.getNotes())
            .build();

        interview = interviewRepository.save(interview);
        eventPublisher.publishEvent(new InterviewScheduledEvent(this, interview));
        return toResponse(interview);
    }

    @Transactional(readOnly = true)
    public List<InterviewResponse> getMyInterviews(UserPrincipal principal) {
        return interviewRepository.findUpcomingBySeekerIdAndScheduledAtAfter(
            principal.getId(), LocalDateTime.now()
        ).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<InterviewResponse> getScheduledInterviews(UserPrincipal principal) {
        return interviewRepository.findByRecruiterId(principal.getId())
            .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public InterviewResponse getInterviewById(Long id, UserPrincipal principal) {
        Interview interview = interviewRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Interview", "id", id));

        boolean isSeeker = interview.getApplication().getSeeker().getId().equals(principal.getId());
        boolean isRecruiter = interview.getApplication().getJob().getRecruiter().getUser().getId().equals(principal.getId());

        if (!isSeeker && !isRecruiter) {
            throw new UnauthorizedException("Not authorized to view this interview");
        }

        return toResponse(interview);
    }

    @Transactional
    public InterviewResponse updateInterview(Long id, UserPrincipal principal, ScheduleInterviewRequest request) {
        Interview interview = interviewRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Interview", "id", id));

        if (!interview.getApplication().getJob().getRecruiter().getUser().getId().equals(principal.getId())) {
            throw new UnauthorizedException("Not authorized to update this interview");
        }

        if (request.getScheduledAt() != null) interview.setScheduledAt(request.getScheduledAt());
        if (request.getDurationMinutes() != null) interview.setDurationMinutes(request.getDurationMinutes());
        if (request.getMeetingLink() != null) interview.setMeetingLink(request.getMeetingLink());
        if (request.getVenue() != null) interview.setVenue(request.getVenue());
        if (request.getNotes() != null) interview.setNotes(request.getNotes());

        interview = interviewRepository.save(interview);
        return toResponse(interview);
    }

    @Transactional
    public void cancelInterview(Long id, UserPrincipal principal) {
        Interview interview = interviewRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Interview", "id", id));

        if (!interview.getApplication().getJob().getRecruiter().getUser().getId().equals(principal.getId())) {
            throw new UnauthorizedException("Not authorized to cancel this interview");
        }

        interview.setStatus("CANCELLED");
        interviewRepository.save(interview);
    }

    private InterviewResponse toResponse(Interview interview) {
        Application app = interview.getApplication();
        SeekerProfile profile = seekerProfileRepository.findByUserId(app.getSeeker().getId()).orElse(null);

        return InterviewResponse.builder()
            .id(interview.getId())
            .applicationId(app.getId())
            .jobTitle(app.getJob().getTitle())
            .companyName(app.getJob().getCompany().getName())
            .seekerName(profile != null ? profile.getFullName() : app.getSeeker().getEmail())
            .scheduledAt(interview.getScheduledAt())
            .durationMinutes(interview.getDurationMinutes())
            .type(interview.getType().name())
            .meetingLink(interview.getMeetingLink())
            .venue(interview.getVenue())
            .notes(interview.getNotes())
            .status(interview.getStatus())
            .build();
    }
}
