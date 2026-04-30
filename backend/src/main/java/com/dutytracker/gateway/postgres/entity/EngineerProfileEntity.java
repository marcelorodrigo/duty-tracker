package com.dutytracker.gateway.postgres.entity;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "engineer_profile")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class EngineerProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Set<DayOfWeek> workingDays;

    private LocalTime workStartTime;

    private LocalTime workEndTime;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public EngineerProfileEntity(Long id, Set<DayOfWeek> workingDays, LocalTime workStartTime, LocalTime workEndTime) {
        this.id = id;
        this.workingDays = workingDays;
        this.workStartTime = workStartTime;
        this.workEndTime = workEndTime;
    }
}
