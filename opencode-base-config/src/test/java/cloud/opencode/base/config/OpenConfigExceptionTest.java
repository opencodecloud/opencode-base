package cloud.opencode.base.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenConfigException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("OpenConfigException 测试")
class OpenConfigExceptionTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("带消息的构造方法")
        void testMessageConstructor() {
            OpenConfigException ex = new OpenConfigException("test message");

            assertThat(ex.getMessage()).contains("test message");
            assertThat(ex.configKey()).isNull();
            assertThat(ex.configSource()).isNull();
        }

        @Test
        @DisplayName("带消息和原因的构造方法")
        void testMessageCauseConstructor() {
            RuntimeException cause = new RuntimeException("cause");
            OpenConfigException ex = new OpenConfigException("test message", cause);

            assertThat(ex.getMessage()).contains("test message");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("带configKey和source的构造方法")
        void testKeySourceConstructor() {
            OpenConfigException ex = new OpenConfigException("key", "source", "message");

            assertThat(ex.configKey()).isEqualTo("key");
            assertThat(ex.configSource()).isEqualTo("source");
            assertThat(ex.getMessage()).contains("message");
        }

        @Test
        @DisplayName("完整参数的构造方法")
        void testFullConstructor() {
            RuntimeException cause = new RuntimeException("cause");
            OpenConfigException ex = new OpenConfigException("key", "source", "message", cause);

            assertThat(ex.configKey()).isEqualTo("key");
            assertThat(ex.configSource()).isEqualTo("source");
            assertThat(ex.getMessage()).contains("message");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("keyNotFound - 键未找到")
        void testKeyNotFound() {
            OpenConfigException ex = OpenConfigException.keyNotFound("database.url");

            assertThat(ex.configKey()).isEqualTo("database.url");
            assertThat(ex.getMessage()).contains("database.url");
            assertThat(ex.getMessage()).contains("not found");
        }

        @Test
        @DisplayName("requiredKeyMissing - 必填键缺失")
        void testRequiredKeyMissing() {
            OpenConfigException ex = OpenConfigException.requiredKeyMissing("api.key");

            assertThat(ex.configKey()).isEqualTo("api.key");
            assertThat(ex.getMessage()).contains("api.key");
            assertThat(ex.getMessage()).contains("Required");
        }

        @Test
        @DisplayName("conversionFailed - 类型转换失败")
        void testConversionFailed() {
            OpenConfigException ex = OpenConfigException.conversionFailed("port", "abc", Integer.class);

            assertThat(ex.configKey()).isEqualTo("port");
            assertThat(ex.getMessage()).contains("port");
            // Value is redacted for security — must NOT contain the raw value
            assertThat(ex.getMessage()).doesNotContain("abc");
            assertThat(ex.getMessage()).contains("redacted");
            assertThat(ex.getMessage()).contains("Integer");
        }

        @Test
        @DisplayName("conversionFailed - 带原因的类型转换失败")
        void testConversionFailedWithCause() {
            NumberFormatException cause = new NumberFormatException("not a number");
            OpenConfigException ex = OpenConfigException.conversionFailed("port", "abc", Integer.class, cause);

            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("sourceLoadFailed - 源加载失败")
        void testSourceLoadFailed() {
            Exception cause = new Exception("IO error");
            OpenConfigException ex = OpenConfigException.sourceLoadFailed("config.properties", cause);

            assertThat(ex.configSource()).isEqualTo("config.properties");
            assertThat(ex.getMessage()).contains("config.properties");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("sourceNotSupported - 源不支持")
        void testSourceNotSupported() {
            OpenConfigException ex = OpenConfigException.sourceNotSupported("ftp://server/config");

            assertThat(ex.getMessage()).contains("ftp://server/config");
            assertThat(ex.getMessage()).contains("Unsupported");
        }

        @Test
        @DisplayName("placeholderResolveFailed - 占位符解析失败")
        void testPlaceholderResolveFailed() {
            OpenConfigException ex = OpenConfigException.placeholderResolveFailed("undefined.key");

            assertThat(ex.getMessage()).contains("${undefined.key}");
        }

        @Test
        @DisplayName("placeholderRecursionTooDeep - 占位符递归过深")
        void testPlaceholderRecursionTooDeep() {
            OpenConfigException ex = OpenConfigException.placeholderRecursionTooDeep("${a}");

            assertThat(ex.getMessage()).contains("recursion too deep");
        }

        @Test
        @DisplayName("bindFailed - 配置绑定失败")
        void testBindFailed() {
            Exception cause = new Exception("bind error");
            OpenConfigException ex = OpenConfigException.bindFailed("database", Object.class, cause);

            assertThat(ex.getMessage()).contains("database");
            assertThat(ex.getMessage()).contains("Object");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("fieldBindFailed - 字段绑定失败")
        void testFieldBindFailed() {
            Exception cause = new Exception("field error");
            OpenConfigException ex = OpenConfigException.fieldBindFailed("port", cause);

            assertThat(ex.getMessage()).contains("port");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("validationFailed - 验证失败")
        void testValidationFailed() {
            OpenConfigException ex = OpenConfigException.validationFailed("port out of range");

            assertThat(ex.getMessage()).contains("validation failed");
            assertThat(ex.getMessage()).contains("port out of range");
        }

        @Test
        @DisplayName("invalidBoolean - 无效布尔值")
        void testInvalidBoolean() {
            OpenConfigException ex = OpenConfigException.invalidBoolean("maybe");

            assertThat(ex.getMessage()).contains("maybe");
            assertThat(ex.getMessage()).contains("Invalid boolean");
        }

        @Test
        @DisplayName("invalidUrl - 无效URL")
        void testInvalidUrl() {
            Exception cause = new Exception("malformed url");
            OpenConfigException ex = OpenConfigException.invalidUrl("not-a-url", cause);

            assertThat(ex.getMessage()).contains("not-a-url");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("converterNotFound - 转换器未找到")
        void testConverterNotFound() {
            OpenConfigException ex = OpenConfigException.converterNotFound(java.net.InetAddress.class);

            assertThat(ex.getMessage()).contains("InetAddress");
            assertThat(ex.getMessage()).contains("No converter");
        }

        @Test
        @DisplayName("decryptionFailed - 解密失败")
        void testDecryptionFailed() {
            Exception cause = new Exception("decryption error");
            OpenConfigException ex = OpenConfigException.decryptionFailed(cause);

            assertThat(ex.getMessage()).contains("decrypt");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }
}
