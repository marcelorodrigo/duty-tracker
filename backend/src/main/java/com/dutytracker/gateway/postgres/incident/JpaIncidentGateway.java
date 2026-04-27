package com.dutytracker.gateway.postgres.incident;

import com.dutytracker.domain.Incident;
import com.dutytracker.gateway.incident.IncidentGateway;
import com.dutytracker.gateway.incident.IncidentMapper;
import com.dutytracker.gateway.postgres.entity.IncidentEntity;
import com.dutytracker.gateway.postgres.entity.OnCallPeriodEntity;
import com.dutytracker.gateway.postgres.repository.IncidentJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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
        return mapper.toDomain(repository.findById(saved.getId()).orElseThrow());
    }

    @Override
    public Optional<Incident> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Incident> findByOnCallPeriodId(Long onCallPeriodId) {
        return mapper.toDomainList(repository.findByOnCallPeriodId(onCallPeriodId));
    }

    @Override
    public List<Incident> findAll() {
        return mapper.toDomainList(repository.findAll());
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

}
