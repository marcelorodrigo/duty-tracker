package com.dutytracker.gateway.profile;

import com.dutytracker.domain.EngineerProfile;
import java.util.Optional;

public interface EngineerProfileGateway {
    EngineerProfile save(EngineerProfile profile);

    Optional<EngineerProfile> find();

    void deleteAll();

    void deleteById(Long id);
}
