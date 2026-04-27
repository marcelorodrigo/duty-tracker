package com.dutytracker.gateway.postgres.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "holiday_override")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HolidayOverrideEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "on_call_period_id", nullable = false)
    private OnCallPeriodEntity onCallPeriod;

    private LocalDate date;
}
