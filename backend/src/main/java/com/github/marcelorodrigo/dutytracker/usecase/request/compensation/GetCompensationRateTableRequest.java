package com.github.marcelorodrigo.dutytracker.usecase.request.compensation;

import org.springframework.data.domain.Pageable;

public record GetCompensationRateTableRequest(Pageable pageable) {}
