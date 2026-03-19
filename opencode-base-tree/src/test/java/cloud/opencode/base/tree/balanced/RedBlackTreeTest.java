package cloud.opencode.base.tree.balanced;

import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * RedBlackTreeTest Tests
 * RedBlackTreeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("RedBlackTree Tests")
class RedBlackTreeTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("default constructor should create empty tree")
        void defaultConstructorShouldCreateEmptyTree() {
            RedBlackTree<Integer> tree = new RedBlackTree<>();

            assertThat(tree.isEmpty()).isTrue();
            assertThat(tree.size()).isZero();
        }

        @Test
        @DisplayName("constructor with comparator should use custom comparator")
        void constructorWithComparatorShouldUseCustomComparator() {
            RedBlackTree<Integer> tree = new RedBlackTree<>(Comparator.reverseOrder());
            tree.insert(1, 2, 3);

            assertThat(tree.max()).contains(1);
            assertThat(tree.min()).contains(3);
        }
    }

    @Nested
    @DisplayName("Insert Tests")
    class InsertTests {

        @Test
        @DisplayName("insert should add element to tree")
        void insertShouldAddElementToTree() {
            RedBlackTree<Integer> tree = new RedBlackTree<>();

            tree.insert(5);

            assertThat(tree.contains(5)).isTrue();
            assertThat(tree.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("insert varargs should add multiple elements")
        void insertVarargsShouldAddMultipleElements() {
            RedBlackTree<Integer> tree = new RedBlackTree<>();

            tree.insert(5, 3, 7, 1, 9);

            assertThat(tree.size()).isEqualTo(5);
        }

        @Test
        @DisplayName("insertAll should add collection elements")
        void insertAllShouldAddCollectionElements() {
            RedBlackTree<Integer> tree = new RedBlackTree<>();

            tree.insertAll(List.of(5, 3, 7));

            assertThat(tree.size()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Delete Tests")
    class DeleteTests {

        @Test
        @DisplayName("delete should remove element from tree")
        void deleteShouldRemoveElementFromTree() {
            RedBlackTree<Integer> tree = new RedBlackTree<>();
            tree.insert(5, 3, 7);

            boolean deleted = tree.delete(3);

            assertThat(deleted).isTrue();
            assertThat(tree.contains(3)).isFalse();
        }

        @Test
        @DisplayName("delete should return false for non-existent element")
        void deleteShouldReturnFalseForNonExistentElement() {
            RedBlackTree<Integer> tree = new RedBlackTree<>();
            tree.insert(5, 3, 7);

            boolean deleted = tree.delete(10);

            assertThat(deleted).isFalse();
        }

        @Test
        @DisplayName("delete root should work correctly")
        void deleteRootShouldWorkCorrectly() {
            RedBlackTree<Integer> tree = new RedBlackTree<>();
            tree.insert(5, 3, 7, 1, 9);

            boolean deleted = tree.delete(5);

            assertThat(deleted).isTrue();
            assertThat(tree.contains(5)).isFalse();
            assertThat(tree.size()).isEqualTo(4);
        }

        @Test
        @DisplayName("clear should remove all elements")
        void clearShouldRemoveAllElements() {
            RedBlackTree<Integer> tree = new RedBlackTree<>();
            tree.insert(5, 3, 7, 9);

            tree.clear();

            assertThat(tree.isEmpty()).isTrue();
            assertThat(tree.size()).isZero();
        }
    }

    @Nested
    @DisplayName("Traversal Tests")
    class TraversalTests {

        @Test
        @DisplayName("inOrderTraversal should visit nodes in sorted order")
        void inOrderTraversalShouldVisitNodesInSortedOrder() {
            RedBlackTree<Integer> tree = new RedBlackTree<>();
            tree.insert(5, 3, 7, 1, 9);

            List<Integer> visited = new ArrayList<>();
            tree.inOrderTraversal(visited::add);

            assertThat(visited).containsExactly(1, 3, 5, 7, 9);
        }

        @Test
        @DisplayName("toSortedList should return sorted elements")
        void toSortedListShouldReturnSortedElements() {
            RedBlackTree<Integer> tree = new RedBlackTree<>();
            tree.insert(5, 3, 7, 1, 9);

            List<Integer> sorted = tree.toSortedList();

            assertThat(sorted).containsExactly(1, 3, 5, 7, 9);
        }
    }

    @Nested
    @DisplayName("Balance Tests")
    class BalanceTests {

        @Test
        @DisplayName("tree height should be reasonable after insertions")
        void treeHeightShouldBeReasonableAfterInsertions() {
            RedBlackTree<Integer> tree = new RedBlackTree<>();

            for (int i = 1; i <= 100; i++) {
                tree.insert(i);
            }

            // For a balanced tree with 100 elements, height should be around log2(100) ≈ 7
            // Red-Black trees guarantee at most 2*log2(n+1) height
            assertThat(tree.height()).isLessThanOrEqualTo(15);
        }

        @Test
        @DisplayName("blackHeight should return consistent value")
        void blackHeightShouldReturnConsistentValue() {
            RedBlackTree<Integer> tree = new RedBlackTree<>();
            tree.insert(5, 3, 7, 1, 9, 4, 6, 8, 10);

            int blackHeight = tree.blackHeight();

            assertThat(blackHeight).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Range Query Tests")
    class RangeQueryTests {

        @Test
        @DisplayName("range should return elements in range")
        void rangeShouldReturnElementsInRange() {
            RedBlackTree<Integer> tree = new RedBlackTree<>();
            tree.insert(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

            List<Integer> range = tree.range(3, 7);

            assertThat(range).containsExactly(3, 4, 5, 6, 7);
        }
    }
}
