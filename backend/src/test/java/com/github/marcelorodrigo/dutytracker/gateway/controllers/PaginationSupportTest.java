package com.github.marcelorodrigo.dutytracker.gateway.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidPaginationRequestException;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

class PaginationSupportTest {

    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "startDateTime");
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "startDateTime");

    @Test
    @DisplayName("should apply defaults when page, size and sort are null")
    void shouldApplyDefaultsWhenParametersAreNull() {
        // when
        var pageable = PaginationSupport.toPageable(null, null, null, SORTABLE_FIELDS, DEFAULT_SORT);

        // then
        assertThat(pageable).isEqualTo(PageRequest.of(0, 20, DEFAULT_SORT));
    }

    @Test
    @DisplayName("should build pageable from explicit page, size and sort")
    void shouldBuildPageableFromExplicitParameters() {
        // when
        var pageable = PaginationSupport.toPageable(2, 10, "id,asc", SORTABLE_FIELDS, DEFAULT_SORT);

        // then
        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Test
    @DisplayName("should chain multiple sort tokens into a composite sort")
    void shouldChainMultipleSortTokens() {
        // when
        var pageable = PaginationSupport.toPageable(0, 20, "startDateTime,desc;id,asc", SORTABLE_FIELDS, DEFAULT_SORT);

        // then
        assertThat(pageable.getSort())
                .isEqualTo(Sort.by(Sort.Direction.DESC, "startDateTime").and(Sort.by(Sort.Direction.ASC, "id")));
    }

    @Test
    @DisplayName("should default to ascending direction when only field is given")
    void shouldDefaultToAscendingWhenDirectionOmitted() {
        // when
        var pageable = PaginationSupport.toPageable(0, 20, "id", SORTABLE_FIELDS, DEFAULT_SORT);

        // then
        assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Test
    @DisplayName("should throw when page index is negative")
    void shouldThrowWhenPageIndexIsNegative() {
        assertThatThrownBy(() -> PaginationSupport.toPageable(-1, 20, null, SORTABLE_FIELDS, DEFAULT_SORT))
                .isInstanceOf(InvalidPaginationRequestException.class)
                .hasMessageContaining("zero or positive");
    }

    @Test
    @DisplayName("should throw when page size is below the minimum")
    void shouldThrowWhenPageSizeIsBelowMinimum() {
        assertThatThrownBy(() -> PaginationSupport.toPageable(0, 0, null, SORTABLE_FIELDS, DEFAULT_SORT))
                .isInstanceOf(InvalidPaginationRequestException.class)
                .hasMessageContaining("at least 1");
    }

    @Test
    @DisplayName("should throw when page size exceeds the maximum")
    void shouldThrowWhenPageSizeExceedsMaximum() {
        assertThatThrownBy(() -> PaginationSupport.toPageable(0, 101, null, SORTABLE_FIELDS, DEFAULT_SORT))
                .isInstanceOf(InvalidPaginationRequestException.class)
                .hasMessageContaining("must not exceed 100");
    }

    @Test
    @DisplayName("should throw when sort field is not whitelisted")
    void shouldThrowWhenSortFieldIsNotWhitelisted() {
        assertThatThrownBy(() -> PaginationSupport.toPageable(0, 20, "secretField", SORTABLE_FIELDS, DEFAULT_SORT))
                .isInstanceOf(InvalidPaginationRequestException.class)
                .hasMessageContaining("Unknown sort field 'secretField'");
    }

    @Test
    @DisplayName("should throw when sort direction is invalid")
    void shouldThrowWhenSortDirectionIsInvalid() {
        assertThatThrownBy(() -> PaginationSupport.toPageable(0, 20, "id,sideways", SORTABLE_FIELDS, DEFAULT_SORT))
                .isInstanceOf(InvalidPaginationRequestException.class)
                .hasMessageContaining("Invalid sort direction 'sideways'");
    }
}
