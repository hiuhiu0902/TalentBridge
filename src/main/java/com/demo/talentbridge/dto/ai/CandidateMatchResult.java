package com.demo.talentbridge.dto.ai;

import java.util.List;

public record CandidateMatchResult(
        Long applicationId,
        Long candidateId,
        String candidateName,
        String candidateEmail,
        Integer matchScore,
        List<String> matchedSkills,
        List<String> missingSkills,
        List<String> reasons,
        Double estimatedYearsExperience,
        String applicationStatus,
        Object appliedAt,
        String cvUrlAtTime
) {
}