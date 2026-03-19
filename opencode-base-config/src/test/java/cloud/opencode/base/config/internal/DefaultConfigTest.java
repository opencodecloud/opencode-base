package cloud.opencode.base.config.internal;

import cloud.opencode.base.config.*;
import cloud.opencode.base.config.converter.ConverterRegistry;
import cloud.opencode.base.config.source.CompositeConfigSource;
import cloud.opencode.base.config.source.InMemoryConfigSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * DefaultConfig 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("DefaultConfig 测试")
class DefaultConfigTest {

    private DefaultConfig config;
    private InMemoryConfigSource source;

    @BeforeEach
    void setUp() {
        source = new InMemoryConfigSource(Map.of(
                "app.name", "TestApp",
                "app.version", "1.0.0",
                "server.port", "8080",
                "server.host", "localhost",
                "feature.enabled", "true",
                "timeout", "30s",
                "rate", "3.14",
                "items", "a,b,c",
                "nested.value", "nested-value"
        ));
        CompositeConfigSource composite = new CompositeConfigSource(List.of(source));
        config = new DefaultConfig(composite, ConverterRegistry.defaults());
    }

    @Nested
    @DisplayName("getString测试")
    class GetStringTests {

        @Test
        @DisplayName("获取存在的字符串值")
        void testGetStringExists() {
            assertThat(config.getString("app.name")).isEqualTo("TestApp");
        }

        @Test
        @DisplayName("获取不存在的键 - 抛出异常")
        void testGetStringNotExists() {
            assertThatThrownBy(() -> config.getString("nonexistent"))
                    .isInstanceOf(OpenConfigException.class);
        }

        @Test
        @DisplayName("获取不存在的键 - 返回默认值")
        void testGetStringWithDefault() {
            assertThat(config.getString("nonexistent", "default")).isEqualTo("default");
        }

        @Test
        @DisplayName("获取存在的键 - 忽略默认值")
        void testGetStringExistsIgnoreDefault() {
            assertThat(config.getString("app.name", "default")).isEqualTo("TestApp");
        }
    }

    @Nested
    @DisplayName("getInt测试")
    class GetIntTests {

        @Test
        @DisplayName("获取整数值")
        void testGetInt() {
            assertThat(config.getInt("server.port")).isEqualTo(8080);
        }

        @Test
        @DisplayName("获取整数值 - 带默认值")
        void testGetIntWithDefault() {
            assertThat(config.getInt("nonexistent", 9090)).isEqualTo(9090);
        }

        @Test
        @DisplayName("整数值存在 - 忽略默认值")
        void testGetIntExistsIgnoreDefault() {
            assertThat(config.getInt("server.port", 9090)).isEqualTo(8080);
        }
    }

    @Nested
    @DisplayName("getLong测试")
    class GetLongTests {

        @Test
        @DisplayName("获取长整数值")
        void testGetLong() {
            assertThat(config.getLong("server.port")).isEqualTo(8080L);
        }

        @Test
        @DisplayName("获取长整数值 - 带默认值")
        void testGetLongWithDefault() {
            assertThat(config.getLong("nonexistent", 9999L)).isEqualTo(9999L);
        }
    }

    @Nested
    @DisplayName("getDouble测试")
    class GetDoubleTests {

        @Test
        @DisplayName("获取双精度值")
        void testGetDouble() {
            assertThat(config.getDouble("rate")).isEqualTo(3.14);
        }

        @Test
        @DisplayName("获取双精度值 - 带默认值")
        void testGetDoubleWithDefault() {
            assertThat(config.getDouble("nonexistent", 2.71)).isEqualTo(2.71);
        }
    }

    @Nested
    @DisplayName("getBoolean测试")
    class GetBooleanTests {

        @Test
        @DisplayName("获取布尔值")
        void testGetBoolean() {
            assertThat(config.getBoolean("feature.enabled")).isTrue();
        }

        @Test
        @DisplayName("获取布尔值 - 带默认值")
        void testGetBooleanWithDefault() {
            assertThat(config.getBoolean("nonexistent", false)).isFalse();
        }
    }

    @Nested
    @DisplayName("getDuration测试")
    class GetDurationTests {

        @Test
        @DisplayName("获取Duration值")
        void testGetDuration() {
            assertThat(config.getDuration("timeout")).isEqualTo(Duration.ofSeconds(30));
        }

        @Test
        @DisplayName("获取Duration值 - 带默认值")
        void testGetDurationWithDefault() {
            Duration defaultDuration = Duration.ofMinutes(5);
            assertThat(config.getDuration("nonexistent", defaultDuration)).isEqualTo(defaultDuration);
        }
    }

    @Nested
    @DisplayName("get泛型测试")
    class GenericGetTests {

        @Test
        @DisplayName("获取泛型类型值")
        void testGenericGet() {
            Integer port = config.get("server.port", Integer.class);
            assertThat(port).isEqualTo(8080);
        }

        @Test
        @DisplayName("获取泛型类型值 - 带默认值")
        void testGenericGetWithDefault() {
            Integer port = config.get("nonexistent", Integer.class, 3000);
            assertThat(port).isEqualTo(3000);
        }
    }

    @Nested
    @DisplayName("getList测试")
    class GetListTests {

        @Test
        @DisplayName("获取列表值")
        void testGetList() {
            List<String> items = config.getList("items", String.class);
            assertThat(items).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("getOptional测试")
    class GetOptionalTests {

        @Test
        @DisplayName("获取存在的Optional值")
        void testGetOptionalExists() {
            Optional<String> value = config.getOptional("app.name");
            assertThat(value).isPresent().contains("TestApp");
        }

        @Test
        @DisplayName("获取不存在的Optional值")
        void testGetOptionalNotExists() {
            Optional<String> value = config.getOptional("nonexistent");
            assertThat(value).isEmpty();
        }

        @Test
        @DisplayName("获取类型化的Optional值")
        void testGetOptionalTyped() {
            Optional<Integer> value = config.getOptional("server.port", Integer.class);
            assertThat(value).isPresent().contains(8080);
        }
    }

    @Nested
    @DisplayName("getSubConfig测试")
    class GetSubConfigTests {

        @Test
        @DisplayName("获取子配置")
        void testGetSubConfig() {
            Config serverConfig = config.getSubConfig("server");

            assertThat(serverConfig.getString("port")).isEqualTo("8080");
            assertThat(serverConfig.getString("host")).isEqualTo("localhost");
        }

        @Test
        @DisplayName("子配置的getKeys")
        void testSubConfigGetKeys() {
            Config serverConfig = config.getSubConfig("server");

            assertThat(serverConfig.getKeys()).contains("port", "host");
        }
    }

    @Nested
    @DisplayName("getByPrefix测试")
    class GetByPrefixTests {

        @Test
        @DisplayName("按前缀获取配置")
        void testGetByPrefix() {
            Map<String, String> serverProps = config.getByPrefix("server.");

            assertThat(serverProps).containsEntry("server.port", "8080");
            assertThat(serverProps).containsEntry("server.host", "localhost");
        }

        @Test
        @DisplayName("按前缀获取 - 无匹配")
        void testGetByPrefixNoMatch() {
            Map<String, String> props = config.getByPrefix("nonexistent.");
            assertThat(props).isEmpty();
        }
    }

    @Nested
    @DisplayName("hasKey测试")
    class HasKeyTests {

        @Test
        @DisplayName("键存在")
        void testHasKeyExists() {
            assertThat(config.hasKey("app.name")).isTrue();
        }

        @Test
        @DisplayName("键不存在")
        void testHasKeyNotExists() {
            assertThat(config.hasKey("nonexistent")).isFalse();
        }
    }

    @Nested
    @DisplayName("getKeys测试")
    class GetKeysTests {

        @Test
        @DisplayName("获取所有键")
        void testGetKeys() {
            Set<String> keys = config.getKeys();

            assertThat(keys).contains("app.name", "app.version", "server.port");
        }
    }

    @Nested
    @DisplayName("监听器测试")
    class ListenerTests {

        @Test
        @DisplayName("添加全局监听器")
        void testAddGlobalListener() {
            List<ConfigChangeEvent> events = new ArrayList<>();
            config.addListener(events::add);

            assertThat(events).isEmpty(); // 仅添加监听器,不触发事件
        }

        @Test
        @DisplayName("添加键监听器")
        void testAddKeyListener() {
            List<ConfigChangeEvent> events = new ArrayList<>();
            config.addListener("app.name", events::add);

            assertThat(events).isEmpty();
        }

        @Test
        @DisplayName("移除监听器")
        void testRemoveListener() {
            List<ConfigChangeEvent> events = new ArrayList<>();
            ConfigListener listener = events::add;
            config.addListener(listener);
            config.removeListener(listener);

            assertThat(events).isEmpty();
        }
    }

    @Nested
    @DisplayName("占位符解析测试")
    class PlaceholderResolutionTests {

        @Test
        @DisplayName("启用占位符解析")
        void testEnablePlaceholderResolution() {
            InMemoryConfigSource placeholderSource = new InMemoryConfigSource(Map.of(
                    "base", "http://localhost",
                    "url", "${base}/api"
            ));
            CompositeConfigSource composite = new CompositeConfigSource(List.of(placeholderSource));
            DefaultConfig cfg = new DefaultConfig(composite, ConverterRegistry.defaults());
            cfg.enablePlaceholderResolution();

            assertThat(cfg.getString("url")).isEqualTo("http://localhost/api");
        }
    }

    @Nested
    @DisplayName("bind测试")
    class BindTests {

        @Test
        @DisplayName("绑定到POJO")
        void testBindToPojo() {
            InMemoryConfigSource bindSource = new InMemoryConfigSource(Map.of(
                    "my-app.name", "BoundApp",
                    "my-app.version", "2.0.0"
            ));
            CompositeConfigSource composite = new CompositeConfigSource(List.of(bindSource));
            DefaultConfig cfg = new DefaultConfig(composite, ConverterRegistry.defaults());

            SimpleConfig bound = cfg.bind("my-app", SimpleConfig.class);

            assertThat(bound.name).isEqualTo("BoundApp");
            assertThat(bound.version).isEqualTo("2.0.0");
        }
    }

    // 用于测试绑定的简单类
    public static class SimpleConfig {
        private String name;
        private String version;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
    }
}
