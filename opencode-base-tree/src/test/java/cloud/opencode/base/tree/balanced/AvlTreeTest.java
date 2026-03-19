package cloud.opencode.base.tree.balanced;

import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * AvlTreeTest Tests
 * AvlTreeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("AvlTree Tests")
class AvlTreeTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("default constructor should create empty tree")
        void defaultConstructorShouldCreateEmptyTree() {
            AvlTree<Integer> tree = new AvlTree<>();

            assertThat(tree.isEmpty()).isTrue();
            assertThat(tree.size()).isZero();
        }

        @Test
        @DisplayName("constructor with comparator should use custom comparator")
        void constructorWithComparatorShouldUseCustomComparator() {
            AvlTree<Integer> tree = new AvlTree<>(Comparator.reverseOrder());
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
            AvlTree<Integer> tree = new AvlTree<>();

            tree.insert(5);

            assertThat(tree.contains(5)).isTrue();
            assertThat(tree.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("insert varargs should add multiple elements")
        void insertVarargsShouldAddMultipleElements() {
            AvlTree<Integer> tree = new AvlTree<>();

            tree.insert(5, 3, 7, 1, 9);

            assertThat(tree.size()).isEqualTo(5);
            assertThat(tree.contains(5)).isTrue();
            assertThat(tree.contains(1)).isTrue();
            assertThat(tree.contains(9)).isTrue();
        }

        @Test
        @DisplayName("insertAll should add collection elements")
        void insertAllShouldAddCollectionElements() {
            AvlTree<Integer> tree = new AvlTree<>();

            tree.insertAll(List.of(5, 3, 7));

            assertThat(tree.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("insert should ignore duplicates")
        void insertShouldIgnoreDuplicates() {
            AvlTree<Integer> tree = new AvlTree<>();

            tree.insert(5, 5, 5);

            assertThat(tree.size()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Delete Tests")
    class DeleteTests {

        @Test
        @DisplayName("delete should remove element from tree")
        void deleteShouldRemoveElementFromTree() {
            AvlTree<Integer> tree = new AvlTree<>();
            tree.insert(5, 3, 7);

            boolean deleted = tree.delete(3);

            assertThat(deleted).isTrue();
            assertThat(tree.contains(3)).isFalse();
            assertThat(tree.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("delete should return false for non-existent element")
        void deleteShouldReturnFalseForNonExistentElement() {
            AvlTree<Integer> tree = new AvlTree<>();
            tree.insert(5, 3, 7);

            boolean deleted = tree.delete(10);

            assertThat(deleted).isFalse();
            assertThat(tree.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("deleteMin should remove minimum element")
        void deleteMinShouldRemoveMinimumElement() {
            AvlTree<Integer> tree = new AvlTree<>();
            tree.insert(5, 3, 7, 1);

            tree.deleteMin();

            assertThat(tree.contains(1)).isFalse();
            assertThat(tree.min()).contains(3);
        }

        @Test
        @DisplayName("deleteMax should remove maximum element")
        void deleteMaxShouldRemoveMaximumElement() {
            AvlTree<Integer> tree = new AvlTree<>();
            tree.insert(5, 3, 7, 9);

            tree.deleteMax();

            assertThat(tree.contains(9)).isFalse();
            assertThat(tree.max()).contains(7);
        }
    }

    @Nested
    @DisplayName("Search Tests")
    class SearchTests {

        @Test
        @DisplayName("contains should return true for existing element")
        void containsShouldReturnTrueForExistingElement() {
            AvlTree<Integer> tree = new AvlTree<>();
            tree.insert(5, 3, 7);

            assertThat(tree.contains(5)).isTrue();
            assertThat(tree.contains(3)).isTrue();
        }

        @Test
        @DisplayName("contains should return false for non-existing element")
        void containsShouldReturnFalseForNonExistingElement() {
            AvlTree<Integer> tree = new AvlTree<>();
            tree.insert(5, 3, 7);

            assertThat(tree.contains(10)).isFalse();
        }

        @Test
        @DisplayName("search should return element if found")
        void searchShouldReturnElementIfFound() {
            AvlTree<Integer> tree = new AvlTree<>();
            tree.insert(5, 3, 7);

            assertThat(tree.search(5)).contains(5);
        }

        @Test
        @DisplayName("search should return empty if not found")
        void searchShouldReturnEmptyIfNotFound() {
            AvlTree<Integer> tree = new AvlTree<>();
            tree.insert(5, 3, 7);

            assertThat(tree.search(10)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Min/Max Tests")
    class MinMaxTests {

        @Test
        @DisplayName("min should return minimum element")
        void minShouldReturnMinimumElement() {
            AvlTree<Integer> tree = new AvlTree<>();
            tree.insert(5, 3, 7, 1, 9);

            assertThat(tree.min()).contains(1);
        }

        @Test
        @DisplayName("min should return empty for empty tree")
        void minShouldReturnEmptyForEmptyTree() {
            AvlTree<Integer> tree = new AvlTree<>();

            assertThat(tree.min()).isEmpty();
        }

        @Test
        @DisplayName("max should return maximum element")
        void maxShouldReturnMaximumElement() {
            AvlTree<Integer> tree = new AvlTree<>();
            tree.insert(5, 3, 7, 1, 9);

            assertThat(tree.max()).contains(9);
        }

        @Test
        @DisplayName("max should return empty for empty tree")
        void maxShouldReturnEmptyForEmptyTree() {
            AvlTree<Integer> tree = new AvlTree<>();

            assertThat(tree.max()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Traversal Tests")
    class TraversalTests {

        @Test
        @DisplayName("inOrderTraversal should visit nodes in sorted order")
        void inOrderTraversalShouldVisitNodesInSortedOrder() {
            AvlTree<Integer> tree = new AvlTree<>();
            tree.insert(5, 3, 7, 1, 9);

            List<Integer> visited = new ArrayList<>();
            tree.inOrderTraversal(visited::add);

            assertThat(visited).containsExactly(1, 3, 5, 7, 9);
        }

        @Test
        @DisplayName("preOrderTraversal should visit root before children")
        void preOrderTraversalShouldVisitRootBeforeChildren() {
            AvlTree<Integer> tree = new AvlTree<>();
            tree.insert(5, 3, 7);

            List<Integer> visited = new ArrayList<>();
            tree.preOrderTraversal(visited::add);

            assertThat(visited.get(0)).isEqualTo(5);
            assertThat(visited).containsExactlyInAnyOrder(3, 5, 7);
        }

        @Test
        @DisplayName("postOrderTraversal should visit root after children")
        void postOrderTraversalShouldVisitRootAfterChildren() {
            AvlTree<Integer> tree = new AvlTree<>();
            tree.insert(5, 3, 7);

            List<Integer> visited = new ArrayList<>();
            tree.postOrderTraversal(visited::add);

            assertThat(visited.get(2)).isEqualTo(5);
        }

        @Test
        @DisplayName("levelOrderTraversal should visit level by level")
        void levelOrderTraversalShouldVisitLevelByLevel() {
            AvlTree<Integer> tree = new AvlTree<>();
            tree.insert(5, 3, 7);

            List<Integer> visited = new ArrayList<>();
            tree.levelOrderTraversal(visited::add);

            assertThat(visited.get(0)).isEqualTo(5);
            assertThat(visited).hasSize(3);
        }

        @Test
        @DisplayName("toSortedList should return sorted elements")
        void toSortedListShouldReturnSortedElements() {
            AvlTree<Integer> tree = new AvlTree<>();
            tree.insert(5, 3, 7, 1, 9);

            List<Integer> sorted = tree.toSortedList();

            assertThat(sorted).containsExactly(1, 3, 5, 7, 9);
        }
    }

    @Nested
    @DisplayName("Range Query Tests")
    class RangeQueryTests {

        @Test
        @DisplayName("range should return elements in range")
        void rangeShouldReturnElementsInRange() {
            AvlTree<Integer> tree = new AvlTree<>();
            tree.insert(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

            List<Integer> range = tree.range(3, 7);

            assertThat(range).containsExactly(3, 4, 5, 6, 7);
        }

        @Test
        @DisplayName("range should return empty for no matches")
        void rangeShouldReturnEmptyForNoMatches() {
            AvlTree<Integer> tree = new AvlTree<>();
            tree.insert(1, 2, 3);

            List<Integer> range = tree.range(10, 20);

            assertThat(range).isEmpty();
        }
    }

    @Nested
    @DisplayName("Balance Tests")
    class BalanceTests {

        @Test
        @DisplayName("tree should remain balanced after insertions")
        void treeShouldRemainBalancedAfterInsertions() {
            AvlTree<Integer> tree = new AvlTree<>();

            for (int i = 1; i <= 100; i++) {
                tree.insert(i);
            }

            assertThat(tree.isBalanced()).isTrue();
        }

        @Test
        @DisplayName("tree should remain balanced after deletions")
        void treeShouldRemainBalancedAfterDeletions() {
            AvlTree<Integer> tree = new AvlTree<>();
            for (int i = 1; i <= 100; i++) {
                tree.insert(i);
            }

            for (int i = 1; i <= 50; i++) {
                tree.delete(i);
            }

            assertThat(tree.isBalanced()).isTrue();
        }

        @Test
        @DisplayName("height should be logarithmic")
        void heightShouldBeLogarithmic() {
            AvlTree<Integer> tree = new AvlTree<>();
            for (int i = 1; i <= 1000; i++) {
                tree.insert(i);
            }

            // For AVL tree, height <= 1.44 * log2(n+2)
            int maxExpectedHeight = (int) (1.44 * Math.log(1002) / Math.log(2));
            assertThat(tree.height()).isLessThanOrEqualTo(maxExpectedHeight);
        }
    }

    @Nested
    @DisplayName("String Element Tests")
    class StringElementTests {

        @Test
        @DisplayName("should work with String elements")
        void shouldWorkWithStringElements() {
            AvlTree<String> tree = new AvlTree<>();
            tree.insert("banana", "apple", "cherry");

            assertThat(tree.min()).contains("apple");
            assertThat(tree.max()).contains("cherry");
            assertThat(tree.toSortedList()).containsExactly("apple", "banana", "cherry");
        }
    }
}
