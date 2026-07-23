package com.github.marcelorodrigo.dutytracker.gateway.postgres;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.CompensationRateEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.EngineerProfileEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.HolidayEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.IncidentEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.OnCallPeriodEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.CompensationRateJpaRepository;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.EngineerProfileJpaRepository;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.HolidayJpaRepository;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.IncidentJpaRepository;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.OnCallPeriodJpaRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.EnumSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;

class PostgreSqlRepositoryIntegrationTest extends PostgreSqlRepositoryTestSupport {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 7, 22, 12, 0);

    @Autowired
    private CompensationRateJpaRepository compensationRateRepository;

    @Autowired
    private EngineerProfileJpaRepository engineerProfileRepository;

    @Autowired
    private HolidayJpaRepository holidayRepository;

    @Autowired
    private IncidentJpaRepository incidentRepository;

    @Autowired
    private OnCallPeriodJpaRepository onCallPeriodRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    @DisplayName("should find an overlapping on-call period")
    void shouldFindAnOverlappingOnCallPeriod() {
        // given
        persistPeriod(LocalDateTime.of(2026, 7, 1, 9, 0), LocalDateTime.of(2026, 7, 1, 17, 0));

        // when
        var exists = onCallPeriodRepository.existsOverlapping(
                LocalDateTime.of(2026, 7, 1, 12, 0), LocalDateTime.of(2026, 7, 1, 18, 0), null);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("should not treat adjacent on-call periods as overlapping")
    void shouldNotTreatAdjacentOnCallPeriodsAsOverlapping() {
        // given
        persistPeriod(LocalDateTime.of(2026, 7, 1, 9, 0), LocalDateTime.of(2026, 7, 1, 17, 0));

        // when
        var exists = onCallPeriodRepository.existsOverlapping(
                LocalDateTime.of(2026, 7, 1, 17, 0), LocalDateTime.of(2026, 7, 2, 9, 0), null);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("should exclude an on-call period from its overlap check")
    void shouldExcludeAnOnCallPeriodFromItsOverlapCheck() {
        // given
        var period = persistPeriod(LocalDateTime.of(2026, 7, 1, 9, 0), LocalDateTime.of(2026, 7, 1, 17, 0));

        // when
        var exists = onCallPeriodRepository.existsOverlapping(
                period.getStartDateTime(), period.getEndDateTime(), period.getId());

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("should find an overlapping incident in the same on-call period")
    void shouldFindAnOverlappingIncidentInTheSameOnCallPeriod() {
        // given
        var period = persistPeriod(LocalDateTime.of(2026, 7, 1, 0, 0), LocalDateTime.of(2026, 7, 2, 0, 0));
        persistIncident(period, LocalDateTime.of(2026, 7, 1, 10, 0), LocalDateTime.of(2026, 7, 1, 11, 0));

        // when
        var exists = incidentRepository.existsOverlapping(
                period.getId(), LocalDateTime.of(2026, 7, 1, 10, 30), LocalDateTime.of(2026, 7, 1, 11, 30), null);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("should scope incident overlap checks to an on-call period")
    void shouldScopeIncidentOverlapChecksToAnOnCallPeriod() {
        // given
        var period = persistPeriod(LocalDateTime.of(2026, 7, 1, 0, 0), LocalDateTime.of(2026, 7, 2, 0, 0));
        var otherPeriod = persistPeriod(LocalDateTime.of(2026, 7, 3, 0, 0), LocalDateTime.of(2026, 7, 4, 0, 0));
        persistIncident(period, LocalDateTime.of(2026, 7, 1, 10, 0), LocalDateTime.of(2026, 7, 1, 11, 0));

        // when
        var exists = incidentRepository.existsOverlapping(
                otherPeriod.getId(), LocalDateTime.of(2026, 7, 1, 10, 30), LocalDateTime.of(2026, 7, 1, 11, 30), null);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("should exclude an incident from its overlap check")
    void shouldExcludeAnIncidentFromItsOverlapCheck() {
        // given
        var period = persistPeriod(LocalDateTime.of(2026, 7, 1, 0, 0), LocalDateTime.of(2026, 7, 2, 0, 0));
        var incident =
                persistIncident(period, LocalDateTime.of(2026, 7, 1, 10, 0), LocalDateTime.of(2026, 7, 1, 11, 0));

        // when
        var exists = incidentRepository.existsOverlapping(
                period.getId(), incident.getStartDateTime(), incident.getEndDateTime(), incident.getId());

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("should round-trip working days through the DayOfWeek set converter")
    void shouldRoundTripWorkingDaysThroughTheDayOfWeekSetConverter() {
        // given
        var profile = new EngineerProfileEntity(
                null,
                EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                new BigDecimal("50.00"),
                new BigDecimal("0.06700"),
                new BigDecimal("0.08400"));

        // when
        var saved = engineerProfileRepository.saveAndFlush(profile);
        entityManager.clear();
        var reloaded = engineerProfileRepository.findById(saved.getId());
        var storedValue = jdbcClient
                .sql("SELECT working_days FROM engineer_profile WHERE id = :id")
                .param("id", saved.getId())
                .query(String.class)
                .single();

        // then
        assertThat(storedValue).isEqualTo("MONDAY,WEDNESDAY");
        assertThat(reloaded)
                .isPresent()
                .get()
                .extracting(EngineerProfileEntity::getWorkingDays)
                .isEqualTo(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));
    }

    @Test
    @DisplayName("should enforce compensation rate uniqueness when nullable keys match")
    void shouldEnforceCompensationRateUniquenessWhenNullableKeysMatch() {
        // given
        var duplicate = new CompensationRateEntity(
                null, RateCategory.ONCALL_WEEKDAY_SATURDAY, null, "Duplicate on-call rate", null, null, BigDecimal.ONE);

        // when / then
        assertThatThrownBy(() -> compensationRateRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("uq_compensation_rate");
    }

    @Test
    @DisplayName("should enforce holiday uniqueness within an on-call period")
    void shouldEnforceHolidayUniquenessWithinAnOnCallPeriod() {
        // given
        var period = persistPeriod(LocalDateTime.of(2026, 12, 24, 0, 0), LocalDateTime.of(2026, 12, 27, 0, 0));
        holidayRepository.saveAndFlush(new HolidayEntity(null, period, LocalDate.of(2026, 12, 25), "Christmas"));
        var duplicate = new HolidayEntity(null, period, LocalDate.of(2026, 12, 25), "Christmas Day");

        // when / then
        assertThatThrownBy(() -> holidayRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("holiday_on_call_period_id_date_key");
    }

    @Test
    @DisplayName("should cascade on-call period deletion to holidays and incidents")
    void shouldCascadeOnCallPeriodDeletionToHolidaysAndIncidents() {
        // given
        var period = persistPeriod(LocalDateTime.of(2026, 12, 24, 0, 0), LocalDateTime.of(2026, 12, 27, 0, 0));
        holidayRepository.saveAndFlush(new HolidayEntity(null, period, LocalDate.of(2026, 12, 25), "Christmas"));
        persistIncident(period, LocalDateTime.of(2026, 12, 25, 10, 0), LocalDateTime.of(2026, 12, 25, 11, 0));
        entityManager.clear();

        // when
        onCallPeriodRepository.deleteById(period.getId());
        onCallPeriodRepository.flush();
        entityManager.clear();

        // then
        assertThat(rowCount("holiday", period.getId())).isZero();
        assertThat(rowCount("incident", period.getId())).isZero();
    }

    @Test
    @DisplayName("should compare an entity and its lazy proxy by persisted identity")
    void shouldCompareAnEntityAndItsLazyProxyByPersistedIdentity() {
        // given
        var period = persistPeriod(
                LocalDateTime.of(2027, Month.JANUARY, 1, 9, 0), LocalDateTime.of(2027, Month.JANUARY, 1, 17, 0));
        entityManager.flush();
        entityManager.clear();

        // when
        var proxy = onCallPeriodRepository.getReferenceById(period.getId());

        // then
        assertThat(period).isEqualTo(proxy);
        assertThat(proxy).isEqualTo(period);
        assertThat(period).hasSameHashCodeAs(proxy);
    }

    private OnCallPeriodEntity persistPeriod(LocalDateTime start, LocalDateTime end) {
        var period = new OnCallPeriodEntity(null, start, end, CREATED_AT);
        return onCallPeriodRepository.saveAndFlush(period);
    }

    private IncidentEntity persistIncident(OnCallPeriodEntity period, LocalDateTime start, LocalDateTime end) {
        var incident = new IncidentEntity(null, period, "Production incident", start, end, CREATED_AT);
        return incidentRepository.saveAndFlush(incident);
    }

    private long rowCount(String tableName, Long periodId) {
        return jdbcClient
                .sql("SELECT COUNT(*) FROM " + tableName + " WHERE on_call_period_id = :periodId")
                .param("periodId", periodId)
                .query(Long.class)
                .single();
    }
}
