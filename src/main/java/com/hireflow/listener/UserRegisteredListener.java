package com.hireflow.listener;

import com.hireflow.entity.NotificationType;
import com.hireflow.event.UserRegisteredEvent;
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
public class UserRegisteredListener {

    private final EmailService emailService;

    @Async
    @EventListener
    public void handle(UserRegisteredEvent event) {
        log.info("Handling UserRegisteredEvent for userId={}", event.getUser().getId());
        emailService.sendVerificationOtp(event.getUser().getEmail(), event.getOtp());
    }
}
