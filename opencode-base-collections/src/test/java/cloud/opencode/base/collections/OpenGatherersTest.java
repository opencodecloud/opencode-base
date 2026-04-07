package cloud.opencode.base.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenGatherers 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("OpenGatherers 测试")
class OpenGatherersTest {

    @Nested
    @DisplayName("窗口操作测试")
    class WindowOperationsTests {

        @Test
        @DisplayName("windowFixed - 固定窗口")
        void testWindowFixed() {
            List<List<Integer>> result = Stream.of(1, 2, 3, 4, 5, 6)
                    .gather(OpenGatherers.windowFixed(2))
                    .toList();

            assertThat(result).hasSize(3);
            assertThat(result.get(0)).containsExactly(1, 2);
            assertThat(result.get(1)).containsExactly(3, 4);
            assertThat(result.get(2)).containsExactly(5, 6);
        }

        @Test
        @DisplayName("windowSliding - 滑动窗口")
        void testWindowSliding() {
            List<List<Integer>> result = Stream.of(1, 2, 3, 4)
                    .gather(OpenGatherers.windowSliding(2))
                    .toList();

            assertThat(result).hasSize(3);
            assertThat(result.get(0)).containsExactly(1, 2);
            assertThat(result.get(1)).containsExactly(2, 3);
            assertThat(result.get(2)).containsExactly(3, 4);
        }

        @Test
        @DisplayName("windowSliding - 自定义步长")
        void testWindowSlidingWithStep() {
            List<List<Integer>> result = Stream.of(1, 2, 3, 4, 5, 6)
                    .gather(OpenGatherers.windowSliding(3, 2))
                    .toList();

            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("windowSliding - 无效窗口大小抛异常")
        void testWindowSlidingInvalidSize() {
            assertThatThrownBy(() -> OpenGatherers.windowSliding(0, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("windowSliding - 无效步长抛异常")
        void testWindowSlidingInvalidStep() {
            assertThatThrownBy(() -> OpenGatherers.windowSliding(2, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("批处理测试")
    class BatchingTests {

        @Test
        @DisplayName("batch - 批处理")
        void testBatch() {
            List<List<Integer>> result = Stream.of(1, 2, 3, 4, 5)
                    .gather(OpenGatherers.batch(2))
                    .toList();

            assertThat(result).hasSize(3);
            assertThat(result.get(0)).containsExactly(1, 2);
            assertThat(result.get(1)).containsExactly(3, 4);
            assertThat(result.get(2)).containsExactly(5);
        }

        @Test
        @DisplayName("batchProcess - 带处理函数的批处理")
        void testBatchProcess() {
            List<Integer> result = Stream.of(1, 2, 3, 4, 5, 6)
                    .gather(OpenGatherers.batchProcess(2, batch -> batch.stream().mapToInt(Integer::intValue).sum()))
                    .toList();

            assertThat(result).containsExactly(3, 7, 11);
        }

        @Test
        @DisplayName("batchProcess - 无效批大小抛异常")
        void testBatchProcessInvalidSize() {
            assertThatThrownBy(() -> OpenGatherers.batchProcess(0, list -> list))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("batchProcess - null处理器抛异常")
        void testBatchProcessNullProcessor() {
            assertThatThrownBy(() -> OpenGatherers.batchProcess(2, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("去重操作测试")
    class DistinctTests {

        @Test
        @DisplayName("distinctBy - 按键去重")
        void testDistinctBy() {
            List<String> result = Stream.of("apple", "ant", "banana", "ball")
                    .gather(OpenGatherers.distinctBy(s -> s.charAt(0)))
                    .toList();

            assertThat(result).containsExactly("apple", "banana");
        }

        @Test
        @DisplayName("distinctBy - null键提取器抛异常")
        void testDistinctByNullExtractor() {
            assertThatThrownBy(() -> OpenGatherers.distinctBy(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("distinctBy - 自定义相等性")
        void testDistinctByCustomEquality() {
            List<String> result = Stream.of("A", "a", "B", "b")
                    .gather(OpenGatherers.distinctBy(
                            s -> s,
                            (a, b) -> a.equalsIgnoreCase(b),
                            s -> s.toLowerCase().hashCode()))
                    .toList();

            assertThat(result).containsExactly("A", "B");
        }
    }

    @Nested
    @DisplayName("扫描操作测试")
    class ScanTests {

        @Test
        @DisplayName("scan - 运行累积")
        void testScan() {
            List<Integer> result = Stream.of(1, 2, 3, 4)
                    .gather(OpenGatherers.scan(0, Integer::sum))
                    .toList();

            assertThat(result).containsExactly(1, 3, 6, 10);
        }

        @Test
        @DisplayName("scan - null累加器抛异常")
        void testScanNullAccumulator() {
            assertThatThrownBy(() -> OpenGatherers.scan(0, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("scan - 带完成器")
        void testScanWithFinisher() {
            List<String> result = Stream.of(1, 2, 3)
                    .gather(OpenGatherers.scan(
                            () -> 0,
                            Integer::sum,
                            sum -> "Sum: " + sum))
                    .toList();

            assertThat(result).containsExactly("Sum: 1", "Sum: 3", "Sum: 6");
        }
    }

    @Nested
    @DisplayName("带状态过滤测试")
    class StatefulFilterTests {

        @Test
        @DisplayName("takeWhileIndexed - 带索引的takeWhile")
        void testTakeWhileIndexed() {
            List<String> result = Stream.of("a", "b", "c", "d")
                    .gather(OpenGatherers.takeWhileIndexed((i, e) -> i < 2))
                    .toList();

            assertThat(result).containsExactly("a", "b");
        }

        @Test
        @DisplayName("dropWhileIndexed - 带索引的dropWhile")
        void testDropWhileIndexed() {
            List<String> result = Stream.of("a", "b", "c", "d")
                    .gather(OpenGatherers.dropWhileIndexed((i, e) -> i < 2))
                    .toList();

            assertThat(result).containsExactly("c", "d");
        }

        @Test
        @DisplayName("filterIndexed - 带索引的过滤")
        void testFilterIndexed() {
            List<String> result = Stream.of("a", "b", "c", "d")
                    .gather(OpenGatherers.filterIndexed((i, e) -> i % 2 == 0))
                    .toList();

            assertThat(result).containsExactly("a", "c");
        }

        @Test
        @DisplayName("changed - 变化检测")
        void testChanged() {
            List<Integer> result = Stream.of(1, 1, 2, 2, 2, 3, 1)
                    .gather(OpenGatherers.changed())
                    .toList();

            assertThat(result).containsExactly(1, 2, 3, 1);
        }

        @Test
        @DisplayName("changedBy - 按键变化检测")
        void testChangedBy() {
            List<String> result = Stream.of("apple", "ant", "banana", "ball", "cat")
                    .gather(OpenGatherers.changedBy(s -> s.charAt(0)))
                    .toList();

            assertThat(result).containsExactly("apple", "banana", "cat");
        }
    }

    @Nested
    @DisplayName("带上下文映射测试")
    class MappingWithContextTests {

        @Test
        @DisplayName("mapIndexed - 带索引映射")
        void testMapIndexed() {
            List<String> result = Stream.of("a", "b", "c")
                    .gather(OpenGatherers.mapIndexed((i, e) -> i + ":" + e))
                    .toList();

            assertThat(result).containsExactly("0:a", "1:b", "2:c");
        }

        @Test
        @DisplayName("mapWithPrevious - 带前一个元素映射")
        void testMapWithPrevious() {
            List<Integer> result = Stream.of(1, 2, 3, 4)
                    .gather(OpenGatherers.mapWithPrevious((prev, curr) -> prev + curr))
                    .toList();

            assertThat(result).containsExactly(3, 5, 7);
        }

        @Test
        @DisplayName("zipWithNext - 与下一个元素配对")
        void testZipWithNext() {
            List<String> result = Stream.of("a", "b", "c")
                    .gather(OpenGatherers.zipWithNext((a, b) -> a + "-" + b))
                    .toList();

            assertThat(result).containsExactly("a-b", "b-c");
        }
    }

    @Nested
    @DisplayName("分组测试")
    class GroupingTests {

        @Test
        @DisplayName("groupRuns - 连续分组")
        void testGroupRuns() {
            List<List<Integer>> result = Stream.of(1, 1, 2, 2, 2, 1, 3, 3)
                    .gather(OpenGatherers.groupRuns(i -> i))
                    .toList();

            assertThat(result).hasSize(4);
            assertThat(result.get(0)).containsExactly(1, 1);
            assertThat(result.get(1)).containsExactly(2, 2, 2);
            assertThat(result.get(2)).containsExactly(1);
            assertThat(result.get(3)).containsExactly(3, 3);
        }
    }

    @Nested
    @DisplayName("限制操作测试")
    class LimitingTests {

        @Test
        @DisplayName("takeLast - 获取最后n个")
        void testTakeLast() {
            List<Integer> result = Stream.of(1, 2, 3, 4, 5)
                    .gather(OpenGatherers.takeLast(2))
                    .toList();

            assertThat(result).containsExactly(4, 5);
        }

        @Test
        @DisplayName("takeLast - 无效n抛异常")
        void testTakeLastInvalidN() {
            assertThatThrownBy(() -> OpenGatherers.takeLast(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("dropLast - 丢弃最后n个")
        void testDropLast() {
            List<Integer> result = Stream.of(1, 2, 3, 4, 5)
                    .gather(OpenGatherers.dropLast(2))
                    .toList();

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("dropLast - 无效n抛异常")
        void testDropLastInvalidN() {
            assertThatThrownBy(() -> OpenGatherers.dropLast(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("折叠操作测试")
    class FoldTests {

        @Test
        @DisplayName("fold - 折叠")
        void testFold() {
            List<Integer> result = Stream.of(1, 2, 3, 4)
                    .gather(OpenGatherers.fold(0, Integer::sum))
                    .toList();

            assertThat(result).containsExactly(10);
        }
    }

    @Nested
    @DisplayName("交错操作测试")
    class InterleaveTests {

        @Test
        @DisplayName("intersperse - 插入分隔符")
        void testIntersperse() {
            List<String> result = Stream.of("a", "b", "c")
                    .gather(OpenGatherers.intersperse(","))
                    .toList();

            assertThat(result).containsExactly("a", ",", "b", ",", "c");
        }
    }

    @Nested
    @DisplayName("zipWithIndex 测试")
    class ZipWithIndexTests {

        @Test
        @DisplayName("should_pairElementsWithIndex")
        void should_pairElementsWithIndex() {
            List<Pair<Long, String>> result = Stream.of("a", "b", "c")
                    .gather(OpenGatherers.zipWithIndex())
                    .toList();

            assertThat(result).containsExactly(
                    Pair.of(0L, "a"),
                    Pair.of(1L, "b"),
                    Pair.of(2L, "c")
            );
        }

        @Test
        @DisplayName("should_returnEmptyForEmptyStream")
        void should_returnEmptyForEmptyStream() {
            List<Pair<Long, String>> result = Stream.<String>empty()
                    .gather(OpenGatherers.zipWithIndex())
                    .toList();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should_handleSingleElement")
        void should_handleSingleElement() {
            List<Pair<Long, String>> result = Stream.of("a")
                    .gather(OpenGatherers.zipWithIndex())
                    .toList();

            assertThat(result).containsExactly(Pair.of(0L, "a"));
        }

        @Test
        @DisplayName("should_returnPairType")
        void should_returnPairType() {
            List<Pair<Long, Integer>> result = Stream.of(42)
                    .gather(OpenGatherers.zipWithIndex())
                    .toList();

            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isInstanceOf(Pair.class);
            assertThat(result.getFirst().first()).isEqualTo(0L);
            assertThat(result.getFirst().second()).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("takeWhileInclusive 测试")
    class TakeWhileInclusiveTests {

        @Test
        @DisplayName("should_includeFirstFailingElement")
        void should_includeFirstFailingElement() {
            List<Integer> result = Stream.of(1, 2, 3, 4, 5)
                    .gather(OpenGatherers.takeWhileInclusive(x -> x < 3))
                    .toList();

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("should_returnAllIfAllMatch")
        void should_returnAllIfAllMatch() {
            List<Integer> result = Stream.of(1, 2, 3)
                    .gather(OpenGatherers.takeWhileInclusive(x -> x < 10))
                    .toList();

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("should_returnFirstIfNoneMatch")
        void should_returnFirstIfNoneMatch() {
            List<Integer> result = Stream.of(5, 6, 7)
                    .gather(OpenGatherers.takeWhileInclusive(x -> x < 3))
                    .toList();

            assertThat(result).containsExactly(5);
        }

        @Test
        @DisplayName("should_handleEmptyStream")
        void should_handleEmptyStream() {
            List<Integer> result = Stream.<Integer>empty()
                    .gather(OpenGatherers.takeWhileInclusive(x -> x < 3))
                    .toList();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("interleave 测试")
    class InterleaveWithIteratorTests {

        @Test
        @DisplayName("should_interleaveEqualLength")
        void should_interleaveEqualLength() {
            List<String> result = Stream.of("1", "2", "3")
                    .gather(OpenGatherers.interleave(List.of("a", "b", "c").iterator()))
                    .toList();

            assertThat(result).containsExactly("1", "a", "2", "b", "3", "c");
        }

        @Test
        @DisplayName("should_appendRemainingFromSource")
        void should_appendRemainingFromSource() {
            List<String> result = Stream.of("1", "2", "3")
                    .gather(OpenGatherers.interleave(List.of("a").iterator()))
                    .toList();

            assertThat(result).containsExactly("1", "a", "2", "3");
        }

        @Test
        @DisplayName("should_appendRemainingFromOther")
        void should_appendRemainingFromOther() {
            List<String> result = Stream.of("1")
                    .gather(OpenGatherers.interleave(List.of("a", "b", "c").iterator()))
                    .toList();

            assertThat(result).containsExactly("1", "a", "b", "c");
        }

        @Test
        @DisplayName("should_handleEmptyOther")
        void should_handleEmptyOther() {
            List<String> result = Stream.of("1", "2")
                    .gather(OpenGatherers.interleave(Collections.<String>emptyIterator()))
                    .toList();

            assertThat(result).containsExactly("1", "2");
        }

        @Test
        @DisplayName("should_handleEmptySource")
        void should_handleEmptySource() {
            List<String> result = Stream.<String>empty()
                    .gather(OpenGatherers.interleave(List.of("a", "b").iterator()))
                    .toList();

            assertThat(result).containsExactly("a", "b");
        }
    }

    @Nested
    @DisplayName("工具方法测试")
    class UtilityTests {

        @Test
        @DisplayName("indexed - 添加索引")
        void testIndexed() {
            List<OpenGatherers.IndexedElement<String>> result = Stream.of("a", "b", "c")
                    .gather(OpenGatherers.indexed())
                    .toList();

            assertThat(result).hasSize(3);
            assertThat(result.get(0).index()).isEqualTo(0);
            assertThat(result.get(0).value()).isEqualTo("a");
            assertThat(result.get(1).index()).isEqualTo(1);
            assertThat(result.get(2).index()).isEqualTo(2);
        }

        @Test
        @DisplayName("peekWithIndex - 带索引的peek")
        void testPeekWithIndex() {
            List<String> peeked = new ArrayList<>();
            List<String> result = Stream.of("a", "b", "c")
                    .gather(OpenGatherers.peekWithIndex((i, e) -> peeked.add(i + ":" + e)))
                    .toList();

            assertThat(result).containsExactly("a", "b", "c");
            assertThat(peeked).containsExactly("0:a", "1:b", "2:c");
        }
    }
}
