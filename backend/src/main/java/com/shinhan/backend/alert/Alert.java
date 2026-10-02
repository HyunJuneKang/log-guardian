package com.shinhan.backend.alert;

import jakarta.persistence.*;
import com.shinhan.backend.account.Account;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "alert", uniqueConstraints = @UniqueConstraint(name = "uk_alert_run_account", columnNames = {"run_id", "account_id"}))
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", nullable = false, length = 100)
    private String runId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<DetectionRule> rules;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> evidence;

    @Column(nullable = false, columnDefinition = "text")
    private String report;

    @Column(name = "ai_investigated", nullable = false)
    private boolean aiInvestigated;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommended_action", nullable = false, length = 30)
    private RecommendedAction recommendedAction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertStatus status = AlertStatus.OPEN;

    @Column(name = "decided_by", length = 100)
    private String decidedBy;

    @Column(name = "decided_at", columnDefinition = "timestamp without time zone")
    private LocalDateTime decidedAt;

    protected Alert() {
    }

    public Alert(String runId, Account account, List<DetectionRule> rules, Severity severity, Map<String, Object> evidence, String report, boolean aiInvestigated, RecommendedAction recommendedAction) {
        this.runId = runId;
        this.account = account;
        this.rules = new java.util.ArrayList<>(rules);
        this.severity = severity;
        this.evidence = new java.util.LinkedHashMap<>(evidence);
        this.report = report;
        this.aiInvestigated = aiInvestigated;
        this.recommendedAction = recommendedAction;
    }

    public Long getId() {
        return id;
    }

    public String getRunId() {
        return runId;
    }

    public Account getAccount() {
        return account;
    }

    public List<DetectionRule> getRules() {
        return rules;
    }

    public Severity getSeverity() {
        return severity;
    }

    public Map<String, Object> getEvidence() {
        return evidence;
    }

    public String getReport() {
        return report;
    }

    public boolean isAiInvestigated() {
        return aiInvestigated;
    }

    public RecommendedAction getRecommendedAction() {
        return recommendedAction;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public String getDecidedBy() {
        return decidedBy;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }
}
