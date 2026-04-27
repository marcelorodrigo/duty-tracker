package com.dutytracker.gateway.incident;



import com.dutytracker.domain.Incident;
import java.util.List;
import java.util.Optional;
public interface IncidentGateway {
    Incident save(Incident incident);
    Optional<Incident> findById(Long id);
    List<Incident> findByOnCallPeriodId(Long onCallPeriodId);
    List<Incident> findAll();
    void deleteById(Long id);
}
