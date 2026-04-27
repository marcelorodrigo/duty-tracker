package com.dutytracker.gateway.incident;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.dutytracker.domain.Incident;
import com.dutytracker.gateway.postgres.entity.IncidentEntity;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = SPRING)
public interface IncidentMapper {

    IncidentEntity toEntity(Incident domain);

    Incident toDomain(IncidentEntity entity);

    List<Incident> toDomainList(List<IncidentEntity> entities);
}
