package com.github.marcelorodrigo.dutytracker.gateway.postgres.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "on_call_period")
@EntityListeners(AuditingEntityListener.class)
@Getter
public class OnCallPeriodEntity extends JpaEntity {

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected OnCallPeriodEntity() {}

    public OnCallPeriodEntity(Long id, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this(id, startDateTime, endDateTime, null);
    }

    public OnCallPeriodEntity(
            Long id, LocalDateTime startDateTime, LocalDateTime endDateTime, LocalDateTime createdAt) {
        super(id);
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.createdAt = createdAt;
    }

    public void reschedule(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }
}
