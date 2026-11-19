package com.github.marcelorodrigo.dutytracker.gateway.postgres.oncall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodMapper;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.OnCallPeriodEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.OnCallPeriodJpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JpaOnCallPeriodGatewayTest {

    @Mock
    private OnCallPeriodJpaRepository repository;

    @Mock
    private OnCallPeriodMapper mapper;

    @InjectMocks
    private JpaOnCallPeriodGateway gateway;

    private static OnCallPeriod aPeriod() {
        return new OnCallPeriod(
                4L,
                LocalDateTime.of(2025, 3, 1, 0, 0),
                LocalDateTime.of(2025, 3, 31, 23, 59),
                LocalDateTime.of(2025, 2, 1, 12, 0));
    }

    private static OnCallPeriodEntity anEntity() {
        var entity =
                new OnCallPeriodEntity(4L, LocalDateTime.of(2025, 3, 1, 0, 0), LocalDateTime.of(2025, 3, 31, 23, 59));
        entity.setCreatedAt(LocalDateTime.of(2025, 2, 1, 12, 0));
        return entity;
    }

    @Test
    @DisplayName("should save and return the mapped on-call period")
    void shouldSaveAndReturnMappedOnCallPeriod() {
        // given
        var period = aPeriod();
        var entity = anEntity();
        when(mapper.toEntity(period)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(period);

        // when
        var result = gateway.save(period);

        // then
        assertThat(result).isEqualTo(period);
        verify(repository).save(entity);
        verify(mapper).toDomain(entity);
    }

    @Test
    @DisplayName("should return the mapped on-call period when it exists")
    void shouldReturnMappedOnCallPeriodWhenItExists() {
        // given
        var entity = anEntity();
        var period = aPeriod();
        when(repository.findById(4L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(period);

        // when
        var result = gateway.findById(4L);

        // then
        assertThat(result).isPresent().contains(period);
    }

    @Test
    @DisplayName("should return an empty optional when the on-call period does not exist")
    void shouldReturnEmptyOptionalWhenOnCallPeriodDoesNotExist() {
        // given
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // when
        var result = gateway.findById(99L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should delegate the on-call period overlap lookup")
    void shouldDelegateOnCallPeriodOverlapLookup() {
        // given
        var start = LocalDateTime.of(2025, 3, 1, 0, 0);
        var end = LocalDateTime.of(2025, 3, 31, 23, 59);
        when(repository.existsOverlapping(start, end, 4L)).thenReturn(true);

        // when
        var result = gateway.existsOverlapping(start, end, 4L);

        // then
        assertThat(result).isTrue();
        verify(repository).existsOverlapping(start, end, 4L);
    }
}
