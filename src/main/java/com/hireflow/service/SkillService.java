package com.hireflow.service;

import com.hireflow.dto.response.skill.SkillResponse;
import com.hireflow.entity.Skill;
import com.hireflow.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "skills", key = "#query + '_' + #limit")
    public List<SkillResponse> searchSkills(String query, int limit) {
        return skillRepository.searchByName(query, PageRequest.of(0, limit))
            .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "skills", key = "'categories'")
    public List<String> getCategories() {
        return skillRepository.findAllCategories();
    }

    private SkillResponse toResponse(Skill skill) {
        return SkillResponse.builder()
            .id(skill.getId())
            .name(skill.getName())
            .category(skill.getCategory())
            .build();
    }
}
