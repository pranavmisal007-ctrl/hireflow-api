package com.hireflow.service;

import com.hireflow.dto.request.profile.UpdateRecruiterProfileRequest;
import com.hireflow.dto.response.profile.RecruiterProfileResponse;
import com.hireflow.entity.Company;
import com.hireflow.entity.RecruiterProfile;
import com.hireflow.exception.DuplicateResourceException;
import com.hireflow.exception.ResourceNotFoundException;
import com.hireflow.repository.CompanyRepository;
import com.hireflow.repository.RecruiterProfileRepository;
import com.hireflow.security.UserPrincipal;
import com.hireflow.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecruiterProfileService {

    private final RecruiterProfileRepository recruiterProfileRepository;
    private final CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public RecruiterProfileResponse getProfile(UserPrincipal principal) {
        RecruiterProfile profile = getRecruiterProfile(principal.getId());
        return toResponse(profile);
    }

    @Transactional
    public RecruiterProfileResponse updateProfile(UserPrincipal principal, UpdateRecruiterProfileRequest request) {
        RecruiterProfile profile = getRecruiterProfile(principal.getId());

        if (request.getFullName() != null) profile.setFullName(request.getFullName());
        if (request.getPhone() != null) profile.setPhone(request.getPhone());
        if (request.getDesignation() != null) profile.setDesignation(request.getDesignation());

        profile = recruiterProfileRepository.save(profile);
        return toResponse(profile);
    }

    @Transactional
    public RecruiterProfileResponse registerCompany(UserPrincipal principal, Map<String, Object> companyData) {
        RecruiterProfile profile = getRecruiterProfile(principal.getId());

        String name = (String) companyData.get("name");
        if (companyRepository.existsByName(name)) {
            throw new DuplicateResourceException("Company", "name", name);
        }

        String slug = SlugUtil.generateUniqueSlug(name, companyRepository::existsByName);

        Company company = Company.builder()
            .name(name)
            .slug(slug)
            .description((String) companyData.get("description"))
            .website((String) companyData.get("website"))
            .industry((String) companyData.get("industry"))
            .size((String) companyData.get("size"))
            .location((String) companyData.get("location"))
            .build();

        company = companyRepository.save(company);
        profile.setCompany(company);
        profile = recruiterProfileRepository.save(profile);
        return toResponse(profile);
    }

    @Transactional
    public RecruiterProfileResponse updateCompany(UserPrincipal principal, Map<String, Object> companyData) {
        RecruiterProfile profile = getRecruiterProfile(principal.getId());
        if (profile.getCompany() == null) {
            throw new ResourceNotFoundException("Company", "recruiterId", principal.getId());
        }

        Company company = profile.getCompany();
        if (companyData.containsKey("description")) company.setDescription((String) companyData.get("description"));
        if (companyData.containsKey("website")) company.setWebsite((String) companyData.get("website"));
        if (companyData.containsKey("industry")) company.setIndustry((String) companyData.get("industry"));
        if (companyData.containsKey("size")) company.setSize((String) companyData.get("size"));
        if (companyData.containsKey("location")) company.setLocation((String) companyData.get("location"));

        companyRepository.save(company);
        return toResponse(profile);
    }

    private RecruiterProfile getRecruiterProfile(Long userId) {
        return recruiterProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("RecruiterProfile", "userId", userId));
    }

    public RecruiterProfile getRecruiterProfileEntity(Long userId) {
        return getRecruiterProfile(userId);
    }

    private RecruiterProfileResponse toResponse(RecruiterProfile profile) {
        RecruiterProfileResponse.CompanyInfo companyInfo = null;
        if (profile.getCompany() != null) {
            Company c = profile.getCompany();
            companyInfo = RecruiterProfileResponse.CompanyInfo.builder()
                .id(c.getId())
                .name(c.getName())
                .slug(c.getSlug())
                .website(c.getWebsite())
                .industry(c.getIndustry())
                .size(c.getSize())
                .logoUrl(c.getLogoUrl())
                .location(c.getLocation())
                .build();
        }

        return RecruiterProfileResponse.builder()
            .id(profile.getId())
            .userId(profile.getUser().getId())
            .email(profile.getUser().getEmail())
            .fullName(profile.getFullName())
            .phone(profile.getPhone())
            .designation(profile.getDesignation())
            .company(companyInfo)
            .build();
    }
}
