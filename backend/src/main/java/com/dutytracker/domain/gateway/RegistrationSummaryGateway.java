package com.dutytracker.domain.gateway;

import com.dutytracker.domain.model.RegistrationSummary;

import java.util.List;
import java.util.Optional;

public interface RegistrationSummaryGateway {
    RegistrationSummary save(RegistrationSummary summary);
    Optional<RegistrationSummary> findById(Long id);
    List<RegistrationSummary> findAll();
    void deleteById(Long id);
    boolean existsAny();
}
