package com.github.marcelorodrigo.dutytracker.gateway.postgres.oncall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodMapper;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.OnCallPeriodEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.OnCallPeriodJpaRepository;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JpaOnCallPeriodGatewayTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, Month.JULY, 20, 9, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, Month.JULY, 20, 17, 0);

    @Mock
    private OnCallPeriodJpaRepository repository;

    @Mock
    private OnCallPeriodMapper mapper;

    @InjectMocks
    private JpaOnCallPeriodGateway gateway;

    @Test
    @DisplayName("should create an on-call period from its mapped entity")
    void shouldCreateAnOnCallPeriodFromItsMappedEntity() {
        // given
        var domain = new OnCallPeriod(null, START, END, null);
        var entity = new OnCallPeriodEntity(7L, START, END);
        var savedDomain = new OnCallPeriod(7L, START, END, START.minusHours(1));
        when(mapper.toEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(repository.findById(7L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(savedDomain);

        // when
        var result = gateway.save(domain);

        // then
        assertThat(result).isEqualTo(savedDomain);
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("should reschedule a loaded on-call period while preserving its persistence state")
    void shouldRescheduleALoadedOnCallPeriodWhilePreservingItsPersistenceState() {
        // given
        var entity = new OnCallPeriodEntity(7L, START, END);
        var updatedDomain = new OnCallPeriod(7L, START.plusHours(1), END.plusHours(1), null);
        when(repository.findById(7L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(updatedDomain);

        // when
        var result = gateway.save(updatedDomain);

        // then
        assertThat(result).isEqualTo(updatedDomain);
        assertThat(entity.getStartDateTime()).isEqualTo(updatedDomain.startDateTime());
        assertThat(entity.getEndDateTime()).isEqualTo(updatedDomain.endDateTime());
        verify(mapper, never()).toEntity(updatedDomain);
    }
}
