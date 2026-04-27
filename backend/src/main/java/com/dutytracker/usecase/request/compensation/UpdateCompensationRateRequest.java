package com.dutytracker.usecase.request.compensation;

import java.math.BigDecimal;

public record UpdateCompensationRateRequest(Long rateId, BigDecimal percentage, String label) {}
