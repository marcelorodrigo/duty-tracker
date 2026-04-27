package com.dutytracker.gateway.incident;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.dutytracker.domain.OvertimeEntry;
import com.dutytracker.gateway.postgres.entity.OvertimeEntryEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = SPRING)
public interface OvertimeEntryMapper {

    @Mapping(target = "incident.id", source = "incidentId")
    OvertimeEntryEntity toEntity(OvertimeEntry domain);

    @Mapping(target = "incidentId", source = "incident.id")
    OvertimeEntry toDomain(OvertimeEntryEntity entity);

    List<OvertimeEntry> toDomainList(List<OvertimeEntryEntity> entities);
}
