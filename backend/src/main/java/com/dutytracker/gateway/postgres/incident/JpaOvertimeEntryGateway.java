package com.dutytracker.gateway.postgres.incident;

import com.dutytracker.domain.OvertimeEntry;
import com.dutytracker.gateway.incident.OvertimeEntryGateway;
import com.dutytracker.gateway.incident.OvertimeEntryMapper;
import com.dutytracker.gateway.postgres.entity.IncidentEntity;
import com.dutytracker.gateway.postgres.entity.OvertimeEntryEntity;
import com.dutytracker.gateway.postgres.repository.OvertimeEntryJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class JpaOvertimeEntryGateway implements OvertimeEntryGateway {

    private final OvertimeEntryJpaRepository repository;
    private final OvertimeEntryMapper mapper;

    @Override
    public OvertimeEntry save(OvertimeEntry entry) {
        var entity = mapper.toEntity(entry);
        var saved = repository.save(entity);
        return mapper.toDomain(repository.findById(saved.getId()).orElseThrow());
    }

    @Override
    public List<OvertimeEntry> saveAll(List<OvertimeEntry> entries) {
        List<OvertimeEntryEntity> entities =
                entries.stream().map(mapper::toEntity).toList();
        List<OvertimeEntryEntity> saved = repository.saveAll(entities);
        return saved.stream()
                .map(entity -> repository.findById(entity.getId()).orElseThrow())
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<OvertimeEntry> findByIncidentId(Long incidentId) {
        return mapper.toDomainList(repository.findByIncidentId(incidentId));
    }

    @Override
    public Optional<OvertimeEntry> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public void deleteByIncidentId(Long incidentId) {
        List<OvertimeEntryEntity> entities = repository.findByIncidentId(incidentId);
        for (OvertimeEntryEntity entity : entities) {
            repository.deleteById(entity.getId());
        }
    }

}
