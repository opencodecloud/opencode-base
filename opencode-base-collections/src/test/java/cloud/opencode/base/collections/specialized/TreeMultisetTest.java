package cloud.opencode.base.collections.specialized;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TreeMultiset 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("TreeMultiset 测试")
class TreeMultisetTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 创建空 Multiset (自然排序)")
        void testCreate() {
            TreeMultiset<String> multiset = TreeMultiset.create();

            assertThat(multiset).isEmpty();
            assertThat(multiset.size()).isZero();
        }

        @Test
        @DisplayName("create - 使用自定义比较器")
        void testCreateWithComparator() {
            TreeMultiset<String> multiset = TreeMultiset.create(Comparator.reverseOrder());
            multiset.add("a");
            multiset.add("c");
            multiset.add("b");

            List<String> elements = new ArrayList<>(multiset.elementSet());

            assertThat(elements).containsExactly("c", "b", "a");
        }

        @Test
        @DisplayName("create - 从 Iterable 创建")
        void testCreateFromIterable() {
            List<String> source = List.of("c", "a", "b", "a");
            TreeMultiset<String> multiset = TreeMultiset.create(source);

            assertThat(multiset.size()).isEqualTo(4);
            assertThat(multiset.count("a")).isEqualTo(2);
            List<String> elements = new ArrayList<>(multiset.elementSet());
            assertThat(elements).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("Multiset 方法测试")
    class MultisetMethodTests {

        @Test
        @DisplayName("count - 计数")
        void testCount() {
            TreeMultiset<String> multiset = TreeMultiset.create();
            multiset.add("a", 3);
            multiset.add("b", 1);

            assertThat(multiset.count("a")).isEqualTo(3);
            assertThat(multiset.count("b")).isEqualTo(1);
            assertThat(multiset.count("c")).isZero();
        }

        @Test
        @DisplayName("add - 添加元素")
        void testAdd() {
            TreeMultiset<String> multiset = TreeMultiset.create();

            int oldCount = multiset.add("a", 3);

            assertThat(oldCount).isZero();
            assertThat(multiset.count("a")).isEqualTo(3);
        }

        @Test
        @DisplayName("add - null 元素抛异常")
        void testAddNull() {
            TreeMultiset<String> multiset = TreeMultiset.create();

            assertThatThrownBy(() -> multiset.add(null, 1))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("remove - 移除元素")
        void testRemove() {
            TreeMultiset<String> multiset = TreeMultiset.create();
            multiset.add("a", 5);

            int oldCount = multiset.remove("a", 2);

            assertThat(oldCount).isEqualTo(5);
            assertThat(multiset.count("a")).isEqualTo(3);
        }

        @Test
        @DisplayName("setCount - 设置计数")
        void testSetCount() {
            TreeMultiset<String> multiset = TreeMultiset.create();
            multiset.add("a", 2);

            int oldCount = multiset.setCount("a", 5);

            assertThat(oldCount).isEqualTo(2);
            assertThat(multiset.count("a")).isEqualTo(5);
        }

        @Test
        @DisplayName("elementSet - 有序元素集")
        void testElementSet() {
            TreeMultiset<String> multiset = TreeMultiset.create();
            multiset.add("c", 2);
            multiset.add("a", 1);
            multiset.add("b", 3);

            NavigableSet<String> elementSet = multiset.elementSet();

            assertThat(new ArrayList<>(elementSet)).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("NavigableMultiset 方法测试")
    class NavigableMultisetMethodTests {

        @Test
        @DisplayName("first - 第一个元素")
        void testFirst() {
            TreeMultiset<String> multiset = TreeMultiset.create();
            multiset.add("c");
            multiset.add("a");
            multiset.add("b");

            assertThat(multiset.first()).isEqualTo("a");
        }

        @Test
        @DisplayName("first - 空集抛异常")
        void testFirstEmpty() {
            TreeMultiset<String> multiset = TreeMultiset.create();

            assertThatThrownBy(multiset::first)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("last - 最后一个元素")
        void testLast() {
            TreeMultiset<String> multiset = TreeMultiset.create();
            multiset.add("c");
            multiset.add("a");
            multiset.add("b");

            assertThat(multiset.last()).isEqualTo("c");
        }

        @Test
        @DisplayName("lower - 小于给定元素的最大元素")
        void testLower() {
            TreeMultiset<Integer> multiset = TreeMultiset.create();
            multiset.add(1);
            multiset.add(3);
            multiset.add(5);

            assertThat(multiset.lower(3)).isEqualTo(1);
            assertThat(multiset.lower(4)).isEqualTo(3);
            assertThat(multiset.lower(1)).isNull();
        }

        @Test
        @DisplayName("higher - 大于给定元素的最小元素")
        void testHigher() {
            TreeMultiset<Integer> multiset = TreeMultiset.create();
            multiset.add(1);
            multiset.add(3);
            multiset.add(5);

            assertThat(multiset.higher(3)).isEqualTo(5);
            assertThat(multiset.higher(2)).isEqualTo(3);
            assertThat(multiset.higher(5)).isNull();
        }

        @Test
        @DisplayName("floor - 小于等于给定元素的最大元素")
        void testFloor() {
            TreeMultiset<Integer> multiset = TreeMultiset.create();
            multiset.add(1);
            multiset.add(3);
            multiset.add(5);

            assertThat(multiset.floor(3)).isEqualTo(3);
            assertThat(multiset.floor(4)).isEqualTo(3);
            assertThat(multiset.floor(0)).isNull();
        }

        @Test
        @DisplayName("ceiling - 大于等于给定元素的最小元素")
        void testCeiling() {
            TreeMultiset<Integer> multiset = TreeMultiset.create();
            multiset.add(1);
            multiset.add(3);
            multiset.add(5);

            assertThat(multiset.ceiling(3)).isEqualTo(3);
            assertThat(multiset.ceiling(2)).isEqualTo(3);
            assertThat(multiset.ceiling(6)).isNull();
        }

        @Test
        @DisplayName("pollFirstEntry - 移除并返回第一个条目")
        void testPollFirstEntry() {
            TreeMultiset<String> multiset = TreeMultiset.create();
            multiset.add("c", 2);
            multiset.add("a", 3);

            var entry = multiset.pollFirstEntry();

            assertThat(entry.getElement()).isEqualTo("a");
            assertThat(entry.getCount()).isEqualTo(3);
            assertThat(multiset.contains("a")).isFalse();
            assertThat(multiset.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("pollLastEntry - 移除并返回最后一个条目")
        void testPollLastEntry() {
            TreeMultiset<String> multiset = TreeMultiset.create();
            multiset.add("a", 2);
            multiset.add("c", 3);

            var entry = multiset.pollLastEntry();

            assertThat(entry.getElement()).isEqualTo("c");
            assertThat(entry.getCount()).isEqualTo(3);
            assertThat(multiset.contains("c")).isFalse();
            assertThat(multiset.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("pollFirstEntry - 空集返回 null")
        void testPollFirstEntryEmpty() {
            TreeMultiset<String> multiset = TreeMultiset.create();

            var entry = multiset.pollFirstEntry();

            assertThat(entry).isNull();
        }

        @Test
        @DisplayName("comparator - 获取比较器")
        void testComparator() {
            TreeMultiset<String> natural = TreeMultiset.create();
            TreeMultiset<String> custom = TreeMultiset.create(Comparator.reverseOrder());

            assertThat(natural.comparator()).isNull();
            assertThat(custom.comparator()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Collection 方法测试")
    class CollectionMethodTests {

        @Test
        @DisplayName("size - 总大小")
        void testSize() {
            TreeMultiset<String> multiset = TreeMultiset.create();
            multiset.add("a", 3);
            multiset.add("b", 2);

            assertThat(multiset.size()).isEqualTo(5);
        }

        @Test
        @DisplayName("isEmpty - 空判断")
        void testIsEmpty() {
            TreeMultiset<String> multiset = TreeMultiset.create();

            assertThat(multiset.isEmpty()).isTrue();

            multiset.add("a");

            assertThat(multiset.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("contains - 包含")
        void testContains() {
            TreeMultiset<String> multiset = TreeMultiset.create();
            multiset.add("a");

            assertThat(multiset.contains("a")).isTrue();
            assertThat(multiset.contains("b")).isFalse();
        }

        @Test
        @DisplayName("clear - 清空")
        void testClear() {
            TreeMultiset<String> multiset = TreeMultiset.create();
            multiset.add("a", 3);
            multiset.add("b", 2);

            multiset.clear();

            assertThat(multiset).isEmpty();
            assertThat(multiset.size()).isZero();
        }

        @Test
        @DisplayName("iterator - 按顺序迭代")
        void testIterator() {
            TreeMultiset<String> multiset = TreeMultiset.create();
            multiset.add("c", 1);
            multiset.add("a", 2);

            List<String> result = new ArrayList<>();
            for (String s : multiset) {
                result.add(s);
            }

            assertThat(result).containsExactly("a", "a", "c");
        }

        @Test
        @DisplayName("iterator.remove - 移除")
        void testIteratorRemove() {
            TreeMultiset<String> multiset = TreeMultiset.create();
            multiset.add("a", 3);

            Iterator<String> it = multiset.iterator();
            it.next();
            it.remove();

            assertThat(multiset.count("a")).isEqualTo(2);
        }
    }
}
