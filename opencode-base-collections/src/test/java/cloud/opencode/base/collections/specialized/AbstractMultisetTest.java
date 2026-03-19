package cloud.opencode.base.collections.specialized;

import cloud.opencode.base.collections.HashMultiset;
import cloud.opencode.base.collections.Multiset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * AbstractMultiset 抽象类测试
 * 通过 HashMultiset 实现类测试 AbstractMultiset 的所有方法
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("AbstractMultiset 测试")
class AbstractMultisetTest {

    // Helper: Create a concrete implementation for testing
    private HashMultiset<String> createMultiset() {
        return HashMultiset.create();
    }

    @Nested
    @DisplayName("Collection 方法测试")
    class CollectionMethodTests {

        @Test
        @DisplayName("add - 添加单个元素")
        void testAdd() {
            HashMultiset<String> multiset = createMultiset();

            boolean result = multiset.add("a");

            assertThat(result).isTrue();
            assertThat(multiset.count("a")).isEqualTo(1);
        }

        @Test
        @DisplayName("add - 多次添加")
        void testAddMultiple() {
            HashMultiset<String> multiset = createMultiset();

            multiset.add("a");
            multiset.add("a");
            multiset.add("a");

            assertThat(multiset.count("a")).isEqualTo(3);
        }

        @Test
        @DisplayName("remove - 移除单个")
        void testRemove() {
            HashMultiset<String> multiset = createMultiset();
            multiset.add("a", 3);

            boolean result = multiset.remove("a");

            assertThat(result).isTrue();
            assertThat(multiset.count("a")).isEqualTo(2);
        }

        @Test
        @DisplayName("remove - 移除不存在的元素")
        void testRemoveNonExistent() {
            HashMultiset<String> multiset = createMultiset();

            boolean result = multiset.remove("x");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("containsAll - 包含所有")
        void testContainsAll() {
            HashMultiset<String> multiset = createMultiset();
            multiset.add("a");
            multiset.add("b");
            multiset.add("c");

            assertThat(multiset.containsAll(List.of("a", "b"))).isTrue();
            assertThat(multiset.containsAll(List.of("a", "x"))).isFalse();
        }

        @Test
        @DisplayName("containsAll - 空集合")
        void testContainsAllEmpty() {
            HashMultiset<String> multiset = createMultiset();
            multiset.add("a");

            assertThat(multiset.containsAll(List.of())).isTrue();
        }

        @Test
        @DisplayName("addAll - 批量添加")
        void testAddAll() {
            HashMultiset<String> multiset = createMultiset();

            boolean result = multiset.addAll(List.of("a", "b", "c"));

            assertThat(result).isTrue();
            assertThat(multiset.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("addAll - 添加重复元素")
        void testAddAllDuplicates() {
            HashMultiset<String> multiset = createMultiset();

            multiset.addAll(List.of("a", "a", "a"));

            assertThat(multiset.count("a")).isEqualTo(3);
        }

        @Test
        @DisplayName("removeAll - 批量移除")
        void testRemoveAll() {
            HashMultiset<String> multiset = createMultiset();
            multiset.add("a", 3);
            multiset.add("b", 2);
            multiset.add("c", 1);

            boolean result = multiset.removeAll(List.of("a", "b"));

            assertThat(result).isTrue();
            // removeAll removes ALL occurrences of specified elements
            assertThat(multiset.count("a")).isEqualTo(0);
            assertThat(multiset.count("b")).isEqualTo(0);
            assertThat(multiset.count("c")).isEqualTo(1);
        }

        @Test
        @DisplayName("removeAll - 移除不存在的元素")
        void testRemoveAllNonExistent() {
            HashMultiset<String> multiset = createMultiset();
            multiset.add("a");

            boolean result = multiset.removeAll(List.of("x", "y"));

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("retainAll - 保留元素")
        void testRetainAll() {
            HashMultiset<String> multiset = createMultiset();
            multiset.add("a", 2);
            multiset.add("b", 2);
            multiset.add("c", 2);

            boolean result = multiset.retainAll(List.of("a", "b"));

            assertThat(result).isTrue();
            assertThat(multiset.contains("a")).isTrue();
            assertThat(multiset.contains("b")).isTrue();
            assertThat(multiset.contains("c")).isFalse();
        }

        @Test
        @DisplayName("retainAll - 无变化")
        void testRetainAllNoChange() {
            HashMultiset<String> multiset = createMultiset();
            multiset.add("a");
            multiset.add("b");

            boolean result = multiset.retainAll(List.of("a", "b", "c"));

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("toArray - 转为数组")
        void testToArray() {
            HashMultiset<String> multiset = createMultiset();
            multiset.add("a", 2);
            multiset.add("b", 1);

            Object[] array = multiset.toArray();

            assertThat(array).hasSize(3);
            assertThat(array).contains("a", "a", "b");
        }

        @Test
        @DisplayName("toArray - 空多重集合")
        void testToArrayEmpty() {
            HashMultiset<String> multiset = createMultiset();

            Object[] array = multiset.toArray();

            assertThat(array).isEmpty();
        }

        @Test
        @DisplayName("toArray(T[]) - 类型化数组")
        void testToArrayTyped() {
            HashMultiset<String> multiset = createMultiset();
            multiset.add("a", 2);
            multiset.add("b", 1);

            String[] array = multiset.toArray(new String[0]);

            assertThat(array).hasSize(3);
            assertThat(array).contains("a", "a", "b");
        }

        @Test
        @DisplayName("toArray(T[]) - 数组足够大")
        void testToArrayTypedLargeEnough() {
            HashMultiset<String> multiset = createMultiset();
            multiset.add("a", 2);

            String[] array = multiset.toArray(new String[5]);

            assertThat(array[0]).isEqualTo("a");
            assertThat(array[1]).isEqualTo("a");
            assertThat(array[2]).isNull();
        }

        @Test
        @DisplayName("toArray(T[]) - 数组不够大")
        void testToArrayTypedTooSmall() {
            HashMultiset<String> multiset = createMultiset();
            multiset.add("a", 3);

            String[] array = multiset.toArray(new String[1]);

            assertThat(array).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等")
        void testEquals() {
            HashMultiset<String> multiset1 = createMultiset();
            multiset1.add("a", 2);
            multiset1.add("b", 1);

            HashMultiset<String> multiset2 = createMultiset();
            multiset2.add("a", 2);
            multiset2.add("b", 1);

            assertThat(multiset1.equals(multiset2)).isTrue();
        }

        @Test
        @DisplayName("equals - 自身")
        void testEqualsSelf() {
            HashMultiset<String> multiset = createMultiset();
            multiset.add("a");

            assertThat(multiset.equals(multiset)).isTrue();
        }

        @Test
        @DisplayName("equals - 不相等（计数不同）")
        void testNotEqualsDifferentCount() {
            HashMultiset<String> multiset1 = createMultiset();
            multiset1.add("a", 2);

            HashMultiset<String> multiset2 = createMultiset();
            multiset2.add("a", 3);

            assertThat(multiset1.equals(multiset2)).isFalse();
        }

        @Test
        @DisplayName("equals - 不相等（元素不同）")
        void testNotEqualsDifferentElements() {
            HashMultiset<String> multiset1 = createMultiset();
            multiset1.add("a", 2);

            HashMultiset<String> multiset2 = createMultiset();
            multiset2.add("b", 2);

            assertThat(multiset1.equals(multiset2)).isFalse();
        }

        @Test
        @DisplayName("equals - 非 Multiset 对象")
        void testEqualsNonMultiset() {
            HashMultiset<String> multiset = createMultiset();
            multiset.add("a");

            assertThat(multiset.equals("not a multiset")).isFalse();
            assertThat(multiset.equals(List.of("a"))).isFalse();
        }

        @Test
        @DisplayName("equals - 大小不同")
        void testEqualsDifferentSize() {
            HashMultiset<String> multiset1 = createMultiset();
            multiset1.add("a", 2);

            HashMultiset<String> multiset2 = createMultiset();
            multiset2.add("a", 1);
            multiset2.add("b", 1);

            assertThat(multiset1.equals(multiset2)).isFalse();
        }

        @Test
        @DisplayName("hashCode - 相等对象相同哈希")
        void testHashCode() {
            HashMultiset<String> multiset1 = createMultiset();
            multiset1.add("a", 2);

            HashMultiset<String> multiset2 = createMultiset();
            multiset2.add("a", 2);

            assertThat(multiset1.hashCode()).isEqualTo(multiset2.hashCode());
        }

        @Test
        @DisplayName("hashCode - 空多重集合")
        void testHashCodeEmpty() {
            HashMultiset<String> multiset = createMultiset();

            // Should not throw
            assertThat(multiset.hashCode()).isNotNull();
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            HashMultiset<String> multiset = createMultiset();
            multiset.add("a", 2);

            String str = multiset.toString();

            assertThat(str).startsWith("[");
            assertThat(str).endsWith("]");
            assertThat(str).contains("a x 2");
        }

        @Test
        @DisplayName("toString - 多个元素")
        void testToStringMultiple() {
            HashMultiset<String> multiset = createMultiset();
            multiset.add("a", 2);
            multiset.add("b", 3);

            String str = multiset.toString();

            assertThat(str).contains("a x 2");
            assertThat(str).contains("b x 3");
            assertThat(str).contains(", ");
        }

        @Test
        @DisplayName("toString - 空多重集合")
        void testToStringEmpty() {
            HashMultiset<String> multiset = createMultiset();

            assertThat(multiset.toString()).isEqualTo("[]");
        }
    }

    @Nested
    @DisplayName("迭代测试")
    class IterationTests {

        @Test
        @DisplayName("iterator - 遍历")
        void testIterator() {
            HashMultiset<String> multiset = createMultiset();
            multiset.add("a", 2);
            multiset.add("b", 1);

            List<String> elements = new ArrayList<>();
            for (String e : multiset) {
                elements.add(e);
            }

            assertThat(elements).hasSize(3);
            assertThat(elements).contains("a", "a", "b");
        }

        @Test
        @DisplayName("iterator - 空多重集合")
        void testIteratorEmpty() {
            HashMultiset<String> multiset = createMultiset();

            List<String> elements = new ArrayList<>();
            for (String e : multiset) {
                elements.add(e);
            }

            assertThat(elements).isEmpty();
        }
    }

    @Nested
    @DisplayName("Entry 测试")
    class EntryTests {

        @Test
        @DisplayName("entrySet - 获取条目集")
        void testEntrySet() {
            HashMultiset<String> multiset = createMultiset();
            multiset.add("a", 2);
            multiset.add("b", 3);

            Set<Multiset.Entry<String>> entries = multiset.entrySet();

            assertThat(entries).hasSize(2);
        }

        @Test
        @DisplayName("Entry - getElement 和 getCount")
        void testEntryGetters() {
            HashMultiset<String> multiset = createMultiset();
            multiset.add("test", 5);

            for (Multiset.Entry<String> entry : multiset.entrySet()) {
                if ("test".equals(entry.getElement())) {
                    assertThat(entry.getElement()).isEqualTo("test");
                    assertThat(entry.getCount()).isEqualTo(5);
                }
            }
        }
    }
}
