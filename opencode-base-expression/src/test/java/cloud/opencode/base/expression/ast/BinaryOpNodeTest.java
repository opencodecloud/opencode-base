package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.OpenExpressionException;
import cloud.opencode.base.expression.context.StandardContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * BinaryOpNode Tests
 * BinaryOpNode 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("BinaryOpNode Tests | BinaryOpNode 测试")
class BinaryOpNodeTest {

    private final StandardContext ctx = new StandardContext();

    @Nested
    @DisplayName("Factory Method Tests | 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of creates node | of 创建节点")
        void testOf() {
            BinaryOpNode node = BinaryOpNode.of(LiteralNode.of(1), "+", LiteralNode.of(2));
            assertThat(node.operator()).isEqualTo("+");
            assertThat(node.left()).isInstanceOf(LiteralNode.class);
            assertThat(node.right()).isInstanceOf(LiteralNode.class);
        }

        @Test
        @DisplayName("Constructor validation | 构造函数验证")
        void testConstructorValidation() {
            assertThatThrownBy(() -> new BinaryOpNode(null, LiteralNode.of(1), LiteralNode.of(2)))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> new BinaryOpNode("+", null, LiteralNode.of(2)))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> new BinaryOpNode("+", LiteralNode.of(1), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Arithmetic Operations Tests | 算术运算测试")
    class ArithmeticOperationsTests {

        @Test
        @DisplayName("Addition | 加法")
        void testAddition() {
            // Integer addition
            assertThat(eval(1, "+", 2)).isEqualTo(3);

            // Long addition
            assertThat(eval(1L, "+", 2L)).isEqualTo(3L);

            // Double addition
            assertThat(eval(1.5, "+", 2.5)).isEqualTo(4.0);

            // Mixed types
            assertThat(eval(1, "+", 2.0)).isEqualTo(3.0);
            assertThat(eval(1L, "+", 2)).isEqualTo(3L);
        }

        @Test
        @DisplayName("String concatenation | 字符串连接")
        void testStringConcatenation() {
            assertThat(eval("hello", "+", " world")).isEqualTo("hello world");
            assertThat(eval("value: ", "+", 42)).isEqualTo("value: 42");
            assertThat(eval(42, "+", " is the answer")).isEqualTo("42 is the answer");
        }

        @Test
        @DisplayName("Subtraction | 减法")
        void testSubtraction() {
            assertThat(eval(5, "-", 3)).isEqualTo(2);
            assertThat(eval(5L, "-", 3L)).isEqualTo(2L);
            assertThat(eval(5.0, "-", 3.0)).isEqualTo(2.0);
        }

        @Test
        @DisplayName("Multiplication | 乘法")
        void testMultiplication() {
            assertThat(eval(3, "*", 4)).isEqualTo(12);
            assertThat(eval(3L, "*", 4L)).isEqualTo(12L);
            assertThat(eval(3.0, "*", 4.0)).isEqualTo(12.0);
        }

        @Test
        @DisplayName("Division | 除法")
        void testDivision() {
            assertThat(eval(10, "/", 2)).isEqualTo(5);
            assertThat(eval(10L, "/", 2L)).isEqualTo(5L);
            assertThat(eval(10.0, "/", 4.0)).isEqualTo(2.5);
        }

        @Test
        @DisplayName("Division by zero | 除以零")
        void testDivisionByZero() {
            BinaryOpNode node = BinaryOpNode.of(LiteralNode.of(10), "/", LiteralNode.of(0));
            assertThatThrownBy(() -> node.evaluate(ctx))
                    .isInstanceOf(OpenExpressionException.class);
        }

        @Test
        @DisplayName("Modulo | 取模")
        void testModulo() {
            assertThat(eval(10, "%", 3)).isEqualTo(1);
            assertThat(eval(10L, "%", 3L)).isEqualTo(1L);
            assertThat(eval(10.5, "%", 3.0)).isEqualTo(1.5);
        }

        @Test
        @DisplayName("Modulo by zero | 取模零")
        void testModuloByZero() {
            BinaryOpNode node = BinaryOpNode.of(LiteralNode.of(10), "%", LiteralNode.of(0));
            assertThatThrownBy(() -> node.evaluate(ctx))
                    .isInstanceOf(OpenExpressionException.class);
        }

        @Test
        @DisplayName("Power | 幂运算")
        void testPower() {
            assertThat(eval(2, "**", 3)).isEqualTo(8.0);
            assertThat(eval(4.0, "**", 0.5)).isEqualTo(2.0);
        }

        @Test
        @DisplayName("Type error for arithmetic | 算术类型错误")
        void testArithmeticTypeError() {
            BinaryOpNode node = BinaryOpNode.of(LiteralNode.of("a"), "-", LiteralNode.of("b"));
            assertThatThrownBy(() -> node.evaluate(ctx))
                    .isInstanceOf(OpenExpressionException.class);
        }
    }

    @Nested
    @DisplayName("Comparison Operations Tests | 比较运算测试")
    class ComparisonOperationsTests {

        @Test
        @DisplayName("Equals | 等于")
        void testEquals() {
            assertThat(eval(5, "==", 5)).isEqualTo(true);
            assertThat(eval(5, "==", 6)).isEqualTo(false);
            assertThat(eval(5.0, "==", 5)).isEqualTo(true);
            assertThat(eval("a", "==", "a")).isEqualTo(true);
            assertThat(evalNulls("==")).isEqualTo(true);
        }

        @Test
        @DisplayName("Not equals | 不等于")
        void testNotEquals() {
            assertThat(eval(5, "!=", 6)).isEqualTo(true);
            assertThat(eval(5, "!=", 5)).isEqualTo(false);
        }

        @Test
        @DisplayName("Greater than | 大于")
        void testGreaterThan() {
            assertThat(eval(5, ">", 3)).isEqualTo(true);
            assertThat(eval(3, ">", 5)).isEqualTo(false);
            assertThat(eval(5, ">", 5)).isEqualTo(false);
        }

        @Test
        @DisplayName("Greater than or equal | 大于等于")
        void testGreaterThanOrEqual() {
            assertThat(eval(5, ">=", 3)).isEqualTo(true);
            assertThat(eval(5, ">=", 5)).isEqualTo(true);
            assertThat(eval(3, ">=", 5)).isEqualTo(false);
        }

        @Test
        @DisplayName("Less than | 小于")
        void testLessThan() {
            assertThat(eval(3, "<", 5)).isEqualTo(true);
            assertThat(eval(5, "<", 3)).isEqualTo(false);
            assertThat(eval(5, "<", 5)).isEqualTo(false);
        }

        @Test
        @DisplayName("Less than or equal | 小于等于")
        void testLessThanOrEqual() {
            assertThat(eval(3, "<=", 5)).isEqualTo(true);
            assertThat(eval(5, "<=", 5)).isEqualTo(true);
            assertThat(eval(5, "<=", 3)).isEqualTo(false);
        }

        @Test
        @DisplayName("Compare with null | 与 null 比较")
        void testCompareWithNull() {
            assertThat(evalWithNull(5, ">")).isEqualTo(true);
            assertThat(evalNullWith("<", 5)).isEqualTo(true);
        }

        @Test
        @DisplayName("Compare strings | 比较字符串")
        void testCompareStrings() {
            assertThat(eval("b", ">", "a")).isEqualTo(true);
            assertThat(eval("a", "<", "b")).isEqualTo(true);
        }
    }

    @Nested
    @DisplayName("Logical Operations Tests | 逻辑运算测试")
    class LogicalOperationsTests {

        @Test
        @DisplayName("Logical AND | 逻辑与")
        void testLogicalAnd() {
            assertThat(eval(true, "&&", true)).isEqualTo(true);
            assertThat(eval(true, "&&", false)).isEqualTo(false);
            assertThat(eval(false, "&&", true)).isEqualTo(false);
            assertThat(eval(false, "&&", false)).isEqualTo(false);
        }

        @Test
        @DisplayName("Logical AND short-circuit | 逻辑与短路")
        void testLogicalAndShortCircuit() {
            // If left is false, right should not be evaluated
            ctx.setVariable("x", 0);
            BinaryOpNode node = BinaryOpNode.of(
                    LiteralNode.of(false),
                    "&&",
                    BinaryOpNode.of(
                            IdentifierNode.of("x"),
                            "/",
                            LiteralNode.of(0) // Would throw if evaluated
                    )
            );
            assertThat(node.evaluate(ctx)).isEqualTo(false);
        }

        @Test
        @DisplayName("Logical OR | 逻辑或")
        void testLogicalOr() {
            assertThat(eval(true, "||", true)).isEqualTo(true);
            assertThat(eval(true, "||", false)).isEqualTo(true);
            assertThat(eval(false, "||", true)).isEqualTo(true);
            assertThat(eval(false, "||", false)).isEqualTo(false);
        }

        @Test
        @DisplayName("Logical OR short-circuit | 逻辑或短路")
        void testLogicalOrShortCircuit() {
            // If left is true, right should not be evaluated
            ctx.setVariable("x", 0);
            BinaryOpNode node = BinaryOpNode.of(
                    LiteralNode.of(true),
                    "||",
                    BinaryOpNode.of(
                            IdentifierNode.of("x"),
                            "/",
                            LiteralNode.of(0) // Would throw if evaluated
                    )
            );
            assertThat(node.evaluate(ctx)).isEqualTo(true);
        }
    }

    @Nested
    @DisplayName("Pattern Matching Tests | 模式匹配测试")
    class PatternMatchingTests {

        @Test
        @DisplayName("Matches operator | matches 运算符")
        void testMatches() {
            assertThat(eval("hello123", "matches", "hello\\d+")).isEqualTo(true);
            assertThat(eval("hello", "matches", "\\d+")).isEqualTo(false);
        }

        @Test
        @DisplayName("Matches with null | matches 与 null")
        void testMatchesWithNull() {
            BinaryOpNode node = BinaryOpNode.of(LiteralNode.ofNull(), "matches", LiteralNode.of(".*"));
            assertThat(node.evaluate(ctx)).isEqualTo(false);
        }

        @Test
        @DisplayName("Instanceof operator | instanceof 运算符")
        void testInstanceof() {
            assertThat(eval("hello", "instanceof", "string")).isEqualTo(true);
            assertThat(eval(123, "instanceof", "number")).isEqualTo(true);
            assertThat(eval(123, "instanceof", "integer")).isEqualTo(true);
            assertThat(eval(123L, "instanceof", "long")).isEqualTo(true);
            assertThat(eval(3.14, "instanceof", "double")).isEqualTo(true);
            assertThat(eval(true, "instanceof", "boolean")).isEqualTo(true);
        }

        @Test
        @DisplayName("Instanceof with Class | instanceof 与 Class")
        void testInstanceofWithClass() {
            BinaryOpNode node = BinaryOpNode.of(LiteralNode.of("hello"), "instanceof", LiteralNode.of(String.class));
            assertThat(node.evaluate(ctx)).isEqualTo(true);
        }

        @Test
        @DisplayName("Instanceof with null | instanceof 与 null")
        void testInstanceofWithNull() {
            BinaryOpNode node = BinaryOpNode.of(LiteralNode.ofNull(), "instanceof", LiteralNode.of("string"));
            assertThat(node.evaluate(ctx)).isEqualTo(false);
        }
    }

    @Nested
    @DisplayName("Unknown Operator Tests | 未知运算符测试")
    class UnknownOperatorTests {

        @Test
        @DisplayName("Unknown operator throws | 未知运算符抛出异常")
        void testUnknownOperator() {
            BinaryOpNode node = BinaryOpNode.of(LiteralNode.of(1), "??", LiteralNode.of(2));
            assertThatThrownBy(() -> node.evaluate(ctx))
                    .isInstanceOf(OpenExpressionException.class)
                    .hasMessageContaining("Unknown operator");
        }
    }

    @Nested
    @DisplayName("ToExpressionString Tests | 表达式字符串测试")
    class ToExpressionStringTests {

        @Test
        @DisplayName("Format binary operation | 格式化二元运算")
        void testToExpressionString() {
            BinaryOpNode node = BinaryOpNode.of(LiteralNode.of(1), "+", LiteralNode.of(2));
            assertThat(node.toExpressionString()).isEqualTo("(1 + 2)");
        }
    }

    @Nested
    @DisplayName("Record Tests | Record 测试")
    class RecordTests {

        @Test
        @DisplayName("Accessors | 访问器")
        void testAccessors() {
            BinaryOpNode node = new BinaryOpNode("+", LiteralNode.of(1), LiteralNode.of(2));
            assertThat(node.operator()).isEqualTo("+");
            assertThat(node.left()).isEqualTo(LiteralNode.of(1));
            assertThat(node.right()).isEqualTo(LiteralNode.of(2));
        }

        @Test
        @DisplayName("Equals and hashCode | equals 和 hashCode")
        void testEqualsAndHashCode() {
            BinaryOpNode node1 = BinaryOpNode.of(LiteralNode.of(1), "+", LiteralNode.of(2));
            BinaryOpNode node2 = BinaryOpNode.of(LiteralNode.of(1), "+", LiteralNode.of(2));
            assertThat(node1).isEqualTo(node2);
            assertThat(node1.hashCode()).isEqualTo(node2.hashCode());
        }

        @Test
        @DisplayName("getTypeName | 获取类型名称")
        void testGetTypeName() {
            assertThat(BinaryOpNode.of(LiteralNode.of(1), "+", LiteralNode.of(2)).getTypeName()).isEqualTo("BinaryOp");
        }
    }

    private Object eval(Object left, String op, Object right) {
        return BinaryOpNode.of(LiteralNode.of(left), op, LiteralNode.of(right)).evaluate(ctx);
    }

    private Object evalNulls(String op) {
        return BinaryOpNode.of(LiteralNode.ofNull(), op, LiteralNode.ofNull()).evaluate(ctx);
    }

    private Object evalWithNull(Object left, String op) {
        return BinaryOpNode.of(LiteralNode.of(left), op, LiteralNode.ofNull()).evaluate(ctx);
    }

    private Object evalNullWith(String op, Object right) {
        return BinaryOpNode.of(LiteralNode.ofNull(), op, LiteralNode.of(right)).evaluate(ctx);
    }
}
