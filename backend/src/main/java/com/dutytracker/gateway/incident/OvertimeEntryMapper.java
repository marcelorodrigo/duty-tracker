package com.dutytracker.gateway.incident;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.dutytracker.domain.OvertimeEntry;
import com.dutytracker.gateway.postgres.entity.OvertimeEntryEntity;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = SPRING)
public interface OvertimeEntryMapper {

    OvertimeEntryEntity toEntity(OvertimeEntry domain);

    OvertimeEntry toDomain(OvertimeEntryEntity entity);

    List<OvertimeEntry> toDomainList(List<OvertimeEntryEntity> entities);
}
