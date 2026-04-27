package com.dutytracker.gateway.postgres.oncall;

import com.dutytracker.domain.OnCallDayEntry;
import com.dutytracker.gateway.oncall.OnCallDayEntryGateway;
import com.dutytracker.gateway.postgres.entity.OnCallDayEntryEntity;
import com.dutytracker.gateway.postgres.entity.OnCallPeriodEntity;
import com.dutytracker.gateway.postgres.repository.OnCallDayEntryJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
class JpaOnCallDayEntryGateway implements OnCallDayEntryGateway {

    private final OnCallDayEntryJpaRepository repository;

    public JpaOnCallDayEntryGateway(OnCallDayEntryJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public OnCallDayEntry save(OnCallDayEntry entry) {
        OnCallDayEntryEntity entity = toEntity(entry);
        OnCallDayEntryEntity saved = repository.save(entity);
        return toDomain(repository.findById(saved.getId()).orElseThrow());
    }

    @Override
    public List<OnCallDayEntry> saveAll(List<OnCallDayEntry> entries) {
        List<OnCallDayEntryEntity> entities =
                entries.stream().map(this::toEntity).toList();
        List<OnCallDayEntryEntity> saved = repository.saveAll(entities);
        return saved.stream()
                .map(entity -> repository.findById(entity.getId()).orElseThrow())
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<OnCallDayEntry> findByOnCallPeriodId(Long onCallPeriodId) {
        return toDomainList(repository.findByOnCallPeriodId(onCallPeriodId));
    }

    @Override
    public Optional<OnCallDayEntry> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public void deleteByOnCallPeriodId(Long onCallPeriodId) {
        List<OnCallDayEntryEntity> entities = repository.findByOnCallPeriodId(onCallPeriodId);
        for (OnCallDayEntryEntity entity : entities) {
            repository.deleteById(entity.getId());
        }
    }

    private OnCallDayEntryEntity toEntity(OnCallDayEntry domain) {
        OnCallPeriodEntity onCallPeriod = new OnCallPeriodEntity(domain.onCallPeriodId(), null, null);
        return new OnCallDayEntryEntity(
                domain.id(),
                onCallPeriod,
                domain.date(),
                domain.hours(),
                domain.rateType(),
                domain.capped(),
                domain.timeForTimeFlag(),
                domain.manualOverride());
    }

    private OnCallDayEntry toDomain(OnCallDayEntryEntity entity) {
        return new OnCallDayEntry(
                entity.getId(),
                entity.getOnCallPeriod().getId(),
                entity.getDate(),
                entity.getHours(),
                entity.getRateType(),
                entity.isCapped(),
                entity.isTimeForTimeFlag(),
                entity.isManualOverride());
    }

    private List<OnCallDayEntry> toDomainList(List<OnCallDayEntryEntity> entities) {
        return entities.stream().map(this::toDomain).toList();
    }
}
