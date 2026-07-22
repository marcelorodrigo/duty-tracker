package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;

import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayGateway;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.HolidayEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.OnCallPeriodEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.HolidayJpaRepository;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.IncidentJpaRepository;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.OnCallPeriodJpaRepository;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.UpdateHolidaysRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.UpdateOnCallPeriodRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.HolidayResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
class UseCaseTransactionIntegrationTest {

    private static final PostgreSQLContainer POSTGRESQL = new PostgreSQLContainer("postgres:18-alpine");
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 7, 22, 12, 0);

    static {
        POSTGRESQL.start();
    }

    @DynamicPropertySource
    static void configurePostgreSql(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL::getPassword);
    }

    @Autowired
    private UpdateHolidaysUseCase updateHolidaysUseCase;

    @Autowired
    private UpdateOnCallPeriodUseCase updateOnCallPeriodUseCase;

    @Autowired
    private HolidayJpaRepository holidayRepository;

    @Autowired
    private IncidentJpaRepository incidentRepository;

    @Autowired
    private OnCallPeriodJpaRepository onCallPeriodRepository;

    @MockitoSpyBean
    private HolidayGateway holidayGateway;

    @BeforeEach
    void cleanDatabase() {
        holidayRepository.deleteAll();
        incidentRepository.deleteAll();
        onCallPeriodRepository.deleteAll();
    }

    @Test
    @DisplayName("should roll back the period update when holiday cleanup fails")
    void shouldRollBackPeriodUpdateWhenHolidayCleanupFails() {
        // given
        var originalStart = LocalDateTime.of(2026, 7, 1, 0, 0);
        var originalEnd = LocalDateTime.of(2026, 7, 4, 0, 0);
        var period = persistPeriod(originalStart, originalEnd);
        persistHoliday(period, LocalDate.of(2026, 7, 3), "Summer holiday");
        var updatedEnd = LocalDateTime.of(2026, 7, 2, 0, 0);
        doAnswer(invocation -> {
                    invocation.callRealMethod();
                    throw new SimulatedPersistenceFailure();
                })
                .when(holidayGateway)
                .deleteOutOfRange(eq(period.getId()), eq(originalStart.toLocalDate()), eq(updatedEnd.toLocalDate()));
        var request = new UpdateOnCallPeriodRequest(period.getId(), originalStart, updatedEnd);

        // when / then
        assertThatThrownBy(() -> updateOnCallPeriodUseCase.execute(request))
                .isInstanceOf(SimulatedPersistenceFailure.class);
        assertThat(onCallPeriodRepository.findById(period.getId()))
                .isPresent()
                .get()
                .satisfies(persisted -> {
                    assertThat(persisted.getStartDateTime()).isEqualTo(originalStart);
                    assertThat(persisted.getEndDateTime()).isEqualTo(originalEnd);
                });
        assertThat(holidayRepository.findByOnCallPeriodId(period.getId()))
                .extracting(HolidayEntity::getDate, HolidayEntity::getName)
                .containsExactly(tuple(LocalDate.of(2026, 7, 3), "Summer holiday"));
    }

    @Test
    @DisplayName("should roll back holiday deletion when replacement persistence fails")
    void shouldRollBackHolidayDeletionWhenReplacementPersistenceFails() {
        // given
        var period = persistPeriod(LocalDateTime.of(2026, 12, 24, 0, 0), LocalDateTime.of(2026, 12, 27, 0, 0));
        persistHoliday(period, LocalDate.of(2026, 12, 25), "Christmas");
        var replacement = new HolidayResponse(LocalDate.of(2026, 12, 26), "Boxing Day");
        doThrow(new SimulatedPersistenceFailure()).when(holidayGateway).saveAll(anyList());
        var request = new UpdateHolidaysRequest(period.getId(), List.of(replacement));

        // when / then
        assertThatThrownBy(() -> updateHolidaysUseCase.execute(request))
                .isInstanceOf(SimulatedPersistenceFailure.class);
        assertThat(holidayRepository.findByOnCallPeriodId(period.getId()))
                .extracting(HolidayEntity::getDate, HolidayEntity::getName)
                .containsExactly(tuple(LocalDate.of(2026, 12, 25), "Christmas"));
    }

    private OnCallPeriodEntity persistPeriod(LocalDateTime start, LocalDateTime end) {
        var period = new OnCallPeriodEntity(null, start, end);
        period.setCreatedAt(CREATED_AT);
        return onCallPeriodRepository.saveAndFlush(period);
    }

    private void persistHoliday(OnCallPeriodEntity period, LocalDate date, String name) {
        holidayRepository.saveAndFlush(new HolidayEntity(null, period, date, name));
    }

    private static final class SimulatedPersistenceFailure extends RuntimeException {}
}
