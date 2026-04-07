package cloud.opencode.base.expression;

import cloud.opencode.base.expression.context.StandardContext;
import cloud.opencode.base.expression.sandbox.DefaultSandbox;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Security Tests for Expression Engine
 * 表达式引擎安全测试
 *
 * <p>Validates that security boundaries are enforced for DoS prevention,
 * sandbox escape prevention, and resource exhaustion guards.</p>
 * <p>验证 DoS 防护、沙箱逃逸防护和资源耗尽防护的安全边界。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.3
 */
@DisplayName("Security Tests | 安全测试")
class SecurityTest {

    @Nested
    @DisplayName("DoS Prevention - String Functions | DoS 防护 - 字符串函数")
    class StringDoSTests {

        @Test
        @DisplayName("lpad rejects length exceeding maximum")
        void lpadRejectsHugeLength() {
            assertThatThrownBy(() -> OpenExpression.eval("lpad('x', 100000000)"))
                    .isInstanceOf(OpenExpressionException.class)
                    .hasMessageContaining("exceeds maximum");
        }

        @Test
        @DisplayName("rpad rejects length exceeding maximum")
        void rpadRejectsHugeLength() {
            assertThatThrownBy(() -> OpenExpression.eval("rpad('x', 100000000)"))
                    .isInstanceOf(OpenExpressionException.class)
                    .hasMessageContaining("exceeds maximum");
        }

        @Test
        @DisplayName("repeat rejects count exceeding maximum")
        void repeatRejectsHugeCount() {
            assertThatThrownBy(() -> OpenExpression.eval("repeat('x', 100000000)"))
                    .isInstanceOf(OpenExpressionException.class)
                    .hasMessageContaining("exceeds maximum");
        }

        @Test
        @DisplayName("lpad works within safe bounds")
        void lpadWithinBounds() {
            Object result = OpenExpression.eval("lpad('x', 5, '0')");
            assertThat(result).isEqualTo("0000x");
        }

        @Test
        @DisplayName("rpad works within safe bounds")
        void rpadWithinBounds() {
            Object result = OpenExpression.eval("rpad('x', 5, '0')");
            assertThat(result).isEqualTo("x0000");
        }

        @Test
        @DisplayName("repeat works within safe bounds")
        void repeatWithinBounds() {
            Object result = OpenExpression.eval("repeat('ab', 3)");
            assertThat(result).isEqualTo("ababab");
        }
    }

    @Nested
    @DisplayName("Format String Safety | 格式化字符串安全")
    class FormatStringTests {

        @Test
        @DisplayName("format with valid specifiers works")
        void formatValidSpecifiers() {
            Object result = OpenExpression.eval("format('Hello %s, age %d', 'Jon', 30)");
            assertThat(result).isEqualTo("Hello Jon, age 30");
        }

        @Test
        @DisplayName("format with invalid format string throws")
        void formatInvalidThrows() {
            assertThatThrownBy(() -> OpenExpression.eval("format('%d', 'not_a_number')"))
                    .isInstanceOf(OpenExpressionException.class)
                    .hasMessageContaining("Invalid format string");
        }
    }

    @Nested
    @DisplayName("Integer Overflow Prevention | 整数溢出防护")
    class IntOverflowTests {

        @Test
        @DisplayName("toInt rejects out-of-range long values")
        void toIntRejectsOutOfRange() {
            assertThatThrownBy(() -> OpenExpression.eval("lpad('x', 9999999999999L)"))
                    .isInstanceOf(OpenExpressionException.class)
                    .hasMessageContaining("out of range");
        }
    }

    @Nested
    @DisplayName("Null Safety in New Operators | 新运算符空值安全")
    class NullSafetyTests {

        @Test
        @DisplayName("'in' with null collection returns false")
        void inWithNullCollection() {
            StandardContext ctx = new StandardContext();
            ctx.setVariable("x", 1);
            ctx.setVariable("col", null);
            Object result = OpenExpression.eval("x in col", ctx);
            assertThat(result).isEqualTo(false);
        }

        @Test
        @DisplayName("'between' with null value returns false")
        void betweenWithNullValue() {
            StandardContext ctx = new StandardContext();
            ctx.setVariable("x", null);
            Object result = OpenExpression.eval("x between 1 and 10", ctx);
            assertThat(result).isEqualTo(false);
        }

        @Test
        @DisplayName("'between' with null bounds returns false")
        void betweenWithNullBounds() {
            StandardContext ctx = new StandardContext();
            ctx.setVariable("x", 5);
            ctx.setVariable("lo", null);
            Object result = OpenExpression.eval("x between lo and 10", ctx);
            assertThat(result).isEqualTo(false);
        }
    }

    @Nested
    @DisplayName("Template Depth Limit | 模板深度限制")
    class TemplateDepthTests {

        @Test
        @DisplayName("Template rejects deeply nested braces")
        void templateRejectsDeepNesting() {
            StringBuilder sb = new StringBuilder("${");
            for (int i = 0; i < 60; i++) {
                sb.append("{");
            }
            for (int i = 0; i < 60; i++) {
                sb.append("}");
            }
            sb.append("}");
            String template = sb.toString();

            assertThatThrownBy(() -> ExpressionTemplate.render(template, Map.of()))
                    .isInstanceOf(OpenExpressionException.class)
                    .hasMessageContaining("nesting exceeds maximum depth");
        }

        @Test
        @DisplayName("Template works with moderate nesting")
        void templateModerateNesting() {
            String template = "Result: ${1 + 2}";
            String result = ExpressionTemplate.render(template, Map.of());
            assertThat(result).isEqualTo("Result: 3");
        }
    }

    @Nested
    @DisplayName("Sandbox Escape Prevention | 沙箱逃逸防护")
    class SandboxEscapeTests {

        @Test
        @DisplayName("getClass() is blocked in standard sandbox")
        void getClassBlocked() {
            StandardContext ctx = StandardContext.builder()
                    .sandbox(DefaultSandbox.standard())
                    .build();
            ctx.setVariable("str", "test");

            assertThatThrownBy(() -> OpenExpression.eval("str.getClass()", ctx))
                    .hasMessageContaining("not allowed");
        }

        @Test
        @DisplayName("getClass() is blocked in restrictive sandbox")
        void getClassBlockedRestrictive() {
            StandardContext ctx = StandardContext.builder()
                    .sandbox(DefaultSandbox.restrictive())
                    .build();
            ctx.setVariable("str", "test");

            assertThatThrownBy(() -> OpenExpression.eval("str.getClass()", ctx))
                    .hasMessageContaining("not allowed");
        }
    }

    @Nested
    @DisplayName("Child Context Isolation | 子上下文隔离")
    class ChildContextIsolationTests {

        @Test
        @DisplayName("Child context does not modify parent variables")
        void childDoesNotModifyParent() {
            StandardContext parent = new StandardContext();
            parent.setVariable("x", 10);

            var child = parent.createChild();
            child.setVariable("x", 99);
            child.setVariable("y", 42);

            assertThat(parent.getVariable("x")).isEqualTo(10);
            assertThat(parent.hasVariable("y")).isFalse();

            assertThat(child.getVariable("x")).isEqualTo(99);
            assertThat(child.getVariable("y")).isEqualTo(42);
        }

        @Test
        @DisplayName("Child context inherits parent variables")
        void childInheritsParent() {
            StandardContext parent = new StandardContext();
            parent.setVariable("x", 10);

            var child = parent.createChild();

            assertThat(child.getVariable("x")).isEqualTo(10);
            assertThat(child.hasVariable("x")).isTrue();
        }

        @Test
        @DisplayName("Grandchild context chains correctly")
        void grandchildChaining() {
            StandardContext root = new StandardContext();
            root.setVariable("a", 1);

            var child = root.createChild();
            child.setVariable("b", 2);

            var grandchild = child.createChild();
            grandchild.setVariable("c", 3);

            assertThat(grandchild.getVariable("a")).isEqualTo(1);
            assertThat(grandchild.getVariable("b")).isEqualTo(2);
            assertThat(grandchild.getVariable("c")).isEqualTo(3);
        }
    }
}
