package com.hireflow.listener;

import com.hireflow.entity.NotificationType;
import com.hireflow.event.InterviewScheduledEvent;
import com.hireflow.service.EmailService;
import com.hireflow.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InterviewScheduledListener {

    private final EmailService emailService;
    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handle(InterviewScheduledEvent event) {
        var interview = event.getInterview();
        var application = interview.getApplication();
        log.info("Handling InterviewScheduledEvent for interviewId={}", interview.getId());

        String seekerEmail = application.getSeeker().getEmail();
        String jobTitle = application.getJob().getTitle();
        String companyName = application.getJob().getCompany().getName();

        emailService.sendInterviewInvite(
            seekerEmail,
            jobTitle,
            companyName,
            interview.getScheduledAt().toString(),
            interview.getType().name(),
            interview.getMeetingLink()
        );

        notificationService.notify(
            application.getSeeker().getId(),
            NotificationType.INTERVIEW_SCHEDULED,
            "Interview Scheduled",
            String.format("Interview for '%s' at %s is scheduled on %s",
                jobTitle, companyName, interview.getScheduledAt()),
            interview.getId()
        );
    }
}
