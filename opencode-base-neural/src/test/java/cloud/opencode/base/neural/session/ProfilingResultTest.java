package cloud.opencode.base.neural.session;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for {@link ProfilingResult}.
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-neural V1.0.0
 */
@DisplayName("ProfilingResult — 性能分析结果")
class ProfilingResultTest {

    @Nested
    @DisplayName("totalTimeMillis 总时间转换")
    class TotalTimeMillisTest {

        @Test
        @DisplayName("1_000_000 nanos == 1.0 ms")
        void oneMillisecond() {
            var result = new ProfilingResult(List.of(), 1_000_000L);
            assertThat(result.totalTimeMillis()).isCloseTo(1.0, within(1e-9));
        }

        @Test
        @DisplayName("500_000 nanos == 0.5 ms")
        void halfMillisecond() {
            var result = new ProfilingResult(List.of(), 500_000L);
            assertThat(result.totalTimeMillis()).isCloseTo(0.5, within(1e-9));
        }

        @Test
        @DisplayName("0 nanos == 0.0 ms")
        void zeroNanos() {
            var result = new ProfilingResult(List.of(), 0L);
            assertThat(result.totalTimeMillis()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("OpTiming 算子计时")
    class OpTimingTest {

        @Test
        @DisplayName("timeMillis 转换正确")
        void timeMillisConversion() {
            var timing = new ProfilingResult.OpTiming("conv1", "Conv2D", 2_500_000L);
            assertThat(timing.timeMillis()).isCloseTo(2.5, within(1e-9));
            assertThat(timing.nodeName()).isEqualTo("conv1");
            assertThat(timing.opType()).isEqualTo("Conv2D");
            assertThat(timing.timeNanos()).isEqualTo(2_500_000L);
        }
    }

    @Nested
    @DisplayName("summary 摘要")
    class SummaryTest {

        @Test
        @DisplayName("summary 包含标题和总时间")
        void summaryContainsHeader() {
            var result = new ProfilingResult(List.of(), 5_000_000L);
            String summary = result.summary();
            assertThat(summary).contains("Profiling Summary");
            assertThat(summary).contains("5.000 ms");
            assertThat(summary).contains("Operators:  0");
        }

        @Test
        @DisplayName("summary 按时间降序排列算子")
        void summaryOrderedByTimeDescending() {
            var timings = List.of(
                    new ProfilingResult.OpTiming("fast_op", "ReLU", 100_000L),
                    new ProfilingResult.OpTiming("slow_op", "Conv2D", 3_000_000L),
                    new ProfilingResult.OpTiming("mid_op", "MatMul", 1_000_000L)
            );
            var result = new ProfilingResult(timings, 4_100_000L);
            String summary = result.summary();

            // slow_op should appear before mid_op, which should appear before fast_op
            int slowIdx = summary.indexOf("slow_op");
            int midIdx = summary.indexOf("mid_op");
            int fastIdx = summary.indexOf("fast_op");
            assertThat(slowIdx).isLessThan(midIdx);
            assertThat(midIdx).isLessThan(fastIdx);
        }

        @Test
        @DisplayName("summary 包含算子类型信息")
        void summaryContainsOpTypes() {
            var timings = List.of(
                    new ProfilingResult.OpTiming("node1", "Conv2D", 1_000_000L)
            );
            var result = new ProfilingResult(timings, 1_000_000L);
            String summary = result.summary();
            assertThat(summary).contains("node1");
            assertThat(summary).contains("Conv2D");
        }
    }

    @Nested
    @DisplayName("防御性拷贝与验证")
    class DefensiveCopyTest {

        @Test
        @DisplayName("opTimings 列表不可修改")
        void opTimingsImmutable() {
            var mutableList = new ArrayList<>(List.of(
                    new ProfilingResult.OpTiming("op1", "ReLU", 100L)
            ));
            var result = new ProfilingResult(mutableList, 100L);

            // Modifying the original list should not affect the result
            mutableList.add(new ProfilingResult.OpTiming("op2", "Sigmoid", 200L));
            assertThat(result.opTimings()).hasSize(1);

            // The returned list should be unmodifiable
            assertThatThrownBy(() -> result.opTimings().add(
                    new ProfilingResult.OpTiming("op3", "Tanh", 300L)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("totalTimeNanos < 0 → IllegalArgumentException")
        void negativeTotalTimeNanos() {
            assertThatThrownBy(() -> new ProfilingResult(List.of(), -1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("totalTimeNanos");
        }
    }
}
