package com.github.marcelorodrigo.dutytracker.gateway.oncall;

import com.github.marcelorodrigo.dutytracker.domain.HolidayOverride;
import java.util.List;

public interface HolidayOverrideGateway {
    HolidayOverride save(HolidayOverride override);

    List<HolidayOverride> findByOnCallPeriodId(Long onCallPeriodId);

    void deleteById(Long id);
}
