package com.github.marcelorodrigo.dutytracker.gateway.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidPaginationRequestException;
import com.github.marcelorodrigo.dutytracker.usecase.request.PaginationRequest.Direction;
import com.github.marcelorodrigo.dutytracker.usecase.request.PaginationRequest.SortOrder;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PaginationSupportTest {

    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "startDateTime");
    private static final List<SortOrder> DEFAULT_SORT = List.of(new SortOrder("startDateTime", Direction.DESC));

    @Test
    @DisplayName("should apply defaults when page, size and sort are null")
    void shouldApplyDefaultsWhenParametersAreNull() {
        // when
        var pagination = PaginationSupport.toPaginationRequest(null, null, null, SORTABLE_FIELDS, DEFAULT_SORT);

        // then
        assertThat(pagination.page()).isZero();
        assertThat(pagination.size()).isEqualTo(20);
        assertThat(pagination.sorts()).isEqualTo(DEFAULT_SORT);
    }

    @Test
    @DisplayName("should build pagination request from explicit page, size and sort")
    void shouldBuildPageableFromExplicitParameters() {
        // when
        var pagination = PaginationSupport.toPaginationRequest(2, 10, "id,asc", SORTABLE_FIELDS, DEFAULT_SORT);

        // then
        assertThat(pagination.page()).isEqualTo(2);
        assertThat(pagination.size()).isEqualTo(10);
        assertThat(pagination.sorts()).containsExactly(new SortOrder("id", Direction.ASC));
    }

    @Test
    @DisplayName("should chain multiple sort tokens into a composite sort")
    void shouldChainMultipleSortTokens() {
        // when
        var pagination = PaginationSupport.toPaginationRequest(
                0, 20, "startDateTime,desc;id,asc", SORTABLE_FIELDS, DEFAULT_SORT);

        // then
        assertThat(pagination.sorts())
                .containsExactly(new SortOrder("startDateTime", Direction.DESC), new SortOrder("id", Direction.ASC));
    }

    @Test
    @DisplayName("should default to ascending direction when only field is given")
    void shouldDefaultToAscendingWhenDirectionOmitted() {
        // when
        var pagination = PaginationSupport.toPaginationRequest(0, 20, "id", SORTABLE_FIELDS, DEFAULT_SORT);

        // then
        assertThat(pagination.sorts()).containsExactly(new SortOrder("id", Direction.ASC));
    }

    @Test
    @DisplayName("should throw when page index is negative")
    void shouldThrowWhenPageIndexIsNegative() {
        assertThatThrownBy(() -> PaginationSupport.toPaginationRequest(-1, 20, null, SORTABLE_FIELDS, DEFAULT_SORT))
                .isInstanceOf(InvalidPaginationRequestException.class)
                .hasMessageContaining("zero or positive");
    }

    @Test
    @DisplayName("should throw when page size is below the minimum")
    void shouldThrowWhenPageSizeIsBelowMinimum() {
        assertThatThrownBy(() -> PaginationSupport.toPaginationRequest(0, 0, null, SORTABLE_FIELDS, DEFAULT_SORT))
                .isInstanceOf(InvalidPaginationRequestException.class)
                .hasMessageContaining("at least 1");
    }

    @Test
    @DisplayName("should throw when page size exceeds the maximum")
    void shouldThrowWhenPageSizeExceedsMaximum() {
        assertThatThrownBy(() -> PaginationSupport.toPaginationRequest(0, 101, null, SORTABLE_FIELDS, DEFAULT_SORT))
                .isInstanceOf(InvalidPaginationRequestException.class)
                .hasMessageContaining("must not exceed 100");
    }

    @Test
    @DisplayName("should throw when sort field is not whitelisted")
    void shouldThrowWhenSortFieldIsNotWhitelisted() {
        assertThatThrownBy(() ->
                        PaginationSupport.toPaginationRequest(0, 20, "secretField", SORTABLE_FIELDS, DEFAULT_SORT))
                .isInstanceOf(InvalidPaginationRequestException.class)
                .hasMessageContaining("Unknown sort field 'secretField'");
    }

    @Test
    @DisplayName("should throw when sort direction is invalid")
    void shouldThrowWhenSortDirectionIsInvalid() {
        assertThatThrownBy(() ->
                        PaginationSupport.toPaginationRequest(0, 20, "id,sideways", SORTABLE_FIELDS, DEFAULT_SORT))
                .isInstanceOf(InvalidPaginationRequestException.class)
                .hasMessageContaining("Invalid sort direction 'sideways'");
    }

    @Test
    @DisplayName("should throw with a 400 when sort token has a trailing comma")
    void shouldThrowWhenSortTokenHasTrailingComma() {
        assertThatThrownBy(() -> PaginationSupport.toPaginationRequest(0, 20, "id,", SORTABLE_FIELDS, DEFAULT_SORT))
                .isInstanceOf(InvalidPaginationRequestException.class)
                .hasMessageContaining("Invalid sort specification 'id,'");
    }

    @Test
    @DisplayName("should throw when sort token has too many parts")
    void shouldThrowWhenSortTokenHasTooManyParts() {
        assertThatThrownBy(() ->
                        PaginationSupport.toPaginationRequest(0, 20, "id,asc,extra", SORTABLE_FIELDS, DEFAULT_SORT))
                .isInstanceOf(InvalidPaginationRequestException.class)
                .hasMessageContaining("Invalid sort specification 'id,asc,extra'");
    }

    @Test
    @DisplayName("should throw when sort contains empty fields from delimiters")
    void shouldThrowWhenSortHasEmptyFields() {
        for (String malformed : new String[] {"id;", ";id", "id;;startDateTime"}) {
            assertThatThrownBy(() ->
                            PaginationSupport.toPaginationRequest(0, 20, malformed, SORTABLE_FIELDS, DEFAULT_SORT))
                    .isInstanceOf(InvalidPaginationRequestException.class)
                    .hasMessageContaining("empty sort field is not allowed");
        }
    }
}
