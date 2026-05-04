package com.github.marcelorodrigo.dutytracker.gateway.postgres.repository;

import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.IncidentEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IncidentJpaRepository extends JpaRepository<IncidentEntity, Long> {
    List<IncidentEntity> findByOnCallPeriodIdOrderByStartDateTime(Long onCallPeriodId);

    List<IncidentEntity> findAllByOrderByStartDateTime();

    @Query("""
            SELECT COUNT(i) > 0 FROM IncidentEntity i
            WHERE i.onCallPeriod.id = :onCallPeriodId
              AND i.startDateTime < :end
              AND i.endDateTime > :start
              AND (:excludeId IS NULL OR i.id <> :excludeId)
            """)
    boolean existsOverlapping(
            @Param("onCallPeriodId") Long onCallPeriodId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("excludeId") Long excludeId);
}
