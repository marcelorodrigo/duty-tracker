package com.dutytracker.presentation.api;

import com.dutytracker.application.usecase.incident.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/v1/incidents")
public class IncidentController {

    private final LogIncidentUseCase logIncident;
    private final UpdateIncidentUseCase updateIncident;
    private final DeleteIncidentUseCase deleteIncident;
    private final ListIncidentsUseCase listIncidents;
    private final CalculateOvertimeEntriesUseCase calculateOvertime;

    public IncidentController(LogIncidentUseCase logIncident,
                               UpdateIncidentUseCase updateIncident,
                               DeleteIncidentUseCase deleteIncident,
                               ListIncidentsUseCase listIncidents,
                               CalculateOvertimeEntriesUseCase calculateOvertime) {
        this.logIncident = logIncident;
        this.updateIncident = updateIncident;
        this.deleteIncident = deleteIncident;
        this.listIncidents = listIncidents;
        this.calculateOvertime = calculateOvertime;
    }

    @PostMapping
    public ResponseEntity<IncidentResponse> log(@RequestBody LogIncidentRequest request) {
        var response = logIncident.execute(request);
        return ResponseEntity.created(URI.create("/api/v1/incidents/" + response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<IncidentListResponse> list(
            @RequestParam(required = false) Long onCallPeriodId) {
        return ResponseEntity.ok(listIncidents.execute(new ListIncidentsRequest(onCallPeriodId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentResponse> get(@PathVariable Long id) {
        var all = listIncidents.execute(new ListIncidentsRequest(null));
        return all.incidents().stream()
                .filter(i -> i.id().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncidentResponse> update(@PathVariable Long id,
                                                    @RequestBody UpdateIncidentBody body) {
        return ResponseEntity.ok(updateIncident.execute(
                new UpdateIncidentRequest(id, body.date(), body.startTime(), body.endTime())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteIncident.execute(new DeleteIncidentRequest(id));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/calculate")
    public ResponseEntity<OvertimeEntriesResponse> calculate(@PathVariable Long id) {
        return ResponseEntity.ok(calculateOvertime.execute(new CalculateOvertimeEntriesRequest(id)));
    }

    record UpdateIncidentBody(LocalDate date, LocalTime startTime, LocalTime endTime) {}
}
