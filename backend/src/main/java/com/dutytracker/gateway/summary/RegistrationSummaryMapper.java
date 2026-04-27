package com.dutytracker.gateway.summary;

import com.dutytracker.domain.RegistrationSummary;
import com.dutytracker.gateway.postgres.entity.RegistrationSummaryEntity;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RegistrationSummaryMapper {

    RegistrationSummaryEntity toEntity(RegistrationSummary domain);

    RegistrationSummary toDomain(RegistrationSummaryEntity entity);

    List<RegistrationSummary> toDomainList(List<RegistrationSummaryEntity> entities);
}
