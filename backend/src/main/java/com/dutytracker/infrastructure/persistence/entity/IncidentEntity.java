package com.dutytracker.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "incident")
public class IncidentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "on_call_period_id", nullable = true)
    private OnCallPeriodEntity onCallPeriod;

    private LocalDate date;

    private LocalTime startTime;

    private LocalTime endTime;

    private Instant createdAt;

    public IncidentEntity() {
    }

    public IncidentEntity(Long id, OnCallPeriodEntity onCallPeriod, LocalDate date,
                          LocalTime startTime, LocalTime endTime, Instant createdAt) {
        this.id = id;
        this.onCallPeriod = onCallPeriod;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createdAt = createdAt;
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

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
