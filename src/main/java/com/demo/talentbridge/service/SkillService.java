package com.demo.talentbridge.service;

import com.demo.talentbridge.dto.response.SkillResponse;
import com.demo.talentbridge.enums.SkillName;

import java.util.List;

/**
 * Skills are now enum-based (SkillName enum).
 * This service provides utility methods for skill lookups.
 * No DB CRUD needed.
 */
public interface SkillService {
    List<SkillResponse> getAllSkills();
    SkillResponse getSkillByName(SkillName skillName);
}
