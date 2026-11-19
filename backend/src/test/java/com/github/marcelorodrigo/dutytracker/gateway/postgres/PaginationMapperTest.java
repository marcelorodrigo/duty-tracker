package com.github.marcelorodrigo.dutytracker.gateway.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.marcelorodrigo.dutytracker.usecase.request.PaginationRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.PaginationRequest.Direction;
import com.github.marcelorodrigo.dutytracker.usecase.request.PaginationRequest.SortOrder;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

class PaginationMapperTest {

    @Test
    @DisplayName("should map page, size and composite sort to a PageRequest")
    void shouldMapToPageRequest() {
        var pagination = new PaginationRequest(
                3, 15, List.of(new SortOrder("startDateTime", Direction.DESC), new SortOrder("id", Direction.ASC)));

        var result = PaginationMapper.toPageRequest(pagination);

        assertThat(result)
                .isEqualTo(PageRequest.of(
                        3, 15, Sort.by(Sort.Direction.DESC, "startDateTime").and(Sort.by(Sort.Direction.ASC, "id"))));
    }

    @Test
    @DisplayName("should map empty sort to unsorted PageRequest")
    void shouldMapEmptySortToUnsorted() {
        var pagination = new PaginationRequest(0, 20, List.of());

        var result = PaginationMapper.toPageRequest(pagination);

        assertThat(result).isEqualTo(PageRequest.of(0, 20, Sort.unsorted()));
    }

    @Test
    @DisplayName("should map a Spring Data page to a framework-neutral PageResult")
    void shouldMapToPageResult() {
        var page = new PageImpl<>(List.of("a", "b"), PageRequest.of(1, 2), 5L);

        var result = PaginationMapper.toPageResult(page);

        assertThat(result.content()).containsExactly("a", "b");
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(5L);
        assertThat(result.totalPages()).isEqualTo(3);
    }
}
