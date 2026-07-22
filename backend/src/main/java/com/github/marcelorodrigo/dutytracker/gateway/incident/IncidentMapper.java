package com.github.marcelorodrigo.dutytracker.gateway.incident;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.IncidentEntity;
import com.github.marcelorodrigo.dutytracker.usecase.mapper.StrictMapperConfig;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = StrictMapperConfig.class)
public interface IncidentMapper {

    @Mapping(target = "onCallPeriod.id", source = "onCallPeriodId")
    @Mapping(target = "createdAt", ignore = true)
    IncidentEntity toEntity(Incident domain);

    @Mapping(target = "onCallPeriodId", source = "onCallPeriod.id")
    Incident toDomain(IncidentEntity entity);

    List<Incident> toDomainList(List<IncidentEntity> entities);
}
