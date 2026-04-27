package com.dutytracker.gateway.postgres.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "holiday_override")
public class HolidayOverrideEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "on_call_period_id", nullable = false)
    private OnCallPeriodEntity onCallPeriod;

    private LocalDate date;

    public HolidayOverrideEntity() {
    }

    public HolidayOverrideEntity(Long id, OnCallPeriodEntity onCallPeriod, LocalDate date) {
        this.id = id;
        this.onCallPeriod = onCallPeriod;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OnCallPeriodEntity getOnCallPeriod() {
        return onCallPeriod;
    }

    public void setOnCallPeriod(OnCallPeriodEntity onCallPeriod) {
        this.onCallPeriod = onCallPeriod;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
