package cloud.opencode.base.core.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("OpenException 测试")
class OpenExceptionTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造")
        void testMessageOnlyConstructor() {
            OpenException ex = new OpenException("Test message");

            assertThat(ex.getMessage()).isEqualTo("Test message");
            assertThat(ex.getRawMessage()).isEqualTo("Test message");
            assertThat(ex.getErrorCode()).isNull();
            assertThat(ex.getComponent()).isNull();
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("消息和原因构造")
        void testMessageAndCauseConstructor() {
            Exception cause = new RuntimeException("Original");
            OpenException ex = new OpenException("Test message", cause);

            assertThat(ex.getMessage()).isEqualTo("Test message");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getErrorCode()).isNull();
            assertThat(ex.getComponent()).isNull();
        }

        @Test
        @DisplayName("组件、错误码和消息构造")
        void testComponentErrorCodeMessageConstructor() {
            OpenException ex = new OpenException("Core", "CORE_001", "Parameter required");

            assertThat(ex.getMessage()).isEqualTo("[Core] (CORE_001) Parameter required");
            assertThat(ex.getRawMessage()).isEqualTo("Parameter required");
            assertThat(ex.getErrorCode()).isEqualTo("CORE_001");
            assertThat(ex.getComponent()).isEqualTo("Core");
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("完整参数构造")
        void testFullConstructor() {
            Exception cause = new RuntimeException("Original");
            OpenException ex = new OpenException("Core", "CORE_001", "Parameter required", cause);

            assertThat(ex.getMessage()).isEqualTo("[Core] (CORE_001) Parameter required");
            assertThat(ex.getRawMessage()).isEqualTo("Parameter required");
            assertThat(ex.getErrorCode()).isEqualTo("CORE_001");
            assertThat(ex.getComponent()).isEqualTo("Core");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("getMessage 格式化测试")
    class GetMessageTests {

        @Test
        @DisplayName("只有组件时的格式")
        void testMessageWithComponentOnly() {
            OpenException ex = new OpenException("Core", null, "Error message", null);
            assertThat(ex.getMessage()).isEqualTo("[Core] Error message");
        }

        @Test
        @DisplayName("只有错误码时的格式")
        void testMessageWithErrorCodeOnly() {
            OpenException ex = new OpenException(null, "ERR001", "Error message", null);
            assertThat(ex.getMessage()).isEqualTo("(ERR001) Error message");
        }

        @Test
        @DisplayName("空组件和错误码时的格式")
        void testMessageWithEmptyComponentAndCode() {
            OpenException ex = new OpenException("", "", "Error message", null);
            assertThat(ex.getMessage()).isEqualTo("Error message");
        }

        @Test
        @DisplayName("消息为 null 时的格式")
        void testMessageWithNullMessage() {
            OpenException ex = new OpenException("Core", "ERR001", null, null);
            assertThat(ex.getMessage()).isEqualTo("[Core] (ERR001) ");
        }

        @Test
        @DisplayName("所有参数都为 null 时")
        void testMessageAllNull() {
            OpenException ex = new OpenException(null, null, null, null);
            assertThat(ex.getMessage()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Getter 方法测试")
    class GetterTests {

        @Test
        @DisplayName("getErrorCode 返回正确值")
        void testGetErrorCode() {
            OpenException ex = new OpenException("Core", "ERR001", "message");
            assertThat(ex.getErrorCode()).isEqualTo("ERR001");
        }

        @Test
        @DisplayName("getComponent 返回正确值")
        void testGetComponent() {
            OpenException ex = new OpenException("MyComponent", "ERR001", "message");
            assertThat(ex.getComponent()).isEqualTo("MyComponent");
        }

        @Test
        @DisplayName("getRawMessage 返回原始消息")
        void testGetRawMessage() {
            OpenException ex = new OpenException("Core", "ERR001", "Raw message");
            assertThat(ex.getRawMessage()).isEqualTo("Raw message");
        }
    }

    @Nested
    @DisplayName("异常特性测试")
    class ExceptionFeatureTests {

        @Test
        @DisplayName("是 RuntimeException 的子类")
        void testIsRuntimeException() {
            OpenException ex = new OpenException("Test");
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("可以被抛出和捕获")
        void testThrowAndCatch() {
            assertThatThrownBy(() -> {
                throw new OpenException("Test");
            }).isInstanceOf(OpenException.class)
              .hasMessage("Test");
        }

        @Test
        @DisplayName("异常链正确传递")
        void testExceptionChain() {
            Exception root = new IllegalArgumentException("Root cause");
            OpenException ex = new OpenException("Wrapper", root);

            assertThat(ex.getCause()).isEqualTo(root);
            assertThat(ex.getCause().getMessage()).isEqualTo("Root cause");
        }
    }

    @Nested
    @DisplayName("序列化测试")
    class SerializationTests {

        @Test
        @DisplayName("包含 serialVersionUID")
        void testSerialVersionUID() throws NoSuchFieldException {
            var field = OpenException.class.getDeclaredField("serialVersionUID");
            assertThat(field).isNotNull();
        }
    }
}
