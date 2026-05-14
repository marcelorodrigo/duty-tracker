package com.github.marcelorodrigo.dutytracker.gateway.postgres.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "engineer_profile")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class EngineerProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Set<DayOfWeek> workingDays;

    private LocalTime workStartTime;

    private LocalTime workEndTime;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal hourlyRate;

    @Column(name = "standby_weekday_saturday_pct", precision = 10, scale = 5, nullable = false)
    private BigDecimal standbyWeekdaySaturdayPercentage;

    @Column(name = "standby_sunday_holiday_pct", precision = 10, scale = 5, nullable = false)
    private BigDecimal standbyWeekdaySundayHolidayPercentage;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public EngineerProfileEntity(
            Long id,
            Set<DayOfWeek> workingDays,
            LocalTime workStartTime,
            LocalTime workEndTime,
            BigDecimal hourlyRate,
            BigDecimal standbyWeekdaySaturdayPercentage,
            BigDecimal standbyWeekdaySundayHolidayPercentage) {
        this.id = id;
        this.workingDays = workingDays;
        this.workStartTime = workStartTime;
        this.workEndTime = workEndTime;
        this.hourlyRate = hourlyRate;
        this.standbyWeekdaySaturdayPercentage = standbyWeekdaySaturdayPercentage;
        this.standbyWeekdaySundayHolidayPercentage = standbyWeekdaySundayHolidayPercentage;
    }
}
