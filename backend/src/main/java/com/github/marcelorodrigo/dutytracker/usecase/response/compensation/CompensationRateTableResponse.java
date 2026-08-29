package com.github.marcelorodrigo.dutytracker.usecase.response.compensation;

import java.util.List;

public record CompensationRateTableResponse(
        List<CompensationRateResponse> content, int page, int size, long totalElements, int totalPages) {}
