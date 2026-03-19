package cloud.opencode.base.config.advanced;

import cloud.opencode.base.config.OpenConfigException;
import cloud.opencode.base.config.source.ConfigSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ConfigSourceFactory.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("ConfigSourceFactory Tests")
class ConfigSourceFactoryTest {

    @Nested
    @DisplayName("Built-in URI Pattern Tests")
    class BuiltInUriTests {

        @Test
        @DisplayName("creates source for classpath URI")
        void testClasspathUri() {
            ConfigSource source = ConfigSourceFactory.create("classpath:application.properties");
            assertThat(source).isNotNull();
        }

        @Test
        @DisplayName("creates source for env URI")
        void testEnvUri() {
            ConfigSource source = ConfigSourceFactory.create("env:APP_");
            assertThat(source).isNotNull();
        }

        @Test
        @DisplayName("creates source for file URI")
        void testFileUri() {
            ConfigSource source = ConfigSourceFactory.create("file:/tmp/nonexistent.properties");
            assertThat(source).isNotNull();
        }
    }

    @Nested
    @DisplayName("Unsupported URI Tests")
    class UnsupportedUriTests {

        @Test
        @DisplayName("throws for unsupported URI scheme")
        void testUnsupportedScheme() {
            assertThatThrownBy(() -> ConfigSourceFactory.create("unknown://something"))
                    .isInstanceOf(OpenConfigException.class);
        }

        @Test
        @DisplayName("throws for random string URI")
        void testRandomString() {
            assertThatThrownBy(() -> ConfigSourceFactory.create("just-a-string"))
                    .isInstanceOf(OpenConfigException.class);
        }
    }

    @Nested
    @DisplayName("Options Tests")
    class OptionsTests {

        @Test
        @DisplayName("create with empty options works for built-in URIs")
        void testWithEmptyOptions() {
            ConfigSource source = ConfigSourceFactory.create("classpath:test.properties", java.util.Map.of());
            assertThat(source).isNotNull();
        }

        @Test
        @DisplayName("create without options delegates to create with empty map")
        void testWithoutOptions() {
            ConfigSource source1 = ConfigSourceFactory.create("env:TEST_");
            ConfigSource source2 = ConfigSourceFactory.create("env:TEST_", java.util.Map.of());
            assertThat(source1).isNotNull();
            assertThat(source2).isNotNull();
        }
    }
}
