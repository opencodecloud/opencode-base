package cloud.opencode.base.json.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenJsonProcessingException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("OpenJsonProcessingException 测试")
class OpenJsonProcessingExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用消息创建")
        void testMessageConstructor() {
            OpenJsonProcessingException ex = new OpenJsonProcessingException("Test error");

            assertThat(ex.getMessage()).contains("Test error");
            assertThat(ex.getErrorType()).isEqualTo(OpenJsonProcessingException.ErrorType.UNKNOWN);
        }

        @Test
        @DisplayName("使用消息和原因创建")
        void testMessageCauseConstructor() {
            RuntimeException cause = new RuntimeException("Cause");
            OpenJsonProcessingException ex = new OpenJsonProcessingException("Test error", cause);

            assertThat(ex.getMessage()).contains("Test error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getErrorType()).isEqualTo(OpenJsonProcessingException.ErrorType.UNKNOWN);
        }

        @Test
        @DisplayName("使用消息和错误类型创建")
        void testMessageErrorTypeConstructor() {
            OpenJsonProcessingException ex = new OpenJsonProcessingException(
                "Parse failed", OpenJsonProcessingException.ErrorType.PARSE_ERROR);

            assertThat(ex.getMessage()).contains("Parse failed");
            assertThat(ex.getErrorType()).isEqualTo(OpenJsonProcessingException.ErrorType.PARSE_ERROR);
        }

        @Test
        @DisplayName("使用消息、错误类型和原因创建")
        void testFullConstructor() {
            RuntimeException cause = new RuntimeException("Cause");
            OpenJsonProcessingException ex = new OpenJsonProcessingException(
                "Serialization failed",
                OpenJsonProcessingException.ErrorType.SERIALIZATION_ERROR,
                cause);

            assertThat(ex.getMessage()).contains("Serialization failed");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getErrorType()).isEqualTo(OpenJsonProcessingException.ErrorType.SERIALIZATION_ERROR);
        }

        @Test
        @DisplayName("使用完整位置信息创建")
        void testLocationConstructor() {
            OpenJsonProcessingException ex = new OpenJsonProcessingException(
                "Error at position",
                OpenJsonProcessingException.ErrorType.PARSE_ERROR,
                null, 10, 25, "test.json");

            assertThat(ex.getLine()).isEqualTo(10);
            assertThat(ex.getColumn()).isEqualTo(25);
            assertThat(ex.getSource()).isEqualTo("test.json");
            assertThat(ex.hasLocation()).isTrue();
        }

        @Test
        @DisplayName("null错误类型使用UNKNOWN")
        void testNullErrorType() {
            OpenJsonProcessingException ex = new OpenJsonProcessingException("Test", null, null);

            assertThat(ex.getErrorType()).isEqualTo(OpenJsonProcessingException.ErrorType.UNKNOWN);
        }
    }

    @Nested
    @DisplayName("位置信息测试")
    class LocationTests {

        @Test
        @DisplayName("没有位置信息")
        void testNoLocation() {
            OpenJsonProcessingException ex = new OpenJsonProcessingException("Error");

            assertThat(ex.getLine()).isEqualTo(-1);
            assertThat(ex.getColumn()).isEqualTo(-1);
            assertThat(ex.hasLocation()).isFalse();
        }

        @Test
        @DisplayName("有位置信息")
        void testHasLocation() {
            OpenJsonProcessingException ex = new OpenJsonProcessingException(
                "Error", OpenJsonProcessingException.ErrorType.PARSE_ERROR,
                null, 5, 10, null);

            assertThat(ex.hasLocation()).isTrue();
        }

        @Test
        @DisplayName("getLocationString无位置")
        void testLocationStringNoLocation() {
            OpenJsonProcessingException ex = new OpenJsonProcessingException("Error");

            assertThat(ex.getLocationString()).isEqualTo("unknown location");
        }

        @Test
        @DisplayName("getLocationString有位置无源")
        void testLocationStringWithLocation() {
            OpenJsonProcessingException ex = new OpenJsonProcessingException(
                "Error", OpenJsonProcessingException.ErrorType.PARSE_ERROR,
                null, 10, 20, null);

            assertThat(ex.getLocationString()).isEqualTo("line 10, column 20");
        }

        @Test
        @DisplayName("getLocationString有位置有源")
        void testLocationStringWithSource() {
            OpenJsonProcessingException ex = new OpenJsonProcessingException(
                "Error", OpenJsonProcessingException.ErrorType.PARSE_ERROR,
                null, 10, 20, "data.json");

            assertThat(ex.getLocationString()).isEqualTo("data.json:line 10, column 20");
        }

        @Test
        @DisplayName("getMessage包含位置信息")
        void testMessageIncludesLocation() {
            OpenJsonProcessingException ex = new OpenJsonProcessingException(
                "Parse error", OpenJsonProcessingException.ErrorType.PARSE_ERROR,
                null, 5, 15, null);

            assertThat(ex.getMessage()).contains("Parse error");
            assertThat(ex.getMessage()).contains("line 5");
            assertThat(ex.getMessage()).contains("column 15");
        }
    }

    @Nested
    @DisplayName("ErrorType枚举测试")
    class ErrorTypeTests {

        @Test
        @DisplayName("所有错误类型存在")
        void testAllErrorTypes() {
            assertThat(OpenJsonProcessingException.ErrorType.values())
                .containsExactlyInAnyOrder(
                    OpenJsonProcessingException.ErrorType.PARSE_ERROR,
                    OpenJsonProcessingException.ErrorType.SERIALIZATION_ERROR,
                    OpenJsonProcessingException.ErrorType.DESERIALIZATION_ERROR,
                    OpenJsonProcessingException.ErrorType.TYPE_CONVERSION_ERROR,
                    OpenJsonProcessingException.ErrorType.PATH_ERROR,
                    OpenJsonProcessingException.ErrorType.IO_ERROR,
                    OpenJsonProcessingException.ErrorType.CONFIG_ERROR,
                    OpenJsonProcessingException.ErrorType.UNKNOWN
                );
        }

        @Test
        @DisplayName("valueOf方法")
        void testValueOf() {
            assertThat(OpenJsonProcessingException.ErrorType.valueOf("PARSE_ERROR"))
                .isEqualTo(OpenJsonProcessingException.ErrorType.PARSE_ERROR);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("parseError创建解析错误")
        void testParseError() {
            OpenJsonProcessingException ex = OpenJsonProcessingException.parseError("Invalid JSON");

            assertThat(ex.getErrorType()).isEqualTo(OpenJsonProcessingException.ErrorType.PARSE_ERROR);
            assertThat(ex.getMessage()).contains("Invalid JSON");
        }

        @Test
        @DisplayName("parseError带位置")
        void testParseErrorWithLocation() {
            OpenJsonProcessingException ex = OpenJsonProcessingException.parseError("Unexpected token", 3, 8);

            assertThat(ex.getErrorType()).isEqualTo(OpenJsonProcessingException.ErrorType.PARSE_ERROR);
            assertThat(ex.getLine()).isEqualTo(3);
            assertThat(ex.getColumn()).isEqualTo(8);
        }

        @Test
        @DisplayName("serializationError创建序列化错误")
        void testSerializationError() {
            RuntimeException cause = new RuntimeException("Cycle detected");
            OpenJsonProcessingException ex = OpenJsonProcessingException.serializationError(
                "Failed to serialize", cause);

            assertThat(ex.getErrorType()).isEqualTo(OpenJsonProcessingException.ErrorType.SERIALIZATION_ERROR);
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("deserializationError创建反序列化错误")
        void testDeserializationError() {
            RuntimeException cause = new RuntimeException("Type mismatch");
            OpenJsonProcessingException ex = OpenJsonProcessingException.deserializationError(
                "Failed to deserialize", cause);

            assertThat(ex.getErrorType()).isEqualTo(OpenJsonProcessingException.ErrorType.DESERIALIZATION_ERROR);
        }

        @Test
        @DisplayName("typeConversionError创建类型转换错误")
        void testTypeConversionError() {
            OpenJsonProcessingException ex = OpenJsonProcessingException.typeConversionError(
                "Cannot convert string to int");

            assertThat(ex.getErrorType()).isEqualTo(OpenJsonProcessingException.ErrorType.TYPE_CONVERSION_ERROR);
        }

        @Test
        @DisplayName("pathError创建路径错误")
        void testPathError() {
            OpenJsonProcessingException ex = OpenJsonProcessingException.pathError("Invalid path");

            assertThat(ex.getErrorType()).isEqualTo(OpenJsonProcessingException.ErrorType.PATH_ERROR);
        }

        @Test
        @DisplayName("ioError创建IO错误")
        void testIoError() {
            Exception cause = new java.io.IOException("Read failed");
            OpenJsonProcessingException ex = OpenJsonProcessingException.ioError("IO operation failed", cause);

            assertThat(ex.getErrorType()).isEqualTo(OpenJsonProcessingException.ErrorType.IO_ERROR);
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("configError创建配置错误")
        void testConfigError() {
            OpenJsonProcessingException ex = OpenJsonProcessingException.configError("Invalid config");

            assertThat(ex.getErrorType()).isEqualTo(OpenJsonProcessingException.ErrorType.CONFIG_ERROR);
        }
    }

    @Nested
    @DisplayName("getter方法测试")
    class GetterTests {

        @Test
        @DisplayName("getErrorType返回错误类型")
        void testGetErrorType() {
            OpenJsonProcessingException ex = new OpenJsonProcessingException(
                "Error", OpenJsonProcessingException.ErrorType.IO_ERROR);

            assertThat(ex.getErrorType()).isEqualTo(OpenJsonProcessingException.ErrorType.IO_ERROR);
        }

        @Test
        @DisplayName("getSource返回源标识")
        void testGetSource() {
            OpenJsonProcessingException ex = new OpenJsonProcessingException(
                "Error", OpenJsonProcessingException.ErrorType.PARSE_ERROR,
                null, 1, 1, "input.json");

            assertThat(ex.getSource()).isEqualTo("input.json");
        }

        @Test
        @DisplayName("无源时getSource返回null")
        void testGetSourceNull() {
            OpenJsonProcessingException ex = new OpenJsonProcessingException("Error");

            assertThat(ex.getSource()).isNull();
        }
    }

    @Nested
    @DisplayName("OpenException 继承测试")
    class OpenExceptionInheritanceTests {

        @Test
        @DisplayName("是 OpenException 的子类")
        void testExtendsOpenException() {
            OpenJsonProcessingException ex = new OpenJsonProcessingException("test");
            assertThat(ex).isInstanceOf(OpenException.class);
            assertThat(ex.getComponent()).isEqualTo("json");
            assertThat(ex.getErrorCode()).isEqualTo("UNKNOWN");
        }

        @Test
        @DisplayName("getMessage 包含组件和错误码前缀")
        void testGetMessageFormat() {
            OpenJsonProcessingException ex = new OpenJsonProcessingException(
                "Parse failed", OpenJsonProcessingException.ErrorType.PARSE_ERROR);

            assertThat(ex.getMessage()).startsWith("[json] (PARSE_ERROR) ");
            assertThat(ex.getMessage()).contains("Parse failed");
        }

        @Test
        @DisplayName("getMessage 带位置信息包含前缀和位置")
        void testGetMessageWithLocationFormat() {
            OpenJsonProcessingException ex = new OpenJsonProcessingException(
                "Unexpected token", OpenJsonProcessingException.ErrorType.PARSE_ERROR,
                null, 5, 10, null);

            assertThat(ex.getMessage()).startsWith("[json] (PARSE_ERROR) ");
            assertThat(ex.getMessage()).contains("Unexpected token");
            assertThat(ex.getMessage()).contains("at line 5, column 10");
        }

        @Test
        @DisplayName("getRawMessage 返回不含前缀的原始消息")
        void testGetRawMessage() {
            OpenJsonProcessingException ex = new OpenJsonProcessingException(
                "some error", OpenJsonProcessingException.ErrorType.IO_ERROR);

            assertThat(ex.getRawMessage()).isEqualTo("some error");
        }

        @Test
        @DisplayName("不同错误类型映射到正确的 errorCode")
        void testErrorTypeAsErrorCode() {
            OpenJsonProcessingException ex = new OpenJsonProcessingException(
                "err", OpenJsonProcessingException.ErrorType.SERIALIZATION_ERROR);

            assertThat(ex.getErrorCode()).isEqualTo("SERIALIZATION_ERROR");
            assertThat(ex.getComponent()).isEqualTo("json");
        }
    }
}
