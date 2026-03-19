package cloud.opencode.base.functional.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenFunctionalException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@DisplayName("OpenFunctionalException 测试")
class OpenFunctionalExceptionTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用消息构造")
        void testMessageConstructor() {
            OpenFunctionalException ex = new OpenFunctionalException("test message");

            assertThat(ex.getMessage()).contains("test message");
        }

        @Test
        @DisplayName("使用消息和原因构造")
        void testMessageAndCauseConstructor() {
            Throwable cause = new RuntimeException("cause");
            OpenFunctionalException ex = new OpenFunctionalException("test message", cause);

            assertThat(ex.getMessage()).contains("test message");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("使用错误码和消息构造")
        void testErrorCodeAndMessageConstructor() {
            OpenFunctionalException ex = new OpenFunctionalException("ERR_001", "error message");

            assertThat(ex.getMessage()).contains("error message");
            assertThat(ex.getErrorCode()).isEqualTo("ERR_001");
        }

        @Test
        @DisplayName("使用错误码、消息和原因构造")
        void testErrorCodeMessageAndCauseConstructor() {
            Throwable cause = new RuntimeException("cause");
            OpenFunctionalException ex = new OpenFunctionalException("ERR_001", "error message", cause);

            assertThat(ex.getMessage()).contains("error message");
            assertThat(ex.getErrorCode()).isEqualTo("ERR_001");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("getComponent() 测试")
    class GetComponentTests {

        @Test
        @DisplayName("返回FUNCTIONAL组件名")
        void testGetComponent() {
            OpenFunctionalException ex = new OpenFunctionalException("test");

            assertThat(ex.getComponent()).isEqualTo("FUNCTIONAL");
        }
    }

    @Nested
    @DisplayName("computationFailed() 工厂方法测试")
    class ComputationFailedTests {

        @Test
        @DisplayName("创建计算失败异常（仅消息）")
        void testComputationFailedWithMessage() {
            OpenFunctionalException ex = OpenFunctionalException.computationFailed("Division by zero");

            assertThat(ex.getMessage()).contains("Computation failed");
            assertThat(ex.getMessage()).contains("Division by zero");
            assertThat(ex.getErrorCode()).isEqualTo("FUNC_001");
        }

        @Test
        @DisplayName("创建计算失败异常（带原因）")
        void testComputationFailedWithCause() {
            Throwable cause = new ArithmeticException("/ by zero");
            OpenFunctionalException ex = OpenFunctionalException.computationFailed("calc", cause);

            assertThat(ex.getMessage()).contains("calc");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("noValue() 工厂方法测试")
    class NoValueTests {

        @Test
        @DisplayName("创建无值异常")
        void testNoValue() {
            OpenFunctionalException ex = OpenFunctionalException.noValue("Option is empty");

            assertThat(ex.getMessage()).contains("No value present");
            assertThat(ex.getMessage()).contains("Option is empty");
            assertThat(ex.getErrorCode()).isEqualTo("FUNC_002");
        }
    }

    @Nested
    @DisplayName("invalidState() 工厂方法测试")
    class InvalidStateTests {

        @Test
        @DisplayName("创建无效状态异常")
        void testInvalidState() {
            OpenFunctionalException ex = OpenFunctionalException.invalidState("already resolved");

            assertThat(ex.getMessage()).contains("Invalid state");
            assertThat(ex.getMessage()).contains("already resolved");
            assertThat(ex.getErrorCode()).isEqualTo("FUNC_003");
        }
    }

    @Nested
    @DisplayName("mappingFailed() 工厂方法测试")
    class MappingFailedTests {

        @Test
        @DisplayName("创建映射失败异常")
        void testMappingFailed() {
            Throwable cause = new RuntimeException("mapping error");
            OpenFunctionalException ex = OpenFunctionalException.mappingFailed(cause);

            assertThat(ex.getMessage()).contains("Mapping operation failed");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getErrorCode()).isEqualTo("FUNC_004");
        }
    }

    @Nested
    @DisplayName("filterFailed() 工厂方法测试")
    class FilterFailedTests {

        @Test
        @DisplayName("创建过滤失败异常")
        void testFilterFailed() {
            OpenFunctionalException ex = OpenFunctionalException.filterFailed("predicate failed");

            assertThat(ex.getMessage()).contains("Filter predicate not satisfied");
            assertThat(ex.getMessage()).contains("predicate failed");
            assertThat(ex.getErrorCode()).isEqualTo("FUNC_005");
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("继承自RuntimeException")
        void testExtendsRuntimeException() {
            OpenFunctionalException ex = new OpenFunctionalException("test");

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("可以作为异常抛出")
        void testCanBeThrown() {
            assertThatThrownBy(() -> {
                throw new OpenFunctionalException("test error");
            }).isInstanceOf(OpenFunctionalException.class)
                    .hasMessageContaining("test error");
        }
    }
}
