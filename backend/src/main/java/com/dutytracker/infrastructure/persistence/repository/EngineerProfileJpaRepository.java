package com.dutytracker.infrastructure.persistence.repository;

import com.dutytracker.infrastructure.persistence.entity.EngineerProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EngineerProfileJpaRepository extends JpaRepository<EngineerProfileEntity, Long> {
}
