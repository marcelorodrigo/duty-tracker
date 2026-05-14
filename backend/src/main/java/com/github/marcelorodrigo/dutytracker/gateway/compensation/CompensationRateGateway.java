package com.github.marcelorodrigo.dutytracker.gateway.compensation;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import java.util.List;
import java.util.Optional;

public interface CompensationRateGateway {
    CompensationRate save(CompensationRate rate);

    List<CompensationRate> findAll();

    List<CompensationRate> findByRateCategory(RateCategory rateCategory);

    List<CompensationRate> findByRateCategoryAndOvertimeDayType(
            RateCategory rateCategory, OvertimeDayType overtimeDayType);

    CompensationRate update(CompensationRate rate);

    void deleteById(Long id);

    Optional<CompensationRate> findById(Long id);
}
