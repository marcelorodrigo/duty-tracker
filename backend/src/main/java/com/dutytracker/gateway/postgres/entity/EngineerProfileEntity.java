package com.dutytracker.gateway.postgres.entity;


import com.dutytracker.domain.EmployeeType;
import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Set;
@Entity
@Table(name = "engineer_profile")
public class EngineerProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private EmployeeType employeeType;

    private Set<DayOfWeek> workingDays;

    private LocalTime workStartTime;

    private LocalTime workEndTime;

    private Instant createdAt;

    public EngineerProfileEntity() {
    }

    public EngineerProfileEntity(Long id, EmployeeType employeeType, Set<DayOfWeek> workingDays,
                                 LocalTime workStartTime, LocalTime workEndTime, Instant createdAt) {
        this.id = id;
        this.employeeType = employeeType;
        this.workingDays = workingDays;
        this.workStartTime = workStartTime;
        this.workEndTime = workEndTime;
        this.createdAt = createdAt;
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

    public Set<DayOfWeek> getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(Set<DayOfWeek> workingDays) {
        this.workingDays = workingDays;
    }

    public LocalTime getWorkStartTime() {
        return workStartTime;
    }

    public void setWorkStartTime(LocalTime workStartTime) {
        this.workStartTime = workStartTime;
    }

    public LocalTime getWorkEndTime() {
        return workEndTime;
    }

    public void setWorkEndTime(LocalTime workEndTime) {
        this.workEndTime = workEndTime;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
