package cloud.opencode.base.config.bind;

import cloud.opencode.base.config.Config;
import cloud.opencode.base.config.ConfigBuilder;
import cloud.opencode.base.config.converter.ConverterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * DefaultValue on POJO fields test
 * POJO字段上的DefaultValue测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.3
 */
@DisplayName("DefaultValue POJO 绑定测试")
class DefaultValuePojoTest {

    @Nested
    @DisplayName("默认值应用测试")
    class DefaultValueAppliedTests {

        @Test
        @DisplayName("int字段使用默认值")
        void testIntDefaultValue() {
            Config config = new ConfigBuilder()
                    .addProperties(Map.of())
                    .disablePlaceholders()
                    .build();

            ConfigBinder binder = new ConfigBinder(config, ConverterRegistry.defaults());
            ServerConfig result = binder.bind("server", ServerConfig.class);

            assertThat(result.port).isEqualTo(8080);
        }

        @Test
        @DisplayName("String字段使用默认值")
        void testStringDefaultValue() {
            Config config = new ConfigBuilder()
                    .addProperties(Map.of())
                    .disablePlaceholders()
                    .build();

            ConfigBinder binder = new ConfigBinder(config, ConverterRegistry.defaults());
            ServerConfig result = binder.bind("server", ServerConfig.class);

            assertThat(result.host).isEqualTo("localhost");
        }

        @Test
        @DisplayName("boolean字段使用默认值")
        void testBooleanDefaultValue() {
            Config config = new ConfigBuilder()
                    .addProperties(Map.of())
                    .disablePlaceholders()
                    .build();

            ConfigBinder binder = new ConfigBinder(config, ConverterRegistry.defaults());
            ServerConfig result = binder.bind("server", ServerConfig.class);

            assertThat(result.enabled).isTrue();
        }

        @Test
        @DisplayName("Duration字段使用默认值")
        void testDurationDefaultValue() {
            Config config = new ConfigBuilder()
                    .addProperties(Map.of())
                    .disablePlaceholders()
                    .build();

            ConfigBinder binder = new ConfigBinder(config, ConverterRegistry.defaults());
            ServerConfig result = binder.bind("server", ServerConfig.class);

            assertThat(result.timeout).isEqualTo(Duration.ofSeconds(30));
        }

        @Test
        @DisplayName("所有默认值一起应用")
        void testAllDefaultValuesApplied() {
            Config config = new ConfigBuilder()
                    .addProperties(Map.of())
                    .disablePlaceholders()
                    .build();

            ConfigBinder binder = new ConfigBinder(config, ConverterRegistry.defaults());
            ServerConfig result = binder.bind("server", ServerConfig.class);

            assertThat(result.host).isEqualTo("localhost");
            assertThat(result.port).isEqualTo(8080);
            assertThat(result.enabled).isTrue();
            assertThat(result.timeout).isEqualTo(Duration.ofSeconds(30));
        }
    }

    @Nested
    @DisplayName("配置值覆盖默认值测试")
    class ConfigOverridesDefaultTests {

        @Test
        @DisplayName("配置值覆盖String默认值")
        void testConfigOverridesStringDefault() {
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("server.host", "192.168.1.1"))
                    .disablePlaceholders()
                    .build();

            ConfigBinder binder = new ConfigBinder(config, ConverterRegistry.defaults());
            ServerConfig result = binder.bind("server", ServerConfig.class);

            assertThat(result.host).isEqualTo("192.168.1.1");
        }

        @Test
        @DisplayName("配置值覆盖int默认值")
        void testConfigOverridesIntDefault() {
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("server.port", "9090"))
                    .disablePlaceholders()
                    .build();

            ConfigBinder binder = new ConfigBinder(config, ConverterRegistry.defaults());
            ServerConfig result = binder.bind("server", ServerConfig.class);

            assertThat(result.port).isEqualTo(9090);
        }

        @Test
        @DisplayName("配置值覆盖boolean默认值")
        void testConfigOverridesBooleanDefault() {
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("server.enabled", "false"))
                    .disablePlaceholders()
                    .build();

            ConfigBinder binder = new ConfigBinder(config, ConverterRegistry.defaults());
            ServerConfig result = binder.bind("server", ServerConfig.class);

            assertThat(result.enabled).isFalse();
        }

        @Test
        @DisplayName("配置值覆盖Duration默认值")
        void testConfigOverridesDurationDefault() {
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("server.timeout", "5m"))
                    .disablePlaceholders()
                    .build();

            ConfigBinder binder = new ConfigBinder(config, ConverterRegistry.defaults());
            ServerConfig result = binder.bind("server", ServerConfig.class);

            assertThat(result.timeout).isEqualTo(Duration.ofMinutes(5));
        }

        @Test
        @DisplayName("部分配置值覆盖,部分使用默认值")
        void testPartialOverride() {
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("server.host", "example.com", "server.port", "443"))
                    .disablePlaceholders()
                    .build();

            ConfigBinder binder = new ConfigBinder(config, ConverterRegistry.defaults());
            ServerConfig result = binder.bind("server", ServerConfig.class);

            assertThat(result.host).isEqualTo("example.com");
            assertThat(result.port).isEqualTo(443);
            assertThat(result.enabled).isTrue(); // default
            assertThat(result.timeout).isEqualTo(Duration.ofSeconds(30)); // default
        }
    }

    @Nested
    @DisplayName("嵌套POJO默认值测试")
    class NestedPojoDefaultValueTests {

        @Test
        @DisplayName("嵌套POJO字段使用默认值")
        void testNestedPojoDefaultValue() {
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("app.name", "MyApp"))
                    .disablePlaceholders()
                    .build();

            ConfigBinder binder = new ConfigBinder(config, ConverterRegistry.defaults());
            AppConfig result = binder.bind("app", AppConfig.class);

            assertThat(result.name).isEqualTo("MyApp");
            assertThat(result.server).isNotNull();
            assertThat(result.server.host).isEqualTo("localhost");
            assertThat(result.server.port).isEqualTo(8080);
            assertThat(result.server.enabled).isTrue();
            assertThat(result.server.timeout).isEqualTo(Duration.ofSeconds(30));
        }

        @Test
        @DisplayName("嵌套POJO字段被配置值覆盖")
        void testNestedPojoOverride() {
            Config config = new ConfigBuilder()
                    .addProperties(Map.of(
                            "app.name", "MyApp",
                            "app.server.host", "prod.example.com",
                            "app.server.port", "443"
                    ))
                    .disablePlaceholders()
                    .build();

            ConfigBinder binder = new ConfigBinder(config, ConverterRegistry.defaults());
            AppConfig result = binder.bind("app", AppConfig.class);

            assertThat(result.name).isEqualTo("MyApp");
            assertThat(result.server.host).isEqualTo("prod.example.com");
            assertThat(result.server.port).isEqualTo(443);
            assertThat(result.server.enabled).isTrue(); // default
            assertThat(result.server.timeout).isEqualTo(Duration.ofSeconds(30)); // default
        }
    }

    @Nested
    @DisplayName("无默认值字段测试")
    class NoDefaultValueTests {

        @Test
        @DisplayName("无默认值且无配置的字段保持Java默认值")
        void testFieldWithoutDefaultValueStaysJavaDefault() {
            Config config = new ConfigBuilder()
                    .addProperties(Map.of())
                    .disablePlaceholders()
                    .build();

            ConfigBinder binder = new ConfigBinder(config, ConverterRegistry.defaults());
            MixedConfig result = binder.bind("mixed", MixedConfig.class);

            assertThat(result.withDefault).isEqualTo("fallback");
            assertThat(result.withoutDefault).isNull();
        }
    }

    // ===== Test POJO classes =====

    public static class ServerConfig {
        @DefaultValue("localhost")
        private String host;

        @DefaultValue("8080")
        private int port;

        @DefaultValue("true")
        private boolean enabled;

        @DefaultValue("30s")
        private Duration timeout;

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public Duration getTimeout() { return timeout; }
        public void setTimeout(Duration timeout) { this.timeout = timeout; }
    }

    public static class AppConfig {
        private String name;

        @NestedConfig
        private ServerConfig server;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public ServerConfig getServer() { return server; }
        public void setServer(ServerConfig server) { this.server = server; }
    }

    public static class MixedConfig {
        @DefaultValue("fallback")
        private String withDefault;

        private String withoutDefault;

        public String getWithDefault() { return withDefault; }
        public void setWithDefault(String withDefault) { this.withDefault = withDefault; }
        public String getWithoutDefault() { return withoutDefault; }
        public void setWithoutDefault(String withoutDefault) { this.withoutDefault = withoutDefault; }
    }
}
