package cloud.opencode.base.collections.primitive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.DoubleConsumer;

import static org.assertj.core.api.Assertions.*;

/**
 * DoubleList 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("DoubleList 测试")
class DoubleListTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 创建空列表")
        void testCreate() {
            DoubleList list = DoubleList.create();

            assertThat(list.isEmpty()).isTrue();
            assertThat(list.size()).isZero();
        }

        @Test
        @DisplayName("create - 指定容量")
        void testCreateWithCapacity() {
            DoubleList list = DoubleList.create(100);

            assertThat(list.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("create - 负容量抛异常")
        void testCreateNegativeCapacity() {
            assertThatThrownBy(() -> DoubleList.create(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("of - 从值创建")
        void testOf() {
            DoubleList list = DoubleList.of(1.0, 2.0, 3.0, 4.0, 5.0);

            assertThat(list.size()).isEqualTo(5);
            assertThat(list.get(0)).isEqualTo(1.0);
            assertThat(list.get(4)).isEqualTo(5.0);
        }
    }

    @Nested
    @DisplayName("基本操作测试")
    class BasicOperationTests {

        @Test
        @DisplayName("get - 获取元素")
        void testGet() {
            DoubleList list = DoubleList.of(1.5, 2.5, 3.5);

            assertThat(list.get(0)).isEqualTo(1.5);
            assertThat(list.get(1)).isEqualTo(2.5);
            assertThat(list.get(2)).isEqualTo(3.5);
        }

        @Test
        @DisplayName("get - 索引越界")
        void testGetOutOfBounds() {
            DoubleList list = DoubleList.of(1.0);

            assertThatThrownBy(() -> list.get(-1))
                    .isInstanceOf(IndexOutOfBoundsException.class);

            assertThatThrownBy(() -> list.get(1))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("set - 设置元素")
        void testSet() {
            DoubleList list = DoubleList.of(1.0, 2.0, 3.0);

            double old = list.set(1, 20.0);

            assertThat(old).isEqualTo(2.0);
            assertThat(list.get(1)).isEqualTo(20.0);
        }

        @Test
        @DisplayName("add - 添加元素")
        void testAdd() {
            DoubleList list = DoubleList.create();

            list.add(1.0);
            list.add(2.0);
            list.add(3.0);

            assertThat(list.size()).isEqualTo(3);
            assertThat(list.toArray()).containsExactly(1.0, 2.0, 3.0);
        }

        @Test
        @DisplayName("add - 在索引处添加")
        void testAddAtIndex() {
            DoubleList list = DoubleList.of(1.0, 3.0);

            list.add(1, 2.0);

            assertThat(list.toArray()).containsExactly(1.0, 2.0, 3.0);
        }

        @Test
        @DisplayName("add - 索引越界")
        void testAddAtIndexOutOfBounds() {
            DoubleList list = DoubleList.of(1.0);

            assertThatThrownBy(() -> list.add(-1, 0.0))
                    .isInstanceOf(IndexOutOfBoundsException.class);

            assertThatThrownBy(() -> list.add(5, 0.0))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("addAll - 添加所有")
        void testAddAll() {
            DoubleList list = DoubleList.of(1.0);

            list.addAll(2.0, 3.0, 4.0);

            assertThat(list.toArray()).containsExactly(1.0, 2.0, 3.0, 4.0);
        }

        @Test
        @DisplayName("removeAt - 移除索引处元素")
        void testRemoveAt() {
            DoubleList list = DoubleList.of(1.0, 2.0, 3.0);

            double removed = list.removeAt(1);

            assertThat(removed).isEqualTo(2.0);
            assertThat(list.toArray()).containsExactly(1.0, 3.0);
        }

        @Test
        @DisplayName("remove - 移除值")
        void testRemove() {
            DoubleList list = DoubleList.of(1.0, 2.0, 3.0, 2.0);

            boolean result = list.remove(2.0);

            assertThat(result).isTrue();
            assertThat(list.toArray()).containsExactly(1.0, 3.0, 2.0);
        }

        @Test
        @DisplayName("remove - 值不存在")
        void testRemoveNotFound() {
            DoubleList list = DoubleList.of(1.0, 2.0, 3.0);

            boolean result = list.remove(4.0);

            assertThat(result).isFalse();
            assertThat(list.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("clear - 清空")
        void testClear() {
            DoubleList list = DoubleList.of(1.0, 2.0, 3.0);

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
            DoubleList list = DoubleList.of(1.0, 2.0, 3.0);

            assertThat(list.contains(2.0)).isTrue();
            assertThat(list.contains(4.0)).isFalse();
        }

        @Test
        @DisplayName("indexOf - 首次出现索引")
        void testIndexOf() {
            DoubleList list = DoubleList.of(1.0, 2.0, 3.0, 2.0);

            assertThat(list.indexOf(2.0)).isEqualTo(1);
            assertThat(list.indexOf(4.0)).isEqualTo(-1);
        }

        @Test
        @DisplayName("lastIndexOf - 最后出现索引")
        void testLastIndexOf() {
            DoubleList list = DoubleList.of(1.0, 2.0, 3.0, 2.0);

            assertThat(list.lastIndexOf(2.0)).isEqualTo(3);
            assertThat(list.lastIndexOf(4.0)).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("统计操作测试")
    class StatisticsOperationTests {

        @Test
        @DisplayName("sum - 求和")
        void testSum() {
            DoubleList list = DoubleList.of(1.0, 2.0, 3.0, 4.0, 5.0);

            assertThat(list.sum()).isEqualTo(15.0);
        }

        @Test
        @DisplayName("sum - 空列表")
        void testSumEmpty() {
            DoubleList list = DoubleList.create();

            assertThat(list.sum()).isZero();
        }

        @Test
        @DisplayName("min - 最小值")
        void testMin() {
            DoubleList list = DoubleList.of(3.0, 1.0, 4.0, 1.0, 5.0);

            assertThat(list.min()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("min - 空列表抛异常")
        void testMinEmpty() {
            DoubleList list = DoubleList.create();

            assertThatThrownBy(list::min)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("max - 最大值")
        void testMax() {
            DoubleList list = DoubleList.of(3.0, 1.0, 4.0, 1.0, 5.0);

            assertThat(list.max()).isEqualTo(5.0);
        }

        @Test
        @DisplayName("max - 空列表抛异常")
        void testMaxEmpty() {
            DoubleList list = DoubleList.create();

            assertThatThrownBy(list::max)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("average - 平均值")
        void testAverage() {
            DoubleList list = DoubleList.of(1.0, 2.0, 3.0, 4.0, 5.0);

            assertThat(list.average()).isEqualTo(3.0);
        }

        @Test
        @DisplayName("average - 空列表抛异常")
        void testAverageEmpty() {
            DoubleList list = DoubleList.create();

            assertThatThrownBy(list::average)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("variance - 方差")
        void testVariance() {
            DoubleList list = DoubleList.of(2.0, 4.0, 4.0, 4.0, 5.0, 5.0, 7.0, 9.0);

            assertThat(list.variance()).isCloseTo(4.0, within(0.001));
        }

        @Test
        @DisplayName("variance - 空列表抛异常")
        void testVarianceEmpty() {
            DoubleList list = DoubleList.create();

            assertThatThrownBy(list::variance)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("standardDeviation - 标准差")
        void testStandardDeviation() {
            DoubleList list = DoubleList.of(2.0, 4.0, 4.0, 4.0, 5.0, 5.0, 7.0, 9.0);

            assertThat(list.standardDeviation()).isCloseTo(2.0, within(0.001));
        }
    }

    @Nested
    @DisplayName("转换操作测试")
    class ConversionOperationTests {

        @Test
        @DisplayName("toArray - 转为数组")
        void testToArray() {
            DoubleList list = DoubleList.of(1.0, 2.0, 3.0);

            double[] array = list.toArray();

            assertThat(array).containsExactly(1.0, 2.0, 3.0);
        }

        @Test
        @DisplayName("stream - 转为流")
        void testStream() {
            DoubleList list = DoubleList.of(1.0, 2.0, 3.0, 4.0, 5.0);

            double sum = list.stream().sum();

            assertThat(sum).isEqualTo(15.0);
        }

        @Test
        @DisplayName("sort - 排序")
        void testSort() {
            DoubleList list = DoubleList.of(3.0, 1.0, 4.0, 1.0, 5.0);

            list.sort();

            assertThat(list.toArray()).containsExactly(1.0, 1.0, 3.0, 4.0, 5.0);
        }

        @Test
        @DisplayName("reverse - 反转")
        void testReverse() {
            DoubleList list = DoubleList.of(1.0, 2.0, 3.0, 4.0, 5.0);

            list.reverse();

            assertThat(list.toArray()).containsExactly(5.0, 4.0, 3.0, 2.0, 1.0);
        }
    }

    @Nested
    @DisplayName("迭代器测试")
    class IteratorTests {

        @Test
        @DisplayName("primitiveIterator - 原始迭代器")
        void testPrimitiveIterator() {
            DoubleList list = DoubleList.of(1.0, 2.0, 3.0);
            PrimitiveIterator.OfDouble iterator = list.primitiveIterator();

            List<Double> result = new ArrayList<>();
            while (iterator.hasNext()) {
                result.add(iterator.nextDouble());
            }

            assertThat(result).containsExactly(1.0, 2.0, 3.0);
        }

        @Test
        @DisplayName("primitiveIterator - 越界")
        void testPrimitiveIteratorOutOfBounds() {
            DoubleList list = DoubleList.of(1.0);
            PrimitiveIterator.OfDouble iterator = list.primitiveIterator();
            iterator.nextDouble();

            assertThatThrownBy(iterator::nextDouble)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("iterator - 常规迭代器")
        void testIterator() {
            DoubleList list = DoubleList.of(1.0, 2.0, 3.0);
            List<Double> result = new ArrayList<>();

            for (Double value : list) {
                result.add(value);
            }

            assertThat(result).containsExactly(1.0, 2.0, 3.0);
        }

        @Test
        @DisplayName("iterator - 越界")
        void testIteratorOutOfBounds() {
            DoubleList list = DoubleList.of(1.0);
            Iterator<Double> iterator = list.iterator();
            iterator.next();

            assertThatThrownBy(iterator::next)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("forEach - 遍历")
        void testForEach() {
            DoubleList list = DoubleList.of(1.0, 2.0, 3.0);
            List<Double> result = new ArrayList<>();

            list.forEach((DoubleConsumer) result::add);

            assertThat(result).containsExactly(1.0, 2.0, 3.0);
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等比较")
        void testEquals() {
            DoubleList list1 = DoubleList.of(1.0, 2.0, 3.0);
            DoubleList list2 = DoubleList.of(1.0, 2.0, 3.0);
            DoubleList list3 = DoubleList.of(1.0, 2.0, 4.0);

            assertThat(list1).isEqualTo(list2);
            assertThat(list1).isNotEqualTo(list3);
        }

        @Test
        @DisplayName("equals - 同一引用")
        void testEqualsSameReference() {
            DoubleList list = DoubleList.of(1.0, 2.0, 3.0);

            assertThat(list.equals(list)).isTrue();
        }

        @Test
        @DisplayName("equals - 不同大小")
        void testEqualsDifferentSize() {
            DoubleList list1 = DoubleList.of(1.0, 2.0);
            DoubleList list2 = DoubleList.of(1.0, 2.0, 3.0);

            assertThat(list1).isNotEqualTo(list2);
        }

        @Test
        @DisplayName("hashCode - 哈希码一致性")
        void testHashCode() {
            DoubleList list1 = DoubleList.of(1.0, 2.0, 3.0);
            DoubleList list2 = DoubleList.of(1.0, 2.0, 3.0);

            assertThat(list1.hashCode()).isEqualTo(list2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            assertThat(DoubleList.of(1.0, 2.0, 3.0).toString()).isEqualTo("[1.0, 2.0, 3.0]");
            assertThat(DoubleList.create().toString()).isEqualTo("[]");
        }
    }

    @Nested
    @DisplayName("容量扩展测试")
    class CapacityTests {

        @Test
        @DisplayName("自动扩容")
        void testAutoExpand() {
            DoubleList list = DoubleList.create(2);

            for (int i = 0; i < 100; i++) {
                list.add(i * 1.0);
            }

            assertThat(list.size()).isEqualTo(100);
            for (int i = 0; i < 100; i++) {
                assertThat(list.get(i)).isEqualTo(i * 1.0);
            }
        }
    }
}
