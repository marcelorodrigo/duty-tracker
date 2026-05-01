package com.github.marcelorodrigo.dutytracker.gateway.oncall;

import com.github.marcelorodrigo.dutytracker.domain.HolidayOverride;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.HolidayOverrideEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.OnCallPeriodEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = OnCallPeriodEntity.class)
public interface HolidayOverrideMapper {

    @Mapping(target = "onCallPeriod", expression = "java(new OnCallPeriodEntity(domain.onCallPeriodId(), null, null))")
    HolidayOverrideEntity toEntity(HolidayOverride domain);

    @Mapping(target = "onCallPeriodId", source = "onCallPeriod.id")
    HolidayOverride toDomain(HolidayOverrideEntity entity);

    List<HolidayOverride> toDomainList(List<HolidayOverrideEntity> entities);
}
