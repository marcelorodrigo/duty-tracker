package com.dutytracker.infrastructure.persistence.repository;

import com.dutytracker.infrastructure.persistence.entity.RegistrationSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationSummaryJpaRepository extends JpaRepository<RegistrationSummaryEntity, Long> {
}
