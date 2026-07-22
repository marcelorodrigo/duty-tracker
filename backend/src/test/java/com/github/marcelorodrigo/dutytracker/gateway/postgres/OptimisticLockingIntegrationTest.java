package com.github.marcelorodrigo.dutytracker.gateway.postgres;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.CompensationRateEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.IncidentEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.OnCallPeriodEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.CompensationRateJpaRepository;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.IncidentJpaRepository;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.OnCallPeriodJpaRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
class OptimisticLockingIntegrationTest extends PostgreSqlRepositoryTestSupport {

    @Autowired
    private CompensationRateJpaRepository compensationRateRepository;

    @Autowired
    private IncidentJpaRepository incidentRepository;

    @Autowired
    private OnCallPeriodJpaRepository onCallPeriodRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Long compensationRateId;
    private Long onCallPeriodId;

    @AfterEach
    void removeCommittedFixtures() {
        transactions().executeWithoutResult(status -> {
            if (compensationRateId != null) {
                compensationRateRepository.deleteById(compensationRateId);
            }
            if (onCallPeriodId != null) {
                onCallPeriodRepository.deleteById(onCallPeriodId);
            }
        });
    }

    @Test
    @DisplayName("should reject a stale compensation rate update from a separate transaction")
    void shouldRejectAStaleCompensationRateUpdateFromASeparateTransaction() {
        // given
        compensationRateId = inTransaction(() -> compensationRateRepository
                .saveAndFlush(new CompensationRateEntity(
                        null,
                        RateCategory.OVERTIME_ALLOWANCE,
                        null,
                        "Optimistic-lock fixture",
                        LocalTime.of(3, 17),
                        LocalTime.of(3, 18),
                        BigDecimal.TEN))
                .getId());
        var firstWriter = inTransaction(
                () -> compensationRateRepository.findById(compensationRateId).orElseThrow());
        var staleWriter = inTransaction(
                () -> compensationRateRepository.findById(compensationRateId).orElseThrow());

        // when
        inTransaction(() -> {
            firstWriter.updateDetails("First writer", new BigDecimal("20.0000"));
            return compensationRateRepository.saveAndFlush(firstWriter);
        });

        // then
        assertThatThrownBy(() -> inTransaction(() -> {
                    staleWriter.updateDetails("Stale writer", new BigDecimal("30.0000"));
                    return compensationRateRepository.saveAndFlush(staleWriter);
                }))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
        assertThat(inTransaction(() -> compensationRateRepository
                        .findById(compensationRateId)
                        .orElseThrow()
                        .getVersion()))
                .isEqualTo(1L);
    }

    @Test
    @DisplayName("should reject a stale incident update from a separate transaction")
    void shouldRejectAStaleIncidentUpdateFromASeparateTransaction() {
        // given
        var period = inTransaction(() -> onCallPeriodRepository.saveAndFlush(new OnCallPeriodEntity(
                null,
                LocalDateTime.of(2031, 1, 1, 0, 0),
                LocalDateTime.of(2031, 1, 2, 0, 0),
                LocalDateTime.of(2030, 12, 31, 12, 0))));
        onCallPeriodId = period.getId();
        var incidentId = inTransaction(() -> incidentRepository
                .saveAndFlush(new IncidentEntity(
                        null,
                        period,
                        "Optimistic-lock fixture",
                        LocalDateTime.of(2031, 1, 1, 10, 0),
                        LocalDateTime.of(2031, 1, 1, 11, 0),
                        LocalDateTime.of(2031, 1, 1, 9, 0)))
                .getId());
        var firstWriter =
                inTransaction(() -> incidentRepository.findById(incidentId).orElseThrow());
        var staleWriter =
                inTransaction(() -> incidentRepository.findById(incidentId).orElseThrow());

        // when
        inTransaction(() -> {
            firstWriter.updateDetails(
                    "First writer", LocalDateTime.of(2031, 1, 1, 10, 0), LocalDateTime.of(2031, 1, 1, 12, 0));
            return incidentRepository.saveAndFlush(firstWriter);
        });

        // then
        assertThatThrownBy(() -> inTransaction(() -> {
                    staleWriter.updateDetails(
                            "Stale writer", LocalDateTime.of(2031, 1, 1, 10, 0), LocalDateTime.of(2031, 1, 1, 13, 0));
                    return incidentRepository.saveAndFlush(staleWriter);
                }))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    private <T> T inTransaction(Supplier<T> action) {
        return transactions().execute(status -> action.get());
    }

    private TransactionTemplate transactions() {
        return new TransactionTemplate(transactionManager);
    }
}
