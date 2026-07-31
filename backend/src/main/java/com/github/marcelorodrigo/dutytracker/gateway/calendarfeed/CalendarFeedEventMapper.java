package com.github.marcelorodrigo.dutytracker.gateway.calendarfeed;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.github.marcelorodrigo.dutytracker.usecase.calendarfeed.CalendarFeedEventResponseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CalendarFeedEventMapper extends CalendarFeedEventResponseMapper {}
