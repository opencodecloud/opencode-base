package cloud.opencode.base.expression;

import cloud.opencode.base.expression.ast.LambdaNode;
import cloud.opencode.base.expression.context.StandardContext;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * New Operators Tests
 * 新运算符测试
 *
 * <p>Comprehensive tests for all new expression operators introduced in V1.0.3:
 * Elvis, In, Between, Bitwise, Lambda, Map Literal, and String Interpolation.</p>
 * <p>V1.0.3中引入的所有新表达式运算符的全面测试：
 * Elvis、In、Between、位运算、Lambda、Map字面量和字符串插值。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.3
 */
class NewOperatorsTest {

    // ==================== Elvis Operator | Elvis 运算符 ====================

    @Nested
    @DisplayName("Elvis Operator Tests | Elvis 运算符测试")
    class ElvisOperatorTests {

        @Test
        @DisplayName("Null value returns default | 空值返回默认值")
        void nullValueReturnsDefault() {
            Map<String, Object> vars = new HashMap<>();
            vars.put("name", null);
            assertThat(OpenExpression.eval("name ?: 'default'", vars)).isEqualTo("default");
        }

        @Test
        @DisplayName("Non-null value returns itself | 非空值返回自身")
        void nonNullValueReturnsItself() {
            Map<String, Object> vars = Map.of("name", "Jon");
            assertThat(OpenExpression.eval("name ?: 'default'", vars)).isEqualTo("Jon");
        }

        @Test
        @DisplayName("Zero is non-null, returns zero | 零为非空，返回零")
        void zeroIsNonNull() {
            Map<String, Object> vars = Map.of("x", 0);
            assertThat(OpenExpression.eval("x ?: 42", vars)).isEqualTo(0);
        }

        @Test
        @DisplayName("Empty string is non-null, returns empty | 空字符串为非空，返回空字符串")
        void emptyStringIsNonNull() {
            Map<String, Object> vars = Map.of("name", "");
            assertThat(OpenExpression.eval("name ?: 'fallback'", vars)).isEqualTo("");
        }

        @Test
        @DisplayName("Chained elvis with all nulls returns last | 链式Elvis全为空返回最后值")
        void chainedElvisAllNullsReturnsLast() {
            Map<String, Object> vars = new HashMap<>();
            vars.put("a", null);
            vars.put("b", null);
            assertThat(OpenExpression.eval("a ?: b ?: 'last'", vars)).isEqualTo("last");
        }

        @Test
        @DisplayName("Chained elvis returns first non-null | 链式Elvis返回第一个非空值")
        void chainedElvisReturnsFirstNonNull() {
            Map<String, Object> vars = new HashMap<>();
            vars.put("a", null);
            vars.put("b", "found");
            assertThat(OpenExpression.eval("a ?: b ?: 'last'", vars)).isEqualTo("found");
        }
    }

    // ==================== In Operator | In 运算符 ====================

    @Nested
    @DisplayName("In Operator Tests | In 运算符测试")
    class InOperatorTests {

        @Test
        @DisplayName("Value present in set returns true | 值在集合中返回true")
        void valuePresentInSet() {
            Map<String, Object> vars = Map.of("x", 2);
            assertThat(OpenExpression.eval("x in {1, 2, 3}", vars)).isEqualTo(true);
        }

        @Test
        @DisplayName("Value absent from set returns false | 值不在集合中返回false")
        void valueAbsentFromSet() {
            Map<String, Object> vars = Map.of("x", 5);
            assertThat(OpenExpression.eval("x in {1, 2, 3}", vars)).isEqualTo(false);
        }

        @Test
        @DisplayName("String present in set returns true | 字符串在集合中返回true")
        void stringPresentInSet() {
            Map<String, Object> vars = Map.of("name", "Bob");
            assertThat(OpenExpression.eval("name in {'Alice', 'Bob'}", vars)).isEqualTo(true);
        }

        @Test
        @DisplayName("String absent from set returns false | 字符串不在集合中返回false")
        void stringAbsentFromSet() {
            Map<String, Object> vars = Map.of("name", "Charlie");
            assertThat(OpenExpression.eval("name in {'Alice', 'Bob'}", vars)).isEqualTo(false);
        }

        @Test
        @DisplayName("Null value in set returns false | 空值在集合中返回false")
        void nullValueInSet() {
            Map<String, Object> vars = new HashMap<>();
            vars.put("x", null);
            assertThat(OpenExpression.eval("x in {1, 2, 3}", vars)).isEqualTo(false);
        }
    }

    // ==================== Between Operator | Between 运算符 ====================

    @Nested
    @DisplayName("Between Operator Tests | Between 运算符测试")
    class BetweenOperatorTests {

        @Test
        @DisplayName("Value within range returns true | 值在范围内返回true")
        void valueWithinRange() {
            Map<String, Object> vars = Map.of("x", 5);
            assertThat(OpenExpression.eval("x between 1 and 10", vars)).isEqualTo(true);
        }

        @Test
        @DisplayName("Value at lower bound returns true (inclusive) | 值在下界返回true（包含）")
        void valueAtLowerBound() {
            Map<String, Object> vars = Map.of("x", 1);
            assertThat(OpenExpression.eval("x between 1 and 10", vars)).isEqualTo(true);
        }

        @Test
        @DisplayName("Value at upper bound returns true (inclusive) | 值在上界返回true（包含）")
        void valueAtUpperBound() {
            Map<String, Object> vars = Map.of("x", 10);
            assertThat(OpenExpression.eval("x between 1 and 10", vars)).isEqualTo(true);
        }

        @Test
        @DisplayName("Value below range returns false | 值低于范围返回false")
        void valueBelowRange() {
            Map<String, Object> vars = Map.of("x", 0);
            assertThat(OpenExpression.eval("x between 1 and 10", vars)).isEqualTo(false);
        }

        @Test
        @DisplayName("Value above range returns false | 值高于范围返回false")
        void valueAboveRange() {
            Map<String, Object> vars = Map.of("x", 11);
            assertThat(OpenExpression.eval("x between 1 and 10", vars)).isEqualTo(false);
        }

        @Test
        @DisplayName("Double value within range returns true | 浮点值在范围内返回true")
        void doubleValueWithinRange() {
            Map<String, Object> vars = Map.of("x", 1.5);
            assertThat(OpenExpression.eval("x between 1.0 and 2.0", vars)).isEqualTo(true);
        }
    }

    // ==================== Bitwise Operator | 位运算符 ====================

    @Nested
    @DisplayName("Bitwise Operator Tests | 位运算符测试")
    class BitwiseOperatorTests {

        @Test
        @DisplayName("Bitwise AND | 按位与")
        void bitwiseAnd() {
            assertThat(OpenExpression.eval("5 & 3")).isEqualTo(1L);
        }

        @Test
        @DisplayName("Bitwise OR | 按位或")
        void bitwiseOr() {
            assertThat(OpenExpression.eval("5 | 3")).isEqualTo(7L);
        }

        @Test
        @DisplayName("Bitwise XOR | 按位异或")
        void bitwiseXor() {
            assertThat(OpenExpression.eval("5 ^ 3")).isEqualTo(6L);
        }

        @Test
        @DisplayName("Bitwise NOT | 按位取反")
        void bitwiseNot() {
            assertThat(OpenExpression.eval("~5")).isEqualTo(-6L);
        }

        @Test
        @DisplayName("Left shift | 左移")
        void leftShift() {
            assertThat(OpenExpression.eval("1 << 3")).isEqualTo(8L);
        }

        @Test
        @DisplayName("Right shift | 右移")
        void rightShift() {
            assertThat(OpenExpression.eval("16 >> 2")).isEqualTo(4L);
        }

        @Test
        @DisplayName("Combined bitwise with shift | 组合位运算与移位")
        void combinedBitwiseWithShift() {
            Map<String, Object> vars = Map.of("a", 18);
            assertThat(OpenExpression.eval("(a & 255) << 8", vars)).isEqualTo(4608L);
        }
    }

    // ==================== Lambda Expression | Lambda 表达式 ====================

    @Nested
    @DisplayName("Lambda Expression Tests | Lambda 表达式测试")
    class LambdaExpressionTests {

        @Test
        @DisplayName("Lambda evaluates to LambdaNode instance | Lambda求值为LambdaNode实例")
        void lambdaEvaluatesToLambdaNode() {
            Object result = OpenExpression.eval("x -> x + 1");
            assertThat(result).isInstanceOf(LambdaNode.class);
        }

        @Test
        @DisplayName("Lambda apply with arithmetic body | Lambda应用算术函数体")
        void lambdaApplyArithmetic() {
            Object result = OpenExpression.eval("x -> x + 1");
            assertThat(result).isInstanceOf(LambdaNode.class);

            LambdaNode lambda = (LambdaNode) result;
            StandardContext ctx = new StandardContext();
            assertThat(lambda.apply(5, ctx)).isEqualTo(6);
        }

        @Test
        @DisplayName("Lambda apply with comparison body returns true | Lambda应用比较函数体返回true")
        void lambdaApplyComparisonTrue() {
            Object result = OpenExpression.eval("x -> x > 3");
            assertThat(result).isInstanceOf(LambdaNode.class);

            LambdaNode lambda = (LambdaNode) result;
            StandardContext ctx = new StandardContext();
            assertThat(lambda.apply(4, ctx)).isEqualTo(true);
        }

        @Test
        @DisplayName("Lambda apply with comparison body returns false | Lambda应用比较函数体返回false")
        void lambdaApplyComparisonFalse() {
            Object result = OpenExpression.eval("x -> x > 3");
            assertThat(result).isInstanceOf(LambdaNode.class);

            LambdaNode lambda = (LambdaNode) result;
            StandardContext ctx = new StandardContext();
            assertThat(lambda.apply(2, ctx)).isEqualTo(false);
        }

        @Test
        @DisplayName("Lambda parameter name preserved | Lambda参数名保留")
        void lambdaParameterNamePreserved() {
            Object result = OpenExpression.eval("item -> item * 2");
            assertThat(result).isInstanceOf(LambdaNode.class);

            LambdaNode lambda = (LambdaNode) result;
            assertThat(lambda.parameter()).isEqualTo("item");
        }

        @Test
        @DisplayName("Lambda toExpressionString | Lambda表达式字符串")
        void lambdaToExpressionString() {
            Object result = OpenExpression.eval("x -> x + 1");
            assertThat(result).isInstanceOf(LambdaNode.class);

            LambdaNode lambda = (LambdaNode) result;
            assertThat(lambda.toExpressionString()).contains("x");
            assertThat(lambda.toExpressionString()).contains("->");
        }
    }

    // ==================== Map Literal | Map 字面量 ====================

    @Nested
    @DisplayName("Map Literal Tests | Map 字面量测试")
    class MapLiteralTests {

        @Test
        @DisplayName("Simple map literal | 简单Map字面量")
        @SuppressWarnings("unchecked")
        void simpleMapLiteral() {
            Object result = OpenExpression.eval("#{'name': 'Jon', 'age': 30}");
            assertThat(result).isInstanceOf(Map.class);

            Map<Object, Object> map = (Map<Object, Object>) result;
            assertThat(map).containsEntry("name", "Jon");
            assertThat(map).containsEntry("age", 30);
        }

        @Test
        @DisplayName("Empty map literal | 空Map字面量")
        void emptyMapLiteral() {
            Object result = OpenExpression.eval("#{}");
            assertThat(result).isInstanceOf(Map.class);
            assertThat((Map<?, ?>) result).isEmpty();
        }

        @Test
        @DisplayName("Map literal with expression values | 带表达式值的Map字面量")
        @SuppressWarnings("unchecked")
        void mapLiteralWithExpressionValues() {
            Object result = OpenExpression.eval("#{'x': 1 + 2, 'y': 3 * 4}");
            assertThat(result).isInstanceOf(Map.class);

            Map<Object, Object> map = (Map<Object, Object>) result;
            assertThat(map).containsEntry("x", 3);
            assertThat(map).containsEntry("y", 12);
        }
    }

    // ==================== String Interpolation | 字符串插值 ====================

    @Nested
    @DisplayName("String Interpolation Tests | 字符串插值测试")
    class StringInterpolationTests {

        @Test
        @DisplayName("Simple variable interpolation | 简单变量插值")
        void simpleVariableInterpolation() {
            String result = ExpressionTemplate.render(
                    "Hello ${name}", Map.of("name", "World"));
            assertThat(result).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("Expression evaluation in template | 模板中的表达式求值")
        void expressionEvaluationInTemplate() {
            String result = ExpressionTemplate.render(
                    "${a} + ${b} = ${a + b}", Map.of("a", 1, "b", 2));
            assertThat(result).isEqualTo("1 + 2 = 3");
        }

        @Test
        @DisplayName("No interpolation returns literal text | 无插值返回字面文本")
        void noInterpolationReturnsLiteralText() {
            String result = ExpressionTemplate.render(
                    "No interpolation", Map.of());
            assertThat(result).isEqualTo("No interpolation");
        }

        @Test
        @DisplayName("Escaped placeholder not evaluated | 转义占位符不被求值")
        void escapedPlaceholderNotEvaluated() {
            String result = ExpressionTemplate.render(
                    "Escaped \\${not_a_var}", Map.of());
            assertThat(result).isEqualTo("Escaped ${not_a_var}");
        }

        @Test
        @DisplayName("Nested elvis in template | 模板中嵌套Elvis运算符")
        void nestedElvisInTemplate() {
            Map<String, Object> vars = new HashMap<>();
            vars.put("name", null);
            String result = ExpressionTemplate.render(
                    "Nested ${name ?: 'default'}", vars);
            assertThat(result).isEqualTo("Nested default");
        }
    }
}
