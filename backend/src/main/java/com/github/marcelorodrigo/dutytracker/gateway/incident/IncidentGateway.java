package com.github.marcelorodrigo.dutytracker.gateway.incident;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.usecase.request.PaginationRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.PageResult;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IncidentGateway {
    Incident save(Incident incident);

    Optional<Incident> findById(Long id);

    List<Incident> findByOnCallPeriodId(Long onCallPeriodId);

    PageResult<Incident> findByOnCallPeriodId(Long onCallPeriodId, PaginationRequest pagination);

    PageResult<Incident> findAll(PaginationRequest pagination);

    void deleteById(Long id);

    boolean existsOverlapping(Long onCallPeriodId, LocalDateTime start, LocalDateTime end, Long excludeId);
}
