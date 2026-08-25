package com.github.marcelorodrigo.dutytracker.gateway.calendarfeed;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ResolvedIpAddressValidatorTest {

    @Test
    @DisplayName("accepts a public IPv4 address as globally routable")
    void acceptsPublicIpv4() throws Exception {
        assertThat(ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(InetAddress.getByName("93.184.216.34")))
                .isFalse();
    }

    @Test
    @DisplayName("accepts a public IPv6 address as globally routable")
    void acceptsPublicIpv6() throws Exception {
        assertThat(ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(
                        InetAddress.getByName("2606:4700:4700::1111")))
                .isFalse();
    }

    @Test
    @DisplayName("rejects loopback addresses")
    void rejectsLoopback() throws Exception {
        assertThat(ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(InetAddress.getByName("127.0.0.1")))
                .isTrue();
        assertThat(ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(InetAddress.getByName("::1")))
                .isTrue();
    }

    @Test
    @DisplayName("rejects private IPv4 ranges")
    void rejectsPrivateIpv4() throws Exception {
        assertThat(ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(InetAddress.getByName("10.0.0.1")))
                .isTrue();
        assertThat(ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(InetAddress.getByName("172.16.0.1")))
                .isTrue();
        assertThat(ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(InetAddress.getByName("192.168.1.1")))
                .isTrue();
    }

    @Test
    @DisplayName("rejects link-local addresses including cloud metadata ranges")
    void rejectsLinkLocal() throws Exception {
        assertThat(ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(InetAddress.getByName("169.254.169.254")))
                .isTrue();
        assertThat(ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(InetAddress.getByName("fe80::1")))
                .isTrue();
    }

    @Test
    @DisplayName("rejects carrier-grade NAT (100.64.0.0/10)")
    void rejectsCarrierGradeNat() throws Exception {
        assertThat(ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(InetAddress.getByName("100.64.0.1")))
                .isTrue();
        assertThat(ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(InetAddress.getByName("100.127.255.254")))
                .isTrue();
    }

    @Test
    @DisplayName("rejects benchmarking (198.18.0.0/15)")
    void rejectsBenchmarking() throws Exception {
        assertThat(ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(InetAddress.getByName("198.18.0.1")))
                .isTrue();
        assertThat(ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(InetAddress.getByName("198.19.255.254")))
                .isTrue();
    }

    @Test
    @DisplayName("rejects documentation networks")
    void rejectsDocumentationRanges() throws Exception {
        assertThat(ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(InetAddress.getByName("192.0.2.1")))
                .isTrue();
        assertThat(ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(InetAddress.getByName("198.51.100.1")))
                .isTrue();
        assertThat(ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(InetAddress.getByName("203.0.113.1")))
                .isTrue();
        assertThat(ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(InetAddress.getByName("2001:db8::1")))
                .isTrue();
    }

    @Test
    @DisplayName("rejects multicast and unspecified addresses")
    void rejectsMulticastAndUnspecified() throws Exception {
        assertThat(ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(InetAddress.getByName("224.0.0.1")))
                .isTrue();
        assertThat(ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(InetAddress.getByName("0.0.0.0")))
                .isTrue();
    }
}
