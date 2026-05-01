package com.github.marcelorodrigo.dutytracker.usecase.incident;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.LogIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.IncidentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IncidentResponseMapper {
    IncidentResponse toResponse(Incident incident);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    Incident toDomain(LogIncidentRequest request);
}
