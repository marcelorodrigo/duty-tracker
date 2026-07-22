package com.github.marcelorodrigo.dutytracker.usecase.incident;

import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;

record OvertimeDayClassification(OvertimeDayType dayType, boolean fullDayOvertime) {}
