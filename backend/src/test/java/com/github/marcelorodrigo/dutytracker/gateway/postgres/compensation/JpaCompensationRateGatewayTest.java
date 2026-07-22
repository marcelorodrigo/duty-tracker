package com.github.marcelorodrigo.dutytracker.gateway.postgres.compensation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.DuplicateCompensationRateException;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationMapper;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.CompensationRateEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.CompensationRateJpaRepository;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

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
    @DisplayName("should save and return mapped domain object")
    void shouldSaveAndReturnMappedDomainObject() {
        // given
        var domain = aDomain();
        var entity = anEntity();
        when(mapper.toEntity(domain)).thenReturn(entity);
        when(repository.saveAndFlush(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domain);

        // when
        var result = gateway.save(domain);

        // then
        assertThat(result).isEqualTo(domain);
        verify(repository).saveAndFlush(entity);
    }

    @Test
    @DisplayName("should translate the compensation rate uniqueness constraint violation")
    void shouldTranslateCompensationRateUniquenessConstraintViolation() {
        // given
        var domain = aDomain();
        var entity = anEntity();
        var cause = new ConstraintViolationException(
                "duplicate compensation rate", new SQLException(), "uq_compensation_rate");
        var violation = new DataIntegrityViolationException("duplicate compensation rate", cause);
        when(mapper.toEntity(domain)).thenReturn(entity);
        when(repository.saveAndFlush(entity)).thenThrow(violation);

        // when / then
        assertThatThrownBy(() -> gateway.save(domain))
                .isInstanceOf(DuplicateCompensationRateException.class)
                .hasMessage("A compensation rate already exists for this time window");
    }

    @Test
    @DisplayName("should preserve unrelated compensation rate constraint violations")
    void shouldPreserveUnrelatedCompensationRateConstraintViolations() {
        // given
        var domain = aDomain();
        var entity = anEntity();
        var cause = new ConstraintViolationException("invalid compensation rate", new SQLException(), "check_rate");
        var violation = new DataIntegrityViolationException("invalid compensation rate", cause);
        when(mapper.toEntity(domain)).thenReturn(entity);
        when(repository.saveAndFlush(entity)).thenThrow(violation);

        // when / then
        assertThatThrownBy(() -> gateway.save(domain)).isSameAs(violation);
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
    @DisplayName("should update and return mapped domain object")
    void shouldUpdateAndReturnMappedDomainObject() {
        // given
        var domain = aDomain();
        var entity = anEntity();
        when(mapper.toEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domain);

        // when
        var result = gateway.update(domain);

        // then
        assertThat(result).isEqualTo(domain);
        verify(repository).save(entity);
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
