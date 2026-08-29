package com.github.marcelorodrigo.dutytracker.gateway.postgres.incident;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentMapper;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.PaginationMapper;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.IncidentJpaRepository;
import com.github.marcelorodrigo.dutytracker.usecase.request.PaginationRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
        return mapper.toDomainList(repository.findByOnCallPeriodIdOrderByStartDateTime(onCallPeriodId));
    }

    @Override
    public Page<Incident> findByOnCallPeriodId(Long onCallPeriodId, PaginationRequest pagination) {
        return repository
                .findByOnCallPeriodId(onCallPeriodId, PaginationMapper.toPageRequest(pagination))
                .map(mapper::toDomain);
    }

    @Override
    public Page<Incident> findAll(PaginationRequest pagination) {
        return repository.findAll(PaginationMapper.toPageRequest(pagination)).map(mapper::toDomain);
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
