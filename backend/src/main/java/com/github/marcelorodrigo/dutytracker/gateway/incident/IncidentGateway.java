package com.github.marcelorodrigo.dutytracker.gateway.incident;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.usecase.request.PaginationRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface IncidentGateway {
    Incident save(Incident incident);

    Optional<Incident> findById(Long id);

    List<Incident> findByOnCallPeriodId(Long onCallPeriodId);

    Page<Incident> findByOnCallPeriodId(Long onCallPeriodId, PaginationRequest pagination);

    Page<Incident> findAll(PaginationRequest pagination);

    void deleteById(Long id);

    boolean existsOverlapping(Long onCallPeriodId, LocalDateTime start, LocalDateTime end, Long excludeId);
}
