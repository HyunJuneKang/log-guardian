package com.shinhan.backend.alert.dto;

import com.shinhan.backend.account.AccountType;
import com.shinhan.backend.account.AccountStatus;
import com.shinhan.backend.alert.*;
import java.util.List;

public record AlertSummaryResponse(
        long alertId,
        String accountId,
        String accountName,
        AccountType accountType,
        AccountStatus accountStatus,
        List<DetectionRule> rules,
        Severity severity,
        boolean aiInvestigated,
        RecommendedAction recommendedAction,
        AlertStatus status
) {
}
