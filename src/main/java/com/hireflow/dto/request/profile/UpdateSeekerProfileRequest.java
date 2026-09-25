package com.hireflow.dto.request.profile;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateSeekerProfileRequest {
    @Size(max = 100)
    private String fullName;

    @Size(max = 20)
    private String phone;

    @Size(max = 100)
    private String location;

    private String bio;

    @Size(max = 255)
    private String linkedinUrl;

    @Size(max = 255)
    private String githubUrl;

    @Size(max = 255)
    private String portfolioUrl;

    @Size(max = 200)
    private String headline;

    private Integer yearsOfExperience;

    @Size(max = 50)
    private String jobTypePreference;
}
