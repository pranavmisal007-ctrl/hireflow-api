package com.hireflow.dto.request.profile;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AddExperienceRequest {
    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Title is required")
    private String title;

    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCurrent = false;
    private String description;
}
