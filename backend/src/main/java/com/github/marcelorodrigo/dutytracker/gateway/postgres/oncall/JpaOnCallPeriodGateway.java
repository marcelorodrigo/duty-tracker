package com.github.marcelorodrigo.dutytracker.gateway.postgres.oncall;

import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodMapper;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.OnCallPeriodJpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class JpaOnCallPeriodGateway implements OnCallPeriodGateway {

    private final OnCallPeriodJpaRepository repository;
    private final OnCallPeriodMapper mapper;

    @Override
    public OnCallPeriod save(OnCallPeriod period) {
        var entity = mapper.toEntity(period);
        var saved = repository.save(entity);
        return mapper.toDomain(repository.findById(saved.getId()).orElseThrow());
    }

    @Override
    public Optional<OnCallPeriod> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<OnCallPeriod> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDomain);
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
