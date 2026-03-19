package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.OpenExpressionException;
import cloud.opencode.base.expression.context.StandardContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * UnaryOpNode Tests
 * UnaryOpNode 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("UnaryOpNode Tests | UnaryOpNode 测试")
class UnaryOpNodeTest {

    private final StandardContext ctx = new StandardContext();

    @Nested
    @DisplayName("Factory Method Tests | 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of creates node | of 创建节点")
        void testOf() {
            UnaryOpNode node = UnaryOpNode.of("-", LiteralNode.of(5));
            assertThat(node.operator()).isEqualTo("-");
            assertThat(node.operand()).isInstanceOf(LiteralNode.class);
        }

        @Test
        @DisplayName("Constructor validation | 构造函数验证")
        void testConstructorValidation() {
            assertThatThrownBy(() -> new UnaryOpNode(null, LiteralNode.of(5)))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> new UnaryOpNode("-", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Negation Tests | 取负测试")
    class NegationTests {

        @Test
        @DisplayName("Negate integer | 整数取负")
        void testNegateInteger() {
            assertThat(eval("-", 5)).isEqualTo(-5);
            assertThat(eval("-", -5)).isEqualTo(5);
        }

        @Test
        @DisplayName("Negate long | 长整数取负")
        void testNegateLong() {
            assertThat(eval("-", 5L)).isEqualTo(-5L);
        }

        @Test
        @DisplayName("Negate double | 双精度取负")
        void testNegateDouble() {
            assertThat(eval("-", 3.14)).isEqualTo(-3.14);
        }

        @Test
        @DisplayName("Negate other number | 其他数字取负")
        void testNegateOtherNumber() {
            // Float will be converted to double when negated
            Object result = eval("-", 3.14f);
            assertThat(result).isInstanceOf(Number.class);
            assertThat(((Number) result).floatValue()).isEqualTo(-3.14f);
        }

        @Test
        @DisplayName("Negate non-number throws | 非数字取负抛出异常")
        void testNegateNonNumber() {
            UnaryOpNode node = UnaryOpNode.of("-", LiteralNode.of("hello"));
            assertThatThrownBy(() -> node.evaluate(ctx))
                    .isInstanceOf(OpenExpressionException.class);
        }
    }

    @Nested
    @DisplayName("Logical Not Tests | 逻辑非测试")
    class LogicalNotTests {

        @Test
        @DisplayName("Not boolean | 布尔取反")
        void testNotBoolean() {
            assertThat(eval("!", true)).isEqualTo(false);
            assertThat(eval("!", false)).isEqualTo(true);
        }

        @Test
        @DisplayName("Not truthy values | 真值取反")
        void testNotTruthyValues() {
            assertThat(eval("!", 1)).isEqualTo(false);
            assertThat(eval("!", "non-empty")).isEqualTo(false);
        }

        @Test
        @DisplayName("Not falsy values | 假值取反")
        void testNotFalsyValues() {
            assertThat(eval("!", 0)).isEqualTo(true);
            assertThat(eval("!", "")).isEqualTo(true);
        }

        @Test
        @DisplayName("Not null | null 取反")
        void testNotNull() {
            UnaryOpNode node = UnaryOpNode.of("!", LiteralNode.ofNull());
            assertThat(node.evaluate(ctx)).isEqualTo(true);
        }
    }

    @Nested
    @DisplayName("Unary Plus Tests | 一元加测试")
    class UnaryPlusTests {

        @Test
        @DisplayName("Plus returns value | 加号返回值")
        void testPlusReturnsValue() {
            assertThat(eval("+", 5)).isEqualTo(5);
            assertThat(eval("+", -5)).isEqualTo(-5);
            assertThat(eval("+", 3.14)).isEqualTo(3.14);
        }
    }

    @Nested
    @DisplayName("Unknown Operator Tests | 未知运算符测试")
    class UnknownOperatorTests {

        @Test
        @DisplayName("Unknown operator throws | 未知运算符抛出异常")
        void testUnknownOperator() {
            UnaryOpNode node = UnaryOpNode.of("~", LiteralNode.of(5));
            assertThatThrownBy(() -> node.evaluate(ctx))
                    .isInstanceOf(OpenExpressionException.class)
                    .hasMessageContaining("Unknown unary operator");
        }
    }

    @Nested
    @DisplayName("ToExpressionString Tests | 表达式字符串测试")
    class ToExpressionStringTests {

        @Test
        @DisplayName("Format unary operation | 格式化一元运算")
        void testToExpressionString() {
            assertThat(UnaryOpNode.of("-", LiteralNode.of(5)).toExpressionString()).isEqualTo("-5");
            assertThat(UnaryOpNode.of("!", LiteralNode.of(true)).toExpressionString()).isEqualTo("!true");
        }
    }

    @Nested
    @DisplayName("Record Tests | Record 测试")
    class RecordTests {

        @Test
        @DisplayName("Accessors | 访问器")
        void testAccessors() {
            UnaryOpNode node = new UnaryOpNode("-", LiteralNode.of(5));
            assertThat(node.operator()).isEqualTo("-");
            assertThat(node.operand()).isEqualTo(LiteralNode.of(5));
        }

        @Test
        @DisplayName("Equals and hashCode | equals 和 hashCode")
        void testEqualsAndHashCode() {
            UnaryOpNode node1 = UnaryOpNode.of("-", LiteralNode.of(5));
            UnaryOpNode node2 = UnaryOpNode.of("-", LiteralNode.of(5));
            assertThat(node1).isEqualTo(node2);
            assertThat(node1.hashCode()).isEqualTo(node2.hashCode());
        }

        @Test
        @DisplayName("getTypeName | 获取类型名称")
        void testGetTypeName() {
            assertThat(UnaryOpNode.of("-", LiteralNode.of(5)).getTypeName()).isEqualTo("UnaryOp");
        }
    }

    private Object eval(String op, Object value) {
        return UnaryOpNode.of(op, LiteralNode.of(value)).evaluate(ctx);
    }
}
