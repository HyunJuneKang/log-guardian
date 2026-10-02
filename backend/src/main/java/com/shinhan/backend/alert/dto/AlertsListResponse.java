package com.shinhan.backend.alert.dto;

import java.time.LocalDate;
import java.util.List;

public record AlertsListResponse(
        String runId,
        LocalDate analysisDate,
        List<AlertSummaryResponse> alerts
) {
}
