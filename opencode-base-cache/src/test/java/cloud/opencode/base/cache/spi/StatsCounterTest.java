package cloud.opencode.base.cache.spi;

import cloud.opencode.base.cache.CacheStats;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * StatsCounterTest Tests
 * StatsCounterTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("StatsCounter 接口测试")
class StatsCounterTest {

    @Nested
    @DisplayName("disabled计数器测试")
    class DisabledCounterTests {

        @Test
        @DisplayName("disabled返回空操作计数器")
        void testDisabled() {
            StatsCounter counter = StatsCounter.disabled();

            assertThatNoException().isThrownBy(() -> {
                counter.recordHits(10);
                counter.recordMisses(5);
                counter.recordLoadSuccess(100);
                counter.recordLoadFailure(50);
                counter.recordEviction(1);
            });
        }

        @Test
        @DisplayName("disabled的snapshot返回空统计")
        void testDisabledSnapshot() {
            StatsCounter counter = StatsCounter.disabled();

            CacheStats stats = counter.snapshot();

            assertThat(stats).isNotNull();
        }
    }

    @Nested
    @DisplayName("concurrent计数器测试")
    class ConcurrentCounterTests {

        @Test
        @DisplayName("concurrent记录命中和未命中")
        void testRecordHitsAndMisses() {
            StatsCounter counter = StatsCounter.concurrent();

            counter.recordHits(3);
            counter.recordMisses(2);

            CacheStats stats = counter.snapshot();
            assertThat(stats.hitCount()).isEqualTo(3);
            assertThat(stats.missCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("concurrent记录加载成功")
        void testRecordLoadSuccess() {
            StatsCounter counter = StatsCounter.concurrent();

            counter.recordLoadSuccess(1000);

            CacheStats stats = counter.snapshot();
            assertThat(stats.loadSuccessCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("concurrent记录淘汰")
        void testRecordEviction() {
            StatsCounter counter = StatsCounter.concurrent();

            counter.recordEviction(5);

            CacheStats stats = counter.snapshot();
            assertThat(stats.evictionCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("reset默认为空操作")
        void testResetDefault() {
            StatsCounter counter = StatsCounter.disabled();

            assertThatNoException().isThrownBy(counter::reset);
        }
    }

    @Nested
    @DisplayName("sampling计数器测试")
    class SamplingCounterTests {

        @Test
        @DisplayName("sampling创建采样计数器")
        void testSampling() {
            StatsCounter counter = StatsCounter.sampling(0.5);

            assertThat(counter).isNotNull();
            assertThatNoException().isThrownBy(() -> counter.recordHits(1));
        }

        @Test
        @DisplayName("samplingHighThroughput创建高吞吐量计数器")
        void testSamplingHighThroughput() {
            StatsCounter counter = StatsCounter.samplingHighThroughput();
            assertThat(counter).isNotNull();
        }

        @Test
        @DisplayName("samplingBalanced创建平衡计数器")
        void testSamplingBalanced() {
            StatsCounter counter = StatsCounter.samplingBalanced();
            assertThat(counter).isNotNull();
        }
    }
}
