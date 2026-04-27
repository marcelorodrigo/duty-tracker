package com.dutytracker.gateway.postgres.compensation;

import com.dutytracker.gateway.compensation.CompensationRateGateway;
import com.dutytracker.domain.CompensationRate;
import com.dutytracker.domain.EmployeeType;
import com.dutytracker.gateway.postgres.entity.CompensationRateEntity;
import com.dutytracker.gateway.postgres.repository.CompensationRateJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
class JpaCompensationRateGateway implements CompensationRateGateway {

    private final CompensationRateJpaRepository repository;

    public JpaCompensationRateGateway(CompensationRateJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<CompensationRate> saveAll(List<CompensationRate> rates) {
        List<CompensationRateEntity> entities = rates.stream().map(this::toEntity).toList();
        List<CompensationRateEntity> saved = repository.saveAll(entities);
        return saved.stream()
                .map(entity -> repository.findById(entity.getId()).orElseThrow())
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<CompensationRate> findAll() {
        return toDomainList(repository.findAll());
    }

    @Override
    public List<CompensationRate> findByEmployeeType(EmployeeType employeeType) {
        return toDomainList(repository.findByEmployeeType(employeeType));
    }

    @Override
    public CompensationRate update(CompensationRate rate) {
        CompensationRateEntity entity = toEntity(rate);
        CompensationRateEntity updated = repository.save(entity);
        return toDomain(repository.findById(updated.getId()).orElseThrow());
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<CompensationRate> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    private CompensationRateEntity toEntity(CompensationRate domain) {
        return new CompensationRateEntity(
                domain.id(),
                domain.employeeType(),
                domain.rateCategory(),
                domain.label(),
                domain.timeFrom(),
                domain.timeTo(),
                domain.percentage()
        );
    }

    private CompensationRate toDomain(CompensationRateEntity entity) {
        return new CompensationRate(
                entity.getId(),
                entity.getEmployeeType(),
                entity.getRateCategory(),
                entity.getLabel(),
                entity.getTimeFrom(),
                entity.getTimeTo(),
                entity.getPercentage()
        );
    }

    private List<CompensationRate> toDomainList(List<CompensationRateEntity> entities) {
        return entities.stream().map(this::toDomain).toList();
    }
}
