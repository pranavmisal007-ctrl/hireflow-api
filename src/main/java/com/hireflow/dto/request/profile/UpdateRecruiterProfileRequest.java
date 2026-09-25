package com.hireflow.dto.request.profile;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateRecruiterProfileRequest {
    @Size(max = 100)
    private String fullName;

    @Size(max = 20)
    private String phone;

    @Size(max = 100)
    private String designation;
}
