package com.github.marcelorodrigo.dutytracker.gateway.oncall;

import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OnCallPeriodGateway {
    OnCallPeriod save(OnCallPeriod period);

    Optional<OnCallPeriod> findById(Long id);

    Page<OnCallPeriod> findAll(Pageable pageable);

    void deleteById(Long id);

    boolean existsOverlapping(LocalDateTime start, LocalDateTime end, Long excludeId);
}
