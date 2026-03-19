package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.context.EvaluationContext;
import cloud.opencode.base.expression.context.MapContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Node Interface Tests
 * Node 接口测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("Node Interface Tests | Node 接口测试")
class NodeTest {

    @Nested
    @DisplayName("Default Methods Tests | 默认方法测试")
    class DefaultMethodsTests {

        @Test
        @DisplayName("getTypeName returns class name without Node suffix | getTypeName 返回不带 Node 后缀的类名")
        void testGetTypeName() {
            Node node = LiteralNode.of(42);
            assertThat(node.getTypeName()).isEqualTo("Literal");
        }

        @Test
        @DisplayName("getTypeName for IdentifierNode | getTypeName 对 IdentifierNode")
        void testGetTypeNameIdentifier() {
            Node node = IdentifierNode.of("x");
            assertThat(node.getTypeName()).isEqualTo("Identifier");
        }

        @Test
        @DisplayName("getTypeName for BinaryOpNode | getTypeName 对 BinaryOpNode")
        void testGetTypeNameBinaryOp() {
            Node left = LiteralNode.of(1);
            Node right = LiteralNode.of(2);
            Node node = BinaryOpNode.of(left, "+", right);
            assertThat(node.getTypeName()).isEqualTo("BinaryOp");
        }
    }

    @Nested
    @DisplayName("Evaluate Tests | 求值测试")
    class EvaluateTests {

        @Test
        @DisplayName("evaluate LiteralNode returns value | evaluate LiteralNode 返回值")
        void testEvaluateLiteral() {
            Node node = LiteralNode.of(42);
            EvaluationContext context = new MapContext();

            Object result = node.evaluate(context);
            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("evaluate IdentifierNode returns variable | evaluate IdentifierNode 返回变量")
        void testEvaluateIdentifier() {
            Node node = IdentifierNode.of("x");
            MapContext context = new MapContext();
            context.setVariable("x", 100);

            Object result = node.evaluate(context);
            assertThat(result).isEqualTo(100);
        }

        @Test
        @DisplayName("evaluate BinaryOpNode returns computed value | evaluate BinaryOpNode 返回计算值")
        void testEvaluateBinaryOp() {
            Node left = LiteralNode.of(10L);
            Node right = LiteralNode.of(5L);
            Node node = BinaryOpNode.of(left, "+", right);
            EvaluationContext context = new MapContext();

            Object result = node.evaluate(context);
            assertThat(result).isEqualTo(15L);
        }

        @Test
        @DisplayName("evaluate UnaryOpNode returns computed value | evaluate UnaryOpNode 返回计算值")
        void testEvaluateUnaryOp() {
            Node operand = LiteralNode.of(true);
            Node node = UnaryOpNode.of("!", operand);
            EvaluationContext context = new MapContext();

            Object result = node.evaluate(context);
            assertThat(result).isEqualTo(false);
        }

        @Test
        @DisplayName("evaluate TernaryOpNode returns correct branch | evaluate TernaryOpNode 返回正确的分支")
        void testEvaluateTernaryOp() {
            Node condition = LiteralNode.of(true);
            Node thenBranch = LiteralNode.of("yes");
            Node elseBranch = LiteralNode.of("no");
            Node node = TernaryOpNode.of(condition, thenBranch, elseBranch);
            EvaluationContext context = new MapContext();

            Object result = node.evaluate(context);
            assertThat(result).isEqualTo("yes");
        }
    }

    @Nested
    @DisplayName("ToExpressionString Tests | toExpressionString 测试")
    class ToExpressionStringTests {

        @Test
        @DisplayName("LiteralNode toExpressionString returns string representation | LiteralNode toExpressionString 返回字符串表示")
        void testLiteralToExpressionString() {
            Node node = LiteralNode.of(42);
            assertThat(node.toExpressionString()).isEqualTo("42");
        }

        @Test
        @DisplayName("String literal toExpressionString is quoted | 字符串字面量 toExpressionString 带引号")
        void testStringLiteralToExpressionString() {
            Node node = LiteralNode.of("hello");
            assertThat(node.toExpressionString()).contains("hello");
        }

        @Test
        @DisplayName("IdentifierNode toExpressionString returns name | IdentifierNode toExpressionString 返回名称")
        void testIdentifierToExpressionString() {
            Node node = IdentifierNode.of("myVar");
            assertThat(node.toExpressionString()).isEqualTo("myVar");
        }

        @Test
        @DisplayName("BinaryOpNode toExpressionString shows operation | BinaryOpNode toExpressionString 显示操作")
        void testBinaryOpToExpressionString() {
            Node left = LiteralNode.of(1);
            Node right = LiteralNode.of(2);
            Node node = BinaryOpNode.of(left, "+", right);

            String expr = node.toExpressionString();
            assertThat(expr).contains("1");
            assertThat(expr).contains("+");
            assertThat(expr).contains("2");
        }
    }

    @Nested
    @DisplayName("Sealed Interface Tests | 密封接口测试")
    class SealedInterfaceTests {

        @Test
        @DisplayName("Node is sealed with specific permits | Node 是有特定许可的密封接口")
        void testNodeIsSealed() {
            assertThat(Node.class.isSealed()).isTrue();

            Class<?>[] permitted = Node.class.getPermittedSubclasses();
            assertThat(permitted).isNotEmpty();
        }

        @Test
        @DisplayName("All permitted subclasses are Node implementations | 所有许可的子类都是 Node 实现")
        void testPermittedSubclasses() {
            Class<?>[] permitted = Node.class.getPermittedSubclasses();

            for (Class<?> clazz : permitted) {
                assertThat(Node.class.isAssignableFrom(clazz)).isTrue();
            }
        }

        @Test
        @DisplayName("LiteralNode is permitted | LiteralNode 是许可的")
        void testLiteralNodePermitted() {
            Class<?>[] permitted = Node.class.getPermittedSubclasses();
            List<String> names = java.util.Arrays.stream(permitted)
                    .map(Class::getSimpleName)
                    .toList();

            assertThat(names).contains("LiteralNode");
        }

        @Test
        @DisplayName("IdentifierNode is permitted | IdentifierNode 是许可的")
        void testIdentifierNodePermitted() {
            Class<?>[] permitted = Node.class.getPermittedSubclasses();
            List<String> names = java.util.Arrays.stream(permitted)
                    .map(Class::getSimpleName)
                    .toList();

            assertThat(names).contains("IdentifierNode");
        }

        @Test
        @DisplayName("BinaryOpNode is permitted | BinaryOpNode 是许可的")
        void testBinaryOpNodePermitted() {
            Class<?>[] permitted = Node.class.getPermittedSubclasses();
            List<String> names = java.util.Arrays.stream(permitted)
                    .map(Class::getSimpleName)
                    .toList();

            assertThat(names).contains("BinaryOpNode");
        }
    }

    @Nested
    @DisplayName("Node Hierarchy Tests | 节点层次结构测试")
    class NodeHierarchyTests {

        @Test
        @DisplayName("ListLiteralNode contains elements | ListLiteralNode 包含元素")
        void testListLiteralNode() {
            List<Node> elements = List.of(
                    LiteralNode.of(1),
                    LiteralNode.of(2),
                    LiteralNode.of(3)
            );
            Node node = ListLiteralNode.of(elements);
            EvaluationContext context = new MapContext();

            Object result = node.evaluate(context);
            assertThat(result).isInstanceOf(List.class);
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) result;
            assertThat(list).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("PropertyAccessNode accesses property | PropertyAccessNode 访问属性")
        void testPropertyAccessNode() {
            MapContext context = new MapContext();
            context.setVariable("obj", new TestObject("testValue"));

            Node target = IdentifierNode.of("obj");
            Node node = PropertyAccessNode.of(target, "name");

            Object result = node.evaluate(context);
            assertThat(result).isEqualTo("testValue");
        }

        @Test
        @DisplayName("IndexAccessNode accesses index | IndexAccessNode 访问索引")
        void testIndexAccessNode() {
            MapContext context = new MapContext();
            context.setVariable("arr", List.of("a", "b", "c"));

            Node target = IdentifierNode.of("arr");
            Node index = LiteralNode.of(1);
            Node node = IndexAccessNode.of(target, index);

            Object result = node.evaluate(context);
            assertThat(result).isEqualTo("b");
        }
    }

    // Helper class for testing
    public static class TestObject {
        private final String name;

        public TestObject(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
