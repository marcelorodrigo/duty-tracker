package com.github.marcelorodrigo.dutytracker.gateway.controllers.incident;

import com.github.marcelorodrigo.dutytracker.gateway.api.IncidentsApi;
import com.github.marcelorodrigo.dutytracker.usecase.incident.CalculateOvertimeEntriesUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.incident.DeleteIncidentUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.incident.GetIncidentUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.incident.ListIncidentsUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.incident.LogIncidentUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.incident.UpdateIncidentUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.CalculateOvertimeEntriesRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.DeleteIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.GetIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.ListIncidentsRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.LogIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.UpdateIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.IncidentListResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.IncidentResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.OvertimeEntriesResponse;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class IncidentController implements IncidentsApi {

    private static final String INCIDENT_ID = "incidentId";
    private final LogIncidentUseCase logIncident;
    private final UpdateIncidentUseCase updateIncident;
    private final DeleteIncidentUseCase deleteIncident;
    private final ListIncidentsUseCase listIncidents;
    private final GetIncidentUseCase getIncident;
    private final CalculateOvertimeEntriesUseCase calculateOvertime;

    @Override
    public ResponseEntity<IncidentResponse> logIncident(LogIncidentRequest logIncidentRequest) {
        var response = logIncident.execute(logIncidentRequest);
        log.atInfo()
                .addKeyValue(INCIDENT_ID, response.id())
                .addKeyValue("onCallPeriodId", logIncidentRequest.onCallPeriodId())
                .addKeyValue("incidentName", logIncidentRequest.name())
                .addKeyValue("startDateTime", logIncidentRequest.startDateTime())
                .addKeyValue("endDateTime", logIncidentRequest.endDateTime())
                .log("Incident logged");
        return ResponseEntity.created(URI.create("/api/v1/incidents/" + response.id()))
                .body(response);
    }

    @Override
    public ResponseEntity<IncidentListResponse> listIncidents(Long onCallPeriodId) {
        return ResponseEntity.ok(listIncidents.execute(new ListIncidentsRequest(onCallPeriodId)));
    }

    @Override
    public ResponseEntity<IncidentResponse> getIncident(Long id) {
        return ResponseEntity.ok(getIncident.execute(new GetIncidentRequest(id)));
    }

    @Override
    public ResponseEntity<IncidentResponse> updateIncident(Long id, UpdateIncidentBody updateIncidentBody) {
        var response = updateIncident.execute(new UpdateIncidentRequest(
                id, updateIncidentBody.name(), updateIncidentBody.startDateTime(), updateIncidentBody.endDateTime()));
        log.atInfo().addKeyValue(INCIDENT_ID, response.id()).log("Incident updated");
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteIncident(Long id) {
        deleteIncident.execute(new DeleteIncidentRequest(id));
        log.atInfo().addKeyValue(INCIDENT_ID, id).log("Incident deleted");
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<OvertimeEntriesResponse> calculateIncidentOvertime(Long id) {
        log.atInfo().addKeyValue(INCIDENT_ID, id).log("Incident overtime entries calculation requested");
        return ResponseEntity.ok(calculateOvertime.execute(new CalculateOvertimeEntriesRequest(id)));
    }
}
