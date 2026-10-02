package com.shinhan.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"app.analysis-date=2026-10-09", "spring.jpa.hibernate.ddl-auto=none"})
class SampleApiTests {
    private final HttpClient client = HttpClient.newHttpClient();
    private final JsonMapper mapper = JsonMapper.builder().build();

    @Autowired
    private Environment environment;
    @Autowired
    private Clock clock;

    @Test
    void sampleWorkflowMatchesContractsAndUsesConfiguredAnalysisDate() throws Exception {
        var empty = request("GET", "/api/alerts");
        assertThat(empty.statusCode()).isEqualTo(200);
        var emptyJson = mapper.readTree(empty.body());
        assertThat(emptyJson.get("runId").isNull()).isTrue();
        assertThat(emptyJson.get("alerts").isArray()).isTrue();
        assertThat(emptyJson.get("alerts").size()).isZero();
        assertThat(emptyJson.get("analysisDate").asText()).isEqualTo("2026-10-09");

        assertError("/api/alerts/12", 404, "ALERT_NOT_FOUND");
        var first = request("POST", "/api/analysis/run");
        assertThat(first.statusCode()).isEqualTo(200);
        var firstJson = mapper.readTree(first.body());
        assertThat(firstJson.get("runId").asText()).startsWith("run-"
                + LocalDate.now(clock).format(DateTimeFormatter.BASIC_ISO_DATE) + "-");
        assertContract("analysis-run-response.json", firstJson);

        var list = request("GET", "/api/alerts");
        assertThat(list.statusCode()).isEqualTo(200);
        var listJson = mapper.readTree(list.body());
        assertContract("alerts-list-response.json", listJson);
        assertThat(listJson.get("runId")).isEqualTo(firstJson.get("runId"));
        assertThat(listJson.get("alerts").size()).isEqualTo(firstJson.get("candidateCount").asInt());

        var detail = request("GET", "/api/alerts/12");
        assertThat(detail.statusCode()).isEqualTo(200);
        var detailJson = mapper.readTree(detail.body());
        assertContract("alert-detail-response.json", detailJson);
        assertThat(detailJson.get("decidedBy").isNull()).isTrue();
        assertThat(detailJson.get("decidedAt").isNull()).isTrue();
        assertThat(detailJson.get("runId")).isEqualTo(firstJson.get("runId"));
        assertError("/api/alerts/999", 404, "ALERT_NOT_FOUND");
        assertError("/api/alerts/not-a-number", 400, "INVALID_REQUEST");
        assertError("/no-such-api", 404, "NOT_FOUND");

        var second = mapper.readTree(request("POST", "/api/analysis/run").body());
        assertThat(second.get("runId")).isNotEqualTo(firstJson.get("runId"));
        var latest = mapper.readTree(request("GET", "/api/alerts").body());
        assertThat(latest.get("runId")).isEqualTo(second.get("runId"));
        assertThat(clock.getZone().getId()).isEqualTo("Asia/Seoul");
    }

    private HttpResponse<String> request(String method, String path) throws Exception {
        var uri = URI.create("http://localhost:" + environment.getRequiredProperty("local.server.port") + path);
        return client.send(HttpRequest.newBuilder(uri).method(method, HttpRequest.BodyPublishers.noBody()).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private void assertError(String path, int status, String code) throws Exception {
        var response = request("GET", path);
        assertThat(response.statusCode()).isEqualTo(status);
        var json = mapper.readTree(response.body());
        assertThat(json.size()).isEqualTo(2);
        assertThat(json.get("code").asText()).isEqualTo(code);
        assertThat(json.get("message").asText()).isNotBlank();
    }

    private void assertContract(String file, JsonNode actual) throws Exception {
        var expected = (ObjectNode) mapper.readTree(Files.readString(Path.of("../contracts", file)));
        expected.put("runId", actual.get("runId").asText());
        expected.put("analysisDate", "2026-10-09");
        assertThat(actual).isEqualTo(expected);
    }
}
