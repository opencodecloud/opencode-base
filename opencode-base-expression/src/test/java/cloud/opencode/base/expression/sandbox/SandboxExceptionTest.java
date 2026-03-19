package cloud.opencode.base.expression.sandbox;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * SandboxException Tests
 * SandboxException 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("SandboxException Tests | SandboxException 测试")
class SandboxExceptionTest {

    @Nested
    @DisplayName("Constructor Tests | 构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("Create with message | 使用消息创建")
        void testMessageConstructor() {
            SandboxException ex = new SandboxException("Test error");
            assertThat(ex.getMessage()).contains("Sandbox violation");
            assertThat(ex.getMessage()).contains("Test error");
        }

        @Test
        @DisplayName("Create with violation type | 使用违规类型创建")
        void testViolationTypeConstructor() {
            SandboxException ex = new SandboxException(
                    "Class not allowed",
                    SandboxException.ViolationType.CLASS_ACCESS,
                    "java.lang.Runtime"
            );

            assertThat(ex.getViolationType()).isEqualTo(SandboxException.ViolationType.CLASS_ACCESS);
            assertThat(ex.getViolatedResource()).isEqualTo("java.lang.Runtime");
        }

        @Test
        @DisplayName("Create with cause | 使用原因创建")
        void testCauseConstructor() {
            Throwable cause = new RuntimeException("Original");
            SandboxException ex = new SandboxException("Wrapped error", cause);

            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("Factory Method Tests | 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("Class not allowed | 类不允许")
        void testClassNotAllowed() {
            SandboxException ex = SandboxException.classNotAllowed("java.lang.Runtime");

            assertThat(ex.getViolationType()).isEqualTo(SandboxException.ViolationType.CLASS_ACCESS);
            assertThat(ex.getViolatedResource()).isEqualTo("java.lang.Runtime");
            assertThat(ex.getMessage()).contains("java.lang.Runtime");
        }

        @Test
        @DisplayName("Class not allowed by Class | 通过 Class 对象")
        void testClassNotAllowedByClass() {
            SandboxException ex = SandboxException.classNotAllowed(Runtime.class);

            assertThat(ex.getViolationType()).isEqualTo(SandboxException.ViolationType.CLASS_ACCESS);
            assertThat(ex.getMessage()).contains("java.lang.Runtime");
        }

        @Test
        @DisplayName("Method not allowed | 方法不允许")
        void testMethodNotAllowed() {
            SandboxException ex = SandboxException.methodNotAllowed("exec");

            assertThat(ex.getViolationType()).isEqualTo(SandboxException.ViolationType.METHOD_CALL);
            assertThat(ex.getMessage()).contains("exec");
        }

        @Test
        @DisplayName("Method not allowed with class | 带类的方法不允许")
        void testMethodNotAllowedWithClass() {
            SandboxException ex = SandboxException.methodNotAllowed("Runtime", "exec");

            assertThat(ex.getViolationType()).isEqualTo(SandboxException.ViolationType.METHOD_CALL);
            assertThat(ex.getViolatedResource()).isEqualTo("Runtime.exec");
        }

        @Test
        @DisplayName("Property not allowed | 属性不允许")
        void testPropertyNotAllowed() {
            SandboxException ex = SandboxException.propertyNotAllowed("dangerous");

            assertThat(ex.getViolationType()).isEqualTo(SandboxException.ViolationType.PROPERTY_ACCESS);
            assertThat(ex.getMessage()).contains("dangerous");
        }

        @Test
        @DisplayName("Function not allowed | 函数不允许")
        void testFunctionNotAllowed() {
            SandboxException ex = SandboxException.functionNotAllowed("eval");

            assertThat(ex.getViolationType()).isEqualTo(SandboxException.ViolationType.FUNCTION_CALL);
            assertThat(ex.getMessage()).contains("eval");
        }

        @Test
        @DisplayName("Timeout | 超时")
        void testTimeout() {
            SandboxException ex = SandboxException.timeout(5000);

            assertThat(ex.getViolationType()).isEqualTo(SandboxException.ViolationType.TIMEOUT);
            assertThat(ex.getMessage()).contains("5000ms");
        }

        @Test
        @DisplayName("Iteration limit exceeded | 迭代限制超出")
        void testIterationLimitExceeded() {
            SandboxException ex = SandboxException.iterationLimitExceeded(10000);

            assertThat(ex.getViolationType()).isEqualTo(SandboxException.ViolationType.ITERATION_LIMIT);
            assertThat(ex.getMessage()).contains("10000");
        }

        @Test
        @DisplayName("Expression too long | 表达式过长")
        void testExpressionTooLong() {
            SandboxException ex = SandboxException.expressionTooLong(1000, 2000);

            assertThat(ex.getViolationType()).isEqualTo(SandboxException.ViolationType.EXPRESSION_LENGTH);
            assertThat(ex.getMessage()).contains("2000");
            assertThat(ex.getMessage()).contains("1000");
        }

        @Test
        @DisplayName("Depth limit exceeded | 深度限制超出")
        void testDepthLimitExceeded() {
            SandboxException ex = SandboxException.depthLimitExceeded(100);

            assertThat(ex.getViolationType()).isEqualTo(SandboxException.ViolationType.DEPTH_LIMIT);
            assertThat(ex.getMessage()).contains("100");
        }
    }

    @Nested
    @DisplayName("Violation Type Tests | 违规类型测试")
    class ViolationTypeTests {

        @Test
        @DisplayName("All violation types | 所有违规类型")
        void testAllViolationTypes() {
            assertThat(SandboxException.ViolationType.values())
                    .contains(
                            SandboxException.ViolationType.CLASS_ACCESS,
                            SandboxException.ViolationType.METHOD_CALL,
                            SandboxException.ViolationType.PROPERTY_ACCESS,
                            SandboxException.ViolationType.FUNCTION_CALL,
                            SandboxException.ViolationType.TIMEOUT,
                            SandboxException.ViolationType.ITERATION_LIMIT,
                            SandboxException.ViolationType.EXPRESSION_LENGTH,
                            SandboxException.ViolationType.DEPTH_LIMIT,
                            SandboxException.ViolationType.UNKNOWN
                    );
        }
    }
}
