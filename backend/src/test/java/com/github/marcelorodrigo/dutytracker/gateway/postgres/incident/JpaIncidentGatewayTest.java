package com.github.marcelorodrigo.dutytracker.gateway.postgres.incident;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.IncidentOverlapException;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentMapper;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.IncidentEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.IncidentJpaRepository;
import java.sql.SQLException;
import java.time.LocalDateTime;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class JpaIncidentGatewayTest {

    @Mock
    private IncidentJpaRepository repository;

    @Mock
    private IncidentMapper mapper;

    @InjectMocks
    private JpaIncidentGateway gateway;

    @Test
    @DisplayName("should translate the incident overlap constraint violation")
    void shouldTranslateTheIncidentOverlapConstraintViolation() {
        // given
        var start = LocalDateTime.of(2026, 7, 22, 9, 0);
        var end = start.plusHours(1);
        var incident = new Incident(null, 1L, "Production incident", start, end, start);
        var entity = new IncidentEntity();
        var cause =
                new ConstraintViolationException("overlapping incident", new SQLException(), "ex_incident_no_overlap");
        var violation = new DataIntegrityViolationException("overlapping incident", cause);
        when(mapper.toEntity(incident)).thenReturn(entity);
        when(repository.saveAndFlush(entity)).thenThrow(violation);

        // when / then
        assertThatThrownBy(() -> gateway.save(incident))
                .isInstanceOf(IncidentOverlapException.class)
                .hasMessage("Incident overlaps with an existing incident in the same on-call period");
    }
}
