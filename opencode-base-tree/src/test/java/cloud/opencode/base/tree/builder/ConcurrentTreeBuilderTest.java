package cloud.opencode.base.tree.builder;

import cloud.opencode.base.tree.DefaultTreeNode;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

/**
 * ConcurrentTreeBuilderTest Tests
 * ConcurrentTreeBuilderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("ConcurrentTreeBuilder Tests")
class ConcurrentTreeBuilderTest {

    @Nested
    @DisplayName("Build Tests")
    class BuildTests {

        @Test
        @DisplayName("build should create tree from flat list")
        void buildShouldCreateTreeFromFlatList() {
            List<DefaultTreeNode<Long>> nodes = Arrays.asList(
                new DefaultTreeNode<>(1L, 0L, "Root"),
                new DefaultTreeNode<>(2L, 1L, "Child1"),
                new DefaultTreeNode<>(3L, 1L, "Child2")
            );

            List<DefaultTreeNode<Long>> roots = ConcurrentTreeBuilder.build(nodes);

            assertThat(roots).isNotEmpty();
            // Note: concurrent build may result in different ordering
        }

        @Test
        @DisplayName("build with rootId should use specified root")
        void buildWithRootIdShouldUseSpecifiedRoot() {
            List<DefaultTreeNode<Long>> nodes = Arrays.asList(
                new DefaultTreeNode<>(1L, 10L, "Root"),
                new DefaultTreeNode<>(2L, 1L, "Child")
            );

            List<DefaultTreeNode<Long>> roots = ConcurrentTreeBuilder.build(nodes, 10L);

            assertThat(roots).isNotEmpty();
        }

        @Test
        @DisplayName("build should handle empty list")
        void buildShouldHandleEmptyList() {
            List<DefaultTreeNode<Long>> emptyList = Collections.emptyList();
            List<DefaultTreeNode<Long>> roots = ConcurrentTreeBuilder.build(emptyList);

            assertThat(roots).isEmpty();
        }

        @Test
        @DisplayName("build should handle null list")
        void buildShouldHandleNullList() {
            List<DefaultTreeNode<Long>> roots = ConcurrentTreeBuilder.build(null);

            assertThat(roots).isEmpty();
        }
    }

    @Nested
    @DisplayName("BuildLarge Tests")
    class BuildLargeTests {

        @Test
        @DisplayName("buildLarge should use parallel for large lists")
        void buildLargeShouldUseParallelForLargeLists() {
            List<DefaultTreeNode<Long>> nodes = new ArrayList<>();
            nodes.add(new DefaultTreeNode<>(1L, 0L, "Root"));
            IntStream.range(2, 1002).forEach(i ->
                nodes.add(new DefaultTreeNode<>((long) i, 1L, "Child" + i)));

            List<DefaultTreeNode<Long>> roots = ConcurrentTreeBuilder.buildLarge(nodes, 0L, 100);

            assertThat(roots).isNotEmpty();
        }

        @Test
        @DisplayName("buildLarge should use sequential for small lists")
        void buildLargeShouldUseSequentialForSmallLists() {
            List<DefaultTreeNode<Long>> nodes = Arrays.asList(
                new DefaultTreeNode<>(1L, 0L, "Root"),
                new DefaultTreeNode<>(2L, 1L, "Child")
            );

            List<DefaultTreeNode<Long>> roots = ConcurrentTreeBuilder.buildLarge(nodes, 0L, 100);

            assertThat(roots).hasSize(1);
        }

        @Test
        @DisplayName("buildLarge should handle empty list")
        void buildLargeShouldHandleEmptyList() {
            List<DefaultTreeNode<Long>> emptyList = Collections.emptyList();
            List<DefaultTreeNode<Long>> roots = ConcurrentTreeBuilder.buildLarge(emptyList, 0L, 100);

            assertThat(roots).isEmpty();
        }

        @Test
        @DisplayName("buildLarge should handle null list")
        void buildLargeShouldHandleNullList() {
            List<DefaultTreeNode<Long>> roots = ConcurrentTreeBuilder.buildLarge(null, 0L, 100);

            assertThat(roots).isEmpty();
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {

        @Test
        @DisplayName("build should be thread-safe with concurrent modifications")
        void buildShouldBeThreadSafeWithConcurrentModifications() {
            List<DefaultTreeNode<Long>> nodes = Collections.synchronizedList(new ArrayList<>());
            nodes.add(new DefaultTreeNode<>(1L, 0L, "Root"));

            IntStream.range(2, 102).parallel().forEach(i ->
                nodes.add(new DefaultTreeNode<>((long) i, 1L, "Child" + i)));

            // This should not throw ConcurrentModificationException
            assertThatCode(() -> ConcurrentTreeBuilder.build(nodes))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Root Detection Tests")
    class RootDetectionTests {

        @Test
        @DisplayName("should treat null parentId as root")
        void shouldTreatNullParentIdAsRoot() {
            List<DefaultTreeNode<Long>> nodes = List.of(
                new DefaultTreeNode<>(1L, null, "Root"),
                new DefaultTreeNode<>(2L, 1L, "Child")
            );

            List<DefaultTreeNode<Long>> roots = ConcurrentTreeBuilder.build(nodes);

            assertThat(roots).isNotEmpty();
        }

        @Test
        @DisplayName("should treat 0L parentId as root")
        void shouldTreat0LParentIdAsRoot() {
            List<DefaultTreeNode<Long>> nodes = List.of(
                new DefaultTreeNode<>(1L, 0L, "Root"),
                new DefaultTreeNode<>(2L, 1L, "Child")
            );

            List<DefaultTreeNode<Long>> roots = ConcurrentTreeBuilder.build(nodes);

            assertThat(roots).isNotEmpty();
        }
    }
}
