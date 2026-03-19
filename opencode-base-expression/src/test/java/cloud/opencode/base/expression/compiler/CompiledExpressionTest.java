package cloud.opencode.base.expression.compiler;

import cloud.opencode.base.expression.context.StandardContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * CompiledExpression Tests
 * CompiledExpression 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("CompiledExpression Tests | CompiledExpression 测试")
class CompiledExpressionTest {

    @Nested
    @DisplayName("Compile Tests | 编译测试")
    class CompileTests {

        @Test
        @DisplayName("compile creates expression | compile 创建表达式")
        void testCompile() {
            CompiledExpression expr = CompiledExpression.compile("1 + 2");
            assertThat(expr).isNotNull();
            assertThat(expr.getExpressionString()).isEqualTo("1 + 2");
        }

        @Test
        @DisplayName("compile throws on null | compile 对 null 抛出异常")
        void testCompileNull() {
            assertThatThrownBy(() -> CompiledExpression.compile(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("GetValue Tests | getValue 测试")
    class GetValueTests {

        @Test
        @DisplayName("getValue evaluates expression | getValue 求值表达式")
        void testGetValue() {
            CompiledExpression expr = CompiledExpression.compile("1 + 2");
            assertThat(expr.getValue()).isEqualTo(3);
        }

        @Test
        @DisplayName("getValue with context | getValue 使用上下文")
        void testGetValueWithContext() {
            CompiledExpression expr = CompiledExpression.compile("x + y");
            StandardContext ctx = new StandardContext();
            ctx.setVariable("x", 10);
            ctx.setVariable("y", 20);
            assertThat(expr.getValue(ctx)).isEqualTo(30);
        }

        @Test
        @DisplayName("getValue with null context | getValue 使用 null 上下文")
        void testGetValueWithNullContext() {
            CompiledExpression expr = CompiledExpression.compile("1 + 2");
            assertThat(expr.getValue((cloud.opencode.base.expression.context.EvaluationContext) null)).isEqualTo(3);
        }

        @Test
        @DisplayName("getValue with targetType | getValue 使用目标类型")
        void testGetValueWithTargetType() {
            CompiledExpression expr = CompiledExpression.compile("1 + 2");
            assertThat(expr.getValue(Integer.class)).isEqualTo(3);
        }

        @Test
        @DisplayName("getValue with context and targetType | getValue 使用上下文和目标类型")
        void testGetValueWithContextAndTargetType() {
            CompiledExpression expr = CompiledExpression.compile("x");
            StandardContext ctx = new StandardContext();
            ctx.setVariable("x", 42);
            assertThat(expr.getValue(ctx, String.class)).isEqualTo("42");
        }

        @Test
        @DisplayName("getValue with rootObject | getValue 使用根对象")
        void testGetValueWithRootObject() {
            CompiledExpression expr = CompiledExpression.compile("name");
            Map<String, Object> root = Map.of("name", "test");
            assertThat(expr.getValue(root)).isEqualTo("test");
        }

        @Test
        @DisplayName("getValue with rootObject and targetType | getValue 使用根对象和目标类型")
        void testGetValueWithRootObjectAndTargetType() {
            CompiledExpression expr = CompiledExpression.compile("value");
            Map<String, Object> root = Map.of("value", 100);
            assertThat(expr.getValue(root, String.class)).isEqualTo("100");
        }
    }

    @Nested
    @DisplayName("GetValueType Tests | getValueType 测试")
    class GetValueTypeTests {

        @Test
        @DisplayName("getValueType returns type | getValueType 返回类型")
        void testGetValueType() {
            CompiledExpression expr = CompiledExpression.compile("1 + 2");
            assertThat(expr.getValueType()).isEqualTo(Integer.class);
        }

        @Test
        @DisplayName("getValueType with context | getValueType 使用上下文")
        void testGetValueTypeWithContext() {
            CompiledExpression expr = CompiledExpression.compile("'hello'");
            assertThat(expr.getValueType(new StandardContext())).isEqualTo(String.class);
        }

        @Test
        @DisplayName("getValueType for null value | getValueType 对 null 值")
        void testGetValueTypeForNull() {
            CompiledExpression expr = CompiledExpression.compile("null");
            assertThat(expr.getValueType()).isEqualTo(Object.class);
        }
    }

    @Nested
    @DisplayName("GetAst Tests | getAst 测试")
    class GetAstTests {

        @Test
        @DisplayName("getAst returns AST node | getAst 返回 AST 节点")
        void testGetAst() {
            CompiledExpression expr = CompiledExpression.compile("1 + 2");
            assertThat(expr.getAst()).isNotNull();
        }
    }

    @Nested
    @DisplayName("ToString Tests | toString 测试")
    class ToStringTests {

        @Test
        @DisplayName("toString returns formatted string | toString 返回格式化字符串")
        void testToString() {
            CompiledExpression expr = CompiledExpression.compile("1 + 2");
            assertThat(expr.toString()).isEqualTo("CompiledExpression[1 + 2]");
        }
    }
}
