package com.shinhan.backend.account;

import jakarta.persistence.*;

@Entity
@Table(name = "account")
public class Account {

    @Id
    @Column(name = "account_id", nullable = false, length = 100)
    private String accountId;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountType type;

    @Column(nullable = false, length = 100)
    private String dept;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;

    protected Account() {
    }

    public Account(String accountId, String name, AccountType type, String dept) {
        this.accountId = accountId;
        this.name = name;
        this.type = type;
        this.dept = dept;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getName() {
        return name;
    }

    public AccountType getType() {
        return type;
    }

    public String getDept() {
        return dept;
    }

    public AccountStatus getStatus() {
        return status;
    }
}
