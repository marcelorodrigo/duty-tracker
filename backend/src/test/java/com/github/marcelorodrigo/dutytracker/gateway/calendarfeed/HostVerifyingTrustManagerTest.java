package com.github.marcelorodrigo.dutytracker.gateway.calendarfeed;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.security.auth.x500.X500Principal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HostVerifyingTrustManagerTest {

    @Mock
    private X509ExtendedTrustManager delegate;

    @Mock
    private X509Certificate certificate;

    private HttpCalendarFeedGateway.HostVerifyingTrustManager trustManager;

    @BeforeEach
    void setUp() {
        trustManager = new HttpCalendarFeedGateway.HostVerifyingTrustManager(delegate);
    }

    @Test
    @DisplayName("delegates chain verification and accepts a matching SAN over SSLEngine")
    void serverTrustedWithMatchingSan() throws CertificateException {
        when(certificate.getSubjectAlternativeNames()).thenReturn(List.of(List.of(2, "app.incident.io")));
        when(certificate.getSubjectX500Principal()).thenReturn(new X500Principal("CN=app.incident.io"));
        SSLEngine engine = engineWithSni("app.incident.io");
        X509Certificate[] chain = {certificate};

        assertThatCode(() -> trustManager.checkServerTrusted(chain, "RSA", engine))
                .doesNotThrowAnyException();

        verify(delegate).checkServerTrusted(chain, "RSA", engine);
    }

    @Test
    @DisplayName("throws when the certificate does not match the SNI host")
    void serverTrustedWithMismatchedSan() throws CertificateException {
        when(certificate.getSubjectAlternativeNames()).thenReturn(List.of(List.of(2, "other.example.com")));
        when(certificate.getSubjectX500Principal()).thenReturn(new X500Principal("CN=other.example.com"));
        SSLEngine engine = engineWithSni("app.incident.io");
        X509Certificate[] chain = {certificate};

        assertThatThrownBy(() -> trustManager.checkServerTrusted(chain, "RSA", engine))
                .isInstanceOf(CertificateException.class)
                .hasMessageContaining("does not match host");
    }

    @Test
    @DisplayName("uses the peer host when no SNI server name is present")
    void fallsBackToPeerHostWithoutSni() throws CertificateException {
        SSLParameters params = new SSLParameters();
        params.setServerNames(null);
        SSLEngine engine = org.mockito.Mockito.mock(SSLEngine.class);
        when(engine.getSSLParameters()).thenReturn(params);
        when(engine.getPeerHost()).thenReturn("app.incident.io");
        when(certificate.getSubjectAlternativeNames()).thenReturn(List.of(List.of(2, "app.incident.io")));
        when(certificate.getSubjectX500Principal()).thenReturn(new X500Principal("CN=app.incident.io"));
        X509Certificate[] chain = {certificate};

        assertThatCode(() -> trustManager.checkServerTrusted(chain, "RSA", engine))
                .doesNotThrowAnyException();

        verify(delegate).checkServerTrusted(chain, "RSA", engine);
    }

    @Test
    @DisplayName("delegates server verification over a Socket using the resolved host name")
    void serverTrustedOverSocket() throws CertificateException {
        Socket socket = org.mockito.Mockito.mock(Socket.class);
        when(socket.getInetAddress()).thenReturn(InetAddress.getLoopbackAddress());
        when(certificate.getSubjectAlternativeNames()).thenReturn(List.of(List.of(2, "localhost")));
        when(certificate.getSubjectX500Principal()).thenReturn(new X500Principal("CN=localhost"));
        X509Certificate[] chain = {certificate};

        assertThatCode(() -> trustManager.checkServerTrusted(chain, "RSA", socket))
                .doesNotThrowAnyException();

        verify(delegate).checkServerTrusted(chain, "RSA", socket);
    }

    @Test
    @DisplayName("delegates the no-transport server verification")
    void serverTrustedWithoutTransport() throws CertificateException {
        X509Certificate[] chain = {certificate};

        assertThatCode(() -> trustManager.checkServerTrusted(chain, "RSA")).doesNotThrowAnyException();

        verify(delegate).checkServerTrusted(chain, "RSA");
    }

    @Test
    @DisplayName("delegates client verification variants")
    void delegatesClientVerification() throws CertificateException {
        X509Certificate[] chain = {certificate};

        trustManager.checkClientTrusted(chain, "RSA");
        trustManager.checkClientTrusted(chain, "RSA", new java.net.Socket());
        trustManager.checkClientTrusted(chain, "RSA", org.mockito.Mockito.mock(SSLEngine.class));

        verify(delegate).checkClientTrusted(chain, "RSA");
    }

    @Test
    @DisplayName("delegates accepted issuers")
    void delegatesAcceptedIssuers() {
        X509Certificate[] issuers = {certificate};
        when(delegate.getAcceptedIssuers()).thenReturn(issuers);

        assertThatCode(() -> trustManager.getAcceptedIssuers()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("propagates delegate chain verification failures")
    void propagatesDelegateFailure() throws CertificateException {
        X509Certificate[] chain = {certificate};
        SSLEngine engine = org.mockito.Mockito.mock(SSLEngine.class);
        doThrow(new CertificateException("chain broken")).when(delegate).checkServerTrusted(chain, "RSA", engine);

        assertThatThrownBy(() -> trustManager.checkServerTrusted(chain, "RSA", engine))
                .hasMessageContaining("chain broken");
    }

    private static SSLEngine engineWithSni(String host) {
        SSLParameters params = new SSLParameters();
        params.setServerNames(List.of(new SNIHostName(host)));
        SSLEngine engine = org.mockito.Mockito.mock(SSLEngine.class);
        when(engine.getSSLParameters()).thenReturn(params);
        return engine;
    }
}
