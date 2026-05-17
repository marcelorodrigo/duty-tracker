package com.github.marcelorodrigo.dutytracker.gateway.controllers.incident;

import com.github.marcelorodrigo.dutytracker.usecase.incident.*;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.*;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.*;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.IncidentListResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.IncidentResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.OvertimeEntriesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/incidents")
@Tag(name = "Incidents", description = "Manage incidents and on-call overtime entries")
@RequiredArgsConstructor
@Slf4j
public class IncidentController {

    private final LogIncidentUseCase logIncident;
    private final UpdateIncidentUseCase updateIncident;
    private final DeleteIncidentUseCase deleteIncident;
    private final ListIncidentsUseCase listIncidents;
    private final CalculateOvertimeEntriesUseCase calculateOvertime;

    @PostMapping
    @Operation(summary = "Log a new incident", description = "Create a new incident entry for overtime tracking")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Incident created successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = IncidentResponse.class))),
                @ApiResponse(responseCode = "400", description = "Invalid incident data")
            })
    public ResponseEntity<IncidentResponse> log(@RequestBody LogIncidentRequest request) {
        var response = logIncident.execute(request);
        log.atInfo()
                .addKeyValue("incidentId", response.id())
                .addKeyValue("onCallPeriodId", request.onCallPeriodId())
                .addKeyValue("incidentName", request.name())
                .addKeyValue("startDateTime", request.startDateTime())
                .addKeyValue("endDateTime", request.endDateTime())
                .log("Incident logged");
        return ResponseEntity.created(URI.create("/api/v1/incidents/" + response.id()))
                .body(response);
    }

    @GetMapping
    @Operation(
            summary = "List incidents",
            description = "Retrieve all incidents, optionally filtered by on-call period")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Incidents retrieved successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = IncidentListResponse.class)))
            })
    public ResponseEntity<IncidentListResponse> list(
            @Parameter(description = "On-call period ID to filter incidents (optional)") @RequestParam(required = false)
                    Long onCallPeriodId) {
        return ResponseEntity.ok(listIncidents.execute(new ListIncidentsRequest(onCallPeriodId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get incident by ID", description = "Retrieve details of a specific incident")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Incident retrieved successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = IncidentResponse.class))),
                @ApiResponse(responseCode = "404", description = "Incident not found")
            })
    public ResponseEntity<IncidentResponse> get(@Parameter(description = "Incident ID") @PathVariable Long id) {
        var all = listIncidents.execute(new ListIncidentsRequest(null));
        return all.incidents().stream()
                .filter(i -> i.id().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update incident", description = "Update the date and time of an incident")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Incident updated successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = IncidentResponse.class))),
                @ApiResponse(responseCode = "400", description = "Invalid incident data"),
                @ApiResponse(responseCode = "404", description = "Incident not found")
            })
    public ResponseEntity<IncidentResponse> update(
            @Parameter(description = "Incident ID") @PathVariable Long id, @RequestBody UpdateIncidentBody body) {
        log.atInfo()
                .addKeyValue("incidentId", id)
                .addKeyValue("incidentName", body.name())
                .addKeyValue("startDateTime", body.startDateTime())
                .addKeyValue("endDateTime", body.endDateTime())
                .log("Incident updated");
        return ResponseEntity.ok(updateIncident.execute(
                new UpdateIncidentRequest(id, body.name(), body.startDateTime(), body.endDateTime())));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete incident", description = "Remove an incident from the system")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Incident deleted successfully"),
                @ApiResponse(responseCode = "404", description = "Incident not found")
            })
    public ResponseEntity<Void> delete(@Parameter(description = "Incident ID") @PathVariable Long id) {
        log.atInfo().addKeyValue("incidentId", id).log("Incident deleted");
        deleteIncident.execute(new DeleteIncidentRequest(id));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/calculate")
    @Operation(
            summary = "Calculate overtime entries",
            description = "Generate overtime entries for a specific incident")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Overtime entries calculated successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = OvertimeEntriesResponse.class))),
                @ApiResponse(responseCode = "404", description = "Incident not found")
            })
    public ResponseEntity<OvertimeEntriesResponse> calculate(
            @Parameter(description = "Incident ID") @PathVariable Long id) {
        log.atInfo().addKeyValue("incidentId", id).log("Incident overtime entries calculation requested");
        return ResponseEntity.ok(calculateOvertime.execute(new CalculateOvertimeEntriesRequest(id)));
    }

    record UpdateIncidentBody(String name, LocalDateTime startDateTime, LocalDateTime endDateTime) {}
}
