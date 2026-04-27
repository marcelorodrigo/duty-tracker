package com.dutytracker.gateway.postgres.oncall;

import com.dutytracker.domain.OnCallPeriod;
import com.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.dutytracker.gateway.oncall.OnCallPeriodMapper;
import com.dutytracker.gateway.postgres.entity.OnCallPeriodEntity;
import com.dutytracker.gateway.postgres.repository.OnCallPeriodJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class JpaOnCallPeriodGateway implements OnCallPeriodGateway {

    private final OnCallPeriodJpaRepository repository;
    private final OnCallPeriodMapper mapper;

    @Override
    public OnCallPeriod save(OnCallPeriod period) {
        OnCallPeriodEntity entity = mapper.toEntity(period);
        OnCallPeriodEntity saved = repository.save(entity);
        return mapper.toDomain(repository.findById(saved.getId()).orElseThrow());
    }

    @Override
    public Optional<OnCallPeriod> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<OnCallPeriod> findAll() {
        return mapper.toDomainList(repository.findAll());
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private OnCallPeriodEntity toEntity(OnCallPeriod domain) {
        return new OnCallPeriodEntity(domain.id(), domain.startDateTime(), domain.endDateTime());
    }

    private OnCallPeriod toDomain(OnCallPeriodEntity entity) {
        return new OnCallPeriod(
                entity.getId(), entity.getStartDateTime(), entity.getEndDateTime(), entity.getCreatedAt());
    }

    private List<OnCallPeriod> toDomainList(List<OnCallPeriodEntity> entities) {
        return entities.stream().map(this::toDomain).toList();
    }
}
