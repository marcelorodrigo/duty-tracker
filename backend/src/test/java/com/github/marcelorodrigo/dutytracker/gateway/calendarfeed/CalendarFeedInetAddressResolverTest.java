package com.github.marcelorodrigo.dutytracker.gateway.calendarfeed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.spi.InetAddressResolver;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CalendarFeedInetAddressResolverTest {

    @Mock
    private InetAddressResolver delegate;

    private CalendarFeedInetAddressResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new CalendarFeedInetAddressResolver(delegate, "app.incident.io");
    }

    @Test
    @DisplayName("returns the resolved public addresses for the allowed host")
    void returnsPublicAddressesForAllowedHost() throws UnknownHostException {
        InetAddress publicIp = InetAddress.getByName("93.184.216.34");
        when(delegate.lookupByName(eq("app.incident.io"), any())).thenReturn(Stream.of(publicIp));

        assertThat(resolver.lookupByName("app.incident.io", null)).containsExactly(publicIp);
    }

    @Test
    @DisplayName("blocks a DNS rebinding attack that resolves the allowed host to a private address")
    void blocksRebindingToPrivateAddress() throws UnknownHostException {
        when(delegate.lookupByName(eq("app.incident.io"), any()))
                .thenReturn(Stream.of(InetAddress.getByName("127.0.0.1")));

        assertThatThrownBy(() -> resolver.lookupByName("app.incident.io", null))
                .isInstanceOf(UnknownHostException.class)
                .hasMessageContaining("disallowed address");
    }

    @Test
    @DisplayName("blocks the allowed host when any of several addresses is not globally routable")
    void blocksWhenAnyAddressIsPrivate() throws UnknownHostException {
        when(delegate.lookupByName(eq("app.incident.io"), any()))
                .thenReturn(Stream.of(InetAddress.getByName("93.184.216.34"), InetAddress.getByName("10.0.0.1")));

        assertThatThrownBy(() -> resolver.lookupByName("app.incident.io", null))
                .isInstanceOf(UnknownHostException.class)
                .hasMessageContaining("disallowed address");
    }

    @Test
    @DisplayName("delegates resolutions for unrelated hosts without validation")
    void delegatesOtherHosts() throws UnknownHostException {
        InetAddress ip = InetAddress.getByName("10.0.0.5");
        when(delegate.lookupByName(anyString(), any())).thenReturn(Stream.of(ip));

        assertThat(resolver.lookupByName("intranet.example.com", null)).containsExactly(ip);
    }

    @Test
    @DisplayName("propagates failures from the delegate")
    void propagatesDelegateFailure() throws UnknownHostException {
        when(delegate.lookupByName(eq("app.incident.io"), any())).thenThrow(new UnknownHostException("boom"));

        assertThatThrownBy(() -> resolver.lookupByName("app.incident.io", null))
                .isInstanceOf(UnknownHostException.class)
                .hasMessageContaining("boom");
    }

    @Test
    @DisplayName("matches the allowed host case-insensitively")
    void matchesAllowedHostCaseInsensitively() throws UnknownHostException {
        InetAddress publicIp = InetAddress.getByName("93.184.216.34");
        when(delegate.lookupByName(anyString(), any())).thenReturn(Stream.of(publicIp));

        assertThat(resolver.lookupByName("APP.INCIDENT.IO", null)).containsExactly(publicIp);
    }

    @Test
    @DisplayName("delegates reverse lookups by address without validation")
    void delegatesLookupByAddress() throws UnknownHostException {
        when(delegate.lookupByAddress(any(byte[].class))).thenReturn("resolved.example.com");

        assertThat(resolver.lookupByAddress(new byte[] {93, (byte) 184, (byte) 216, 34}))
                .isEqualTo("resolved.example.com");
    }
}
