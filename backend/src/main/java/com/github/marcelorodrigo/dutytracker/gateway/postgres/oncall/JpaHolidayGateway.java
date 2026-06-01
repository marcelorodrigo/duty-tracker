package com.github.marcelorodrigo.dutytracker.gateway.postgres.oncall;

import com.github.marcelorodrigo.dutytracker.domain.Holiday;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayMapper;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.HolidayJpaRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class JpaHolidayGateway implements HolidayGateway {

    private final HolidayJpaRepository repository;
    private final HolidayMapper mapper;

    @Override
    public Holiday save(Holiday holiday) {
        var entity = mapper.toEntity(holiday);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<Holiday> saveAll(List<Holiday> holidays) {
        var entities = holidays.stream().map(mapper::toEntity).toList();
        var saved = repository.saveAll(entities);
        return mapper.toDomainList(saved);
    }

    @Override
    public List<Holiday> findByOnCallPeriodId(Long onCallPeriodId) {
        return mapper.toDomainList(repository.findByOnCallPeriodId(onCallPeriodId));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public void deleteByOnCallPeriodId(Long onCallPeriodId) {
        repository.deleteByOnCallPeriodId(onCallPeriodId);
    }

    @Override
    public void deleteOutOfRange(Long periodId, LocalDate start, LocalDate end) {
        repository.deleteOutOfRange(periodId, start, end);
    }
}
