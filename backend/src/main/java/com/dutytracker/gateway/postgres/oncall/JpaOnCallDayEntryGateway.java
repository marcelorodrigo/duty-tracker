package com.dutytracker.gateway.postgres.oncall;

import com.dutytracker.domain.OnCallDayEntry;
import com.dutytracker.gateway.oncall.OnCallDayEntryGateway;
import com.dutytracker.gateway.oncall.OnCallDayEntryMapper;
import com.dutytracker.gateway.postgres.entity.OnCallDayEntryEntity;
import com.dutytracker.gateway.postgres.repository.OnCallDayEntryJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class JpaOnCallDayEntryGateway implements OnCallDayEntryGateway {

    private final OnCallDayEntryJpaRepository repository;
    private final OnCallDayEntryMapper mapper;

    @Override
    public OnCallDayEntry save(OnCallDayEntry entry) {
        OnCallDayEntryEntity entity = mapper.toEntity(entry);
        OnCallDayEntryEntity saved = repository.save(entity);
        return mapper.toDomain(repository.findById(saved.getId()).orElseThrow());
    }

    @Override
    public List<OnCallDayEntry> saveAll(List<OnCallDayEntry> entries) {
        List<OnCallDayEntryEntity> entities =
                entries.stream().map(mapper::toEntity).toList();
        List<OnCallDayEntryEntity> saved = repository.saveAll(entities);
        return saved.stream()
                .map(entity -> repository.findById(entity.getId()).orElseThrow())
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<OnCallDayEntry> findByOnCallPeriodId(Long onCallPeriodId) {
        return mapper.toDomainList(repository.findByOnCallPeriodId(onCallPeriodId));
    }

    @Override
    public Optional<OnCallDayEntry> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
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
}
