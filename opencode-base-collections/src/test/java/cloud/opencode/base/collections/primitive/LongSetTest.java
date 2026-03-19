package cloud.opencode.base.collections.primitive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.*;

/**
 * LongSet 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("LongSet 测试")
class LongSetTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 创建空集合")
        void testCreate() {
            LongSet set = LongSet.create();

            assertThat(set.isEmpty()).isTrue();
            assertThat(set.size()).isZero();
        }

        @Test
        @DisplayName("create - 指定初始容量")
        void testCreateWithCapacity() {
            LongSet set = LongSet.create(100);

            assertThat(set.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("of - 从数组创建")
        void testOf() {
            LongSet set = LongSet.of(1L, 2L, 3L, 4L, 5L);

            assertThat(set.size()).isEqualTo(5);
            assertThat(set.contains(1L)).isTrue();
            assertThat(set.contains(5L)).isTrue();
        }

        @Test
        @DisplayName("of - 空数组")
        void testOfEmpty() {
            LongSet set = LongSet.of();

            assertThat(set.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("of - 去重")
        void testOfDeduplicate() {
            LongSet set = LongSet.of(1L, 2L, 2L, 3L, 3L, 3L);

            assertThat(set.size()).isEqualTo(3);
        }

    }

    @Nested
    @DisplayName("添加操作测试")
    class AddTests {

        @Test
        @DisplayName("add - 添加元素")
        void testAdd() {
            LongSet set = LongSet.create();

            boolean added = set.add(42L);

            assertThat(added).isTrue();
            assertThat(set.contains(42L)).isTrue();
        }

        @Test
        @DisplayName("add - 添加重复元素")
        void testAddDuplicate() {
            LongSet set = LongSet.create();
            set.add(42L);

            boolean added = set.add(42L);

            assertThat(added).isFalse();
            assertThat(set.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("add - 添加零")
        void testAddZero() {
            LongSet set = LongSet.create();

            set.add(0L);

            assertThat(set.contains(0L)).isTrue();
        }

        @Test
        @DisplayName("add - 添加负数")
        void testAddNegative() {
            LongSet set = LongSet.create();

            set.add(-100L);

            assertThat(set.contains(-100L)).isTrue();
        }

    }

    @Nested
    @DisplayName("删除操作测试")
    class RemoveTests {

        @Test
        @DisplayName("remove - 删除存在的元素")
        void testRemove() {
            LongSet set = LongSet.of(1L, 2L, 3L);

            boolean removed = set.remove(2L);

            assertThat(removed).isTrue();
            assertThat(set.contains(2L)).isFalse();
            assertThat(set.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("remove - 删除不存在的元素")
        void testRemoveNotExists() {
            LongSet set = LongSet.of(1L, 2L, 3L);

            boolean removed = set.remove(100L);

            assertThat(removed).isFalse();
            assertThat(set.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("clear - 清空")
        void testClear() {
            LongSet set = LongSet.of(1L, 2L, 3L);

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
            LongSet set = LongSet.of(1L, 2L, 3L);

            assertThat(set.contains(1L)).isTrue();
            assertThat(set.contains(2L)).isTrue();
            assertThat(set.contains(3L)).isTrue();
            assertThat(set.contains(4L)).isFalse();
        }

        @Test
        @DisplayName("size - 大小")
        void testSize() {
            LongSet set = LongSet.of(1L, 2L, 3L);

            assertThat(set.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("isEmpty - 空判断")
        void testIsEmpty() {
            LongSet empty = LongSet.create();
            LongSet nonEmpty = LongSet.of(1L);

            assertThat(empty.isEmpty()).isTrue();
            assertThat(nonEmpty.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("转换操作测试")
    class ConversionTests {

        @Test
        @DisplayName("toLongArray - 转为 long 数组")
        void testToLongArray() {
            LongSet set = LongSet.of(1L, 2L, 3L);

            long[] array = set.toLongArray();

            assertThat(array).hasSize(3);
            assertThat(array).containsExactlyInAnyOrder(1L, 2L, 3L);
        }

        @Test
        @DisplayName("toLongArray - 空集合")
        void testToLongArrayEmpty() {
            LongSet set = LongSet.create();

            long[] array = set.toLongArray();

            assertThat(array).isEmpty();
        }

    }

    @Nested
    @DisplayName("迭代器测试")
    class IteratorTests {

        @Test
        @DisplayName("iterator - 迭代所有元素")
        void testIterator() {
            LongSet set = LongSet.of(1L, 2L, 3L);
            List<Long> values = new ArrayList<>();

            var iterator = set.iterator();
            while (iterator.hasNext()) {
                values.add(iterator.nextLong());
            }

            assertThat(values).containsExactlyInAnyOrder(1L, 2L, 3L);
        }

        @Test
        @DisplayName("iterator - 空集合")
        void testIteratorEmpty() {
            LongSet set = LongSet.create();

            var iterator = set.iterator();

            assertThat(iterator.hasNext()).isFalse();
        }

        @Test
        @DisplayName("forEach - 遍历")
        void testForEach() {
            LongSet set = LongSet.of(1L, 2L, 3L);
            AtomicLong sum = new AtomicLong(0);

            set.forEach(sum::addAndGet);

            assertThat(sum.get()).isEqualTo(6L);
        }
    }

    @Nested
    @DisplayName("集合运算测试")
    class SetOperationTests {

        @Test
        @DisplayName("扩容测试 - 大量元素")
        void testResize() {
            LongSet set = LongSet.create();

            for (long i = 0; i < 1000; i++) {
                set.add(i);
            }

            assertThat(set.size()).isEqualTo(1000);
            for (long i = 0; i < 1000; i++) {
                assertThat(set.contains(i)).isTrue();
            }
        }

        @Test
        @DisplayName("边界值测试")
        void testBoundaryValues() {
            LongSet set = LongSet.create();

            set.add(Long.MIN_VALUE);
            set.add(Long.MAX_VALUE);
            set.add(0L);

            assertThat(set.contains(Long.MIN_VALUE)).isTrue();
            assertThat(set.contains(Long.MAX_VALUE)).isTrue();
            assertThat(set.contains(0L)).isTrue();
        }

        @Test
        @DisplayName("大数值测试")
        void testLargeValues() {
            LongSet set = LongSet.create();

            set.add(Long.MAX_VALUE - 1);
            set.add(Long.MIN_VALUE + 1);

            assertThat(set.contains(Long.MAX_VALUE - 1)).isTrue();
            assertThat(set.contains(Long.MIN_VALUE + 1)).isTrue();
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等")
        void testEquals() {
            LongSet set1 = LongSet.of(1L, 2L, 3L);
            LongSet set2 = LongSet.of(1L, 2L, 3L);

            assertThat(set1).isEqualTo(set2);
        }

        @Test
        @DisplayName("equals - 不相等")
        void testNotEquals() {
            LongSet set1 = LongSet.of(1L, 2L, 3L);
            LongSet set2 = LongSet.of(1L, 2L, 4L);

            assertThat(set1).isNotEqualTo(set2);
        }

        @Test
        @DisplayName("hashCode - 哈希码")
        void testHashCode() {
            LongSet set1 = LongSet.of(1L, 2L, 3L);
            LongSet set2 = LongSet.of(1L, 2L, 3L);

            assertThat(set1.hashCode()).isEqualTo(set2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            LongSet set = LongSet.of(1L, 2L, 3L);

            String str = set.toString();

            assertThat(str).contains("1");
            assertThat(str).contains("2");
            assertThat(str).contains("3");
        }
    }
}
