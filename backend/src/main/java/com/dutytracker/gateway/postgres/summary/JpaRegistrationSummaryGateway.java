package com.dutytracker.gateway.postgres.summary;

import com.dutytracker.domain.RegistrationSummary;
import com.dutytracker.gateway.postgres.entity.RegistrationSummaryEntity;
import com.dutytracker.gateway.postgres.repository.RegistrationSummaryJpaRepository;
import com.dutytracker.gateway.summary.RegistrationSummaryGateway;
import com.dutytracker.gateway.summary.RegistrationSummaryMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class JpaRegistrationSummaryGateway implements RegistrationSummaryGateway {

    private final RegistrationSummaryJpaRepository repository;
    private final RegistrationSummaryMapper mapper;

    @Override
    public RegistrationSummary save(RegistrationSummary summary) {
        RegistrationSummaryEntity entity = mapper.toEntity(summary);
        RegistrationSummaryEntity saved = repository.save(entity);
        return mapper.toDomain(repository.findById(saved.getId()).orElseThrow());
    }

    @Override
    public Optional<RegistrationSummary> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<RegistrationSummary> findAll() {
        return mapper.toDomainList(repository.findAll());
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsAny() {
        return repository.count() > 0;
    }
}
