package com.github.marcelorodrigo.dutytracker.gateway.oncall;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.marcelorodrigo.dutytracker.domain.Holiday;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.HolidayEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.OnCallPeriodEntity;
import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class HolidayMapperTest {

    private static final LocalDate CHRISTMAS = LocalDate.of(2026, Month.DECEMBER, 25);

    private final HolidayMapper mapper = Mappers.getMapper(HolidayMapper.class);

    @Test
    @DisplayName("should map a holiday to an entity with its period identity")
    void shouldMapAHolidayToAnEntityWithItsPeriodIdentity() {
        // given
        var domain = new Holiday(9L, 7L, CHRISTMAS, "Christmas");

        // when
        var entity = mapper.toEntity(domain);

        // then
        assertThat(entity.getId()).isEqualTo(domain.id());
        assertThat(entity.getOnCallPeriod().getId()).isEqualTo(domain.onCallPeriodId());
        assertThat(entity.getDate()).isEqualTo(domain.date());
        assertThat(entity.getName()).isEqualTo(domain.name());
    }

    @Test
    @DisplayName("should map every holiday entity field to the domain")
    void shouldMapEveryHolidayEntityFieldToTheDomain() {
        // given
        var entity = new HolidayEntity(9L, new OnCallPeriodEntity(7L, null, null), CHRISTMAS, "Christmas");

        // when
        var domain = mapper.toDomain(entity);

        // then
        assertThat(domain).isEqualTo(new Holiday(9L, 7L, CHRISTMAS, "Christmas"));
    }
}
