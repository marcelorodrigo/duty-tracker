package com.github.marcelorodrigo.dutytracker.usecase.incident;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.LogIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.IncidentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface IncidentResponseMapper {
    IncidentResponse toResponse(Incident incident);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    Incident toDomain(LogIncidentRequest request);
}
