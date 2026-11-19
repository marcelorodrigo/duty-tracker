package com.github.marcelorodrigo.dutytracker.gateway.postgres.oncall;

import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodMapper;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.PaginationMapper;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.OnCallPeriodJpaRepository;
import com.github.marcelorodrigo.dutytracker.usecase.request.PaginationRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.PageResult;
import java.time.LocalDateTime;
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
        var entity = mapper.toEntity(period);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<OnCallPeriod> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public PageResult<OnCallPeriod> findAll(PaginationRequest pagination) {
        return PaginationMapper.toPageResult(
                repository.findAll(PaginationMapper.toPageRequest(pagination)).map(mapper::toDomain));
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
