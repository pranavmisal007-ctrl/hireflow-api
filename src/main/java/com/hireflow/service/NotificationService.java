package com.hireflow.service;

import com.hireflow.entity.Notification;
import com.hireflow.entity.NotificationType;
import com.hireflow.entity.User;
import com.hireflow.dto.response.notification.NotificationResponse;
import com.hireflow.exception.ResourceNotFoundException;
import com.hireflow.exception.UnauthorizedException;
import com.hireflow.repository.NotificationRepository;
import com.hireflow.repository.UserRepository;
import com.hireflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void notify(Long userId, NotificationType type, String title, String message, Long referenceId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Notification notification = Notification.builder()
            .user(user)
            .type(type)
            .title(title)
            .message(message)
            .referenceId(referenceId)
            .createdAt(LocalDateTime.now())
            .build();

        notificationRepository.save(notification);
        log.info("Notification sent to userId={}: {}", userId, title);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(UserPrincipal principal, int page, int size) {
        User user = userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        return notificationRepository.findByUserOrderByCreatedAtDesc(user, PageRequest.of(page, size))
            .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Transactional
    public void markAsRead(Long notificationId, UserPrincipal principal) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (!notification.getUser().getId().equals(principal.getId())) {
            throw new UnauthorizedException("Not authorized to update this notification");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllRead(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));
        notificationRepository.markAllReadByUser(user);
    }

    @Transactional
    public void delete(Long notificationId, UserPrincipal principal) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (!notification.getUser().getId().equals(principal.getId())) {
            throw new UnauthorizedException("Not authorized to delete this notification");
        }

        notificationRepository.delete(notification);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
            .id(n.getId())
            .type(n.getType().name())
            .title(n.getTitle())
            .message(n.getMessage())
            .isRead(n.getIsRead())
            .referenceId(n.getReferenceId())
            .createdAt(n.getCreatedAt())
            .build();
    }
}
