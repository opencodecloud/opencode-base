package cloud.opencode.base.config.bind;

import cloud.opencode.base.config.Config;
import cloud.opencode.base.config.ConfigBuilder;
import cloud.opencode.base.config.OpenConfigException;
import cloud.opencode.base.config.converter.ConverterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * ConfigBinder 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("ConfigBinder 测试")
class ConfigBinderTest {

    private Config config;
    private ConfigBinder binder;

    @BeforeEach
    void setUp() {
        config = new ConfigBuilder()
                .addProperties(Map.of(
                        "app.name", "TestApp",
                        "app.version", "1.0.0",
                        "app.port", "8080",
                        "app.enabled", "true",
                        "app.max-connections", "100",
                        "database.url", "jdbc:mysql://localhost",
                        "database.username", "admin",
                        "database.password", "secret"
                ))
                .disablePlaceholders()
                .build();
        binder = new ConfigBinder(config, ConverterRegistry.defaults());
    }

    @Nested
    @DisplayName("bind测试")
    class BindTests {

        @Test
        @DisplayName("绑定简单属性")
        void testBindSimpleProperties() {
            AppConfig appConfig = binder.bind("app", AppConfig.class);

            assertThat(appConfig.name).isEqualTo("TestApp");
            assertThat(appConfig.version).isEqualTo("1.0.0");
        }

        @Test
        @DisplayName("绑定数值类型")
        void testBindNumericTypes() {
            AppConfig appConfig = binder.bind("app", AppConfig.class);

            assertThat(appConfig.port).isEqualTo(8080);
            assertThat(appConfig.maxConnections).isEqualTo(100);
        }

        @Test
        @DisplayName("绑定布尔类型")
        void testBindBooleanType() {
            AppConfig appConfig = binder.bind("app", AppConfig.class);

            assertThat(appConfig.enabled).isTrue();
        }

        @Test
        @DisplayName("缺失的可选字段 - 保持默认值")
        void testMissingOptionalField() {
            AppConfig appConfig = binder.bind("app", AppConfig.class);

            // description字段不在配置中,应该为null
            assertThat(appConfig.description).isNull();
        }
    }

    @Nested
    @DisplayName("bindTo测试")
    class BindToTests {

        @Test
        @DisplayName("绑定到现有实例")
        void testBindToExistingInstance() {
            AppConfig appConfig = new AppConfig();
            appConfig.description = "existing";

            binder.bindTo("app", appConfig);

            assertThat(appConfig.name).isEqualTo("TestApp");
            assertThat(appConfig.description).isEqualTo("existing"); // 保持不变
        }

        @Test
        @DisplayName("覆盖现有值")
        void testBindToOverwritesExisting() {
            AppConfig appConfig = new AppConfig();
            appConfig.name = "OldName";

            binder.bindTo("app", appConfig);

            assertThat(appConfig.name).isEqualTo("TestApp");
        }
    }

    @Nested
    @DisplayName("嵌套配置测试")
    class NestedConfigTests {

        @Test
        @DisplayName("绑定嵌套配置")
        void testBindNestedConfig() {
            Config nestedConfig = new ConfigBuilder()
                    .addProperties(Map.of(
                            "app.name", "NestedApp",
                            "app.server.host", "localhost",
                            "app.server.port", "9090"
                    ))
                    .disablePlaceholders()
                    .build();

            ConfigBinder nestedBinder = new ConfigBinder(nestedConfig, ConverterRegistry.defaults());
            AppWithNestedConfig appConfig = nestedBinder.bind("app", AppWithNestedConfig.class);

            assertThat(appConfig.name).isEqualTo("NestedApp");
            assertThat(appConfig.server).isNotNull();
            assertThat(appConfig.server.host).isEqualTo("localhost");
            assertThat(appConfig.server.port).isEqualTo(9090);
        }

        @Test
        @DisplayName("嵌套配置使用自定义前缀")
        void testNestedConfigCustomPrefix() {
            Config customPrefixConfig = new ConfigBuilder()
                    .addProperties(Map.of(
                            "app.name", "CustomPrefixApp",
                            "custom-db.url", "jdbc:h2:mem:test"
                    ))
                    .disablePlaceholders()
                    .build();

            ConfigBinder customBinder = new ConfigBinder(customPrefixConfig, ConverterRegistry.defaults());
            AppWithCustomPrefixNested appConfig = customBinder.bind("app", AppWithCustomPrefixNested.class);

            assertThat(appConfig.name).isEqualTo("CustomPrefixApp");
            assertThat(appConfig.database).isNotNull();
            assertThat(appConfig.database.url).isEqualTo("jdbc:h2:mem:test");
        }
    }

    @Nested
    @DisplayName("字段名转换测试")
    class FieldNameConversionTests {

        @Test
        @DisplayName("驼峰命名转换为kebab-case")
        void testCamelCaseToKebabCase() {
            AppConfig appConfig = binder.bind("app", AppConfig.class);

            // maxConnections 应该匹配 max-connections
            assertThat(appConfig.maxConnections).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("错误处理测试")
    class ErrorHandlingTests {

        @Test
        @DisplayName("无默认构造函数 - 抛出异常")
        void testNoDefaultConstructor() {
            assertThatThrownBy(() -> binder.bind("app", NoDefaultConstructorConfig.class))
                    .isInstanceOf(OpenConfigException.class);
        }
    }

    @Nested
    @DisplayName("空前缀测试")
    class EmptyPrefixTests {

        @Test
        @DisplayName("空前缀绑定")
        void testBindWithEmptyPrefix() {
            Config flatConfig = new ConfigBuilder()
                    .addProperties(Map.of("name", "FlatApp"))
                    .disablePlaceholders()
                    .build();

            ConfigBinder flatBinder = new ConfigBinder(flatConfig, ConverterRegistry.defaults());
            FlatConfig cfg = flatBinder.bind("", FlatConfig.class);

            assertThat(cfg.name).isEqualTo("FlatApp");
        }
    }

    // 测试用的配置类
    public static class AppConfig {
        private String name;
        private String version;
        private int port;
        private boolean enabled;
        private int maxConnections;
        private String description;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getMaxConnections() { return maxConnections; }
        public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class AppWithNestedConfig {
        private String name;
        @NestedConfig
        private ServerConfig server;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public ServerConfig getServer() { return server; }
        public void setServer(ServerConfig server) { this.server = server; }
    }

    public static class ServerConfig {
        private String host;
        private int port;

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
    }

    public static class AppWithCustomPrefixNested {
        private String name;
        @NestedConfig(prefix = "custom-db")
        private DatabaseConfig database;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public DatabaseConfig getDatabase() { return database; }
        public void setDatabase(DatabaseConfig database) { this.database = database; }
    }

    public static class DatabaseConfig {
        private String url;

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }

    public static class NoDefaultConstructorConfig {
        private final String name;

        public NoDefaultConstructorConfig(String name) {
            this.name = name;
        }

        public String getName() { return name; }
    }

    public static class FlatConfig {
        private String name;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
