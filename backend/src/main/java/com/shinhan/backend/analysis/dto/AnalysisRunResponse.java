package com.shinhan.backend.analysis.dto;

import java.time.LocalDate;

public record AnalysisRunResponse(
        String runId,
        LocalDate analysisDate,
        int candidateCount,
        int highCount,
        int mediumCount,
        int lowCount
) {
}
