package com.github.marcelorodrigo.dutytracker.gateway.calendarfeed;

import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Verifies that an X.509 server certificate's identity matches the hostname used for the TLS
 * connection. This mirrors the RFC 6125 DNS-id checks the JDK performs, but is applied explicitly
 * because the TCP connection targets a resolved IP while SNI/certificate validation must still use
 * the original hostname.
 */
final class CertificateHostVerifier {

    private CertificateHostVerifier() {}

    static void verifyIdentity(Collection<List<?>> subjectAlternativeNames, String commonName, String host)
            throws CertificateException {
        if (host == null || host.isBlank()) {
            throw new CertificateException("No host to verify certificate identity against");
        }
        String lowerHost = host.toLowerCase(Locale.ROOT);
        boolean hasDnsName = false;
        if (subjectAlternativeNames != null) {
            for (List<?> san : subjectAlternativeNames) {
                if (!(san.get(0) instanceof Integer type) || !(san.get(1) instanceof String value)) {
                    continue;
                }
                if (type == 2) {
                    hasDnsName = true;
                    if (matchesDns(lowerHost, value.toLowerCase(Locale.ROOT))) {
                        return;
                    }
                }
            }
        }
        if (!hasDnsName && commonName != null && matchesDns(lowerHost, commonName.toLowerCase(Locale.ROOT))) {
            return;
        }
        throw new CertificateException("Certificate does not match host " + host);
    }

    static boolean matchesDns(String host, String san) {
        if (host.equals(san)) {
            return true;
        }
        if (san.startsWith("*.")) {
            String suffix = san.substring(1);
            if (host.endsWith(suffix)) {
                String label = host.substring(0, host.length() - suffix.length());
                return !label.isEmpty() && label.indexOf('.') == -1;
            }
        }
        return false;
    }

    static String extractCommonName(String name) {
        for (String part : name.split(",")) {
            String trimmed = part.trim();
            if (trimmed.startsWith("CN=")) {
                return trimmed.substring(3);
            }
        }
        return null;
    }
}
