package com.github.marcelorodrigo.dutytracker.gateway.compensation;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.usecase.response.compensation.CompensationRateResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = SPRING)
public interface CompensationRateResponseMapper {

    CompensationRateResponse toResponse(CompensationRate domain);

    CompensationRate toDomain(CompensationRateResponse response);
}
