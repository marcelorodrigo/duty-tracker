package com.github.marcelorodrigo.dutytracker.gateway.compensation;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.CompensationRateEntity;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = SPRING)
public interface CompensationMapper {

    CompensationRateEntity toEntity(CompensationRate domain);

    CompensationRate toDomain(CompensationRateEntity entity);

    List<CompensationRate> toDomainList(List<CompensationRateEntity> entities);
}
