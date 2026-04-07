package cloud.opencode.base.collections.primitive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * DoubleSet 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
@DisplayName("DoubleSet 测试")
class DoubleSetTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 创建空集合")
        void testCreate() {
            DoubleSet set = DoubleSet.create();

            assertThat(set.isEmpty()).isTrue();
            assertThat(set.size()).isZero();
        }

        @Test
        @DisplayName("create - 指定初始容量")
        void testCreateWithCapacity() {
            DoubleSet set = DoubleSet.create(100);

            assertThat(set.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("of - 从数组创建")
        void testOf() {
            DoubleSet set = DoubleSet.of(1.0, 2.5, 3.14, 4.0, 5.0);

            assertThat(set.size()).isEqualTo(5);
            assertThat(set.contains(1.0)).isTrue();
            assertThat(set.contains(3.14)).isTrue();
        }

        @Test
        @DisplayName("of - 空数组")
        void testOfEmpty() {
            DoubleSet set = DoubleSet.of();

            assertThat(set.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("of - 去重")
        void testOfDeduplicate() {
            DoubleSet set = DoubleSet.of(1.0, 2.0, 2.0, 3.0, 3.0, 3.0);

            assertThat(set.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("from - 从 double 数组创建")
        void testFromArray() {
            double[] array = {1.0, 2.0, 3.0};
            DoubleSet set = DoubleSet.from(array);

            assertThat(set.size()).isEqualTo(3);
            assertThat(set.contains(1.0)).isTrue();
            assertThat(set.contains(2.0)).isTrue();
            assertThat(set.contains(3.0)).isTrue();
        }

        @Test
        @DisplayName("from - 从 Collection 创建")
        void testFromCollection() {
            List<Double> list = List.of(1.0, 2.0, 3.0);
            DoubleSet set = DoubleSet.from(list);

            assertThat(set.size()).isEqualTo(3);
            assertThat(set.contains(1.0)).isTrue();
        }
    }

    @Nested
    @DisplayName("添加操作测试")
    class AddTests {

        @Test
        @DisplayName("add - 添加元素")
        void testAdd() {
            DoubleSet set = DoubleSet.create();

            boolean added = set.add(42.5);

            assertThat(added).isTrue();
            assertThat(set.contains(42.5)).isTrue();
        }

        @Test
        @DisplayName("add - 添加重复元素")
        void testAddDuplicate() {
            DoubleSet set = DoubleSet.create();
            set.add(42.5);

            boolean added = set.add(42.5);

            assertThat(added).isFalse();
            assertThat(set.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("add - 添加零")
        void testAddZero() {
            DoubleSet set = DoubleSet.create();

            set.add(0.0);

            assertThat(set.contains(0.0)).isTrue();
        }

        @Test
        @DisplayName("add - 添加负数")
        void testAddNegative() {
            DoubleSet set = DoubleSet.create();

            set.add(-100.5);

            assertThat(set.contains(-100.5)).isTrue();
        }

        @Test
        @DisplayName("addAll - 添加多个值")
        void testAddAll() {
            DoubleSet set = DoubleSet.create();

            set.addAll(1.0, 2.0, 3.0);

            assertThat(set.size()).isEqualTo(3);
            assertThat(set.contains(1.0)).isTrue();
            assertThat(set.contains(2.0)).isTrue();
            assertThat(set.contains(3.0)).isTrue();
        }
    }

    @Nested
    @DisplayName("删除操作测试")
    class RemoveTests {

        @Test
        @DisplayName("remove - 删除存在的元素")
        void testRemove() {
            DoubleSet set = DoubleSet.of(1.0, 2.0, 3.0);

            boolean removed = set.remove(2.0);

            assertThat(removed).isTrue();
            assertThat(set.contains(2.0)).isFalse();
            assertThat(set.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("remove - 删除不存在的元素")
        void testRemoveNotExists() {
            DoubleSet set = DoubleSet.of(1.0, 2.0, 3.0);

            boolean removed = set.remove(100.0);

            assertThat(removed).isFalse();
            assertThat(set.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("clear - 清空")
        void testClear() {
            DoubleSet set = DoubleSet.of(1.0, 2.0, 3.0);

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
            DoubleSet set = DoubleSet.of(1.0, 2.5, 3.14);

            assertThat(set.contains(1.0)).isTrue();
            assertThat(set.contains(2.5)).isTrue();
            assertThat(set.contains(3.14)).isTrue();
            assertThat(set.contains(4.0)).isFalse();
        }

        @Test
        @DisplayName("size - 大小")
        void testSize() {
            DoubleSet set = DoubleSet.of(1.0, 2.0, 3.0);

            assertThat(set.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("isEmpty - 空判断")
        void testIsEmpty() {
            DoubleSet empty = DoubleSet.create();
            DoubleSet nonEmpty = DoubleSet.of(1.0);

            assertThat(empty.isEmpty()).isTrue();
            assertThat(nonEmpty.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("转换操作测试")
    class ConversionTests {

        @Test
        @DisplayName("toDoubleArray - 转为 double 数组")
        void testToDoubleArray() {
            DoubleSet set = DoubleSet.of(1.0, 2.0, 3.0);

            double[] array = set.toDoubleArray();

            assertThat(array).hasSize(3);
            assertThat(array).containsExactlyInAnyOrder(1.0, 2.0, 3.0);
        }

        @Test
        @DisplayName("toDoubleArray - 空集合")
        void testToDoubleArrayEmpty() {
            DoubleSet set = DoubleSet.create();

            double[] array = set.toDoubleArray();

            assertThat(array).isEmpty();
        }

        @Test
        @DisplayName("toSet - 转为 Set<Double>")
        void testToSet() {
            DoubleSet set = DoubleSet.of(1.0, 2.0, 3.0);

            Set<Double> boxedSet = set.toSet();

            assertThat(boxedSet).containsExactlyInAnyOrder(1.0, 2.0, 3.0);
        }

        @Test
        @DisplayName("stream - DoubleStream")
        void testStream() {
            DoubleSet set = DoubleSet.of(1.0, 2.0, 3.0, 4.0, 5.0);

            double sum = set.stream().sum();

            assertThat(sum).isEqualTo(15.0);
        }
    }

    @Nested
    @DisplayName("迭代器测试")
    class IteratorTests {

        @Test
        @DisplayName("iterator - 迭代所有元素")
        void testIterator() {
            DoubleSet set = DoubleSet.of(1.0, 2.0, 3.0);
            List<Double> values = new ArrayList<>();

            var iterator = set.iterator();
            while (iterator.hasNext()) {
                values.add(iterator.nextDouble());
            }

            assertThat(values).containsExactlyInAnyOrder(1.0, 2.0, 3.0);
        }

        @Test
        @DisplayName("iterator - 空集合")
        void testIteratorEmpty() {
            DoubleSet set = DoubleSet.create();

            var iterator = set.iterator();

            assertThat(iterator.hasNext()).isFalse();
        }

        @Test
        @DisplayName("iterator - 越界抛异常")
        void testIteratorOutOfBounds() {
            DoubleSet set = DoubleSet.of(1.0);
            var iterator = set.iterator();
            iterator.nextDouble();

            assertThatThrownBy(iterator::nextDouble)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("forEach - 遍历")
        void testForEach() {
            DoubleSet set = DoubleSet.of(1.0, 2.0, 3.0);
            List<Double> result = new ArrayList<>();

            set.forEach(result::add);

            assertThat(result).containsExactlyInAnyOrder(1.0, 2.0, 3.0);
        }
    }

    @Nested
    @DisplayName("边界值测试")
    class BoundaryTests {

        @Test
        @DisplayName("NaN 处理")
        void testNaN() {
            DoubleSet set = DoubleSet.create();

            set.add(Double.NaN);

            assertThat(set.contains(Double.NaN)).isTrue();
            assertThat(set.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("NaN 去重")
        void testNaNDeduplicate() {
            DoubleSet set = DoubleSet.create();

            set.add(Double.NaN);
            set.add(Double.NaN);

            assertThat(set.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("+0.0 和 -0.0 视为不同值")
        void testPositiveAndNegativeZero() {
            DoubleSet set = DoubleSet.create();

            // Double.compare(0.0, -0.0) != 0, so they are distinct
            set.add(0.0);
            set.add(-0.0);

            // Double.compare treats +0.0 and -0.0 as different
            assertThat(Double.compare(0.0, -0.0)).isNotZero();
            assertThat(set.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("MIN_VALUE 和 MAX_VALUE")
        void testMinMaxValue() {
            DoubleSet set = DoubleSet.create();

            set.add(Double.MIN_VALUE);
            set.add(Double.MAX_VALUE);
            set.add(Double.NEGATIVE_INFINITY);
            set.add(Double.POSITIVE_INFINITY);

            assertThat(set.contains(Double.MIN_VALUE)).isTrue();
            assertThat(set.contains(Double.MAX_VALUE)).isTrue();
            assertThat(set.contains(Double.NEGATIVE_INFINITY)).isTrue();
            assertThat(set.contains(Double.POSITIVE_INFINITY)).isTrue();
            assertThat(set.size()).isEqualTo(4);
        }

        @Test
        @DisplayName("扩容测试 - 大量元素")
        void testResize() {
            DoubleSet set = DoubleSet.create();

            for (int i = 0; i < 1000; i++) {
                set.add(i * 0.1);
            }

            assertThat(set.size()).isEqualTo(1000);
            for (int i = 0; i < 1000; i++) {
                assertThat(set.contains(i * 0.1)).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("集合运算测试")
    class SetOperationTests {

        @Test
        @DisplayName("addAll - 合并集合")
        void testAddAllSet() {
            DoubleSet set1 = DoubleSet.of(1.0, 2.0);
            DoubleSet set2 = DoubleSet.of(2.0, 3.0);

            set1.addAll(set2);

            assertThat(set1.size()).isEqualTo(3);
            assertThat(set1.contains(1.0)).isTrue();
            assertThat(set1.contains(2.0)).isTrue();
            assertThat(set1.contains(3.0)).isTrue();
        }

        @Test
        @DisplayName("removeAll - 差集")
        void testRemoveAll() {
            DoubleSet set1 = DoubleSet.of(1.0, 2.0, 3.0);
            DoubleSet set2 = DoubleSet.of(2.0, 3.0);

            set1.removeAll(set2);

            assertThat(set1.size()).isEqualTo(1);
            assertThat(set1.contains(1.0)).isTrue();
        }

        @Test
        @DisplayName("retainAll - 交集")
        void testRetainAll() {
            DoubleSet set1 = DoubleSet.of(1.0, 2.0, 3.0);
            DoubleSet set2 = DoubleSet.of(2.0, 3.0, 4.0);

            set1.retainAll(set2);

            assertThat(set1.size()).isEqualTo(2);
            assertThat(set1.contains(2.0)).isTrue();
            assertThat(set1.contains(3.0)).isTrue();
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等")
        void testEquals() {
            DoubleSet set1 = DoubleSet.of(1.0, 2.0, 3.0);
            DoubleSet set2 = DoubleSet.of(1.0, 2.0, 3.0);

            assertThat(set1).isEqualTo(set2);
        }

        @Test
        @DisplayName("equals - 不相等")
        void testNotEquals() {
            DoubleSet set1 = DoubleSet.of(1.0, 2.0, 3.0);
            DoubleSet set2 = DoubleSet.of(1.0, 2.0, 4.0);

            assertThat(set1).isNotEqualTo(set2);
        }

        @Test
        @DisplayName("hashCode - 哈希码")
        void testHashCode() {
            DoubleSet set1 = DoubleSet.of(1.0, 2.0, 3.0);
            DoubleSet set2 = DoubleSet.of(1.0, 2.0, 3.0);

            assertThat(set1.hashCode()).isEqualTo(set2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            DoubleSet set = DoubleSet.of(1.0, 2.0, 3.0);

            String str = set.toString();

            assertThat(str).contains("1.0");
            assertThat(str).contains("2.0");
            assertThat(str).contains("3.0");
        }
    }
}
