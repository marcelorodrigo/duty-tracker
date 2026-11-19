package com.github.marcelorodrigo.dutytracker.gateway.postgres;

import com.github.marcelorodrigo.dutytracker.usecase.request.PaginationRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Translates between the framework-neutral pagination types and Spring Data page types.
 *
 * <p>This is the single point where the use-case pagination types meet the persistence framework,
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

    public static <T> PageResult<T> toPageResult(Page<T> page) {
        return new PageResult<>(
                page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
