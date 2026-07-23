package com.github.marcelorodrigo.dutytracker.gateway.postgres.oncall;

import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodMapper;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.OnCallPeriodJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
class JpaOnCallPeriodGateway implements OnCallPeriodGateway {

    private final OnCallPeriodJpaRepository repository;
    private final OnCallPeriodMapper mapper;

    @Override
    @Transactional
    public OnCallPeriod save(OnCallPeriod period) {
        var entity = period.id() == null
                ? mapper.toEntity(period)
                : repository
                        .findById(period.id())
                        .map(existing -> {
                            existing.reschedule(period.startDateTime(), period.endDateTime());
                            return existing;
                        })
                        .orElseGet(() -> mapper.toEntity(period));
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<OnCallPeriod> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<OnCallPeriod> findAll() {
        return mapper.toDomainList(repository.findAll(Sort.by("startDateTime").descending()));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsOverlapping(LocalDateTime start, LocalDateTime end, Long excludeId) {
        return repository.existsOverlapping(start, end, excludeId);
    }
}
