package com.github.marcelorodrigo.dutytracker.gateway.calendarfeed;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidCalendarFeedUrlException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Resolves a calendar feed host to its IP addresses and rejects any target that would let the
 * connection reach a private, loopback, link-local or unspecified address.
 *
 * <p>This closes the DNS-rebinding (TOCTOU) gap: {@link CalendarFeedUrlValidator} only inspects the
 * hostname string, while the actual connection resolves DNS independently. By resolving here and
 * refusing any disallowed address, the IP that the HTTP client eventually connects to is guaranteed
 * to be public.
 */
final class ResolvedIpAddressValidator {

    private ResolvedIpAddressValidator() {}

    static InetAddress resolveSafeTarget(String host, DnsResolver resolver) throws UnknownHostException {
        InetAddress[] addresses = resolver.resolve(host);
        if (addresses.length == 0) {
            throw new UnknownHostException(host);
        }
        for (InetAddress address : addresses) {
            if (isDisallowed(address)) {
                throw new InvalidCalendarFeedUrlException(
                        "Calendar feed host resolves to a disallowed address: " + address.getHostAddress());
            }
        }
        return addresses[0];
    }

    private static boolean isDisallowed(InetAddress address) {
        return address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress();
    }
}
