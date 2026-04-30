package com.dutytracker.gateway.oncall;

import com.dutytracker.domain.HolidayOverride;
import java.util.List;

public interface HolidayOverrideGateway {
    HolidayOverride save(HolidayOverride override);

    List<HolidayOverride> findByOnCallPeriodId(Long onCallPeriodId);

    void deleteById(Long id);
}
