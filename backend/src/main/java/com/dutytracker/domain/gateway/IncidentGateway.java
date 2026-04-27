package com.dutytracker.domain.gateway;

import com.dutytracker.domain.model.Incident;

import java.util.List;
import java.util.Optional;

public interface IncidentGateway {
    Incident save(Incident incident);
    Optional<Incident> findById(Long id);
    List<Incident> findByOnCallPeriodId(Long onCallPeriodId);
    List<Incident> findAll();
    void deleteById(Long id);
}
