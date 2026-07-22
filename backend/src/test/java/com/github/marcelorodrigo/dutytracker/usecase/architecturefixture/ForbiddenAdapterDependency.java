package com.github.marcelorodrigo.dutytracker.usecase.architecturefixture;

import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.IncidentEntity;

public interface ForbiddenAdapterDependency {

    IncidentEntity adapter();
}
