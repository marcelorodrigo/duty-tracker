package com.github.marcelorodrigo.dutytracker.gateway.postgres.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentMapper;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.IncidentEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.OnCallPeriodEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.IncidentJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JpaIncidentGatewayTest {

    @Mock
    private IncidentJpaRepository repository;

    @Mock
    private IncidentMapper mapper;

    @InjectMocks
    private JpaIncidentGateway gateway;

    private static Incident anIncident() {
        return new Incident(
                7L,
                3L,
                "Database outage",
                LocalDateTime.of(2025, 3, 10, 10, 0),
                LocalDateTime.of(2025, 3, 10, 11, 0),
                LocalDateTime.of(2025, 3, 10, 10, 5));
    }

    private static IncidentEntity anEntity() {
        return new IncidentEntity(
                7L,
                new OnCallPeriodEntity(3L, null, null),
                "Database outage",
                LocalDateTime.of(2025, 3, 10, 10, 0),
                LocalDateTime.of(2025, 3, 10, 11, 0),
                LocalDateTime.of(2025, 3, 10, 10, 5));
    }

    @Test
    @DisplayName("should save and return the mapped incident")
    void shouldSaveAndReturnMappedIncident() {
        // given
        var incident = anIncident();
        var entity = anEntity();
        when(mapper.toEntity(incident)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(incident);

        // when
        var result = gateway.save(incident);

        // then
        assertThat(result).isEqualTo(incident);
        verify(repository).save(entity);
        verify(mapper).toDomain(entity);
    }

    @Test
    @DisplayName("should return the mapped incident when it exists")
    void shouldReturnMappedIncidentWhenItExists() {
        // given
        var entity = anEntity();
        var incident = anIncident();
        when(repository.findById(7L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(incident);

        // when
        var result = gateway.findById(7L);

        // then
        assertThat(result).isPresent().contains(incident);
    }

    @Test
    @DisplayName("should return an empty optional when the incident does not exist")
    void shouldReturnEmptyOptionalWhenIncidentDoesNotExist() {
        // given
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // when
        var result = gateway.findById(99L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should return incidents for an on-call period in start-time order")
    void shouldReturnIncidentsForOnCallPeriod() {
        // given
        var entities = List.of(anEntity());
        var incidents = List.of(anIncident());
        when(repository.findByOnCallPeriodIdOrderByStartDateTime(3L)).thenReturn(entities);
        when(mapper.toDomainList(entities)).thenReturn(incidents);

        // when
        var result = gateway.findByOnCallPeriodId(3L);

        // then
        assertThat(result).containsExactlyElementsOf(incidents);
        verify(repository).findByOnCallPeriodIdOrderByStartDateTime(3L);
    }

    @Test
    @DisplayName("should delegate the incident overlap lookup")
    void shouldDelegateIncidentOverlapLookup() {
        // given
        var start = LocalDateTime.of(2025, 3, 10, 10, 0);
        var end = LocalDateTime.of(2025, 3, 10, 11, 0);
        when(repository.existsOverlapping(3L, start, end, 7L)).thenReturn(true);

        // when
        var result = gateway.existsOverlapping(3L, start, end, 7L);

        // then
        assertThat(result).isTrue();
        verify(repository).existsOverlapping(3L, start, end, 7L);
    }
}
