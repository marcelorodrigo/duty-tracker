package com.github.marcelorodrigo.dutytracker.gateway.oncall;

import com.github.marcelorodrigo.dutytracker.domain.Holiday;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.HolidayEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.OnCallPeriodEntity;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HolidayMapper {

    default HolidayEntity toEntity(Holiday domain) {
        var period = new OnCallPeriodEntity(domain.onCallPeriodId(), null, null);
        return new HolidayEntity(domain.id(), period, domain.date(), domain.name());
    }

    default Holiday toDomain(HolidayEntity entity) {
        return new Holiday(entity.getId(), entity.getOnCallPeriod().getId(), entity.getDate(), entity.getName());
    }

    List<Holiday> toDomainList(List<HolidayEntity> entities);
}
