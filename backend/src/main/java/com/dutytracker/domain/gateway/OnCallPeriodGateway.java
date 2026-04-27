package com.dutytracker.domain.gateway;

import com.dutytracker.domain.model.OnCallPeriod;

import java.util.List;
import java.util.Optional;

public interface OnCallPeriodGateway {
    OnCallPeriod save(OnCallPeriod period);
    Optional<OnCallPeriod> findById(Long id);
    List<OnCallPeriod> findAll();
    void deleteById(Long id);
}
