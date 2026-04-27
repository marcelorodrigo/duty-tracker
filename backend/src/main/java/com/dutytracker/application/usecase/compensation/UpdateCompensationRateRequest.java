package com.dutytracker.application.usecase.compensation;

import java.math.BigDecimal;

public record UpdateCompensationRateRequest(Long rateId, BigDecimal percentage, String label) {}
