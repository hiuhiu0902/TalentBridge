package com.demo.talentbridge.dto.response;

import com.demo.talentbridge.enums.SkillLevel;
import com.demo.talentbridge.enums.SkillName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillResponse {
    private SkillName skillName;
    private String displayName;
    private SkillLevel level;
}
