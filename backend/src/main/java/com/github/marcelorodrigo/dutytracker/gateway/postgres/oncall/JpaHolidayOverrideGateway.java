package com.github.marcelorodrigo.dutytracker.gateway.postgres.oncall;

import com.github.marcelorodrigo.dutytracker.domain.HolidayOverride;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayOverrideGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayOverrideMapper;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.HolidayOverrideJpaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class JpaHolidayOverrideGateway implements HolidayOverrideGateway {

    private final HolidayOverrideJpaRepository repository;
    private final HolidayOverrideMapper mapper;

    @Override
    public HolidayOverride save(HolidayOverride override) {
        var entity = mapper.toEntity(override);
        var saved = repository.save(entity);
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
}
