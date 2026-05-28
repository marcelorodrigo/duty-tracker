package com.github.marcelorodrigo.dutytracker.gateway.postgres.oncall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.Holiday;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayMapper;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.HolidayEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.OnCallPeriodEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.HolidayJpaRepository;
import java.time.LocalDate;
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
class JpaHolidayGatewayTest {

    @Mock
    private HolidayJpaRepository repository;

    @Mock
    private HolidayMapper mapper;

    @InjectMocks
    private JpaHolidayGateway gateway;

    private static final Long PERIOD_ID = 1L;

    private static OnCallPeriodEntity aPeriodEntity() {
        return new OnCallPeriodEntity(
                PERIOD_ID, LocalDateTime.of(2024, 1, 1, 0, 0), LocalDateTime.of(2024, 1, 14, 23, 59));
    }

    private static HolidayEntity anEntity() {
        return new HolidayEntity(1L, aPeriodEntity(), LocalDate.of(2024, 1, 6), "Epiphany");
    }

    private static Holiday aDomain() {
        return new Holiday(1L, PERIOD_ID, LocalDate.of(2024, 1, 6), "Epiphany");
    }

    @Test
    @DisplayName("should save holiday and return mapped domain object")
    void shouldSaveHolidayAndReturnMappedDomainObject() {
        // given
        var domain = aDomain();
        var entity = anEntity();
        when(mapper.toEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        // when
        var result = gateway.save(domain);

        // then
        assertThat(result).isEqualTo(domain);
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("should save all holidays and return mapped domain list")
    void shouldSaveAllHolidaysAndReturnMappedDomainList() {
        // given
        var domains = List.of(aDomain());
        var entities = List.of(anEntity());
        when(mapper.toEntity(domains.getFirst())).thenReturn(entities.getFirst());
        when(repository.saveAll(anyList())).thenReturn(entities);
        when(mapper.toDomainList(entities)).thenReturn(domains);

        // when
        var result = gateway.saveAll(domains);

        // then
        assertThat(result).isEqualTo(domains);
        verify(repository).saveAll(anyList());
    }

    @Test
    @DisplayName("should return holidays for a given on-call period")
    void shouldReturnHolidaysForAGivenOnCallPeriod() {
        // given
        var entities = List.of(anEntity());
        var domains = List.of(aDomain());
        when(repository.findByOnCallPeriodId(PERIOD_ID)).thenReturn(entities);
        when(mapper.toDomainList(entities)).thenReturn(domains);

        // when
        var result = gateway.findByOnCallPeriodId(PERIOD_ID);

        // then
        assertThat(result).isEqualTo(domains);
    }

    @Test
    @DisplayName("should delete holiday by id")
    void shouldDeleteHolidayById() {
        // when
        gateway.deleteById(1L);

        // then
        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("should delete all holidays for an on-call period")
    void shouldDeleteAllHolidaysForAnOnCallPeriod() {
        // when
        gateway.deleteByOnCallPeriodId(PERIOD_ID);

        // then
        verify(repository).deleteByOnCallPeriodId(PERIOD_ID);
    }

    @Test
    @DisplayName("should delete holidays outside the given date range")
    void shouldDeleteHolidaysOutsideTheGivenDateRange() {
        // given
        var start = LocalDate.of(2024, 1, 1);
        var end = LocalDate.of(2024, 1, 14);

        // when
        gateway.deleteOutOfRange(PERIOD_ID, start, end);

        // then
        verify(repository).deleteOutOfRange(PERIOD_ID, start, end);
    }
}
