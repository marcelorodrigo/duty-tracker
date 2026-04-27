package com.dutytracker.infrastructure.persistence.repository;

import com.dutytracker.infrastructure.persistence.entity.OnCallPeriodEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnCallPeriodJpaRepository extends JpaRepository<OnCallPeriodEntity, Long> {
}
