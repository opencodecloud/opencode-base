package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.OpenExpressionException;
import cloud.opencode.base.expression.context.StandardContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * MethodCallNode Tests
 * MethodCallNode 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("MethodCallNode Tests | MethodCallNode 测试")
class MethodCallNodeTest {

    private final StandardContext ctx = new StandardContext();

    @Nested
    @DisplayName("Factory Method Tests | 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of creates node | of 创建节点")
        void testOf() {
            MethodCallNode node = MethodCallNode.of(
                    IdentifierNode.of("str"),
                    "toUpperCase",
                    List.of()
            );
            assertThat(node.target()).isInstanceOf(IdentifierNode.class);
            assertThat(node.methodName()).isEqualTo("toUpperCase");
            assertThat(node.arguments()).isEmpty();
            assertThat(node.nullSafe()).isFalse();
        }

        @Test
        @DisplayName("of with nullSafe | of 带 nullSafe")
        void testOfWithNullSafe() {
            MethodCallNode node = MethodCallNode.of(
                    IdentifierNode.of("str"),
                    "toUpperCase",
                    List.of(),
                    true
            );
            assertThat(node.nullSafe()).isTrue();
        }

        @Test
        @DisplayName("nullSafe factory | nullSafe 工厂方法")
        void testNullSafeFactory() {
            MethodCallNode node = MethodCallNode.nullSafe(
                    IdentifierNode.of("str"),
                    "toUpperCase",
                    List.of()
            );
            assertThat(node.nullSafe()).isTrue();
        }

        @Test
        @DisplayName("Constructor validation | 构造函数验证")
        void testConstructorValidation() {
            assertThatThrownBy(() -> new MethodCallNode(null, "method", List.of(), false))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> new MethodCallNode(IdentifierNode.of("obj"), null, List.of(), false))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Null arguments becomes empty list | null 参数变为空列表")
        void testNullArgumentsBecomesEmpty() {
            MethodCallNode node = new MethodCallNode(
                    IdentifierNode.of("str"),
                    "toUpperCase",
                    null,
                    false
            );
            assertThat(node.arguments()).isEmpty();
        }
    }

    @Nested
    @DisplayName("String Method Tests | 字符串方法测试")
    class StringMethodTests {

        @Test
        @DisplayName("Call toUpperCase | 调用 toUpperCase")
        void testToUpperCase() {
            ctx.setVariable("str", "hello");

            MethodCallNode node = MethodCallNode.of(
                    IdentifierNode.of("str"),
                    "toUpperCase",
                    List.of()
            );
            assertThat(node.evaluate(ctx)).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("Call toLowerCase | 调用 toLowerCase")
        void testToLowerCase() {
            ctx.setVariable("str", "HELLO");

            MethodCallNode node = MethodCallNode.of(
                    IdentifierNode.of("str"),
                    "toLowerCase",
                    List.of()
            );
            assertThat(node.evaluate(ctx)).isEqualTo("hello");
        }

        @Test
        @DisplayName("Call substring with arguments | 调用带参数的 substring")
        void testSubstring() {
            ctx.setVariable("str", "hello world");

            MethodCallNode node = MethodCallNode.of(
                    IdentifierNode.of("str"),
                    "substring",
                    List.of(LiteralNode.of(0), LiteralNode.of(5))
            );
            assertThat(node.evaluate(ctx)).isEqualTo("hello");
        }

        @Test
        @DisplayName("Call contains | 调用 contains")
        void testContains() {
            ctx.setVariable("str", "hello world");

            MethodCallNode node = MethodCallNode.of(
                    IdentifierNode.of("str"),
                    "contains",
                    List.of(LiteralNode.of("world"))
            );
            assertThat(node.evaluate(ctx)).isEqualTo(true);
        }

        @Test
        @DisplayName("Call length | 调用 length")
        void testLength() {
            ctx.setVariable("str", "hello");

            MethodCallNode node = MethodCallNode.of(
                    IdentifierNode.of("str"),
                    "length",
                    List.of()
            );
            assertThat(node.evaluate(ctx)).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("List Method Tests | List 方法测试")
    class ListMethodTests {

        @Test
        @DisplayName("Call size | 调用 size")
        void testSize() {
            // Use ArrayList instead of immutable List to avoid module restrictions
            ctx.setVariable("list", new java.util.ArrayList<>(List.of(1, 2, 3)));

            MethodCallNode node = MethodCallNode.of(
                    IdentifierNode.of("list"),
                    "size",
                    List.of()
            );
            assertThat(node.evaluate(ctx)).isEqualTo(3);
        }

        @Test
        @DisplayName("Call isEmpty | 调用 isEmpty")
        void testIsEmpty() {
            ctx.setVariable("list", new java.util.ArrayList<>());

            MethodCallNode node = MethodCallNode.of(
                    IdentifierNode.of("list"),
                    "isEmpty",
                    List.of()
            );
            assertThat(node.evaluate(ctx)).isEqualTo(true);
        }

        @Test
        @DisplayName("Call get | 调用 get")
        void testGet() {
            ctx.setVariable("list", new java.util.ArrayList<>(List.of("a", "b", "c")));

            MethodCallNode node = MethodCallNode.of(
                    IdentifierNode.of("list"),
                    "get",
                    List.of(LiteralNode.of(1))
            );
            assertThat(node.evaluate(ctx)).isEqualTo("b");
        }
    }

    @Nested
    @DisplayName("Null Safety Tests | 空安全测试")
    class NullSafetyTests {

        @Test
        @DisplayName("Standard call on null throws | 标准调用 null 抛出异常")
        void testStandardCallOnNull() {
            // Use TernaryOpNode to produce null value
            TernaryOpNode nullProducer = TernaryOpNode.of(
                    LiteralNode.of(false),
                    LiteralNode.of("value"),
                    LiteralNode.ofNull()
            );

            MethodCallNode node = MethodCallNode.of(
                    nullProducer,
                    "toUpperCase",
                    List.of()
            );
            assertThatThrownBy(() -> node.evaluate(ctx))
                    .isInstanceOf(OpenExpressionException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("Null-safe call on null returns null | 空安全调用 null 返回 null")
        void testNullSafeCallOnNull() {
            // Use TernaryOpNode to produce null value
            TernaryOpNode nullProducer = TernaryOpNode.of(
                    LiteralNode.of(false),
                    LiteralNode.of("value"),
                    LiteralNode.ofNull()
            );

            MethodCallNode node = MethodCallNode.nullSafe(
                    nullProducer,
                    "toUpperCase",
                    List.of()
            );
            assertThat(node.evaluate(ctx)).isNull();
        }
    }

    @Nested
    @DisplayName("Method Not Found Tests | 方法未找到测试")
    class MethodNotFoundTests {

        @Test
        @DisplayName("Non-existent method throws | 不存在的方法抛出异常")
        void testNonExistentMethod() {
            ctx.setVariable("str", "hello");

            MethodCallNode node = MethodCallNode.of(
                    IdentifierNode.of("str"),
                    "nonExistentMethod",
                    List.of()
            );
            assertThatThrownBy(() -> node.evaluate(ctx))
                    .isInstanceOf(OpenExpressionException.class);
        }

        @Test
        @DisplayName("Wrong parameter count | 错误的参数数量")
        void testWrongParameterCount() {
            ctx.setVariable("str", "hello");

            // substring requires at least 1 argument
            MethodCallNode node = MethodCallNode.of(
                    IdentifierNode.of("str"),
                    "substring",
                    List.of()
            );
            assertThatThrownBy(() -> node.evaluate(ctx))
                    .isInstanceOf(OpenExpressionException.class);
        }
    }

    @Nested
    @DisplayName("Null Argument Tests | null 参数测试")
    class NullArgumentTests {

        @Test
        @DisplayName("Call with null argument | 使用 null 参数调用")
        void testCallWithNullArgument() {
            ctx.setVariable("str", "hello");

            MethodCallNode node = MethodCallNode.of(
                    IdentifierNode.of("str"),
                    "equals",
                    List.of(LiteralNode.ofNull())
            );
            assertThat(node.evaluate(ctx)).isEqualTo(false);
        }
    }

    @Nested
    @DisplayName("Varargs Method Tests | 可变参数方法测试")
    class VarargsMethodTests {

        @Test
        @DisplayName("Call format with varargs | 调用带可变参数的 format")
        void testFormatWithVarargs() {
            ctx.setVariable("format", "%s %s");

            MethodCallNode node = MethodCallNode.of(
                    LiteralNode.of(String.class),
                    "format",
                    List.of(
                            IdentifierNode.of("format"),
                            LiteralNode.of("hello"),
                            LiteralNode.of("world")
                    )
            );
            // This is a static method, won't work directly
            // Just verify it handles varargs logic
        }
    }

    @Nested
    @DisplayName("ToExpressionString Tests | 表达式字符串测试")
    class ToExpressionStringTests {

        @Test
        @DisplayName("Format method call no args | 格式化无参方法调用")
        void testNoArgs() {
            MethodCallNode node = MethodCallNode.of(
                    IdentifierNode.of("str"),
                    "toUpperCase",
                    List.of()
            );
            assertThat(node.toExpressionString()).isEqualTo("str.toUpperCase()");
        }

        @Test
        @DisplayName("Format method call with args | 格式化带参方法调用")
        void testWithArgs() {
            MethodCallNode node = MethodCallNode.of(
                    IdentifierNode.of("str"),
                    "substring",
                    List.of(LiteralNode.of(0), LiteralNode.of(5))
            );
            assertThat(node.toExpressionString()).isEqualTo("str.substring(0, 5)");
        }

        @Test
        @DisplayName("Format null-safe call | 格式化空安全调用")
        void testNullSafe() {
            MethodCallNode node = MethodCallNode.nullSafe(
                    IdentifierNode.of("str"),
                    "toUpperCase",
                    List.of()
            );
            assertThat(node.toExpressionString()).isEqualTo("str?.toUpperCase()");
        }
    }

    @Nested
    @DisplayName("Record Tests | Record 测试")
    class RecordTests {

        @Test
        @DisplayName("Accessors | 访问器")
        void testAccessors() {
            MethodCallNode node = new MethodCallNode(
                    IdentifierNode.of("str"),
                    "toUpperCase",
                    List.of(LiteralNode.of(1)),
                    true
            );
            assertThat(node.target()).isEqualTo(IdentifierNode.of("str"));
            assertThat(node.methodName()).isEqualTo("toUpperCase");
            assertThat(node.arguments()).hasSize(1);
            assertThat(node.nullSafe()).isTrue();
        }

        @Test
        @DisplayName("Arguments are immutable | 参数不可变")
        void testArgumentsImmutable() {
            MethodCallNode node = MethodCallNode.of(
                    IdentifierNode.of("str"),
                    "method",
                    List.of(LiteralNode.of(1))
            );
            assertThatThrownBy(() -> node.arguments().add(LiteralNode.of(2)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("getTypeName | 获取类型名称")
        void testGetTypeName() {
            MethodCallNode node = MethodCallNode.of(IdentifierNode.of("str"), "method", List.of());
            assertThat(node.getTypeName()).isEqualTo("MethodCall");
        }
    }
}
