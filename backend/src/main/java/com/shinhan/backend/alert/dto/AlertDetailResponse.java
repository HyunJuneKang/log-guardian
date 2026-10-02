package com.shinhan.backend.alert.dto;

import com.shinhan.backend.account.AccountType;
import com.shinhan.backend.account.AccountStatus;
import com.shinhan.backend.alert.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record AlertDetailResponse(
        long alertId,
        String runId,
        LocalDate analysisDate,
        String accountId,
        String accountName,
        AccountType accountType,
        String accountDept,
        AccountStatus accountStatus,
        List<DetectionRule> rules,
        Severity severity,
        Map<String, Object> evidence,
        String report,
        boolean aiInvestigated,
        RecommendedAction recommendedAction,
        AlertStatus status,
        String decidedBy,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime decidedAt
) {
}
