package com.hireflow.dto.response.interview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewResponse {
    private Long id;
    private Long applicationId;
    private String jobTitle;
    private String companyName;
    private String seekerName;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private String type;
    private String meetingLink;
    private String venue;
    private String notes;
    private String status;
}
