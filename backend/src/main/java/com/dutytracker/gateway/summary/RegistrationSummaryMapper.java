package com.dutytracker.gateway.summary;

import com.dutytracker.domain.RegistrationSummary;
import com.dutytracker.gateway.postgres.entity.RegistrationSummaryEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RegistrationSummaryMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    RegistrationSummaryEntity toEntity(RegistrationSummary domain);

    RegistrationSummary toDomain(RegistrationSummaryEntity entity);

    List<RegistrationSummary> toDomainList(List<RegistrationSummaryEntity> entities);
}
