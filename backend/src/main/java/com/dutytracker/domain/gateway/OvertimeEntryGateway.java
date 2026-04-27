package com.dutytracker.domain.gateway;

import com.dutytracker.domain.model.OvertimeEntry;

import java.util.List;
import java.util.Optional;

public interface OvertimeEntryGateway {
    OvertimeEntry save(OvertimeEntry entry);
    List<OvertimeEntry> saveAll(List<OvertimeEntry> entries);
    List<OvertimeEntry> findByIncidentId(Long incidentId);
    Optional<OvertimeEntry> findById(Long id);
    void deleteById(Long id);
    void deleteByIncidentId(Long incidentId);
}
