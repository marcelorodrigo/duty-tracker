package com.github.marcelorodrigo.dutytracker.gateway.postgres.compensation;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.DuplicateCompensationRateException;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationMapper;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateGateway;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.ConstraintViolationDetector;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.CompensationRateJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class JpaCompensationRateGateway implements CompensationRateGateway {

    private static final String COMPENSATION_RATE_UNIQUE_CONSTRAINT = "uq_compensation_rate";

    private final CompensationRateJpaRepository repository;
    private final CompensationMapper mapper;

    @Override
    public CompensationRate save(CompensationRate rate) {
        var entity = mapper.toEntity(rate);
        try {
            var saved = repository.saveAndFlush(entity);
            return mapper.toDomain(saved);
        } catch (DataIntegrityViolationException exception) {
            if (ConstraintViolationDetector.causedBy(exception, COMPENSATION_RATE_UNIQUE_CONSTRAINT)) {
                throw new DuplicateCompensationRateException("A compensation rate already exists for this time window");
            }
            throw exception;
        }
    }

    @Override
    public List<CompensationRate> findAll() {
        return mapper.toDomainList(repository.findAll());
    }

    @Override
    public List<CompensationRate> findByRateCategory(RateCategory rateCategory) {
        return mapper.toDomainList(repository.findByRateCategory(rateCategory));
    }

    @Override
    public List<CompensationRate> findByRateCategoryAndOvertimeDayType(
            RateCategory rateCategory, OvertimeDayType overtimeDayType) {
        return mapper.toDomainList(repository.findByRateCategoryAndOvertimeDayType(rateCategory, overtimeDayType));
    }

    @Override
    public CompensationRate update(CompensationRate rate) {
        var entity = mapper.toEntity(rate);
        var updated = repository.save(entity);
        return mapper.toDomain(updated);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<CompensationRate> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }
}
