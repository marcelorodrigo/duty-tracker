package com.dutytracker.gateway.incident;

import com.dutytracker.domain.CompensationRate;
import com.dutytracker.domain.Incident;
import com.dutytracker.gateway.postgres.entity.CompensationRateEntity;
import com.dutytracker.gateway.postgres.entity.IncidentEntity;
import org.mapstruct.Mapper;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface IncidentMapper {

    IncidentEntity toEntity(Incident domain);

    Incident toDomain(IncidentEntity entity);

    List<Incident> toDomainList(List<IncidentEntity> entities);
}
