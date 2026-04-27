package com.dutytracker.gateway.postgres.compensation;

import com.dutytracker.domain.CompensationRate;
import com.dutytracker.domain.EmployeeType;
import com.dutytracker.gateway.compensation.CompensationMapper;
import com.dutytracker.gateway.compensation.CompensationRateGateway;
import com.dutytracker.gateway.postgres.entity.CompensationRateEntity;
import com.dutytracker.gateway.postgres.repository.CompensationRateJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class JpaCompensationRateGateway implements CompensationRateGateway {

    private final CompensationRateJpaRepository repository;
    private final CompensationMapper mapper;

    @Override
    public List<CompensationRate> saveAll(List<CompensationRate> rates) {
        List<CompensationRateEntity> entities =
                rates.stream().map(mapper::toEntity).toList();
        List<CompensationRateEntity> saved = repository.saveAll(entities);
        return saved.stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<CompensationRate> findAll() {
        return mapper.toDomainList(repository.findAll());
    }

    @Override
    public List<CompensationRate> findByEmployeeType(EmployeeType employeeType) {
        return mapper.toDomainList(repository.findByEmployeeType(employeeType));
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
