package cloud.opencode.base.reflect.sealed;

import cloud.opencode.base.reflect.exception.OpenReflectException;
import org.junit.jupiter.api.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * SealedDispatcher Tests
 * SealedDispatcher 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.3
 */
@DisplayName("SealedDispatcher 测试")
class SealedDispatcherTest {

    // ==================== Test Sealed Hierarchies | 测试密封层次结构 ====================

    sealed interface Shape permits Circle, Rectangle, Triangle {}

    record Circle(double radius) implements Shape {}

    record Rectangle(double w, double h) implements Shape {}

    record Triangle(double a, double b, double c) implements Shape {}

    // Nested sealed hierarchy for multi-level tests
    sealed interface Expr permits Literal, BinaryOp {}

    record Literal(int value) implements Expr {}

    sealed interface BinaryOp extends Expr permits Add, Mul {}

    record Add(Expr left, Expr right) implements BinaryOp {}

    record Mul(Expr left, Expr right) implements BinaryOp {}

    // ==================== Builder Tests | 构建器测试 ====================

    @Nested
    @DisplayName("builder方法测试")
    class BuilderTests {

        @Test
        @DisplayName("非密封类抛出OpenReflectException")
        void testBuilderWithNonSealedType() {
            assertThatThrownBy(() -> SealedDispatcher.builder(String.class, String.class))
                    .isInstanceOf(OpenReflectException.class)
                    .hasMessageContaining("not sealed");
        }

        @Test
        @DisplayName("null密封类型抛出NullPointerException")
        void testBuilderWithNullSealedType() {
            assertThatThrownBy(() -> SealedDispatcher.builder(null, String.class))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null结果类型抛出NullPointerException")
        void testBuilderWithNullResultType() {
            assertThatThrownBy(() -> SealedDispatcher.builder(Shape.class, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null子类型抛出NullPointerException")
        void testOnWithNullSubtype() {
            var builder = SealedDispatcher.builder(Shape.class, String.class);
            assertThatThrownBy(() -> builder.on(null, s -> ""))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null处理器抛出NullPointerException")
        void testOnWithNullHandler() {
            var builder = SealedDispatcher.builder(Shape.class, String.class);
            assertThatThrownBy(() -> builder.on(Circle.class, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null默认处理器抛出NullPointerException")
        void testOrElseWithNull() {
            var builder = SealedDispatcher.builder(Shape.class, String.class);
            assertThatThrownBy(() -> builder.orElse(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== Exhaustiveness Tests | 穷举性测试 ====================

    @Nested
    @DisplayName("穷举性验证测试")
    class ExhaustivenessTests {

        @Test
        @DisplayName("缺少分支时构建失败")
        void testBuildFailsWhenMissingBranches() {
            assertThatThrownBy(() ->
                    SealedDispatcher.builder(Shape.class, String.class)
                            .on(Circle.class, c -> "circle")
                            .on(Rectangle.class, r -> "rectangle")
                            // missing Triangle
                            .build()
            )
                    .isInstanceOf(OpenReflectException.class)
                    .hasMessageContaining("Non-exhaustive")
                    .hasMessageContaining("Triangle");
        }

        @Test
        @DisplayName("所有分支覆盖时构建成功")
        void testBuildSucceedsWithAllBranches() {
            assertThatCode(() ->
                    SealedDispatcher.builder(Shape.class, String.class)
                            .on(Circle.class, c -> "circle")
                            .on(Rectangle.class, r -> "rectangle")
                            .on(Triangle.class, t -> "triangle")
                            .build()
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("有默认处理器时跳过穷举检查")
        void testBuildSucceedsWithOrElse() {
            assertThatCode(() ->
                    SealedDispatcher.builder(Shape.class, String.class)
                            .on(Circle.class, c -> "circle")
                            .orElse(s -> "default")
                            .build()
            ).doesNotThrowAnyException();
        }
    }

    // ==================== Dispatch Tests | 分发测试 ====================

    @Nested
    @DisplayName("dispatch方法测试")
    class DispatchTests {

        @Test
        @DisplayName("基本分发 - 所有分支覆盖")
        void testBasicDispatch() {
            var dispatcher = SealedDispatcher.builder(Shape.class, Double.class)
                    .on(Circle.class, c -> Math.PI * c.radius() * c.radius())
                    .on(Rectangle.class, r -> r.w() * r.h())
                    .on(Triangle.class, t -> {
                        double s = (t.a() + t.b() + t.c()) / 2;
                        return Math.sqrt(s * (s - t.a()) * (s - t.b()) * (s - t.c()));
                    })
                    .build();

            assertThat(dispatcher.dispatch(new Circle(1.0)))
                    .isCloseTo(Math.PI, within(0.0001));

            assertThat(dispatcher.dispatch(new Rectangle(3.0, 4.0)))
                    .isEqualTo(12.0);

            assertThat(dispatcher.dispatch(new Triangle(3.0, 4.0, 5.0)))
                    .isCloseTo(6.0, within(0.0001));
        }

        @Test
        @DisplayName("使用默认处理器分发")
        void testDispatchWithDefault() {
            var dispatcher = SealedDispatcher.builder(Shape.class, String.class)
                    .on(Circle.class, c -> "circle")
                    .orElse(s -> "other")
                    .build();

            assertThat(dispatcher.dispatch(new Circle(1.0))).isEqualTo("circle");
            assertThat(dispatcher.dispatch(new Rectangle(1.0, 2.0))).isEqualTo("other");
            assertThat(dispatcher.dispatch(new Triangle(1.0, 1.0, 1.0))).isEqualTo("other");
        }

        @Test
        @DisplayName("null输入抛出NullPointerException")
        void testDispatchNull() {
            var dispatcher = SealedDispatcher.builder(Shape.class, String.class)
                    .on(Circle.class, c -> "circle")
                    .on(Rectangle.class, r -> "rectangle")
                    .on(Triangle.class, t -> "triangle")
                    .build();

            assertThatThrownBy(() -> dispatcher.dispatch(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== DispatchSafe Tests | 安全分发测试 ====================

    @Nested
    @DisplayName("dispatchSafe方法测试")
    class DispatchSafeTests {

        @Test
        @DisplayName("匹配时返回Optional.of")
        void testDispatchSafeMatch() {
            var dispatcher = SealedDispatcher.builder(Shape.class, String.class)
                    .on(Circle.class, c -> "circle")
                    .on(Rectangle.class, r -> "rectangle")
                    .on(Triangle.class, t -> "triangle")
                    .build();

            Optional<String> result = dispatcher.dispatchSafe(new Circle(1.0));
            assertThat(result).isPresent().hasValue("circle");
        }

        @Test
        @DisplayName("不匹配时返回Optional.empty")
        void testDispatchSafeNoMatch() {
            var dispatcher = SealedDispatcher.builder(Shape.class, String.class)
                    .on(Circle.class, c -> "circle")
                    .orElse(s -> null)
                    .build();

            // orElse returns null -> Optional.empty()
            Optional<String> result = dispatcher.dispatchSafe(new Rectangle(1.0, 2.0));
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null输入抛出NullPointerException")
        void testDispatchSafeNull() {
            var dispatcher = SealedDispatcher.builder(Shape.class, String.class)
                    .on(Circle.class, c -> "circle")
                    .orElse(s -> "default")
                    .build();

            assertThatThrownBy(() -> dispatcher.dispatchSafe(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== Nested Hierarchy Tests | 嵌套层次结构测试 ====================

    @Nested
    @DisplayName("嵌套密封层次结构测试")
    class NestedHierarchyTests {

        @Test
        @DisplayName("多层密封层次结构 - 叶节点分发")
        void testNestedHierarchyLeafDispatch() {
            var dispatcher = SealedDispatcher.builder(Expr.class, Integer.class)
                    .on(Literal.class, l -> l.value())
                    .on(Add.class, a -> 0) // placeholder
                    .on(Mul.class, m -> 0) // placeholder
                    .build();

            assertThat(dispatcher.dispatch(new Literal(42))).isEqualTo(42);
            assertThat(dispatcher.dispatch(new Add(new Literal(1), new Literal(2)))).isEqualTo(0);
            assertThat(dispatcher.dispatch(new Mul(new Literal(3), new Literal(4)))).isEqualTo(0);
        }

        @Test
        @DisplayName("中间密封类型作为处理器")
        void testIntermediateSealedTypeHandler() {
            var dispatcher = SealedDispatcher.builder(Expr.class, String.class)
                    .on(Literal.class, l -> "literal")
                    .on(BinaryOp.class, b -> "binary")
                    .build();

            assertThat(dispatcher.dispatch(new Literal(1))).isEqualTo("literal");
            // Add and Mul should match via BinaryOp
            assertThat(dispatcher.dispatch(new Add(new Literal(1), new Literal(2)))).isEqualTo("binary");
            assertThat(dispatcher.dispatch(new Mul(new Literal(3), new Literal(4)))).isEqualTo("binary");
        }

        @Test
        @DisplayName("缺少嵌套叶分支时构建失败")
        void testNestedHierarchyMissingLeaf() {
            assertThatThrownBy(() ->
                    SealedDispatcher.builder(Expr.class, String.class)
                            .on(Literal.class, l -> "literal")
                            .on(Add.class, a -> "add")
                            // missing Mul
                            .build()
            )
                    .isInstanceOf(OpenReflectException.class)
                    .hasMessageContaining("Non-exhaustive")
                    .hasMessageContaining("Mul");
        }
    }
}
