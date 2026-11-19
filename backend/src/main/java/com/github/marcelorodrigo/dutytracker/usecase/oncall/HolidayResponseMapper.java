package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.github.marcelorodrigo.dutytracker.domain.Holiday;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.HolidayResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = SPRING)
public interface HolidayResponseMapper {

    HolidayResponse toResponse(Holiday holiday);
}
