package cloud.opencode.base.tree.diff;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * TreeDiffResultTest Tests
 * TreeDiffResultTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("TreeDiffResult Tests")
class TreeDiffResultTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("empty should create result with no changes")
        void emptyShouldCreateResultWithNoChanges() {
            TreeDiffResult<String> result = TreeDiffResult.empty();

            assertThat(result.added()).isEmpty();
            assertThat(result.removed()).isEmpty();
            assertThat(result.modified()).isEmpty();
            assertThat(result.unchanged()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("constructor should accept all lists")
        void constructorShouldAcceptAllLists() {
            TreeDiffResult<String> result = new TreeDiffResult<>(
                List.of("added"),
                List.of("removed"),
                List.of(new TreeDiffResult.ModifiedNode<>("old", "new")),
                List.of("unchanged")
            );

            assertThat(result.added()).containsExactly("added");
            assertThat(result.removed()).containsExactly("removed");
            assertThat(result.modified()).hasSize(1);
            assertThat(result.unchanged()).containsExactly("unchanged");
        }

        @Test
        @DisplayName("constructor should handle null lists as empty")
        void constructorShouldHandleNullListsAsEmpty() {
            TreeDiffResult<String> result = new TreeDiffResult<>(null, null, null, null);

            assertThat(result.added()).isEmpty();
            assertThat(result.removed()).isEmpty();
            assertThat(result.modified()).isEmpty();
            assertThat(result.unchanged()).isEmpty();
        }
    }

    @Nested
    @DisplayName("IsEqual Tests")
    class IsEqualTests {

        @Test
        @DisplayName("isEqual should return true when no changes")
        void isEqualShouldReturnTrueWhenNoChanges() {
            TreeDiffResult<String> result = new TreeDiffResult<>(
                List.of(), List.of(), List.of(), List.of("unchanged")
            );

            assertThat(result.isEqual()).isTrue();
        }

        @Test
        @DisplayName("isEqual should return false when has additions")
        void isEqualShouldReturnFalseWhenHasAdditions() {
            TreeDiffResult<String> result = new TreeDiffResult<>(
                List.of("added"), List.of(), List.of(), List.of()
            );

            assertThat(result.isEqual()).isFalse();
        }

        @Test
        @DisplayName("isEqual should return false when has removals")
        void isEqualShouldReturnFalseWhenHasRemovals() {
            TreeDiffResult<String> result = new TreeDiffResult<>(
                List.of(), List.of("removed"), List.of(), List.of()
            );

            assertThat(result.isEqual()).isFalse();
        }

        @Test
        @DisplayName("isEqual should return false when has modifications")
        void isEqualShouldReturnFalseWhenHasModifications() {
            TreeDiffResult<String> result = new TreeDiffResult<>(
                List.of(), List.of(),
                List.of(new TreeDiffResult.ModifiedNode<>("old", "new")),
                List.of()
            );

            assertThat(result.isEqual()).isFalse();
        }
    }

    @Nested
    @DisplayName("HasChanges Tests")
    class HasChangesTests {

        @Test
        @DisplayName("hasChanges should return true when has any changes")
        void hasChangesShouldReturnTrueWhenHasAnyChanges() {
            TreeDiffResult<String> result = new TreeDiffResult<>(
                List.of("added"), List.of(), List.of(), List.of()
            );

            assertThat(result.hasChanges()).isTrue();
        }

        @Test
        @DisplayName("hasChanges should return false when no changes")
        void hasChangesShouldReturnFalseWhenNoChanges() {
            TreeDiffResult<String> result = TreeDiffResult.empty();

            assertThat(result.hasChanges()).isFalse();
        }
    }

    @Nested
    @DisplayName("Count Methods Tests")
    class CountMethodsTests {

        @Test
        @DisplayName("getTotalChanges should return sum of all changes")
        void getTotalChangesShouldReturnSumOfAllChanges() {
            TreeDiffResult<String> result = new TreeDiffResult<>(
                List.of("a1", "a2"),
                List.of("r1"),
                List.of(new TreeDiffResult.ModifiedNode<>("old", "new")),
                List.of("u1", "u2", "u3")
            );

            assertThat(result.getTotalChanges()).isEqualTo(4);
        }

        @Test
        @DisplayName("getAddedCount should return added count")
        void getAddedCountShouldReturnAddedCount() {
            TreeDiffResult<String> result = new TreeDiffResult<>(
                List.of("a1", "a2"), List.of(), List.of(), List.of()
            );

            assertThat(result.getAddedCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("getRemovedCount should return removed count")
        void getRemovedCountShouldReturnRemovedCount() {
            TreeDiffResult<String> result = new TreeDiffResult<>(
                List.of(), List.of("r1", "r2", "r3"), List.of(), List.of()
            );

            assertThat(result.getRemovedCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("getModifiedCount should return modified count")
        void getModifiedCountShouldReturnModifiedCount() {
            TreeDiffResult<String> result = new TreeDiffResult<>(
                List.of(), List.of(),
                List.of(
                    new TreeDiffResult.ModifiedNode<>("o1", "n1"),
                    new TreeDiffResult.ModifiedNode<>("o2", "n2")
                ),
                List.of()
            );

            assertThat(result.getModifiedCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("GetSummary Tests")
    class GetSummaryTests {

        @Test
        @DisplayName("getSummary should return formatted summary")
        void getSummaryShouldReturnFormattedSummary() {
            TreeDiffResult<String> result = new TreeDiffResult<>(
                List.of("a1"),
                List.of("r1", "r2"),
                List.of(new TreeDiffResult.ModifiedNode<>("o1", "n1")),
                List.of("u1", "u2", "u3")
            );

            String summary = result.getSummary();

            assertThat(summary).contains("+1");
            assertThat(summary).contains("-2");
            assertThat(summary).contains("~1");
            assertThat(summary).contains("=3");
        }
    }

    @Nested
    @DisplayName("ModifiedNode Tests")
    class ModifiedNodeTests {

        @Test
        @DisplayName("ModifiedNode should store old and new values")
        void modifiedNodeShouldStoreOldAndNewValues() {
            TreeDiffResult.ModifiedNode<String> node =
                new TreeDiffResult.ModifiedNode<>("old", "new");

            assertThat(node.oldNode()).isEqualTo("old");
            assertThat(node.newNode()).isEqualTo("new");
        }
    }
}
