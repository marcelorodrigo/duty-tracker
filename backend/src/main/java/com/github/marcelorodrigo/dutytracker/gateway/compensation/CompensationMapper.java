package com.github.marcelorodrigo.dutytracker.gateway.compensation;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.CompensationRateEntity;
import com.github.marcelorodrigo.dutytracker.usecase.mapper.StrictMapperConfig;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(config = StrictMapperConfig.class)
public interface CompensationMapper {

    CompensationRateEntity toEntity(CompensationRate domain);

    CompensationRate toDomain(CompensationRateEntity entity);

    List<CompensationRate> toDomainList(List<CompensationRateEntity> entities);
}
