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

    @Test
    @DisplayName("rejects the 192.0.0.0/24 special-purpose range")
    void rejects192_0_0_0Range() throws Exception {
        assertThat(ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(InetAddress.getByName("192.0.0.1")))
                .isTrue();
    }

    @Test
    @DisplayName("rejects the IPv6 unspecified address")
    void rejectsIpv6Unspecified() throws Exception {
        assertThat(ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(InetAddress.getByName("::")))
                .isTrue();
    }

    @Test
    @DisplayName("rejects IPv6 multicast addresses")
    void rejectsIpv6Multicast() throws Exception {
        assertThat(ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(InetAddress.getByName("ff00::1")))
                .isTrue();
        assertThat(ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(InetAddress.getByName("ff02::1")))
                .isTrue();
    }

    @Test
    @DisplayName("rejects IPv6 unique-local addresses")
    void rejectsIpv6UniqueLocal() throws Exception {
        assertThat(ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(InetAddress.getByName("fc00::1")))
                .isTrue();
        assertThat(ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(InetAddress.getByName("fd12:3456::1")))
                .isTrue();
    }

    @Test
    @DisplayName("rejects IPv6 6to4 addresses")
    void rejectsIpv66to4() throws Exception {
        assertThat(ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(InetAddress.getByName("2002::1")))
                .isTrue();
        assertThat(ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(InetAddress.getByName("2002:c000:0224::1")))
                .isTrue();
    }

    @Test
    @DisplayName("rejects IPv4-mapped IPv6 addresses that map to private IPv4")
    void rejectsIpv4MappedIpv6Private() throws Exception {
        assertThat(ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(InetAddress.getByName("::ffff:192.168.1.1")))
                .isTrue();
    }

    @Test
    @DisplayName("accepts IPv4-mapped IPv6 addresses that map to public IPv4")
    void acceptsIpv4MappedIpv6Public() throws Exception {
        assertThat(ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(
                        InetAddress.getByName("::ffff:93.184.216.34")))
                .isFalse();
    }

    @Test
    @DisplayName("rejects IPv6 global unicast outside the 2000::/3 range")
    void rejectsIpv6OutsideGlobalUnicast() throws Exception {
        assertThat(ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(InetAddress.getByName("2003::1")))
                .isFalse();
    }
}
