package com.dutytracker.gateway.postgres.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "on_call_period")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class OnCallPeriodEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public OnCallPeriodEntity(Long id, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.id = id;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }
}
