package com.dutytracker.gateway.postgres.repository;

import com.dutytracker.gateway.postgres.entity.RegistrationSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationSummaryJpaRepository extends JpaRepository<RegistrationSummaryEntity, Long> {}
