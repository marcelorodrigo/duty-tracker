package com.github.marcelorodrigo.dutytracker.gateway.postgres.repository;

import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.EngineerProfileEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EngineerProfileJpaRepository extends JpaRepository<EngineerProfileEntity, Long> {
    Optional<EngineerProfileEntity> findFirstByOrderByIdAsc();
}
