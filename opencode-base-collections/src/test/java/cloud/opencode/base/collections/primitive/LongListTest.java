package cloud.opencode.base.collections.primitive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.LongConsumer;

import static org.assertj.core.api.Assertions.*;

/**
 * LongList 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("LongList 测试")
class LongListTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 创建空列表")
        void testCreate() {
            LongList list = LongList.create();

            assertThat(list.isEmpty()).isTrue();
            assertThat(list.size()).isZero();
        }

        @Test
        @DisplayName("create - 指定容量")
        void testCreateWithCapacity() {
            LongList list = LongList.create(100);

            assertThat(list.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("create - 负容量抛异常")
        void testCreateNegativeCapacity() {
            assertThatThrownBy(() -> LongList.create(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("of - 从值创建")
        void testOf() {
            LongList list = LongList.of(1L, 2L, 3L, 4L, 5L);

            assertThat(list.size()).isEqualTo(5);
            assertThat(list.get(0)).isEqualTo(1L);
            assertThat(list.get(4)).isEqualTo(5L);
        }

        @Test
        @DisplayName("range - 从范围创建")
        void testRange() {
            LongList list = LongList.range(0L, 5L);

            assertThat(list.size()).isEqualTo(5);
            assertThat(list.toArray()).containsExactly(0L, 1L, 2L, 3L, 4L);
        }

        @Test
        @DisplayName("range - 开始大于结束抛异常")
        void testRangeInvalid() {
            assertThatThrownBy(() -> LongList.range(5L, 0L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("基本操作测试")
    class BasicOperationTests {

        @Test
        @DisplayName("get - 获取元素")
        void testGet() {
            LongList list = LongList.of(10L, 20L, 30L);

            assertThat(list.get(0)).isEqualTo(10L);
            assertThat(list.get(1)).isEqualTo(20L);
            assertThat(list.get(2)).isEqualTo(30L);
        }

        @Test
        @DisplayName("get - 索引越界")
        void testGetOutOfBounds() {
            LongList list = LongList.of(1L);

            assertThatThrownBy(() -> list.get(-1))
                    .isInstanceOf(IndexOutOfBoundsException.class);

            assertThatThrownBy(() -> list.get(1))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("set - 设置元素")
        void testSet() {
            LongList list = LongList.of(1L, 2L, 3L);

            long old = list.set(1, 20L);

            assertThat(old).isEqualTo(2L);
            assertThat(list.get(1)).isEqualTo(20L);
        }

        @Test
        @DisplayName("add - 添加元素")
        void testAdd() {
            LongList list = LongList.create();

            list.add(1L);
            list.add(2L);
            list.add(3L);

            assertThat(list.size()).isEqualTo(3);
            assertThat(list.toArray()).containsExactly(1L, 2L, 3L);
        }

        @Test
        @DisplayName("add - 在索引处添加")
        void testAddAtIndex() {
            LongList list = LongList.of(1L, 3L);

            list.add(1, 2L);

            assertThat(list.toArray()).containsExactly(1L, 2L, 3L);
        }

        @Test
        @DisplayName("add - 索引越界")
        void testAddAtIndexOutOfBounds() {
            LongList list = LongList.of(1L);

            assertThatThrownBy(() -> list.add(-1, 0L))
                    .isInstanceOf(IndexOutOfBoundsException.class);

            assertThatThrownBy(() -> list.add(5, 0L))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("addAll - 添加所有")
        void testAddAll() {
            LongList list = LongList.of(1L);

            list.addAll(2L, 3L, 4L);

            assertThat(list.toArray()).containsExactly(1L, 2L, 3L, 4L);
        }

        @Test
        @DisplayName("removeAt - 移除索引处元素")
        void testRemoveAt() {
            LongList list = LongList.of(1L, 2L, 3L);

            long removed = list.removeAt(1);

            assertThat(removed).isEqualTo(2L);
            assertThat(list.toArray()).containsExactly(1L, 3L);
        }

        @Test
        @DisplayName("remove - 移除值")
        void testRemove() {
            LongList list = LongList.of(1L, 2L, 3L, 2L);

            boolean result = list.remove(2L);

            assertThat(result).isTrue();
            assertThat(list.toArray()).containsExactly(1L, 3L, 2L);
        }

        @Test
        @DisplayName("remove - 值不存在")
        void testRemoveNotFound() {
            LongList list = LongList.of(1L, 2L, 3L);

            boolean result = list.remove(4L);

            assertThat(result).isFalse();
            assertThat(list.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("clear - 清空")
        void testClear() {
            LongList list = LongList.of(1L, 2L, 3L);

            list.clear();

            assertThat(list.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("查找操作测试")
    class SearchOperationTests {

        @Test
        @DisplayName("contains - 包含检查")
        void testContains() {
            LongList list = LongList.of(1L, 2L, 3L);

            assertThat(list.contains(2L)).isTrue();
            assertThat(list.contains(4L)).isFalse();
        }

        @Test
        @DisplayName("indexOf - 首次出现索引")
        void testIndexOf() {
            LongList list = LongList.of(1L, 2L, 3L, 2L);

            assertThat(list.indexOf(2L)).isEqualTo(1);
            assertThat(list.indexOf(4L)).isEqualTo(-1);
        }

        @Test
        @DisplayName("lastIndexOf - 最后出现索引")
        void testLastIndexOf() {
            LongList list = LongList.of(1L, 2L, 3L, 2L);

            assertThat(list.lastIndexOf(2L)).isEqualTo(3);
            assertThat(list.lastIndexOf(4L)).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("统计操作测试")
    class StatisticsOperationTests {

        @Test
        @DisplayName("sum - 求和")
        void testSum() {
            LongList list = LongList.of(1L, 2L, 3L, 4L, 5L);

            assertThat(list.sum()).isEqualTo(15L);
        }

        @Test
        @DisplayName("sum - 空列表")
        void testSumEmpty() {
            LongList list = LongList.create();

            assertThat(list.sum()).isZero();
        }

        @Test
        @DisplayName("min - 最小值")
        void testMin() {
            LongList list = LongList.of(3L, 1L, 4L, 1L, 5L);

            assertThat(list.min()).isEqualTo(1L);
        }

        @Test
        @DisplayName("min - 空列表抛异常")
        void testMinEmpty() {
            LongList list = LongList.create();

            assertThatThrownBy(list::min)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("max - 最大值")
        void testMax() {
            LongList list = LongList.of(3L, 1L, 4L, 1L, 5L);

            assertThat(list.max()).isEqualTo(5L);
        }

        @Test
        @DisplayName("max - 空列表抛异常")
        void testMaxEmpty() {
            LongList list = LongList.create();

            assertThatThrownBy(list::max)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("average - 平均值")
        void testAverage() {
            LongList list = LongList.of(1L, 2L, 3L, 4L, 5L);

            assertThat(list.average()).isEqualTo(3.0);
        }

        @Test
        @DisplayName("average - 空列表抛异常")
        void testAverageEmpty() {
            LongList list = LongList.create();

            assertThatThrownBy(list::average)
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("转换操作测试")
    class ConversionOperationTests {

        @Test
        @DisplayName("toArray - 转为数组")
        void testToArray() {
            LongList list = LongList.of(1L, 2L, 3L);

            long[] array = list.toArray();

            assertThat(array).containsExactly(1L, 2L, 3L);
        }

        @Test
        @DisplayName("stream - 转为流")
        void testStream() {
            LongList list = LongList.of(1L, 2L, 3L, 4L, 5L);

            long sum = list.stream().sum();

            assertThat(sum).isEqualTo(15L);
        }

        @Test
        @DisplayName("sort - 排序")
        void testSort() {
            LongList list = LongList.of(3L, 1L, 4L, 1L, 5L);

            list.sort();

            assertThat(list.toArray()).containsExactly(1L, 1L, 3L, 4L, 5L);
        }

        @Test
        @DisplayName("reverse - 反转")
        void testReverse() {
            LongList list = LongList.of(1L, 2L, 3L, 4L, 5L);

            list.reverse();

            assertThat(list.toArray()).containsExactly(5L, 4L, 3L, 2L, 1L);
        }
    }

    @Nested
    @DisplayName("迭代器测试")
    class IteratorTests {

        @Test
        @DisplayName("primitiveIterator - 原始迭代器")
        void testPrimitiveIterator() {
            LongList list = LongList.of(1L, 2L, 3L);
            PrimitiveIterator.OfLong iterator = list.primitiveIterator();

            List<Long> result = new ArrayList<>();
            while (iterator.hasNext()) {
                result.add(iterator.nextLong());
            }

            assertThat(result).containsExactly(1L, 2L, 3L);
        }

        @Test
        @DisplayName("primitiveIterator - 越界")
        void testPrimitiveIteratorOutOfBounds() {
            LongList list = LongList.of(1L);
            PrimitiveIterator.OfLong iterator = list.primitiveIterator();
            iterator.nextLong();

            assertThatThrownBy(iterator::nextLong)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("iterator - 常规迭代器")
        void testIterator() {
            LongList list = LongList.of(1L, 2L, 3L);
            List<Long> result = new ArrayList<>();

            for (Long value : list) {
                result.add(value);
            }

            assertThat(result).containsExactly(1L, 2L, 3L);
        }

        @Test
        @DisplayName("iterator - 越界")
        void testIteratorOutOfBounds() {
            LongList list = LongList.of(1L);
            Iterator<Long> iterator = list.iterator();
            iterator.next();

            assertThatThrownBy(iterator::next)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("forEach - 遍历")
        void testForEach() {
            LongList list = LongList.of(1L, 2L, 3L);
            List<Long> result = new ArrayList<>();

            list.forEach((LongConsumer) result::add);

            assertThat(result).containsExactly(1L, 2L, 3L);
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等比较")
        void testEquals() {
            LongList list1 = LongList.of(1L, 2L, 3L);
            LongList list2 = LongList.of(1L, 2L, 3L);
            LongList list3 = LongList.of(1L, 2L, 4L);

            assertThat(list1).isEqualTo(list2);
            assertThat(list1).isNotEqualTo(list3);
        }

        @Test
        @DisplayName("equals - 同一引用")
        void testEqualsSameReference() {
            LongList list = LongList.of(1L, 2L, 3L);

            assertThat(list.equals(list)).isTrue();
        }

        @Test
        @DisplayName("equals - 不同大小")
        void testEqualsDifferentSize() {
            LongList list1 = LongList.of(1L, 2L);
            LongList list2 = LongList.of(1L, 2L, 3L);

            assertThat(list1).isNotEqualTo(list2);
        }

        @Test
        @DisplayName("hashCode - 哈希码一致性")
        void testHashCode() {
            LongList list1 = LongList.of(1L, 2L, 3L);
            LongList list2 = LongList.of(1L, 2L, 3L);

            assertThat(list1.hashCode()).isEqualTo(list2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            assertThat(LongList.of(1L, 2L, 3L).toString()).isEqualTo("[1, 2, 3]");
            assertThat(LongList.create().toString()).isEqualTo("[]");
        }
    }

    @Nested
    @DisplayName("容量扩展测试")
    class CapacityTests {

        @Test
        @DisplayName("自动扩容")
        void testAutoExpand() {
            LongList list = LongList.create(2);

            for (int i = 0; i < 100; i++) {
                list.add(i);
            }

            assertThat(list.size()).isEqualTo(100);
            for (int i = 0; i < 100; i++) {
                assertThat(list.get(i)).isEqualTo(i);
            }
        }
    }
}
