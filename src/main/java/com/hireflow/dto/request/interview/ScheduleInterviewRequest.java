package com.hireflow.dto.request.interview;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduleInterviewRequest {
    @NotNull(message = "Application ID is required")
    private Long applicationId;

    @NotNull(message = "Scheduled time is required")
    private LocalDateTime scheduledAt;

    private Integer durationMinutes = 60;

    @NotNull(message = "Interview type is required")
    private String type; // PHONE / VIDEO / ONSITE / TECHNICAL

    private String meetingLink;
    private String venue;
    private String notes;
}
