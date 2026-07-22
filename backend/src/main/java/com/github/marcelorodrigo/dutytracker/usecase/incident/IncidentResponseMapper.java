package com.github.marcelorodrigo.dutytracker.usecase.incident;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.LogIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.IncidentResponse;
import java.time.LocalDateTime;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IncidentResponseMapper {
    IncidentResponse toResponse(Incident incident);

    default Incident toDomain(LogIncidentRequest request) {
        return Incident.create(
                request.onCallPeriodId(),
                request.name(),
                request.startDateTime(),
                request.endDateTime(),
                LocalDateTime.now());
    }
}
