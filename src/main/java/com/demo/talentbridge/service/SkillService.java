package com.demo.talentbridge.service;

import com.demo.talentbridge.dto.request.SkillRequest;
import com.demo.talentbridge.dto.response.SkillResponse;

import java.util.List;

public interface SkillService {
    SkillResponse createSkill(SkillRequest request);
    SkillResponse updateSkill(Long id, SkillRequest request);
    void deleteSkill(Long id);
    SkillResponse getSkillById(Long id);
    List<SkillResponse> getAllSkills();
}
