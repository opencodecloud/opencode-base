package cloud.opencode.base.tree.validation;

import cloud.opencode.base.tree.DefaultTreeNode;
import cloud.opencode.base.tree.exception.CycleDetectedException;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * CycleDetectorTest Tests
 * CycleDetectorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("CycleDetector Tests")
class CycleDetectorTest {

    @Nested
    @DisplayName("HasCycle Tests")
    class HasCycleTests {

        @Test
        @DisplayName("hasCycle should return false for valid tree")
        void hasCycleShouldReturnFalseForValidTree() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, 0L, "Root");
            DefaultTreeNode<Long> child1 = new DefaultTreeNode<>(2L, 1L, "Child1");
            DefaultTreeNode<Long> child2 = new DefaultTreeNode<>(3L, 1L, "Child2");
            root.setChildren(new ArrayList<>(List.of(child1, child2)));

            boolean hasCycle = CycleDetector.hasCycle(List.of(root));

            assertThat(hasCycle).isFalse();
        }

        @Test
        @DisplayName("hasCycle should return false for empty list")
        void hasCycleShouldReturnFalseForEmptyList() {
            List<DefaultTreeNode<Long>> emptyList = List.of();
            boolean hasCycle = CycleDetector.hasCycle(emptyList);

            assertThat(hasCycle).isFalse();
        }

        @Test
        @DisplayName("hasCycle should return false for single node")
        void hasCycleShouldReturnFalseForSingleNode() {
            DefaultTreeNode<Long> node = new DefaultTreeNode<>(1L, 0L, "Single");

            boolean hasCycle = CycleDetector.hasCycle(List.of(node));

            assertThat(hasCycle).isFalse();
        }
    }

    @Nested
    @DisplayName("FindCyclePath Tests")
    class FindCyclePathTests {

        @Test
        @DisplayName("findCyclePath should return empty for valid tree")
        void findCyclePathShouldReturnEmptyForValidTree() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, 0L, "Root");
            DefaultTreeNode<Long> child = new DefaultTreeNode<>(2L, 1L, "Child");
            root.setChildren(new ArrayList<>(List.of(child)));

            Optional<List<Long>> cyclePath = CycleDetector.findCyclePath(List.of(root));

            assertThat(cyclePath).isEmpty();
        }

        @Test
        @DisplayName("findCyclePath should return empty for empty tree")
        void findCyclePathShouldReturnEmptyForEmptyTree() {
            List<DefaultTreeNode<Long>> emptyList = List.of();
            Optional<List<Long>> cyclePath = CycleDetector.findCyclePath(emptyList);

            assertThat(cyclePath).isEmpty();
        }
    }

    @Nested
    @DisplayName("CheckNoCycle Tests")
    class CheckNoCycleTests {

        @Test
        @DisplayName("checkNoCycle should not throw for valid tree")
        void checkNoCycleShouldNotThrowForValidTree() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, 0L, "Root");
            DefaultTreeNode<Long> child = new DefaultTreeNode<>(2L, 1L, "Child");
            root.setChildren(new ArrayList<>(List.of(child)));

            assertThatCode(() -> CycleDetector.checkNoCycle(List.of(root)))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("checkNoCycle should not throw for empty tree")
        void checkNoCycleShouldNotThrowForEmptyTree() {
            List<DefaultTreeNode<Long>> emptyList = List.of();

            assertThatCode(() -> CycleDetector.checkNoCycle(emptyList))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("HasPotentialCycle Tests")
    class HasPotentialCycleTests {

        @Test
        @DisplayName("hasPotentialCycle should return false for valid flat list")
        void hasPotentialCycleShouldReturnFalseForValidFlatList() {
            List<DefaultTreeNode<Long>> nodes = List.of(
                new DefaultTreeNode<>(1L, 0L, "Root"),
                new DefaultTreeNode<>(2L, 1L, "Child"),
                new DefaultTreeNode<>(3L, 2L, "Grandchild")
            );

            boolean hasPotentialCycle = CycleDetector.hasPotentialCycle(nodes);

            assertThat(hasPotentialCycle).isFalse();
        }

        @Test
        @DisplayName("hasPotentialCycle should return true for self-referencing node")
        void hasPotentialCycleShouldReturnTrueForSelfReferencingNode() {
            List<DefaultTreeNode<Long>> nodes = List.of(
                new DefaultTreeNode<>(1L, 1L, "SelfRef")
            );

            boolean hasPotentialCycle = CycleDetector.hasPotentialCycle(nodes);

            assertThat(hasPotentialCycle).isTrue();
        }

        @Test
        @DisplayName("hasPotentialCycle should return true for circular reference")
        void hasPotentialCycleShouldReturnTrueForCircularReference() {
            List<DefaultTreeNode<Long>> nodes = List.of(
                new DefaultTreeNode<>(1L, 3L, "Node1"),
                new DefaultTreeNode<>(2L, 1L, "Node2"),
                new DefaultTreeNode<>(3L, 2L, "Node3")
            );

            boolean hasPotentialCycle = CycleDetector.hasPotentialCycle(nodes);

            assertThat(hasPotentialCycle).isTrue();
        }

        @Test
        @DisplayName("hasPotentialCycle should return false for empty list")
        void hasPotentialCycleShouldReturnFalseForEmptyList() {
            List<DefaultTreeNode<Long>> emptyList = List.of();
            boolean hasPotentialCycle = CycleDetector.hasPotentialCycle(emptyList);

            assertThat(hasPotentialCycle).isFalse();
        }
    }
}
