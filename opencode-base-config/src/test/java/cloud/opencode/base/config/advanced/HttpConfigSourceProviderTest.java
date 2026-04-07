package cloud.opencode.base.config.advanced;

import cloud.opencode.base.config.source.ConfigSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for HttpConfigSourceProvider.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("HttpConfigSourceProvider Tests")
class HttpConfigSourceProviderTest {

    private HttpConfigSourceProvider provider;

    @BeforeEach
    void setUp() {
        provider = new HttpConfigSourceProvider();
    }

    @Nested
    @DisplayName("Supports Tests")
    class SupportsTests {

        @Test
        @DisplayName("supports http:// URI")
        void testSupportsHttp() {
            assertThat(provider.supports("http://config-server/app")).isTrue();
        }

        @Test
        @DisplayName("supports https:// URI")
        void testSupportsHttps() {
            assertThat(provider.supports("https://config-server/app")).isTrue();
        }

        @Test
        @DisplayName("does not support ftp:// URI")
        void testDoesNotSupportFtp() {
            assertThat(provider.supports("ftp://server/config")).isFalse();
        }

        @Test
        @DisplayName("does not support file: URI")
        void testDoesNotSupportFile() {
            assertThat(provider.supports("file:/path/to/config")).isFalse();
        }

        @Test
        @DisplayName("does not support plain string")
        void testDoesNotSupportPlainString() {
            assertThat(provider.supports("just-a-string")).isFalse();
        }
    }

    @Nested
    @DisplayName("Create Tests")
    class CreateTests {

        @Test
        @DisplayName("create with unresolvable host throws IllegalArgumentException (SSRF protection)")
        void testCreateWithUnresolvableHostThrows() {
            // Unresolvable hosts are rejected to prevent DNS rebinding SSRF
            assertThatThrownBy(() -> provider.create("http://config-server/app", Map.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot resolve host");
        }

        @Test
        @DisplayName("create with localhost throws IllegalArgumentException (SSRF protection)")
        void testCreateWithLocalhostThrows() {
            assertThatThrownBy(() -> provider.create("http://localhost/app", Map.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("blocked hostname");
        }

        @Test
        @DisplayName("create with loopback IP throws IllegalArgumentException (SSRF protection)")
        void testCreateWithLoopbackIpThrows() {
            assertThatThrownBy(() -> provider.create("http://127.0.0.1/app", Map.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("internal/private");
        }
    }

    @Nested
    @DisplayName("Priority Tests")
    class PriorityTests {

        @Test
        @DisplayName("priority is 100")
        void testPriority() {
            assertThat(provider.priority()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("Interface Implementation Tests")
    class InterfaceTests {

        @Test
        @DisplayName("implements ConfigSourceProvider")
        void testImplementsInterface() {
            assertThat(provider).isInstanceOf(ConfigSourceProvider.class);
        }
    }
}
