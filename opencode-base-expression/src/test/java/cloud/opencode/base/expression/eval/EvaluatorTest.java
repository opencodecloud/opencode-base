package cloud.opencode.base.expression.eval;

import cloud.opencode.base.expression.ast.LiteralNode;
import cloud.opencode.base.expression.ast.Node;
import cloud.opencode.base.expression.context.EvaluationContext;
import cloud.opencode.base.expression.context.MapContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Evaluator Interface Tests
 * Evaluator 接口测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("Evaluator Interface Tests | Evaluator 接口测试")
class EvaluatorTest {

    @Nested
    @DisplayName("Default Methods Tests | 默认方法测试")
    class DefaultMethodsTests {

        @Test
        @DisplayName("canEvaluate returns true by default | canEvaluate 默认返回 true")
        void testCanEvaluateDefault() {
            Evaluator evaluator = new SimpleEvaluator();
            Node node = LiteralNode.of(42);

            assertThat(evaluator.canEvaluate(node)).isTrue();
        }

        @Test
        @DisplayName("evaluate with type converts result | evaluate 带类型转换结果")
        void testEvaluateWithType() {
            Evaluator evaluator = new SimpleEvaluator();
            Node node = LiteralNode.of(42L);
            EvaluationContext context = new MapContext();

            Integer result = evaluator.evaluate(node, context, Integer.class);
            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("evaluate with type handles string to number | evaluate 带类型处理字符串到数字")
        void testEvaluateWithTypeStringToNumber() {
            Evaluator evaluator = (n, ctx) -> "123";
            Node node = LiteralNode.of("123");
            EvaluationContext context = new MapContext();

            Long result = evaluator.evaluate(node, context, Long.class);
            assertThat(result).isEqualTo(123L);
        }

        @Test
        @DisplayName("evaluate with type handles null | evaluate 带类型处理 null")
        void testEvaluateWithTypeNull() {
            Evaluator evaluator = (n, ctx) -> null;
            Node node = LiteralNode.of(null);
            EvaluationContext context = new MapContext();

            String result = evaluator.evaluate(node, context, String.class);
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Basic Evaluation Tests | 基本求值测试")
    class BasicEvaluationTests {

        @Test
        @DisplayName("evaluate returns node result | evaluate 返回节点结果")
        void testEvaluateBasic() {
            Evaluator evaluator = new SimpleEvaluator();
            Node node = LiteralNode.of(100);
            EvaluationContext context = new MapContext();

            Object result = evaluator.evaluate(node, context);
            assertThat(result).isEqualTo(100);
        }

        @Test
        @DisplayName("evaluate with string literal | evaluate 处理字符串字面量")
        void testEvaluateStringLiteral() {
            Evaluator evaluator = new SimpleEvaluator();
            Node node = LiteralNode.of("hello");
            EvaluationContext context = new MapContext();

            Object result = evaluator.evaluate(node, context);
            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("evaluate with boolean literal | evaluate 处理布尔字面量")
        void testEvaluateBooleanLiteral() {
            Evaluator evaluator = new SimpleEvaluator();
            Node node = LiteralNode.of(true);
            EvaluationContext context = new MapContext();

            Object result = evaluator.evaluate(node, context);
            assertThat(result).isEqualTo(true);
        }

        @Test
        @DisplayName("evaluate with null literal | evaluate 处理 null 字面量")
        void testEvaluateNullLiteral() {
            Evaluator evaluator = new SimpleEvaluator();
            Node node = LiteralNode.of(null);
            EvaluationContext context = new MapContext();

            Object result = evaluator.evaluate(node, context);
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Custom Evaluator Tests | 自定义求值器测试")
    class CustomEvaluatorTests {

        @Test
        @DisplayName("custom evaluator with selective canEvaluate | 自定义求值器带选择性 canEvaluate")
        void testSelectiveCanEvaluate() {
            Evaluator evaluator = new Evaluator() {
                @Override
                public Object evaluate(Node node, EvaluationContext context) {
                    return node.evaluate(context);
                }

                @Override
                public boolean canEvaluate(Node node) {
                    return node instanceof LiteralNode;
                }
            };

            Node literalNode = LiteralNode.of(42);
            assertThat(evaluator.canEvaluate(literalNode)).isTrue();
        }

        @Test
        @DisplayName("evaluator uses context | 求值器使用上下文")
        void testEvaluatorUsesContext() {
            Evaluator evaluator = (node, context) -> {
                // Return value from context instead of node
                return context.getVariable("override");
            };

            Node node = LiteralNode.of(42);
            MapContext context = new MapContext();
            context.setVariable("override", "overridden value");

            Object result = evaluator.evaluate(node, context);
            assertThat(result).isEqualTo("overridden value");
        }
    }

    @Nested
    @DisplayName("AstEvaluator Integration Tests | AstEvaluator 集成测试")
    class AstEvaluatorIntegrationTests {

        @Test
        @DisplayName("AstEvaluator evaluates literal node | AstEvaluator 求值字面量节点")
        void testAstEvaluatorLiteral() {
            Evaluator evaluator = new AstEvaluator();
            Node node = LiteralNode.of(42);
            EvaluationContext context = new MapContext();

            Object result = evaluator.evaluate(node, context);
            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("AstEvaluator can evaluate any node | AstEvaluator 可以求值任何节点")
        void testAstEvaluatorCanEvaluate() {
            Evaluator evaluator = new AstEvaluator();
            Node node = LiteralNode.of(42);

            assertThat(evaluator.canEvaluate(node)).isTrue();
        }
    }

    // Simple evaluator implementation for testing
    private static class SimpleEvaluator implements Evaluator {
        @Override
        public Object evaluate(Node node, EvaluationContext context) {
            return node.evaluate(context);
        }
    }
}
