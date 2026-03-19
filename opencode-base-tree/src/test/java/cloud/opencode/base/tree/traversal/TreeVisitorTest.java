package cloud.opencode.base.tree.traversal;

import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * TreeVisitorTest Tests
 * TreeVisitorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("TreeVisitor Tests")
class TreeVisitorTest {

    @Nested
    @DisplayName("Functional Interface Tests")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("should be usable as lambda")
        void shouldBeUsableAsLambda() {
            TreeVisitor<String> visitor = (node, depth) -> true;
            assertThat(visitor.visit("test", 0)).isTrue();
        }

        @Test
        @DisplayName("should support returning false to stop traversal")
        void shouldSupportReturningFalse() {
            TreeVisitor<String> visitor = (node, depth) -> false;
            assertThat(visitor.visit("test", 0)).isFalse();
        }
    }

    @Nested
    @DisplayName("Of Factory Method Tests")
    class OfFactoryMethodTests {

        @Test
        @DisplayName("should create visitor from consumer")
        void shouldCreateVisitorFromConsumer() {
            List<String> visited = new ArrayList<>();
            TreeVisitor<String> visitor = TreeVisitor.of(visited::add);

            boolean result = visitor.visit("hello", 0);

            assertThat(result).isTrue();
            assertThat(visited).containsExactly("hello");
        }

        @Test
        @DisplayName("should always return true (continue)")
        void shouldAlwaysReturnTrue() {
            TreeVisitor<String> visitor = TreeVisitor.of(node -> {});

            assertThat(visitor.visit("a", 0)).isTrue();
            assertThat(visitor.visit("b", 5)).isTrue();
        }
    }

    @Nested
    @DisplayName("WithDepth Factory Method Tests")
    class WithDepthFactoryMethodTests {

        @Test
        @DisplayName("should create visitor that receives depth")
        void shouldCreateVisitorWithDepth() {
            List<String> results = new ArrayList<>();
            TreeVisitor<String> visitor = TreeVisitor.withDepth((node, depth) ->
                results.add(node + ":" + depth));

            visitor.visit("root", 0);
            visitor.visit("child", 1);

            assertThat(results).containsExactly("root:0", "child:1");
        }

        @Test
        @DisplayName("should always return true (continue)")
        void shouldAlwaysReturnTrue() {
            TreeVisitor<String> visitor = TreeVisitor.withDepth((node, depth) -> {});

            assertThat(visitor.visit("a", 0)).isTrue();
            assertThat(visitor.visit("b", 10)).isTrue();
        }
    }
}
