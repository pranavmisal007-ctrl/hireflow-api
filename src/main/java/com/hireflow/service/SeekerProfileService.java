package com.hireflow.service;

import com.hireflow.dto.request.profile.*;
import com.hireflow.dto.response.profile.*;
import com.hireflow.entity.*;
import com.hireflow.exception.ResourceNotFoundException;
import com.hireflow.exception.UnauthorizedException;
import com.hireflow.repository.*;
import com.hireflow.security.UserPrincipal;
import com.hireflow.util.ProfileCompletionCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeekerProfileService {

    private final SeekerProfileRepository seekerProfileRepository;
    private final ExperienceRepository experienceRepository;
    private final EducationRepository educationRepository;
    private final ProjectRepository projectRepository;
    private final SeekerSkillRepository seekerSkillRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final ProfileCompletionCalculator completionCalculator;

    @Transactional(readOnly = true)
    public SeekerProfileResponse getProfile(UserPrincipal principal) {
        SeekerProfile profile = getSeekerProfile(principal.getId());
        return buildProfileResponse(profile);
    }

    @Transactional
    public SeekerProfileResponse updateProfile(UserPrincipal principal, UpdateSeekerProfileRequest request) {
        SeekerProfile profile = getSeekerProfile(principal.getId());

        if (request.getFullName() != null) profile.setFullName(request.getFullName());
        if (request.getPhone() != null) profile.setPhone(request.getPhone());
        if (request.getLocation() != null) profile.setLocation(request.getLocation());
        if (request.getBio() != null) profile.setBio(request.getBio());
        if (request.getLinkedinUrl() != null) profile.setLinkedinUrl(request.getLinkedinUrl());
        if (request.getGithubUrl() != null) profile.setGithubUrl(request.getGithubUrl());
        if (request.getPortfolioUrl() != null) profile.setPortfolioUrl(request.getPortfolioUrl());
        if (request.getHeadline() != null) profile.setHeadline(request.getHeadline());
        if (request.getYearsOfExperience() != null) profile.setYearsOfExperience(request.getYearsOfExperience());
        if (request.getJobTypePreference() != null) profile.setJobTypePreference(request.getJobTypePreference());

        profile = seekerProfileRepository.save(profile);
        return buildProfileResponse(profile);
    }

    @Transactional(readOnly = true)
    public ProfileCompletionResponse getCompletion(UserPrincipal principal) {
        SeekerProfile profile = getSeekerProfile(principal.getId());
        int percent = completionCalculator.calculate(profile);

        List<String> completed = new ArrayList<>();
        List<String> pending = new ArrayList<>();

        checkItem("Basic info (name, phone, location)", isBasicInfoComplete(profile), completed, pending);
        checkItem("Avatar uploaded", profile.getAvatarUrl() != null, completed, pending);
        checkItem("Work experience added", experienceRepository.countBySeekerProfileId(profile.getId()) >= 1, completed, pending);
        checkItem("Education added", educationRepository.countBySeekerProfileId(profile.getId()) >= 1, completed, pending);
        checkItem("At least 3 skills added", seekerSkillRepository.findBySeekerProfileId(profile.getId()).size() >= 3, completed, pending);

        checkItem("Bio filled", profile.getBio() != null && !profile.getBio().isBlank(), completed, pending);
        checkItem("Social links added", profile.getLinkedinUrl() != null || profile.getGithubUrl() != null, completed, pending);

        return ProfileCompletionResponse.builder()
            .completionPercentage(percent)
            .completedItems(completed)
            .pendingItems(pending)
            .build();
    }

    @Transactional
    public void addExperience(UserPrincipal principal, AddExperienceRequest request) {
        SeekerProfile profile = getSeekerProfile(principal.getId());
        Experience exp = Experience.builder()
            .seekerProfile(profile)
            .companyName(request.getCompanyName())
            .title(request.getTitle())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .isCurrent(request.getIsCurrent())
            .description(request.getDescription())
            .build();
        experienceRepository.save(exp);
    }

    @Transactional
    public void updateExperience(UserPrincipal principal, Long expId, AddExperienceRequest request) {
        Experience exp = experienceRepository.findById(expId)
            .orElseThrow(() -> new ResourceNotFoundException("Experience", "id", expId));
        validateOwnership(exp.getSeekerProfile(), principal.getId());

        exp.setCompanyName(request.getCompanyName());
        exp.setTitle(request.getTitle());
        exp.setStartDate(request.getStartDate());
        exp.setEndDate(request.getEndDate());
        exp.setIsCurrent(request.getIsCurrent());
        exp.setDescription(request.getDescription());
        experienceRepository.save(exp);
    }

    @Transactional
    public void deleteExperience(UserPrincipal principal, Long expId) {
        Experience exp = experienceRepository.findById(expId)
            .orElseThrow(() -> new ResourceNotFoundException("Experience", "id", expId));
        validateOwnership(exp.getSeekerProfile(), principal.getId());
        experienceRepository.delete(exp);
    }

    @Transactional
    public void addEducation(UserPrincipal principal, AddEducationRequest request) {
        SeekerProfile profile = getSeekerProfile(principal.getId());
        Education edu = Education.builder()
            .seekerProfile(profile)
            .institution(request.getInstitution())
            .degree(request.getDegree())
            .fieldOfStudy(request.getFieldOfStudy())
            .startYear(request.getStartYear())
            .endYear(request.getEndYear())
            .grade(request.getGrade())
            .build();
        educationRepository.save(edu);
    }

    @Transactional
    public void updateEducation(UserPrincipal principal, Long eduId, AddEducationRequest request) {
        Education edu = educationRepository.findById(eduId)
            .orElseThrow(() -> new ResourceNotFoundException("Education", "id", eduId));
        validateOwnership(edu.getSeekerProfile(), principal.getId());

        edu.setInstitution(request.getInstitution());
        edu.setDegree(request.getDegree());
        edu.setFieldOfStudy(request.getFieldOfStudy());
        edu.setStartYear(request.getStartYear());
        edu.setEndYear(request.getEndYear());
        edu.setGrade(request.getGrade());
        educationRepository.save(edu);
    }

    @Transactional
    public void deleteEducation(UserPrincipal principal, Long eduId) {
        Education edu = educationRepository.findById(eduId)
            .orElseThrow(() -> new ResourceNotFoundException("Education", "id", eduId));
        validateOwnership(edu.getSeekerProfile(), principal.getId());
        educationRepository.delete(edu);
    }

    @Transactional
    public void addProject(UserPrincipal principal, AddProjectRequest request) {
        SeekerProfile profile = getSeekerProfile(principal.getId());
        Project proj = Project.builder()
            .seekerProfile(profile)
            .title(request.getTitle())
            .description(request.getDescription())
            .techStack(request.getTechStack())
            .projectUrl(request.getProjectUrl())
            .repoUrl(request.getRepoUrl())
            .build();
        projectRepository.save(proj);
    }

    @Transactional
    public void updateProject(UserPrincipal principal, Long projId, AddProjectRequest request) {
        Project proj = projectRepository.findById(projId)
            .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projId));
        validateOwnership(proj.getSeekerProfile(), principal.getId());

        proj.setTitle(request.getTitle());
        proj.setDescription(request.getDescription());
        proj.setTechStack(request.getTechStack());
        proj.setProjectUrl(request.getProjectUrl());
        proj.setRepoUrl(request.getRepoUrl());
        projectRepository.save(proj);
    }

    @Transactional
    public void deleteProject(UserPrincipal principal, Long projId) {
        Project proj = projectRepository.findById(projId)
            .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projId));
        validateOwnership(proj.getSeekerProfile(), principal.getId());
        projectRepository.delete(proj);
    }

    @Transactional
    public void addSkill(UserPrincipal principal, Long skillId, String proficiency, Integer yearsUsed) {
        SeekerProfile profile = getSeekerProfile(principal.getId());
        Skill skill = skillRepository.findById(skillId)
            .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", skillId));

        if (seekerSkillRepository.existsBySeekerProfileIdAndSkillId(profile.getId(), skillId)) {
            throw new com.hireflow.exception.DuplicateResourceException("Skill", "id", skillId);
        }

        SeekerSkill seekerSkill = SeekerSkill.builder()
            .seekerProfile(profile)
            .skill(skill)
            .proficiency(proficiency)
            .yearsUsed(yearsUsed)
            .build();
        seekerSkillRepository.save(seekerSkill);
    }

    @Transactional
    public void removeSkill(UserPrincipal principal, Long skillId) {
        SeekerProfile profile = getSeekerProfile(principal.getId());
        seekerSkillRepository.deleteBySeekerProfileIdAndSkillId(profile.getId(), skillId);
    }

    private SeekerProfile getSeekerProfile(Long userId) {
        return seekerProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("SeekerProfile", "userId", userId));
    }

    private void validateOwnership(SeekerProfile profile, Long userId) {
        if (!profile.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission to modify this resource");
        }
    }

    private boolean isBasicInfoComplete(SeekerProfile profile) {
        return profile.getFullName() != null && !profile.getFullName().isBlank()
            && profile.getPhone() != null && !profile.getPhone().isBlank()
            && profile.getLocation() != null && !profile.getLocation().isBlank();
    }

    private void checkItem(String label, boolean condition, List<String> completed, List<String> pending) {
        if (condition) completed.add(label);
        else pending.add(label);
    }

    private SeekerProfileResponse buildProfileResponse(SeekerProfile profile) {
        List<SeekerProfileResponse.ExperienceInfo> experiences = experienceRepository
            .findBySeekerProfileIdOrderByStartDateDesc(profile.getId())
            .stream().map(e -> SeekerProfileResponse.ExperienceInfo.builder()
                .id(e.getId())
                .companyName(e.getCompanyName())
                .title(e.getTitle())
                .startDate(e.getStartDate() != null ? e.getStartDate().toString() : null)
                .endDate(e.getEndDate() != null ? e.getEndDate().toString() : null)
                .isCurrent(e.getIsCurrent())
                .description(e.getDescription())
                .build()).toList();

        List<SeekerProfileResponse.EducationInfo> educations = educationRepository
            .findBySeekerProfileIdOrderByEndYearDesc(profile.getId())
            .stream().map(e -> SeekerProfileResponse.EducationInfo.builder()
                .id(e.getId())
                .institution(e.getInstitution())
                .degree(e.getDegree())
                .fieldOfStudy(e.getFieldOfStudy())
                .startYear(e.getStartYear())
                .endYear(e.getEndYear())
                .grade(e.getGrade())
                .build()).toList();

        List<SeekerProfileResponse.ProjectInfo> projects = projectRepository
            .findBySeekerProfileId(profile.getId())
            .stream().map(p -> SeekerProfileResponse.ProjectInfo.builder()
                .id(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .techStack(p.getTechStack())
                .projectUrl(p.getProjectUrl())
                .repoUrl(p.getRepoUrl())
                .build()).toList();

        List<SeekerProfileResponse.SkillInfo> skills = seekerSkillRepository
            .findBySeekerProfileId(profile.getId())
            .stream().map(ss -> SeekerProfileResponse.SkillInfo.builder()
                .id(ss.getId())
                .skillName(ss.getSkill().getName())
                .proficiency(ss.getProficiency())
                .yearsUsed(ss.getYearsUsed())
                .build()).toList();

        return SeekerProfileResponse.builder()
            .id(profile.getId())
            .userId(profile.getUser().getId())
            .email(profile.getUser().getEmail())
            .fullName(profile.getFullName())
            .phone(profile.getPhone())
            .location(profile.getLocation())
            .bio(profile.getBio())
            .linkedinUrl(profile.getLinkedinUrl())
            .githubUrl(profile.getGithubUrl())
            .portfolioUrl(profile.getPortfolioUrl())
            .headline(profile.getHeadline())
            .yearsOfExperience(profile.getYearsOfExperience())
            .jobTypePreference(profile.getJobTypePreference())
            .avatarUrl(profile.getAvatarUrl())
            .experiences(experiences)
            .educations(educations)
            .projects(projects)
            .skills(skills)
            .build();
    }
}
