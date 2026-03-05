package com.demo.talentbridge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {
    private long totalUsers;
    private long totalCandidates;
    private long totalEmployers;
    private long totalActiveJobs;
    private long totalPendingJobs;
    private long totalClosedJobs;
    private long totalApplications;
    private long totalCategories;
}
