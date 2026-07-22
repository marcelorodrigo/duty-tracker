package com.github.marcelorodrigo.dutytracker.gateway.compensation;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.CompensationRateEntity;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = SPRING)
public interface CompensationMapper {

    default CompensationRateEntity toEntity(CompensationRate domain) {
        return new CompensationRateEntity(
                domain.id(),
                domain.rateCategory(),
                domain.overtimeDayType(),
                domain.label(),
                domain.timeFrom(),
                domain.timeTo(),
                domain.percentage());
    }

    CompensationRate toDomain(CompensationRateEntity entity);

    List<CompensationRate> toDomainList(List<CompensationRateEntity> entities);
}
