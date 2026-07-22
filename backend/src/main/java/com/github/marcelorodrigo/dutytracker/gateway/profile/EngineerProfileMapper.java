package com.github.marcelorodrigo.dutytracker.gateway.profile;

import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.EngineerProfileEntity;
import com.github.marcelorodrigo.dutytracker.usecase.mapper.StrictMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = StrictMapperConfig.class)
public interface EngineerProfileMapper {

    @Mapping(target = "createdAt", ignore = true)
    EngineerProfileEntity toEntity(EngineerProfile domain);

    EngineerProfile toDomain(EngineerProfileEntity entity);
}
