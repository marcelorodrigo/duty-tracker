package com.github.marcelorodrigo.dutytracker.gateway.postgres.repository;

import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.IncidentEntity;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentJpaRepository extends JpaRepository<IncidentEntity, Long> {
    List<IncidentEntity> findByOnCallPeriodId(Long onCallPeriodId, Sort sort);
}
