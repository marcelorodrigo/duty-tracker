package com.dutytracker.gateway.oncall;

import com.dutytracker.domain.OnCallPeriod;

import java.util.List;
import java.util.Optional;

public interface OnCallPeriodGateway {
    OnCallPeriod save(OnCallPeriod period);
    Optional<OnCallPeriod> findById(Long id);
    List<OnCallPeriod> findAll();
    void deleteById(Long id);
}
