package com.github.marcelorodrigo.dutytracker.gateway.oncall;

import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.usecase.request.PaginationRequest;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface OnCallPeriodGateway {
    OnCallPeriod save(OnCallPeriod period);

    Optional<OnCallPeriod> findById(Long id);

    Page<OnCallPeriod> findAll(PaginationRequest pagination);

    void deleteById(Long id);

    boolean existsOverlapping(LocalDateTime start, LocalDateTime end, Long excludeId);
}
