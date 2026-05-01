package com.github.marcelorodrigo.dutytracker.gateway.postgres.entity;

import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "compensation_rate")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompensationRateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RateCategory rateCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "overtime_day_type")
    private OvertimeDayType overtimeDayType;

    private String label;

    private LocalTime timeFrom;

    private LocalTime timeTo;

    private BigDecimal percentage;
}
