package com.github.marcelorodrigo.dutytracker.gateway.profile;

import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import java.util.Optional;

public interface EngineerProfileGateway {
    EngineerProfile save(EngineerProfile profile);

    Optional<EngineerProfile> find();

    void deleteAll();

    void deleteById(Long id);
}
