package com.dutytracker.infrastructure.persistence.repository;

import com.dutytracker.domain.model.EmployeeType;
import com.dutytracker.infrastructure.persistence.entity.CompensationRateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompensationRateJpaRepository extends JpaRepository<CompensationRateEntity, Long> {
    List<CompensationRateEntity> findByEmployeeType(EmployeeType type);
}
