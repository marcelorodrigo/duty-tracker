package com.dutytracker.gateway.oncall;

import com.dutytracker.domain.OnCallDayEntry;

import java.util.List;
import java.util.Optional;

public interface OnCallDayEntryGateway {
    OnCallDayEntry save(OnCallDayEntry entry);
    List<OnCallDayEntry> saveAll(List<OnCallDayEntry> entries);
    List<OnCallDayEntry> findByOnCallPeriodId(Long onCallPeriodId);
    Optional<OnCallDayEntry> findById(Long id);
    void deleteById(Long id);
    void deleteByOnCallPeriodId(Long onCallPeriodId);
}
