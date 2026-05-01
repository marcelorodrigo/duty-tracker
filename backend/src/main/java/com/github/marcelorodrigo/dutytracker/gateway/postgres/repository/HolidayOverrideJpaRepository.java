package com.github.marcelorodrigo.dutytracker.gateway.postgres.repository;

import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.HolidayOverrideEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HolidayOverrideJpaRepository extends JpaRepository<HolidayOverrideEntity, Long> {
    List<HolidayOverrideEntity> findByOnCallPeriodId(Long id);
}
