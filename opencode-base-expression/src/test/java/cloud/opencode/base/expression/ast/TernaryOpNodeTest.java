package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.context.StandardContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * TernaryOpNode Tests
 * TernaryOpNode 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("TernaryOpNode Tests | TernaryOpNode 测试")
class TernaryOpNodeTest {

    private final StandardContext ctx = new StandardContext();

    @Nested
    @DisplayName("Factory Method Tests | 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of creates node | of 创建节点")
        void testOf() {
            TernaryOpNode node = TernaryOpNode.of(
                    LiteralNode.of(true),
                    LiteralNode.of("yes"),
                    LiteralNode.of("no")
            );
            assertThat(node.condition()).isInstanceOf(LiteralNode.class);
            assertThat(node.trueValue()).isInstanceOf(LiteralNode.class);
            assertThat(node.falseValue()).isInstanceOf(LiteralNode.class);
        }

        @Test
        @DisplayName("Constructor validation | 构造函数验证")
        void testConstructorValidation() {
            assertThatThrownBy(() -> new TernaryOpNode(null, LiteralNode.of(1), LiteralNode.of(2)))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> new TernaryOpNode(LiteralNode.of(true), null, LiteralNode.of(2)))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> new TernaryOpNode(LiteralNode.of(true), LiteralNode.of(1), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Evaluation Tests | 求值测试")
    class EvaluationTests {

        @Test
        @DisplayName("True condition | 条件为真")
        void testTrueCondition() {
            TernaryOpNode node = TernaryOpNode.of(
                    LiteralNode.of(true),
                    LiteralNode.of("yes"),
                    LiteralNode.of("no")
            );
            assertThat(node.evaluate(ctx)).isEqualTo("yes");
        }

        @Test
        @DisplayName("False condition | 条件为假")
        void testFalseCondition() {
            TernaryOpNode node = TernaryOpNode.of(
                    LiteralNode.of(false),
                    LiteralNode.of("yes"),
                    LiteralNode.of("no")
            );
            assertThat(node.evaluate(ctx)).isEqualTo("no");
        }

        @Test
        @DisplayName("Truthy number condition | 数字真值条件")
        void testTruthyNumberCondition() {
            TernaryOpNode node = TernaryOpNode.of(
                    LiteralNode.of(1),
                    LiteralNode.of("yes"),
                    LiteralNode.of("no")
            );
            assertThat(node.evaluate(ctx)).isEqualTo("yes");
        }

        @Test
        @DisplayName("Falsy number condition | 数字假值条件")
        void testFalsyNumberCondition() {
            TernaryOpNode node = TernaryOpNode.of(
                    LiteralNode.of(0),
                    LiteralNode.of("yes"),
                    LiteralNode.of("no")
            );
            assertThat(node.evaluate(ctx)).isEqualTo("no");
        }

        @Test
        @DisplayName("Null condition is falsy | null 条件为假")
        void testNullCondition() {
            TernaryOpNode node = TernaryOpNode.of(
                    LiteralNode.ofNull(),
                    LiteralNode.of("yes"),
                    LiteralNode.of("no")
            );
            assertThat(node.evaluate(ctx)).isEqualTo("no");
        }

        @Test
        @DisplayName("Nested ternary | 嵌套三元运算")
        void testNestedTernary() {
            // (true ? (false ? "a" : "b") : "c") => "b"
            TernaryOpNode inner = TernaryOpNode.of(
                    LiteralNode.of(false),
                    LiteralNode.of("a"),
                    LiteralNode.of("b")
            );
            TernaryOpNode outer = TernaryOpNode.of(
                    LiteralNode.of(true),
                    inner,
                    LiteralNode.of("c")
            );
            assertThat(outer.evaluate(ctx)).isEqualTo("b");
        }
    }

    @Nested
    @DisplayName("ToExpressionString Tests | 表达式字符串测试")
    class ToExpressionStringTests {

        @Test
        @DisplayName("Format ternary expression | 格式化三元表达式")
        void testToExpressionString() {
            TernaryOpNode node = TernaryOpNode.of(
                    LiteralNode.of(true),
                    LiteralNode.of("yes"),
                    LiteralNode.of("no")
            );
            assertThat(node.toExpressionString()).isEqualTo("(true ? 'yes' : 'no')");
        }
    }

    @Nested
    @DisplayName("Record Tests | Record 测试")
    class RecordTests {

        @Test
        @DisplayName("Accessors | 访问器")
        void testAccessors() {
            TernaryOpNode node = new TernaryOpNode(
                    LiteralNode.of(true),
                    LiteralNode.of(1),
                    LiteralNode.of(2)
            );
            assertThat(node.condition()).isEqualTo(LiteralNode.of(true));
            assertThat(node.trueValue()).isEqualTo(LiteralNode.of(1));
            assertThat(node.falseValue()).isEqualTo(LiteralNode.of(2));
        }

        @Test
        @DisplayName("Equals and hashCode | equals 和 hashCode")
        void testEqualsAndHashCode() {
            TernaryOpNode node1 = TernaryOpNode.of(LiteralNode.of(true), LiteralNode.of(1), LiteralNode.of(2));
            TernaryOpNode node2 = TernaryOpNode.of(LiteralNode.of(true), LiteralNode.of(1), LiteralNode.of(2));
            assertThat(node1).isEqualTo(node2);
            assertThat(node1.hashCode()).isEqualTo(node2.hashCode());
        }

        @Test
        @DisplayName("getTypeName | 获取类型名称")
        void testGetTypeName() {
            assertThat(TernaryOpNode.of(LiteralNode.of(true), LiteralNode.of(1), LiteralNode.of(2)).getTypeName())
                    .isEqualTo("TernaryOp");
        }
    }
}
