package com.github.marcelorodrigo.dutytracker.gateway.postgres.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JpaEntityTest {

    private static final List<Class<? extends JpaEntity>> ENTITY_TYPES = List.of(
            EngineerProfileEntity.class,
            CompensationRateEntity.class,
            OnCallPeriodEntity.class,
            HolidayEntity.class,
            IncidentEntity.class);

    @Test
    @DisplayName("should not expose bean setters for entity identity or state")
    void shouldNotExposeBeanSettersForEntityIdentityOrState() {
        // when / then
        ENTITY_TYPES.forEach(entityType -> assertThat(entityType.getMethods())
                .as("public methods on %s", entityType.getSimpleName())
                .noneMatch(method -> method.getName().startsWith("set")));
    }

    @Test
    @DisplayName("should compare entities only by matching non-null persisted identity and type")
    void shouldCompareEntitiesOnlyByMatchingNonNullPersistedIdentityAndType() {
        // given
        var first = compensationRate(1L, "First");
        var sameIdentity = compensationRate(1L, "Changed");
        var differentIdentity = compensationRate(2L, "First");
        var differentType = new OnCallPeriodEntity(1L, null, null);

        // when / then
        assertThat(first).isEqualTo(sameIdentity).hasSameHashCodeAs(sameIdentity);
        assertThat(first).isNotEqualTo(differentIdentity).isNotEqualTo(differentType);
    }

    @Test
    @DisplayName("should keep distinct transient entities separate in hash-based collections")
    void shouldKeepDistinctTransientEntitiesSeparateInHashBasedCollections() {
        // given
        var first = compensationRate(null, "First");
        var second = compensationRate(null, "First");

        // when
        var entities = new HashSet<>(List.of(first, second));

        // then
        assertThat(first).isNotEqualTo(second);
        assertThat(entities).containsExactlyInAnyOrder(first, second);
    }

    private CompensationRateEntity compensationRate(Long id, String label) {
        return new CompensationRateEntity(
                id,
                RateCategory.OVERTIME_ALLOWANCE,
                OvertimeDayType.WEEKDAY,
                label,
                LocalTime.MIDNIGHT,
                LocalTime.of(1, 0),
                BigDecimal.ONE);
    }
}
