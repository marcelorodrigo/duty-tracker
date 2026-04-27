package com.dutytracker.gateway.profile;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.dutytracker.domain.EngineerProfile;
import com.dutytracker.gateway.postgres.entity.EngineerProfileEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EngineerProfileMapper {

    @Mapping(target = "createdAt", ignore = true)
    EngineerProfileEntity toEntity(EngineerProfile domain);

    EngineerProfile toDomain(EngineerProfileEntity entity);
}
