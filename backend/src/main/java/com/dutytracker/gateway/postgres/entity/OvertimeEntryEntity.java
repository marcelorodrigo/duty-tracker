package com.dutytracker.gateway.postgres.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "overtime_entry")
public class OvertimeEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private IncidentEntity incident;

    private BigDecimal overtimeHours;

    private BigDecimal allowanceHours;

    private BigDecimal allowancePercentage;

    private LocalTime timeFrom;

    private LocalTime timeTo;

    private boolean isAllowanceEntry;

    private boolean manualOverride;

    public OvertimeEntryEntity() {}

    public OvertimeEntryEntity(
            Long id,
            IncidentEntity incident,
            BigDecimal overtimeHours,
            BigDecimal allowanceHours,
            BigDecimal allowancePercentage,
            LocalTime timeFrom,
            LocalTime timeTo,
            boolean isAllowanceEntry,
            boolean manualOverride) {
        this.id = id;
        this.incident = incident;
        this.overtimeHours = overtimeHours;
        this.allowanceHours = allowanceHours;
        this.allowancePercentage = allowancePercentage;
        this.timeFrom = timeFrom;
        this.timeTo = timeTo;
        this.isAllowanceEntry = isAllowanceEntry;
        this.manualOverride = manualOverride;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public IncidentEntity getIncident() {
        return incident;
    }

    public void setIncident(IncidentEntity incident) {
        this.incident = incident;
    }

    public BigDecimal getOvertimeHours() {
        return overtimeHours;
    }

    public void setOvertimeHours(BigDecimal overtimeHours) {
        this.overtimeHours = overtimeHours;
    }

    public BigDecimal getAllowanceHours() {
        return allowanceHours;
    }

    public void setAllowanceHours(BigDecimal allowanceHours) {
        this.allowanceHours = allowanceHours;
    }

    public BigDecimal getAllowancePercentage() {
        return allowancePercentage;
    }

    public void setAllowancePercentage(BigDecimal allowancePercentage) {
        this.allowancePercentage = allowancePercentage;
    }

    public LocalTime getTimeFrom() {
        return timeFrom;
    }

    public void setTimeFrom(LocalTime timeFrom) {
        this.timeFrom = timeFrom;
    }

    public LocalTime getTimeTo() {
        return timeTo;
    }

    public void setTimeTo(LocalTime timeTo) {
        this.timeTo = timeTo;
    }

    public boolean isAllowanceEntry() {
        return isAllowanceEntry;
    }

    public void setAllowanceEntry(boolean allowanceEntry) {
        isAllowanceEntry = allowanceEntry;
    }

    public boolean isManualOverride() {
        return manualOverride;
    }

    public void setManualOverride(boolean manualOverride) {
        this.manualOverride = manualOverride;
    }
}
