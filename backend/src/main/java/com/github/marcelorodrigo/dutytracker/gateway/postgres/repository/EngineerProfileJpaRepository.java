package com.github.marcelorodrigo.dutytracker.gateway.postgres.repository;

import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.EngineerProfileEntity;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EngineerProfileJpaRepository extends JpaRepository<EngineerProfileEntity, Long> {
    Optional<EngineerProfileEntity> findFirstByOrderByIdAsc();

    @Query("""
            SELECT DISTINCT profile
            FROM EngineerProfileEntity profile
            JOIN profile.workingDays workingDay
            WHERE workingDay = :workingDay
            """)
    List<EngineerProfileEntity> findByWorkingDay(@Param("workingDay") DayOfWeek workingDay);
}
