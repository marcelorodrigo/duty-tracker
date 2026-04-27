package com.dutytracker.gateway.postgres.incident;

import com.dutytracker.domain.Incident;
import com.dutytracker.gateway.incident.IncidentGateway;
import com.dutytracker.gateway.postgres.entity.IncidentEntity;
import com.dutytracker.gateway.postgres.entity.OnCallPeriodEntity;
import com.dutytracker.gateway.postgres.repository.IncidentJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
class JpaIncidentGateway implements IncidentGateway {

    private final IncidentJpaRepository repository;

    public JpaIncidentGateway(IncidentJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Incident save(Incident incident) {
        IncidentEntity entity = toEntity(incident);
        IncidentEntity saved = repository.save(entity);
        return toDomain(repository.findById(saved.getId()).orElseThrow());
    }

    @Override
    public Optional<Incident> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Incident> findByOnCallPeriodId(Long onCallPeriodId) {
        return toDomainList(repository.findByOnCallPeriodId(onCallPeriodId));
    }

    @Override
    public List<Incident> findAll() {
        return toDomainList(repository.findAll());
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private IncidentEntity toEntity(Incident domain) {
        OnCallPeriodEntity onCallPeriod = domain.onCallPeriodId() == null
                ? null
                : new OnCallPeriodEntity(domain.onCallPeriodId(), null, null, null);
        return new IncidentEntity(
                domain.id(), onCallPeriod, domain.date(), domain.startTime(), domain.endTime(), domain.createdAt());
    }

    private Incident toDomain(IncidentEntity entity) {
        return new Incident(
                entity.getId(),
                entity.getOnCallPeriod() == null
                        ? null
                        : entity.getOnCallPeriod().getId(),
                entity.getDate(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getCreatedAt());
    }

    private List<Incident> toDomainList(List<IncidentEntity> entities) {
        return entities.stream().map(this::toDomain).toList();
    }
}
