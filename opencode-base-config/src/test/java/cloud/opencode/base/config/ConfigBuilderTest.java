package cloud.opencode.base.config;

import cloud.opencode.base.config.source.InMemoryConfigSource;
import cloud.opencode.base.config.validation.RequiredValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * ConfigBuilder 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("ConfigBuilder 测试")
class ConfigBuilderTest {

    @Nested
    @DisplayName("添加配置源测试")
    class AddSourceTests {

        @Test
        @DisplayName("添加属性映射")
        void testAddProperties() {
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("key1", "value1", "key2", "value2"))
                    .build();

            assertThat(config.getString("key1")).isEqualTo("value1");
            assertThat(config.getString("key2")).isEqualTo("value2");
        }

        @Test
        @DisplayName("添加多个属性映射 - 后添加的优先级更高")
        void testAddMultipleProperties() {
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("key", "value1"))
                    .addProperties(Map.of("key", "value2"))
                    .build();

            assertThat(config.getString("key")).isEqualTo("value2");
        }

        @Test
        @DisplayName("添加自定义配置源")
        void testAddSource() {
            InMemoryConfigSource source = new InMemoryConfigSource(Map.of("custom.key", "custom.value"));
            Config config = new ConfigBuilder()
                    .addSource(source)
                    .build();

            assertThat(config.getString("custom.key")).isEqualTo("custom.value");
        }

        @Test
        @DisplayName("添加系统属性")
        void testAddSystemProperties() {
            String testKey = "test.builder.system.prop." + System.currentTimeMillis();
            System.setProperty(testKey, "test-value");

            try {
                Config config = new ConfigBuilder()
                        .addSystemProperties()
                        .build();

                assertThat(config.getString(testKey)).isEqualTo("test-value");
            } finally {
                System.clearProperty(testKey);
            }
        }

        @Test
        @DisplayName("添加环境变量")
        void testAddEnvironmentVariables() {
            Config config = new ConfigBuilder()
                    .addEnvironmentVariables()
                    .build();

            // PATH 环境变量通常存在
            assertThat(config.hasKey("PATH") || config.hasKey("path")).isTrue();
        }

        @Test
        @DisplayName("添加命令行参数")
        void testAddCommandLineArgs() {
            String[] args = {"--app.name=MyApp", "--app.port=8080"};
            Config config = new ConfigBuilder()
                    .addCommandLineArgs(args)
                    .build();

            assertThat(config.getString("app.name")).isEqualTo("MyApp");
            assertThat(config.getString("app.port")).isEqualTo("8080");
        }
    }

    @Nested
    @DisplayName("类型转换器测试")
    class ConverterTests {

        @Test
        @DisplayName("注册自定义转换器")
        void testRegisterConverter() {
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("custom.value", "hello"))
                    .registerConverter(StringBuilder.class, StringBuilder::new)
                    .build();

            StringBuilder result = config.get("custom.value", StringBuilder.class);
            assertThat(result.toString()).isEqualTo("hello");
        }

        @Test
        @DisplayName("覆盖默认转换器")
        void testOverrideDefaultConverter() {
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("number", "5"))
                    .registerConverter(Integer.class, s -> Integer.parseInt(s) * 2)
                    .build();

            // 使用get方法获取Integer.class类型,这样会使用注册的转换器
            assertThat(config.get("number", Integer.class)).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("占位符测试")
    class PlaceholderTests {

        @Test
        @DisplayName("默认启用占位符解析")
        void testPlaceholderEnabled() {
            Config config = new ConfigBuilder()
                    .addProperties(Map.of(
                            "base.url", "http://localhost",
                            "api.url", "${base.url}/api"
                    ))
                    .build();

            assertThat(config.getString("api.url")).isEqualTo("http://localhost/api");
        }

        @Test
        @DisplayName("禁用占位符解析")
        void testDisablePlaceholders() {
            Config config = new ConfigBuilder()
                    .addProperties(Map.of(
                            "base.url", "http://localhost",
                            "api.url", "${base.url}/api"
                    ))
                    .disablePlaceholders()
                    .build();

            assertThat(config.getString("api.url")).isEqualTo("${base.url}/api");
        }
    }

    @Nested
    @DisplayName("热重载测试")
    class HotReloadTests {

        @Test
        @DisplayName("启用热重载")
        void testEnableHotReload() {
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("key", "value"))
                    .enableHotReload()
                    .build();

            assertThat(config).isNotNull();
        }

        @Test
        @DisplayName("设置热重载间隔")
        void testHotReloadInterval() {
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("key", "value"))
                    .enableHotReload()
                    .hotReloadInterval(Duration.ofSeconds(10))
                    .build();

            assertThat(config).isNotNull();
        }
    }

    @Nested
    @DisplayName("验证测试")
    class ValidationTests {

        @Test
        @DisplayName("必填键验证通过")
        void testRequiredValidationPass() {
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("required.key", "value"))
                    .required("required.key")
                    .build();

            assertThat(config.getString("required.key")).isEqualTo("value");
        }

        @Test
        @DisplayName("必填键验证失败 - 抛出异常")
        void testRequiredValidationFail() {
            assertThatThrownBy(() -> new ConfigBuilder()
                    .addProperties(Map.of("other.key", "value"))
                    .required("required.key")
                    .build())
                    .isInstanceOf(OpenConfigException.class);
        }

        @Test
        @DisplayName("添加自定义验证器")
        void testAddValidator() {
            RequiredValidator validator = new RequiredValidator("must.exist");

            assertThatThrownBy(() -> new ConfigBuilder()
                    .addProperties(Map.of("other", "value"))
                    .addValidator(validator)
                    .build())
                    .isInstanceOf(OpenConfigException.class);
        }

        @Test
        @DisplayName("多个必填键")
        void testMultipleRequired() {
            Config config = new ConfigBuilder()
                    .addProperties(Map.of(
                            "db.url", "jdbc:mysql://localhost",
                            "db.user", "admin",
                            "db.pass", "secret"
                    ))
                    .required("db.url", "db.user", "db.pass")
                    .build();

            assertThat(config.getString("db.url")).isEqualTo("jdbc:mysql://localhost");
        }
    }

    @Nested
    @DisplayName("构建测试")
    class BuildTests {

        @Test
        @DisplayName("空配置构建")
        void testBuildEmpty() {
            Config config = new ConfigBuilder().build();
            assertThat(config).isNotNull();
            assertThat(config.getKeys()).isEmpty();
        }

        @Test
        @DisplayName("链式调用")
        void testFluentApi() {
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("a", "1"))
                    .addProperties(Map.of("b", "2"))
                    .addSystemProperties()
                    .addEnvironmentVariables()
                    .disablePlaceholders()
                    .build();

            assertThat(config.getString("a")).isEqualTo("1");
            assertThat(config.getString("b")).isEqualTo("2");
        }
    }

    @Nested
    @DisplayName("多资源测试")
    class MultiResourceTests {

        @Test
        @DisplayName("添加多个类路径资源")
        void testAddClasspathResources() {
            // 这些资源可能不存在，但不应该抛出异常
            ConfigBuilder builder = new ConfigBuilder()
                    .addClasspathResources("application.properties", "test.properties");

            Config config = builder.build();
            assertThat(config).isNotNull();
        }
    }
}
