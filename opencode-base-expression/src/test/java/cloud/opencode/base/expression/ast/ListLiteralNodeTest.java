package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.context.StandardContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ListLiteralNode Tests
 * ListLiteralNode 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("ListLiteralNode Tests | ListLiteralNode 测试")
class ListLiteralNodeTest {

    private final StandardContext ctx = new StandardContext();

    @Nested
    @DisplayName("Factory Method Tests | 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of with list | of 使用列表")
        void testOfWithList() {
            ListLiteralNode node = ListLiteralNode.of(List.of(
                    LiteralNode.of(1),
                    LiteralNode.of(2),
                    LiteralNode.of(3)
            ));
            assertThat(node.elements()).hasSize(3);
        }

        @Test
        @DisplayName("of with varargs | of 使用可变参数")
        void testOfWithVarargs() {
            ListLiteralNode node = ListLiteralNode.of(
                    LiteralNode.of(1),
                    LiteralNode.of(2),
                    LiteralNode.of(3)
            );
            assertThat(node.elements()).hasSize(3);
        }

        @Test
        @DisplayName("empty creates empty list | empty 创建空列表")
        void testEmpty() {
            ListLiteralNode node = ListLiteralNode.empty();
            assertThat(node.elements()).isEmpty();
        }

        @Test
        @DisplayName("Constructor validation | 构造函数验证")
        void testConstructorValidation() {
            assertThatThrownBy(() -> new ListLiteralNode(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Evaluation Tests | 求值测试")
    class EvaluationTests {

        @Test
        @DisplayName("Evaluate empty list | 求值空列表")
        void testEvaluateEmptyList() {
            ListLiteralNode node = ListLiteralNode.empty();
            Object result = node.evaluate(ctx);

            assertThat(result).isInstanceOf(List.class);
            assertThat((List<?>) result).isEmpty();
        }

        @Test
        @DisplayName("Evaluate list with literals | 求值带字面量的列表")
        void testEvaluateWithLiterals() {
            ListLiteralNode node = ListLiteralNode.of(
                    LiteralNode.of(1),
                    LiteralNode.of("two"),
                    LiteralNode.of(true)
            );
            Object result = node.evaluate(ctx);

            assertThat(result).isInstanceOf(List.class);
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) result;
            assertThat(list).containsExactly(1, "two", true);
        }

        @Test
        @DisplayName("Evaluate list with expressions | 求值带表达式的列表")
        void testEvaluateWithExpressions() {
            ctx.setVariable("x", 10);
            ctx.setVariable("y", 20);

            ListLiteralNode node = ListLiteralNode.of(
                    IdentifierNode.of("x"),
                    BinaryOpNode.of(IdentifierNode.of("x"), "+", IdentifierNode.of("y")),
                    IdentifierNode.of("y")
            );
            Object result = node.evaluate(ctx);

            assertThat(result).isInstanceOf(List.class);
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) result;
            assertThat(list).containsExactly(10, 30, 20);
        }

        @Test
        @DisplayName("Evaluate nested list | 求值嵌套列表")
        void testEvaluateNestedList() {
            ListLiteralNode inner = ListLiteralNode.of(LiteralNode.of(1), LiteralNode.of(2));
            ListLiteralNode outer = ListLiteralNode.of(inner, LiteralNode.of(3));

            Object result = outer.evaluate(ctx);

            assertThat(result).isInstanceOf(List.class);
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) result;
            assertThat(list).hasSize(2);
            assertThat(list.get(0)).isInstanceOf(List.class);
            assertThat(list.get(1)).isEqualTo(3);
        }

        @Test
        @DisplayName("Evaluate list with null | 求值带 null 的列表")
        void testEvaluateWithNull() {
            ListLiteralNode node = ListLiteralNode.of(
                    LiteralNode.of(1),
                    LiteralNode.ofNull(),
                    LiteralNode.of(3)
            );
            Object result = node.evaluate(ctx);

            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) result;
            assertThat(list).containsExactly(1, null, 3);
        }
    }

    @Nested
    @DisplayName("ToExpressionString Tests | 表达式字符串测试")
    class ToExpressionStringTests {

        @Test
        @DisplayName("Empty list format | 空列表格式")
        void testEmptyListFormat() {
            ListLiteralNode node = ListLiteralNode.empty();
            assertThat(node.toExpressionString()).isEqualTo("{}");
        }

        @Test
        @DisplayName("Single element format | 单元素格式")
        void testSingleElementFormat() {
            ListLiteralNode node = ListLiteralNode.of(LiteralNode.of(1));
            assertThat(node.toExpressionString()).isEqualTo("{1}");
        }

        @Test
        @DisplayName("Multiple elements format | 多元素格式")
        void testMultipleElementsFormat() {
            ListLiteralNode node = ListLiteralNode.of(
                    LiteralNode.of(1),
                    LiteralNode.of(2),
                    LiteralNode.of(3)
            );
            assertThat(node.toExpressionString()).isEqualTo("{1, 2, 3}");
        }

        @Test
        @DisplayName("Mixed types format | 混合类型格式")
        void testMixedTypesFormat() {
            ListLiteralNode node = ListLiteralNode.of(
                    LiteralNode.of(1),
                    LiteralNode.of("two"),
                    LiteralNode.of(true)
            );
            assertThat(node.toExpressionString()).isEqualTo("{1, 'two', true}");
        }
    }

    @Nested
    @DisplayName("Record Tests | Record 测试")
    class RecordTests {

        @Test
        @DisplayName("elements accessor | elements 访问器")
        void testElementsAccessor() {
            ListLiteralNode node = new ListLiteralNode(List.of(LiteralNode.of(1), LiteralNode.of(2)));
            assertThat(node.elements()).hasSize(2);
        }

        @Test
        @DisplayName("Elements are immutable | 元素不可变")
        void testElementsImmutable() {
            ListLiteralNode node = ListLiteralNode.of(LiteralNode.of(1));
            assertThatThrownBy(() -> node.elements().add(LiteralNode.of(2)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Equals and hashCode | equals 和 hashCode")
        void testEqualsAndHashCode() {
            ListLiteralNode node1 = ListLiteralNode.of(LiteralNode.of(1), LiteralNode.of(2));
            ListLiteralNode node2 = ListLiteralNode.of(LiteralNode.of(1), LiteralNode.of(2));
            ListLiteralNode node3 = ListLiteralNode.of(LiteralNode.of(1), LiteralNode.of(3));

            assertThat(node1).isEqualTo(node2);
            assertThat(node1.hashCode()).isEqualTo(node2.hashCode());
            assertThat(node1).isNotEqualTo(node3);
        }

        @Test
        @DisplayName("getTypeName | 获取类型名称")
        void testGetTypeName() {
            assertThat(ListLiteralNode.empty().getTypeName()).isEqualTo("ListLiteral");
        }
    }
}
