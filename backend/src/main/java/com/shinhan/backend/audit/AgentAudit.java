package com.shinhan.backend.audit;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "agent_audit")
public class AgentAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", nullable = false, length = 100)
    private String runId;

    @Column(nullable = false, columnDefinition = "text")
    private String path;

    @Column(nullable = false, columnDefinition = "text")
    private String params;

    @Column(name = "http_status", nullable = false)
    private int httpStatus;

    @Column(name = "called_at", nullable = false, columnDefinition = "timestamp without time zone")
    private LocalDateTime calledAt;

    protected AgentAudit() {
    }

    public AgentAudit(String runId, String path, String params, int httpStatus, LocalDateTime calledAt) {
        this.runId = runId;
        this.path = path;
        this.params = params;
        this.httpStatus = httpStatus;
        this.calledAt = calledAt;
    }

    public Long getId() {
        return id;
    }

    public String getRunId() {
        return runId;
    }

    public String getPath() {
        return path;
    }

    public String getParams() {
        return params;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public LocalDateTime getCalledAt() {
        return calledAt;
    }
}
