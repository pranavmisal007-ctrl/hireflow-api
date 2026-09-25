package com.hireflow.listener;

import com.hireflow.entity.NotificationType;
import com.hireflow.event.ApplicationStatusChangedEvent;
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
public class ApplicationStatusChangedListener {

    private final EmailService emailService;
    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handle(ApplicationStatusChangedEvent event) {
        var application = event.getApplication();
        String newStatus = application.getStatus().name();
        log.info("Handling ApplicationStatusChangedEvent: applicationId={}, status={}", application.getId(), newStatus);

        // Email status update to seeker
        emailService.sendStatusUpdateEmail(
            application.getSeeker().getEmail(),
            application.getJob().getTitle(),
            newStatus
        );

        // In-app notification to seeker
        notificationService.notify(
            application.getSeeker().getId(),
            NotificationType.APPLICATION_UPDATE,
            "Application Status Updated",
            String.format("Your application for '%s' is now: %s", application.getJob().getTitle(), newStatus),
            application.getId()
        );
    }
}
