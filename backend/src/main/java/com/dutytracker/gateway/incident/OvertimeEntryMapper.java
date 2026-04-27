package com.dutytracker.gateway.incident;

import com.dutytracker.domain.OvertimeEntry;
import com.dutytracker.gateway.postgres.entity.OvertimeEntryEntity;
import org.mapstruct.Mapper;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface OvertimeEntryMapper {

    OvertimeEntryEntity toEntity(OvertimeEntry domain);

    OvertimeEntry toDomain(OvertimeEntryEntity entity);

    List<OvertimeEntry> toDomainList(List<OvertimeEntryEntity> entities);
}
