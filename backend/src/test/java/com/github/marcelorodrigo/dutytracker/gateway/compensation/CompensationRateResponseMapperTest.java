package com.github.marcelorodrigo.dutytracker.gateway.compensation;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.usecase.response.compensation.CompensationRateResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CompensationRateResponseMapperTest {

    @Test
    @DisplayName("should expose only the production-used mapping direction")
    void shouldExposeOnlyTheProductionUsedMappingDirection() {
        // given
        var mapperType = CompensationRateResponseMapper.class;

        // when
        var mappingMethods = mapperType.getDeclaredMethods();

        // then
        assertThat(mappingMethods).singleElement().satisfies(method -> {
            assertThat(method.getName()).isEqualTo("toResponse");
            assertThat(method.getParameterTypes()).containsExactly(CompensationRate.class);
            assertThat(method.getReturnType()).isEqualTo(CompensationRateResponse.class);
        });
    }
}
