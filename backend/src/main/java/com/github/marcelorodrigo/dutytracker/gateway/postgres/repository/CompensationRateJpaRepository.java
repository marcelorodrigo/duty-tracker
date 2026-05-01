package com.github.marcelorodrigo.dutytracker.gateway.postgres.repository;

import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.CompensationRateEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompensationRateJpaRepository extends JpaRepository<CompensationRateEntity, Long> {
    List<CompensationRateEntity> findByRateCategoryAndOvertimeDayType(
            RateCategory rateCategory, OvertimeDayType overtimeDayType);
}
