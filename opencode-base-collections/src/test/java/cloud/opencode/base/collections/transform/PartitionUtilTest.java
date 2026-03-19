package cloud.opencode.base.collections.transform;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * PartitionUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("PartitionUtil 测试")
class PartitionUtilTest {

    @Nested
    @DisplayName("谓词分区测试")
    class PredicatePartitionTests {

        @Test
        @DisplayName("partition - 按谓词分区")
        void testPartition() {
            List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

            Map<Boolean, List<Integer>> result = PartitionUtil.partition(numbers, n -> n % 2 == 0);

            assertThat(result.get(true)).containsExactly(2, 4, 6, 8, 10);
            assertThat(result.get(false)).containsExactly(1, 3, 5, 7, 9);
        }

        @Test
        @DisplayName("partition - 空集合")
        void testPartitionEmpty() {
            List<Integer> numbers = List.of();

            Map<Boolean, List<Integer>> result = PartitionUtil.partition(numbers, n -> n % 2 == 0);

            assertThat(result.get(true)).isEmpty();
            assertThat(result.get(false)).isEmpty();
        }

        @Test
        @DisplayName("partition - 全部匹配")
        void testPartitionAllMatch() {
            List<Integer> numbers = List.of(2, 4, 6, 8);

            Map<Boolean, List<Integer>> result = PartitionUtil.partition(numbers, n -> n % 2 == 0);

            assertThat(result.get(true)).containsExactly(2, 4, 6, 8);
            assertThat(result.get(false)).isEmpty();
        }

        @Test
        @DisplayName("partitionBy - 按多个谓词分区")
        void testPartitionBy() {
            List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

            List<List<Integer>> result = PartitionUtil.partitionBy(numbers,
                    n -> n <= 3,
                    n -> n <= 6,
                    n -> n <= 9);

            assertThat(result.get(0)).containsExactly(1, 2, 3);
            assertThat(result.get(1)).containsExactly(4, 5, 6);
            assertThat(result.get(2)).containsExactly(7, 8, 9);
            assertThat(result.get(3)).containsExactly(10);
        }
    }

    @Nested
    @DisplayName("大小分区测试")
    class SizePartitionTests {

        @Test
        @DisplayName("partitionBySize - 列表分区")
        void testPartitionBySize() {
            List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

            List<List<Integer>> result = PartitionUtil.partitionBySize(numbers, 3);

            assertThat(result).hasSize(4);
            assertThat(result.get(0)).containsExactly(1, 2, 3);
            assertThat(result.get(1)).containsExactly(4, 5, 6);
            assertThat(result.get(2)).containsExactly(7, 8, 9);
            assertThat(result.get(3)).containsExactly(10);
        }

        @Test
        @DisplayName("partitionBySize - 整除")
        void testPartitionBySizeExact() {
            List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6);

            List<List<Integer>> result = PartitionUtil.partitionBySize(numbers, 2);

            assertThat(result).hasSize(3);
            assertThat(result.get(0)).containsExactly(1, 2);
            assertThat(result.get(1)).containsExactly(3, 4);
            assertThat(result.get(2)).containsExactly(5, 6);
        }

        @Test
        @DisplayName("partitionBySize - 空列表")
        void testPartitionBySizeEmpty() {
            List<Integer> numbers = List.of();

            List<List<Integer>> result = PartitionUtil.partitionBySize(numbers, 3);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("partitionBySize - 大小为0抛异常")
        void testPartitionBySizeZero() {
            List<Integer> numbers = List.of(1, 2, 3);

            assertThatThrownBy(() -> PartitionUtil.partitionBySize(numbers, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("partitionBySize - 可迭代对象")
        void testPartitionBySizeIterable() {
            Set<Integer> numbers = new LinkedHashSet<>(List.of(1, 2, 3, 4, 5));

            List<List<Integer>> result = PartitionUtil.partitionBySize((Iterable<Integer>) numbers, 2);

            assertThat(result).hasSize(3);
            assertThat(result.get(0)).containsExactly(1, 2);
            assertThat(result.get(1)).containsExactly(3, 4);
            assertThat(result.get(2)).containsExactly(5);
        }

        @Test
        @DisplayName("partitionIntoN - 分区为 N 部分")
        void testPartitionIntoN() {
            List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

            List<List<Integer>> result = PartitionUtil.partitionIntoN(numbers, 3);

            assertThat(result).hasSize(3);
            assertThat(result.get(0)).containsExactly(1, 2, 3, 4);
            assertThat(result.get(1)).containsExactly(5, 6, 7);
            assertThat(result.get(2)).containsExactly(8, 9, 10);
        }

        @Test
        @DisplayName("partitionIntoN - N大于列表大小")
        void testPartitionIntoNLargerThanSize() {
            List<Integer> numbers = List.of(1, 2, 3);

            List<List<Integer>> result = PartitionUtil.partitionIntoN(numbers, 5);

            assertThat(result).hasSize(5);
            assertThat(result.get(0)).containsExactly(1);
            assertThat(result.get(1)).containsExactly(2);
            assertThat(result.get(2)).containsExactly(3);
            assertThat(result.get(3)).isEmpty();
            assertThat(result.get(4)).isEmpty();
        }

        @Test
        @DisplayName("partitionIntoN - N为0抛异常")
        void testPartitionIntoNZero() {
            List<Integer> numbers = List.of(1, 2, 3);

            assertThatThrownBy(() -> PartitionUtil.partitionIntoN(numbers, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("滑动窗口测试")
    class SlidingWindowTests {

        @Test
        @DisplayName("slidingWindow - 默认步长")
        void testSlidingWindow() {
            List<Integer> numbers = List.of(1, 2, 3, 4, 5);

            List<List<Integer>> result = PartitionUtil.slidingWindow(numbers, 3);

            assertThat(result).hasSize(3);
            assertThat(result.get(0)).containsExactly(1, 2, 3);
            assertThat(result.get(1)).containsExactly(2, 3, 4);
            assertThat(result.get(2)).containsExactly(3, 4, 5);
        }

        @Test
        @DisplayName("slidingWindow - 指定步长")
        void testSlidingWindowWithStep() {
            List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8);

            List<List<Integer>> result = PartitionUtil.slidingWindow(numbers, 3, 2);

            assertThat(result).hasSize(3);
            assertThat(result.get(0)).containsExactly(1, 2, 3);
            assertThat(result.get(1)).containsExactly(3, 4, 5);
            assertThat(result.get(2)).containsExactly(5, 6, 7);
        }

        @Test
        @DisplayName("slidingWindow - 窗口大于列表")
        void testSlidingWindowLargerThanList() {
            List<Integer> numbers = List.of(1, 2, 3);

            List<List<Integer>> result = PartitionUtil.slidingWindow(numbers, 5);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("slidingWindow - 窗口大小为0抛异常")
        void testSlidingWindowSizeZero() {
            List<Integer> numbers = List.of(1, 2, 3);

            assertThatThrownBy(() -> PartitionUtil.slidingWindow(numbers, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("slidingWindow - 步长为0抛异常")
        void testSlidingWindowStepZero() {
            List<Integer> numbers = List.of(1, 2, 3);

            assertThatThrownBy(() -> PartitionUtil.slidingWindow(numbers, 2, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("特殊分区测试")
    class SpecialPartitionTests {

        @Test
        @DisplayName("headTail - 头部和尾部")
        void testHeadTail() {
            List<Integer> numbers = List.of(1, 2, 3, 4, 5);

            Map<String, Object> result = PartitionUtil.headTail(numbers);

            assertThat(result.get("head")).isEqualTo(1);
            @SuppressWarnings("unchecked")
            List<Integer> tail = (List<Integer>) result.get("tail");
            assertThat(tail).containsExactly(2, 3, 4, 5);
        }

        @Test
        @DisplayName("headTail - 单元素")
        void testHeadTailSingle() {
            List<Integer> numbers = List.of(1);

            Map<String, Object> result = PartitionUtil.headTail(numbers);

            assertThat(result.get("head")).isEqualTo(1);
            assertThat((List<?>) result.get("tail")).isEmpty();
        }

        @Test
        @DisplayName("headTail - 空列表")
        void testHeadTailEmpty() {
            List<Integer> numbers = List.of();

            Map<String, Object> result = PartitionUtil.headTail(numbers);

            assertThat(result.get("head")).isNull();
            assertThat((List<?>) result.get("tail")).isEmpty();
        }

        @Test
        @DisplayName("initLast - 初始和最后")
        void testInitLast() {
            List<Integer> numbers = List.of(1, 2, 3, 4, 5);

            Map<String, Object> result = PartitionUtil.initLast(numbers);

            @SuppressWarnings("unchecked")
            List<Integer> init = (List<Integer>) result.get("init");
            assertThat(init).containsExactly(1, 2, 3, 4);
            assertThat(result.get("last")).isEqualTo(5);
        }

        @Test
        @DisplayName("initLast - 单元素")
        void testInitLastSingle() {
            List<Integer> numbers = List.of(1);

            Map<String, Object> result = PartitionUtil.initLast(numbers);

            assertThat((List<?>) result.get("init")).isEmpty();
            assertThat(result.get("last")).isEqualTo(1);
        }

        @Test
        @DisplayName("initLast - 空列表")
        void testInitLastEmpty() {
            List<Integer> numbers = List.of();

            Map<String, Object> result = PartitionUtil.initLast(numbers);

            assertThat((List<?>) result.get("init")).isEmpty();
            assertThat(result.get("last")).isNull();
        }

        @Test
        @DisplayName("splitAt - 在索引处分割")
        void testSplitAt() {
            List<Integer> numbers = List.of(1, 2, 3, 4, 5);

            List<List<Integer>> result = PartitionUtil.splitAt(numbers, 3);

            assertThat(result.get(0)).containsExactly(1, 2, 3);
            assertThat(result.get(1)).containsExactly(4, 5);
        }

        @Test
        @DisplayName("splitAt - 在开头分割")
        void testSplitAtStart() {
            List<Integer> numbers = List.of(1, 2, 3);

            List<List<Integer>> result = PartitionUtil.splitAt(numbers, 0);

            assertThat(result.get(0)).isEmpty();
            assertThat(result.get(1)).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("splitAt - 在末尾分割")
        void testSplitAtEnd() {
            List<Integer> numbers = List.of(1, 2, 3);

            List<List<Integer>> result = PartitionUtil.splitAt(numbers, 3);

            assertThat(result.get(0)).containsExactly(1, 2, 3);
            assertThat(result.get(1)).isEmpty();
        }

        @Test
        @DisplayName("splitAt - 索引越界")
        void testSplitAtOutOfBounds() {
            List<Integer> numbers = List.of(1, 2, 3);

            assertThatThrownBy(() -> PartitionUtil.splitAt(numbers, 5))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("takeWhile - 获取满足条件的元素")
        void testTakeWhile() {
            List<Integer> numbers = List.of(1, 2, 3, 4, 5, 1, 2);

            List<Integer> result = PartitionUtil.takeWhile(numbers, n -> n < 4);

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("takeWhile - 全部满足")
        void testTakeWhileAllMatch() {
            List<Integer> numbers = List.of(1, 2, 3);

            List<Integer> result = PartitionUtil.takeWhile(numbers, n -> n < 10);

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("takeWhile - 全部不满足")
        void testTakeWhileNoneMatch() {
            List<Integer> numbers = List.of(1, 2, 3);

            List<Integer> result = PartitionUtil.takeWhile(numbers, n -> n > 10);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("dropWhile - 丢弃满足条件的元素")
        void testDropWhile() {
            List<Integer> numbers = List.of(1, 2, 3, 4, 5, 1, 2);

            List<Integer> result = PartitionUtil.dropWhile(numbers, n -> n < 4);

            assertThat(result).containsExactly(4, 5, 1, 2);
        }

        @Test
        @DisplayName("dropWhile - 全部满足")
        void testDropWhileAllMatch() {
            List<Integer> numbers = List.of(1, 2, 3);

            List<Integer> result = PartitionUtil.dropWhile(numbers, n -> n < 10);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("dropWhile - 全部不满足")
        void testDropWhileNoneMatch() {
            List<Integer> numbers = List.of(1, 2, 3);

            List<Integer> result = PartitionUtil.dropWhile(numbers, n -> n > 10);

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("span - takeWhile 和 dropWhile 组合")
        void testSpan() {
            List<Integer> numbers = List.of(1, 2, 3, 4, 5);

            List<List<Integer>> result = PartitionUtil.span(numbers, n -> n < 4);

            assertThat(result.get(0)).containsExactly(1, 2, 3);
            assertThat(result.get(1)).containsExactly(4, 5);
        }
    }
}
