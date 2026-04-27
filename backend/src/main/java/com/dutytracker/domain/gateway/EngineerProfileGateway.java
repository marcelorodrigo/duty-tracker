package com.dutytracker.domain.gateway;

import com.dutytracker.domain.model.EngineerProfile;

import java.util.Optional;

public interface EngineerProfileGateway {
    EngineerProfile save(EngineerProfile profile);
    Optional<EngineerProfile> find();
}
