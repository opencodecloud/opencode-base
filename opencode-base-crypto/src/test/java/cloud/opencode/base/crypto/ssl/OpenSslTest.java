package cloud.opencode.base.crypto.ssl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OpenSsl")
class OpenSslTest {

    @Nested
    @DisplayName("builder")
    class BuilderMethod {

        @Test
        @DisplayName("should return a SslContextBuilder")
        void shouldReturnBuilder() {
            SslContextBuilder builder = OpenSsl.builder();
            assertThat(builder).isNotNull();
        }
    }

    @Nested
    @DisplayName("getDefaultContext")
    class GetDefaultContext {

        @Test
        @DisplayName("should return non-null default SSLContext")
        void shouldReturnDefault() {
            SSLContext ctx = OpenSsl.getDefaultContext();
            assertThat(ctx).isNotNull();
        }
    }

    @Nested
    @DisplayName("createTrustAllContext")
    class CreateTrustAllContext {

        @Test
        @DisplayName("should return non-null trust-all SSLContext")
        void shouldCreateTrustAll() {
            SSLContext ctx = OpenSsl.createTrustAllContext();
            assertThat(ctx).isNotNull();
        }
    }

    @Nested
    @DisplayName("createTrustAllSocketFactory")
    class CreateTrustAllSocketFactory {

        @Test
        @DisplayName("should return non-null SSLSocketFactory")
        void shouldCreateFactory() {
            SSLSocketFactory factory = OpenSsl.createTrustAllSocketFactory();
            assertThat(factory).isNotNull();
        }
    }

    @Nested
    @DisplayName("getDefaultSocketFactory")
    class GetDefaultSocketFactory {

        @Test
        @DisplayName("should return non-null default SSLSocketFactory")
        void shouldReturnDefaultFactory() {
            SSLSocketFactory factory = OpenSsl.getDefaultSocketFactory();
            assertThat(factory).isNotNull();
        }
    }

    @Nested
    @DisplayName("getTrustAllHostnameVerifier")
    class GetTrustAllHostnameVerifier {

        @Test
        @DisplayName("should return verifier that accepts all hostnames")
        void shouldAcceptAll() {
            HostnameVerifier verifier = OpenSsl.getTrustAllHostnameVerifier();
            assertThat(verifier.verify("any.host", null)).isTrue();
            assertThat(verifier.verify("another.host", null)).isTrue();
        }
    }

    @Nested
    @DisplayName("getDefaultHostnameVerifier")
    class GetDefaultHostnameVerifier {

        @Test
        @DisplayName("should return non-null default hostname verifier")
        void shouldReturnDefault() {
            HostnameVerifier verifier = OpenSsl.getDefaultHostnameVerifier();
            assertThat(verifier).isNotNull();
        }
    }

    @Nested
    @DisplayName("getSupportedProtocols")
    class GetSupportedProtocols {

        @Test
        @DisplayName("should return non-empty protocol array")
        void shouldReturnProtocols() {
            String[] protocols = OpenSsl.getSupportedProtocols();
            assertThat(protocols).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("should include TLSv1.2")
        void shouldIncludeTls12() {
            String[] protocols = OpenSsl.getSupportedProtocols();
            assertThat(protocols).contains("TLSv1.2");
        }
    }

    @Nested
    @DisplayName("getSupportedCipherSuites")
    class GetSupportedCipherSuites {

        @Test
        @DisplayName("should return non-empty cipher suite array")
        void shouldReturnCipherSuites() {
            String[] suites = OpenSsl.getSupportedCipherSuites();
            assertThat(suites).isNotNull().isNotEmpty();
        }
    }

    @Nested
    @DisplayName("isTls13Supported")
    class IsTls13Supported {

        @Test
        @DisplayName("should return boolean value")
        void shouldReturnBoolean() {
            boolean supported = OpenSsl.isTls13Supported();
            assertThat(supported).isTrue();
        }
    }

    @Nested
    @DisplayName("getCertificateSubject")
    class GetCertificateSubject {

        @Test
        @DisplayName("should return subject DN from certificate")
        void shouldReturnSubject() throws Exception {
            X509Certificate cert = getAnyCacertsCertificate();
            if (cert != null) {
                String subject = OpenSsl.getCertificateSubject(cert);
                assertThat(subject).isNotNull().isNotEmpty();
            }
        }
    }

    @Nested
    @DisplayName("getCertificateIssuer")
    class GetCertificateIssuer {

        @Test
        @DisplayName("should return issuer DN from certificate")
        void shouldReturnIssuer() throws Exception {
            X509Certificate cert = getAnyCacertsCertificate();
            if (cert != null) {
                String issuer = OpenSsl.getCertificateIssuer(cert);
                assertThat(issuer).isNotNull().isNotEmpty();
            }
        }
    }

    private static X509Certificate getAnyCacertsCertificate() throws Exception {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        tmf.init((KeyStore) null);
        for (javax.net.ssl.TrustManager tm : tmf.getTrustManagers()) {
            if (tm instanceof X509TrustManager x509tm) {
                X509Certificate[] issuers = x509tm.getAcceptedIssuers();
                if (issuers.length > 0) {
                    return issuers[0];
                }
            }
        }
        return null;
    }
}
