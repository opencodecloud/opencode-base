package cloud.opencode.base.collections.specialized;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * LinkedHashMultiset 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("LinkedHashMultiset 测试")
class LinkedHashMultisetTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 创建空 Multiset")
        void testCreate() {
            LinkedHashMultiset<String> multiset = LinkedHashMultiset.create();

            assertThat(multiset).isEmpty();
            assertThat(multiset.size()).isZero();
        }

        @Test
        @DisplayName("create - 指定预期大小")
        void testCreateWithExpectedSize() {
            LinkedHashMultiset<String> multiset = LinkedHashMultiset.create(100);

            assertThat(multiset).isEmpty();
        }

        @Test
        @DisplayName("create - 从 Iterable 创建")
        void testCreateFromIterable() {
            List<String> source = List.of("a", "b", "a", "c");
            LinkedHashMultiset<String> multiset = LinkedHashMultiset.create(source);

            assertThat(multiset.size()).isEqualTo(4);
            assertThat(multiset.count("a")).isEqualTo(2);
            assertThat(multiset.count("b")).isEqualTo(1);
            assertThat(multiset.count("c")).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Multiset 方法测试")
    class MultisetMethodTests {

        @Test
        @DisplayName("count - 计数")
        void testCount() {
            LinkedHashMultiset<String> multiset = LinkedHashMultiset.create();
            multiset.add("a", 3);
            multiset.add("b", 1);

            assertThat(multiset.count("a")).isEqualTo(3);
            assertThat(multiset.count("b")).isEqualTo(1);
            assertThat(multiset.count("c")).isZero();
        }

        @Test
        @DisplayName("count - null 元素返回 0")
        void testCountNull() {
            LinkedHashMultiset<String> multiset = LinkedHashMultiset.create();
            multiset.add("a");

            assertThat(multiset.count(null)).isZero();
        }

        @Test
        @DisplayName("add - 添加元素")
        void testAdd() {
            LinkedHashMultiset<String> multiset = LinkedHashMultiset.create();

            int oldCount = multiset.add("a", 3);

            assertThat(oldCount).isZero();
            assertThat(multiset.count("a")).isEqualTo(3);
        }

        @Test
        @DisplayName("add - 添加零次不改变计数")
        void testAddZero() {
            LinkedHashMultiset<String> multiset = LinkedHashMultiset.create();
            multiset.add("a", 2);

            int oldCount = multiset.add("a", 0);

            assertThat(oldCount).isEqualTo(2);
            assertThat(multiset.count("a")).isEqualTo(2);
        }

        @Test
        @DisplayName("add - null 元素抛异常")
        void testAddNull() {
            LinkedHashMultiset<String> multiset = LinkedHashMultiset.create();

            assertThatThrownBy(() -> multiset.add(null, 1))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("add - 负数次数抛异常")
        void testAddNegative() {
            LinkedHashMultiset<String> multiset = LinkedHashMultiset.create();

            assertThatThrownBy(() -> multiset.add("a", -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("remove - 移除元素")
        void testRemove() {
            LinkedHashMultiset<String> multiset = LinkedHashMultiset.create();
            multiset.add("a", 5);

            int oldCount = multiset.remove("a", 2);

            assertThat(oldCount).isEqualTo(5);
            assertThat(multiset.count("a")).isEqualTo(3);
        }

        @Test
        @DisplayName("remove - 移除全部")
        void testRemoveAll() {
            LinkedHashMultiset<String> multiset = LinkedHashMultiset.create();
            multiset.add("a", 3);

            int oldCount = multiset.remove("a", 5);

            assertThat(oldCount).isEqualTo(3);
            assertThat(multiset.count("a")).isZero();
        }

        @Test
        @DisplayName("remove - null 元素返回 0")
        void testRemoveNull() {
            LinkedHashMultiset<String> multiset = LinkedHashMultiset.create();
            multiset.add("a");

            int result = multiset.remove(null, 1);

            assertThat(result).isZero();
        }

        @Test
        @DisplayName("setCount - 设置计数")
        void testSetCount() {
            LinkedHashMultiset<String> multiset = LinkedHashMultiset.create();
            multiset.add("a", 2);

            int oldCount = multiset.setCount("a", 5);

            assertThat(oldCount).isEqualTo(2);
            assertThat(multiset.count("a")).isEqualTo(5);
        }

        @Test
        @DisplayName("setCount - 设置为零移除元素")
        void testSetCountZero() {
            LinkedHashMultiset<String> multiset = LinkedHashMultiset.create();
            multiset.add("a", 2);

            int oldCount = multiset.setCount("a", 0);

            assertThat(oldCount).isEqualTo(2);
            assertThat(multiset.contains("a")).isFalse();
        }

        @Test
        @DisplayName("setCount - 负数计数抛异常")
        void testSetCountNegative() {
            LinkedHashMultiset<String> multiset = LinkedHashMultiset.create();

            assertThatThrownBy(() -> multiset.setCount("a", -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("setCount - 条件设置成功")
        void testSetCountConditionalSuccess() {
            LinkedHashMultiset<String> multiset = LinkedHashMultiset.create();
            multiset.add("a", 2);

            boolean result = multiset.setCount("a", 2, 5);

            assertThat(result).isTrue();
            assertThat(multiset.count("a")).isEqualTo(5);
        }

        @Test
        @DisplayName("setCount - 条件设置失败")
        void testSetCountConditionalFail() {
            LinkedHashMultiset<String> multiset = LinkedHashMultiset.create();
            multiset.add("a", 2);

            boolean result = multiset.setCount("a", 3, 5);

            assertThat(result).isFalse();
            assertThat(multiset.count("a")).isEqualTo(2);
        }

        @Test
        @DisplayName("elementSet - 元素集")
        void testElementSet() {
            LinkedHashMultiset<String> multiset = LinkedHashMultiset.create();
            multiset.add("a", 2);
            multiset.add("b", 1);

            Set<String> elementSet = multiset.elementSet();

            assertThat(elementSet).containsExactly("a", "b");
        }

        @Test
        @DisplayName("entrySet - 条目集")
        void testEntrySet() {
            LinkedHashMultiset<String> multiset = LinkedHashMultiset.create();
            multiset.add("a", 2);
            multiset.add("b", 1);

            var entrySet = multiset.entrySet();

            assertThat(entrySet).hasSize(2);
        }
    }

    @Nested
    @DisplayName("插入顺序测试")
    class InsertionOrderTests {

        @Test
        @DisplayName("elementSet 保持插入顺序")
        void testElementSetPreservesOrder() {
            LinkedHashMultiset<String> multiset = LinkedHashMultiset.create();
            multiset.add("cherry");
            multiset.add("apple");
            multiset.add("banana");

            List<String> elements = new ArrayList<>(multiset.elementSet());

            assertThat(elements).containsExactly("cherry", "apple", "banana");
        }

        @Test
        @DisplayName("迭代保持插入顺序")
        void testIterationPreservesOrder() {
            LinkedHashMultiset<String> multiset = LinkedHashMultiset.create();
            multiset.add("cherry", 2);
            multiset.add("apple", 1);

            List<String> result = new ArrayList<>();
            for (String s : multiset) {
                result.add(s);
            }

            assertThat(result).containsExactly("cherry", "cherry", "apple");
        }
    }

    @Nested
    @DisplayName("Collection 方法测试")
    class CollectionMethodTests {

        @Test
        @DisplayName("size - 总大小")
        void testSize() {
            LinkedHashMultiset<String> multiset = LinkedHashMultiset.create();
            multiset.add("a", 3);
            multiset.add("b", 2);

            assertThat(multiset.size()).isEqualTo(5);
        }

        @Test
        @DisplayName("isEmpty - 空判断")
        void testIsEmpty() {
            LinkedHashMultiset<String> multiset = LinkedHashMultiset.create();

            assertThat(multiset.isEmpty()).isTrue();

            multiset.add("a");

            assertThat(multiset.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("contains - 包含")
        void testContains() {
            LinkedHashMultiset<String> multiset = LinkedHashMultiset.create();
            multiset.add("a");

            assertThat(multiset.contains("a")).isTrue();
            assertThat(multiset.contains("b")).isFalse();
        }

        @Test
        @DisplayName("clear - 清空")
        void testClear() {
            LinkedHashMultiset<String> multiset = LinkedHashMultiset.create();
            multiset.add("a", 3);
            multiset.add("b", 2);

            multiset.clear();

            assertThat(multiset).isEmpty();
            assertThat(multiset.size()).isZero();
        }

        @Test
        @DisplayName("iterator - 迭代")
        void testIterator() {
            LinkedHashMultiset<String> multiset = LinkedHashMultiset.create();
            multiset.add("a", 2);

            List<String> result = new ArrayList<>();
            Iterator<String> it = multiset.iterator();
            while (it.hasNext()) {
                result.add(it.next());
            }

            assertThat(result).containsExactly("a", "a");
        }

        @Test
        @DisplayName("iterator.remove - 移除")
        void testIteratorRemove() {
            LinkedHashMultiset<String> multiset = LinkedHashMultiset.create();
            multiset.add("a", 3);

            Iterator<String> it = multiset.iterator();
            it.next();
            it.remove();

            assertThat(multiset.count("a")).isEqualTo(2);
        }
    }
}
