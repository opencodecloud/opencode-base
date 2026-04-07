package cloud.opencode.base.collections.primitive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * FloatList 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
@DisplayName("FloatList 测试")
class FloatListTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 创建空列表")
        void testCreate() {
            FloatList list = FloatList.create();

            assertThat(list.isEmpty()).isTrue();
            assertThat(list.size()).isZero();
        }

        @Test
        @DisplayName("create - 指定容量")
        void testCreateWithCapacity() {
            FloatList list = FloatList.create(100);

            assertThat(list.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("create - 负容量抛异常")
        void testCreateNegativeCapacity() {
            assertThatThrownBy(() -> FloatList.create(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("of - 从值创建")
        void testOf() {
            FloatList list = FloatList.of(1.0f, 2.0f, 3.0f, 4.0f, 5.0f);

            assertThat(list.size()).isEqualTo(5);
            assertThat(list.get(0)).isEqualTo(1.0f);
            assertThat(list.get(4)).isEqualTo(5.0f);
        }

        @Test
        @DisplayName("from - 从 float 数组创建")
        void testFromArray() {
            float[] array = {1.0f, 2.0f, 3.0f};
            FloatList list = FloatList.from(array);

            assertThat(list.size()).isEqualTo(3);
            assertThat(list.get(0)).isEqualTo(1.0f);
        }

        @Test
        @DisplayName("from - 从 Collection 创建")
        void testFromCollection() {
            List<Float> source = List.of(1.0f, 2.0f, 3.0f);
            FloatList list = FloatList.from(source);

            assertThat(list.size()).isEqualTo(3);
            assertThat(list.get(0)).isEqualTo(1.0f);
        }
    }

    @Nested
    @DisplayName("基本操作测试")
    class BasicOperationTests {

        @Test
        @DisplayName("get - 获取元素")
        void testGet() {
            FloatList list = FloatList.of(1.5f, 2.5f, 3.5f);

            assertThat(list.get(0)).isEqualTo(1.5f);
            assertThat(list.get(1)).isEqualTo(2.5f);
            assertThat(list.get(2)).isEqualTo(3.5f);
        }

        @Test
        @DisplayName("get - 索引越界")
        void testGetOutOfBounds() {
            FloatList list = FloatList.of(1.0f);

            assertThatThrownBy(() -> list.get(-1))
                    .isInstanceOf(IndexOutOfBoundsException.class);

            assertThatThrownBy(() -> list.get(1))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("set - 设置元素")
        void testSet() {
            FloatList list = FloatList.of(1.0f, 2.0f, 3.0f);

            float old = list.set(1, 20.0f);

            assertThat(old).isEqualTo(2.0f);
            assertThat(list.get(1)).isEqualTo(20.0f);
        }

        @Test
        @DisplayName("add - 添加元素")
        void testAdd() {
            FloatList list = FloatList.create();

            list.add(1.0f);
            list.add(2.0f);
            list.add(3.0f);

            assertThat(list.size()).isEqualTo(3);
            assertThat(list.toFloatArray()).containsExactly(1.0f, 2.0f, 3.0f);
        }

        @Test
        @DisplayName("addAll - 添加所有")
        void testAddAll() {
            FloatList list = FloatList.of(1.0f);

            list.addAll(2.0f, 3.0f, 4.0f);

            assertThat(list.toFloatArray()).containsExactly(1.0f, 2.0f, 3.0f, 4.0f);
        }

        @Test
        @DisplayName("clear - 清空")
        void testClear() {
            FloatList list = FloatList.of(1.0f, 2.0f, 3.0f);

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
            FloatList list = FloatList.of(1.0f, 2.0f, 3.0f);

            assertThat(list.contains(2.0f)).isTrue();
            assertThat(list.contains(4.0f)).isFalse();
        }

        @Test
        @DisplayName("indexOf - 首次出现索引")
        void testIndexOf() {
            FloatList list = FloatList.of(1.0f, 2.0f, 3.0f, 2.0f);

            assertThat(list.indexOf(2.0f)).isEqualTo(1);
            assertThat(list.indexOf(4.0f)).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("统计操作测试")
    class StatisticsOperationTests {

        @Test
        @DisplayName("sum - 求和")
        void testSum() {
            FloatList list = FloatList.of(1.0f, 2.0f, 3.0f, 4.0f, 5.0f);

            assertThat(list.sum()).isEqualTo(15.0f);
        }

        @Test
        @DisplayName("sum - 空列表")
        void testSumEmpty() {
            FloatList list = FloatList.create();

            assertThat(list.sum()).isZero();
        }

        @Test
        @DisplayName("min - 最小值")
        void testMin() {
            FloatList list = FloatList.of(3.0f, 1.0f, 4.0f, 1.0f, 5.0f);

            assertThat(list.min()).isEqualTo(1.0f);
        }

        @Test
        @DisplayName("min - 空列表抛异常")
        void testMinEmpty() {
            FloatList list = FloatList.create();

            assertThatThrownBy(list::min)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("max - 最大值")
        void testMax() {
            FloatList list = FloatList.of(3.0f, 1.0f, 4.0f, 1.0f, 5.0f);

            assertThat(list.max()).isEqualTo(5.0f);
        }

        @Test
        @DisplayName("max - 空列表抛异常")
        void testMaxEmpty() {
            FloatList list = FloatList.create();

            assertThatThrownBy(list::max)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("average - 平均值")
        void testAverage() {
            FloatList list = FloatList.of(1.0f, 2.0f, 3.0f, 4.0f, 5.0f);

            assertThat(list.average()).isEqualTo(3.0f);
        }

        @Test
        @DisplayName("average - 空列表抛异常")
        void testAverageEmpty() {
            FloatList list = FloatList.create();

            assertThatThrownBy(list::average)
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("转换操作测试")
    class ConversionOperationTests {

        @Test
        @DisplayName("toFloatArray - 转为数组")
        void testToFloatArray() {
            FloatList list = FloatList.of(1.0f, 2.0f, 3.0f);

            float[] array = list.toFloatArray();

            assertThat(array).containsExactly(1.0f, 2.0f, 3.0f);
        }

        @Test
        @DisplayName("toList - 转为 List<Float>")
        void testToList() {
            FloatList list = FloatList.of(1.0f, 2.0f, 3.0f);

            List<Float> boxed = list.toList();

            assertThat(boxed).containsExactly(1.0f, 2.0f, 3.0f);
        }

        @Test
        @DisplayName("sort - 排序")
        void testSort() {
            FloatList list = FloatList.of(3.0f, 1.0f, 4.0f, 1.0f, 5.0f);

            list.sort();

            assertThat(list.toFloatArray()).containsExactly(1.0f, 1.0f, 3.0f, 4.0f, 5.0f);
        }

        @Test
        @DisplayName("reverse - 反转")
        void testReverse() {
            FloatList list = FloatList.of(1.0f, 2.0f, 3.0f, 4.0f, 5.0f);

            list.reverse();

            assertThat(list.toFloatArray()).containsExactly(5.0f, 4.0f, 3.0f, 2.0f, 1.0f);
        }

        @Test
        @DisplayName("subList - 子列表")
        void testSubList() {
            FloatList list = FloatList.of(1.0f, 2.0f, 3.0f, 4.0f, 5.0f);

            FloatList sub = list.subList(1, 4);

            assertThat(sub.toFloatArray()).containsExactly(2.0f, 3.0f, 4.0f);
        }

        @Test
        @DisplayName("subList - 越界抛异常")
        void testSubListOutOfBounds() {
            FloatList list = FloatList.of(1.0f, 2.0f, 3.0f);

            assertThatThrownBy(() -> list.subList(-1, 2))
                    .isInstanceOf(IndexOutOfBoundsException.class);

            assertThatThrownBy(() -> list.subList(0, 10))
                    .isInstanceOf(IndexOutOfBoundsException.class);

            assertThatThrownBy(() -> list.subList(2, 1))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }
    }

    @Nested
    @DisplayName("迭代测试")
    class IterationTests {

        @Test
        @DisplayName("forEach - 遍历")
        void testForEach() {
            FloatList list = FloatList.of(1.0f, 2.0f, 3.0f);
            List<Float> result = new ArrayList<>();

            list.forEach(result::add);

            assertThat(result).containsExactly(1.0f, 2.0f, 3.0f);
        }
    }

    @Nested
    @DisplayName("边界值测试")
    class BoundaryTests {

        @Test
        @DisplayName("NaN 处理")
        void testNaN() {
            FloatList list = FloatList.of(1.0f, Float.NaN, 3.0f);

            assertThat(list.contains(Float.NaN)).isTrue();
            assertThat(list.indexOf(Float.NaN)).isEqualTo(1);
        }

        @Test
        @DisplayName("+0.0f 和 -0.0f")
        void testPositiveAndNegativeZero() {
            FloatList list = FloatList.of(0.0f, -0.0f);

            // Float.compare treats +0.0f and -0.0f as different
            assertThat(Float.compare(0.0f, -0.0f)).isNotZero();
            assertThat(list.indexOf(0.0f)).isEqualTo(0);
            assertThat(list.indexOf(-0.0f)).isEqualTo(1);
        }

        @Test
        @DisplayName("MIN_VALUE 和 MAX_VALUE")
        void testMinMaxValue() {
            FloatList list = FloatList.of(Float.MIN_VALUE, Float.MAX_VALUE);

            assertThat(list.contains(Float.MIN_VALUE)).isTrue();
            assertThat(list.contains(Float.MAX_VALUE)).isTrue();
        }

        @Test
        @DisplayName("POSITIVE_INFINITY 和 NEGATIVE_INFINITY")
        void testInfinity() {
            FloatList list = FloatList.of(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY);

            assertThat(list.contains(Float.POSITIVE_INFINITY)).isTrue();
            assertThat(list.contains(Float.NEGATIVE_INFINITY)).isTrue();
        }
    }

    @Nested
    @DisplayName("容量扩展测试")
    class CapacityTests {

        @Test
        @DisplayName("自动扩容")
        void testAutoExpand() {
            FloatList list = FloatList.create(2);

            for (int i = 0; i < 100; i++) {
                list.add(i * 1.0f);
            }

            assertThat(list.size()).isEqualTo(100);
            for (int i = 0; i < 100; i++) {
                assertThat(list.get(i)).isEqualTo(i * 1.0f);
            }
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等比较")
        void testEquals() {
            FloatList list1 = FloatList.of(1.0f, 2.0f, 3.0f);
            FloatList list2 = FloatList.of(1.0f, 2.0f, 3.0f);
            FloatList list3 = FloatList.of(1.0f, 2.0f, 4.0f);

            assertThat(list1).isEqualTo(list2);
            assertThat(list1).isNotEqualTo(list3);
        }

        @Test
        @DisplayName("equals - 同一引用")
        void testEqualsSameReference() {
            FloatList list = FloatList.of(1.0f, 2.0f, 3.0f);

            assertThat(list.equals(list)).isTrue();
        }

        @Test
        @DisplayName("equals - 不同大小")
        void testEqualsDifferentSize() {
            FloatList list1 = FloatList.of(1.0f, 2.0f);
            FloatList list2 = FloatList.of(1.0f, 2.0f, 3.0f);

            assertThat(list1).isNotEqualTo(list2);
        }

        @Test
        @DisplayName("hashCode - 哈希码一致性")
        void testHashCode() {
            FloatList list1 = FloatList.of(1.0f, 2.0f, 3.0f);
            FloatList list2 = FloatList.of(1.0f, 2.0f, 3.0f);

            assertThat(list1.hashCode()).isEqualTo(list2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            assertThat(FloatList.of(1.0f, 2.0f, 3.0f).toString()).isEqualTo("[1.0, 2.0, 3.0]");
            assertThat(FloatList.create().toString()).isEqualTo("[]");
        }
    }
}
