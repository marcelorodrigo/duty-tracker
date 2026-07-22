package com.github.marcelorodrigo.dutytracker.gateway.postgres.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "incident")
@EntityListeners(AuditingEntityListener.class)
@Getter
public class IncidentEntity extends JpaEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "on_call_period_id", nullable = false)
    private OnCallPeriodEntity onCallPeriod;

    private String name;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected IncidentEntity() {}

    public IncidentEntity(
            Long id,
            OnCallPeriodEntity onCallPeriod,
            String name,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            LocalDateTime createdAt) {
        super(id);
        this.onCallPeriod = onCallPeriod;
        this.name = name;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.createdAt = createdAt;
    }

    public void updateDetails(String name, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.name = name;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }
}
