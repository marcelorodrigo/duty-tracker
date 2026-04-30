package com.dutytracker.gateway.compensation;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.dutytracker.domain.CompensationRate;
import com.dutytracker.usecase.response.compensation.CompensationRateResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = SPRING)
public interface CompensationRateResponseMapper {

    CompensationRateResponse toResponse(CompensationRate domain);

    CompensationRate toDomain(CompensationRateResponse response);
}
