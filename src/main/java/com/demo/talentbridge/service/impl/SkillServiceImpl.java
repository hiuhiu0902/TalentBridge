package com.demo.talentbridge.service.impl;

import com.demo.talentbridge.dto.response.SkillResponse;
import com.demo.talentbridge.enums.SkillName;
import com.demo.talentbridge.service.SkillService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Skills are enum-based. No DB interaction needed.
 */
@Service
public class SkillServiceImpl implements SkillService {

    @Override
    public List<SkillResponse> getAllSkills() {
        return Arrays.stream(SkillName.values())
                .map(s -> SkillResponse.builder()
                        .skillName(s)
                        .displayName(s.name())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public SkillResponse getSkillByName(SkillName skillName) {
        return SkillResponse.builder()
                .skillName(skillName)
                .displayName(skillName.name())
                .build();
    }
}
