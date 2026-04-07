package cloud.opencode.base.collections.immutable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

/**
 * PersistentSet 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
@DisplayName("PersistentSet 测试")
class PersistentSetTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("empty - 创建空集合")
        void testEmpty() {
            PersistentSet<String> set = PersistentSet.empty();

            assertThat(set.isEmpty()).isTrue();
            assertThat(set.size()).isZero();
        }

        @Test
        @DisplayName("of - 通过 varargs 创建集合")
        void testOf() {
            PersistentSet<String> set = PersistentSet.of("a", "b", "c");

            assertThat(set.size()).isEqualTo(3);
            assertThat(set.contains("a")).isTrue();
            assertThat(set.contains("b")).isTrue();
            assertThat(set.contains("c")).isTrue();
        }

        @Test
        @DisplayName("of - 重复元素被去重")
        void testOfWithDuplicates() {
            PersistentSet<String> set = PersistentSet.of("a", "b", "a", "c", "b");

            assertThat(set.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("from - 从 Iterable 创建集合")
        void testFrom() {
            List<Integer> source = List.of(10, 20, 30);
            PersistentSet<Integer> set = PersistentSet.from(source);

            assertThat(set.size()).isEqualTo(3);
            assertThat(set.contains(10)).isTrue();
            assertThat(set.contains(20)).isTrue();
            assertThat(set.contains(30)).isTrue();
        }

        @Test
        @DisplayName("from - 从带重复的 Iterable 创建集合")
        void testFromWithDuplicates() {
            List<String> source = List.of("x", "y", "x");
            PersistentSet<String> set = PersistentSet.from(source);

            assertThat(set.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("核心操作测试")
    class CoreOperationTests {

        @Test
        @DisplayName("add - 添加元素返回新集合，原集合不变")
        void testAddReturnsNewSet() {
            PersistentSet<String> original = PersistentSet.of("a", "b");
            PersistentSet<String> updated = original.add("c");

            assertThat(original.size()).isEqualTo(2);
            assertThat(original.contains("c")).isFalse();
            assertThat(updated.size()).isEqualTo(3);
            assertThat(updated.contains("c")).isTrue();
        }

        @Test
        @DisplayName("add - 添加已存在元素返回同一引用")
        void testAddExistingElement() {
            PersistentSet<String> set = PersistentSet.of("a", "b");
            PersistentSet<String> same = set.add("a");

            assertThat(same).isSameAs(set);
        }

        @Test
        @DisplayName("remove - 删除元素返回新集合")
        void testRemove() {
            PersistentSet<String> set = PersistentSet.of("a", "b", "c");
            PersistentSet<String> removed = set.remove("b");

            assertThat(set.size()).isEqualTo(3);
            assertThat(removed.size()).isEqualTo(2);
            assertThat(removed.contains("b")).isFalse();
            assertThat(removed.contains("a")).isTrue();
            assertThat(removed.contains("c")).isTrue();
        }

        @Test
        @DisplayName("remove - 删除不存在的元素返回同一引用")
        void testRemoveNonExistent() {
            PersistentSet<String> set = PersistentSet.of("a", "b");
            PersistentSet<String> same = set.remove("z");

            assertThat(same).isSameAs(set);
        }

        @Test
        @DisplayName("contains - 查询存在和不存在的元素")
        void testContains() {
            PersistentSet<Integer> set = PersistentSet.of(1, 2, 3);

            assertThat(set.contains(1)).isTrue();
            assertThat(set.contains(2)).isTrue();
            assertThat(set.contains(3)).isTrue();
            assertThat(set.contains(4)).isFalse();
            assertThat(set.contains(0)).isFalse();
        }

        @Test
        @DisplayName("size - 多次 add/remove 后的大小")
        void testSizeAfterMultipleOperations() {
            PersistentSet<String> set = PersistentSet.<String>empty()
                    .add("a")
                    .add("b")
                    .add("c")
                    .remove("b")
                    .add("d")
                    .add("a"); // duplicate

            assertThat(set.size()).isEqualTo(3);
            assertThat(set.contains("a")).isTrue();
            assertThat(set.contains("c")).isTrue();
            assertThat(set.contains("d")).isTrue();
            assertThat(set.contains("b")).isFalse();
        }
    }

    @Nested
    @DisplayName("集合代数测试")
    class SetAlgebraTests {

        @Test
        @DisplayName("union - 两个集合的并集")
        void testUnion() {
            PersistentSet<String> a = PersistentSet.of("a", "b", "c");
            PersistentSet<String> b = PersistentSet.of("b", "c", "d", "e");

            PersistentSet<String> union = a.union(b);

            assertThat(union.size()).isEqualTo(5);
            assertThat(union.toSet()).containsExactlyInAnyOrder("a", "b", "c", "d", "e");
        }

        @Test
        @DisplayName("union - 与空集合的并集")
        void testUnionWithEmpty() {
            PersistentSet<String> set = PersistentSet.of("a", "b");
            PersistentSet<String> union = set.union(PersistentSet.empty());

            assertThat(union.toSet()).isEqualTo(set.toSet());
        }

        @Test
        @DisplayName("intersection - 两个集合的交集")
        void testIntersection() {
            PersistentSet<String> a = PersistentSet.of("a", "b", "c");
            PersistentSet<String> b = PersistentSet.of("b", "c", "d");

            PersistentSet<String> inter = a.intersection(b);

            assertThat(inter.size()).isEqualTo(2);
            assertThat(inter.toSet()).containsExactlyInAnyOrder("b", "c");
        }

        @Test
        @DisplayName("intersection - 无交集的两个集合")
        void testIntersectionDisjoint() {
            PersistentSet<Integer> a = PersistentSet.of(1, 2);
            PersistentSet<Integer> b = PersistentSet.of(3, 4);

            PersistentSet<Integer> inter = a.intersection(b);

            assertThat(inter.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("difference - 两个集合的差集")
        void testDifference() {
            PersistentSet<String> a = PersistentSet.of("a", "b", "c");
            PersistentSet<String> b = PersistentSet.of("b", "c", "d");

            PersistentSet<String> diff = a.difference(b);

            assertThat(diff.size()).isEqualTo(1);
            assertThat(diff.toSet()).containsExactly("a");
        }

        @Test
        @DisplayName("difference - 与空集合的差集")
        void testDifferenceWithEmpty() {
            PersistentSet<String> set = PersistentSet.of("a", "b");
            PersistentSet<String> diff = set.difference(PersistentSet.empty());

            assertThat(diff.toSet()).isEqualTo(set.toSet());
        }
    }

    @Nested
    @DisplayName("迭代与转换测试")
    class IterationConversionTests {

        @Test
        @DisplayName("iterator - 遍历所有元素")
        void testIterator() {
            PersistentSet<String> set = PersistentSet.of("a", "b", "c");
            Iterator<String> it = set.iterator();

            int count = 0;
            while (it.hasNext()) {
                String element = it.next();
                assertThat(set.contains(element)).isTrue();
                count++;
            }
            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("stream - 流操作")
        void testStream() {
            PersistentSet<Integer> set = PersistentSet.of(1, 2, 3, 4, 5);

            Set<Integer> collected = set.stream().collect(Collectors.toSet());

            assertThat(collected).containsExactlyInAnyOrder(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("toSet - 转换为 JDK Set")
        void testToSet() {
            PersistentSet<String> set = PersistentSet.of("x", "y", "z");

            Set<String> jdkSet = set.toSet();

            assertThat(jdkSet).containsExactlyInAnyOrder("x", "y", "z");
            assertThatThrownBy(() -> jdkSet.add("w"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相同元素的集合相等")
        void testEquals() {
            PersistentSet<String> a = PersistentSet.of("a", "b", "c");
            PersistentSet<String> b = PersistentSet.of("c", "b", "a");

            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("equals - 不同元素的集合不相等")
        void testNotEquals() {
            PersistentSet<String> a = PersistentSet.of("a", "b");
            PersistentSet<String> b = PersistentSet.of("a", "c");

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("equals - 不同大小的集合不相等")
        void testNotEqualsDifferentSize() {
            PersistentSet<String> a = PersistentSet.of("a", "b");
            PersistentSet<String> b = PersistentSet.of("a", "b", "c");

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("equals - 与非 PersistentSet 对象不相等")
        void testNotEqualsOtherType() {
            PersistentSet<String> set = PersistentSet.of("a");

            assertThat(set).isNotEqualTo("not a set");
        }

        @Test
        @DisplayName("hashCode - 相同元素的集合 hashCode 相等")
        void testHashCode() {
            PersistentSet<String> a = PersistentSet.of("a", "b", "c");
            PersistentSet<String> b = PersistentSet.of("c", "b", "a");

            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("toString - 输出格式正确")
        void testToString() {
            PersistentSet<String> empty = PersistentSet.empty();

            assertThat(empty.toString()).isEqualTo("PersistentSet[]");

            PersistentSet<Integer> single = PersistentSet.of(42);
            assertThat(single.toString()).startsWith("PersistentSet[").endsWith("]");
            assertThat(single.toString()).contains("42");
        }
    }
}
