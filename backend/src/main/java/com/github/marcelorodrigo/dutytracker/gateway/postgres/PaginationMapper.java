package com.github.marcelorodrigo.dutytracker.gateway.postgres;

import com.github.marcelorodrigo.dutytracker.usecase.request.PaginationRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Translates the framework-neutral {@link PaginationRequest} into a Spring Data {@link PageRequest}.
 *
 * <p>This is the single point where the use-case pagination type meets the persistence framework,
 * keeping {@code org.springframework.data} references out of the use-case layer.
 */
public final class PaginationMapper {

    private PaginationMapper() {}

    public static PageRequest toPageRequest(PaginationRequest pagination) {
        Sort sort = Sort.unsorted();
        for (PaginationRequest.SortOrder order : pagination.sorts()) {
            Sort.Direction direction =
                    order.direction() == PaginationRequest.Direction.DESC ? Sort.Direction.DESC : Sort.Direction.ASC;
            sort = sort.and(Sort.by(direction, order.field()));
        }
        return PageRequest.of(pagination.page(), pagination.size(), sort);
    }
}
