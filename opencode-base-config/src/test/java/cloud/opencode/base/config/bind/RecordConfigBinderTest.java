package cloud.opencode.base.config.bind;

import cloud.opencode.base.config.Config;
import cloud.opencode.base.config.ConfigBuilder;
import cloud.opencode.base.config.OpenConfigException;
import cloud.opencode.base.config.converter.ConverterRegistry;
import cloud.opencode.base.config.jdk25.DefaultValue;
import cloud.opencode.base.config.jdk25.Required;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * RecordConfigBinder 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("RecordConfigBinder 测试")
class RecordConfigBinderTest {

    private RecordConfigBinder binder;

    @BeforeEach
    void setUp() {
        Config config = new ConfigBuilder()
                .addProperties(Map.of(
                        "server.host", "localhost",
                        "server.port", "8080"
                ))
                .disablePlaceholders()
                .build();
        binder = new RecordConfigBinder(config, ConverterRegistry.defaults());
    }

    @Nested
    @DisplayName("基本绑定测试")
    class BasicBindTests {

        @Test
        @DisplayName("绑定简单Record")
        void testBindSimpleRecord() {
            Config config = new ConfigBuilder()
                    .addProperties(Map.of(
                            "app.name", "TestApp",
                            "app.version", "1.0.0"
                    ))
                    .disablePlaceholders()
                    .build();
            RecordConfigBinder binder = new RecordConfigBinder(config, ConverterRegistry.defaults());

            SimpleRecord record = binder.bind("app", SimpleRecord.class);

            assertThat(record.name()).isEqualTo("TestApp");
            assertThat(record.version()).isEqualTo("1.0.0");
        }

        @Test
        @DisplayName("绑定数值类型")
        void testBindNumericTypes() {
            ServerRecord record = binder.bind("server", ServerRecord.class);

            assertThat(record.host()).isEqualTo("localhost");
            assertThat(record.port()).isEqualTo(8080);
        }
    }

    @Nested
    @DisplayName("默认值测试")
    class DefaultValueTests {

        @Test
        @DisplayName("使用@DefaultValue注解的默认值")
        void testDefaultValueAnnotation() {
            Config emptyConfig = new ConfigBuilder()
                    .addProperties(Map.of())
                    .disablePlaceholders()
                    .build();
            RecordConfigBinder binder = new RecordConfigBinder(emptyConfig, ConverterRegistry.defaults());

            RecordWithDefaults record = binder.bind("prefix", RecordWithDefaults.class);

            assertThat(record.host()).isEqualTo("127.0.0.1");
            assertThat(record.port()).isEqualTo(3000);
        }

        @Test
        @DisplayName("配置值覆盖默认值")
        void testConfigOverridesDefault() {
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("prefix.host", "192.168.1.1"))
                    .disablePlaceholders()
                    .build();
            RecordConfigBinder binder = new RecordConfigBinder(config, ConverterRegistry.defaults());

            RecordWithDefaults record = binder.bind("prefix", RecordWithDefaults.class);

            assertThat(record.host()).isEqualTo("192.168.1.1");
            assertThat(record.port()).isEqualTo(3000); // 仍使用默认值
        }
    }

    @Nested
    @DisplayName("必填字段测试")
    class RequiredFieldTests {

        @Test
        @DisplayName("必填字段存在 - 绑定成功")
        void testRequiredFieldPresent() {
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("db.url", "jdbc:mysql://localhost"))
                    .disablePlaceholders()
                    .build();
            RecordConfigBinder binder = new RecordConfigBinder(config, ConverterRegistry.defaults());

            RequiredRecord record = binder.bind("db", RequiredRecord.class);

            assertThat(record.url()).isEqualTo("jdbc:mysql://localhost");
        }

        @Test
        @DisplayName("必填字段缺失 - 抛出异常")
        void testRequiredFieldMissing() {
            Config emptyConfig = new ConfigBuilder()
                    .addProperties(Map.of())
                    .disablePlaceholders()
                    .build();
            RecordConfigBinder binder = new RecordConfigBinder(emptyConfig, ConverterRegistry.defaults());

            assertThatThrownBy(() -> binder.bind("db", RequiredRecord.class))
                    .isInstanceOf(OpenConfigException.class);
        }
    }

    @Nested
    @DisplayName("嵌套Record测试")
    class NestedRecordTests {

        @Test
        @DisplayName("绑定嵌套Record")
        void testBindNestedRecord() {
            Config config = new ConfigBuilder()
                    .addProperties(Map.of(
                            "app.name", "NestedApp",
                            "app.server.host", "localhost",
                            "app.server.port", "9090"
                    ))
                    .disablePlaceholders()
                    .build();
            RecordConfigBinder binder = new RecordConfigBinder(config, ConverterRegistry.defaults());

            AppRecord record = binder.bind("app", AppRecord.class);

            assertThat(record.name()).isEqualTo("NestedApp");
            assertThat(record.server()).isNotNull();
            assertThat(record.server().host()).isEqualTo("localhost");
            assertThat(record.server().port()).isEqualTo(9090);
        }
    }

    @Nested
    @DisplayName("字段名转换测试")
    class FieldNameConversionTests {

        @Test
        @DisplayName("驼峰命名转换为kebab-case")
        void testCamelCaseToKebabCase() {
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("prefix.max-connections", "100"))
                    .disablePlaceholders()
                    .build();
            RecordConfigBinder binder = new RecordConfigBinder(config, ConverterRegistry.defaults());

            CamelCaseRecord record = binder.bind("prefix", CamelCaseRecord.class);

            assertThat(record.maxConnections()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("默认类型值测试")
    class DefaultTypeValueTests {

        @Test
        @DisplayName("缺失的int字段返回0")
        void testMissingIntReturnsZero() {
            Config emptyConfig = new ConfigBuilder()
                    .addProperties(Map.of())
                    .disablePlaceholders()
                    .build();
            RecordConfigBinder binder = new RecordConfigBinder(emptyConfig, ConverterRegistry.defaults());

            PrimitiveRecord record = binder.bind("prefix", PrimitiveRecord.class);

            assertThat(record.intValue()).isEqualTo(0);
            assertThat(record.longValue()).isEqualTo(0L);
            assertThat(record.doubleValue()).isEqualTo(0.0);
            assertThat(record.booleanValue()).isFalse();
        }
    }

    // 测试用的Record定义
    public record SimpleRecord(String name, String version) {}

    public record ServerRecord(String host, int port) {}

    public record RecordWithDefaults(
            @DefaultValue("127.0.0.1") String host,
            @DefaultValue("3000") int port
    ) {}

    public record RequiredRecord(
            @Required String url
    ) {}

    public record AppRecord(String name, ServerRecord server) {}

    public record CamelCaseRecord(int maxConnections) {}

    public record PrimitiveRecord(int intValue, long longValue, double doubleValue, boolean booleanValue) {}
}
