package com.github.marcelorodrigo.dutytracker.gateway.postgres.incident;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentMapper;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.IncidentJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
class JpaIncidentGateway implements IncidentGateway {

    private final IncidentJpaRepository repository;
    private final IncidentMapper mapper;

    @Override
    @Transactional
    public Incident save(Incident incident) {
        var entity = incident.id() == null
                ? mapper.toEntity(incident)
                : repository
                        .findById(incident.id())
                        .map(existing -> {
                            existing.updateDetails(incident.name(), incident.startDateTime(), incident.endDateTime());
                            return existing;
                        })
                        .orElseGet(() -> mapper.toEntity(incident));
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Incident> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Incident> findByOnCallPeriodId(Long onCallPeriodId) {
        return mapper.toDomainList(repository.findByOnCallPeriodIdOrderByStartDateTime(onCallPeriodId));
    }

    @Override
    public List<Incident> findAll() {
        return mapper.toDomainList(repository.findAllByOrderByStartDateTime());
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsOverlapping(Long onCallPeriodId, LocalDateTime start, LocalDateTime end, Long excludeId) {
        return repository.existsOverlapping(onCallPeriodId, start, end, excludeId);
    }
}
