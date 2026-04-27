package com.dutytracker.gateway.postgres.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "overtime_entry")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
}
