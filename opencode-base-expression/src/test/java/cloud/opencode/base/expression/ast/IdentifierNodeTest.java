package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.OpenExpressionException;
import cloud.opencode.base.expression.context.StandardContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * IdentifierNode Tests
 * IdentifierNode 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("IdentifierNode Tests | IdentifierNode 测试")
class IdentifierNodeTest {

    @Nested
    @DisplayName("Factory Method Tests | 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of creates node | of 创建节点")
        void testOf() {
            IdentifierNode node = IdentifierNode.of("myVar");
            assertThat(node.name()).isEqualTo("myVar");
        }

        @Test
        @DisplayName("Constructor validation | 构造函数验证")
        void testConstructorValidation() {
            assertThatThrownBy(() -> new IdentifierNode(null))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new IdentifierNode(""))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new IdentifierNode("   "))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Variable Lookup Tests | 变量查找测试")
    class VariableLookupTests {

        @Test
        @DisplayName("Get variable | 获取变量")
        void testGetVariable() {
            StandardContext ctx = new StandardContext();
            ctx.setVariable("x", 42);

            IdentifierNode node = IdentifierNode.of("x");
            assertThat(node.evaluate(ctx)).isEqualTo(42);
        }

        @Test
        @DisplayName("Get variable with # prefix | 获取带 # 前缀的变量")
        void testGetVariableWithPrefix() {
            StandardContext ctx = new StandardContext();
            ctx.setVariable("x", 42);

            IdentifierNode node = IdentifierNode.of("#x");
            assertThat(node.evaluate(ctx)).isEqualTo(42);
        }

        @Test
        @DisplayName("Variable not found throws | 变量未找到抛出异常")
        void testVariableNotFound() {
            StandardContext ctx = new StandardContext();
            IdentifierNode node = IdentifierNode.of("nonexistent");
            assertThatThrownBy(() -> node.evaluate(ctx))
                    .isInstanceOf(OpenExpressionException.class)
                    .hasMessageContaining("Variable not found");
        }
    }

    @Nested
    @DisplayName("Special Identifier Tests | 特殊标识符测试")
    class SpecialIdentifierTests {

        @Test
        @DisplayName("root returns root object | root 返回根对象")
        void testRoot() {
            Map<String, Object> rootObj = Map.of("key", "value");
            StandardContext ctx = new StandardContext(rootObj);

            assertThat(IdentifierNode.of("root").evaluate(ctx)).isEqualTo(rootObj);
            assertThat(IdentifierNode.of("#root").evaluate(ctx)).isEqualTo(rootObj);
        }

        @Test
        @DisplayName("this returns this object | this 返回 this 对象")
        void testThis() {
            StandardContext ctx = new StandardContext();
            ctx.setVariable("#this", "current");

            assertThat(IdentifierNode.of("this").evaluate(ctx)).isEqualTo("current");
            assertThat(IdentifierNode.of("#this").evaluate(ctx)).isEqualTo("current");
        }

        @Test
        @DisplayName("this returns root when not set | this 未设置时返回 root")
        void testThisReturnsRoot() {
            Map<String, Object> rootObj = Map.of("key", "value");
            StandardContext ctx = new StandardContext(rootObj);

            assertThat(IdentifierNode.of("this").evaluate(ctx)).isEqualTo(rootObj);
        }
    }

    @Nested
    @DisplayName("Property Access Tests | 属性访问测试")
    class PropertyAccessTests {

        @Test
        @DisplayName("Access root object property | 访问根对象属性")
        void testAccessRootProperty() {
            Map<String, Object> rootObj = Map.of("name", "John");
            StandardContext ctx = new StandardContext(rootObj);

            IdentifierNode node = IdentifierNode.of("name");
            assertThat(node.evaluate(ctx)).isEqualTo("John");
        }
    }

    @Nested
    @DisplayName("ToExpressionString Tests | 表达式字符串测试")
    class ToExpressionStringTests {

        @Test
        @DisplayName("Returns name | 返回名称")
        void testToExpressionString() {
            assertThat(IdentifierNode.of("myVar").toExpressionString()).isEqualTo("myVar");
            assertThat(IdentifierNode.of("#root").toExpressionString()).isEqualTo("#root");
        }
    }

    @Nested
    @DisplayName("Record Tests | Record 测试")
    class RecordTests {

        @Test
        @DisplayName("name accessor | name 访问器")
        void testNameAccessor() {
            IdentifierNode node = new IdentifierNode("myVar");
            assertThat(node.name()).isEqualTo("myVar");
        }

        @Test
        @DisplayName("Equals and hashCode | equals 和 hashCode")
        void testEqualsAndHashCode() {
            IdentifierNode node1 = IdentifierNode.of("x");
            IdentifierNode node2 = IdentifierNode.of("x");
            IdentifierNode node3 = IdentifierNode.of("y");

            assertThat(node1).isEqualTo(node2);
            assertThat(node1.hashCode()).isEqualTo(node2.hashCode());
            assertThat(node1).isNotEqualTo(node3);
        }

        @Test
        @DisplayName("getTypeName | 获取类型名称")
        void testGetTypeName() {
            assertThat(IdentifierNode.of("x").getTypeName()).isEqualTo("Identifier");
        }
    }
}
