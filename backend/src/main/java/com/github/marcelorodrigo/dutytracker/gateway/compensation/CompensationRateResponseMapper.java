package com.github.marcelorodrigo.dutytracker.gateway.compensation;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.usecase.mapper.StrictMapperConfig;
import com.github.marcelorodrigo.dutytracker.usecase.response.compensation.CompensationRateResponse;
import org.mapstruct.Mapper;

@Mapper(config = StrictMapperConfig.class)
public interface CompensationRateResponseMapper {

    CompensationRateResponse toResponse(CompensationRate domain);
}
