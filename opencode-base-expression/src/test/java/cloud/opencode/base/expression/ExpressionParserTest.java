package cloud.opencode.base.expression;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ExpressionParser Interface Tests
 * ExpressionParser 接口测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("ExpressionParser Interface Tests | ExpressionParser 接口测试")
class ExpressionParserTest {

    @Nested
    @DisplayName("Default Methods Tests | 默认方法测试")
    class DefaultMethodsTests {

        @Test
        @DisplayName("parseTemplate delegates to parseExpression by default | parseTemplate 默认委托给 parseExpression")
        void testParseTemplateDefault() {
            TestExpressionParser parser = new TestExpressionParser();
            Expression expr = parser.parseTemplate("Hello #{name}");

            assertThat(parser.getLastParsedExpression()).isEqualTo("Hello #{name}");
            assertThat(expr).isNotNull();
        }
    }

    @Nested
    @DisplayName("Parse Expression Tests | 解析表达式测试")
    class ParseExpressionTests {

        @Test
        @DisplayName("parseExpression returns expression | parseExpression 返回表达式")
        void testParseExpression() {
            TestExpressionParser parser = new TestExpressionParser();
            Expression expr = parser.parseExpression("1 + 2");

            assertThat(expr).isNotNull();
            assertThat(expr.getExpressionString()).isEqualTo("1 + 2");
        }

        @Test
        @DisplayName("parseExpression with complex expression | parseExpression 处理复杂表达式")
        void testParseComplexExpression() {
            TestExpressionParser parser = new TestExpressionParser();
            Expression expr = parser.parseExpression("a.b.c + d[0] * e()");

            assertThat(expr).isNotNull();
            assertThat(expr.getExpressionString()).isEqualTo("a.b.c + d[0] * e()");
        }
    }

    @Nested
    @DisplayName("OpenExpression Parser Tests | OpenExpression 解析器测试")
    class OpenExpressionParserTests {

        @Test
        @DisplayName("parse simple arithmetic | 解析简单算术")
        void testParseSimpleArithmetic() {
            ExpressionParser parser = OpenExpression.parser();
            Expression expr = parser.parseExpression("1 + 2 * 3");

            assertThat(((Number) expr.getValue()).intValue()).isEqualTo(7);
        }

        @Test
        @DisplayName("parse string literal | 解析字符串字面量")
        void testParseStringLiteral() {
            ExpressionParser parser = OpenExpression.parser();
            Expression expr = parser.parseExpression("'hello world'");

            assertThat(expr.getValue()).isEqualTo("hello world");
        }

        @Test
        @DisplayName("parse boolean expression | 解析布尔表达式")
        void testParseBooleanExpression() {
            ExpressionParser parser = OpenExpression.parser();
            Expression expr = parser.parseExpression("true && false");

            assertThat(expr.getValue()).isEqualTo(false);
        }

        @Test
        @DisplayName("parse ternary expression | 解析三元表达式")
        void testParseTernaryExpression() {
            ExpressionParser parser = OpenExpression.parser();
            Expression expr = parser.parseExpression("true ? 'yes' : 'no'");

            assertThat(expr.getValue()).isEqualTo("yes");
        }

        @Test
        @DisplayName("parse invalid expression throws exception | 解析无效表达式抛出异常")
        void testParseInvalidExpression() {
            ExpressionParser parser = OpenExpression.parser();

            assertThatThrownBy(() -> parser.parseExpression("1 + + 2"))
                    .isInstanceOf(OpenExpressionException.class);
        }

        @Test
        @DisplayName("parse null expression throws exception | 解析 null 表达式抛出异常")
        void testParseNullExpression() {
            ExpressionParser parser = OpenExpression.parser();

            // OpenExpression.parser() throws NullPointerException for null input
            assertThatThrownBy(() -> parser.parseExpression(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("parse empty expression throws exception | 解析空表达式抛出异常")
        void testParseEmptyExpression() {
            ExpressionParser parser = OpenExpression.parser();

            // OpenExpression.parser() throws OpenExpressionException for empty input
            assertThatThrownBy(() -> parser.parseExpression(""))
                    .isInstanceOf(cloud.opencode.base.expression.OpenExpressionException.class);
        }
    }

    // Helper class for testing default method behavior
    private static class TestExpressionParser implements ExpressionParser {
        private String lastParsedExpression;

        @Override
        public Expression parseExpression(String expressionString) {
            this.lastParsedExpression = expressionString;
            return new SimpleExpression(expressionString);
        }

        String getLastParsedExpression() {
            return lastParsedExpression;
        }
    }

    private record SimpleExpression(String expressionString) implements Expression {
        @Override
        public String getExpressionString() {
            return expressionString;
        }

        @Override
        public Object getValue() {
            return null;
        }

        @Override
        public Object getValue(cloud.opencode.base.expression.context.EvaluationContext context) {
            return null;
        }

        @Override
        public <T> T getValue(Class<T> targetType) {
            return null;
        }

        @Override
        public <T> T getValue(cloud.opencode.base.expression.context.EvaluationContext context, Class<T> targetType) {
            return null;
        }

        @Override
        public Object getValue(Object rootObject) {
            return null;
        }

        @Override
        public <T> T getValue(Object rootObject, Class<T> targetType) {
            return null;
        }

        @Override
        public Class<?> getValueType() {
            return Object.class;
        }

        @Override
        public Class<?> getValueType(cloud.opencode.base.expression.context.EvaluationContext context) {
            return Object.class;
        }
    }
}
