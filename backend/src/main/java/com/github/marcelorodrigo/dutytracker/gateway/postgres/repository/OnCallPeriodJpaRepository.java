package com.github.marcelorodrigo.dutytracker.gateway.postgres.repository;

import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.OnCallPeriodEntity;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OnCallPeriodJpaRepository extends JpaRepository<OnCallPeriodEntity, Long> {

    @Query("""
            SELECT COUNT(p) > 0 FROM OnCallPeriodEntity p
            WHERE p.startDateTime < :end
              AND p.endDateTime > :start
              AND (:excludeId IS NULL OR p.id <> :excludeId)
            """)
    boolean existsOverlapping(
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("excludeId") Long excludeId);
}
