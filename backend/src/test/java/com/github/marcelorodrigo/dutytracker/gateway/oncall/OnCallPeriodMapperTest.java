package com.github.marcelorodrigo.dutytracker.gateway.oncall;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OnCallPeriodMapperTest {

    private final OnCallPeriodMapper mapper = new OnCallPeriodMapperImpl();

    @Test
    @DisplayName("should ignore createdAt when mapping domain to entity")
    void shouldIgnoreCreatedAtWhenMappingDomainToEntity() {
        // given
        var domain = new OnCallPeriod(
                1L,
                LocalDateTime.of(2024, 1, 1, 9, 0),
                LocalDateTime.of(2024, 1, 1, 17, 0),
                LocalDateTime.of(2024, 1, 1, 10, 0));

        // when
        var entity = mapper.toEntity(domain);

        // then
        assertThat(entity.getCreatedAt()).isNull();
    }
}
