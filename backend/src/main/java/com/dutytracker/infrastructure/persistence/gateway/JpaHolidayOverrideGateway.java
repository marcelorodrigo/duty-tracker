package com.dutytracker.infrastructure.persistence.gateway;

import com.dutytracker.domain.gateway.HolidayOverrideGateway;
import com.dutytracker.domain.model.HolidayOverride;
import com.dutytracker.infrastructure.persistence.entity.HolidayOverrideEntity;
import com.dutytracker.infrastructure.persistence.entity.OnCallPeriodEntity;
import com.dutytracker.infrastructure.persistence.repository.HolidayOverrideJpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
class JpaHolidayOverrideGateway implements HolidayOverrideGateway {

    private final HolidayOverrideJpaRepository repository;

    public JpaHolidayOverrideGateway(HolidayOverrideJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public HolidayOverride save(HolidayOverride override) {
        HolidayOverrideEntity entity = toEntity(override);
        HolidayOverrideEntity saved = repository.save(entity);
        return toDomain(repository.findById(saved.getId()).orElseThrow());
    }

    @Override
    public List<HolidayOverride> findByOnCallPeriodId(Long onCallPeriodId) {
        return toDomainList(repository.findByOnCallPeriodId(onCallPeriodId));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<HolidayOverride> findByOnCallPeriodIdAndDate(Long onCallPeriodId, LocalDate date) {
        return repository.findByOnCallPeriodIdAndDate(onCallPeriodId, date).map(this::toDomain);
    }

    private HolidayOverrideEntity toEntity(HolidayOverride domain) {
        OnCallPeriodEntity onCallPeriod = new OnCallPeriodEntity(domain.onCallPeriodId(), null, null, null);
        return new HolidayOverrideEntity(
                domain.id(),
                onCallPeriod,
                domain.date()
        );
    }

    private HolidayOverride toDomain(HolidayOverrideEntity entity) {
        return new HolidayOverride(
                entity.getId(),
                entity.getOnCallPeriod().getId(),
                entity.getDate()
        );
    }

    private List<HolidayOverride> toDomainList(List<HolidayOverrideEntity> entities) {
        return entities.stream().map(this::toDomain).toList();
    }
}
