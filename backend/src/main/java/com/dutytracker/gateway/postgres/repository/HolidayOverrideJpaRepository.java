package com.dutytracker.gateway.postgres.repository;

import com.dutytracker.gateway.postgres.entity.HolidayOverrideEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HolidayOverrideJpaRepository extends JpaRepository<HolidayOverrideEntity, Long> {
    List<HolidayOverrideEntity> findByOnCallPeriodId(Long id);

    Optional<HolidayOverrideEntity> findByOnCallPeriodIdAndDate(Long id, LocalDate date);
}
