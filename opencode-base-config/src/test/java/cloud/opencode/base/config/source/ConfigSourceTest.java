package cloud.opencode.base.config.source;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ConfigSource interface and its default methods.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("ConfigSource Interface Tests")
class ConfigSourceTest {

    @Nested
    @DisplayName("Default Method Tests")
    class DefaultMethodTests {

        @Test
        @DisplayName("getProperty returns value from getProperties")
        void testGetPropertyDefault() {
            ConfigSource source = new TestConfigSource(Map.of("key", "value"));
            assertThat(source.getProperty("key")).isEqualTo("value");
        }

        @Test
        @DisplayName("getProperty returns null for missing key")
        void testGetPropertyMissing() {
            ConfigSource source = new TestConfigSource(Map.of("key", "value"));
            assertThat(source.getProperty("missing")).isNull();
        }

        @Test
        @DisplayName("default getPriority is 0")
        void testDefaultPriority() {
            ConfigSource source = new TestConfigSource(Map.of());
            assertThat(source.getPriority()).isEqualTo(0);
        }

        @Test
        @DisplayName("default supportsReload is false")
        void testDefaultSupportsReload() {
            ConfigSource source = new TestConfigSource(Map.of());
            assertThat(source.supportsReload()).isFalse();
        }

        @Test
        @DisplayName("default reload is no-op")
        void testDefaultReload() {
            ConfigSource source = new TestConfigSource(Map.of());
            assertThatCode(() -> source.reload()).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Custom Implementation Tests")
    class CustomImplementationTests {

        @Test
        @DisplayName("custom priority overrides default")
        void testCustomPriority() {
            ConfigSource source = new ConfigSource() {
                @Override
                public String getName() { return "custom"; }
                @Override
                public Map<String, String> getProperties() { return Map.of(); }
                @Override
                public int getPriority() { return 200; }
            };
            assertThat(source.getPriority()).isEqualTo(200);
        }

        @Test
        @DisplayName("custom supportsReload overrides default")
        void testCustomSupportsReload() {
            ConfigSource source = new ConfigSource() {
                @Override
                public String getName() { return "reloadable"; }
                @Override
                public Map<String, String> getProperties() { return Map.of(); }
                @Override
                public boolean supportsReload() { return true; }
            };
            assertThat(source.supportsReload()).isTrue();
        }
    }

    @Nested
    @DisplayName("getProperty Default Method Tests")
    class GetPropertyTests {

        @Test
        @DisplayName("getProperty delegates to getProperties map lookup")
        void testGetPropertyDelegates() {
            Map<String, String> props = Map.of("a", "1", "b", "2", "c", "3");
            ConfigSource source = new TestConfigSource(props);

            assertThat(source.getProperty("a")).isEqualTo("1");
            assertThat(source.getProperty("b")).isEqualTo("2");
            assertThat(source.getProperty("c")).isEqualTo("3");
            assertThat(source.getProperty("d")).isNull();
        }
    }

    // Test helper
    static class TestConfigSource implements ConfigSource {
        private final Map<String, String> properties;

        TestConfigSource(Map<String, String> properties) {
            this.properties = properties;
        }

        @Override
        public String getName() {
            return "test";
        }

        @Override
        public Map<String, String> getProperties() {
            return properties;
        }
    }
}
