package com.github.marcelorodrigo.dutytracker.gateway.calendarfeed;

import java.net.spi.InetAddressResolver;
import java.net.spi.InetAddressResolverProvider;

/**
 * Installs {@link CalendarFeedInetAddressResolver} as the JVM's address resolver. Registered via
 * {@code META-INF/services/java.net.spi.InetAddressResolverProvider}. Resolutions for the calendar
 * feed host are validated against private/special-purpose ranges; all other hosts are handled by the
 * platform default resolver.
 */
public final class CalendarFeedResolverProvider extends InetAddressResolverProvider {

    static final String ALLOWED_HOST = "app.incident.io";

    @Override
    public InetAddressResolver get(Configuration configuration) {
        return new CalendarFeedInetAddressResolver(configuration.builtinResolver(), ALLOWED_HOST);
    }

    @Override
    public String name() {
        return "duty-tracker-calendar-feed";
    }
}
