package com.github.marcelorodrigo.dutytracker.usecase.request;

import java.util.List;

/**
 * Framework-neutral pagination request carried through the use-case layer.
 *
 * <p>Keeps Spring Data types (and any other persistence framework) out of the use-case
 * request records. The gateway adapter is responsible for translating this into the
 * framework's native paging type immediately before touching persistence.
 */
public record PaginationRequest(int page, int size, List<SortOrder> sorts) {

    public enum Direction {
        ASC,
        DESC
    }

    public record SortOrder(String field, Direction direction) {}
}
