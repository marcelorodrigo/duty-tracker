package com.github.marcelorodrigo.dutytracker.infrastructure.config;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class ObservabilityIntegrationTest {

    private static final String DATABASE_PASSWORD = UUID.randomUUID().toString();
    private static final String DATABASE_USERNAME = "observability-test-user";

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18-alpine")
            .withDatabaseName("observability")
            .withUsername(DATABASE_USERNAME)
            .withPassword(DATABASE_PASSWORD);

    @Autowired
    private RestTestClient restClient;

    @Autowired
    private Environment environment;

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Test
    @DisplayName("should expose only health and Prometheus management endpoints")
    void shouldExposeOnlyHealthAndPrometheusManagementEndpoints() {
        // given
        var healthPath = "/actuator/health";
        var prometheusPath = "/actuator/prometheus";

        // when
        var healthResponse = restClient.get().uri(healthPath).exchange();
        var prometheusResponse = restClient.get().uri(prometheusPath).exchange();
        var metricsResponse = restClient.get().uri("/actuator/metrics").exchange();
        var environmentResponse = restClient.get().uri("/actuator/env").exchange();

        // then
        healthResponse.expectStatus().isOk();
        prometheusResponse.expectStatus().isOk();
        metricsResponse.expectStatus().isNotFound();
        environmentResponse.expectStatus().isNotFound();
    }

    @Test
    @DisplayName("should return health status without sensitive details")
    void shouldReturnHealthStatusWithoutSensitiveDetails() {
        // given
        var healthPath = "/actuator/health";

        // when
        var response = restClient
                .get()
                .uri(healthPath)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .returnResult();
        var body = new String(response.getResponseBody(), UTF_8);

        // then
        assertThat(body)
                .contains("\"status\":\"UP\"")
                .doesNotContain("\"components\"", "\"details\"")
                .doesNotContain(DATABASE_USERNAME, DATABASE_PASSWORD, POSTGRES.getJdbcUrl());
    }

    @Test
    @DisplayName("should expose liveness and database-aware readiness probes")
    void shouldExposeLivenessAndDatabaseAwareReadinessProbes() {
        // given
        var expectedBody = "{\"status\":\"UP\"}";

        // when
        var livenessResponse = restClient.get().uri("/actuator/health/liveness").exchange();
        var readinessResponse =
                restClient.get().uri("/actuator/health/readiness").exchange();

        // then
        livenessResponse.expectStatus().isOk().expectBody(String.class).isEqualTo(expectedBody);
        readinessResponse.expectStatus().isOk().expectBody(String.class).isEqualTo(expectedBody);
        assertThat(environment.getProperty("management.endpoint.health.group.readiness.include"))
                .isEqualTo("readinessState,db");
    }

    @Test
    @DisplayName("should export actionable service metrics without database credentials")
    void shouldExportActionableServiceMetricsWithoutDatabaseCredentials() {
        // given
        restClient.get().uri("/api/v1/incidents").exchange().expectStatus().isOk();

        // when
        var response = restClient
                .get()
                .uri("/actuator/prometheus")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .returnResult();
        var body = new String(response.getResponseBody(), UTF_8);

        // then
        assertThat(body)
                .contains("jvm_memory_used_bytes")
                .contains("process_uptime_seconds")
                .contains("http_server_requests")
                .contains("jdbc_connections_active")
                .contains("hikaricp_connections_active")
                .contains("application=\"duty-tracker-backend\"")
                .doesNotContain(DATABASE_USERNAME, DATABASE_PASSWORD, POSTGRES.getJdbcUrl());
    }
}
