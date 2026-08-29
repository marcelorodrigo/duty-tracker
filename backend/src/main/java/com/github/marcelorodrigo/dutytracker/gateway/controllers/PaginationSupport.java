package com.github.marcelorodrigo.dutytracker.gateway.controllers;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidPaginationRequestException;
import com.github.marcelorodrigo.dutytracker.usecase.request.PaginationRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.PaginationRequest.Direction;
import com.github.marcelorodrigo.dutytracker.usecase.request.PaginationRequest.SortOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Translates raw pagination query parameters into a framework-neutral {@link PaginationRequest}.
 *
 * <p>Numeric bounds ({@code page >= 0}, {@code 1 <= size <= MAX_SIZE}) are additionally enforced by the generated
 * controller bindings ({@code @Min}/{@code @Max}); this helper re-validates them defensively and is the single place
 * that validates the {@code sort} whitelist so invalid requests fail with a domain-meaningful 400. The gateway adapter
 * is responsible for converting the result into the persistence framework's paging type.
 */
public final class PaginationSupport {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    private PaginationSupport() {}

    public static PaginationRequest toPaginationRequest(
            Integer page, Integer size, String sort, Set<String> sortableFields, List<SortOrder> defaultSort) {
        int pageNumber = page == null ? DEFAULT_PAGE : page;
        int pageSize = size == null ? DEFAULT_SIZE : size;

        if (pageNumber < 0) {
            throw new InvalidPaginationRequestException("Page index must be zero or positive.");
        }
        if (pageSize < 1) {
            throw new InvalidPaginationRequestException("Page size must be at least 1.");
        }
        if (pageSize > MAX_SIZE) {
            throw new InvalidPaginationRequestException("Page size must not exceed " + MAX_SIZE + ".");
        }

        List<SortOrder> effectiveSort =
                (sort == null || sort.isBlank()) ? defaultSort : parseSort(sort, sortableFields);
        return new PaginationRequest(pageNumber, pageSize, effectiveSort);
    }

    private static List<SortOrder> parseSort(String sort, Set<String> sortableFields) {
        List<SortOrder> result = new ArrayList<>();
        for (String spec : sort.split(";")) {
            String token = spec.trim();
            if (token.isEmpty()) {
                continue;
            }
            Direction direction = Direction.ASC;
            String field;
            if (token.startsWith("-")) {
                direction = Direction.DESC;
                field = token.substring(1).trim();
            } else if (token.contains(",")) {
                String[] parts = token.split(",", -1);
                if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
                    throw new InvalidPaginationRequestException(
                            "Invalid sort specification '" + token + "'. Use 'field', '-field', or 'field,asc|desc'.");
                }
                field = parts[0].trim();
                String raw = parts[1].trim().toLowerCase();
                if ("asc".equals(raw)) {
                    direction = Direction.ASC;
                } else if ("desc".equals(raw)) {
                    direction = Direction.DESC;
                } else {
                    throw new InvalidPaginationRequestException(
                            "Invalid sort direction '" + parts[1].trim() + "'. Use 'asc' or 'desc'.");
                }
            } else {
                field = token;
            }
            if (!sortableFields.contains(field)) {
                throw new InvalidPaginationRequestException(
                        "Unknown sort field '" + field + "'. Allowed fields: " + sortedFields(sortableFields) + ".");
            }
            result.add(new SortOrder(field, direction));
        }
        return result;
    }

    private static String sortedFields(Set<String> sortableFields) {
        return Arrays.stream(sortableFields.toArray(new String[0]))
                .sorted()
                .collect(Collectors.toList())
                .toString();
    }
}
