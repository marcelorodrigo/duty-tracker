package com.dutytracker.infrastructure.persistence.repository;

import com.dutytracker.infrastructure.persistence.entity.OnCallDayEntryEntity;
import com.dutytracker.infrastructure.persistence.entity.OnCallPeriodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OnCallDayEntryJpaRepository extends JpaRepository<OnCallDayEntryEntity, Long> {
    List<OnCallDayEntryEntity> findByOnCallPeriodId(Long id);

    @Transactional
    void deleteByOnCallPeriod(OnCallPeriodEntity p);
}
