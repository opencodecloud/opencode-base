package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.OpenExpressionException;
import cloud.opencode.base.expression.context.StandardContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * CollectionFilterNode Tests
 * CollectionFilterNode 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("CollectionFilterNode Tests | CollectionFilterNode 测试")
class CollectionFilterNodeTest {

    private final StandardContext ctx = new StandardContext();

    @Nested
    @DisplayName("Factory Method Tests | 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of creates ALL mode | of 创建 ALL 模式")
        void testOf() {
            CollectionFilterNode node = CollectionFilterNode.of(
                    IdentifierNode.of("list"),
                    BinaryOpNode.of(IdentifierNode.of("#this"), ">", LiteralNode.of(5))
            );
            assertThat(node.mode()).isEqualTo(CollectionFilterNode.FilterMode.ALL);
        }

        @Test
        @DisplayName("all creates ALL mode | all 创建 ALL 模式")
        void testAll() {
            CollectionFilterNode node = CollectionFilterNode.all(
                    IdentifierNode.of("list"),
                    LiteralNode.of(true)
            );
            assertThat(node.mode()).isEqualTo(CollectionFilterNode.FilterMode.ALL);
        }

        @Test
        @DisplayName("first creates FIRST mode | first 创建 FIRST 模式")
        void testFirst() {
            CollectionFilterNode node = CollectionFilterNode.first(
                    IdentifierNode.of("list"),
                    LiteralNode.of(true)
            );
            assertThat(node.mode()).isEqualTo(CollectionFilterNode.FilterMode.FIRST);
        }

        @Test
        @DisplayName("last creates LAST mode | last 创建 LAST 模式")
        void testLast() {
            CollectionFilterNode node = CollectionFilterNode.last(
                    IdentifierNode.of("list"),
                    LiteralNode.of(true)
            );
            assertThat(node.mode()).isEqualTo(CollectionFilterNode.FilterMode.LAST);
        }

        @Test
        @DisplayName("Constructor validation | 构造函数验证")
        void testConstructorValidation() {
            assertThatThrownBy(() -> new CollectionFilterNode(null, LiteralNode.of(true), CollectionFilterNode.FilterMode.ALL))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> new CollectionFilterNode(IdentifierNode.of("list"), null, CollectionFilterNode.FilterMode.ALL))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Null mode defaults to ALL | null 模式默认为 ALL")
        void testNullModeDefaultsToAll() {
            CollectionFilterNode node = new CollectionFilterNode(
                    IdentifierNode.of("list"),
                    LiteralNode.of(true),
                    null
            );
            assertThat(node.mode()).isEqualTo(CollectionFilterNode.FilterMode.ALL);
        }
    }

    @Nested
    @DisplayName("Filter ALL Tests | 过滤 ALL 测试")
    class FilterAllTests {

        @Test
        @DisplayName("Filter all matching elements | 过滤所有匹配元素")
        void testFilterAllMatching() {
            ctx.setVariable("list", List.of(1, 2, 3, 4, 5, 6));

            CollectionFilterNode node = CollectionFilterNode.all(
                    IdentifierNode.of("list"),
                    BinaryOpNode.of(IdentifierNode.of("#this"), ">", LiteralNode.of(3))
            );
            Object result = node.evaluate(ctx);

            assertThat(result).isInstanceOf(List.class);
            @SuppressWarnings("unchecked")
            List<Integer> filtered = (List<Integer>) result;
            assertThat(filtered).containsExactly(4, 5, 6);
        }

        @Test
        @DisplayName("Filter with no matches | 过滤无匹配")
        void testFilterNoMatches() {
            ctx.setVariable("list", List.of(1, 2, 3));

            CollectionFilterNode node = CollectionFilterNode.all(
                    IdentifierNode.of("list"),
                    BinaryOpNode.of(IdentifierNode.of("#this"), ">", LiteralNode.of(10))
            );
            Object result = node.evaluate(ctx);

            assertThat(result).isInstanceOf(List.class);
            @SuppressWarnings("unchecked")
            List<Integer> filtered = (List<Integer>) result;
            assertThat(filtered).isEmpty();
        }

        @Test
        @DisplayName("Filter all elements | 过滤所有元素")
        void testFilterAllElements() {
            ctx.setVariable("list", List.of(1, 2, 3));

            CollectionFilterNode node = CollectionFilterNode.all(
                    IdentifierNode.of("list"),
                    LiteralNode.of(true)
            );
            Object result = node.evaluate(ctx);

            @SuppressWarnings("unchecked")
            List<Integer> filtered = (List<Integer>) result;
            assertThat(filtered).containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("Filter FIRST Tests | 过滤 FIRST 测试")
    class FilterFirstTests {

        @Test
        @DisplayName("Get first matching element | 获取第一个匹配元素")
        void testFirstMatching() {
            ctx.setVariable("list", List.of(1, 2, 3, 4, 5));

            CollectionFilterNode node = CollectionFilterNode.first(
                    IdentifierNode.of("list"),
                    BinaryOpNode.of(IdentifierNode.of("#this"), ">", LiteralNode.of(2))
            );
            assertThat(node.evaluate(ctx)).isEqualTo(3);
        }

        @Test
        @DisplayName("First with no match returns null | 无匹配返回 null")
        void testFirstNoMatch() {
            ctx.setVariable("list", List.of(1, 2, 3));

            CollectionFilterNode node = CollectionFilterNode.first(
                    IdentifierNode.of("list"),
                    BinaryOpNode.of(IdentifierNode.of("#this"), ">", LiteralNode.of(10))
            );
            assertThat(node.evaluate(ctx)).isNull();
        }
    }

    @Nested
    @DisplayName("Filter LAST Tests | 过滤 LAST 测试")
    class FilterLastTests {

        @Test
        @DisplayName("Get last matching element | 获取最后一个匹配元素")
        void testLastMatching() {
            ctx.setVariable("list", List.of(1, 2, 3, 4, 5));

            CollectionFilterNode node = CollectionFilterNode.last(
                    IdentifierNode.of("list"),
                    BinaryOpNode.of(IdentifierNode.of("#this"), "<", LiteralNode.of(4))
            );
            assertThat(node.evaluate(ctx)).isEqualTo(3);
        }

        @Test
        @DisplayName("Last with no match returns null | 无匹配返回 null")
        void testLastNoMatch() {
            ctx.setVariable("list", List.of(1, 2, 3));

            CollectionFilterNode node = CollectionFilterNode.last(
                    IdentifierNode.of("list"),
                    BinaryOpNode.of(IdentifierNode.of("#this"), ">", LiteralNode.of(10))
            );
            assertThat(node.evaluate(ctx)).isNull();
        }
    }

    @Nested
    @DisplayName("Null and Type Error Tests | Null 和类型错误测试")
    class NullAndTypeErrorTests {

        @Test
        @DisplayName("Null collection returns empty/null | null 集合返回空/null")
        void testNullCollection() {
            // Use TernaryOpNode to produce null value
            TernaryOpNode nullProducer = TernaryOpNode.of(
                    LiteralNode.of(false),
                    ListLiteralNode.of(LiteralNode.of(1)),
                    LiteralNode.ofNull()
            );

            CollectionFilterNode allNode = CollectionFilterNode.all(
                    nullProducer,
                    LiteralNode.of(true)
            );
            CollectionFilterNode firstNode = CollectionFilterNode.first(
                    nullProducer,
                    LiteralNode.of(true)
            );

            assertThat(allNode.evaluate(ctx)).isEqualTo(List.of());
            assertThat(firstNode.evaluate(ctx)).isNull();
        }

        @Test
        @DisplayName("Non-collection throws | 非集合抛出异常")
        void testNonCollection() {
            ctx.setVariable("notList", "hello");

            CollectionFilterNode node = CollectionFilterNode.all(
                    IdentifierNode.of("notList"),
                    LiteralNode.of(true)
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
        @DisplayName("ALL mode format | ALL 模式格式")
        void testAllModeFormat() {
            CollectionFilterNode node = CollectionFilterNode.all(
                    IdentifierNode.of("list"),
                    BinaryOpNode.of(IdentifierNode.of("#this"), ">", LiteralNode.of(5))
            );
            assertThat(node.toExpressionString()).isEqualTo("list.?[(#this > 5)]");
        }

        @Test
        @DisplayName("FIRST mode format | FIRST 模式格式")
        void testFirstModeFormat() {
            CollectionFilterNode node = CollectionFilterNode.first(
                    IdentifierNode.of("list"),
                    LiteralNode.of(true)
            );
            assertThat(node.toExpressionString()).isEqualTo("list.^[true]");
        }

        @Test
        @DisplayName("LAST mode format | LAST 模式格式")
        void testLastModeFormat() {
            CollectionFilterNode node = CollectionFilterNode.last(
                    IdentifierNode.of("list"),
                    LiteralNode.of(true)
            );
            assertThat(node.toExpressionString()).isEqualTo("list.$[true]");
        }
    }

    @Nested
    @DisplayName("FilterMode Enum Tests | FilterMode 枚举测试")
    class FilterModeEnumTests {

        @Test
        @DisplayName("All filter modes exist | 所有过滤模式存在")
        void testAllFilterModes() {
            assertThat(CollectionFilterNode.FilterMode.values())
                    .containsExactly(
                            CollectionFilterNode.FilterMode.ALL,
                            CollectionFilterNode.FilterMode.FIRST,
                            CollectionFilterNode.FilterMode.LAST
                    );
        }
    }

    @Nested
    @DisplayName("Record Tests | Record 测试")
    class RecordTests {

        @Test
        @DisplayName("Accessors | 访问器")
        void testAccessors() {
            CollectionFilterNode node = new CollectionFilterNode(
                    IdentifierNode.of("list"),
                    LiteralNode.of(true),
                    CollectionFilterNode.FilterMode.FIRST
            );
            assertThat(node.target()).isEqualTo(IdentifierNode.of("list"));
            assertThat(node.predicate()).isEqualTo(LiteralNode.of(true));
            assertThat(node.mode()).isEqualTo(CollectionFilterNode.FilterMode.FIRST);
        }

        @Test
        @DisplayName("getTypeName | 获取类型名称")
        void testGetTypeName() {
            assertThat(CollectionFilterNode.of(IdentifierNode.of("list"), LiteralNode.of(true)).getTypeName())
                    .isEqualTo("CollectionFilter");
        }
    }
}
