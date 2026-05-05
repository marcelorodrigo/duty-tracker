package com.github.marcelorodrigo.dutytracker.gateway.postgres.repository;

import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.HolidayEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HolidayJpaRepository extends JpaRepository<HolidayEntity, Long> {

    List<HolidayEntity> findByOnCallPeriodId(Long id);

    void deleteByOnCallPeriodId(Long onCallPeriodId);

    @Modifying
    @Query("DELETE FROM HolidayEntity h WHERE h.onCallPeriod.id = :periodId AND (h.date < :start OR h.date > :end)")
    void deleteOutOfRange(
            @Param("periodId") Long periodId, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
