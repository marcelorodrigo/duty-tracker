package com.github.marcelorodrigo.dutytracker.gateway.calendarfeed;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.spi.InetAddressResolverProvider;
import java.util.ServiceLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CalendarFeedResolverProviderTest {

    @Test
    @DisplayName("is registered as the address resolver provider and reports its name")
    void isRegisteredAsResolverProvider() {
        ServiceLoader<InetAddressResolverProvider> loader = ServiceLoader.load(InetAddressResolverProvider.class);
        boolean registered = false;
        for (InetAddressResolverProvider provider : loader) {
            if (provider instanceof CalendarFeedResolverProvider feedProvider) {
                assertThat(feedProvider.name()).isEqualTo("duty-tracker-calendar-feed");
                registered = true;
            }
        }
        assertThat(registered).isTrue();
    }
}
