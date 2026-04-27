package com.dutytracker.infrastructure.persistence.repository;

import com.dutytracker.infrastructure.persistence.entity.UserPreferencesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferencesJpaRepository extends JpaRepository<UserPreferencesEntity, Long> {
}
