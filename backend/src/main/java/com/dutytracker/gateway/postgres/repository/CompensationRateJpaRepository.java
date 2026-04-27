package com.dutytracker.gateway.postgres.repository;

import com.dutytracker.domain.EmployeeType;
import com.dutytracker.gateway.postgres.entity.CompensationRateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompensationRateJpaRepository extends JpaRepository<CompensationRateEntity, Long> {
    List<CompensationRateEntity> findByEmployeeType(EmployeeType type);
}
