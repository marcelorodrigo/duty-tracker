package com.dutytracker.domain;

import java.time.Clock;
import java.time.ZoneId;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Codifies the business timezone for the entire application.
 * All LocalDate, LocalTime, and LocalDateTime fields are implicitly in this timezone.
 * Use this constant when converting between local and absolute time (e.g., for calendar feeds,
 * cross-timezone integrations).
 */
@Configuration
@NoArgsConstructor
public class BusinessClock {
    public static final ZoneId BUSINESS_ZONE = ZoneId.of("Europe/Amsterdam");

    @Bean
    public ZoneId getBusinessZone() {
        return BUSINESS_ZONE;
    }

    @Bean
    public Clock getClock() {
        return Clock.system(BUSINESS_ZONE);
    }
}
