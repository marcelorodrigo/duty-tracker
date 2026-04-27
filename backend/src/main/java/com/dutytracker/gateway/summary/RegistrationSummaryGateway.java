package com.dutytracker.gateway.summary;

import com.dutytracker.domain.RegistrationSummary;

import java.util.List;
import java.util.Optional;

public interface RegistrationSummaryGateway {
    RegistrationSummary save(RegistrationSummary summary);
    Optional<RegistrationSummary> findById(Long id);
    List<RegistrationSummary> findAll();
    void deleteById(Long id);
    boolean existsAny();
}
