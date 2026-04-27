package com.dutytracker.gateway.oncall;

import com.dutytracker.domain.OnCallDayEntry;
import com.dutytracker.gateway.postgres.entity.OnCallDayEntryEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OnCallDayEntryMapper {

    @Mapping(target = "onCallPeriod", expression = "java(new OnCallPeriodEntity(domain.onCallPeriodId(), null, null))")
    OnCallDayEntryEntity toEntity(OnCallDayEntry domain);

    @Mapping(target = "onCallPeriodId", source = "onCallPeriod.id")
    OnCallDayEntry toDomain(OnCallDayEntryEntity entity);

    List<OnCallDayEntry> toDomainList(List<OnCallDayEntryEntity> entities);
}
