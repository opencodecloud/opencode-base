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
        @DisplayName("create returns a ConfigSource")
        void testCreateReturnsSource() {
            ConfigSource source = provider.create("http://config-server/app", Map.of());
            assertThat(source).isNotNull();
        }

        @Test
        @DisplayName("create returns source with empty properties (TODO impl)")
        void testCreateReturnsEmptyProperties() {
            ConfigSource source = provider.create("http://config-server/app", Map.of());
            assertThat(source.getProperties()).isEmpty();
        }

        @Test
        @DisplayName("create source name contains URI")
        void testCreateSourceName() {
            String uri = "http://config-server/app";
            ConfigSource source = provider.create(uri, Map.of());
            assertThat(source.getName()).contains(uri);
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
