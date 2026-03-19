package cloud.opencode.base.tree.virtual;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * LazyChildLoaderTest Tests
 * LazyChildLoaderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("LazyChildLoader Tests")
class LazyChildLoaderTest {

    @Nested
    @DisplayName("Functional Interface Tests")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("should implement functional interface with lambda")
        void shouldImplementFunctionalInterfaceWithLambda() {
            LazyChildLoader<String, Long> loader = parentId ->
                List.of("child1", "child2");

            List<String> children = loader.loadChildren(1L);

            assertThat(children).containsExactly("child1", "child2");
        }

        @Test
        @DisplayName("should implement with method reference")
        void shouldImplementWithMethodReference() {
            LazyChildLoader<String, Long> loader = this::loadChildrenForParent;

            List<String> children = loader.loadChildren(1L);

            assertThat(children).containsExactly("child-1");
        }

        private List<String> loadChildrenForParent(Long parentId) {
            return List.of("child-" + parentId);
        }
    }

    @Nested
    @DisplayName("Loading Behavior Tests")
    class LoadingBehaviorTests {

        @Test
        @DisplayName("loadChildren should be called with correct parentId")
        void loadChildrenShouldBeCalledWithCorrectParentId() {
            AtomicInteger callCount = new AtomicInteger(0);
            Long[] capturedParentId = new Long[1];

            LazyChildLoader<String, Long> loader = parentId -> {
                callCount.incrementAndGet();
                capturedParentId[0] = parentId;
                return List.of();
            };

            loader.loadChildren(42L);

            assertThat(callCount.get()).isEqualTo(1);
            assertThat(capturedParentId[0]).isEqualTo(42L);
        }

        @Test
        @DisplayName("loadChildren should support null parentId")
        void loadChildrenShouldSupportNullParentId() {
            LazyChildLoader<String, Long> loader = parentId ->
                parentId == null ? List.of("root-child") : List.of("child");

            List<String> rootChildren = loader.loadChildren(null);

            assertThat(rootChildren).containsExactly("root-child");
        }

        @Test
        @DisplayName("loadChildren should return empty list when no children")
        void loadChildrenShouldReturnEmptyListWhenNoChildren() {
            LazyChildLoader<String, Long> loader = parentId -> List.of();

            List<String> children = loader.loadChildren(1L);

            assertThat(children).isEmpty();
        }
    }

    @Nested
    @DisplayName("Type Parameter Tests")
    class TypeParameterTests {

        @Test
        @DisplayName("should work with String ID type")
        void shouldWorkWithStringIdType() {
            LazyChildLoader<String, String> loader = parentId ->
                List.of("child-of-" + parentId);

            List<String> children = loader.loadChildren("parent-1");

            assertThat(children).containsExactly("child-of-parent-1");
        }

        @Test
        @DisplayName("should work with complex node type")
        void shouldWorkWithComplexNodeType() {
            record TreeItem(Long id, Long parentId, String name) {}

            LazyChildLoader<TreeItem, Long> loader = parentId ->
                List.of(
                    new TreeItem(parentId * 10 + 1, parentId, "Child1"),
                    new TreeItem(parentId * 10 + 2, parentId, "Child2")
                );

            List<TreeItem> children = loader.loadChildren(1L);

            assertThat(children).hasSize(2);
            assertThat(children.get(0).id()).isEqualTo(11L);
            assertThat(children.get(1).id()).isEqualTo(12L);
        }
    }
}
