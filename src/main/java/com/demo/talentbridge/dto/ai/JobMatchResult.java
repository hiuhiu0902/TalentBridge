package com.demo.talentbridge.dto.ai;

import java.util.List;

public record JobMatchResult(
        Long jobId,
        String title,
        String companyName,
        Integer matchScore,
        List<String> matchedSkills,
        List<String> missingSkills,
        List<String> reasons,
        String location,
        String experienceLevel,
        Object salaryMin,
        Object salaryMax,
        String categoryName,
        Object postedAt,
        Object expiredAt
) {
}