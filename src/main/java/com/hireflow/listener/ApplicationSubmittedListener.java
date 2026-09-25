package com.hireflow.listener;

import com.hireflow.entity.NotificationType;
import com.hireflow.event.ApplicationSubmittedEvent;
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
public class ApplicationSubmittedListener {

    private final EmailService emailService;
    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handle(ApplicationSubmittedEvent event) {
        var application = event.getApplication();
        log.info("Handling ApplicationSubmittedEvent for applicationId={}", application.getId());

        // Email confirmation to seeker
        emailService.sendApplicationConfirmation(
            application.getSeeker().getEmail(),
            application.getJob().getTitle(),
            application.getJob().getCompany().getName()
        );

        // Notify recruiter about new applicant
        notificationService.notify(
            application.getJob().getRecruiter().getUser().getId(),
            NotificationType.GENERAL,
            "New Application Received",
            String.format("A new applicant applied for '%s'", application.getJob().getTitle()),
            application.getId()
        );
    }
}
