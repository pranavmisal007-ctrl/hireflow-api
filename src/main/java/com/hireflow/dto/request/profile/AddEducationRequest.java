package com.hireflow.dto.request.profile;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddEducationRequest {
    @NotBlank(message = "Institution is required")
    private String institution;

    @NotBlank(message = "Degree is required")
    private String degree;

    private String fieldOfStudy;
    private Integer startYear;
    private Integer endYear;
    private String grade;
}
