package com.dutytracker.gateway.postgres.incident;

import com.dutytracker.domain.OvertimeEntry;
import com.dutytracker.gateway.incident.OvertimeEntryGateway;
import com.dutytracker.gateway.postgres.entity.IncidentEntity;
import com.dutytracker.gateway.postgres.entity.OvertimeEntryEntity;
import com.dutytracker.gateway.postgres.repository.OvertimeEntryJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
class JpaOvertimeEntryGateway implements OvertimeEntryGateway {

    private final OvertimeEntryJpaRepository repository;

    public JpaOvertimeEntryGateway(OvertimeEntryJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public OvertimeEntry save(OvertimeEntry entry) {
        OvertimeEntryEntity entity = toEntity(entry);
        OvertimeEntryEntity saved = repository.save(entity);
        return toDomain(repository.findById(saved.getId()).orElseThrow());
    }

    @Override
    public List<OvertimeEntry> saveAll(List<OvertimeEntry> entries) {
        List<OvertimeEntryEntity> entities =
                entries.stream().map(this::toEntity).toList();
        List<OvertimeEntryEntity> saved = repository.saveAll(entities);
        return saved.stream()
                .map(entity -> repository.findById(entity.getId()).orElseThrow())
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<OvertimeEntry> findByIncidentId(Long incidentId) {
        return toDomainList(repository.findByIncidentId(incidentId));
    }

    @Override
    public Optional<OvertimeEntry> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
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

    private OvertimeEntryEntity toEntity(OvertimeEntry domain) {
        IncidentEntity incident = new IncidentEntity(domain.incidentId(), null, null, null, null);
        return new OvertimeEntryEntity(
                domain.id(),
                incident,
                domain.overtimeHours(),
                domain.allowanceHours(),
                domain.allowancePercentage(),
                domain.timeFrom(),
                domain.timeTo(),
                domain.isAllowanceEntry(),
                domain.manualOverride());
    }

    private OvertimeEntry toDomain(OvertimeEntryEntity entity) {
        return new OvertimeEntry(
                entity.getId(),
                entity.getIncident().getId(),
                entity.getOvertimeHours(),
                entity.getAllowanceHours(),
                entity.getAllowancePercentage(),
                entity.getTimeFrom(),
                entity.getTimeTo(),
                entity.isAllowanceEntry(),
                entity.isManualOverride());
    }

    private List<OvertimeEntry> toDomainList(List<OvertimeEntryEntity> entities) {
        return entities.stream().map(this::toDomain).toList();
    }
}
