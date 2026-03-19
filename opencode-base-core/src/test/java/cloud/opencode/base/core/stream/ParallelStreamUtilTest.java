package cloud.opencode.base.core.stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * ParallelStreamUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("ParallelStreamUtil 测试")
class ParallelStreamUtilTest {

    @Nested
    @DisplayName("stream 测试")
    class StreamTests {

        @Test
        @DisplayName("stream 小集合返回顺序流")
        void testStreamSmallCollectionSequential() {
            List<Integer> list = List.of(1, 2, 3, 4, 5);
            Stream<Integer> stream = ParallelStreamUtil.stream(list);

            assertThat(stream.isParallel()).isFalse();
        }

        @Test
        @DisplayName("stream 大集合根据 CPU 可能返回并行流")
        void testStreamLargeCollection() {
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < 2000; i++) {
                list.add(i);
            }

            Stream<Integer> stream = ParallelStreamUtil.stream(list);
            // 结果取决于 CPU 数量
            if (ParallelStreamUtil.getAvailableProcessors() > 1) {
                assertThat(stream.isParallel()).isTrue();
            }
        }

        @Test
        @DisplayName("stream null 集合返回空流")
        void testStreamNullCollection() {
            Stream<Integer> stream = ParallelStreamUtil.stream(null);
            assertThat(stream.count()).isEqualTo(0);
        }

        @Test
        @DisplayName("stream 自定义阈值")
        void testStreamCustomThreshold() {
            List<Integer> list = List.of(1, 2, 3, 4, 5);

            // 阈值为 3，集合大小为 5，应该返回并行流（如果有多核）
            Stream<Integer> stream = ParallelStreamUtil.stream(list, 3);
            if (ParallelStreamUtil.getAvailableProcessors() > 1) {
                assertThat(stream.isParallel()).isTrue();
            }
        }

        @Test
        @DisplayName("stream 阈值大于集合大小返回顺序流")
        void testStreamThresholdGreaterThanSize() {
            List<Integer> list = List.of(1, 2, 3);
            Stream<Integer> stream = ParallelStreamUtil.stream(list, 10);
            assertThat(stream.isParallel()).isFalse();
        }
    }

    @Nested
    @DisplayName("isParallelRecommended 测试")
    class IsParallelRecommendedTests {

        @Test
        @DisplayName("isParallelRecommended 默认阈值")
        void testIsParallelRecommendedDefault() {
            assertThat(ParallelStreamUtil.isParallelRecommended(500)).isFalse();
            // 如果 CPU > 1，1000 应该推荐并行
            if (ParallelStreamUtil.getAvailableProcessors() > 1) {
                assertThat(ParallelStreamUtil.isParallelRecommended(1000)).isTrue();
            }
        }

        @Test
        @DisplayName("isParallelRecommended 自定义阈值")
        void testIsParallelRecommendedCustom() {
            if (ParallelStreamUtil.getAvailableProcessors() > 1) {
                assertThat(ParallelStreamUtil.isParallelRecommended(100, 50)).isTrue();
                assertThat(ParallelStreamUtil.isParallelRecommended(30, 50)).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("系统信息测试")
    class SystemInfoTests {

        @Test
        @DisplayName("getAvailableProcessors")
        void testGetAvailableProcessors() {
            int processors = ParallelStreamUtil.getAvailableProcessors();
            assertThat(processors).isGreaterThan(0);
            assertThat(processors).isEqualTo(Runtime.getRuntime().availableProcessors());
        }

        @Test
        @DisplayName("getParallelism")
        void testGetParallelism() {
            int parallelism = ParallelStreamUtil.getParallelism();
            assertThat(parallelism).isGreaterThan(0);
        }

        @Test
        @DisplayName("getRecommendedThreshold")
        void testGetRecommendedThreshold() {
            int threshold = ParallelStreamUtil.getRecommendedThreshold();
            // 推荐阈值应该与 CPU 核心数相关
            assertThat(threshold).isEqualTo(1000 * ParallelStreamUtil.getAvailableProcessors());
        }
    }

    @Nested
    @DisplayName("强制流类型测试")
    class ForceStreamTypeTests {

        @Test
        @DisplayName("sequentialStream 强制顺序流")
        void testSequentialStream() {
            List<Integer> list = List.of(1, 2, 3);
            Stream<Integer> stream = ParallelStreamUtil.sequentialStream(list);
            assertThat(stream.isParallel()).isFalse();
        }

        @Test
        @DisplayName("sequentialStream null 返回空流")
        void testSequentialStreamNull() {
            Stream<Integer> stream = ParallelStreamUtil.sequentialStream(null);
            assertThat(stream.count()).isEqualTo(0);
        }

        @Test
        @DisplayName("parallelStream 强制并行流")
        void testParallelStream() {
            List<Integer> list = List.of(1, 2, 3);
            Stream<Integer> stream = ParallelStreamUtil.parallelStream(list);
            assertThat(stream.isParallel()).isTrue();
        }

        @Test
        @DisplayName("parallelStream null 返回空流")
        void testParallelStreamNull() {
            Stream<Integer> stream = ParallelStreamUtil.parallelStream(null);
            assertThat(stream.count()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("流转换测试")
    class StreamConversionTests {

        @Test
        @DisplayName("toParallelIf 条件为 true")
        void testToParallelIfTrue() {
            Stream<Integer> stream = Stream.of(1, 2, 3);
            Stream<Integer> result = ParallelStreamUtil.toParallelIf(stream, true);

            if (ParallelStreamUtil.getAvailableProcessors() > 1) {
                assertThat(result.isParallel()).isTrue();
            }
        }

        @Test
        @DisplayName("toParallelIf 条件为 false")
        void testToParallelIfFalse() {
            Stream<Integer> stream = Stream.of(1, 2, 3);
            Stream<Integer> result = ParallelStreamUtil.toParallelIf(stream, false);
            assertThat(result.isParallel()).isFalse();
        }

        @Test
        @DisplayName("toSequential")
        void testToSequential() {
            Stream<Integer> stream = Stream.of(1, 2, 3).parallel();
            assertThat(stream.isParallel()).isTrue();

            Stream<Integer> result = ParallelStreamUtil.toSequential(stream);
            assertThat(result.isParallel()).isFalse();
        }
    }

    @Nested
    @DisplayName("功能验证测试")
    class FunctionalTests {

        @Test
        @DisplayName("并行流处理结果正确")
        void testParallelStreamCorrectResult() {
            List<Integer> list = List.of(1, 2, 3, 4, 5);
            int sum = ParallelStreamUtil.parallelStream(list)
                    .mapToInt(Integer::intValue)
                    .sum();
            assertThat(sum).isEqualTo(15);
        }

        @Test
        @DisplayName("顺序流处理结果正确")
        void testSequentialStreamCorrectResult() {
            List<Integer> list = List.of(1, 2, 3, 4, 5);
            int sum = ParallelStreamUtil.sequentialStream(list)
                    .mapToInt(Integer::intValue)
                    .sum();
            assertThat(sum).isEqualTo(15);
        }
    }
}
