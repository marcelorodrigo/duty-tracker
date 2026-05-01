package com.github.marcelorodrigo.dutytracker.gateway.postgres.incident;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentMapper;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.IncidentJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class JpaIncidentGateway implements IncidentGateway {

    private final IncidentJpaRepository repository;
    private final IncidentMapper mapper;

    @Override
    public Incident save(Incident incident) {
        var entity = mapper.toEntity(incident);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Incident> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Incident> findByOnCallPeriodId(Long onCallPeriodId) {
        return mapper.toDomainList(repository.findByOnCallPeriodId(
                onCallPeriodId, Sort.by("startTime").ascending()));
    }

    @Override
    public List<Incident> findAll() {
        return mapper.toDomainList(repository.findAll(Sort.by("startTime").ascending()));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
