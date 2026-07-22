package com.github.marcelorodrigo.dutytracker.gateway.compensation;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.domain.Percentage;
import com.github.marcelorodrigo.dutytracker.usecase.response.compensation.CompensationRateResponse;
import java.math.BigDecimal;
import org.mapstruct.Mapper;

@Mapper(componentModel = SPRING)
public interface CompensationRateResponseMapper {

    CompensationRateResponse toResponse(CompensationRate domain);

    CompensationRate toDomain(CompensationRateResponse response);

    default BigDecimal map(Percentage percentage) {
        return percentage == null ? null : percentage.value();
    }

    default Percentage map(BigDecimal value) {
        return value == null ? null : Percentage.of(value);
    }
}
