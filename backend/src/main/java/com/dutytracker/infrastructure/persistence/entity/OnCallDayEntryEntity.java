package com.dutytracker.infrastructure.persistence.entity;

import com.dutytracker.domain.model.StandbyRateType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "on_call_day_entry")
public class OnCallDayEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "on_call_period_id", nullable = false)
    private OnCallPeriodEntity onCallPeriod;

    private LocalDate date;

    private BigDecimal hours;

    @Enumerated(EnumType.STRING)
    private StandbyRateType rateType;

    private boolean capped;

    private boolean timeForTimeFlag;

    private boolean manualOverride;

    public OnCallDayEntryEntity() {
    }

    public OnCallDayEntryEntity(Long id, OnCallPeriodEntity onCallPeriod, LocalDate date, BigDecimal hours,
                                StandbyRateType rateType, boolean capped, boolean timeForTimeFlag, boolean manualOverride) {
        this.id = id;
        this.onCallPeriod = onCallPeriod;
        this.date = date;
        this.hours = hours;
        this.rateType = rateType;
        this.capped = capped;
        this.timeForTimeFlag = timeForTimeFlag;
        this.manualOverride = manualOverride;
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

    public BigDecimal getHours() {
        return hours;
    }

    public void setHours(BigDecimal hours) {
        this.hours = hours;
    }

    public StandbyRateType getRateType() {
        return rateType;
    }

    public void setRateType(StandbyRateType rateType) {
        this.rateType = rateType;
    }

    public boolean isCapped() {
        return capped;
    }

    public void setCapped(boolean capped) {
        this.capped = capped;
    }

    public boolean isTimeForTimeFlag() {
        return timeForTimeFlag;
    }

    public void setTimeForTimeFlag(boolean timeForTimeFlag) {
        this.timeForTimeFlag = timeForTimeFlag;
    }

    public boolean isManualOverride() {
        return manualOverride;
    }

    public void setManualOverride(boolean manualOverride) {
        this.manualOverride = manualOverride;
    }
}
