package cloud.opencode.base.expression.compiler;

import cloud.opencode.base.expression.ast.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Optimizer Tests
 * Optimizer 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("Optimizer Tests | Optimizer 测试")
class OptimizerTest {

    @Nested
    @DisplayName("Constant Folding Tests | 常量折叠测试")
    class ConstantFoldingTests {

        @Test
        @DisplayName("Fold arithmetic constants | 折叠算术常量")
        void testFoldArithmeticConstants() {
            Optimizer optimizer = new Optimizer();

            // 2 + 3
            Node node = BinaryOpNode.of(LiteralNode.of(2), "+", LiteralNode.of(3));
            Node optimized = optimizer.foldConstants(node);

            assertThat(optimized).isInstanceOf(LiteralNode.class);
            assertThat(((LiteralNode) optimized).value()).isEqualTo(5L);
        }

        @Test
        @DisplayName("Fold multiplication constants | 折叠乘法常量")
        void testFoldMultiplicationConstants() {
            Optimizer optimizer = new Optimizer();

            // 4 * 5
            Node node = BinaryOpNode.of(LiteralNode.of(4), "*", LiteralNode.of(5));
            Node optimized = optimizer.foldConstants(node);

            assertThat(optimized).isInstanceOf(LiteralNode.class);
            assertThat(((LiteralNode) optimized).value()).isEqualTo(20L);
        }

        @Test
        @DisplayName("Fold nested arithmetic | 折叠嵌套算术")
        void testFoldNestedArithmetic() {
            Optimizer optimizer = new Optimizer();

            // (2 + 3) * 4
            Node inner = BinaryOpNode.of(LiteralNode.of(2), "+", LiteralNode.of(3));
            Node node = BinaryOpNode.of(inner, "*", LiteralNode.of(4));
            Node optimized = optimizer.foldConstants(node);

            assertThat(optimized).isInstanceOf(LiteralNode.class);
            assertThat(((LiteralNode) optimized).value()).isEqualTo(20L);
        }

        @Test
        @DisplayName("Don't fold non-constant | 不折叠非常量")
        void testDontFoldNonConstant() {
            Optimizer optimizer = new Optimizer();

            // x + 3 (x is variable)
            Node node = BinaryOpNode.of(IdentifierNode.of("x"), "+", LiteralNode.of(3));
            Node optimized = optimizer.foldConstants(node);

            assertThat(optimized).isInstanceOf(BinaryOpNode.class);
        }

        @Test
        @DisplayName("Fold unary negation | 折叠一元取负")
        void testFoldUnaryNegation() {
            Optimizer optimizer = new Optimizer();

            // -5
            Node node = UnaryOpNode.of("-", LiteralNode.of(5));
            Node optimized = optimizer.foldConstants(node);

            assertThat(optimized).isInstanceOf(LiteralNode.class);
            assertThat(((LiteralNode) optimized).value()).isEqualTo(-5);
        }

        @Test
        @DisplayName("Fold ternary with true condition | 折叠条件为 true 的三元")
        void testFoldTernaryTrue() {
            Optimizer optimizer = new Optimizer();

            // true ? "yes" : "no"
            Node node = TernaryOpNode.of(
                    LiteralNode.of(true),
                    LiteralNode.of("yes"),
                    LiteralNode.of("no")
            );
            Node optimized = optimizer.foldConstants(node);

            assertThat(optimized).isInstanceOf(LiteralNode.class);
            assertThat(((LiteralNode) optimized).value()).isEqualTo("yes");
        }

        @Test
        @DisplayName("Fold ternary with false condition | 折叠条件为 false 的三元")
        void testFoldTernaryFalse() {
            Optimizer optimizer = new Optimizer();

            // false ? "yes" : "no"
            Node node = TernaryOpNode.of(
                    LiteralNode.of(false),
                    LiteralNode.of("yes"),
                    LiteralNode.of("no")
            );
            Node optimized = optimizer.foldConstants(node);

            assertThat(optimized).isInstanceOf(LiteralNode.class);
            assertThat(((LiteralNode) optimized).value()).isEqualTo("no");
        }
    }

    @Nested
    @DisplayName("Short Circuit Optimization Tests | 短路优化测试")
    class ShortCircuitTests {

        @Test
        @DisplayName("Short circuit AND with false | 短路 AND 为 false")
        void testShortCircuitAndFalse() {
            Optimizer optimizer = new Optimizer();

            // false && x
            Node node = BinaryOpNode.of(LiteralNode.of(false), "&&", IdentifierNode.of("x"));
            Node optimized = optimizer.optimizeShortCircuit(node);

            assertThat(optimized).isInstanceOf(LiteralNode.class);
            assertThat(((LiteralNode) optimized).value()).isEqualTo(false);
        }

        @Test
        @DisplayName("Short circuit AND with true | 短路 AND 为 true")
        void testShortCircuitAndTrue() {
            Optimizer optimizer = new Optimizer();

            // true && x -> x
            Node node = BinaryOpNode.of(LiteralNode.of(true), "&&", IdentifierNode.of("x"));
            Node optimized = optimizer.optimizeShortCircuit(node);

            assertThat(optimized).isInstanceOf(IdentifierNode.class);
            assertThat(((IdentifierNode) optimized).name()).isEqualTo("x");
        }

        @Test
        @DisplayName("Short circuit OR with true | 短路 OR 为 true")
        void testShortCircuitOrTrue() {
            Optimizer optimizer = new Optimizer();

            // true || x
            Node node = BinaryOpNode.of(LiteralNode.of(true), "||", IdentifierNode.of("x"));
            Node optimized = optimizer.optimizeShortCircuit(node);

            assertThat(optimized).isInstanceOf(LiteralNode.class);
            assertThat(((LiteralNode) optimized).value()).isEqualTo(true);
        }

        @Test
        @DisplayName("Short circuit OR with false | 短路 OR 为 false")
        void testShortCircuitOrFalse() {
            Optimizer optimizer = new Optimizer();

            // false || x -> x
            Node node = BinaryOpNode.of(LiteralNode.of(false), "||", IdentifierNode.of("x"));
            Node optimized = optimizer.optimizeShortCircuit(node);

            assertThat(optimized).isInstanceOf(IdentifierNode.class);
            assertThat(((IdentifierNode) optimized).name()).isEqualTo("x");
        }
    }

    @Nested
    @DisplayName("Configuration Tests | 配置测试")
    class ConfigurationTests {

        @Test
        @DisplayName("Enable/disable constant folding | 启用/禁用常量折叠")
        void testConstantFoldingToggle() {
            Optimizer optimizer = new Optimizer();

            assertThat(optimizer.isConstantFoldingEnabled()).isTrue();

            optimizer.setConstantFoldingEnabled(false);
            assertThat(optimizer.isConstantFoldingEnabled()).isFalse();
        }

        @Test
        @DisplayName("Enable/disable short circuit | 启用/禁用短路")
        void testShortCircuitToggle() {
            Optimizer optimizer = new Optimizer();

            assertThat(optimizer.isShortCircuitEnabled()).isTrue();

            optimizer.setShortCircuitEnabled(false);
            assertThat(optimizer.isShortCircuitEnabled()).isFalse();
        }
    }

    @Nested
    @DisplayName("Builder Tests | 构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("Build with options | 使用选项构建")
        void testBuilderWithOptions() {
            Optimizer optimizer = Optimizer.builder()
                    .constantFolding(true)
                    .shortCircuit(false)
                    .deadCodeElimination(true)
                    .build();

            assertThat(optimizer.isConstantFoldingEnabled()).isTrue();
            assertThat(optimizer.isShortCircuitEnabled()).isFalse();
            assertThat(optimizer.isDeadCodeEliminationEnabled()).isTrue();
        }

        @Test
        @DisplayName("Build with no optimizations | 构建无优化")
        void testBuilderNoOptimizations() {
            Optimizer optimizer = Optimizer.builder()
                    .noOptimizations()
                    .build();

            assertThat(optimizer.isConstantFoldingEnabled()).isFalse();
            assertThat(optimizer.isShortCircuitEnabled()).isFalse();
            assertThat(optimizer.isDeadCodeEliminationEnabled()).isFalse();
        }
    }

    @Nested
    @DisplayName("Full Optimization Tests | 完整优化测试")
    class FullOptimizationTests {

        @Test
        @DisplayName("Optimize complex expression | 优化复杂表达式")
        void testOptimizeComplexExpression() {
            Optimizer optimizer = new Optimizer();

            // (2 + 3) * 4 + (false && x)
            // Should fold to 20 + false = 20
            Node left = BinaryOpNode.of(
                    BinaryOpNode.of(LiteralNode.of(2), "+", LiteralNode.of(3)),
                    "*",
                    LiteralNode.of(4)
            );
            Node right = BinaryOpNode.of(LiteralNode.of(false), "&&", IdentifierNode.of("x"));
            Node node = BinaryOpNode.of(left, "+", right);

            Node optimized = optimizer.optimize(node);

            // After optimization, left should be 20, right should be false
            assertThat(optimized).isInstanceOf(BinaryOpNode.class);
            BinaryOpNode binOp = (BinaryOpNode) optimized;
            assertThat(binOp.left()).isInstanceOf(LiteralNode.class);
            assertThat(((LiteralNode) binOp.left()).value()).isEqualTo(20L);
        }

        @Test
        @DisplayName("Optimize null returns null | 优化 null 返回 null")
        void testOptimizeNull() {
            Optimizer optimizer = new Optimizer();
            assertThat(optimizer.optimize(null)).isNull();
        }
    }
}
