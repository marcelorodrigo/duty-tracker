package com.github.marcelorodrigo.dutytracker.gateway.oncall;

import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OnCallPeriodGateway {
    OnCallPeriod save(OnCallPeriod period);

    Optional<OnCallPeriod> findById(Long id);

    List<OnCallPeriod> findAll();

    void deleteById(Long id);

    boolean existsOverlapping(LocalDateTime start, LocalDateTime end, Long excludeId);
}
