package com.shinhan.backend;

import com.shinhan.backend.account.Account;
import com.shinhan.backend.account.AccountStatus;
import com.shinhan.backend.account.AccountType;
import com.shinhan.backend.accesslog.AccessAction;
import com.shinhan.backend.accesslog.AccessLog;
import com.shinhan.backend.alert.Alert;
import com.shinhan.backend.alert.AlertStatus;
import com.shinhan.backend.alert.DetectionRule;
import com.shinhan.backend.alert.RecommendedAction;
import com.shinhan.backend.alert.Severity;
import com.shinhan.backend.audit.AgentAudit;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Uses a unique disposable schema; never creates or drops tables in public. */
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@Transactional
class EntityMappingTests {
    private static final String SCHEMA = "entity_test_" + UUID.randomUUID().toString().replace("-", "");

    @DynamicPropertySource
    static void isolateSchema(DynamicPropertyRegistry registry) {
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> SCHEMA);
    }

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsFourEntitiesWithJsonbAndUnshiftedLocalTime() {
        var occurredAt = LocalDateTime.of(2026, 10, 8, 3, 0, 0);
        var account = new Account("partner_017", "협력사17", AccountType.PARTNER, "대출모집");
        entityManager.persist(account);
        var log = new AccessLog(account, occurredAt, "203.0.113.7", AccessAction.VIEW, "C0001532", true);
        entityManager.persist(log);
        var numbers = new LinkedHashMap<String, Object>();
        numbers.put("viewCount", 3120);
        numbers.put("dailyAvg", 0);
        numbers.put("ratio", null);
        var alert = new Alert("run-test-01", account, List.of(DetectionRule.VOLUME_SPIKE),
                Severity.HIGH, Map.of("VOLUME_SPIKE", numbers), "근거 보고서", true,
                RecommendedAction.LOCK_ACCOUNT);
        entityManager.persist(alert);
        var audit = new AgentAudit("run-test-01", "/internal/logs", "accountId=partner_017", 200, occurredAt);
        entityManager.persist(audit);
        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(Account.class, account.getAccountId()).getStatus()).isEqualTo(AccountStatus.ACTIVE);
        var savedLog = entityManager.find(AccessLog.class, log.getId());
        assertThat(savedLog.getOccurredAt()).isEqualTo(occurredAt);
        assertThat(savedLog.getAccount().getAccountId()).isEqualTo("partner_017");
        var savedAlert = entityManager.find(Alert.class, alert.getId());
        assertThat(savedAlert.getRules()).containsExactly(DetectionRule.VOLUME_SPIKE);
        assertThat(savedAlert.getStatus()).isEqualTo(AlertStatus.OPEN);
        assertThat(savedAlert.getDecidedAt()).isNull();
        assertThat(savedAlert.getEvidence()).containsKey("VOLUME_SPIKE");
        assertThat((Map<?, ?>) savedAlert.getEvidence().get("VOLUME_SPIKE")).isEqualTo(numbers);
        assertThat(entityManager.find(AgentAudit.class, audit.getId()).getCalledAt()).isEqualTo(occurredAt);
        assertThat(entityManager.createNativeQuery("select pg_typeof(evidence)::text from " + SCHEMA + ".alert").getSingleResult()).isEqualTo("jsonb");
        assertThat(entityManager.createNativeQuery("select pg_typeof(rules)::text from " + SCHEMA + ".alert").getSingleResult()).isEqualTo("jsonb");
        assertThat(entityManager.createNativeQuery("select pg_typeof(occurred_at)::text from " + SCHEMA + ".access_log").getSingleResult()).isEqualTo("timestamp without time zone");
    }

    @Test
    void rejectsDuplicateAlertForSameRunAndAccount() {
        var account = new Account("partner_duplicate", "검증 계정", AccountType.PARTNER, "개발");
        entityManager.persist(account);
        entityManager.persist(new Alert("run-duplicate", account, List.of(), Severity.LOW,
                Map.of(), "확인됨", false, RecommendedAction.NONE));
        entityManager.flush();
        assertThatThrownBy(() -> {
            entityManager.persist(new Alert("run-duplicate", account, List.of(), Severity.LOW,
                    Map.of(), "확인됨", false, RecommendedAction.NONE));
            entityManager.flush();
        }).isInstanceOf(RuntimeException.class).hasStackTraceContaining("uk_alert_run_account");
    }
}
