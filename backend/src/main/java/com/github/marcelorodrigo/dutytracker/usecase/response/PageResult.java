package com.github.marcelorodrigo.dutytracker.usecase.response;

import java.util.List;

/**
 * Framework-neutral page of results, mirroring the paginated API wire contract.
 *
 * <p>{@code page} is a zero-based index. Gateways translate persistence-specific page types into this
 * record so that {@code org.springframework.data} never crosses the use-case boundary.
 */
public record PageResult<T>(List<T> content, int page, int size, long totalElements, int totalPages) {}
