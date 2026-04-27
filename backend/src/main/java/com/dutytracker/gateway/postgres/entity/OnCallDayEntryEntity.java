package com.dutytracker.gateway.postgres.entity;

import com.dutytracker.domain.StandbyRateType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "on_call_day_entry")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
}
