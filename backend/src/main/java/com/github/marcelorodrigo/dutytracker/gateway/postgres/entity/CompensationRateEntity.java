package com.github.marcelorodrigo.dutytracker.gateway.postgres.entity;

import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalTime;
import lombok.Getter;

@Entity
@Table(name = "compensation_rate")
@Getter
public class CompensationRateEntity extends JpaEntity {

    @Enumerated(EnumType.STRING)
    private RateCategory rateCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "overtime_day_type")
    private OvertimeDayType overtimeDayType;

    private String label;

    private LocalTime timeFrom;

    private LocalTime timeTo;

    private BigDecimal percentage;

    protected CompensationRateEntity() {}

    public CompensationRateEntity(
            Long id,
            RateCategory rateCategory,
            OvertimeDayType overtimeDayType,
            String label,
            LocalTime timeFrom,
            LocalTime timeTo,
            BigDecimal percentage) {
        super(id);
        this.rateCategory = rateCategory;
        this.overtimeDayType = overtimeDayType;
        this.label = label;
        this.timeFrom = timeFrom;
        this.timeTo = timeTo;
        this.percentage = percentage;
    }

    public void updateDetails(String label, BigDecimal percentage) {
        this.label = label;
        this.percentage = percentage;
    }
}
