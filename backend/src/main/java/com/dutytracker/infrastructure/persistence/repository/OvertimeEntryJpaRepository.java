package com.dutytracker.infrastructure.persistence.repository;

import com.dutytracker.infrastructure.persistence.entity.IncidentEntity;
import com.dutytracker.infrastructure.persistence.entity.OvertimeEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OvertimeEntryJpaRepository extends JpaRepository<OvertimeEntryEntity, Long> {
    List<OvertimeEntryEntity> findByIncidentId(Long id);

    @Transactional
    void deleteByIncident(IncidentEntity i);
}
