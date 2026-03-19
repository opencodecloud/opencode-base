package cloud.opencode.base.config.advanced;

import cloud.opencode.base.config.source.ConfigSource;
import cloud.opencode.base.config.source.InMemoryConfigSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ConfigSourceProvider SPI.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("ConfigSourceProvider Tests")
class ConfigSourceProviderTest {

    @Nested
    @DisplayName("Interface Contract Tests")
    class InterfaceContractTests {

        @Test
        @DisplayName("supports returns true for matching URI")
        void testSupportsMatchingUri() {
            ConfigSourceProvider provider = new TestProvider();
            assertThat(provider.supports("test://myconfig")).isTrue();
        }

        @Test
        @DisplayName("supports returns false for non-matching URI")
        void testSupportsNonMatchingUri() {
            ConfigSourceProvider provider = new TestProvider();
            assertThat(provider.supports("other://myconfig")).isFalse();
        }

        @Test
        @DisplayName("create returns a ConfigSource")
        void testCreate() {
            ConfigSourceProvider provider = new TestProvider();
            ConfigSource source = provider.create("test://myconfig", Map.of());
            assertThat(source).isNotNull();
            assertThat(source.getName()).isEqualTo("test[test://myconfig]");
        }
    }

    @Nested
    @DisplayName("Default Priority Tests")
    class DefaultPriorityTests {

        @Test
        @DisplayName("default priority is 0")
        void testDefaultPriority() {
            ConfigSourceProvider provider = new TestProvider();
            assertThat(provider.priority()).isEqualTo(0);
        }

        @Test
        @DisplayName("custom priority overrides default")
        void testCustomPriority() {
            ConfigSourceProvider provider = new ConfigSourceProvider() {
                @Override
                public boolean supports(String uri) { return false; }
                @Override
                public ConfigSource create(String uri, Map<String, Object> options) {
                    return new InMemoryConfigSource(Map.of());
                }
                @Override
                public int priority() { return 50; }
            };
            assertThat(provider.priority()).isEqualTo(50);
        }
    }

    // Test helper
    static class TestProvider implements ConfigSourceProvider {
        @Override
        public boolean supports(String uri) {
            return uri.startsWith("test://");
        }

        @Override
        public ConfigSource create(String uri, Map<String, Object> options) {
            return new InMemoryConfigSource("test[" + uri + "]", Map.of());
        }
    }
}
