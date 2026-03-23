package com.demo.talentbridge.dto.ai;

import java.util.List;
import java.util.Map;

public record ProfileGapResult(
        String keyword,
        Integer targetJobCount,
        List<String> matchedSkills,
        List<String> missingSkills,
        List<Map<String, Object>> topSkillGaps,
        List<String> suggestedSearchKeywords,
        List<String> exampleJobTitles
) {
}