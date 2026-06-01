package com.github.marcelorodrigo.dutytracker.gateway.oncall;

import com.github.marcelorodrigo.dutytracker.domain.Holiday;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface HolidayGateway {
    Holiday save(Holiday holiday);

    List<Holiday> saveAll(List<Holiday> holidays);

    List<Holiday> findByOnCallPeriodId(Long onCallPeriodId);

    Map<Long, List<Holiday>> findByOnCallPeriodIds(List<Long> periodIds);

    void deleteById(Long id);

    void deleteByOnCallPeriodId(Long onCallPeriodId);

    void deleteOutOfRange(Long onCallPeriodId, LocalDate start, LocalDate end);
}
