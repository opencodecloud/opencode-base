package cloud.opencode.base.tree.path;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * TreePathTest Tests
 * TreePathTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("TreePath Tests")
class TreePathTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("empty should create empty path")
        void emptyShouldCreateEmptyPath() {
            TreePath<String> path = TreePath.empty();

            assertThat(path.isEmpty()).isTrue();
            assertThat(path.length()).isZero();
        }

        @Test
        @DisplayName("of with varargs should create path")
        void ofWithVarargsShouldCreatePath() {
            TreePath<String> path = TreePath.of("A", "B", "C");

            assertThat(path.length()).isEqualTo(3);
            assertThat(path.nodes()).containsExactly("A", "B", "C");
        }

        @Test
        @DisplayName("of with list should create path")
        void ofWithListShouldCreatePath() {
            TreePath<String> path = TreePath.of(List.of("X", "Y", "Z"));

            assertThat(path.length()).isEqualTo(3);
            assertThat(path.nodes()).containsExactly("X", "Y", "Z");
        }
    }

    @Nested
    @DisplayName("Node Access Tests")
    class NodeAccessTests {

        @Test
        @DisplayName("getRoot should return first node")
        void getRootShouldReturnFirstNode() {
            TreePath<String> path = TreePath.of("A", "B", "C");

            assertThat(path.getRoot()).isEqualTo("A");
        }

        @Test
        @DisplayName("getRoot should return null for empty path")
        void getRootShouldReturnNullForEmptyPath() {
            TreePath<String> path = TreePath.empty();

            assertThat(path.getRoot()).isNull();
        }

        @Test
        @DisplayName("getTarget should return last node")
        void getTargetShouldReturnLastNode() {
            TreePath<String> path = TreePath.of("A", "B", "C");

            assertThat(path.getTarget()).isEqualTo("C");
        }

        @Test
        @DisplayName("getTarget should return null for empty path")
        void getTargetShouldReturnNullForEmptyPath() {
            TreePath<String> path = TreePath.empty();

            assertThat(path.getTarget()).isNull();
        }

        @Test
        @DisplayName("get should return node at index")
        void getShouldReturnNodeAtIndex() {
            TreePath<String> path = TreePath.of("A", "B", "C");

            assertThat(path.get(0)).isEqualTo("A");
            assertThat(path.get(1)).isEqualTo("B");
            assertThat(path.get(2)).isEqualTo("C");
        }

        @Test
        @DisplayName("getParent should return second to last node")
        void getParentShouldReturnSecondToLastNode() {
            TreePath<String> path = TreePath.of("A", "B", "C");

            assertThat(path.getParent()).isEqualTo("B");
        }

        @Test
        @DisplayName("getParent should return null for single node path")
        void getParentShouldReturnNullForSingleNodePath() {
            TreePath<String> path = TreePath.of("A");

            assertThat(path.getParent()).isNull();
        }
    }

    @Nested
    @DisplayName("Path Operations Tests")
    class PathOperationsTests {

        @Test
        @DisplayName("subPath should return sub-path")
        void subPathShouldReturnSubPath() {
            TreePath<String> path = TreePath.of("A", "B", "C", "D");

            TreePath<String> sub = path.subPath(1, 3);

            assertThat(sub.nodes()).containsExactly("B", "C");
        }

        @Test
        @DisplayName("append should add node to path")
        void appendShouldAddNodeToPath() {
            TreePath<String> path = TreePath.of("A", "B");

            TreePath<String> appended = path.append("C");

            assertThat(appended.nodes()).containsExactly("A", "B", "C");
            assertThat(path.nodes()).containsExactly("A", "B"); // Original unchanged
        }

        @Test
        @DisplayName("reverse should return reversed path")
        void reverseShouldReturnReversedPath() {
            TreePath<String> path = TreePath.of("A", "B", "C");

            TreePath<String> reversed = path.reverse();

            assertThat(reversed.nodes()).containsExactly("C", "B", "A");
            assertThat(path.nodes()).containsExactly("A", "B", "C"); // Original unchanged
        }

        @Test
        @DisplayName("contains should return true for existing node")
        void containsShouldReturnTrueForExistingNode() {
            TreePath<String> path = TreePath.of("A", "B", "C");

            assertThat(path.contains("B")).isTrue();
        }

        @Test
        @DisplayName("contains should return false for non-existing node")
        void containsShouldReturnFalseForNonExistingNode() {
            TreePath<String> path = TreePath.of("A", "B", "C");

            assertThat(path.contains("X")).isFalse();
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString should return default separator representation")
        void toStringShouldReturnDefaultSeparatorRepresentation() {
            TreePath<String> path = TreePath.of("A", "B", "C");

            assertThat(path.toString()).isEqualTo("A -> B -> C");
        }

        @Test
        @DisplayName("toString with separator should use custom separator")
        void toStringWithSeparatorShouldUseCustomSeparator() {
            TreePath<String> path = TreePath.of("A", "B", "C");

            assertThat(path.toString("/")).isEqualTo("A/B/C");
        }
    }

    @Nested
    @DisplayName("Compact Constructor Tests")
    class CompactConstructorTests {

        @Test
        @DisplayName("constructor should handle null as empty list")
        void constructorShouldHandleNullAsEmptyList() {
            TreePath<String> path = new TreePath<>(null);

            assertThat(path.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("constructor should create immutable copy of list")
        void constructorShouldCreateImmutableCopyOfList() {
            TreePath<String> path = TreePath.of("A", "B");

            assertThatThrownBy(() -> path.nodes().add("C"))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
