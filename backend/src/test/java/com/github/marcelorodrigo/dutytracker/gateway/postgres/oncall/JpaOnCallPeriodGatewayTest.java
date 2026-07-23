package com.github.marcelorodrigo.dutytracker.gateway.postgres.oncall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
    private static final LocalDateTime CREATED_AT = START.minusDays(1);

    @Mock
    private OnCallPeriodJpaRepository repository;

    @Mock
    private OnCallPeriodMapper mapper;

    @InjectMocks
    private JpaOnCallPeriodGateway gateway;

    @Test
    @DisplayName("should map the saved entity when creating without rereading")
    void shouldMapTheSavedEntityWhenCreatingWithoutRereading() {
        // given
        var domain = new OnCallPeriod(null, START, END, null);
        var mappedEntity = new OnCallPeriodEntity(null, START, END);
        var savedEntity = new OnCallPeriodEntity(7L, START, END, CREATED_AT);
        var savedDomain = new OnCallPeriod(7L, START, END, CREATED_AT);
        when(mapper.toEntity(domain)).thenReturn(mappedEntity);
        when(repository.save(mappedEntity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(savedDomain);

        // when
        var result = gateway.save(domain);

        // then
        assertThat(result).isEqualTo(savedDomain);
        verify(repository).save(mappedEntity);
        verify(mapper).toDomain(savedEntity);
        verify(repository, never()).findById(any());
    }

    @Test
    @DisplayName("should update the loaded entity and map the save result without rereading")
    void shouldUpdateTheLoadedEntityAndMapTheSaveResultWithoutRereading() {
        // given
        var entity = new OnCallPeriodEntity(7L, START, END, CREATED_AT);
        var initialVersion = entity.getVersion();
        var requestedUpdate = new OnCallPeriod(7L, START.plusHours(1), END.plusHours(1), null);
        var savedEntity =
                new OnCallPeriodEntity(7L, requestedUpdate.startDateTime(), requestedUpdate.endDateTime(), CREATED_AT);
        var savedDomain =
                new OnCallPeriod(7L, requestedUpdate.startDateTime(), requestedUpdate.endDateTime(), CREATED_AT);
        when(repository.findById(7L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(savedDomain);

        // when
        var result = gateway.save(requestedUpdate);

        // then
        assertThat(result).isEqualTo(savedDomain);
        assertThat(entity.getId()).isEqualTo(7L);
        assertThat(entity.getVersion()).isEqualTo(initialVersion);
        assertThat(entity.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(entity.getStartDateTime()).isEqualTo(requestedUpdate.startDateTime());
        assertThat(entity.getEndDateTime()).isEqualTo(requestedUpdate.endDateTime());
        verify(repository).save(entity);
        verify(mapper).toDomain(savedEntity);
        verify(repository, times(1)).findById(7L);
        verify(mapper, never()).toEntity(requestedUpdate);
    }
}
