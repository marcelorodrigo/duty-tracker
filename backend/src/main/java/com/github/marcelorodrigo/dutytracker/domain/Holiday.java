package com.github.marcelorodrigo.dutytracker.domain;

import java.time.LocalDate;

public record Holiday(Long id, Long onCallPeriodId, LocalDate date, String name) {}
