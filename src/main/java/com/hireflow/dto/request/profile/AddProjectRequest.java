package com.hireflow.dto.request.profile;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddProjectRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private String techStack;
    private String projectUrl;
    private String repoUrl;
}
