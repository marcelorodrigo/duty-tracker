package com.dutytracker.gateway.postgres.oncall;

import com.dutytracker.domain.HolidayOverride;
import com.dutytracker.gateway.oncall.HolidayOverrideGateway;
import com.dutytracker.gateway.oncall.HolidayOverrideMapper;
import com.dutytracker.gateway.postgres.entity.HolidayOverrideEntity;
import com.dutytracker.gateway.postgres.repository.HolidayOverrideJpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class JpaHolidayOverrideGateway implements HolidayOverrideGateway {

    private final HolidayOverrideJpaRepository repository;
    private final HolidayOverrideMapper mapper;

    @Override
    public HolidayOverride save(HolidayOverride override) {
        HolidayOverrideEntity entity = mapper.toEntity(override);
        HolidayOverrideEntity saved = repository.save(entity);
        return mapper.toDomain(repository.findById(saved.getId()).orElseThrow());
    }

    @Override
    public List<HolidayOverride> findByOnCallPeriodId(Long onCallPeriodId) {
        return mapper.toDomainList(repository.findByOnCallPeriodId(onCallPeriodId));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<HolidayOverride> findByOnCallPeriodIdAndDate(Long onCallPeriodId, LocalDate date) {
        return repository.findByOnCallPeriodIdAndDate(onCallPeriodId, date).map(mapper::toDomain);
    }
}
