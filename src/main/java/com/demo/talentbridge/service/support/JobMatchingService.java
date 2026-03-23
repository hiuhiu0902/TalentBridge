package com.demo.talentbridge.service.support;

import com.demo.talentbridge.dto.ai.CandidateMatchResult;
import com.demo.talentbridge.dto.ai.JobMatchResult;
import com.demo.talentbridge.entity.Application;
import com.demo.talentbridge.entity.Candidate;
import com.demo.talentbridge.entity.JobPost;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JobMatchingService {

    public List<JobMatchResult> recommendJobsForCandidate(Candidate candidate, List<JobPost> jobs, Integer limit) {
        int safeLimit = clampLimit(limit, 5);

        if (jobs == null || jobs.isEmpty()) {
            return List.of();
        }

        return jobs.stream()
                .filter(Objects::nonNull)
                .map(job -> buildJobMatchResult(candidate, job))
                .sorted(Comparator.comparing(JobMatchResult::matchScore).reversed())
                .limit(safeLimit)
                .collect(Collectors.toList());
    }

    public CandidateMatchResult scoreCandidateForJob(Candidate candidate, JobPost jobPost, Application application) {
        MatchBundle match = calculateMatch(candidate, jobPost);

        return new CandidateMatchResult(
                application != null ? application.getId() : null,
                candidate != null ? candidate.getId() : null,
                candidate != null && candidate.getUser() != null ? candidate.getUser().getFullName() : null,
                candidate != null && candidate.getUser() != null ? candidate.getUser().getEmail() : null,
                match.score(),
                match.matchedSkills(),
                match.missingSkills(),
                match.reasons(),
                round(match.yearsExperience()),
                application != null && application.getStatus() != null ? application.getStatus().name() : null,
                application != null ? application.getAppliedAt() : null,
                application != null ? application.getCvUrlAtTime() : null
        );
    }

    private JobMatchResult buildJobMatchResult(Candidate candidate, JobPost jobPost) {
        MatchBundle match = calculateMatch(candidate, jobPost);

        return new JobMatchResult(
                jobPost.getId(),
                jobPost.getTitle(),
                jobPost.getEmployer() != null ? jobPost.getEmployer().getCompanyName() : null,
                match.score(),
                match.matchedSkills(),
                match.missingSkills(),
                match.reasons(),
                jobPost.getLocation(),
                jobPost.getExperienceLevel() != null ? jobPost.getExperienceLevel().name() : null,
                jobPost.getSalaryMin(),
                jobPost.getSalaryMax(),
                jobPost.getCategory() != null ? jobPost.getCategory().getName() : null,
                jobPost.getPostedAt(),
                jobPost.getExpiredAt()
        );
    }

    private MatchBundle calculateMatch(Candidate candidate, JobPost jobPost) {
        Set<String> candidateSkills = extractCandidateSkills(candidate);
        Set<String> jobSkills = extractJobSkills(jobPost);

        List<String> matchedSkills = jobSkills.stream()
                .filter(candidateSkills::contains)
                .map(this::humanizeEnumText)
                .collect(Collectors.toList());

        List<String> missingSkills = jobSkills.stream()
                .filter(skill -> !candidateSkills.contains(skill))
                .map(this::humanizeEnumText)
                .collect(Collectors.toList());

        int skillScore = scoreSkills(candidateSkills, jobSkills);
        double yearsExperience = estimateYearsExperience(candidate);
        int experienceScore = scoreExperience(jobPost, yearsExperience);
        int locationScore = scoreLocation(candidate, jobPost);
        int keywordScore = scoreKeywordOverlap(candidate, jobPost);
        int profileScore = scoreProfileCompleteness(candidate);

        int totalScore = Math.max(0, Math.min(100,
                skillScore + experienceScore + locationScore + keywordScore + profileScore));

        List<String> reasons = buildReasons(
                matchedSkills,
                missingSkills,
                yearsExperience,
                locationScore,
                keywordScore,
                profileScore
        );

        return new MatchBundle(totalScore, matchedSkills, missingSkills, reasons, yearsExperience);
    }

    private int scoreSkills(Set<String> candidateSkills, Set<String> jobSkills) {
        if (jobSkills.isEmpty()) {
            return 20;
        }

        long matched = jobSkills.stream().filter(candidateSkills::contains).count();
        double ratio = (double) matched / (double) jobSkills.size();

        return (int) Math.round(ratio * 50.0);
    }

    private int scoreExperience(JobPost jobPost, double yearsExperience) {
        String level = jobPost.getExperienceLevel() == null
                ? ""
                : jobPost.getExperienceLevel().name().toUpperCase(Locale.ROOT);

        if (level.contains("INTERN") || level.contains("FRESHER") || level.contains("ENTRY") || level.contains("JUNIOR")) {
            return yearsExperience >= 0 ? 20 : 0;
        }
        if (level.contains("MID")) {
            return yearsExperience >= 2 ? 20 : (yearsExperience >= 1 ? 12 : 5);
        }
        if (level.contains("SENIOR")) {
            return yearsExperience >= 4 ? 20 : (yearsExperience >= 2 ? 12 : 4);
        }
        if (level.contains("LEAD") || level.contains("PRINCIPAL") || level.contains("MANAGER")) {
            return yearsExperience >= 6 ? 20 : (yearsExperience >= 4 ? 10 : 3);
        }

        return yearsExperience >= 1 ? 12 : 6;
    }

    private int scoreLocation(Candidate candidate, JobPost jobPost) {
        String candidateAddress = candidate != null ? candidate.getAddress() : null;
        String jobLocation = jobPost != null ? jobPost.getLocation() : null;

        if (candidateAddress == null || candidateAddress.isBlank() || jobLocation == null || jobLocation.isBlank()) {
            return 0;
        }

        String normalizedCandidateAddress = normalize(candidateAddress);
        String normalizedJobLocation = normalize(jobLocation);

        if (normalizedCandidateAddress.contains(normalizedJobLocation)
                || normalizedJobLocation.contains(normalizedCandidateAddress)) {
            return 10;
        }

        return 0;
    }

    private int scoreKeywordOverlap(Candidate candidate, JobPost jobPost) {
        Set<String> candidateKeywords = extractCandidateKeywords(candidate);
        Set<String> jobKeywords = extractJobKeywords(jobPost);

        if (candidateKeywords.isEmpty() || jobKeywords.isEmpty()) {
            return 0;
        }

        long overlap = jobKeywords.stream().filter(candidateKeywords::contains).count();
        if (overlap >= 4) {
            return 10;
        }
        if (overlap >= 2) {
            return 6;
        }
        if (overlap >= 1) {
            return 3;
        }
        return 0;
    }

    private int scoreProfileCompleteness(Candidate candidate) {
        int score = 0;

        if (candidate != null && candidate.getSummary() != null && !candidate.getSummary().isBlank()) {
            score += 3;
        }
        if (candidate != null && candidate.getCandidateSkills() != null && !candidate.getCandidateSkills().isEmpty()) {
            score += 3;
        }
        if (candidate != null && candidate.getWorkExperiences() != null && !candidate.getWorkExperiences().isEmpty()) {
            score += 2;
        }
        if (candidate != null && candidate.getEducations() != null && !candidate.getEducations().isEmpty()) {
            score += 2;
        }

        return score;
    }

    private List<String> buildReasons(
            List<String> matchedSkills,
            List<String> missingSkills,
            double yearsExperience,
            int locationScore,
            int keywordScore,
            int profileScore
    ) {
        List<String> reasons = new ArrayList<>();

        if (!matchedSkills.isEmpty()) {
            reasons.add("Khớp kỹ năng: " + String.join(", ", matchedSkills.stream().limit(4).toList()));
        }

        if (yearsExperience > 0) {
            reasons.add("Ước tính kinh nghiệm khoảng " + round(yearsExperience) + " năm");
        }

        if (locationScore > 0) {
            reasons.add("Địa điểm làm việc có độ tương thích với hồ sơ hiện tại");
        }

        if (keywordScore > 0) {
            reasons.add("Từ khóa trong hồ sơ và vị trí tuyển dụng có độ trùng khớp");
        }

        if (profileScore >= 7) {
            reasons.add("Hồ sơ ứng viên khá đầy đủ nên việc đối sánh đáng tin cậy hơn");
        }

        if (!missingSkills.isEmpty()) {
            reasons.add("Kỹ năng còn thiếu nổi bật: " + String.join(", ", missingSkills.stream().limit(3).toList()));
        }

        return reasons.stream().limit(5).collect(Collectors.toList());
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

    private Set<String> extractCandidateKeywords(Candidate candidate) {
        Set<String> keywords = new LinkedHashSet<>();

        if (candidate == null) {
            return keywords;
        }

        keywords.addAll(tokenize(candidate.getSummary()));
        keywords.addAll(extractCandidateSkills(candidate));

        if (candidate.getWorkExperiences() != null) {
            candidate.getWorkExperiences().forEach(work -> {
                if (work != null) {
                    keywords.addAll(tokenize(work.getPosition()));
                    keywords.addAll(tokenize(work.getCompany()));
                }
            });
        }

        if (candidate.getEducations() != null) {
            candidate.getEducations().forEach(education -> {
                if (education != null) {
                    keywords.addAll(tokenize(education.getMajor()));
                    keywords.addAll(tokenize(education.getDegree()));
                }
            });
        }

        return keywords;
    }

    private Set<String> extractJobKeywords(JobPost jobPost) {
        Set<String> keywords = new LinkedHashSet<>();

        if (jobPost == null) {
            return keywords;
        }

        keywords.addAll(tokenize(jobPost.getTitle()));
        keywords.addAll(extractJobSkills(jobPost));

        if (jobPost.getCategory() != null) {
            keywords.addAll(tokenize(jobPost.getCategory().getName()));
        }

        keywords.addAll(tokenize(jobPost.getLocation()));

        return keywords;
    }

    private Set<String> tokenize(String raw) {
        if (raw == null || raw.isBlank()) {
            return Set.of();
        }

        String normalized = normalize(raw);
        String[] tokens = normalized.split("[^a-z0-9+#]+");

        Set<String> result = new LinkedHashSet<>();
        for (String token : tokens) {
            if (token != null && token.length() >= 2) {
                result.add(token);
            }
        }
        return result;
    }

    private double estimateYearsExperience(Candidate candidate) {
        if (candidate == null || candidate.getWorkExperiences() == null || candidate.getWorkExperiences().isEmpty()) {
            return 0.0;
        }

        long totalMonths = 0L;
        for (var work : candidate.getWorkExperiences()) {
            if (work == null || work.getStartDate() == null) {
                continue;
            }

            LocalDate start = work.getStartDate();
            LocalDate end = Boolean.TRUE.equals(work.getCurrentlyWorking()) || work.getEndDate() == null
                    ? LocalDate.now()
                    : work.getEndDate();

            if (end.isBefore(start)) {
                continue;
            }

            totalMonths += Math.max(1, ChronoUnit.MONTHS.between(start, end));
        }

        return totalMonths / 12.0;
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

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private record MatchBundle(
            int score,
            List<String> matchedSkills,
            List<String> missingSkills,
            List<String> reasons,
            double yearsExperience
    ) {
    }
}