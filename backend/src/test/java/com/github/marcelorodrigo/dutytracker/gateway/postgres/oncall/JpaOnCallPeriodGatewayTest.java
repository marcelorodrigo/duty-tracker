package com.github.marcelorodrigo.dutytracker.gateway.postgres.oncall;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.OnCallPeriodOverlapException;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodMapper;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.OnCallPeriodEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.OnCallPeriodJpaRepository;
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
class JpaOnCallPeriodGatewayTest {

    @Mock
    private OnCallPeriodJpaRepository repository;

    @Mock
    private OnCallPeriodMapper mapper;

    @InjectMocks
    private JpaOnCallPeriodGateway gateway;

    @Test
    @DisplayName("should translate the on-call overlap constraint violation")
    void shouldTranslateTheOnCallOverlapConstraintViolation() {
        // given
        var start = LocalDateTime.of(2026, 7, 22, 9, 0);
        var end = start.plusHours(8);
        var period = new OnCallPeriod(null, start, end, start);
        var entity = new OnCallPeriodEntity(null, start, end);
        var cause = new ConstraintViolationException(
                "overlapping on-call period", new SQLException(), "ex_on_call_period_no_overlap");
        var violation = new DataIntegrityViolationException("overlapping on-call period", cause);
        when(mapper.toEntity(period)).thenReturn(entity);
        when(repository.saveAndFlush(entity)).thenThrow(violation);

        // when / then
        assertThatThrownBy(() -> gateway.save(period))
                .isInstanceOf(OnCallPeriodOverlapException.class)
                .hasMessage("The requested period overlaps with an existing on-call period.");
    }
}
