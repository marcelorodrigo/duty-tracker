package com.dutytracker.gateway.postgres.entity;

import com.dutytracker.domain.EmployeeType;
import com.dutytracker.domain.RateCategory;
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
    private EmployeeType employeeType;

    @Enumerated(EnumType.STRING)
    private RateCategory rateCategory;

    private String label;

    private LocalTime timeFrom;

    private LocalTime timeTo;

    private BigDecimal percentage;
}
