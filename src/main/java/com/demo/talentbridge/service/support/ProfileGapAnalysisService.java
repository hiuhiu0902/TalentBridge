package com.demo.talentbridge.service.support;

import com.demo.talentbridge.dto.ai.ProfileGapResult;
import com.demo.talentbridge.entity.Candidate;
import com.demo.talentbridge.entity.JobPost;
import com.demo.talentbridge.enums.JobStatus;
import com.demo.talentbridge.repository.JobPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProfileGapAnalysisService {

    @Autowired
    private JobPostRepository jobPostRepository;

    public ProfileGapResult analyzeProfileGap(Candidate candidate, String keyword, Integer limit) {
        int safeLimit = clampLimit(limit, 5);
        List<JobPost> jobs = fetchRelevantJobs(keyword);

        Set<String> candidateSkills = extractCandidateSkills(candidate);

        Map<String, Long> skillDemand = jobs.stream()
                .flatMap(job -> extractJobSkills(job).stream())
                .collect(Collectors.groupingBy(
                        skill -> skill,
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        List<Map.Entry<String, Long>> sortedDemand = skillDemand.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toList());

        List<String> matchedSkills = sortedDemand.stream()
                .map(Map.Entry::getKey)
                .filter(candidateSkills::contains)
                .map(this::humanizeEnumText)
                .limit(10)
                .collect(Collectors.toList());

        List<String> missingSkills = sortedDemand.stream()
                .map(Map.Entry::getKey)
                .filter(skill -> !candidateSkills.contains(skill))
                .map(this::humanizeEnumText)
                .limit(safeLimit)
                .collect(Collectors.toList());

        List<Map<String, Object>> topSkillGaps = sortedDemand.stream()
                .filter(entry -> !candidateSkills.contains(entry.getKey()))
                .limit(safeLimit)
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("skillName", humanizeEnumText(entry.getKey()));
                    item.put("jobCount", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        List<String> exampleJobTitles = jobs.stream()
                .map(JobPost::getTitle)
                .filter(Objects::nonNull)
                .distinct()
                .limit(5)
                .collect(Collectors.toList());

        List<String> suggestedKeywords = buildSuggestedKeywords(candidateSkills, missingSkills, keyword);

        return new ProfileGapResult(
                keyword,
                jobs.size(),
                matchedSkills,
                missingSkills,
                topSkillGaps,
                suggestedKeywords,
                exampleJobTitles
        );
    }

    private List<JobPost> fetchRelevantJobs(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return jobPostRepository.findByStatus(JobStatus.ACTIVE, PageRequest.of(0, 50)).getContent();
        }
        return jobPostRepository.searchByKeyword(keyword.trim(), PageRequest.of(0, 50)).getContent();
    }

    private Set<String> extractCandidateSkills(Candidate candidate) {
        if (candidate == null || candidate.getCandidateSkills() == null) {
            return Set.of();
        }

        return candidate.getCandidateSkills().stream()
                .filter(Objects::nonNull)
                .map(skill -> skill.getSkillName() != null ? skill.getSkillName().name() : null)
                .filter(Objects::nonNull)
                .map(this::normalize)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<String> extractJobSkills(JobPost jobPost) {
        if (jobPost == null || jobPost.getJobSkills() == null) {
            return Set.of();
        }

        return jobPost.getJobSkills().stream()
                .filter(Objects::nonNull)
                .map(skill -> skill.getSkillName() != null ? skill.getSkillName().name() : null)
                .filter(Objects::nonNull)
                .map(this::normalize)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<String> buildSuggestedKeywords(Set<String> candidateSkills, List<String> missingSkills, String keyword) {
        List<String> result = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            result.add(keyword.trim());
        }

        candidateSkills.stream()
                .map(this::humanizeEnumText)
                .limit(3)
                .forEach(result::add);

        missingSkills.stream()
                .limit(3)
                .forEach(result::add);

        return result.stream().distinct().limit(6).collect(Collectors.toList());
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }

        String lower = text.toLowerCase(Locale.ROOT).trim();
        String normalized = Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.replaceAll("\\s+", " ");
    }

    private String humanizeEnumText(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        return text.replace("_", " ");
    }

    private int clampLimit(Integer limit, int defaultValue) {
        if (limit == null) {
            return defaultValue;
        }
        return Math.max(1, Math.min(limit, 20));
    }
}