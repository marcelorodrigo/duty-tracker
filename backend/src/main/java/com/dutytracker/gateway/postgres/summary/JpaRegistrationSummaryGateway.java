package com.dutytracker.gateway.postgres.summary;

import com.dutytracker.gateway.summary.RegistrationSummaryGateway;
import com.dutytracker.domain.RegistrationSummary;
import com.dutytracker.gateway.postgres.entity.RegistrationSummaryEntity;
import com.dutytracker.gateway.postgres.repository.RegistrationSummaryJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
class JpaRegistrationSummaryGateway implements RegistrationSummaryGateway {

    private final RegistrationSummaryJpaRepository repository;

    public JpaRegistrationSummaryGateway(RegistrationSummaryJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public RegistrationSummary save(RegistrationSummary summary) {
        RegistrationSummaryEntity entity = toEntity(summary);
        RegistrationSummaryEntity saved = repository.save(entity);
        return toDomain(repository.findById(saved.getId()).orElseThrow());
    }

    @Override
    public Optional<RegistrationSummary> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<RegistrationSummary> findAll() {
        return toDomainList(repository.findAll());
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsAny() {
        return repository.count() > 0;
    }

    private RegistrationSummaryEntity toEntity(RegistrationSummary domain) {
        return new RegistrationSummaryEntity(
                domain.id(),
                domain.label(),
                domain.periodStart(),
                domain.periodEnd(),
                domain.createdAt(),
                domain.updatedAt()
        );
    }

    private RegistrationSummary toDomain(RegistrationSummaryEntity entity) {
        return new RegistrationSummary(
                entity.getId(),
                entity.getLabel(),
                entity.getPeriodStart(),
                entity.getPeriodEnd(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private List<RegistrationSummary> toDomainList(List<RegistrationSummaryEntity> entities) {
        return entities.stream().map(this::toDomain).toList();
    }
}
