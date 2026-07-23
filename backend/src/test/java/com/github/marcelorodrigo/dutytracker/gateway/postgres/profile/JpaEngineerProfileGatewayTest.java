package com.github.marcelorodrigo.dutytracker.gateway.postgres.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.EngineerProfileEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.EngineerProfileJpaRepository;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileMapper;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JpaEngineerProfileGatewayTest {

    @Mock
    private EngineerProfileJpaRepository repository;

    @Mock
    private EngineerProfileMapper mapper;

    @InjectMocks
    private JpaEngineerProfileGateway gateway;

    private static final Set<DayOfWeek> WORKING_DAYS =
            EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

    private static EngineerProfileEntity anEntity() {
        return new EngineerProfileEntity(
                1L,
                WORKING_DAYS,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                new BigDecimal("50.00"),
                new BigDecimal("15.000"),
                new BigDecimal("30.000"));
    }

    private static EngineerProfile aDomain() {
        return new EngineerProfile(
                1L,
                WORKING_DAYS,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                new BigDecimal("50.00"),
                new BigDecimal("15.000"),
                new BigDecimal("30.000"),
                LocalDateTime.of(2024, Month.JANUARY, 1, 10, 0));
    }

    @Test
    @DisplayName("should save and return mapped domain object")
    void shouldSaveAndReturnMappedDomainObject() {
        // given
        var domain = aDomain();
        var entity = anEntity();
        when(mapper.toEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domain);

        // when
        var result = gateway.save(domain);

        // then
        assertThat(result).isEqualTo(domain);
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("should update a loaded profile while preserving its persistence state")
    void shouldUpdateALoadedProfileWhilePreservingItsPersistenceState() {
        // given
        var entity = anEntity();
        var updatedDomain = new EngineerProfile(
                1L,
                EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                LocalTime.of(8, 0),
                LocalTime.of(16, 0),
                new BigDecimal("75.00"),
                new BigDecimal("20.000"),
                new BigDecimal("40.000"),
                LocalDateTime.of(2024, 1, 1, 10, 0));
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(updatedDomain);

        // when
        var result = gateway.save(updatedDomain);

        // then
        assertThat(result).isEqualTo(updatedDomain);
        assertThat(entity.getWorkingDays()).isEqualTo(updatedDomain.workingDays());
        assertThat(entity.getWorkStartTime()).isEqualTo(updatedDomain.workStartTime());
        assertThat(entity.getWorkEndTime()).isEqualTo(updatedDomain.workEndTime());
        assertThat(entity.getHourlyRate()).isEqualByComparingTo(updatedDomain.hourlyRate());
        verify(mapper, never()).toEntity(updatedDomain);
    }

    @Test
    @DisplayName("should return profile when one exists")
    void shouldReturnProfileWhenOneExists() {
        // given
        var entity = anEntity();
        var domain = aDomain();
        when(repository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        // when
        var result = gateway.find();

        // then
        assertThat(result).isPresent().contains(domain);
    }

    @Test
    @DisplayName("should return empty optional when no profile exists")
    void shouldReturnEmptyOptionalWhenNoProfileExists() {
        // given
        when(repository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        // when
        var result = gateway.find();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should delete by id")
    void shouldDeleteById() {
        // when
        gateway.deleteById(1L);

        // then
        verify(repository).deleteById(1L);
    }
}
