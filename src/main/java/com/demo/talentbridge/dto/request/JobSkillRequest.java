package com.demo.talentbridge.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JobSkillRequest {

    @NotNull(message = "Skill ID is required")
    private Long skillId;

    private String level;
}
