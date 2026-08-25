package com.github.marcelorodrigo.dutytracker.gateway.calendarfeed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidCalendarFeedUrlException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ResolvedIpAddressValidatorTest {

    private static final DnsResolver PUBLIC = host -> new InetAddress[] {InetAddress.getByName("93.184.216.34")};

    @Test
    @DisplayName("returns a safe public address when the host resolves to public IPs")
    void returnsPublicAddress() throws UnknownHostException {
        InetAddress target = ResolvedIpAddressValidator.resolveSafeTarget("app.incident.io", PUBLIC);

        assertThat(target.getHostAddress()).isEqualTo("93.184.216.34");
    }

    @Test
    @DisplayName("blocks a DNS rebinding attack that resolves to loopback")
    void blocksLoopbackAddress() {
        DnsResolver resolver = host -> new InetAddress[] {InetAddress.getByName("127.0.0.1")};

        assertThatThrownBy(() -> ResolvedIpAddressValidator.resolveSafeTarget("app.incident.io", resolver))
                .isInstanceOf(InvalidCalendarFeedUrlException.class)
                .hasMessageContaining("disallowed address");
    }

    @Test
    @DisplayName("blocks resolution to a link-local (cloud metadata) address")
    void blocksLinkLocalAddress() {
        DnsResolver resolver = host -> new InetAddress[] {InetAddress.getByName("169.254.169.254")};

        assertThatThrownBy(() -> ResolvedIpAddressValidator.resolveSafeTarget("app.incident.io", resolver))
                .isInstanceOf(InvalidCalendarFeedUrlException.class)
                .hasMessageContaining("disallowed address");
    }

    @Test
    @DisplayName("blocks resolution to a private site-local address")
    void blocksPrivateAddress() {
        DnsResolver resolver = host -> new InetAddress[] {InetAddress.getByName("10.0.0.1")};

        assertThatThrownBy(() -> ResolvedIpAddressValidator.resolveSafeTarget("app.incident.io", resolver))
                .isInstanceOf(InvalidCalendarFeedUrlException.class)
                .hasMessageContaining("disallowed address");
    }

    @Test
    @DisplayName("blocks the host even when only one of several resolved addresses is private")
    void blocksWhenAnyResolvedAddressIsPrivate() {
        DnsResolver resolver = host ->
                new InetAddress[] {InetAddress.getByName("93.184.216.34"), InetAddress.getByName("192.168.1.1")};

        assertThatThrownBy(() -> ResolvedIpAddressValidator.resolveSafeTarget("app.incident.io", resolver))
                .isInstanceOf(InvalidCalendarFeedUrlException.class)
                .hasMessageContaining("disallowed address");
    }

    @Test
    @DisplayName("blocks resolution to an unspecified (0.0.0.0) address")
    void blocksUnspecifiedAddress() {
        DnsResolver resolver = host -> new InetAddress[] {InetAddress.getByName("0.0.0.0")};

        assertThatThrownBy(() -> ResolvedIpAddressValidator.resolveSafeTarget("app.incident.io", resolver))
                .isInstanceOf(InvalidCalendarFeedUrlException.class)
                .hasMessageContaining("disallowed address");
    }

    @Test
    @DisplayName("blocks resolution to an IPv6 loopback address")
    void blocksIpv6LoopbackAddress() {
        DnsResolver resolver = host -> new InetAddress[] {InetAddress.getByName("::1")};

        assertThatThrownBy(() -> ResolvedIpAddressValidator.resolveSafeTarget("app.incident.io", resolver))
                .isInstanceOf(InvalidCalendarFeedUrlException.class)
                .hasMessageContaining("disallowed address");
    }

    @Test
    @DisplayName("blocks resolution to an IPv6 unique-local address")
    void blocksIpv6UniqueLocalAddress() {
        DnsResolver resolver = host -> new InetAddress[] {InetAddress.getByName("fc00::1")};

        assertThatThrownBy(() -> ResolvedIpAddressValidator.resolveSafeTarget("app.incident.io", resolver))
                .isInstanceOf(InvalidCalendarFeedUrlException.class)
                .hasMessageContaining("disallowed address");
    }

    @Test
    @DisplayName("blocks resolution to an IPv6 link-local address")
    void blocksIpv6LinkLocalAddress() {
        DnsResolver resolver = host -> new InetAddress[] {InetAddress.getByName("fe80::1")};

        assertThatThrownBy(() -> ResolvedIpAddressValidator.resolveSafeTarget("app.incident.io", resolver))
                .isInstanceOf(InvalidCalendarFeedUrlException.class)
                .hasMessageContaining("disallowed address");
    }

    @Test
    @DisplayName("propagates when the host resolves to no addresses")
    void propagatesEmptyResolution() {
        DnsResolver resolver = host -> new InetAddress[0];

        assertThatThrownBy(() -> ResolvedIpAddressValidator.resolveSafeTarget("app.incident.io", resolver))
                .isInstanceOf(UnknownHostException.class);
    }
}
