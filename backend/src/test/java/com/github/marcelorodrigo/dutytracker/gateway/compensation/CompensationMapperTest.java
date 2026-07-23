package com.github.marcelorodrigo.dutytracker.gateway.compensation;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import java.math.BigDecimal;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class CompensationMapperTest {

    private final CompensationMapper mapper = Mappers.getMapper(CompensationMapper.class);

    @Test
    @DisplayName("should map every compensation rate field to its entity")
    void shouldMapEveryCompensationRateFieldToItsEntity() {
        // given
        var domain = new CompensationRate(
                42L,
                RateCategory.OVERTIME_ALLOWANCE,
                OvertimeDayType.SATURDAY,
                "Saturday evening",
                LocalTime.of(18, 0),
                LocalTime.of(19, 0),
                new BigDecimal("50.0000"));

        // when
        var entity = mapper.toEntity(domain);

        // then
        assertThat(entity.getId()).isEqualTo(domain.id());
        assertThat(entity.getRateCategory()).isEqualTo(domain.rateCategory());
        assertThat(entity.getOvertimeDayType()).isEqualTo(domain.overtimeDayType());
        assertThat(entity.getLabel()).isEqualTo(domain.label());
        assertThat(entity.getTimeFrom()).isEqualTo(domain.timeFrom());
        assertThat(entity.getTimeTo()).isEqualTo(domain.timeTo());
        assertThat(entity.getPercentage()).isEqualByComparingTo(domain.percentage());
    }
}
