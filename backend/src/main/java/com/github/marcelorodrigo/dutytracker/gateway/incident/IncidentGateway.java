package com.github.marcelorodrigo.dutytracker.gateway.incident;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IncidentGateway {
    Incident save(Incident incident);

    Optional<Incident> findById(Long id);

    List<Incident> findByOnCallPeriodId(Long onCallPeriodId);

    Page<Incident> findByOnCallPeriodId(Long onCallPeriodId, Pageable pageable);

    Page<Incident> findAll(Pageable pageable);

    void deleteById(Long id);

    boolean existsOverlapping(Long onCallPeriodId, LocalDateTime start, LocalDateTime end, Long excludeId);
}
