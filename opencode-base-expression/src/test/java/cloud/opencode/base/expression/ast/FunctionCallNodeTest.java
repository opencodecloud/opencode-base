package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.OpenExpressionException;
import cloud.opencode.base.expression.context.StandardContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * FunctionCallNode Tests
 * FunctionCallNode 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("FunctionCallNode Tests | FunctionCallNode 测试")
class FunctionCallNodeTest {

    private final StandardContext ctx = new StandardContext();

    @Nested
    @DisplayName("Factory Method Tests | 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of with list | of 使用列表")
        void testOfWithList() {
            FunctionCallNode node = FunctionCallNode.of(
                    "upper",
                    List.of(LiteralNode.of("hello"))
            );
            assertThat(node.functionName()).isEqualTo("upper");
            assertThat(node.arguments()).hasSize(1);
        }

        @Test
        @DisplayName("of with single argument | of 使用单个参数")
        void testOfWithSingleArgument() {
            FunctionCallNode node = FunctionCallNode.of("upper", LiteralNode.of("hello"));
            assertThat(node.functionName()).isEqualTo("upper");
            assertThat(node.arguments()).hasSize(1);
        }

        @Test
        @DisplayName("Constructor validation | 构造函数验证")
        void testConstructorValidation() {
            assertThatThrownBy(() -> new FunctionCallNode(null, List.of()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Null arguments becomes empty list | null 参数变为空列表")
        void testNullArgumentsBecomesEmpty() {
            FunctionCallNode node = new FunctionCallNode("func", null);
            assertThat(node.arguments()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Built-in Function Tests | 内置函数测试")
    class BuiltInFunctionTests {

        @Test
        @DisplayName("Call upper function | 调用 upper 函数")
        void testUpperFunction() {
            FunctionCallNode node = FunctionCallNode.of("upper", LiteralNode.of("hello"));
            assertThat(node.evaluate(ctx)).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("Call lower function | 调用 lower 函数")
        void testLowerFunction() {
            FunctionCallNode node = FunctionCallNode.of("lower", LiteralNode.of("HELLO"));
            assertThat(node.evaluate(ctx)).isEqualTo("hello");
        }

        @Test
        @DisplayName("Call trim function | 调用 trim 函数")
        void testTrimFunction() {
            FunctionCallNode node = FunctionCallNode.of("trim", LiteralNode.of("  hello  "));
            assertThat(node.evaluate(ctx)).isEqualTo("hello");
        }

        @Test
        @DisplayName("Call len function | 调用 len 函数")
        void testLenFunction() {
            FunctionCallNode node = FunctionCallNode.of("len", LiteralNode.of("hello"));
            assertThat(node.evaluate(ctx)).isEqualTo(5);
        }

        @Test
        @DisplayName("Call abs function | 调用 abs 函数")
        void testAbsFunction() {
            FunctionCallNode node = FunctionCallNode.of("abs", LiteralNode.of(-42));
            Object result = node.evaluate(ctx);
            assertThat(((Number) result).intValue()).isEqualTo(42);
        }

        @Test
        @DisplayName("Call max function | 调用 max 函数")
        void testMaxFunction() {
            // max function expects multiple arguments
            FunctionCallNode node = FunctionCallNode.of(
                    "max",
                    List.of(LiteralNode.of(1), LiteralNode.of(5), LiteralNode.of(3))
            );
            Object result = node.evaluate(ctx);
            assertThat(((Number) result).intValue()).isEqualTo(5);
        }

        @Test
        @DisplayName("Call min function | 调用 min 函数")
        void testMinFunction() {
            // min function expects multiple arguments
            FunctionCallNode node = FunctionCallNode.of(
                    "min",
                    List.of(LiteralNode.of(1), LiteralNode.of(5), LiteralNode.of(3))
            );
            Object result = node.evaluate(ctx);
            assertThat(((Number) result).intValue()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Type Functions Tests | 类型函数测试")
    class TypeFunctionsTests {

        @Test
        @DisplayName("Call isnull function | 调用 isnull 函数")
        void testIsNullFunction() {
            FunctionCallNode nodeNull = FunctionCallNode.of("isnull", LiteralNode.ofNull());
            FunctionCallNode nodeNotNull = FunctionCallNode.of("isnull", LiteralNode.of(1));

            assertThat(nodeNull.evaluate(ctx)).isEqualTo(true);
            assertThat(nodeNotNull.evaluate(ctx)).isEqualTo(false);
        }

        @Test
        @DisplayName("Call typeof function | 调用 typeof 函数")
        void testTypeofFunction() {
            assertThat(FunctionCallNode.of("typeof", LiteralNode.of("hello")).evaluate(ctx)).isEqualTo("string");
            assertThat(FunctionCallNode.of("typeof", LiteralNode.of(123)).evaluate(ctx)).isEqualTo("integer");
            assertThat(FunctionCallNode.of("typeof", LiteralNode.ofNull()).evaluate(ctx)).isEqualTo("null");
        }
    }

    @Nested
    @DisplayName("Function Not Found Tests | 函数未找到测试")
    class FunctionNotFoundTests {

        @Test
        @DisplayName("Non-existent function throws | 不存在的函数抛出异常")
        void testNonExistentFunction() {
            FunctionCallNode node = FunctionCallNode.of("nonExistentFunction", LiteralNode.of(1));
            assertThatThrownBy(() -> node.evaluate(ctx))
                    .isInstanceOf(OpenExpressionException.class);
        }
    }

    @Nested
    @DisplayName("Function Exception Tests | 函数异常测试")
    class FunctionExceptionTests {

        @Test
        @DisplayName("Function failure wraps exception | 函数失败包装异常")
        void testFunctionFailureWrapsException() {
            // Try to call a function that will fail internally
            FunctionCallNode node = FunctionCallNode.of(
                    "substring",
                    List.of(LiteralNode.of("hello"), LiteralNode.of(10), LiteralNode.of(20))
            );
            assertThatThrownBy(() -> node.evaluate(ctx))
                    .isInstanceOf(OpenExpressionException.class);
        }
    }

    @Nested
    @DisplayName("ToExpressionString Tests | 表达式字符串测试")
    class ToExpressionStringTests {

        @Test
        @DisplayName("Format function call no args | 格式化无参函数调用")
        void testNoArgs() {
            FunctionCallNode node = FunctionCallNode.of("now", List.of());
            assertThat(node.toExpressionString()).isEqualTo("now()");
        }

        @Test
        @DisplayName("Format function call with args | 格式化带参函数调用")
        void testWithArgs() {
            FunctionCallNode node = FunctionCallNode.of(
                    "substring",
                    List.of(LiteralNode.of("hello"), LiteralNode.of(0), LiteralNode.of(2))
            );
            assertThat(node.toExpressionString()).isEqualTo("substring('hello', 0, 2)");
        }
    }

    @Nested
    @DisplayName("Record Tests | Record 测试")
    class RecordTests {

        @Test
        @DisplayName("Accessors | 访问器")
        void testAccessors() {
            FunctionCallNode node = new FunctionCallNode("func", List.of(LiteralNode.of(1)));
            assertThat(node.functionName()).isEqualTo("func");
            assertThat(node.arguments()).hasSize(1);
        }

        @Test
        @DisplayName("Arguments are immutable | 参数不可变")
        void testArgumentsImmutable() {
            FunctionCallNode node = FunctionCallNode.of("func", List.of(LiteralNode.of(1)));
            assertThatThrownBy(() -> node.arguments().add(LiteralNode.of(2)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Equals and hashCode | equals 和 hashCode")
        void testEqualsAndHashCode() {
            FunctionCallNode node1 = FunctionCallNode.of("upper", LiteralNode.of("a"));
            FunctionCallNode node2 = FunctionCallNode.of("upper", LiteralNode.of("a"));
            assertThat(node1).isEqualTo(node2);
            assertThat(node1.hashCode()).isEqualTo(node2.hashCode());
        }

        @Test
        @DisplayName("getTypeName | 获取类型名称")
        void testGetTypeName() {
            assertThat(FunctionCallNode.of("func", List.of()).getTypeName()).isEqualTo("FunctionCall");
        }
    }
}
