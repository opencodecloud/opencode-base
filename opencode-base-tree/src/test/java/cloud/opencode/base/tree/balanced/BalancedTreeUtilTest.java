package cloud.opencode.base.tree.balanced;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * BalancedTreeUtilTest Tests
 * BalancedTreeUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("BalancedTreeUtil Tests")
class BalancedTreeUtilTest {

    @Nested
    @DisplayName("AVL Factory Tests")
    class AvlFactoryTests {

        @Test
        @DisplayName("avlTreeOf should create tree with elements")
        void avlTreeOfShouldCreateTreeWithElements() {
            AvlTree<Integer> tree = BalancedTreeUtil.avlTreeOf(3, 1, 2);

            assertThat(tree.size()).isEqualTo(3);
            assertThat(tree.contains(1)).isTrue();
            assertThat(tree.contains(2)).isTrue();
            assertThat(tree.contains(3)).isTrue();
        }

        @Test
        @DisplayName("avlTreeFrom should create tree from collection")
        void avlTreeFromShouldCreateTreeFromCollection() {
            List<Integer> elements = List.of(1, 2, 3);

            AvlTree<Integer> tree = BalancedTreeUtil.avlTreeFrom(elements);

            assertThat(tree.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("avlTreeOf with comparator should use custom comparator")
        void avlTreeOfWithComparatorShouldUseCustomComparator() {
            AvlTree<String> tree = BalancedTreeUtil.avlTreeOf(
                Comparator.reverseOrder(), "a", "b", "c");

            assertThat(tree.size()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("RedBlack Factory Tests")
    class RedBlackFactoryTests {

        @Test
        @DisplayName("redBlackTreeOf should create tree with elements")
        void redBlackTreeOfShouldCreateTreeWithElements() {
            RedBlackTree<Integer> tree = BalancedTreeUtil.redBlackTreeOf(3, 1, 2);

            assertThat(tree.size()).isEqualTo(3);
            assertThat(tree.contains(1)).isTrue();
        }

        @Test
        @DisplayName("redBlackTreeFrom should create tree from collection")
        void redBlackTreeFromShouldCreateTreeFromCollection() {
            List<Integer> elements = List.of(1, 2, 3);

            RedBlackTree<Integer> tree = BalancedTreeUtil.redBlackTreeFrom(elements);

            assertThat(tree.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("redBlackTreeOf with comparator should use custom comparator")
        void redBlackTreeOfWithComparatorShouldUseCustomComparator() {
            RedBlackTree<String> tree = BalancedTreeUtil.redBlackTreeOf(
                Comparator.reverseOrder(), "a", "b", "c");

            assertThat(tree.size()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("FromSorted Tests")
    class FromSortedTests {

        @Test
        @DisplayName("fromSortedArray should create balanced tree")
        void fromSortedArrayShouldCreateBalancedTree() {
            Integer[] sorted = {1, 2, 3, 4, 5, 6, 7};

            AvlTree<Integer> tree = BalancedTreeUtil.fromSortedArray(sorted);

            assertThat(tree.size()).isEqualTo(7);
            assertThat(tree.isBalanced()).isTrue();
        }

        @Test
        @DisplayName("fromSortedList should create balanced tree")
        void fromSortedListShouldCreateBalancedTree() {
            List<Integer> sorted = List.of(1, 2, 3, 4, 5);

            AvlTree<Integer> tree = BalancedTreeUtil.fromSortedList(sorted);

            assertThat(tree.size()).isEqualTo(5);
            assertThat(tree.isBalanced()).isTrue();
        }

        @Test
        @DisplayName("fromSortedList should handle empty list")
        void fromSortedListShouldHandleEmptyList() {
            List<Integer> emptyList = List.of();

            AvlTree<Integer> tree = BalancedTreeUtil.fromSortedList(emptyList);

            assertThat(tree.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("Merge Tests")
    class MergeTests {

        @Test
        @DisplayName("merge avl trees should combine elements")
        void mergeAvlTreesShouldCombineElements() {
            AvlTree<Integer> tree1 = BalancedTreeUtil.avlTreeOf(1, 2, 3);
            AvlTree<Integer> tree2 = BalancedTreeUtil.avlTreeOf(4, 5, 6);

            AvlTree<Integer> merged = BalancedTreeUtil.merge(tree1, tree2);

            assertThat(merged.size()).isEqualTo(6);
        }

        @Test
        @DisplayName("merge redblack trees should combine elements")
        void mergeRedblackTreesShouldCombineElements() {
            RedBlackTree<Integer> tree1 = BalancedTreeUtil.redBlackTreeOf(1, 2, 3);
            RedBlackTree<Integer> tree2 = BalancedTreeUtil.redBlackTreeOf(4, 5, 6);

            RedBlackTree<Integer> merged = BalancedTreeUtil.merge(tree1, tree2);

            assertThat(merged.size()).isEqualTo(6);
        }
    }

    @Nested
    @DisplayName("Order Statistics Tests")
    class OrderStatisticsTests {

        @Test
        @DisplayName("kthSmallest should return correct element")
        void kthSmallestShouldReturnCorrectElement() {
            AvlTree<Integer> tree = BalancedTreeUtil.avlTreeOf(5, 3, 7, 1, 9);

            Optional<Integer> result = BalancedTreeUtil.kthSmallest(tree, 3);

            assertThat(result).contains(5);
        }

        @Test
        @DisplayName("kthSmallest should return empty for invalid k")
        void kthSmallestShouldReturnEmptyForInvalidK() {
            AvlTree<Integer> tree = BalancedTreeUtil.avlTreeOf(1, 2, 3);

            Optional<Integer> result = BalancedTreeUtil.kthSmallest(tree, 10);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("kthLargest should return correct element")
        void kthLargestShouldReturnCorrectElement() {
            AvlTree<Integer> tree = BalancedTreeUtil.avlTreeOf(1, 2, 3, 4, 5);

            Optional<Integer> result = BalancedTreeUtil.kthLargest(tree, 2);

            assertThat(result).contains(4);
        }

        @Test
        @DisplayName("median should return middle element")
        void medianShouldReturnMiddleElement() {
            AvlTree<Integer> tree = BalancedTreeUtil.avlTreeOf(1, 2, 3, 4, 5);

            Optional<Integer> result = BalancedTreeUtil.median(tree);

            assertThat(result).contains(3);
        }

        @Test
        @DisplayName("median should return empty for empty tree")
        void medianShouldReturnEmptyForEmptyTree() {
            AvlTree<Integer> tree = new AvlTree<>();

            Optional<Integer> result = BalancedTreeUtil.median(tree);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Floor/Ceiling Tests")
    class FloorCeilingTests {

        @Test
        @DisplayName("floor should return largest element less than or equal")
        void floorShouldReturnLargestElementLessThanOrEqual() {
            AvlTree<Integer> tree = BalancedTreeUtil.avlTreeOf(1, 3, 5, 7, 9);

            Optional<Integer> result = BalancedTreeUtil.floor(tree, 6);

            assertThat(result).contains(5);
        }

        @Test
        @DisplayName("ceiling should return smallest element greater than or equal")
        void ceilingShouldReturnSmallestElementGreaterThanOrEqual() {
            AvlTree<Integer> tree = BalancedTreeUtil.avlTreeOf(1, 3, 5, 7, 9);

            Optional<Integer> result = BalancedTreeUtil.ceiling(tree, 4);

            assertThat(result).contains(5);
        }
    }

    @Nested
    @DisplayName("Set Operations Tests")
    class SetOperationsTests {

        @Test
        @DisplayName("intersection should return common elements")
        void intersectionShouldReturnCommonElements() {
            AvlTree<Integer> tree1 = BalancedTreeUtil.avlTreeOf(1, 2, 3, 4);
            AvlTree<Integer> tree2 = BalancedTreeUtil.avlTreeOf(3, 4, 5, 6);

            AvlTree<Integer> result = BalancedTreeUtil.intersection(tree1, tree2);

            assertThat(result.size()).isEqualTo(2);
            assertThat(result.contains(3)).isTrue();
            assertThat(result.contains(4)).isTrue();
        }

        @Test
        @DisplayName("union should return all elements")
        void unionShouldReturnAllElements() {
            AvlTree<Integer> tree1 = BalancedTreeUtil.avlTreeOf(1, 2);
            AvlTree<Integer> tree2 = BalancedTreeUtil.avlTreeOf(3, 4);

            AvlTree<Integer> result = BalancedTreeUtil.union(tree1, tree2);

            assertThat(result.size()).isEqualTo(4);
        }

        @Test
        @DisplayName("difference should return elements in first but not second")
        void differenceShouldReturnElementsInFirstButNotSecond() {
            AvlTree<Integer> tree1 = BalancedTreeUtil.avlTreeOf(1, 2, 3, 4);
            AvlTree<Integer> tree2 = BalancedTreeUtil.avlTreeOf(3, 4, 5);

            AvlTree<Integer> result = BalancedTreeUtil.difference(tree1, tree2);

            assertThat(result.size()).isEqualTo(2);
            assertThat(result.contains(1)).isTrue();
            assertThat(result.contains(2)).isTrue();
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("isValidBst should return true for valid BST")
        void isValidBstShouldReturnTrueForValidBst() {
            AvlTree<Integer> tree = BalancedTreeUtil.avlTreeOf(3, 1, 5, 2, 4);

            boolean valid = BalancedTreeUtil.isValidBst(tree);

            assertThat(valid).isTrue();
        }
    }

    @Nested
    @DisplayName("Stats Tests")
    class StatsTests {

        @Test
        @DisplayName("stats should return tree statistics for AVL")
        void statsShouldReturnTreeStatisticsForAvl() {
            AvlTree<Integer> tree = BalancedTreeUtil.avlTreeOf(1, 2, 3, 4, 5);

            BalancedTreeUtil.TreeStats stats = BalancedTreeUtil.stats(tree);

            assertThat(stats.size()).isEqualTo(5);
            assertThat(stats.balanced()).isTrue();
        }

        @Test
        @DisplayName("stats should return tree statistics for RedBlack")
        void statsShouldReturnTreeStatisticsForRedBlack() {
            RedBlackTree<Integer> tree = BalancedTreeUtil.redBlackTreeOf(1, 2, 3, 4, 5);

            BalancedTreeUtil.TreeStats stats = BalancedTreeUtil.stats(tree);

            assertThat(stats.size()).isEqualTo(5);
            assertThat(stats.balanced()).isTrue();
        }

        @Test
        @DisplayName("TreeStats should compute balance efficiency")
        void treeStatsShouldComputeBalanceEfficiency() {
            BalancedTreeUtil.TreeStats stats = new BalancedTreeUtil.TreeStats(7, 3, true);

            assertThat(stats.minPossibleHeight()).isEqualTo(3);
            assertThat(stats.balanceEfficiency()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("TreeStats with zero height should return 1.0 efficiency")
        void treeStatsWithZeroHeightShouldReturn1Efficiency() {
            BalancedTreeUtil.TreeStats stats = new BalancedTreeUtil.TreeStats(0, 0, true);

            assertThat(stats.balanceEfficiency()).isEqualTo(1.0);
        }
    }
}
