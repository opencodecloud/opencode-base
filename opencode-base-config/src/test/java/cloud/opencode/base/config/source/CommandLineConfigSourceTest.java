package cloud.opencode.base.config.source;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * CommandLineConfigSource 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("CommandLineConfigSource 测试")
class CommandLineConfigSourceTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("空参数数组")
        void testEmptyArgs() {
            CommandLineConfigSource source = new CommandLineConfigSource(new String[0]);

            assertThat(source.getProperties()).isEmpty();
        }

        @Test
        @DisplayName("解析--key=value格式")
        void testDoubleDashKeyValue() {
            String[] args = {"--server.port=8080", "--app.name=MyApp"};
            CommandLineConfigSource source = new CommandLineConfigSource(args);

            assertThat(source.getProperty("server.port")).isEqualTo("8080");
            assertThat(source.getProperty("app.name")).isEqualTo("MyApp");
        }

        @Test
        @DisplayName("解析-Dkey=value格式")
        void testSystemPropertyFormat() {
            String[] args = {"-Dapp.env=prod", "-Ddatabase.url=jdbc:mysql://localhost/db"};
            CommandLineConfigSource source = new CommandLineConfigSource(args);

            assertThat(source.getProperty("app.env")).isEqualTo("prod");
            assertThat(source.getProperty("database.url")).isEqualTo("jdbc:mysql://localhost/db");
        }

        @Test
        @DisplayName("解析布尔标志")
        void testBooleanFlag() {
            String[] args = {"--debug", "--verbose", "-Denable-feature"};
            CommandLineConfigSource source = new CommandLineConfigSource(args);

            assertThat(source.getProperty("debug")).isEqualTo("true");
            assertThat(source.getProperty("verbose")).isEqualTo("true");
            assertThat(source.getProperty("enable-feature")).isEqualTo("true");
        }

        @Test
        @DisplayName("解析混合格式")
        void testMixedFormats() {
            String[] args = {
                    "--server.port=9090",
                    "-Dapp.env=prod",
                    "--debug",
                    "-verbose"
            };
            CommandLineConfigSource source = new CommandLineConfigSource(args);

            assertThat(source.getProperty("server.port")).isEqualTo("9090");
            assertThat(source.getProperty("app.env")).isEqualTo("prod");
            assertThat(source.getProperty("debug")).isEqualTo("true");
            assertThat(source.getProperty("verbose")).isEqualTo("true");
        }

        @Test
        @DisplayName("忽略非参数")
        void testIgnoreNonArgs() {
            String[] args = {"not-an-arg", "also-not", "--valid=value"};
            CommandLineConfigSource source = new CommandLineConfigSource(args);

            assertThat(source.getProperties()).hasSize(1);
            assertThat(source.getProperty("valid")).isEqualTo("value");
        }
    }

    @Nested
    @DisplayName("ConfigSource接口测试")
    class ConfigSourceInterfaceTests {

        @Test
        @DisplayName("getName - 返回command-line")
        void testGetName() {
            CommandLineConfigSource source = new CommandLineConfigSource(new String[0]);
            assertThat(source.getName()).isEqualTo("command-line");
        }

        @Test
        @DisplayName("getPriority - 返回200（最高）")
        void testGetPriority() {
            CommandLineConfigSource source = new CommandLineConfigSource(new String[0]);
            assertThat(source.getPriority()).isEqualTo(200);
        }

        @Test
        @DisplayName("getProperties - 返回不可变映射")
        void testGetPropertiesImmutable() {
            CommandLineConfigSource source = new CommandLineConfigSource(new String[]{"--key=value"});

            Map<String, String> props = source.getProperties();

            assertThatThrownBy(() -> props.put("new", "value"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("getProperty - 获取单个属性")
        void testGetProperty() {
            CommandLineConfigSource source = new CommandLineConfigSource(new String[]{"--key=value"});

            assertThat(source.getProperty("key")).isEqualTo("value");
            assertThat(source.getProperty("nonexistent")).isNull();
        }
    }

    @Nested
    @DisplayName("特殊值测试")
    class SpecialValueTests {

        @Test
        @DisplayName("值包含等号")
        void testValueWithEquals() {
            String[] args = {"--connection=jdbc:mysql://host?user=admin"};
            CommandLineConfigSource source = new CommandLineConfigSource(args);

            assertThat(source.getProperty("connection")).isEqualTo("jdbc:mysql://host?user=admin");
        }

        @Test
        @DisplayName("空值")
        void testEmptyValue() {
            String[] args = {"--key="};
            CommandLineConfigSource source = new CommandLineConfigSource(args);

            assertThat(source.getProperty("key")).isEqualTo("");
        }

        @Test
        @DisplayName("值包含空格（引号）")
        void testValueWithSpaces() {
            String[] args = {"--message=Hello World"};
            CommandLineConfigSource source = new CommandLineConfigSource(args);

            assertThat(source.getProperty("message")).isEqualTo("Hello World");
        }
    }
}
