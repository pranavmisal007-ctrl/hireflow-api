package com.hireflow.dto.request.application;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplyJobRequest {
    @NotNull(message = "Job ID is required")
    private Long jobId;

    private String coverLetter;

}
