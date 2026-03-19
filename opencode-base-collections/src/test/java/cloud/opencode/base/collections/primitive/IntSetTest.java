package cloud.opencode.base.collections.primitive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * IntSet 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("IntSet 测试")
class IntSetTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 创建空集合")
        void testCreate() {
            IntSet set = IntSet.create();

            assertThat(set.isEmpty()).isTrue();
            assertThat(set.size()).isZero();
        }

        @Test
        @DisplayName("create - 指定初始容量")
        void testCreateWithCapacity() {
            IntSet set = IntSet.create(100);

            assertThat(set.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("of - 从数组创建")
        void testOf() {
            IntSet set = IntSet.of(1, 2, 3, 4, 5);

            assertThat(set.size()).isEqualTo(5);
            assertThat(set.contains(1)).isTrue();
            assertThat(set.contains(5)).isTrue();
        }

        @Test
        @DisplayName("of - 空数组")
        void testOfEmpty() {
            IntSet set = IntSet.of();

            assertThat(set.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("of - 去重")
        void testOfDeduplicate() {
            IntSet set = IntSet.of(1, 2, 2, 3, 3, 3);

            assertThat(set.size()).isEqualTo(3);
        }

    }

    @Nested
    @DisplayName("添加操作测试")
    class AddTests {

        @Test
        @DisplayName("add - 添加元素")
        void testAdd() {
            IntSet set = IntSet.create();

            boolean added = set.add(42);

            assertThat(added).isTrue();
            assertThat(set.contains(42)).isTrue();
        }

        @Test
        @DisplayName("add - 添加重复元素")
        void testAddDuplicate() {
            IntSet set = IntSet.create();
            set.add(42);

            boolean added = set.add(42);

            assertThat(added).isFalse();
            assertThat(set.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("add - 添加零")
        void testAddZero() {
            IntSet set = IntSet.create();

            set.add(0);

            assertThat(set.contains(0)).isTrue();
        }

        @Test
        @DisplayName("add - 添加负数")
        void testAddNegative() {
            IntSet set = IntSet.create();

            set.add(-100);

            assertThat(set.contains(-100)).isTrue();
        }

    }

    @Nested
    @DisplayName("删除操作测试")
    class RemoveTests {

        @Test
        @DisplayName("remove - 删除存在的元素")
        void testRemove() {
            IntSet set = IntSet.of(1, 2, 3);

            boolean removed = set.remove(2);

            assertThat(removed).isTrue();
            assertThat(set.contains(2)).isFalse();
            assertThat(set.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("remove - 删除不存在的元素")
        void testRemoveNotExists() {
            IntSet set = IntSet.of(1, 2, 3);

            boolean removed = set.remove(100);

            assertThat(removed).isFalse();
            assertThat(set.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("clear - 清空")
        void testClear() {
            IntSet set = IntSet.of(1, 2, 3);

            set.clear();

            assertThat(set.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("查询操作测试")
    class QueryTests {

        @Test
        @DisplayName("contains - 包含元素")
        void testContains() {
            IntSet set = IntSet.of(1, 2, 3);

            assertThat(set.contains(1)).isTrue();
            assertThat(set.contains(2)).isTrue();
            assertThat(set.contains(3)).isTrue();
            assertThat(set.contains(4)).isFalse();
        }

        @Test
        @DisplayName("size - 大小")
        void testSize() {
            IntSet set = IntSet.of(1, 2, 3);

            assertThat(set.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("isEmpty - 空判断")
        void testIsEmpty() {
            IntSet empty = IntSet.create();
            IntSet nonEmpty = IntSet.of(1);

            assertThat(empty.isEmpty()).isTrue();
            assertThat(nonEmpty.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("转换操作测试")
    class ConversionTests {

        @Test
        @DisplayName("toIntArray - 转为 int 数组")
        void testToIntArray() {
            IntSet set = IntSet.of(1, 2, 3);

            int[] array = set.toIntArray();

            assertThat(array).hasSize(3);
            assertThat(array).containsExactlyInAnyOrder(1, 2, 3);
        }

        @Test
        @DisplayName("toIntArray - 空集合")
        void testToIntArrayEmpty() {
            IntSet set = IntSet.create();

            int[] array = set.toIntArray();

            assertThat(array).isEmpty();
        }

    }

    @Nested
    @DisplayName("迭代器测试")
    class IteratorTests {

        @Test
        @DisplayName("iterator - 迭代所有元素")
        void testIterator() {
            IntSet set = IntSet.of(1, 2, 3);
            List<Integer> values = new ArrayList<>();

            var iterator = set.iterator();
            while (iterator.hasNext()) {
                values.add(iterator.nextInt());
            }

            assertThat(values).containsExactlyInAnyOrder(1, 2, 3);
        }

        @Test
        @DisplayName("iterator - 空集合")
        void testIteratorEmpty() {
            IntSet set = IntSet.create();

            var iterator = set.iterator();

            assertThat(iterator.hasNext()).isFalse();
        }

        @Test
        @DisplayName("forEach - 遍历")
        void testForEach() {
            IntSet set = IntSet.of(1, 2, 3);
            AtomicInteger sum = new AtomicInteger(0);

            set.forEach(sum::addAndGet);

            assertThat(sum.get()).isEqualTo(6);
        }
    }

    @Nested
    @DisplayName("集合运算测试")
    class SetOperationTests {

        @Test
        @DisplayName("扩容测试 - 大量元素")
        void testResize() {
            IntSet set = IntSet.create();

            for (int i = 0; i < 1000; i++) {
                set.add(i);
            }

            assertThat(set.size()).isEqualTo(1000);
            for (int i = 0; i < 1000; i++) {
                assertThat(set.contains(i)).isTrue();
            }
        }

        @Test
        @DisplayName("边界值测试")
        void testBoundaryValues() {
            IntSet set = IntSet.create();

            set.add(Integer.MIN_VALUE);
            set.add(Integer.MAX_VALUE);
            set.add(0);

            assertThat(set.contains(Integer.MIN_VALUE)).isTrue();
            assertThat(set.contains(Integer.MAX_VALUE)).isTrue();
            assertThat(set.contains(0)).isTrue();
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等")
        void testEquals() {
            IntSet set1 = IntSet.of(1, 2, 3);
            IntSet set2 = IntSet.of(1, 2, 3);

            assertThat(set1).isEqualTo(set2);
        }

        @Test
        @DisplayName("equals - 不相等")
        void testNotEquals() {
            IntSet set1 = IntSet.of(1, 2, 3);
            IntSet set2 = IntSet.of(1, 2, 4);

            assertThat(set1).isNotEqualTo(set2);
        }

        @Test
        @DisplayName("hashCode - 哈希码")
        void testHashCode() {
            IntSet set1 = IntSet.of(1, 2, 3);
            IntSet set2 = IntSet.of(1, 2, 3);

            assertThat(set1.hashCode()).isEqualTo(set2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            IntSet set = IntSet.of(1, 2, 3);

            String str = set.toString();

            assertThat(str).contains("1");
            assertThat(str).contains("2");
            assertThat(str).contains("3");
        }
    }
}
