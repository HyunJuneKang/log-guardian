package com.shinhan.backend.accesslog;

import jakarta.persistence.*;
import com.shinhan.backend.account.Account;
import java.time.LocalDateTime;

@Entity
@Table(name = "access_log")
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "occurred_at", nullable = false, columnDefinition = "timestamp without time zone")
    private LocalDateTime occurredAt;

    @Column(nullable = false, length = 45)
    private String ip;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccessAction action;

    @Column(length = 100)
    private String target;

    @Column(nullable = false)
    private boolean success;

    protected AccessLog() {
    }

    public AccessLog(Account account, LocalDateTime occurredAt, String ip, AccessAction action, String target, boolean success) {
        this.account = account;
        this.occurredAt = occurredAt;
        this.ip = ip;
        this.action = action;
        this.target = target;
        this.success = success;
    }

    public Long getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public String getIp() {
        return ip;
    }

    public AccessAction getAction() {
        return action;
    }

    public String getTarget() {
        return target;
    }

    public boolean isSuccess() {
        return success;
    }
}
