package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.OpenExpressionException;
import cloud.opencode.base.expression.context.StandardContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * CollectionProjectNode Tests
 * CollectionProjectNode 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("CollectionProjectNode Tests | CollectionProjectNode 测试")
class CollectionProjectNodeTest {

    private final StandardContext ctx = new StandardContext();

    @Nested
    @DisplayName("Factory Method Tests | 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of creates node | of 创建节点")
        void testOf() {
            CollectionProjectNode node = CollectionProjectNode.of(
                    IdentifierNode.of("list"),
                    IdentifierNode.of("#this")
            );
            assertThat(node.target()).isInstanceOf(IdentifierNode.class);
            assertThat(node.projection()).isInstanceOf(IdentifierNode.class);
        }

        @Test
        @DisplayName("Constructor validation | 构造函数验证")
        void testConstructorValidation() {
            assertThatThrownBy(() -> new CollectionProjectNode(null, LiteralNode.of(1)))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> new CollectionProjectNode(IdentifierNode.of("list"), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Projection Tests | 投影测试")
    class ProjectionTests {

        @Test
        @DisplayName("Project identity | 投影恒等")
        void testProjectIdentity() {
            ctx.setVariable("list", List.of(1, 2, 3));

            CollectionProjectNode node = CollectionProjectNode.of(
                    IdentifierNode.of("list"),
                    IdentifierNode.of("#this")
            );
            Object result = node.evaluate(ctx);

            assertThat(result).isInstanceOf(List.class);
            @SuppressWarnings("unchecked")
            List<Integer> projected = (List<Integer>) result;
            assertThat(projected).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("Project with transformation | 投影带转换")
        void testProjectWithTransformation() {
            ctx.setVariable("list", List.of(1, 2, 3));

            CollectionProjectNode node = CollectionProjectNode.of(
                    IdentifierNode.of("list"),
                    BinaryOpNode.of(IdentifierNode.of("#this"), "*", LiteralNode.of(2))
            );
            Object result = node.evaluate(ctx);

            @SuppressWarnings("unchecked")
            List<Integer> projected = (List<Integer>) result;
            assertThat(projected).containsExactly(2, 4, 6);
        }

        @Test
        @DisplayName("Project property from objects | 从对象投影属性")
        void testProjectProperty() {
            List<Map<String, Object>> users = List.of(
                    Map.of("name", "Alice", "age", 30),
                    Map.of("name", "Bob", "age", 25),
                    Map.of("name", "Charlie", "age", 35)
            );
            ctx.setVariable("users", users);

            CollectionProjectNode node = CollectionProjectNode.of(
                    IdentifierNode.of("users"),
                    PropertyAccessNode.of(IdentifierNode.of("#this"), "name")
            );
            Object result = node.evaluate(ctx);

            @SuppressWarnings("unchecked")
            List<String> names = (List<String>) result;
            assertThat(names).containsExactly("Alice", "Bob", "Charlie");
        }

        @Test
        @DisplayName("Project empty collection | 投影空集合")
        void testProjectEmpty() {
            ctx.setVariable("list", List.of());

            CollectionProjectNode node = CollectionProjectNode.of(
                    IdentifierNode.of("list"),
                    IdentifierNode.of("#this")
            );
            Object result = node.evaluate(ctx);

            @SuppressWarnings("unchecked")
            List<?> projected = (List<?>) result;
            assertThat(projected).isEmpty();
        }

        @Test
        @DisplayName("Project with constant | 投影常量")
        void testProjectConstant() {
            ctx.setVariable("list", List.of(1, 2, 3));

            CollectionProjectNode node = CollectionProjectNode.of(
                    IdentifierNode.of("list"),
                    LiteralNode.of("x")
            );
            Object result = node.evaluate(ctx);

            @SuppressWarnings("unchecked")
            List<String> projected = (List<String>) result;
            assertThat(projected).containsExactly("x", "x", "x");
        }
    }

    @Nested
    @DisplayName("Null and Type Error Tests | Null 和类型错误测试")
    class NullAndTypeErrorTests {

        @Test
        @DisplayName("Null collection returns empty list | null 集合返回空列表")
        void testNullCollection() {
            // Use TernaryOpNode to produce null value
            TernaryOpNode nullProducer = TernaryOpNode.of(
                    LiteralNode.of(false),
                    ListLiteralNode.of(LiteralNode.of(1)),
                    LiteralNode.ofNull()
            );

            CollectionProjectNode node = CollectionProjectNode.of(
                    nullProducer,
                    IdentifierNode.of("#this")
            );
            Object result = node.evaluate(ctx);

            assertThat(result).isEqualTo(List.of());
        }

        @Test
        @DisplayName("Non-collection throws | 非集合抛出异常")
        void testNonCollection() {
            ctx.setVariable("notList", "hello");

            CollectionProjectNode node = CollectionProjectNode.of(
                    IdentifierNode.of("notList"),
                    IdentifierNode.of("#this")
            );
            assertThatThrownBy(() -> node.evaluate(ctx))
                    .isInstanceOf(OpenExpressionException.class)
                    .hasMessageContaining("collection");
        }
    }

    @Nested
    @DisplayName("ToExpressionString Tests | 表达式字符串测试")
    class ToExpressionStringTests {

        @Test
        @DisplayName("Format projection | 格式化投影")
        void testFormat() {
            CollectionProjectNode node = CollectionProjectNode.of(
                    IdentifierNode.of("users"),
                    PropertyAccessNode.of(IdentifierNode.of("#this"), "name")
            );
            assertThat(node.toExpressionString()).isEqualTo("users.![#this.name]");
        }
    }

    @Nested
    @DisplayName("Record Tests | Record 测试")
    class RecordTests {

        @Test
        @DisplayName("Accessors | 访问器")
        void testAccessors() {
            CollectionProjectNode node = new CollectionProjectNode(
                    IdentifierNode.of("list"),
                    LiteralNode.of(1)
            );
            assertThat(node.target()).isEqualTo(IdentifierNode.of("list"));
            assertThat(node.projection()).isEqualTo(LiteralNode.of(1));
        }

        @Test
        @DisplayName("Equals and hashCode | equals 和 hashCode")
        void testEqualsAndHashCode() {
            CollectionProjectNode node1 = CollectionProjectNode.of(IdentifierNode.of("list"), LiteralNode.of(1));
            CollectionProjectNode node2 = CollectionProjectNode.of(IdentifierNode.of("list"), LiteralNode.of(1));
            assertThat(node1).isEqualTo(node2);
            assertThat(node1.hashCode()).isEqualTo(node2.hashCode());
        }

        @Test
        @DisplayName("getTypeName | 获取类型名称")
        void testGetTypeName() {
            assertThat(CollectionProjectNode.of(IdentifierNode.of("list"), LiteralNode.of(1)).getTypeName())
                    .isEqualTo("CollectionProject");
        }
    }
}
