package com.dutytracker.infrastructure.persistence.repository;

import com.dutytracker.infrastructure.persistence.entity.HolidayOverrideEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HolidayOverrideJpaRepository extends JpaRepository<HolidayOverrideEntity, Long> {
    List<HolidayOverrideEntity> findByOnCallPeriodId(Long id);

    Optional<HolidayOverrideEntity> findByOnCallPeriodIdAndDate(Long id, LocalDate date);
}
