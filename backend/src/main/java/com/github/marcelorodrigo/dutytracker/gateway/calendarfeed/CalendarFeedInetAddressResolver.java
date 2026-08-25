package com.github.marcelorodrigo.dutytracker.gateway.calendarfeed;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.spi.InetAddressResolver;
import java.net.spi.InetAddressResolver.LookupPolicy;
import java.util.stream.Stream;

/**
 * DNS resolver that refuses to resolve the calendar feed host to anything other than a globally
 * routable unicast address. Resolutions for any other host are delegated unchanged.
 *
 * <p>Because {@link HttpCalendarFeedGateway} connects using the original hostname, the JDK client
 * derives the {@code Host} header, TLS SNI and certificate validation from that hostname while this
 * resolver guarantees the underlying IP is public. Validation runs on every resolution, so a
 * DNS-rebinding response can never yield a private connection target.
 */
final class CalendarFeedInetAddressResolver implements InetAddressResolver {

    private final InetAddressResolver delegate;
    private final String allowedHost;

    CalendarFeedInetAddressResolver(InetAddressResolver delegate, String allowedHost) {
        this.delegate = delegate;
        this.allowedHost = allowedHost;
    }

    @Override
    public Stream<InetAddress> lookupByName(String host, LookupPolicy mode) throws UnknownHostException {
        if (!allowedHost.equalsIgnoreCase(host)) {
            return delegate.lookupByName(host, mode);
        }
        InetAddress[] addresses = delegate.lookupByName(host, mode).toArray(InetAddress[]::new);
        for (InetAddress address : addresses) {
            if (ResolvedIpAddressValidator.isNotGloballyRoutableUnicast(address)) {
                throw new UnknownHostException("Calendar feed host " + host + " resolves to a disallowed address: "
                        + address.getHostAddress());
            }
        }
        return Stream.of(addresses);
    }

    @Override
    public String lookupByAddress(byte[] addr) throws UnknownHostException {
        return delegate.lookupByAddress(addr);
    }
}
