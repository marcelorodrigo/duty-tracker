package com.github.marcelorodrigo.dutytracker.gateway.compensation;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import com.github.marcelorodrigo.dutytracker.usecase.request.PaginationRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.PageResult;
import java.util.List;
import java.util.Optional;

public interface CompensationRateGateway {
    CompensationRate save(CompensationRate rate);

    PageResult<CompensationRate> findAll(PaginationRequest pagination);

    List<CompensationRate> findByRateCategory(RateCategory rateCategory);

    List<CompensationRate> findByRateCategoryAndOvertimeDayType(
            RateCategory rateCategory, OvertimeDayType overtimeDayType);

    CompensationRate update(CompensationRate rate);

    void deleteById(Long id);

    Optional<CompensationRate> findById(Long id);
}
