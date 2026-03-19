package cloud.opencode.base.expression.eval;

import cloud.opencode.base.expression.OpenExpressionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OperatorEvaluator Tests
 * OperatorEvaluator 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("OperatorEvaluator Tests | OperatorEvaluator 测试")
class OperatorEvaluatorTest {

    @Nested
    @DisplayName("Arithmetic Operations | 算术运算")
    class ArithmeticOperationsTests {

        @Test
        @DisplayName("Addition | 加法")
        void testAddition() {
            assertThat(OperatorEvaluator.add(3, 4)).isEqualTo(7L);
            assertThat(OperatorEvaluator.add(3.5, 2.5)).isEqualTo(6.0);
            assertThat(OperatorEvaluator.add("Hello", " World")).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("Subtraction | 减法")
        void testSubtraction() {
            assertThat(OperatorEvaluator.subtract(10, 3)).isEqualTo(7L);
            assertThat(OperatorEvaluator.subtract(5.5, 2.0)).isEqualTo(3.5);
        }

        @Test
        @DisplayName("Multiplication | 乘法")
        void testMultiplication() {
            assertThat(OperatorEvaluator.multiply(4, 5)).isEqualTo(20L);
            assertThat(OperatorEvaluator.multiply(2.5, 4)).isEqualTo(10.0);
        }

        @Test
        @DisplayName("Division | 除法")
        void testDivision() {
            assertThat(OperatorEvaluator.divide(10, 2)).isEqualTo(5L);
            assertThat(OperatorEvaluator.divide(7, 2)).isEqualTo(3.5);
        }

        @Test
        @DisplayName("Division by zero | 除零")
        void testDivisionByZero() {
            assertThatThrownBy(() -> OperatorEvaluator.divide(10, 0))
                    .isInstanceOf(OpenExpressionException.class);
        }

        @Test
        @DisplayName("Modulo | 取模")
        void testModulo() {
            assertThat(OperatorEvaluator.modulo(10, 3)).isEqualTo(1L);
            assertThat(OperatorEvaluator.modulo(7, 2)).isEqualTo(1L);
        }

        @Test
        @DisplayName("Power | 幂运算")
        void testPower() {
            assertThat(OperatorEvaluator.power(2, 3)).isEqualTo(8.0);
            assertThat(OperatorEvaluator.power(3, 2)).isEqualTo(9.0);
        }
    }

    @Nested
    @DisplayName("Comparison Operations | 比较运算")
    class ComparisonOperationsTests {

        @Test
        @DisplayName("Equals | 相等")
        void testEquals() {
            assertThat(OperatorEvaluator.equals(5, 5)).isTrue();
            assertThat(OperatorEvaluator.equals(5, 5.0)).isTrue();
            assertThat(OperatorEvaluator.equals("a", "a")).isTrue();
            assertThat(OperatorEvaluator.equals(5, 6)).isFalse();
            assertThat(OperatorEvaluator.equals(null, null)).isTrue();
            assertThat(OperatorEvaluator.equals(null, 5)).isFalse();
        }

        @Test
        @DisplayName("Not equals | 不等")
        void testNotEquals() {
            assertThat(OperatorEvaluator.notEquals(5, 6)).isTrue();
            assertThat(OperatorEvaluator.notEquals(5, 5)).isFalse();
        }

        @Test
        @DisplayName("Less than | 小于")
        void testLessThan() {
            assertThat(OperatorEvaluator.lessThan(3, 5)).isTrue();
            assertThat(OperatorEvaluator.lessThan(5, 5)).isFalse();
            assertThat(OperatorEvaluator.lessThan(7, 5)).isFalse();
        }

        @Test
        @DisplayName("Less than or equal | 小于等于")
        void testLessThanOrEqual() {
            assertThat(OperatorEvaluator.lessThanOrEqual(3, 5)).isTrue();
            assertThat(OperatorEvaluator.lessThanOrEqual(5, 5)).isTrue();
            assertThat(OperatorEvaluator.lessThanOrEqual(7, 5)).isFalse();
        }

        @Test
        @DisplayName("Greater than | 大于")
        void testGreaterThan() {
            assertThat(OperatorEvaluator.greaterThan(7, 5)).isTrue();
            assertThat(OperatorEvaluator.greaterThan(5, 5)).isFalse();
            assertThat(OperatorEvaluator.greaterThan(3, 5)).isFalse();
        }

        @Test
        @DisplayName("Greater than or equal | 大于等于")
        void testGreaterThanOrEqual() {
            assertThat(OperatorEvaluator.greaterThanOrEqual(7, 5)).isTrue();
            assertThat(OperatorEvaluator.greaterThanOrEqual(5, 5)).isTrue();
            assertThat(OperatorEvaluator.greaterThanOrEqual(3, 5)).isFalse();
        }
    }

    @Nested
    @DisplayName("Logical Operations | 逻辑运算")
    class LogicalOperationsTests {

        @Test
        @DisplayName("And | 与")
        void testAnd() {
            assertThat(OperatorEvaluator.and(true, true)).isTrue();
            assertThat(OperatorEvaluator.and(true, false)).isFalse();
            assertThat(OperatorEvaluator.and(false, true)).isFalse();
            assertThat(OperatorEvaluator.and(false, false)).isFalse();
        }

        @Test
        @DisplayName("Or | 或")
        void testOr() {
            assertThat(OperatorEvaluator.or(true, true)).isTrue();
            assertThat(OperatorEvaluator.or(true, false)).isTrue();
            assertThat(OperatorEvaluator.or(false, true)).isTrue();
            assertThat(OperatorEvaluator.or(false, false)).isFalse();
        }

        @Test
        @DisplayName("Not | 非")
        void testNot() {
            assertThat(OperatorEvaluator.not(true)).isFalse();
            assertThat(OperatorEvaluator.not(false)).isTrue();
            assertThat(OperatorEvaluator.not(null)).isTrue();
        }
    }

    @Nested
    @DisplayName("Unary Operations | 一元运算")
    class UnaryOperationsTests {

        @Test
        @DisplayName("Negate | 取负")
        void testNegate() {
            assertThat(OperatorEvaluator.negate(5)).isEqualTo(-5);
            assertThat(OperatorEvaluator.negate(-3)).isEqualTo(3);
            assertThat(OperatorEvaluator.negate(2.5)).isEqualTo(-2.5);
        }
    }

    @Nested
    @DisplayName("Pattern Matching | 模式匹配")
    class PatternMatchingTests {

        @Test
        @DisplayName("Matches regex | 正则匹配")
        void testMatches() {
            assertThat(OperatorEvaluator.matches("hello", "hel.*")).isTrue();
            assertThat(OperatorEvaluator.matches("hello", "\\d+")).isFalse();
            assertThat(OperatorEvaluator.matches("123", "\\d+")).isTrue();
        }

        @Test
        @DisplayName("Null handling | null 处理")
        void testMatchesNullHandling() {
            assertThat(OperatorEvaluator.matches(null, ".*")).isFalse();
            assertThat(OperatorEvaluator.matches("test", null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Binary Operator Evaluation | 二元运算符求值")
    class BinaryOperatorEvaluationTests {

        @Test
        @DisplayName("Evaluate binary operators | 求值二元运算符")
        void testEvaluateBinary() {
            assertThat(OperatorEvaluator.evaluateBinary("+", 3, 4)).isEqualTo(7L);
            assertThat(OperatorEvaluator.evaluateBinary("-", 10, 3)).isEqualTo(7L);
            assertThat(OperatorEvaluator.evaluateBinary("*", 4, 5)).isEqualTo(20L);
            assertThat(OperatorEvaluator.evaluateBinary("/", 10, 2)).isEqualTo(5L);
            assertThat(OperatorEvaluator.evaluateBinary("%", 10, 3)).isEqualTo(1L);
            assertThat(OperatorEvaluator.evaluateBinary("==", 5, 5)).isEqualTo(true);
            assertThat(OperatorEvaluator.evaluateBinary("!=", 5, 6)).isEqualTo(true);
            assertThat(OperatorEvaluator.evaluateBinary("&&", true, true)).isEqualTo(true);
            assertThat(OperatorEvaluator.evaluateBinary("||", false, true)).isEqualTo(true);
        }

        @Test
        @DisplayName("Unknown operator throws | 未知运算符抛异常")
        void testUnknownOperator() {
            assertThatThrownBy(() -> OperatorEvaluator.evaluateBinary("???", 1, 2))
                    .isInstanceOf(OpenExpressionException.class);
        }
    }

    @Nested
    @DisplayName("Unary Operator Evaluation | 一元运算符求值")
    class UnaryOperatorEvaluationTests {

        @Test
        @DisplayName("Evaluate unary operators | 求值一元运算符")
        void testEvaluateUnary() {
            assertThat(OperatorEvaluator.evaluateUnary("!", true)).isEqualTo(false);
            assertThat(OperatorEvaluator.evaluateUnary("not", true)).isEqualTo(false);
            assertThat(OperatorEvaluator.evaluateUnary("-", 5)).isEqualTo(-5);
            assertThat(OperatorEvaluator.evaluateUnary("+", 5)).isEqualTo(5);
        }
    }
}
