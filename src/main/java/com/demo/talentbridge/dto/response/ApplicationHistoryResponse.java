package com.demo.talentbridge.dto.response;

import com.demo.talentbridge.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationHistoryResponse {
    private Long id;
    private ApplicationStatus fromStatus;
    private ApplicationStatus toStatus;
    private String note;
    private String changedByUsername;
    private LocalDateTime changedAt;
}
