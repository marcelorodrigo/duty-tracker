package com.github.marcelorodrigo.dutytracker.gateway.calendarfeed;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

/**
 * Decides whether a resolved IP address is safe to connect to.
 *
 * <p>Only globally routable unicast addresses are accepted. Everything reserved for private,
 * loopback, link-local, carrier-grade NAT, benchmarking, documentation, multicast or other
 * special-purpose use is rejected. This is the single source of truth used by the calendar feed
 * DNS resolver to block SSRF, including DNS-rebinding, before a connection is established.
 */
final class ResolvedIpAddressValidator {

    private ResolvedIpAddressValidator() {}

    static boolean isNotGloballyRoutableUnicast(InetAddress address) {
        if (address instanceof Inet4Address) {
            return !isGlobalUnicastV4(address.getAddress());
        }
        if (address instanceof Inet6Address inet6) {
            return !isGlobalUnicastV6(inet6.getAddress());
        }
        return true;
    }

    private static boolean isGlobalUnicastV4(byte[] b) {
        int b0 = b[0] & 0xFF;
        int b1 = b[1] & 0xFF;
        int b2 = b[2] & 0xFF;
        if (b0 == 0) {
            return false; // 0.0.0.0/8
        }
        if (b0 == 10) {
            return false; // 10/8 private
        }
        if (b0 == 127) {
            return false; // 127/8 loopback
        }
        if (b0 == 169 && b1 == 254) {
            return false; // 169.254/16 link-local
        }
        if (b0 == 172 && (b1 & 0xF0) == 16) {
            return false; // 172.16/12 private
        }
        if (b0 == 192 && b1 == 168) {
            return false; // 192.168/16 private
        }
        if (b0 == 100 && (b1 & 0xC0) == 64) {
            return false; // 100.64/10 carrier-grade NAT
        }
        if (b0 == 192 && b1 == 0) {
            return false; // 192.0.0.0/24
        }
        if (b0 == 192 && b1 == 0 && b2 == 2) {
            return false; // 192.0.2.0/24 TEST-NET-1
        }
        if (b0 == 198 && (b1 == 18 || b1 == 19)) {
            return false; // 198.18/15 benchmarking
        }
        if (b0 == 198 && b1 == 51 && b2 == 100) {
            return false; // 198.51.100/24 TEST-NET-2
        }
        if (b0 == 203 && b1 == 0 && b2 == 113) {
            return false; // 203.0.113/24 TEST-NET-3
        }
        if (b0 >= 224) {
            return false; // 224/4 multicast and 240/4 reserved
        }
        return true;
    }

    private static boolean isGlobalUnicastV6(byte[] b) {
        if (isAllZero(b)) {
            return false; // ::
        }
        if (b[15] == 1 && isAllZeroPrefix(b, 15)) {
            return false; // ::1 loopback
        }
        if ((b[0] & 0xFF) == 0xff) {
            return false; // ff00::/8 multicast
        }
        if (b[0] == (byte) 0xfe && (b[1] & 0xC0) == 0x80) {
            return false; // fe80::/10 link-local
        }
        if ((b[0] & 0xFE) == 0xFC) {
            return false; // fc00::/7 unique-local
        }
        if (b[0] == 0
                && b[1] == 0
                && b[2] == 0
                && b[3] == 0
                && b[4] == 0
                && b[5] == 0
                && b[6] == 0
                && b[7] == 0
                && b[8] == 0
                && b[9] == 0
                && b[10] == (byte) 0xff
                && b[11] == (byte) 0xff) {
            return isGlobalUnicastV4(new byte[] {b[12], b[13], b[14], b[15]}); // IPv4-mapped
        }
        if (b[0] == 0x20 && b[1] == 0x01 && b[2] == 0x0d && b[3] == (byte) 0xb8) {
            return false; // 2001:db8::/32 documentation
        }
        if (b[0] == 0x20 && b[1] == 0x02) {
            return false; // 2002::/16 6to4
        }
        return (b[0] & 0xE0) == 0x20; // 2000::/3 global unicast
    }

    private static boolean isAllZero(byte[] b) {
        for (byte value : b) {
            if (value != 0) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAllZeroPrefix(byte[] b, int length) {
        for (int i = 0; i < length; i++) {
            if (b[i] != 0) {
                return false;
            }
        }
        return true;
    }
}
