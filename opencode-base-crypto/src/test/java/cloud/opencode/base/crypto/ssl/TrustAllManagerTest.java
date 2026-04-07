package cloud.opencode.base.crypto.ssl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TrustAllManager")
class TrustAllManagerTest {

    @Nested
    @DisplayName("INSTANCE")
    class Instance {

        @Test
        @DisplayName("should be non-null singleton")
        void shouldBeNonNull() {
            assertThat(TrustAllManager.INSTANCE).isNotNull();
        }

        @Test
        @DisplayName("should implement X509TrustManager")
        void shouldImplementInterface() {
            assertThat(TrustAllManager.INSTANCE).isInstanceOf(X509TrustManager.class);
        }
    }

    @Nested
    @DisplayName("checkClientTrusted")
    class CheckClientTrusted {

        @Test
        @DisplayName("should not throw for any input")
        void shouldNotThrow() {
            assertThatCode(() ->
                    TrustAllManager.INSTANCE.checkClientTrusted(new X509Certificate[0], "RSA")
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should accept null chain")
        void shouldAcceptNullChain() {
            assertThatCode(() ->
                    TrustAllManager.INSTANCE.checkClientTrusted(null, null)
            ).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("checkServerTrusted")
    class CheckServerTrusted {

        @Test
        @DisplayName("should not throw for any input")
        void shouldNotThrow() {
            assertThatCode(() ->
                    TrustAllManager.INSTANCE.checkServerTrusted(new X509Certificate[0], "RSA")
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should accept null chain")
        void shouldAcceptNullChain() {
            assertThatCode(() ->
                    TrustAllManager.INSTANCE.checkServerTrusted(null, null)
            ).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("getAcceptedIssuers")
    class GetAcceptedIssuers {

        @Test
        @DisplayName("should return empty array")
        void shouldReturnEmptyArray() {
            X509Certificate[] issuers = TrustAllManager.INSTANCE.getAcceptedIssuers();
            assertThat(issuers).isNotNull().isEmpty();
        }
    }

    @Nested
    @DisplayName("createUnsafe")
    class CreateUnsafe {

        @Test
        @DisplayName("should create instance when acknowledging risk")
        void shouldCreateWhenAcknowledged() {
            TrustAllManager instance = TrustAllManager.createUnsafe(true);
            assertThat(instance).isNotNull();
        }

        @Test
        @DisplayName("should throw SecurityException when not acknowledged")
        void shouldThrowWhenNotAcknowledged() {
            assertThatThrownBy(() -> TrustAllManager.createUnsafe(false))
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("security risk");
        }
    }

    @Nested
    @DisplayName("TRUST_ALL_ENABLED_PROPERTY")
    class PropertyConstant {

        @Test
        @DisplayName("should have correct property name")
        void shouldHaveCorrectPropertyName() {
            assertThat(TrustAllManager.TRUST_ALL_ENABLED_PROPERTY).isEqualTo("opencode.ssl.trustAll.enabled");
        }
    }
}
