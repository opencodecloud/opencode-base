package cloud.opencode.base.neural.internal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link ParallelEngine}.
 */
@DisplayName("ParallelEngine — 并行执行引擎测试")
class ParallelEngineTest {

    @Nested
    @DisplayName("parallelFor — 并行 for 循环")
    class ParallelForTest {

        @Test
        @DisplayName("顺序执行: 处理所有元素")
        void processesAllElementsSequential() {
            int[] result = new int[100];
            ParallelEngine.parallelFor(0, 100, i -> result[i] = i * 2);

            for (int i = 0; i < 100; i++) {
                assertThat(result[i]).isEqualTo(i * 2);
            }
        }

        @Test
        @DisplayName("并行执行: 大数组处理所有元素")
        void processesAllElementsParallel() {
            int size = 100_000;
            AtomicInteger counter = new AtomicInteger(0);

            ParallelEngine.parallelFor(0, size, i -> counter.incrementAndGet());

            assertThat(counter.get()).isEqualTo(size);
        }

        @Test
        @DisplayName("空范围: start >= end 不执行")
        void emptyRange() {
            AtomicInteger counter = new AtomicInteger(0);
            ParallelEngine.parallelFor(5, 5, i -> counter.incrementAndGet());
            assertThat(counter.get()).isEqualTo(0);

            ParallelEngine.parallelFor(10, 5, i -> counter.incrementAndGet());
            assertThat(counter.get()).isEqualTo(0);
        }

        @Test
        @DisplayName("非零起始索引")
        void nonZeroStart() {
            int[] flags = new int[20];
            ParallelEngine.parallelFor(10, 20, i -> flags[i] = 1);

            for (int i = 0; i < 10; i++) {
                assertThat(flags[i]).isEqualTo(0);
            }
            for (int i = 10; i < 20; i++) {
                assertThat(flags[i]).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("null body 抛出 NullPointerException")
        void nullBody() {
            assertThatNullPointerException().isThrownBy(() ->
                    ParallelEngine.parallelFor(0, 10, null));
        }
    }

    @Nested
    @DisplayName("shouldParallelize — 并行化阈值检查")
    class ShouldParallelizeTest {

        @Test
        @DisplayName("小于阈值返回 false")
        void belowThreshold() {
            assertThat(ParallelEngine.shouldParallelize(100)).isFalse();
            assertThat(ParallelEngine.shouldParallelize(65_536)).isFalse();
        }

        @Test
        @DisplayName("大于阈值返回 true")
        void aboveThreshold() {
            assertThat(ParallelEngine.shouldParallelize(65_537)).isTrue();
            assertThat(ParallelEngine.shouldParallelize(1_000_000)).isTrue();
        }

        @Test
        @DisplayName("零和负数返回 false")
        void zeroAndNegative() {
            assertThat(ParallelEngine.shouldParallelize(0)).isFalse();
            assertThat(ParallelEngine.shouldParallelize(-1)).isFalse();
        }
    }
}
