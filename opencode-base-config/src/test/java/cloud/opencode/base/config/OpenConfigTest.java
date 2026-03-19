package cloud.opencode.base.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for OpenConfig global configuration facade.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("OpenConfig Tests")
class OpenConfigTest {

    @BeforeEach
    void setUp() {
        // Set a controlled global config for testing
        Config config = new ConfigBuilder()
                .addProperties(Map.of(
                        "test.string", "hello",
                        "test.int", "42",
                        "test.long", "9999999999",
                        "test.double", "3.14",
                        "test.bool", "true",
                        "test.duration", "30s",
                        "test.list", "a,b,c",
                        "prefix.key1", "v1",
                        "prefix.key2", "v2"
                ))
                .disablePlaceholders()
                .build();
        OpenConfig.setGlobal(config);
    }

    @AfterEach
    void tearDown() {
        // Reset to null so it will be lazily re-created
        OpenConfig.setGlobal(null);
    }

    @Nested
    @DisplayName("Global Configuration Management Tests")
    class GlobalConfigManagementTests {

        @Test
        @DisplayName("getGlobal returns non-null config")
        void testGetGlobalReturnsConfig() {
            assertThat(OpenConfig.getGlobal()).isNotNull();
        }

        @Test
        @DisplayName("setGlobal sets the global config")
        void testSetGlobal() {
            Config custom = new ConfigBuilder()
                    .addProperties(Map.of("custom.key", "custom.value"))
                    .build();
            OpenConfig.setGlobal(custom);

            assertThat(OpenConfig.getString("custom.key")).isEqualTo("custom.value");
        }

        @Test
        @DisplayName("builder returns new ConfigBuilder")
        void testBuilder() {
            ConfigBuilder builder = OpenConfig.builder();
            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("getGlobal creates default config if null")
        void testGetGlobalCreatesDefault() {
            OpenConfig.setGlobal(null);
            // getGlobal should not throw - it creates a default config
            Config config = OpenConfig.getGlobal();
            assertThat(config).isNotNull();
        }
    }

    @Nested
    @DisplayName("Static Delegate Method Tests")
    class StaticDelegateTests {

        @Test
        @DisplayName("getString delegates to global config")
        void testGetString() {
            assertThat(OpenConfig.getString("test.string")).isEqualTo("hello");
        }

        @Test
        @DisplayName("getString with default delegates to global config")
        void testGetStringDefault() {
            assertThat(OpenConfig.getString("missing", "fallback")).isEqualTo("fallback");
        }

        @Test
        @DisplayName("getInt delegates to global config")
        void testGetInt() {
            assertThat(OpenConfig.getInt("test.int")).isEqualTo(42);
        }

        @Test
        @DisplayName("getInt with default delegates to global config")
        void testGetIntDefault() {
            assertThat(OpenConfig.getInt("missing", 99)).isEqualTo(99);
        }

        @Test
        @DisplayName("getLong delegates to global config")
        void testGetLong() {
            assertThat(OpenConfig.getLong("test.long")).isEqualTo(9999999999L);
        }

        @Test
        @DisplayName("getLong with default delegates to global config")
        void testGetLongDefault() {
            assertThat(OpenConfig.getLong("missing", 100L)).isEqualTo(100L);
        }

        @Test
        @DisplayName("getDouble delegates to global config")
        void testGetDouble() {
            assertThat(OpenConfig.getDouble("test.double")).isEqualTo(3.14);
        }

        @Test
        @DisplayName("getDouble with default delegates to global config")
        void testGetDoubleDefault() {
            assertThat(OpenConfig.getDouble("missing", 1.0)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("getBoolean delegates to global config")
        void testGetBoolean() {
            assertThat(OpenConfig.getBoolean("test.bool")).isTrue();
        }

        @Test
        @DisplayName("getBoolean with default delegates to global config")
        void testGetBooleanDefault() {
            assertThat(OpenConfig.getBoolean("missing", false)).isFalse();
        }

        @Test
        @DisplayName("getDuration delegates to global config")
        void testGetDuration() {
            assertThat(OpenConfig.getDuration("test.duration")).isEqualTo(Duration.ofSeconds(30));
        }

        @Test
        @DisplayName("getDuration with default delegates to global config")
        void testGetDurationDefault() {
            Duration defaultVal = Duration.ofMinutes(5);
            assertThat(OpenConfig.getDuration("missing", defaultVal)).isEqualTo(defaultVal);
        }

        @Test
        @DisplayName("get with type delegates to global config")
        void testGetTyped() {
            assertThat(OpenConfig.get("test.int", Integer.class)).isEqualTo(42);
        }

        @Test
        @DisplayName("get with type and default delegates to global config")
        void testGetTypedDefault() {
            assertThat(OpenConfig.get("missing", Integer.class, 99)).isEqualTo(99);
        }

        @Test
        @DisplayName("getOptional delegates to global config")
        void testGetOptional() {
            Optional<String> result = OpenConfig.getOptional("test.string");
            assertThat(result).isPresent().hasValue("hello");
        }

        @Test
        @DisplayName("getOptional with type delegates to global config")
        void testGetOptionalTyped() {
            Optional<Integer> result = OpenConfig.getOptional("test.int", Integer.class);
            assertThat(result).isPresent().hasValue(42);
        }

        @Test
        @DisplayName("getList delegates to global config")
        void testGetList() {
            assertThat(OpenConfig.getList("test.list", String.class))
                    .containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("getMap delegates to global config")
        void testGetMap() {
            Map<String, String> map = OpenConfig.getMap("prefix", String.class, String.class);
            assertThat(map).containsEntry("prefix.key1", "v1").containsEntry("prefix.key2", "v2");
        }

        @Test
        @DisplayName("getSubConfig delegates to global config")
        void testGetSubConfig() {
            Config sub = OpenConfig.getSubConfig("prefix");
            assertThat(sub).isNotNull();
            assertThat(sub.getString("key1")).isEqualTo("v1");
        }

        @Test
        @DisplayName("getByPrefix delegates to global config")
        void testGetByPrefix() {
            Map<String, String> result = OpenConfig.getByPrefix("prefix.");
            assertThat(result).containsEntry("prefix.key1", "v1");
        }

        @Test
        @DisplayName("hasKey delegates to global config")
        void testHasKey() {
            assertThat(OpenConfig.hasKey("test.string")).isTrue();
            assertThat(OpenConfig.hasKey("missing")).isFalse();
        }

        @Test
        @DisplayName("getKeys delegates to global config")
        void testGetKeys() {
            Set<String> keys = OpenConfig.getKeys();
            assertThat(keys).contains("test.string", "test.int");
        }
    }

    @Nested
    @DisplayName("Quick Loading Method Tests")
    class QuickLoadingTests {

        @Test
        @DisplayName("loadFromClasspath returns config")
        void testLoadFromClasspath() {
            Config config = OpenConfig.loadFromClasspath("nonexistent.properties");
            assertThat(config).isNotNull();
        }

        @Test
        @DisplayName("loadFromProperties returns config with given properties")
        void testLoadFromProperties() {
            Map<String, String> props = Map.of("a", "1", "b", "2");
            Config config = OpenConfig.loadFromProperties(props);
            assertThat(config.getString("a")).isEqualTo("1");
            assertThat(config.getString("b")).isEqualTo("2");
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("OpenConfig cannot be instantiated")
        void testCannotInstantiate() {
            // OpenConfig has private constructor - verify it's final
            assertThat(OpenConfig.class).isFinal();
        }
    }
}
