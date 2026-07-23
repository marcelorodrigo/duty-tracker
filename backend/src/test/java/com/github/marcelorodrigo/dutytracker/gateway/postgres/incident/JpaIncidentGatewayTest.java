package com.github.marcelorodrigo.dutytracker.gateway.postgres.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentMapper;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.IncidentEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.OnCallPeriodEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.IncidentJpaRepository;
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
class JpaIncidentGatewayTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, Month.JULY, 20, 18, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, Month.JULY, 20, 19, 0);

    @Mock
    private IncidentJpaRepository repository;

    @Mock
    private IncidentMapper mapper;

    @InjectMocks
    private JpaIncidentGateway gateway;

    @Test
    @DisplayName("should create an incident from its mapped entity")
    void shouldCreateAnIncidentFromItsMappedEntity() {
        // given
        var domain = new Incident(null, 7L, "Production incident", START, END, null);
        var entity = new IncidentEntity(null, new OnCallPeriodEntity(7L, null, null), domain.name(), START, END, null);
        var savedDomain = new Incident(11L, 7L, domain.name(), START, END, START.minusHours(1));
        when(mapper.toEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(savedDomain);

        // when
        var result = gateway.save(domain);

        // then
        assertThat(result).isEqualTo(savedDomain);
        verify(repository, never()).findById(11L);
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("should update a loaded incident while preserving its persistence state")
    void shouldUpdateALoadedIncidentWhilePreservingItsPersistenceState() {
        // given
        var entity = new IncidentEntity(
                11L, new OnCallPeriodEntity(7L, null, null), "Old name", START, END, START.minusHours(1));
        var updatedDomain = new Incident(11L, 7L, "Updated incident", START.plusHours(1), END.plusHours(1), null);
        when(repository.findById(11L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(updatedDomain);

        // when
        var result = gateway.save(updatedDomain);

        // then
        assertThat(result).isEqualTo(updatedDomain);
        assertThat(entity.getName()).isEqualTo(updatedDomain.name());
        assertThat(entity.getStartDateTime()).isEqualTo(updatedDomain.startDateTime());
        assertThat(entity.getEndDateTime()).isEqualTo(updatedDomain.endDateTime());
        verify(mapper, never()).toEntity(updatedDomain);
    }
}
