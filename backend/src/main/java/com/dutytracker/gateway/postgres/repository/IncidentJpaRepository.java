package com.dutytracker.gateway.postgres.repository;

import com.dutytracker.gateway.postgres.entity.IncidentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentJpaRepository extends JpaRepository<IncidentEntity, Long> {
    List<IncidentEntity> findByOnCallPeriodId(Long id);
}
