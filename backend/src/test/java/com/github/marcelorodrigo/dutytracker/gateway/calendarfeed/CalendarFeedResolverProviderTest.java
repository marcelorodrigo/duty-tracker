package com.github.marcelorodrigo.dutytracker.gateway.calendarfeed;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.net.spi.InetAddressResolver;
import java.net.spi.InetAddressResolverProvider;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import java.util.stream.Stream;
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

    @Test
    @DisplayName("builds a feed resolver wrapping the built-in resolver")
    void buildsFeedResolver() throws Exception {
        Constructor<?> ctor = Class.forName("sun.net.ResolverProviderConfiguration")
                .getConstructor(InetAddressResolver.class, Supplier.class);
        InetAddressResolver builtin = new InetAddressResolver() {
            @Override
            public Stream<java.net.InetAddress> lookupByName(
                    String host, java.net.spi.InetAddressResolver.LookupPolicy mode) {
                return Stream.of();
            }

            @Override
            public String lookupByAddress(byte[] addr) {
                return null;
            }
        };
        Object configuration = ctor.newInstance(builtin, (Supplier<String>) () -> "localhost");

        InetAddressResolverProvider provider = new CalendarFeedResolverProvider();
        InetAddressResolver resolver = provider.get((InetAddressResolverProvider.Configuration) configuration);

        assertThat(resolver).isInstanceOf(CalendarFeedInetAddressResolver.class);
    }
}
