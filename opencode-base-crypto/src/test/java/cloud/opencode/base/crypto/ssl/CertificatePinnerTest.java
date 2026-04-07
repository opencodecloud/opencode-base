package cloud.opencode.base.crypto.ssl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CertificatePinner")
class CertificatePinnerTest {

    @Nested
    @DisplayName("Builder")
    class BuilderTest {

        @Test
        @DisplayName("should build pinner with valid pin")
        void shouldBuildWithValidPin() {
            CertificatePinner pinner = CertificatePinner.builder()
                    .add("sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
                    .build();
            assertThat(pinner).isNotNull();
        }

        @Test
        @DisplayName("should reject pin without sha256/ prefix")
        void shouldRejectInvalidPrefix() {
            assertThatThrownBy(() -> CertificatePinner.builder().add("md5/abc"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("sha256/");
        }

        @Test
        @DisplayName("should reject empty hash after prefix")
        void shouldRejectEmptyHash() {
            assertThatThrownBy(() -> CertificatePinner.builder().add("sha256/"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("empty");
        }

        @Test
        @DisplayName("should reject null pin")
        void shouldRejectNullPin() {
            assertThatThrownBy(() -> CertificatePinner.builder().add(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw when building without pins")
        void shouldThrowWhenNoPins() {
            assertThatThrownBy(() -> CertificatePinner.builder().build())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("At least one pin");
        }

        @Test
        @DisplayName("should accept multiple pins via addAll")
        void shouldAcceptMultiplePins() {
            CertificatePinner pinner = CertificatePinner.builder()
                    .addAll(
                            "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
                            "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB="
                    )
                    .build();
            assertThat(pinner).isNotNull();
        }
    }

    @Nested
    @DisplayName("computePin")
    class ComputePin {

        @Test
        @DisplayName("should compute pin starting with sha256/ using JVM cacerts cert")
        void shouldComputePin() throws Exception {
            X509Certificate cert = getAnyCacertsCertificate();
            if (cert != null) {
                String pin = CertificatePinner.computePin(cert);
                assertThat(pin).startsWith("sha256/");
                assertThat(pin.length()).isGreaterThan("sha256/".length());
            }
        }

        @Test
        @DisplayName("should throw for null certificate")
        void shouldThrowForNullCert() {
            assertThatThrownBy(() -> CertificatePinner.computePin(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should return consistent pin for same cert")
        void shouldBeConsistent() throws Exception {
            X509Certificate cert = getAnyCacertsCertificate();
            if (cert != null) {
                String pin1 = CertificatePinner.computePin(cert);
                String pin2 = CertificatePinner.computePin(cert);
                assertThat(pin1).isEqualTo(pin2);
            }
        }
    }

    @Nested
    @DisplayName("toSslContext")
    class ToSslContext {

        @Test
        @DisplayName("should create SSLContext with pinning")
        void shouldCreateSslContext() throws Exception {
            X509Certificate cert = getAnyCacertsCertificate();
            if (cert != null) {
                String pin = CertificatePinner.computePin(cert);
                CertificatePinner pinner = CertificatePinner.builder()
                        .add(pin)
                        .build();
                SSLContext ctx = pinner.toSslContext();
                assertThat(ctx).isNotNull();
                assertThat(ctx.getProtocol()).isEqualTo("TLS");
            }
        }
    }

    /**
     * Gets any X509Certificate from the JVM's default trust store (cacerts).
     */
    private static X509Certificate getAnyCacertsCertificate() throws Exception {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        // The default trust store is available via TrustManagerFactory
        javax.net.ssl.TrustManagerFactory tmf = javax.net.ssl.TrustManagerFactory.getInstance(
                javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm());
        tmf.init((KeyStore) null);
        for (javax.net.ssl.TrustManager tm : tmf.getTrustManagers()) {
            if (tm instanceof javax.net.ssl.X509TrustManager x509tm) {
                X509Certificate[] issuers = x509tm.getAcceptedIssuers();
                if (issuers.length > 0) {
                    return issuers[0];
                }
            }
        }
        return null;
    }
}
