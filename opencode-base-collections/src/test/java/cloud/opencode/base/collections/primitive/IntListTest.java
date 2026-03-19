package cloud.opencode.base.collections.primitive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.IntConsumer;

import static org.assertj.core.api.Assertions.*;

/**
 * IntList 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("IntList 测试")
class IntListTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 创建空列表")
        void testCreate() {
            IntList list = IntList.create();

            assertThat(list.isEmpty()).isTrue();
            assertThat(list.size()).isZero();
        }

        @Test
        @DisplayName("create - 指定容量")
        void testCreateWithCapacity() {
            IntList list = IntList.create(100);

            assertThat(list.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("create - 负容量抛异常")
        void testCreateNegativeCapacity() {
            assertThatThrownBy(() -> IntList.create(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("of - 从值创建")
        void testOf() {
            IntList list = IntList.of(1, 2, 3, 4, 5);

            assertThat(list.size()).isEqualTo(5);
            assertThat(list.get(0)).isEqualTo(1);
            assertThat(list.get(4)).isEqualTo(5);
        }

        @Test
        @DisplayName("range - 从范围创建")
        void testRange() {
            IntList list = IntList.range(0, 5);

            assertThat(list.size()).isEqualTo(5);
            assertThat(list.toArray()).containsExactly(0, 1, 2, 3, 4);
        }

        @Test
        @DisplayName("range - 开始大于结束抛异常")
        void testRangeInvalid() {
            assertThatThrownBy(() -> IntList.range(5, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("基本操作测试")
    class BasicOperationTests {

        @Test
        @DisplayName("get - 获取元素")
        void testGet() {
            IntList list = IntList.of(10, 20, 30);

            assertThat(list.get(0)).isEqualTo(10);
            assertThat(list.get(1)).isEqualTo(20);
            assertThat(list.get(2)).isEqualTo(30);
        }

        @Test
        @DisplayName("get - 索引越界")
        void testGetOutOfBounds() {
            IntList list = IntList.of(1);

            assertThatThrownBy(() -> list.get(-1))
                    .isInstanceOf(IndexOutOfBoundsException.class);

            assertThatThrownBy(() -> list.get(1))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("set - 设置元素")
        void testSet() {
            IntList list = IntList.of(1, 2, 3);

            int old = list.set(1, 20);

            assertThat(old).isEqualTo(2);
            assertThat(list.get(1)).isEqualTo(20);
        }

        @Test
        @DisplayName("add - 添加元素")
        void testAdd() {
            IntList list = IntList.create();

            list.add(1);
            list.add(2);
            list.add(3);

            assertThat(list.size()).isEqualTo(3);
            assertThat(list.toArray()).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("add - 在索引处添加")
        void testAddAtIndex() {
            IntList list = IntList.of(1, 3);

            list.add(1, 2);

            assertThat(list.toArray()).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("add - 索引越界")
        void testAddAtIndexOutOfBounds() {
            IntList list = IntList.of(1);

            assertThatThrownBy(() -> list.add(-1, 0))
                    .isInstanceOf(IndexOutOfBoundsException.class);

            assertThatThrownBy(() -> list.add(5, 0))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("addAll - 添加所有")
        void testAddAll() {
            IntList list = IntList.of(1);

            list.addAll(2, 3, 4);

            assertThat(list.toArray()).containsExactly(1, 2, 3, 4);
        }

        @Test
        @DisplayName("removeAt - 移除索引处元素")
        void testRemoveAt() {
            IntList list = IntList.of(1, 2, 3);

            int removed = list.removeAt(1);

            assertThat(removed).isEqualTo(2);
            assertThat(list.toArray()).containsExactly(1, 3);
        }

        @Test
        @DisplayName("remove - 移除值")
        void testRemove() {
            IntList list = IntList.of(1, 2, 3, 2);

            boolean result = list.remove(2);

            assertThat(result).isTrue();
            assertThat(list.toArray()).containsExactly(1, 3, 2);
        }

        @Test
        @DisplayName("remove - 值不存在")
        void testRemoveNotFound() {
            IntList list = IntList.of(1, 2, 3);

            boolean result = list.remove(4);

            assertThat(result).isFalse();
            assertThat(list.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("clear - 清空")
        void testClear() {
            IntList list = IntList.of(1, 2, 3);

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
            IntList list = IntList.of(1, 2, 3);

            assertThat(list.contains(2)).isTrue();
            assertThat(list.contains(4)).isFalse();
        }

        @Test
        @DisplayName("indexOf - 首次出现索引")
        void testIndexOf() {
            IntList list = IntList.of(1, 2, 3, 2);

            assertThat(list.indexOf(2)).isEqualTo(1);
            assertThat(list.indexOf(4)).isEqualTo(-1);
        }

        @Test
        @DisplayName("lastIndexOf - 最后出现索引")
        void testLastIndexOf() {
            IntList list = IntList.of(1, 2, 3, 2);

            assertThat(list.lastIndexOf(2)).isEqualTo(3);
            assertThat(list.lastIndexOf(4)).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("统计操作测试")
    class StatisticsOperationTests {

        @Test
        @DisplayName("sum - 求和")
        void testSum() {
            IntList list = IntList.of(1, 2, 3, 4, 5);

            assertThat(list.sum()).isEqualTo(15);
        }

        @Test
        @DisplayName("sum - 空列表")
        void testSumEmpty() {
            IntList list = IntList.create();

            assertThat(list.sum()).isZero();
        }

        @Test
        @DisplayName("min - 最小值")
        void testMin() {
            IntList list = IntList.of(3, 1, 4, 1, 5);

            assertThat(list.min()).isEqualTo(1);
        }

        @Test
        @DisplayName("min - 空列表抛异常")
        void testMinEmpty() {
            IntList list = IntList.create();

            assertThatThrownBy(list::min)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("max - 最大值")
        void testMax() {
            IntList list = IntList.of(3, 1, 4, 1, 5);

            assertThat(list.max()).isEqualTo(5);
        }

        @Test
        @DisplayName("max - 空列表抛异常")
        void testMaxEmpty() {
            IntList list = IntList.create();

            assertThatThrownBy(list::max)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("average - 平均值")
        void testAverage() {
            IntList list = IntList.of(1, 2, 3, 4, 5);

            assertThat(list.average()).isEqualTo(3.0);
        }

        @Test
        @DisplayName("average - 空列表抛异常")
        void testAverageEmpty() {
            IntList list = IntList.create();

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
            IntList list = IntList.of(1, 2, 3);

            int[] array = list.toArray();

            assertThat(array).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("stream - 转为流")
        void testStream() {
            IntList list = IntList.of(1, 2, 3, 4, 5);

            int sum = list.stream().sum();

            assertThat(sum).isEqualTo(15);
        }

        @Test
        @DisplayName("sort - 排序")
        void testSort() {
            IntList list = IntList.of(3, 1, 4, 1, 5);

            list.sort();

            assertThat(list.toArray()).containsExactly(1, 1, 3, 4, 5);
        }

        @Test
        @DisplayName("reverse - 反转")
        void testReverse() {
            IntList list = IntList.of(1, 2, 3, 4, 5);

            list.reverse();

            assertThat(list.toArray()).containsExactly(5, 4, 3, 2, 1);
        }
    }

    @Nested
    @DisplayName("迭代器测试")
    class IteratorTests {

        @Test
        @DisplayName("primitiveIterator - 原始迭代器")
        void testPrimitiveIterator() {
            IntList list = IntList.of(1, 2, 3);
            PrimitiveIterator.OfInt iterator = list.primitiveIterator();

            List<Integer> result = new ArrayList<>();
            while (iterator.hasNext()) {
                result.add(iterator.nextInt());
            }

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("primitiveIterator - 越界")
        void testPrimitiveIteratorOutOfBounds() {
            IntList list = IntList.of(1);
            PrimitiveIterator.OfInt iterator = list.primitiveIterator();
            iterator.nextInt();

            assertThatThrownBy(iterator::nextInt)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("iterator - 常规迭代器")
        void testIterator() {
            IntList list = IntList.of(1, 2, 3);
            List<Integer> result = new ArrayList<>();

            for (Integer value : list) {
                result.add(value);
            }

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("iterator - 越界")
        void testIteratorOutOfBounds() {
            IntList list = IntList.of(1);
            Iterator<Integer> iterator = list.iterator();
            iterator.next();

            assertThatThrownBy(iterator::next)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("forEach - 遍历")
        void testForEach() {
            IntList list = IntList.of(1, 2, 3);
            List<Integer> result = new ArrayList<>();

            list.forEach((IntConsumer) result::add);

            assertThat(result).containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等比较")
        void testEquals() {
            IntList list1 = IntList.of(1, 2, 3);
            IntList list2 = IntList.of(1, 2, 3);
            IntList list3 = IntList.of(1, 2, 4);

            assertThat(list1).isEqualTo(list2);
            assertThat(list1).isNotEqualTo(list3);
        }

        @Test
        @DisplayName("equals - 同一引用")
        void testEqualsSameReference() {
            IntList list = IntList.of(1, 2, 3);

            assertThat(list.equals(list)).isTrue();
        }

        @Test
        @DisplayName("equals - 不同大小")
        void testEqualsDifferentSize() {
            IntList list1 = IntList.of(1, 2);
            IntList list2 = IntList.of(1, 2, 3);

            assertThat(list1).isNotEqualTo(list2);
        }

        @Test
        @DisplayName("hashCode - 哈希码一致性")
        void testHashCode() {
            IntList list1 = IntList.of(1, 2, 3);
            IntList list2 = IntList.of(1, 2, 3);

            assertThat(list1.hashCode()).isEqualTo(list2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            assertThat(IntList.of(1, 2, 3).toString()).isEqualTo("[1, 2, 3]");
            assertThat(IntList.create().toString()).isEqualTo("[]");
        }
    }

    @Nested
    @DisplayName("容量扩展测试")
    class CapacityTests {

        @Test
        @DisplayName("自动扩容")
        void testAutoExpand() {
            IntList list = IntList.create(2);

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
