package com.github.marcelorodrigo.dutytracker.gateway.postgres.compensation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.CompensationRateNotFoundException;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationMapper;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.CompensationRateEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.CompensationRateJpaRepository;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JpaCompensationRateGatewayTest {

    @Mock
    private CompensationRateJpaRepository repository;

    @Mock
    private CompensationMapper mapper;

    @InjectMocks
    private JpaCompensationRateGateway gateway;

    private static CompensationRateEntity anEntity() {
        return new CompensationRateEntity(
                1L,
                RateCategory.OVERTIME_ALLOWANCE,
                OvertimeDayType.WEEKDAY,
                "Test",
                LocalTime.of(8, 0),
                LocalTime.of(17, 0),
                new BigDecimal("25.00"));
    }

    private static CompensationRate aDomain() {
        return new CompensationRate(
                1L,
                RateCategory.OVERTIME_ALLOWANCE,
                OvertimeDayType.WEEKDAY,
                "Test",
                LocalTime.of(8, 0),
                LocalTime.of(17, 0),
                new BigDecimal("25.00"));
    }

    @Test
    @DisplayName("should map and persist a new compensation rate")
    void shouldMapAndPersistNewCompensationRate() {
        // given
        var newRate = new CompensationRate(
                null,
                RateCategory.OVERTIME_ALLOWANCE,
                OvertimeDayType.WEEKDAY,
                "Test",
                LocalTime.of(8, 0),
                LocalTime.of(17, 0),
                new BigDecimal("25.00"));
        var newEntity = new CompensationRateEntity(
                null,
                RateCategory.OVERTIME_ALLOWANCE,
                OvertimeDayType.WEEKDAY,
                "Test",
                LocalTime.of(8, 0),
                LocalTime.of(17, 0),
                new BigDecimal("25.00"));
        var persistedEntity = anEntity();
        var persistedRate = aDomain();
        when(mapper.toEntity(newRate)).thenReturn(newEntity);
        when(repository.save(newEntity)).thenReturn(persistedEntity);
        when(mapper.toDomain(persistedEntity)).thenReturn(persistedRate);

        // when
        var result = gateway.create(newRate);

        // then
        assertThat(result).isEqualTo(persistedRate);
        assertThat(newRate.id()).isNull();
        assertThat(newEntity.getId()).isNull();
        verify(mapper).toEntity(newRate);
        verify(repository).save(newEntity);
    }

    @Test
    @DisplayName("should return all compensation rates")
    void shouldReturnAllCompensationRates() {
        // given
        var entities = List.of(anEntity());
        var domains = List.of(aDomain());
        when(repository.findAll()).thenReturn(entities);
        when(mapper.toDomainList(entities)).thenReturn(domains);

        // when
        var result = gateway.findAll();

        // then
        assertThat(result).hasSize(1).containsExactlyElementsOf(domains);
    }

    @Test
    @DisplayName("should return rates filtered by rate category")
    void shouldReturnRatesFilteredByRateCategory() {
        // given
        var entities = List.of(anEntity());
        var domains = List.of(aDomain());
        when(repository.findByRateCategory(RateCategory.OVERTIME_ALLOWANCE)).thenReturn(entities);
        when(mapper.toDomainList(entities)).thenReturn(domains);

        // when
        var result = gateway.findByRateCategory(RateCategory.OVERTIME_ALLOWANCE);

        // then
        assertThat(result).isEqualTo(domains);
    }

    @Test
    @DisplayName("should return rates filtered by rate category and overtime day type")
    void shouldReturnRatesFilteredByRateCategoryAndOvertimeDayType() {
        // given
        var entities = List.of(anEntity());
        var domains = List.of(aDomain());
        when(repository.findByRateCategoryAndOvertimeDayType(RateCategory.OVERTIME_ALLOWANCE, OvertimeDayType.WEEKDAY))
                .thenReturn(entities);
        when(mapper.toDomainList(entities)).thenReturn(domains);

        // when
        var result =
                gateway.findByRateCategoryAndOvertimeDayType(RateCategory.OVERTIME_ALLOWANCE, OvertimeDayType.WEEKDAY);

        // then
        assertThat(result).isEqualTo(domains);
    }

    @Test
    @DisplayName("should reject update when compensation rate does not exist")
    void shouldRejectUpdateWhenCompensationRateDoesNotExist() {
        // given
        var missingRate = new CompensationRate(
                99L,
                RateCategory.OVERTIME_ALLOWANCE,
                OvertimeDayType.WEEKDAY,
                "Missing",
                LocalTime.of(8, 0),
                LocalTime.of(17, 0),
                new BigDecimal("25.00"));
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> gateway.update(missingRate))
                .isInstanceOf(CompensationRateNotFoundException.class)
                .hasMessageContaining("99");
        verify(repository, never()).save(any());
        verify(mapper, never()).toEntity(any());
    }

    @Test
    @DisplayName("should update a loaded compensation rate while preserving its persistence state")
    void shouldUpdateALoadedCompensationRateWhilePreservingItsPersistenceState() {
        // given
        var entity = anEntity();
        var updatedDomain = new CompensationRate(
                1L,
                RateCategory.OVERTIME_ALLOWANCE,
                OvertimeDayType.WEEKDAY,
                "Updated label",
                LocalTime.of(8, 0),
                LocalTime.of(17, 0),
                new BigDecimal("35.00"));
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(updatedDomain);

        // when
        var result = gateway.update(updatedDomain);

        // then
        assertThat(result).isEqualTo(updatedDomain);
        assertThat(entity.getLabel()).isEqualTo(updatedDomain.label());
        assertThat(entity.getPercentage()).isEqualByComparingTo(updatedDomain.percentage());
        verify(repository).save(entity);
        verify(mapper, never()).toEntity(updatedDomain);
    }

    @Test
    @DisplayName("should delete by id")
    void shouldDeleteById() {
        // when
        gateway.deleteById(1L);

        // then
        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("should return mapped domain when rate is found by id")
    void shouldReturnMappedDomainWhenRateIsFoundById() {
        // given
        var entity = anEntity();
        var domain = aDomain();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        // when
        var result = gateway.findById(1L);

        // then
        assertThat(result).isPresent().contains(domain);
    }

    @Test
    @DisplayName("should return empty optional when rate is not found by id")
    void shouldReturnEmptyOptionalWhenRateIsNotFoundById() {
        // given
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // when
        var result = gateway.findById(99L);

        // then
        assertThat(result).isEmpty();
    }
}
