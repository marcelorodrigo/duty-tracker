package com.github.marcelorodrigo.dutytracker.gateway.incident;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IncidentGateway {
    Incident save(Incident incident);

    Optional<Incident> findById(Long id);

    List<Incident> findByOnCallPeriodId(Long onCallPeriodId);

    List<Incident> findAll();

    void deleteById(Long id);

    boolean existsOverlapping(Long onCallPeriodId, LocalDateTime start, LocalDateTime end, Long excludeId);
}
