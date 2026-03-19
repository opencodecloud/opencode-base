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
 * IndexAccessNode Tests
 * IndexAccessNode 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("IndexAccessNode Tests | IndexAccessNode 测试")
class IndexAccessNodeTest {

    private final StandardContext ctx = new StandardContext();

    @Nested
    @DisplayName("Factory Method Tests | 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of creates standard access | of 创建标准访问")
        void testOf() {
            IndexAccessNode node = IndexAccessNode.of(IdentifierNode.of("list"), LiteralNode.of(0));
            assertThat(node.target()).isInstanceOf(IdentifierNode.class);
            assertThat(node.index()).isInstanceOf(LiteralNode.class);
            assertThat(node.nullSafe()).isFalse();
        }

        @Test
        @DisplayName("nullSafe factory | nullSafe 工厂方法")
        void testNullSafeFactory() {
            IndexAccessNode node = IndexAccessNode.nullSafe(IdentifierNode.of("list"), LiteralNode.of(0));
            assertThat(node.nullSafe()).isTrue();
        }

        @Test
        @DisplayName("Constructor validation | 构造函数验证")
        void testConstructorValidation() {
            assertThatThrownBy(() -> new IndexAccessNode(null, LiteralNode.of(0), false))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> new IndexAccessNode(IdentifierNode.of("list"), null, false))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("List Access Tests | List 访问测试")
    class ListAccessTests {

        @Test
        @DisplayName("Access list by index | 按索引访问 List")
        void testAccessListByIndex() {
            ctx.setVariable("list", List.of("a", "b", "c"));

            IndexAccessNode node = IndexAccessNode.of(IdentifierNode.of("list"), LiteralNode.of(1));
            assertThat(node.evaluate(ctx)).isEqualTo("b");
        }

        @Test
        @DisplayName("Index out of bounds throws | 索引越界抛出异常")
        void testIndexOutOfBounds() {
            ctx.setVariable("list", List.of("a", "b"));

            IndexAccessNode node = IndexAccessNode.of(IdentifierNode.of("list"), LiteralNode.of(5));
            assertThatThrownBy(() -> node.evaluate(ctx))
                    .isInstanceOf(OpenExpressionException.class)
                    .hasMessageContaining("Index out of bounds");
        }

        @Test
        @DisplayName("Negative index throws | 负索引抛出异常")
        void testNegativeIndex() {
            ctx.setVariable("list", List.of("a", "b"));

            IndexAccessNode node = IndexAccessNode.of(IdentifierNode.of("list"), LiteralNode.of(-1));
            assertThatThrownBy(() -> node.evaluate(ctx))
                    .isInstanceOf(OpenExpressionException.class);
        }

        @Test
        @DisplayName("Null-safe out of bounds returns null | 空安全索引越界返回 null")
        void testNullSafeOutOfBounds() {
            ctx.setVariable("list", List.of("a", "b"));

            IndexAccessNode node = IndexAccessNode.nullSafe(IdentifierNode.of("list"), LiteralNode.of(5));
            assertThat(node.evaluate(ctx)).isNull();
        }
    }

    @Nested
    @DisplayName("Array Access Tests | 数组访问测试")
    class ArrayAccessTests {

        @Test
        @DisplayName("Access array by index | 按索引访问数组")
        void testAccessArrayByIndex() {
            ctx.setVariable("arr", new int[]{10, 20, 30});

            IndexAccessNode node = IndexAccessNode.of(IdentifierNode.of("arr"), LiteralNode.of(1));
            assertThat(node.evaluate(ctx)).isEqualTo(20);
        }

        @Test
        @DisplayName("Access object array | 访问对象数组")
        void testAccessObjectArray() {
            ctx.setVariable("arr", new String[]{"a", "b", "c"});

            IndexAccessNode node = IndexAccessNode.of(IdentifierNode.of("arr"), LiteralNode.of(2));
            assertThat(node.evaluate(ctx)).isEqualTo("c");
        }

        @Test
        @DisplayName("Array index out of bounds | 数组索引越界")
        void testArrayIndexOutOfBounds() {
            ctx.setVariable("arr", new int[]{1, 2});

            IndexAccessNode node = IndexAccessNode.of(IdentifierNode.of("arr"), LiteralNode.of(5));
            assertThatThrownBy(() -> node.evaluate(ctx))
                    .isInstanceOf(OpenExpressionException.class);
        }
    }

    @Nested
    @DisplayName("Map Access Tests | Map 访问测试")
    class MapAccessTests {

        @Test
        @DisplayName("Access map by key | 按键访问 Map")
        void testAccessMapByKey() {
            ctx.setVariable("map", Map.of("key1", "value1", "key2", "value2"));

            IndexAccessNode node = IndexAccessNode.of(IdentifierNode.of("map"), LiteralNode.of("key1"));
            assertThat(node.evaluate(ctx)).isEqualTo("value1");
        }

        @Test
        @DisplayName("Access map with integer key | 使用整数键访问 Map")
        void testAccessMapWithIntegerKey() {
            ctx.setVariable("map", Map.of(1, "one", 2, "two"));

            IndexAccessNode node = IndexAccessNode.of(IdentifierNode.of("map"), LiteralNode.of(1));
            assertThat(node.evaluate(ctx)).isEqualTo("one");
        }

        @Test
        @DisplayName("Access missing map key | 访问不存在的 Map 键")
        void testAccessMissingMapKey() {
            ctx.setVariable("map", Map.of("key", "value"));

            IndexAccessNode node = IndexAccessNode.of(IdentifierNode.of("map"), LiteralNode.of("missing"));
            assertThat(node.evaluate(ctx)).isNull();
        }
    }

    @Nested
    @DisplayName("String Access Tests | 字符串访问测试")
    class StringAccessTests {

        @Test
        @DisplayName("Access string character | 访问字符串字符")
        void testAccessStringCharacter() {
            ctx.setVariable("str", "hello");

            IndexAccessNode node = IndexAccessNode.of(IdentifierNode.of("str"), LiteralNode.of(1));
            assertThat(node.evaluate(ctx)).isEqualTo('e');
        }

        @Test
        @DisplayName("String index out of bounds | 字符串索引越界")
        void testStringIndexOutOfBounds() {
            ctx.setVariable("str", "hi");

            IndexAccessNode node = IndexAccessNode.of(IdentifierNode.of("str"), LiteralNode.of(5));
            assertThatThrownBy(() -> node.evaluate(ctx))
                    .isInstanceOf(OpenExpressionException.class);
        }
    }

    @Nested
    @DisplayName("Index Type Conversion Tests | 索引类型转换测试")
    class IndexTypeConversionTests {

        @Test
        @DisplayName("String index is parsed | 字符串索引被解析")
        void testStringIndexIsParsed() {
            ctx.setVariable("list", List.of("a", "b", "c"));

            IndexAccessNode node = IndexAccessNode.of(IdentifierNode.of("list"), LiteralNode.of("1"));
            assertThat(node.evaluate(ctx)).isEqualTo("b");
        }

        @Test
        @DisplayName("Invalid string index throws | 无效字符串索引抛出异常")
        void testInvalidStringIndex() {
            ctx.setVariable("list", List.of("a", "b"));

            IndexAccessNode node = IndexAccessNode.of(IdentifierNode.of("list"), LiteralNode.of("abc"));
            assertThatThrownBy(() -> node.evaluate(ctx))
                    .isInstanceOf(OpenExpressionException.class);
        }

        @Test
        @DisplayName("Long index is converted | Long 索引被转换")
        void testLongIndexIsConverted() {
            ctx.setVariable("list", List.of("a", "b", "c"));

            IndexAccessNode node = IndexAccessNode.of(IdentifierNode.of("list"), LiteralNode.of(1L));
            assertThat(node.evaluate(ctx)).isEqualTo("b");
        }
    }

    @Nested
    @DisplayName("Null Safety Tests | 空安全测试")
    class NullSafetyTests {

        @Test
        @DisplayName("Standard access on null throws | 标准访问 null 抛出异常")
        void testStandardAccessOnNull() {
            // Use TernaryOpNode to produce null value
            TernaryOpNode nullProducer = TernaryOpNode.of(
                    LiteralNode.of(false),
                    ListLiteralNode.of(LiteralNode.of(1)),
                    LiteralNode.ofNull()
            );

            IndexAccessNode node = IndexAccessNode.of(nullProducer, LiteralNode.of(0));
            assertThatThrownBy(() -> node.evaluate(ctx))
                    .isInstanceOf(OpenExpressionException.class);
        }

        @Test
        @DisplayName("Null-safe access on null returns null | 空安全访问 null 返回 null")
        void testNullSafeAccessOnNull() {
            // Use TernaryOpNode to produce null value
            TernaryOpNode nullProducer = TernaryOpNode.of(
                    LiteralNode.of(false),
                    ListLiteralNode.of(LiteralNode.of(1)),
                    LiteralNode.ofNull()
            );

            IndexAccessNode node = IndexAccessNode.nullSafe(nullProducer, LiteralNode.of(0));
            assertThat(node.evaluate(ctx)).isNull();
        }
    }

    @Nested
    @DisplayName("Type Error Tests | 类型错误测试")
    class TypeErrorTests {

        @Test
        @DisplayName("Non-indexable type throws | 不可索引类型抛出异常")
        void testNonIndexableType() {
            ctx.setVariable("obj", new Object());

            IndexAccessNode node = IndexAccessNode.of(IdentifierNode.of("obj"), LiteralNode.of(0));
            assertThatThrownBy(() -> node.evaluate(ctx))
                    .isInstanceOf(OpenExpressionException.class)
                    .hasMessageContaining("indexable");
        }

        @Test
        @DisplayName("Invalid index type throws | 无效索引类型抛出异常")
        void testInvalidIndexType() {
            ctx.setVariable("list", List.of("a", "b"));

            IndexAccessNode node = IndexAccessNode.of(IdentifierNode.of("list"), LiteralNode.of(true));
            assertThatThrownBy(() -> node.evaluate(ctx))
                    .isInstanceOf(OpenExpressionException.class);
        }
    }

    @Nested
    @DisplayName("ToExpressionString Tests | 表达式字符串测试")
    class ToExpressionStringTests {

        @Test
        @DisplayName("Format index access | 格式化索引访问")
        void testToExpressionString() {
            IndexAccessNode node = IndexAccessNode.of(IdentifierNode.of("list"), LiteralNode.of(0));
            assertThat(node.toExpressionString()).isEqualTo("list[0]");
        }

        @Test
        @DisplayName("Format string index | 格式化字符串索引")
        void testStringIndex() {
            IndexAccessNode node = IndexAccessNode.of(IdentifierNode.of("map"), LiteralNode.of("key"));
            assertThat(node.toExpressionString()).isEqualTo("map['key']");
        }
    }

    @Nested
    @DisplayName("Record Tests | Record 测试")
    class RecordTests {

        @Test
        @DisplayName("Accessors | 访问器")
        void testAccessors() {
            IndexAccessNode node = new IndexAccessNode(IdentifierNode.of("list"), LiteralNode.of(0), true);
            assertThat(node.target()).isEqualTo(IdentifierNode.of("list"));
            assertThat(node.index()).isEqualTo(LiteralNode.of(0));
            assertThat(node.nullSafe()).isTrue();
        }

        @Test
        @DisplayName("getTypeName | 获取类型名称")
        void testGetTypeName() {
            assertThat(IndexAccessNode.of(IdentifierNode.of("list"), LiteralNode.of(0)).getTypeName())
                    .isEqualTo("IndexAccess");
        }
    }
}
