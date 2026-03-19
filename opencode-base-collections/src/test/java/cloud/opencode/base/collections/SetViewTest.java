package cloud.opencode.base.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * SetView 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("SetView 测试")
class SetViewTest {

    @Nested
    @DisplayName("通过 SetUtil 创建测试")
    class CreationTests {

        @Test
        @DisplayName("union - 并集视图")
        void testUnionView() {
            Set<String> set1 = Set.of("a", "b", "c");
            Set<String> set2 = Set.of("c", "d", "e");

            SetView<String> union = SetUtil.union(set1, set2);

            assertThat(union).containsExactlyInAnyOrder("a", "b", "c", "d", "e");
        }

        @Test
        @DisplayName("intersection - 交集视图")
        void testIntersectionView() {
            Set<String> set1 = Set.of("a", "b", "c");
            Set<String> set2 = Set.of("b", "c", "d");

            SetView<String> intersection = SetUtil.intersection(set1, set2);

            assertThat(intersection).containsExactlyInAnyOrder("b", "c");
        }

        @Test
        @DisplayName("difference - 差集视图")
        void testDifferenceView() {
            Set<String> set1 = Set.of("a", "b", "c");
            Set<String> set2 = Set.of("b", "c", "d");

            SetView<String> difference = SetUtil.difference(set1, set2);

            assertThat(difference).containsExactly("a");
        }

        @Test
        @DisplayName("symmetricDifference - 对称差集视图")
        void testSymmetricDifferenceView() {
            Set<String> set1 = Set.of("a", "b", "c");
            Set<String> set2 = Set.of("b", "c", "d");

            SetView<String> symDiff = SetUtil.symmetricDifference(set1, set2);

            assertThat(symDiff).containsExactlyInAnyOrder("a", "d");
        }
    }

    @Nested
    @DisplayName("复制操作测试")
    class CopyTests {

        @Test
        @DisplayName("copyInto - 复制到目标集合")
        void testCopyInto() {
            Set<String> set1 = Set.of("a", "b");
            Set<String> set2 = Set.of("c", "d");
            SetView<String> union = SetUtil.union(set1, set2);

            Set<String> target = new HashSet<>();
            Set<String> result = union.copyInto(target);

            assertThat(result).isSameAs(target);
            assertThat(result).containsExactlyInAnyOrder("a", "b", "c", "d");
        }

        @Test
        @DisplayName("toSet - 复制到新HashSet")
        void testToSet() {
            Set<String> set1 = Set.of("a", "b");
            Set<String> set2 = Set.of("c", "d");
            SetView<String> union = SetUtil.union(set1, set2);

            Set<String> copy = union.toSet();

            assertThat(copy).containsExactlyInAnyOrder("a", "b", "c", "d");
            assertThat(copy).isInstanceOf(HashSet.class);
        }
    }

    @Nested
    @DisplayName("不支持的操作测试")
    class UnsupportedOperationsTests {

        @Test
        @DisplayName("add - 抛出UnsupportedOperationException")
        void testAddThrows() {
            Set<String> set1 = Set.of("a", "b");
            Set<String> set2 = Set.of("c", "d");
            SetView<String> union = SetUtil.union(set1, set2);

            assertThatThrownBy(() -> union.add("e"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("remove - 抛出UnsupportedOperationException")
        void testRemoveThrows() {
            Set<String> set1 = Set.of("a", "b");
            Set<String> set2 = Set.of("c", "d");
            SetView<String> union = SetUtil.union(set1, set2);

            assertThatThrownBy(() -> union.remove("a"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("clear - 抛出UnsupportedOperationException")
        void testClearThrows() {
            Set<String> set1 = Set.of("a", "b");
            Set<String> set2 = Set.of("c", "d");
            SetView<String> union = SetUtil.union(set1, set2);

            assertThatThrownBy(union::clear)
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("基本操作测试")
    class BasicOperationsTests {

        @Test
        @DisplayName("size - 获取大小")
        void testSize() {
            Set<String> set1 = Set.of("a", "b");
            Set<String> set2 = Set.of("c", "d");
            SetView<String> union = SetUtil.union(set1, set2);

            assertThat(union.size()).isEqualTo(4);
        }

        @Test
        @DisplayName("isEmpty - 空视图")
        void testIsEmpty() {
            Set<String> empty1 = Set.of();
            Set<String> empty2 = Set.of();
            SetView<String> union = SetUtil.union(empty1, empty2);

            assertThat(union.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("contains - 包含判断")
        void testContains() {
            Set<String> set1 = Set.of("a", "b");
            Set<String> set2 = Set.of("c", "d");
            SetView<String> union = SetUtil.union(set1, set2);

            assertThat(union.contains("a")).isTrue();
            assertThat(union.contains("c")).isTrue();
            assertThat(union.contains("e")).isFalse();
        }

        @Test
        @DisplayName("iterator - 迭代")
        void testIterator() {
            Set<String> set1 = Set.of("a", "b");
            Set<String> set2 = Set.of("c", "d");
            SetView<String> union = SetUtil.union(set1, set2);

            List<String> elements = new ArrayList<>();
            for (String s : union) {
                elements.add(s);
            }

            assertThat(elements).containsExactlyInAnyOrder("a", "b", "c", "d");
        }
    }
}
