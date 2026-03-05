package com.demo.talentbridge.dto.request;

import com.demo.talentbridge.enums.SkillLevel;
import com.demo.talentbridge.enums.SkillName;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CandidateSkillRequest {

    @NotNull(message = "Skill name is required")
    private SkillName skillName;

    private SkillLevel level;
}
