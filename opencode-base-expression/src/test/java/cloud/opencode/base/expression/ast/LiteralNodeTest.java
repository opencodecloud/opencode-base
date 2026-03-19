package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.context.StandardContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * LiteralNode Tests
 * LiteralNode 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("LiteralNode Tests | LiteralNode 测试")
class LiteralNodeTest {

    @Nested
    @DisplayName("Factory Method Tests | 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of with any value | of 创建任意值")
        void testOf() {
            LiteralNode node = LiteralNode.of(42);
            assertThat(node.value()).isEqualTo(42);
        }

        @Test
        @DisplayName("ofNull | 创建 null")
        void testOfNull() {
            LiteralNode node = LiteralNode.ofNull();
            assertThat(node.value()).isNull();
        }

        @Test
        @DisplayName("ofBoolean | 创建布尔值")
        void testOfBoolean() {
            assertThat(LiteralNode.ofBoolean(true).value()).isEqualTo(true);
            assertThat(LiteralNode.ofBoolean(false).value()).isEqualTo(false);
        }

        @Test
        @DisplayName("ofInt | 创建整数")
        void testOfInt() {
            LiteralNode node = LiteralNode.ofInt(123);
            assertThat(node.value()).isEqualTo(123);
        }

        @Test
        @DisplayName("ofLong | 创建长整数")
        void testOfLong() {
            LiteralNode node = LiteralNode.ofLong(123456789L);
            assertThat(node.value()).isEqualTo(123456789L);
        }

        @Test
        @DisplayName("ofDouble | 创建双精度")
        void testOfDouble() {
            LiteralNode node = LiteralNode.ofDouble(3.14);
            assertThat(node.value()).isEqualTo(3.14);
        }

        @Test
        @DisplayName("ofString | 创建字符串")
        void testOfString() {
            LiteralNode node = LiteralNode.ofString("hello");
            assertThat(node.value()).isEqualTo("hello");
        }
    }

    @Nested
    @DisplayName("Evaluate Tests | 求值测试")
    class EvaluateTests {

        @Test
        @DisplayName("Evaluate returns value | 求值返回值")
        void testEvaluate() {
            StandardContext ctx = new StandardContext();

            assertThat(LiteralNode.of(42).evaluate(ctx)).isEqualTo(42);
            assertThat(LiteralNode.of("test").evaluate(ctx)).isEqualTo("test");
            assertThat(LiteralNode.ofNull().evaluate(ctx)).isNull();
            assertThat(LiteralNode.of(true).evaluate(ctx)).isEqualTo(true);
        }
    }

    @Nested
    @DisplayName("ToExpressionString Tests | 表达式字符串测试")
    class ToExpressionStringTests {

        @Test
        @DisplayName("Null literal | null 字面量")
        void testNullLiteral() {
            assertThat(LiteralNode.ofNull().toExpressionString()).isEqualTo("null");
        }

        @Test
        @DisplayName("String literal | 字符串字面量")
        void testStringLiteral() {
            assertThat(LiteralNode.ofString("hello").toExpressionString()).isEqualTo("'hello'");
        }

        @Test
        @DisplayName("String with quote | 带引号的字符串")
        void testStringWithQuote() {
            assertThat(LiteralNode.ofString("it's").toExpressionString()).isEqualTo("'it\\'s'");
        }

        @Test
        @DisplayName("Boolean literal | 布尔字面量")
        void testBooleanLiteral() {
            assertThat(LiteralNode.ofBoolean(true).toExpressionString()).isEqualTo("true");
            assertThat(LiteralNode.ofBoolean(false).toExpressionString()).isEqualTo("false");
        }

        @Test
        @DisplayName("Number literal | 数字字面量")
        void testNumberLiteral() {
            assertThat(LiteralNode.ofInt(42).toExpressionString()).isEqualTo("42");
            assertThat(LiteralNode.ofDouble(3.14).toExpressionString()).isEqualTo("3.14");
        }

        @Test
        @DisplayName("Object literal | 对象字面量")
        void testObjectLiteral() {
            Object obj = new Object() {
                @Override
                public String toString() {
                    return "custom";
                }
            };
            assertThat(LiteralNode.of(obj).toExpressionString()).isEqualTo("custom");
        }
    }

    @Nested
    @DisplayName("Node Interface Tests | Node 接口测试")
    class NodeInterfaceTests {

        @Test
        @DisplayName("getTypeName | 获取类型名称")
        void testGetTypeName() {
            assertThat(LiteralNode.of(1).getTypeName()).isEqualTo("Literal");
        }
    }

    @Nested
    @DisplayName("Record Tests | Record 测试")
    class RecordTests {

        @Test
        @DisplayName("value accessor | value 访问器")
        void testValueAccessor() {
            LiteralNode node = new LiteralNode(42);
            assertThat(node.value()).isEqualTo(42);
        }

        @Test
        @DisplayName("equals and hashCode | equals 和 hashCode")
        void testEqualsAndHashCode() {
            LiteralNode node1 = LiteralNode.of(42);
            LiteralNode node2 = LiteralNode.of(42);
            LiteralNode node3 = LiteralNode.of(43);

            assertThat(node1).isEqualTo(node2);
            assertThat(node1.hashCode()).isEqualTo(node2.hashCode());
            assertThat(node1).isNotEqualTo(node3);
        }

        @Test
        @DisplayName("toString | toString 方法")
        void testToString() {
            LiteralNode node = LiteralNode.of(42);
            assertThat(node.toString()).contains("42");
        }
    }
}
