package com.github.marcelorodrigo.dutytracker.gateway.incident;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.IncidentEntity;
import java.util.List;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = SPRING)
public interface IncidentMapper {

    @Mapping(target = "onCallPeriod.id", source = "onCallPeriodId")
    @Mapping(target = "createdAt", ignore = true)
    IncidentEntity toEntity(Incident domain);

    @AfterMapping
    default void afterToEntity(Incident domain, @MappingTarget IncidentEntity entity) {
        if (domain.onCallPeriodId() == null) {
            entity.setOnCallPeriod(null);
        }
    }

    @Mapping(target = "onCallPeriodId", source = "onCallPeriod.id")
    Incident toDomain(IncidentEntity entity);

    List<Incident> toDomainList(List<IncidentEntity> entities);
}
