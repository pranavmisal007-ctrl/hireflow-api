package com.hireflow.dto.request.job;

import lombok.Data;

import java.util.List;

@Data
public class JobFilterRequest {
    private String title;
    private String location;
    private String jobType;
    private String experienceLevel;
    private Boolean isRemote;
    private Long salaryMin;
    private Long salaryMax;
    private List<String> skills;
    private Long companyId;
    private int page = 0;
    private int size = 10;
    private String sortBy = "createdAt";
    private String sortDir = "desc";
}
