package com.dutytracker.gateway.compensation;

import com.dutytracker.domain.CompensationRate;
import com.dutytracker.domain.OvertimeDayType;
import com.dutytracker.domain.RateCategory;
import java.util.List;
import java.util.Optional;

public interface CompensationRateGateway {
    List<CompensationRate> saveAll(List<CompensationRate> rates);

    List<CompensationRate> findAll();

    List<CompensationRate> findByRateCategoryAndOvertimeDayType(
            RateCategory rateCategory, OvertimeDayType overtimeDayType);

    CompensationRate update(CompensationRate rate);

    void deleteById(Long id);

    Optional<CompensationRate> findById(Long id);
}
