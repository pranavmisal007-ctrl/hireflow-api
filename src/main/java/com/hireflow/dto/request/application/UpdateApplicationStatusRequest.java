package com.hireflow.dto.request.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateApplicationStatusRequest {
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "APPLIED|SHORTLISTED|REJECTED|HIRED", message = "Invalid status")
    private String status;

    private String recruiterNotes;
}
