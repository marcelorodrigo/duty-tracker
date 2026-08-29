package com.github.marcelorodrigo.dutytracker.gateway.postgres.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.CompensationRateEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.IncidentEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.OnCallPeriodEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.CompensationRateJpaRepository;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.IncidentJpaRepository;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.OnCallPeriodJpaRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ExtendWith(DockerAvailableCondition.class)
@Transactional
@DisplayName("Pagination integration tests against PostgreSQL")
class PaginationTest {

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));

    private static final int DATASET_SIZE = 250;

    @BeforeAll
    static void startContainer() {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    private final OnCallPeriodJpaRepository onCallPeriodRepository;
    private final IncidentJpaRepository incidentRepository;
    private final CompensationRateJpaRepository compensationRateRepository;

    PaginationTest(
            OnCallPeriodJpaRepository onCallPeriodRepository,
            IncidentJpaRepository incidentRepository,
            CompensationRateJpaRepository compensationRateRepository) {
        this.onCallPeriodRepository = onCallPeriodRepository;
        this.incidentRepository = incidentRepository;
        this.compensationRateRepository = compensationRateRepository;
    }

    @BeforeEach
    void cleanDatabase() {
        incidentRepository.deleteAll();
        onCallPeriodRepository.deleteAll();
        compensationRateRepository.deleteAll();
    }

    @Test
    @DisplayName("should page on-call periods with an accurate total and ordering")
    void shouldPageOnCallPeriods() {
        // given
        var base = LocalDateTime.of(2025, 1, 1, 0, 0);
        var periods = new ArrayList<OnCallPeriodEntity>();
        for (int i = 0; i < DATASET_SIZE; i++) {
            var period = new OnCallPeriodEntity(
                    null, base.plusDays(i), base.plusDays(i).plusHours(8));
            period.setCreatedAt(base.plusDays(i));
            periods.add(period);
        }
        onCallPeriodRepository.saveAll(periods);

        // when
        Page<OnCallPeriodEntity> page =
                onCallPeriodRepository.findAll(PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "startDateTime")));

        // then
        assertThat(page.getContent()).hasSize(20);
        assertThat(page.getTotalElements()).isEqualTo(DATASET_SIZE);
        assertThat(page.getTotalPages()).isEqualTo(13);
        assertThat(page.getContent().get(0).getStartDateTime())
                .isAfterOrEqualTo(page.getContent().get(19).getStartDateTime());
    }

    @Test
    @DisplayName("should page incidents filtered by on-call period with an accurate total")
    void shouldPageIncidentsByPeriod() {
        // given
        var period = onCallPeriodRepository.save(
                new OnCallPeriodEntity(null, LocalDateTime.of(2025, 1, 1, 0, 0), LocalDateTime.of(2025, 1, 1, 8, 0)));
        var base = LocalDateTime.of(2025, 2, 1, 0, 0);
        var incidents = new ArrayList<IncidentEntity>();
        for (int i = 0; i < DATASET_SIZE; i++) {
            incidents.add(new IncidentEntity(
                    null,
                    period,
                    "Incident " + i,
                    base.plusHours(i),
                    base.plusHours(i).plusMinutes(30),
                    base.plusHours(i)));
        }
        incidentRepository.saveAll(incidents);

        // when
        Page<IncidentEntity> page = incidentRepository.findByOnCallPeriodId(
                period.getId(), PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "startDateTime")));

        // then
        assertThat(page.getContent()).hasSize(20);
        assertThat(page.getTotalElements()).isEqualTo(DATASET_SIZE);
        assertThat(page.getTotalPages()).isEqualTo(13);
    }

    @Test
    @DisplayName("should page all incidents with an accurate total and page number")
    void shouldPageAllIncidents() {
        // given
        var period = onCallPeriodRepository.save(
                new OnCallPeriodEntity(null, LocalDateTime.of(2025, 1, 1, 0, 0), LocalDateTime.of(2025, 1, 1, 8, 0)));
        var base = LocalDateTime.of(2025, 3, 1, 0, 0);
        var incidents = new ArrayList<IncidentEntity>();
        for (int i = 0; i < DATASET_SIZE; i++) {
            incidents.add(new IncidentEntity(
                    null,
                    period,
                    "Incident " + i,
                    base.plusHours(i),
                    base.plusHours(i).plusMinutes(30),
                    base.plusHours(i)));
        }
        incidentRepository.saveAll(incidents);

        // when
        Page<IncidentEntity> page = incidentRepository.findAll(PageRequest.of(1, 20));

        // then
        assertThat(page.getContent()).hasSize(20);
        assertThat(page.getTotalElements()).isEqualTo(DATASET_SIZE);
        assertThat(page.getNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("should page compensation rates with an accurate total")
    void shouldPageCompensationRates() {
        // given
        var rates = new ArrayList<CompensationRateEntity>();
        for (int i = 0; i < DATASET_SIZE; i++) {
            rates.add(new CompensationRateEntity(
                    null,
                    RateCategory.OVERTIME_ALLOWANCE,
                    OvertimeDayType.WEEKDAY,
                    "Rate " + i,
                    LocalTime.of(8, 0).plusMinutes(i),
                    LocalTime.of(17, 0).plusMinutes(i),
                    new BigDecimal("25.00")));
        }
        compensationRateRepository.saveAll(rates);

        // when
        Page<CompensationRateEntity> page =
                compensationRateRepository.findAll(PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id")));

        // then
        assertThat(page.getContent()).hasSize(20);
        assertThat(page.getTotalElements()).isEqualTo(DATASET_SIZE);
        assertThat(page.getTotalPages()).isEqualTo(13);
    }
}
