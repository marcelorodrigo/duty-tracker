package com.dutytracker.gateway.postgres.entity;

import com.dutytracker.domain.EmployeeType;
import com.dutytracker.domain.RateCategory;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "compensation_rate")
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

    public CompensationRateEntity() {
    }

    public CompensationRateEntity(Long id, EmployeeType employeeType, RateCategory rateCategory,
                                  String label, LocalTime timeFrom, LocalTime timeTo, BigDecimal percentage) {
        this.id = id;
        this.employeeType = employeeType;
        this.rateCategory = rateCategory;
        this.label = label;
        this.timeFrom = timeFrom;
        this.timeTo = timeTo;
        this.percentage = percentage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EmployeeType getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(EmployeeType employeeType) {
        this.employeeType = employeeType;
    }

    public RateCategory getRateCategory() {
        return rateCategory;
    }

    public void setRateCategory(RateCategory rateCategory) {
        this.rateCategory = rateCategory;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
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

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }
}
