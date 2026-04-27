package com.dutytracker.gateway.postgres.repository;


import com.dutytracker.gateway.postgres.entity.OnCallPeriodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
public interface OnCallPeriodJpaRepository extends JpaRepository<OnCallPeriodEntity, Long> {
}
