package com.github.marcelorodrigo.dutytracker.gateway.postgres.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "incident")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class IncidentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "on_call_period_id", nullable = true)
    private OnCallPeriodEntity onCallPeriod;

    private String name;

    private LocalDate date;

    private LocalTime startTime;

    private LocalTime endTime;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public IncidentEntity(
            Long id,
            OnCallPeriodEntity onCallPeriod,
            String name,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime) {
        this.id = id;
        this.onCallPeriod = onCallPeriod;
        this.name = name;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
