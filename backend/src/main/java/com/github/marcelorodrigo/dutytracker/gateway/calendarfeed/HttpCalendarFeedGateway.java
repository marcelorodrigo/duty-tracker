package com.github.marcelorodrigo.dutytracker.gateway.calendarfeed;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.CalendarFeedAuthenticationException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.CalendarFeedFetchException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidCalendarFeedUrlException;
import com.github.marcelorodrigo.dutytracker.usecase.validator.calendarfeed.CalendarFeedUrlValidator;
import java.io.InputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * Fetches calendar feeds over HTTPS while protecting against SSRF via DNS rebinding.
 *
 * <p>Two independent checks guard the outbound connection:
 *
 * <ol>
 *   <li>{@link CalendarFeedUrlValidator} validates the URL string (scheme, host, literal private IPs).
 *   <li>{@link ResolvedIpAddressValidator} resolves the host at fetch time and refuses any address that
 *       is private/loopback/link-local. The connection is then pinned to one of those validated public
 *       IPs (the {@code Host} header and TLS SNI keep the original host so the certificate still
 *       verifies), which removes the time-of-check/time-of-use window between validation and connection.
 * </ol>
 */
@Component
public class HttpCalendarFeedGateway implements CalendarFeedGateway {

    public static final int MAX_BODY_SIZE_BYTES = 1024 * 1024;
    public static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration READ_TIMEOUT = Duration.ofSeconds(10);

    private static final String ALLOWED_HOST = "app.incident.io";

    private final RestClient restClient;
    private final CalendarFeedUrlValidator urlValidator;
    private final DnsResolver dnsResolver;

    @Autowired
    public HttpCalendarFeedGateway(CalendarFeedUrlValidator urlValidator) {
        this(urlValidator, InetAddress::getAllByName, RestClient.builder().requestFactory(createRequestFactory()));
    }

    HttpCalendarFeedGateway(CalendarFeedUrlValidator urlValidator, RestClient.Builder restClientBuilder) {
        this(urlValidator, InetAddress::getAllByName, restClientBuilder);
    }

    HttpCalendarFeedGateway(
            CalendarFeedUrlValidator urlValidator, DnsResolver dnsResolver, RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
        this.urlValidator = urlValidator;
        this.dnsResolver = dnsResolver;
    }

    private static ClientHttpRequestFactory createRequestFactory() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .sslContext(createSslContext())
                .sslParameters(createSslParameters())
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(READ_TIMEOUT);
        return factory;
    }

    /**
     * SNI is pinned to {@value ALLOWED_HOST}: {@link CalendarFeedUrlValidator} guarantees the requested
     * host equals this value, so the TLS handshake presents the correct server name and the certificate
     * is verified against it even though the TCP connection targets a resolved IP.
     */
    private static SSLParameters createSslParameters() {
        SSLParameters params = new SSLParameters();
        params.setServerNames(List.of(new SNIHostName(ALLOWED_HOST)));
        return params;
    }

    private static SSLContext createSslContext() {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[] {new HostVerifyingTrustManager(defaultTrustManager())}, null);
            return context;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to initialize SSL context for calendar feed gateway", e);
        }
    }

    private static X509ExtendedTrustManager defaultTrustManager() {
        try {
            TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            factory.init((KeyStore) null);
            for (TrustManager manager : factory.getTrustManagers()) {
                if (manager instanceof X509ExtendedTrustManager m) {
                    return m;
                }
            }
            throw new IllegalStateException("No X509ExtendedTrustManager available");
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to load default trust manager", e);
        }
    }

    @Override
    public String fetch(String url) {
        urlValidator.validate(url);
        URI original = parse(url);
        String host = original.getHost();
        InetAddress target;
        try {
            target = ResolvedIpAddressValidator.resolveSafeTarget(host, dnsResolver);
        } catch (UnknownHostException e) {
            throw new CalendarFeedFetchException("Failed to resolve calendar feed host: " + host);
        }
        URI targetUri = buildIpUri(original, target);
        try {
            return restClient
                    .get()
                    .uri(targetUri)
                    .header(HttpHeaders.HOST, host)
                    .exchange((request, response) -> {
                        int status = response.getStatusCode().value();
                        if (status >= 300 && status < 400) {
                            throw new CalendarFeedFetchException("Calendar feed redirects are not allowed");
                        }
                        if (status == 401 || status == 403 || status == 404) {
                            throw new CalendarFeedAuthenticationException();
                        }
                        if (status >= 400 && status < 600) {
                            throw new CalendarFeedFetchException("Calendar feed upstream returned HTTP " + status);
                        }
                        long contentLength = response.getHeaders().getContentLength();
                        if (contentLength > MAX_BODY_SIZE_BYTES) {
                            throw new CalendarFeedFetchException("Calendar feed response body exceeds maximum size");
                        }
                        try (InputStream body = response.getBody()) {
                            byte[] bytes = body.readNBytes(MAX_BODY_SIZE_BYTES + 1);
                            if (bytes.length > MAX_BODY_SIZE_BYTES) {
                                throw new CalendarFeedFetchException(
                                        "Calendar feed response body exceeds maximum size");
                            }
                            return new String(bytes, StandardCharsets.UTF_8);
                        }
                    });
        } catch (ResourceAccessException e) {
            throw new CalendarFeedFetchException("Failed to fetch calendar feed: " + e.getMessage());
        }
    }

    private static URI parse(String url) {
        try {
            return URI.create(url);
        } catch (IllegalArgumentException e) {
            throw new InvalidCalendarFeedUrlException("Calendar feed URL is not a valid URL");
        }
    }

    private static URI buildIpUri(URI original, InetAddress target) {
        String hostAddress = target.getHostAddress();
        if (target instanceof Inet6Address) {
            hostAddress = "[" + hostAddress + "]";
        }
        try {
            return new URI(
                    original.getScheme(),
                    null,
                    hostAddress,
                    original.getPort(),
                    original.getPath(),
                    original.getQuery(),
                    original.getFragment());
        } catch (URISyntaxException e) {
            throw new CalendarFeedFetchException("Failed to build calendar feed request URI");
        }
    }

    /**
     * Trust manager that delegates chain-of-trust verification to the platform default and additionally
     * confirms the end-entity certificate's identity matches the SNI host name used for the connection.
     * This is required because the TCP connection targets a resolved IP while TLS must still be validated
     * against the original hostname.
     */
    static final class HostVerifyingTrustManager extends X509ExtendedTrustManager {

        private final X509ExtendedTrustManager delegate;

        HostVerifyingTrustManager(X509ExtendedTrustManager delegate) {
            this.delegate = delegate;
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, java.net.Socket socket)
                throws CertificateException {
            delegate.checkServerTrusted(chain, authType, socket);
            verifyIdentity(chain[0], socket.getInetAddress().getHostName());
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, javax.net.ssl.SSLEngine engine)
                throws CertificateException {
            delegate.checkServerTrusted(chain, authType, engine);
            verifyIdentity(chain[0], sniHost(engine));
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            delegate.checkServerTrusted(chain, authType);
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            delegate.checkClientTrusted(chain, authType);
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, java.net.Socket socket)
                throws CertificateException {
            delegate.checkClientTrusted(chain, authType, socket);
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, javax.net.ssl.SSLEngine engine)
                throws CertificateException {
            delegate.checkClientTrusted(chain, authType, engine);
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return delegate.getAcceptedIssuers();
        }

        private static String sniHost(javax.net.ssl.SSLEngine engine) {
            List<javax.net.ssl.SNIServerName> names = engine.getSSLParameters().getServerNames();
            if (names != null) {
                for (javax.net.ssl.SNIServerName name : names) {
                    if (name instanceof SNIHostName sni) {
                        return sni.getAsciiName();
                    }
                }
            }
            return engine.getPeerHost();
        }

        private static void verifyIdentity(X509Certificate certificate, String host) throws CertificateException {
            Collection<List<?>> sans;
            try {
                sans = certificate.getSubjectAlternativeNames();
            } catch (CertificateException e) {
                throw new CertificateException("Unable to parse certificate subject alternative names", e);
            }
            CertificateHostVerifier.verifyIdentity(
                    sans,
                    CertificateHostVerifier.extractCommonName(
                            certificate.getSubjectX500Principal().getName()),
                    host);
        }
    }
}
