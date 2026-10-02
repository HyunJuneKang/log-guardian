package com.shinhan.backend.analysis;

import com.shinhan.backend.account.AccountStatus;
import com.shinhan.backend.account.AccountType;
import com.shinhan.backend.alert.*;
import com.shinhan.backend.alert.dto.*;
import com.shinhan.backend.analysis.dto.AnalysisRunResponse;
import com.shinhan.backend.common.ApiException;
import com.shinhan.backend.config.AppProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/** Day1 fixtures only. No database writes or calls to the AI agent. */
@Service
public class SampleAnalysisService {
    private static final long SAMPLE_ALERT_ID = 12;
    private static final List<DetectionRule> RULES = List.of(
            DetectionRule.VOLUME_SPIKE, DetectionRule.OFF_HOURS, DetectionRule.NEW_IP);
    private final AppProperties properties;
    private final Clock clock;
    private final AtomicReference<SampleRun> latestRun = new AtomicReference<>();

    public SampleAnalysisService(AppProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    public AnalysisRunResponse run() {
        String runId = "run-" + LocalDate.now(clock).format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + UUID.randomUUID();
        var run = new SampleRun(runId, properties.analysisDate());
        latestRun.set(run);
        return new AnalysisRunResponse(run.runId(), run.analysisDate(), 1, 1, 0, 0);
    }

    public AlertsListResponse list() {
        var run = latestRun.get();
        if (run == null) {
            return new AlertsListResponse(null, properties.analysisDate(), List.of());
        }
        var summary = new AlertSummaryResponse(SAMPLE_ALERT_ID, "partner_017", "협력사17",
                AccountType.PARTNER, AccountStatus.ACTIVE, RULES, Severity.HIGH, true,
                RecommendedAction.LOCK_ACCOUNT, AlertStatus.OPEN);
        return new AlertsListResponse(run.runId(), run.analysisDate(), List.of(summary));
    }

    public AlertDetailResponse detail(long alertId) {
        var run = latestRun.get();
        if (run == null || alertId != SAMPLE_ALERT_ID) {
            throw new ApiException(HttpStatus.NOT_FOUND, "ALERT_NOT_FOUND", "의심 건을 찾을 수 없습니다.");
        }
        Map<String, Object> evidence = Map.of(
                "VOLUME_SPIKE", Map.of("viewCount", 3120, "distinctCustomers", 2874, "dailyAvg", 60.0, "ratio", 52.0),
                "OFF_HOURS", Map.of("nightViewCount", 3120, "nightDailyAvg", 0.0),
                "NEW_IP", Map.of("newIps", List.of("203.0.113.7"), "knownIps", List.of("10.20.1.17")));
        return new AlertDetailResponse(SAMPLE_ALERT_ID, run.runId(), run.analysisDate(), "partner_017",
                "협력사17", AccountType.PARTNER, "대출모집", AccountStatus.ACTIVE, RULES, Severity.HIGH,
                evidence, "조회 3,120회(고객 2,874명), 평소 하루 평균 60회의 52배. ...", true,
                RecommendedAction.LOCK_ACCOUNT, AlertStatus.OPEN, null, null);
    }

    private record SampleRun(String runId, LocalDate analysisDate) {}
}
