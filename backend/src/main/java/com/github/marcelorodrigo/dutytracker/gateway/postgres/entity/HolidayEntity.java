package com.github.marcelorodrigo.dutytracker.gateway.postgres.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;

@Entity
@Table(name = "holiday")
@Getter
public class HolidayEntity extends JpaEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "on_call_period_id", nullable = false)
    private OnCallPeriodEntity onCallPeriod;

    private LocalDate date;

    private String name;

    protected HolidayEntity() {}

    public HolidayEntity(Long id, OnCallPeriodEntity onCallPeriod, LocalDate date, String name) {
        super(id);
        this.onCallPeriod = onCallPeriod;
        this.date = date;
        this.name = name;
    }

    public void updateDetails(LocalDate date, String name) {
        this.date = date;
        this.name = name;
    }
}
