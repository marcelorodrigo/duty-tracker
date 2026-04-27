package com.dutytracker.domain.gateway;

import com.dutytracker.domain.model.HolidayOverride;

import java.util.List;
import java.util.Optional;

public interface HolidayOverrideGateway {
    HolidayOverride save(HolidayOverride override);
    List<HolidayOverride> findByOnCallPeriodId(Long onCallPeriodId);
    void deleteById(Long id);
    Optional<HolidayOverride> findByOnCallPeriodIdAndDate(Long onCallPeriodId, java.time.LocalDate date);
}
