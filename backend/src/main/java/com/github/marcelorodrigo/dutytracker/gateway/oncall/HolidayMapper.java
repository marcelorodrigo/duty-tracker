package com.github.marcelorodrigo.dutytracker.gateway.oncall;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.github.marcelorodrigo.dutytracker.domain.Holiday;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.HolidayEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.OnCallPeriodEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR, imports = OnCallPeriodEntity.class)
public interface HolidayMapper {

    @Mapping(target = "onCallPeriod", expression = "java(new OnCallPeriodEntity(domain.onCallPeriodId(), null, null))")
    HolidayEntity toEntity(Holiday domain);

    @Mapping(target = "onCallPeriodId", source = "onCallPeriod.id")
    Holiday toDomain(HolidayEntity entity);

    List<Holiday> toDomainList(List<HolidayEntity> entities);
}
