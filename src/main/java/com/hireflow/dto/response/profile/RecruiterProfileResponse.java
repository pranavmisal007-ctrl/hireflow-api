package com.hireflow.dto.response.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruiterProfileResponse {
    private Long id;
    private Long userId;
    private String email;
    private String fullName;
    private String phone;
    private String designation;
    private CompanyInfo company;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyInfo {
        private Long id;
        private String name;
        private String slug;
        private String website;
        private String industry;
        private String size;
        private String logoUrl;
        private String location;
    }
}
