package cloud.opencode.base.parallel.batch;

import org.junit.jupiter.api.*;

import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * PartitionUtilTest Tests
 * PartitionUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
@DisplayName("PartitionUtil 测试")
class PartitionUtilTest {

    @Nested
    @DisplayName("partition(List)方法测试")
    class PartitionListTests {

        @Test
        @DisplayName("正常分区")
        void testPartition() {
            List<Integer> list = List.of(1, 2, 3, 4, 5, 6, 7);

            List<List<Integer>> result = PartitionUtil.partition(list, 3);

            assertThat(result).hasSize(3);
            assertThat(result.get(0)).containsExactly(1, 2, 3);
            assertThat(result.get(1)).containsExactly(4, 5, 6);
            assertThat(result.get(2)).containsExactly(7);
        }

        @Test
        @DisplayName("刚好整除")
        void testPartitionExact() {
            List<Integer> list = List.of(1, 2, 3, 4, 5, 6);

            List<List<Integer>> result = PartitionUtil.partition(list, 3);

            assertThat(result).hasSize(2);
            assertThat(result.get(0)).containsExactly(1, 2, 3);
            assertThat(result.get(1)).containsExactly(4, 5, 6);
        }

        @Test
        @DisplayName("分区大小大于列表大小")
        void testPartitionSizeLargerThanList() {
            List<Integer> list = List.of(1, 2, 3);

            List<List<Integer>> result = PartitionUtil.partition(list, 10);

            assertThat(result).hasSize(1);
            assertThat(result.get(0)).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("null列表返回空列表")
        void testPartitionNull() {
            List<List<Integer>> result = PartitionUtil.partition((List<Integer>) null, 3);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("空列表返回空列表")
        void testPartitionEmpty() {
            List<List<Integer>> result = PartitionUtil.partition(List.of(), 3);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("分区大小为0抛出异常")
        void testPartitionSizeZero() {
            List<Integer> list = List.of(1, 2, 3);

            assertThatThrownBy(() -> PartitionUtil.partition(list, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("分区大小为负数抛出异常")
        void testPartitionSizeNegative() {
            List<Integer> list = List.of(1, 2, 3);

            assertThatThrownBy(() -> PartitionUtil.partition(list, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("partition(Collection)方法测试")
    class PartitionCollectionTests {

        @Test
        @DisplayName("List类型直接委托")
        void testPartitionList() {
            List<Integer> list = List.of(1, 2, 3, 4, 5);

            List<List<Integer>> result = PartitionUtil.partition((Collection<Integer>) list, 2);

            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("非List类型转换后分区")
        void testPartitionSet() {
            Set<Integer> set = new LinkedHashSet<>(List.of(1, 2, 3, 4, 5));

            List<List<Integer>> result = PartitionUtil.partition(set, 2);

            assertThat(result).hasSize(3);
        }
    }

    @Nested
    @DisplayName("partitionStream方法测试")
    class PartitionStreamTests {

        @Test
        @DisplayName("创建分区流")
        void testPartitionStream() {
            List<Integer> list = List.of(1, 2, 3, 4, 5, 6, 7);

            Stream<List<Integer>> stream = PartitionUtil.partitionStream(list, 3);
            List<List<Integer>> result = stream.toList();

            assertThat(result).hasSize(3);
            assertThat(result.get(0)).containsExactly(1, 2, 3);
            assertThat(result.get(1)).containsExactly(4, 5, 6);
            assertThat(result.get(2)).containsExactly(7);
        }

        @Test
        @DisplayName("null列表返回空流")
        void testPartitionStreamNull() {
            Stream<List<Integer>> stream = PartitionUtil.partitionStream(null, 3);

            assertThat(stream.toList()).isEmpty();
        }

        @Test
        @DisplayName("空列表返回空流")
        void testPartitionStreamEmpty() {
            Stream<List<Integer>> stream = PartitionUtil.partitionStream(List.of(), 3);

            assertThat(stream.toList()).isEmpty();
        }

        @Test
        @DisplayName("分区大小为0抛出异常")
        void testPartitionStreamSizeZero() {
            List<Integer> list = List.of(1, 2, 3);

            assertThatThrownBy(() -> PartitionUtil.partitionStream(list, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("partitionInto方法测试")
    class PartitionIntoTests {

        @Test
        @DisplayName("分割成指定数量的分区")
        void testPartitionInto() {
            List<Integer> list = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

            List<List<Integer>> result = PartitionUtil.partitionInto(list, 3);

            assertThat(result).hasSize(3);
            // ceiling(10/3) = 4, so each partition should have at most 4 elements
        }

        @Test
        @DisplayName("分区数量大于元素数量")
        void testPartitionIntoMoreThanElements() {
            List<Integer> list = List.of(1, 2, 3);

            List<List<Integer>> result = PartitionUtil.partitionInto(list, 10);

            // With 3 elements and 10 partitions, size = ceil(3/10) = 1
            // So we get 3 partitions of size 1
            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("null列表返回空列表")
        void testPartitionIntoNull() {
            List<List<Integer>> result = PartitionUtil.partitionInto(null, 3);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("空列表返回空列表")
        void testPartitionIntoEmpty() {
            List<List<Integer>> result = PartitionUtil.partitionInto(List.of(), 3);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("分区数量为0抛出异常")
        void testPartitionIntoZero() {
            List<Integer> list = List.of(1, 2, 3);

            assertThatThrownBy(() -> PartitionUtil.partitionInto(list, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }
    }

    @Nested
    @DisplayName("partitionCount方法测试")
    class PartitionCountTests {

        @Test
        @DisplayName("计算分区数量")
        void testPartitionCount() {
            assertThat(PartitionUtil.partitionCount(10, 3)).isEqualTo(4);
            assertThat(PartitionUtil.partitionCount(9, 3)).isEqualTo(3);
            assertThat(PartitionUtil.partitionCount(1, 3)).isEqualTo(1);
        }

        @Test
        @DisplayName("总大小为0返回0")
        void testPartitionCountZeroTotal() {
            assertThat(PartitionUtil.partitionCount(0, 3)).isZero();
        }

        @Test
        @DisplayName("总大小为负数返回0")
        void testPartitionCountNegativeTotal() {
            assertThat(PartitionUtil.partitionCount(-5, 3)).isZero();
        }

        @Test
        @DisplayName("分区大小为0抛出异常")
        void testPartitionCountZeroSize() {
            assertThatThrownBy(() -> PartitionUtil.partitionCount(10, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("分区大小为负数抛出异常")
        void testPartitionCountNegativeSize() {
            assertThatThrownBy(() -> PartitionUtil.partitionCount(10, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("getPartition方法测试")
    class GetPartitionTests {

        @Test
        @DisplayName("获取特定分区")
        void testGetPartition() {
            List<Integer> list = List.of(1, 2, 3, 4, 5, 6, 7);

            List<Integer> partition0 = PartitionUtil.getPartition(list, 3, 0);
            List<Integer> partition1 = PartitionUtil.getPartition(list, 3, 1);
            List<Integer> partition2 = PartitionUtil.getPartition(list, 3, 2);

            assertThat(partition0).containsExactly(1, 2, 3);
            assertThat(partition1).containsExactly(4, 5, 6);
            assertThat(partition2).containsExactly(7);
        }

        @Test
        @DisplayName("索引超出范围返回空列表")
        void testGetPartitionOutOfRange() {
            List<Integer> list = List.of(1, 2, 3);

            List<Integer> partition = PartitionUtil.getPartition(list, 3, 10);

            assertThat(partition).isEmpty();
        }

        @Test
        @DisplayName("null列表返回空列表")
        void testGetPartitionNull() {
            List<Integer> result = PartitionUtil.getPartition(null, 3, 0);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("空列表返回空列表")
        void testGetPartitionEmpty() {
            List<Integer> result = PartitionUtil.getPartition(List.of(), 3, 0);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("分区大小为0抛出异常")
        void testGetPartitionSizeZero() {
            List<Integer> list = List.of(1, 2, 3);

            assertThatThrownBy(() -> PartitionUtil.getPartition(list, 0, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("分区索引为负数抛出异常")
        void testGetPartitionNegativeIndex() {
            List<Integer> list = List.of(1, 2, 3);

            assertThatThrownBy(() -> PartitionUtil.getPartition(list, 3, -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("non-negative");
        }
    }

    @Nested
    @DisplayName("PartitionIterator测试")
    class PartitionIteratorTests {

        @Test
        @DisplayName("迭代器正确遍历")
        void testIterator() {
            List<Integer> list = List.of(1, 2, 3, 4, 5);

            List<List<Integer>> result = new ArrayList<>();
            PartitionUtil.partitionStream(list, 2).forEach(result::add);

            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("hasNext在空时返回false")
        void testIteratorHasNext() {
            Stream<List<Integer>> stream = PartitionUtil.partitionStream(List.of(1), 10);

            Iterator<List<Integer>> iterator = stream.iterator();
            assertThat(iterator.hasNext()).isTrue();
            iterator.next();
            assertThat(iterator.hasNext()).isFalse();
        }

        @Test
        @DisplayName("next在无元素时抛出NoSuchElementException")
        void testIteratorNextThrows() {
            Stream<List<Integer>> stream = PartitionUtil.partitionStream(List.of(1), 10);

            Iterator<List<Integer>> iterator = stream.iterator();
            iterator.next();

            assertThatThrownBy(iterator::next)
                    .isInstanceOf(NoSuchElementException.class);
        }
    }
}
