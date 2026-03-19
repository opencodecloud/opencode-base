package cloud.opencode.base.lock.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * LockMetrics Interface Tests
 * LockMetrics 接口测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.0
 */
@DisplayName("LockMetrics Interface Tests | LockMetrics 接口测试")
class LockMetricsTest {

    private LockMetrics metrics;

    @BeforeEach
    void setUp() {
        metrics = new DefaultLockMetrics();
    }

    @Nested
    @DisplayName("Acquire Count Tests | 获取次数测试")
    class AcquireCountTests {

        @Test
        @DisplayName("getAcquireCount returns count | getAcquireCount 返回次数")
        void testGetAcquireCount() {
            assertThat(metrics.getAcquireCount()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("acquire count increases | 获取次数增加")
        void testAcquireCountIncreases() {
            long before = metrics.getAcquireCount();

            ((DefaultLockMetrics) metrics).recordAcquire(Duration.ZERO);

            assertThat(metrics.getAcquireCount()).isEqualTo(before + 1);
        }
    }

    @Nested
    @DisplayName("Release Count Tests | 释放次数测试")
    class ReleaseCountTests {

        @Test
        @DisplayName("getReleaseCount returns count | getReleaseCount 返回次数")
        void testGetReleaseCount() {
            assertThat(metrics.getReleaseCount()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("release count increases | 释放次数增加")
        void testReleaseCountIncreases() {
            long before = metrics.getReleaseCount();

            ((DefaultLockMetrics) metrics).recordRelease();

            assertThat(metrics.getReleaseCount()).isEqualTo(before + 1);
        }
    }

    @Nested
    @DisplayName("Timeout Count Tests | 超时次数测试")
    class TimeoutCountTests {

        @Test
        @DisplayName("getTimeoutCount returns count | getTimeoutCount 返回次数")
        void testGetTimeoutCount() {
            assertThat(metrics.getTimeoutCount()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("timeout count increases | 超时次数增加")
        void testTimeoutCountIncreases() {
            long before = metrics.getTimeoutCount();

            ((DefaultLockMetrics) metrics).recordTimeout();

            assertThat(metrics.getTimeoutCount()).isEqualTo(before + 1);
        }
    }

    @Nested
    @DisplayName("Contention Count Tests | 竞争次数测试")
    class ContentionCountTests {

        @Test
        @DisplayName("getContentionCount returns count | getContentionCount 返回次数")
        void testGetContentionCount() {
            assertThat(metrics.getContentionCount()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("contention count increases | 竞争次数增加")
        void testContentionCountIncreases() {
            long before = metrics.getContentionCount();

            // Contention is recorded when waitTime > 0 in recordAcquire
            ((DefaultLockMetrics) metrics).recordAcquire(Duration.ofMillis(100));

            assertThat(metrics.getContentionCount()).isEqualTo(before + 1);
        }
    }

    @Nested
    @DisplayName("Wait Time Tests | 等待时间测试")
    class WaitTimeTests {

        @Test
        @DisplayName("getAverageWaitTime returns duration | getAverageWaitTime 返回时长")
        void testGetAverageWaitTime() {
            Duration avgWait = metrics.getAverageWaitTime();
            assertThat(avgWait).isNotNull();
        }

        @Test
        @DisplayName("getMaxWaitTime returns duration | getMaxWaitTime 返回时长")
        void testGetMaxWaitTime() {
            Duration maxWait = metrics.getMaxWaitTime();
            assertThat(maxWait).isNotNull();
        }

        @Test
        @DisplayName("max wait time updates | 最大等待时间更新")
        void testMaxWaitTimeUpdates() {
            // Contention and wait time are recorded when waitTime > 0 in recordAcquire
            ((DefaultLockMetrics) metrics).recordAcquire(Duration.ofMillis(100));
            ((DefaultLockMetrics) metrics).recordAcquire(Duration.ofMillis(200));

            assertThat(metrics.getMaxWaitTime()).isGreaterThanOrEqualTo(Duration.ofMillis(200));
        }
    }

    @Nested
    @DisplayName("Hold Count Tests | 持有次数测试")
    class HoldCountTests {

        @Test
        @DisplayName("getCurrentHoldCount returns count | getCurrentHoldCount 返回次数")
        void testGetCurrentHoldCount() {
            assertThat(metrics.getCurrentHoldCount()).isGreaterThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Snapshot Tests | 快照测试")
    class SnapshotTests {

        @Test
        @DisplayName("snapshot returns immutable stats | snapshot 返回不可变统计")
        void testSnapshot() {
            ((DefaultLockMetrics) metrics).recordAcquire(Duration.ofMillis(10));
            ((DefaultLockMetrics) metrics).recordRelease();

            LockStats stats = metrics.snapshot();

            assertThat(stats).isNotNull();
            assertThat(stats.acquireCount()).isEqualTo(metrics.getAcquireCount());
            assertThat(stats.releaseCount()).isEqualTo(metrics.getReleaseCount());
        }

        @Test
        @DisplayName("snapshot is independent of further changes | 快照独立于后续更改")
        void testSnapshotIndependent() {
            ((DefaultLockMetrics) metrics).recordAcquire(Duration.ZERO);
            LockStats stats = metrics.snapshot();
            long acquireAtSnapshot = stats.acquireCount();

            ((DefaultLockMetrics) metrics).recordAcquire(Duration.ZERO);

            assertThat(stats.acquireCount()).isEqualTo(acquireAtSnapshot);
            assertThat(metrics.getAcquireCount()).isEqualTo(acquireAtSnapshot + 1);
        }
    }

    @Nested
    @DisplayName("Reset Tests | 重置测试")
    class ResetTests {

        @Test
        @DisplayName("reset clears all metrics | reset 清除所有指标")
        void testReset() {
            ((DefaultLockMetrics) metrics).recordAcquire(Duration.ofMillis(10));
            ((DefaultLockMetrics) metrics).recordRelease();
            ((DefaultLockMetrics) metrics).recordTimeout();
            // Contention is recorded via recordAcquire when waitTime > 0
            ((DefaultLockMetrics) metrics).recordAcquire(Duration.ofMillis(50));

            metrics.reset();

            assertThat(metrics.getAcquireCount()).isEqualTo(0);
            assertThat(metrics.getReleaseCount()).isEqualTo(0);
            assertThat(metrics.getTimeoutCount()).isEqualTo(0);
            assertThat(metrics.getContentionCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("LockStats Record Tests | LockStats 记录测试")
    class LockStatsRecordTests {

        @Test
        @DisplayName("LockStats contains all fields | LockStats 包含所有字段")
        void testLockStatsFields() {
            ((DefaultLockMetrics) metrics).recordAcquire(Duration.ofMillis(10));
            // Contention is recorded via recordAcquire when waitTime > 0
            ((DefaultLockMetrics) metrics).recordAcquire(Duration.ofMillis(50));

            LockStats stats = metrics.snapshot();

            assertThat(stats.acquireCount()).isGreaterThanOrEqualTo(0);
            assertThat(stats.releaseCount()).isGreaterThanOrEqualTo(0);
            assertThat(stats.timeoutCount()).isGreaterThanOrEqualTo(0);
            assertThat(stats.contentionCount()).isGreaterThanOrEqualTo(0);
            assertThat(stats.averageWaitTime()).isNotNull();
            assertThat(stats.maxWaitTime()).isNotNull();
        }
    }
}
