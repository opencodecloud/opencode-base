package cloud.opencode.base.expression;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenExpressionException Tests
 * OpenExpressionException 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("OpenExpressionException Tests | OpenExpressionException 测试")
class OpenExpressionExceptionTest {

    @Nested
    @DisplayName("Constructor Tests | 构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("constructor with message | 带消息的构造函数")
        void testConstructorWithMessage() {
            OpenExpressionException ex = new OpenExpressionException("Test message");
            assertThat(ex.getMessage()).contains("Test message");
            assertThat(ex.getExpression()).isNull();
            assertThat(ex.getPosition()).isEqualTo(-1);
        }

        @Test
        @DisplayName("constructor with message and cause | 带消息和原因的构造函数")
        void testConstructorWithMessageAndCause() {
            Throwable cause = new RuntimeException("cause");
            OpenExpressionException ex = new OpenExpressionException("Test message", cause);
            assertThat(ex.getMessage()).contains("Test message");
            assertThat(ex.getCause()).isSameAs(cause);
        }

        @Test
        @DisplayName("constructor with expression context | 带表达式上下文的构造函数")
        void testConstructorWithExpressionContext() {
            OpenExpressionException ex = new OpenExpressionException("Error", "a + b", 5);
            assertThat(ex.getMessage()).contains("Error");
            assertThat(ex.getMessage()).contains("position 5");
            assertThat(ex.getMessage()).contains("a + b");
            assertThat(ex.getExpression()).isEqualTo("a + b");
            assertThat(ex.getPosition()).isEqualTo(5);
        }

        @Test
        @DisplayName("constructor with null expression | 带 null 表达式的构造函数")
        void testConstructorWithNullExpression() {
            OpenExpressionException ex = new OpenExpressionException("Error", null, -1);
            assertThat(ex.getMessage()).contains("Error");
        }
    }

    @Nested
    @DisplayName("Parse Error Tests | 解析错误测试")
    class ParseErrorTests {

        @Test
        @DisplayName("parseError with expression and position | parseError 带表达式和位置")
        void testParseErrorWithExpressionAndPosition() {
            OpenExpressionException ex = OpenExpressionException.parseError("Unexpected token", "1 + + 2", 4);
            assertThat(ex.getMessage()).contains("Parse error");
            assertThat(ex.getMessage()).contains("Unexpected token");
            assertThat(ex.getExpression()).isEqualTo("1 + + 2");
            assertThat(ex.getPosition()).isEqualTo(4);
        }

        @Test
        @DisplayName("parseError with position only | parseError 只带位置")
        void testParseErrorWithPositionOnly() {
            OpenExpressionException ex = OpenExpressionException.parseError("Unexpected EOF", 10);
            assertThat(ex.getMessage()).contains("Parse error");
            assertThat(ex.getMessage()).contains("position 10");
        }
    }

    @Nested
    @DisplayName("Evaluation Error Tests | 求值错误测试")
    class EvaluationErrorTests {

        @Test
        @DisplayName("evaluationError with message | evaluationError 带消息")
        void testEvaluationErrorWithMessage() {
            OpenExpressionException ex = OpenExpressionException.evaluationError("Cannot evaluate");
            assertThat(ex.getMessage()).contains("Evaluation error");
            assertThat(ex.getMessage()).contains("Cannot evaluate");
        }

        @Test
        @DisplayName("evaluationError with cause | evaluationError 带原因")
        void testEvaluationErrorWithCause() {
            Throwable cause = new ArithmeticException("Division by zero");
            OpenExpressionException ex = OpenExpressionException.evaluationError("Math error", cause);
            assertThat(ex.getMessage()).contains("Evaluation error");
            assertThat(ex.getCause()).isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("Type Error Tests | 类型错误测试")
    class TypeErrorTests {

        @Test
        @DisplayName("typeError creates exception | typeError 创建异常")
        void testTypeError() {
            OpenExpressionException ex = OpenExpressionException.typeError("integer", "hello");
            assertThat(ex.getMessage()).contains("Type error");
            assertThat(ex.getMessage()).contains("expected integer");
            assertThat(ex.getMessage()).contains("String");
        }

        @Test
        @DisplayName("typeError with null value | typeError 带 null 值")
        void testTypeErrorWithNull() {
            OpenExpressionException ex = OpenExpressionException.typeError("number", null);
            assertThat(ex.getMessage()).contains("expected number");
            assertThat(ex.getMessage()).contains("null");
        }
    }

    @Nested
    @DisplayName("Property/Method Not Found Tests | 属性/方法未找到测试")
    class NotFoundTests {

        @Test
        @DisplayName("propertyNotFound creates exception | propertyNotFound 创建异常")
        void testPropertyNotFound() {
            OpenExpressionException ex = OpenExpressionException.propertyNotFound("name", String.class);
            assertThat(ex.getMessage()).contains("Property 'name' not found");
            assertThat(ex.getMessage()).contains("String");
        }

        @Test
        @DisplayName("methodNotFound creates exception | methodNotFound 创建异常")
        void testMethodNotFound() {
            OpenExpressionException ex = OpenExpressionException.methodNotFound("doSomething", String.class);
            assertThat(ex.getMessage()).contains("Method 'doSomething' not found");
            assertThat(ex.getMessage()).contains("String");
        }

        @Test
        @DisplayName("functionNotFound creates exception | functionNotFound 创建异常")
        void testFunctionNotFound() {
            OpenExpressionException ex = OpenExpressionException.functionNotFound("customFunc");
            assertThat(ex.getMessage()).contains("Function not found");
            assertThat(ex.getMessage()).contains("customFunc");
        }
    }

    @Nested
    @DisplayName("Security Violation Tests | 安全违规测试")
    class SecurityViolationTests {

        @Test
        @DisplayName("securityViolation creates exception | securityViolation 创建异常")
        void testSecurityViolation() {
            OpenExpressionException ex = OpenExpressionException.securityViolation("Access to Runtime denied");
            assertThat(ex.getMessage()).contains("Security violation");
            assertThat(ex.getMessage()).contains("Access to Runtime denied");
        }
    }

    @Nested
    @DisplayName("Timeout Tests | 超时测试")
    class TimeoutTests {

        @Test
        @DisplayName("timeout creates exception | timeout 创建异常")
        void testTimeout() {
            OpenExpressionException ex = OpenExpressionException.timeout(5000);
            assertThat(ex.getMessage()).contains("timed out");
            assertThat(ex.getMessage()).contains("5000ms");
        }
    }

    @Nested
    @DisplayName("Division By Zero Tests | 除零测试")
    class DivisionByZeroTests {

        @Test
        @DisplayName("divisionByZero creates exception | divisionByZero 创建异常")
        void testDivisionByZero() {
            OpenExpressionException ex = OpenExpressionException.divisionByZero();
            assertThat(ex.getMessage()).contains("Division by zero");
        }
    }

    @Nested
    @DisplayName("Null Pointer Tests | 空指针测试")
    class NullPointerTests {

        @Test
        @DisplayName("nullPointer creates exception | nullPointer 创建异常")
        void testNullPointer() {
            OpenExpressionException ex = OpenExpressionException.nullPointer("property access");
            assertThat(ex.getMessage()).contains("Null pointer");
            assertThat(ex.getMessage()).contains("property access");
        }
    }
}
