package com.github.marcelorodrigo.dutytracker.gateway.calendarfeed;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CertificateHostVerifierTest {

    @Test
    @DisplayName("accepts an exact DNS SAN match")
    void acceptsExactDnsSan() {
        Collection<List<?>> sans = List.of(List.of(2, "app.incident.io"));

        assertThatCode(() -> CertificateHostVerifier.verifyIdentity(sans, null, "app.incident.io"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("accepts a single-label wildcard SAN match")
    void acceptsWildcardSan() {
        Collection<List<?>> sans = List.of(List.of(2, "*.incident.io"));

        assertThatCode(() -> CertificateHostVerifier.verifyIdentity(sans, null, "app.incident.io"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("rejects a wildcard that would match more than one label")
    void rejectsMultiLabelWildcard() {
        Collection<List<?>> sans = List.of(List.of(2, "*.incident.io"));

        assertThatThrownBy(() -> CertificateHostVerifier.verifyIdentity(sans, null, "a.b.incident.io"))
                .isInstanceOf(CertificateException.class)
                .hasMessageContaining("does not match host");
    }

    @Test
    @DisplayName("falls back to the CN when no DNS SAN is present")
    void fallsBackToCommonName() {
        assertThatCode(() -> CertificateHostVerifier.verifyIdentity(null, "app.incident.io", "app.incident.io"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("rejects when neither SAN nor CN matches")
    void rejectsNonMatchingIdentity() {
        Collection<List<?>> sans = List.of(List.of(2, "other.example.com"));

        assertThatThrownBy(() -> CertificateHostVerifier.verifyIdentity(sans, "other.example.com", "app.incident.io"))
                .isInstanceOf(CertificateException.class)
                .hasMessageContaining("does not match host");
    }

    @Test
    @DisplayName("rejects a blank host")
    void rejectsBlankHost() {
        assertThatThrownBy(() -> CertificateHostVerifier.verifyIdentity(null, null, "   "))
                .isInstanceOf(CertificateException.class)
                .hasMessageContaining("No host");
    }

    @Test
    @DisplayName("rejects a null host")
    void rejectsNullHost() {
        assertThatThrownBy(() -> CertificateHostVerifier.verifyIdentity(null, null, null))
                .isInstanceOf(CertificateException.class)
                .hasMessageContaining("No host");
    }

    @Test
    @DisplayName("rejects a wildcard SAN whose suffix does not match the host")
    void rejectsWildcardWithNonMatchingSuffix() {
        Collection<List<?>> sans = List.of(List.of(2, "*.incident.io"));

        assertThatThrownBy(() -> CertificateHostVerifier.verifyIdentity(sans, null, "app.example.com"))
                .isInstanceOf(CertificateException.class)
                .hasMessageContaining("does not match host");
    }

    @Test
    @DisplayName("rejects when the CN is present but does not match")
    void rejectsNonMatchingCommonName() {
        assertThatThrownBy(() -> CertificateHostVerifier.verifyIdentity(null, "other.example.com", "app.incident.io"))
                .isInstanceOf(CertificateException.class)
                .hasMessageContaining("does not match host");
    }

    @Test
    @DisplayName("ignores non-DNS SAN entries")
    void ignoresNonDnsSanEntries() {
        Collection<List<?>> sans = List.of(List.of(1, "unsupported"), List.of(7, "1.2.3.4"));

        assertThatThrownBy(() -> CertificateHostVerifier.verifyIdentity(sans, null, "app.incident.io"))
                .isInstanceOf(CertificateException.class);
    }

    @Test
    @DisplayName("matches a host case-insensitively")
    void matchesCaseInsensitively() {
        Collection<List<?>> sans = List.of(List.of(2, "APP.INCIDENT.IO"));

        assertThatCode(() -> CertificateHostVerifier.verifyIdentity(sans, null, "app.incident.io"))
                .doesNotThrowAnyException();
    }
}
