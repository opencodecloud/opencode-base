package cloud.opencode.base.crypto.ssl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import java.security.SecureRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SslContextBuilder")
class SslContextBuilderTest {

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should create a new builder instance")
        void shouldCreateBuilder() {
            SslContextBuilder builder = SslContextBuilder.create();
            assertThat(builder).isNotNull();
        }
    }

    @Nested
    @DisplayName("build with defaults")
    class BuildDefaults {

        @Test
        @DisplayName("should build SSLContext with default protocol")
        void shouldBuildDefault() {
            SSLContext ctx = SslContextBuilder.create().build();
            assertThat(ctx).isNotNull();
            assertThat(ctx.getProtocol()).isEqualTo("TLS");
        }
    }

    @Nested
    @DisplayName("protocol")
    class Protocol {

        @Test
        @DisplayName("should set TLS protocol")
        void shouldSetProtocol() {
            SSLContext ctx = SslContextBuilder.create()
                    .protocol("TLSv1.3")
                    .build();
            assertThat(ctx.getProtocol()).isEqualTo("TLSv1.3");
        }

        @Test
        @DisplayName("should set TLS 1.2 via convenience method")
        void shouldSetTlsV12() {
            SSLContext ctx = SslContextBuilder.create().tlsV12().build();
            assertThat(ctx.getProtocol()).isEqualTo("TLSv1.2");
        }

        @Test
        @DisplayName("should set TLS 1.3 via convenience method")
        void shouldSetTlsV13() {
            SSLContext ctx = SslContextBuilder.create().tlsV13().build();
            assertThat(ctx.getProtocol()).isEqualTo("TLSv1.3");
        }

        @Test
        @DisplayName("should throw for invalid protocol")
        void shouldThrowForInvalidProtocol() {
            assertThatThrownBy(() -> SslContextBuilder.create()
                    .protocol("INVALID_PROTOCOL")
                    .build())
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("trustAll")
    class TrustAll {

        @Test
        @DisplayName("should build SSLContext with trust-all manager")
        void shouldBuildTrustAll() {
            SSLContext ctx = SslContextBuilder.create().trustAll().build();
            assertThat(ctx).isNotNull();
        }
    }

    @Nested
    @DisplayName("secureRandom")
    class SecureRandomTest {

        @Test
        @DisplayName("should accept custom SecureRandom")
        void shouldAcceptSecureRandom() {
            SSLContext ctx = SslContextBuilder.create()
                    .secureRandom(new SecureRandom())
                    .build();
            assertThat(ctx).isNotNull();
        }
    }

    @Nested
    @DisplayName("fluent API chaining")
    class FluentApi {

        @Test
        @DisplayName("should support method chaining")
        void shouldSupportChaining() {
            SSLContext ctx = SslContextBuilder.create()
                    .protocol("TLSv1.3")
                    .trustAll()
                    .secureRandom(new SecureRandom())
                    .build();
            assertThat(ctx).isNotNull();
            assertThat(ctx.getProtocol()).isEqualTo("TLSv1.3");
        }
    }

    @Nested
    @DisplayName("keyManagers and trustManagers")
    class ManagerSetters {

        @Test
        @DisplayName("should accept null key managers")
        void shouldAcceptNullKeyManagers() {
            SSLContext ctx = SslContextBuilder.create()
                    .keyManagers()
                    .build();
            assertThat(ctx).isNotNull();
        }

        @Test
        @DisplayName("should accept null trust managers")
        void shouldAcceptNullTrustManagers() {
            SSLContext ctx = SslContextBuilder.create()
                    .trustManagers()
                    .build();
            assertThat(ctx).isNotNull();
        }
    }
}
