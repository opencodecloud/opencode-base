package cloud.opencode.base.expression;

import cloud.opencode.base.expression.context.EvaluationContext;
import cloud.opencode.base.expression.context.MapContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Expression Interface Tests
 * Expression 接口测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("Expression Interface Tests | Expression 接口测试")
class ExpressionTest {

    @Nested
    @DisplayName("Default Methods Tests | 默认方法测试")
    class DefaultMethodsTests {

        @Test
        @DisplayName("isWritable returns false by default | isWritable 默认返回 false")
        void testIsWritableDefault() {
            Expression expr = createTestExpression("test", 42);
            assertThat(expr.isWritable()).isFalse();
        }

        @Test
        @DisplayName("setValue throws UnsupportedOperationException by default | setValue 默认抛出 UnsupportedOperationException")
        void testSetValueThrowsByDefault() {
            Expression expr = createTestExpression("test", 42);
            EvaluationContext context = new MapContext();

            assertThatThrownBy(() -> expr.setValue(context, "value"))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("Expression is not writable");
        }
    }

    @Nested
    @DisplayName("Basic Operations Tests | 基本操作测试")
    class BasicOperationsTests {

        @Test
        @DisplayName("getExpressionString returns expression | getExpressionString 返回表达式")
        void testGetExpressionString() {
            Expression expr = createTestExpression("1 + 2", 3);
            assertThat(expr.getExpressionString()).isEqualTo("1 + 2");
        }

        @Test
        @DisplayName("getValue returns result | getValue 返回结果")
        void testGetValue() {
            Expression expr = createTestExpression("42", 42);
            assertThat(expr.getValue()).isEqualTo(42);
        }

        @Test
        @DisplayName("getValue with context returns result | getValue 带上下文返回结果")
        void testGetValueWithContext() {
            Expression expr = createTestExpression("x", 100);
            MapContext context = new MapContext();
            context.setVariable("x", 100);
            assertThat(expr.getValue(context)).isEqualTo(100);
        }

        @Test
        @DisplayName("getValue with type returns typed result | getValue 带类型返回类型化结果")
        void testGetValueWithType() {
            Expression expr = createTypedTestExpression("test", "hello");
            assertThat(expr.getValue(String.class)).isEqualTo("hello");
        }

        @Test
        @DisplayName("getValue with root object returns result | getValue 带根对象返回结果")
        void testGetValueWithRootObject() {
            Expression expr = createTestExpression("name", "test");
            Object root = new TestObject("test");
            assertThat(expr.getValue(root)).isEqualTo("test");
        }

        @Test
        @DisplayName("getValueType returns type | getValueType 返回类型")
        void testGetValueType() {
            Expression expr = createTypedTestExpression("test", Integer.valueOf(42));
            assertThat(expr.getValueType()).isEqualTo(Integer.class);
        }
    }

    @Nested
    @DisplayName("Writable Expression Tests | 可写表达式测试")
    class WritableExpressionTests {

        @Test
        @DisplayName("writable expression returns true for isWritable | 可写表达式 isWritable 返回 true")
        void testWritableExpression() {
            Expression expr = createWritableExpression("x");
            assertThat(expr.isWritable()).isTrue();
        }

        @Test
        @DisplayName("setValue on writable expression succeeds | 可写表达式 setValue 成功")
        void testSetValueOnWritable() {
            WritableTestExpression expr = createWritableExpression("x");
            MapContext context = new MapContext();

            expr.setValue(context, "newValue");
            assertThat(expr.getLastSetValue()).isEqualTo("newValue");
        }
    }

    @Nested
    @DisplayName("OpenExpression Integration Tests | OpenExpression 集成测试")
    class OpenExpressionIntegrationTests {

        @Test
        @DisplayName("parse and evaluate expression | 解析并求值表达式")
        void testParseAndEvaluate() {
            Expression expr = OpenExpression.parse("1 + 2");
            assertThat(((Number) expr.getValue()).intValue()).isEqualTo(3);
        }

        @Test
        @DisplayName("parse and evaluate with variables | 解析并带变量求值")
        void testParseAndEvaluateWithVariables() {
            Expression expr = OpenExpression.parse("x + y");
            MapContext context = new MapContext();
            context.setVariable("x", 10);
            context.setVariable("y", 20);
            assertThat(((Number) expr.getValue(context)).intValue()).isEqualTo(30);
        }

        @Test
        @DisplayName("expression string is preserved | 表达式字符串被保留")
        void testExpressionStringPreserved() {
            String exprStr = "a * b + c";
            Expression expr = OpenExpression.parse(exprStr);
            assertThat(expr.getExpressionString()).isEqualTo(exprStr);
        }
    }

    // Helper methods and classes

    private Expression createTestExpression(String expressionString, Object value) {
        return new Expression() {
            @Override
            public String getExpressionString() {
                return expressionString;
            }

            @Override
            public Object getValue() {
                return value;
            }

            @Override
            public Object getValue(EvaluationContext context) {
                return value;
            }

            @Override
            public <T> T getValue(Class<T> targetType) {
                return targetType.cast(value);
            }

            @Override
            public <T> T getValue(EvaluationContext context, Class<T> targetType) {
                return targetType.cast(value);
            }

            @Override
            public Object getValue(Object rootObject) {
                return value;
            }

            @Override
            public <T> T getValue(Object rootObject, Class<T> targetType) {
                return targetType.cast(value);
            }

            @Override
            public Class<?> getValueType() {
                return value != null ? value.getClass() : Object.class;
            }

            @Override
            public Class<?> getValueType(EvaluationContext context) {
                return getValueType();
            }
        };
    }

    private <T> Expression createTypedTestExpression(String expressionString, T value) {
        return createTestExpression(expressionString, value);
    }

    private WritableTestExpression createWritableExpression(String expressionString) {
        return new WritableTestExpression(expressionString);
    }

    private static class WritableTestExpression implements Expression {
        private final String expressionString;
        private Object lastSetValue;

        WritableTestExpression(String expressionString) {
            this.expressionString = expressionString;
        }

        @Override
        public String getExpressionString() {
            return expressionString;
        }

        @Override
        public Object getValue() {
            return lastSetValue;
        }

        @Override
        public Object getValue(EvaluationContext context) {
            return lastSetValue;
        }

        @Override
        public <T> T getValue(Class<T> targetType) {
            return targetType.cast(lastSetValue);
        }

        @Override
        public <T> T getValue(EvaluationContext context, Class<T> targetType) {
            return targetType.cast(lastSetValue);
        }

        @Override
        public Object getValue(Object rootObject) {
            return lastSetValue;
        }

        @Override
        public <T> T getValue(Object rootObject, Class<T> targetType) {
            return targetType.cast(lastSetValue);
        }

        @Override
        public Class<?> getValueType() {
            return lastSetValue != null ? lastSetValue.getClass() : Object.class;
        }

        @Override
        public Class<?> getValueType(EvaluationContext context) {
            return getValueType();
        }

        @Override
        public boolean isWritable() {
            return true;
        }

        @Override
        public void setValue(EvaluationContext context, Object value) {
            this.lastSetValue = value;
        }

        Object getLastSetValue() {
            return lastSetValue;
        }
    }

    private record TestObject(String name) {}
}
