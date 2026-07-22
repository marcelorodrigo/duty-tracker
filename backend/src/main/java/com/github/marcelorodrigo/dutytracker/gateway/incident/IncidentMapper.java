package com.github.marcelorodrigo.dutytracker.gateway.incident;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.IncidentEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.OnCallPeriodEntity;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = SPRING)
public interface IncidentMapper {

    default IncidentEntity toEntity(Incident domain) {
        var period = new OnCallPeriodEntity(domain.onCallPeriodId(), null, null);
        return new IncidentEntity(
                domain.id(), period, domain.name(), domain.startDateTime(), domain.endDateTime(), null);
    }

    default Incident toDomain(IncidentEntity entity) {
        return new Incident(
                entity.getId(),
                entity.getOnCallPeriod().getId(),
                entity.getName(),
                entity.getStartDateTime(),
                entity.getEndDateTime(),
                entity.getCreatedAt());
    }

    List<Incident> toDomainList(List<IncidentEntity> entities);
}
