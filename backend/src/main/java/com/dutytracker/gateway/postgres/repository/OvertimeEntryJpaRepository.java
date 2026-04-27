package com.dutytracker.gateway.postgres.repository;



import com.dutytracker.gateway.postgres.entity.IncidentEntity;
import com.dutytracker.gateway.postgres.entity.OvertimeEntryEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
public interface OvertimeEntryJpaRepository extends JpaRepository<OvertimeEntryEntity, Long> {
    List<OvertimeEntryEntity> findByIncidentId(Long id);

    @Transactional
    void deleteByIncident(IncidentEntity i);
}
