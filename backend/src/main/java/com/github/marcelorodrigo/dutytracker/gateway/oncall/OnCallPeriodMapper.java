package com.github.marcelorodrigo.dutytracker.gateway.oncall;

import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.OnCallPeriodEntity;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OnCallPeriodMapper {

    OnCallPeriodEntity toEntity(OnCallPeriod domain);

    OnCallPeriod toDomain(OnCallPeriodEntity entity);

    List<OnCallPeriod> toDomainList(List<OnCallPeriodEntity> entities);
}
