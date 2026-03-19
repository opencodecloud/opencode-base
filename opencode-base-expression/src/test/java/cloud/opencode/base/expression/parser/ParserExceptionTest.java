package cloud.opencode.base.expression.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ParserException Tests
 * ParserException 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("ParserException Tests | ParserException 测试")
class ParserExceptionTest {

    @Nested
    @DisplayName("Constructor Tests | 构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("Create with message | 使用消息创建")
        void testMessageConstructor() {
            ParserException ex = new ParserException("Test error");
            assertThat(ex.getMessage()).contains("Parse error");
            assertThat(ex.getMessage()).contains("Test error");
        }

        @Test
        @DisplayName("Create with position | 使用位置创建")
        void testPositionConstructor() {
            ParserException ex = new ParserException("Unexpected token", 10);
            assertThat(ex.getMessage()).contains("column 10");
            assertThat(ex.getColumn()).isEqualTo(10);
        }

        @Test
        @DisplayName("Create with expression and position | 使用表达式和位置创建")
        void testExpressionPositionConstructor() {
            ParserException ex = new ParserException("Error", "1 + + 2", 4);
            assertThat(ex.getExpression()).isEqualTo("1 + + 2");
            assertThat(ex.getColumn()).isEqualTo(4);
            assertThat(ex.getMessage()).contains("1 + + 2");
        }

        @Test
        @DisplayName("Create with full details | 使用完整详情创建")
        void testFullDetailsConstructor() {
            ParserException ex = new ParserException(
                    "Unexpected token",
                    "x + y + z",
                    1,
                    5,
                    ParserException.ErrorType.UNEXPECTED_TOKEN
            );

            assertThat(ex.getLine()).isEqualTo(1);
            assertThat(ex.getColumn()).isEqualTo(5);
            assertThat(ex.getErrorType()).isEqualTo(ParserException.ErrorType.UNEXPECTED_TOKEN);
        }
    }

    @Nested
    @DisplayName("Factory Method Tests | 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("Unexpected token | 意外词法单元")
        void testUnexpectedToken() {
            ParserException ex = ParserException.unexpectedToken("@", 5);
            assertThat(ex.getErrorType()).isEqualTo(ParserException.ErrorType.UNEXPECTED_TOKEN);
            assertThat(ex.getMessage()).contains("@");
        }

        @Test
        @DisplayName("Unexpected token with expression | 带表达式的意外词法单元")
        void testUnexpectedTokenWithExpression() {
            ParserException ex = ParserException.unexpectedToken("@", "x @ y", 2);
            assertThat(ex.getExpression()).isEqualTo("x @ y");
        }

        @Test
        @DisplayName("Expected token | 期望词法单元")
        void testExpectedToken() {
            ParserException ex = ParserException.expectedToken(")", "EOF", 10);
            assertThat(ex.getErrorType()).isEqualTo(ParserException.ErrorType.EXPECTED_TOKEN);
            assertThat(ex.getMessage()).contains(")");
            assertThat(ex.getMessage()).contains("EOF");
        }

        @Test
        @DisplayName("Unexpected end | 意外结束")
        void testUnexpectedEnd() {
            ParserException ex = ParserException.unexpectedEnd("1 + ");
            assertThat(ex.getErrorType()).isEqualTo(ParserException.ErrorType.UNEXPECTED_END);
            assertThat(ex.getExpression()).isEqualTo("1 + ");
        }

        @Test
        @DisplayName("Invalid number | 无效数字")
        void testInvalidNumber() {
            ParserException ex = ParserException.invalidNumber("12.34.56", 0);
            assertThat(ex.getErrorType()).isEqualTo(ParserException.ErrorType.INVALID_NUMBER);
            assertThat(ex.getMessage()).contains("12.34.56");
        }

        @Test
        @DisplayName("Unterminated string | 未终止字符串")
        void testUnterminatedString() {
            ParserException ex = ParserException.unterminatedString(5);
            assertThat(ex.getErrorType()).isEqualTo(ParserException.ErrorType.UNTERMINATED_STRING);
        }

        @Test
        @DisplayName("Invalid escape sequence | 无效转义序列")
        void testInvalidEscapeSequence() {
            ParserException ex = ParserException.invalidEscapeSequence("\\q", 3);
            assertThat(ex.getErrorType()).isEqualTo(ParserException.ErrorType.INVALID_ESCAPE);
            assertThat(ex.getMessage()).contains("\\q");
        }

        @Test
        @DisplayName("Unbalanced parentheses | 不平衡括号")
        void testUnbalancedParentheses() {
            ParserException ex = ParserException.unbalancedParentheses(10);
            assertThat(ex.getErrorType()).isEqualTo(ParserException.ErrorType.UNBALANCED_PARENS);
        }
    }

    @Nested
    @DisplayName("Error Type Tests | 错误类型测试")
    class ErrorTypeTests {

        @Test
        @DisplayName("All error types exist | 所有错误类型存在")
        void testAllErrorTypes() {
            assertThat(ParserException.ErrorType.values())
                    .contains(
                            ParserException.ErrorType.GENERAL,
                            ParserException.ErrorType.UNEXPECTED_TOKEN,
                            ParserException.ErrorType.EXPECTED_TOKEN,
                            ParserException.ErrorType.UNEXPECTED_END,
                            ParserException.ErrorType.INVALID_NUMBER,
                            ParserException.ErrorType.UNTERMINATED_STRING,
                            ParserException.ErrorType.INVALID_ESCAPE,
                            ParserException.ErrorType.UNBALANCED_PARENS,
                            ParserException.ErrorType.INVALID_IDENTIFIER,
                            ParserException.ErrorType.UNKNOWN_OPERATOR
                    );
        }
    }

    @Nested
    @DisplayName("Message Formatting Tests | 消息格式化测试")
    class MessageFormattingTests {

        @Test
        @DisplayName("Format with position indicator | 带位置指示器格式化")
        void testFormatWithPositionIndicator() {
            ParserException ex = new ParserException("Error", "x + y + z", 1, 4, ParserException.ErrorType.GENERAL);

            String message = ex.getMessage();
            assertThat(message).contains("x + y + z");
            assertThat(message).contains("^");
        }

        @Test
        @DisplayName("Format without expression | 不带表达式格式化")
        void testFormatWithoutExpression() {
            ParserException ex = new ParserException("Simple error", null, 1, 0, ParserException.ErrorType.GENERAL);
            assertThat(ex.getMessage()).contains("Simple error");
        }
    }
}
