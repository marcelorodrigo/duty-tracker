package com.github.marcelorodrigo.dutytracker.gateway.oncall;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.OnCallPeriodEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OnCallPeriodMapper {

    @Mapping(target = "createdAt", ignore = true)
    OnCallPeriodEntity toEntity(OnCallPeriod domain);

    OnCallPeriod toDomain(OnCallPeriodEntity entity);

    List<OnCallPeriod> toDomainList(List<OnCallPeriodEntity> entities);
}
