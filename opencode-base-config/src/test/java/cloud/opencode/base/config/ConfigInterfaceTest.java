package cloud.opencode.base.config;

import cloud.opencode.base.config.source.InMemoryConfigSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for the Config interface contract through DefaultConfig implementation.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("Config Interface Tests")
class ConfigInterfaceTest {

    private Config config;

    @BeforeEach
    void setUp() {
        config = new ConfigBuilder()
                .addProperties(Map.of(
                        "string.key", "hello",
                        "int.key", "42",
                        "long.key", "9999999999",
                        "double.key", "3.14",
                        "bool.key", "true",
                        "duration.key", "30s",
                        "list.key", "a,b,c",
                        "prefix.sub1", "val1",
                        "prefix.sub2", "val2"
                ))
                .disablePlaceholders()
                .build();
    }

    @Nested
    @DisplayName("String Retrieval Tests")
    class StringRetrievalTests {

        @Test
        @DisplayName("getString returns value for existing key")
        void testGetStringExistingKey() {
            assertThat(config.getString("string.key")).isEqualTo("hello");
        }

        @Test
        @DisplayName("getString throws for missing key")
        void testGetStringMissingKey() {
            assertThatThrownBy(() -> config.getString("missing.key"))
                    .isInstanceOf(OpenConfigException.class);
        }

        @Test
        @DisplayName("getString with default returns value for existing key")
        void testGetStringDefaultExistingKey() {
            assertThat(config.getString("string.key", "default")).isEqualTo("hello");
        }

        @Test
        @DisplayName("getString with default returns default for missing key")
        void testGetStringDefaultMissingKey() {
            assertThat(config.getString("missing.key", "default")).isEqualTo("default");
        }
    }

    @Nested
    @DisplayName("Integer Retrieval Tests")
    class IntRetrievalTests {

        @Test
        @DisplayName("getInt returns value for existing key")
        void testGetIntExistingKey() {
            assertThat(config.getInt("int.key")).isEqualTo(42);
        }

        @Test
        @DisplayName("getInt throws for missing key")
        void testGetIntMissingKey() {
            assertThatThrownBy(() -> config.getInt("missing.key"))
                    .isInstanceOf(OpenConfigException.class);
        }

        @Test
        @DisplayName("getInt with default returns value for existing key")
        void testGetIntDefaultExistingKey() {
            assertThat(config.getInt("int.key", 99)).isEqualTo(42);
        }

        @Test
        @DisplayName("getInt with default returns default for missing key")
        void testGetIntDefaultMissingKey() {
            assertThat(config.getInt("missing.key", 99)).isEqualTo(99);
        }
    }

    @Nested
    @DisplayName("Long Retrieval Tests")
    class LongRetrievalTests {

        @Test
        @DisplayName("getLong returns value for existing key")
        void testGetLongExistingKey() {
            assertThat(config.getLong("long.key")).isEqualTo(9999999999L);
        }

        @Test
        @DisplayName("getLong throws for missing key")
        void testGetLongMissingKey() {
            assertThatThrownBy(() -> config.getLong("missing.key"))
                    .isInstanceOf(OpenConfigException.class);
        }

        @Test
        @DisplayName("getLong with default returns value for existing key")
        void testGetLongDefaultExistingKey() {
            assertThat(config.getLong("long.key", 0L)).isEqualTo(9999999999L);
        }

        @Test
        @DisplayName("getLong with default returns default for missing key")
        void testGetLongDefaultMissingKey() {
            assertThat(config.getLong("missing.key", 100L)).isEqualTo(100L);
        }
    }

    @Nested
    @DisplayName("Double Retrieval Tests")
    class DoubleRetrievalTests {

        @Test
        @DisplayName("getDouble returns value for existing key")
        void testGetDoubleExistingKey() {
            assertThat(config.getDouble("double.key")).isEqualTo(3.14);
        }

        @Test
        @DisplayName("getDouble throws for missing key")
        void testGetDoubleMissingKey() {
            assertThatThrownBy(() -> config.getDouble("missing.key"))
                    .isInstanceOf(OpenConfigException.class);
        }

        @Test
        @DisplayName("getDouble with default returns default for missing key")
        void testGetDoubleDefaultMissingKey() {
            assertThat(config.getDouble("missing.key", 1.0)).isEqualTo(1.0);
        }
    }

    @Nested
    @DisplayName("Boolean Retrieval Tests")
    class BooleanRetrievalTests {

        @Test
        @DisplayName("getBoolean returns value for existing key")
        void testGetBooleanExistingKey() {
            assertThat(config.getBoolean("bool.key")).isTrue();
        }

        @Test
        @DisplayName("getBoolean throws for missing key")
        void testGetBooleanMissingKey() {
            assertThatThrownBy(() -> config.getBoolean("missing.key"))
                    .isInstanceOf(OpenConfigException.class);
        }

        @Test
        @DisplayName("getBoolean with default returns default for missing key")
        void testGetBooleanDefaultMissingKey() {
            assertThat(config.getBoolean("missing.key", false)).isFalse();
        }
    }

    @Nested
    @DisplayName("Duration Retrieval Tests")
    class DurationRetrievalTests {

        @Test
        @DisplayName("getDuration returns value for existing key")
        void testGetDurationExistingKey() {
            assertThat(config.getDuration("duration.key")).isEqualTo(Duration.ofSeconds(30));
        }

        @Test
        @DisplayName("getDuration throws for missing key")
        void testGetDurationMissingKey() {
            assertThatThrownBy(() -> config.getDuration("missing.key"))
                    .isInstanceOf(OpenConfigException.class);
        }

        @Test
        @DisplayName("getDuration with default returns default for missing key")
        void testGetDurationDefaultMissingKey() {
            Duration defaultDuration = Duration.ofMinutes(5);
            assertThat(config.getDuration("missing.key", defaultDuration))
                    .isEqualTo(defaultDuration);
        }
    }

    @Nested
    @DisplayName("Generic Retrieval Tests")
    class GenericRetrievalTests {

        @Test
        @DisplayName("get returns typed value")
        void testGetTypedValue() {
            assertThat(config.get("int.key", Integer.class)).isEqualTo(42);
        }

        @Test
        @DisplayName("get with default returns typed value for existing key")
        void testGetTypedDefaultExistingKey() {
            assertThat(config.get("int.key", Integer.class, 99)).isEqualTo(42);
        }

        @Test
        @DisplayName("get with default returns default for missing key")
        void testGetTypedDefaultMissingKey() {
            assertThat(config.get("missing.key", Integer.class, 99)).isEqualTo(99);
        }

        @Test
        @DisplayName("getList returns list of values")
        void testGetList() {
            List<String> list = config.getList("list.key", String.class);
            assertThat(list).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("getMap returns map of sub-properties")
        void testGetMap() {
            Map<String, String> map = config.getMap("prefix", String.class, String.class);
            assertThat(map).containsEntry("prefix.sub1", "val1")
                    .containsEntry("prefix.sub2", "val2");
        }
    }

    @Nested
    @DisplayName("Optional Retrieval Tests")
    class OptionalRetrievalTests {

        @Test
        @DisplayName("getOptional returns present for existing key")
        void testGetOptionalExistingKey() {
            Optional<String> result = config.getOptional("string.key");
            assertThat(result).isPresent().hasValue("hello");
        }

        @Test
        @DisplayName("getOptional returns empty for missing key")
        void testGetOptionalMissingKey() {
            Optional<String> result = config.getOptional("missing.key");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("getOptional with type returns typed value")
        void testGetOptionalTyped() {
            Optional<Integer> result = config.getOptional("int.key", Integer.class);
            assertThat(result).isPresent().hasValue(42);
        }

        @Test
        @DisplayName("getOptional with type returns empty for missing key")
        void testGetOptionalTypedMissing() {
            Optional<Integer> result = config.getOptional("missing.key", Integer.class);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Sub-Configuration Tests")
    class SubConfigTests {

        @Test
        @DisplayName("getSubConfig returns sub-configuration")
        void testGetSubConfig() {
            Config subConfig = config.getSubConfig("prefix");
            assertThat(subConfig.getString("sub1")).isEqualTo("val1");
            assertThat(subConfig.getString("sub2")).isEqualTo("val2");
        }

        @Test
        @DisplayName("getByPrefix returns properties with prefix")
        void testGetByPrefix() {
            Map<String, String> result = config.getByPrefix("prefix.");
            assertThat(result)
                    .containsEntry("prefix.sub1", "val1")
                    .containsEntry("prefix.sub2", "val2");
        }
    }

    @Nested
    @DisplayName("Configuration Check Tests")
    class ConfigCheckTests {

        @Test
        @DisplayName("hasKey returns true for existing key")
        void testHasKeyExisting() {
            assertThat(config.hasKey("string.key")).isTrue();
        }

        @Test
        @DisplayName("hasKey returns false for missing key")
        void testHasKeyMissing() {
            assertThat(config.hasKey("missing.key")).isFalse();
        }

        @Test
        @DisplayName("getKeys returns all keys")
        void testGetKeys() {
            Set<String> keys = config.getKeys();
            assertThat(keys).contains("string.key", "int.key", "long.key",
                    "double.key", "bool.key", "duration.key", "list.key");
        }
    }

    @Nested
    @DisplayName("Listener Tests")
    class ListenerTests {

        @Test
        @DisplayName("addListener does not throw")
        void testAddListener() {
            assertThatCode(() -> config.addListener(event -> {}))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("addListener with key does not throw")
        void testAddListenerWithKey() {
            assertThatCode(() -> config.addListener("some.key", event -> {}))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("removeListener does not throw")
        void testRemoveListener() {
            ConfigListener listener = event -> {};
            config.addListener(listener);
            assertThatCode(() -> config.removeListener(listener))
                    .doesNotThrowAnyException();
        }
    }
}
