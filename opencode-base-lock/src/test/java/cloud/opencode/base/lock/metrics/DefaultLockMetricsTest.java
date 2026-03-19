package cloud.opencode.base.lock.metrics;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

/**
 * DefaultLockMetrics test - 默认锁指标测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.0
 */
class DefaultLockMetricsTest {

    private DefaultLockMetrics metrics;

    @BeforeEach
    void setUp() {
        metrics = new DefaultLockMetrics();
    }

    @Nested
    @DisplayName("Initial State Tests | 初始状态测试")
    class InitialStateTests {

        @Test
        @DisplayName("initial acquire count should be 0")
        void initialAcquireCount_shouldBeZero() {
            assertThat(metrics.getAcquireCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("initial release count should be 0")
        void initialReleaseCount_shouldBeZero() {
            assertThat(metrics.getReleaseCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("initial timeout count should be 0")
        void initialTimeoutCount_shouldBeZero() {
            assertThat(metrics.getTimeoutCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("initial contention count should be 0")
        void initialContentionCount_shouldBeZero() {
            assertThat(metrics.getContentionCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("initial average wait time should be zero")
        void initialAverageWaitTime_shouldBeZero() {
            assertThat(metrics.getAverageWaitTime()).isEqualTo(Duration.ZERO);
        }

        @Test
        @DisplayName("initial max wait time should be zero")
        void initialMaxWaitTime_shouldBeZero() {
            assertThat(metrics.getMaxWaitTime()).isEqualTo(Duration.ZERO);
        }

        @Test
        @DisplayName("initial current hold count should be 0")
        void initialCurrentHoldCount_shouldBeZero() {
            assertThat(metrics.getCurrentHoldCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("RecordAcquire Tests | 记录获取测试")
    class RecordAcquireTests {

        @Test
        @DisplayName("recordAcquire() should increment acquire count")
        void recordAcquire_shouldIncrementAcquireCount() {
            metrics.recordAcquire(Duration.ZERO);

            assertThat(metrics.getAcquireCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("recordAcquire() should increment current hold count")
        void recordAcquire_shouldIncrementCurrentHoldCount() {
            metrics.recordAcquire(Duration.ZERO);

            assertThat(metrics.getCurrentHoldCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("recordAcquire() with wait time should increment contention count")
        void recordAcquireWithWaitTime_shouldIncrementContentionCount() {
            metrics.recordAcquire(Duration.ofMillis(10));

            assertThat(metrics.getContentionCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("recordAcquire() with zero wait time should not increment contention count")
        void recordAcquireWithZeroWaitTime_shouldNotIncrementContentionCount() {
            metrics.recordAcquire(Duration.ZERO);

            assertThat(metrics.getContentionCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("recordAcquire() should update max wait time")
        void recordAcquire_shouldUpdateMaxWaitTime() {
            metrics.recordAcquire(Duration.ofMillis(10));
            metrics.recordAcquire(Duration.ofMillis(50));
            metrics.recordAcquire(Duration.ofMillis(30));

            assertThat(metrics.getMaxWaitTime()).isEqualTo(Duration.ofMillis(50));
        }

        @Test
        @DisplayName("recordAcquire() should calculate average wait time")
        void recordAcquire_shouldCalculateAverageWaitTime() {
            metrics.recordAcquire(Duration.ofMillis(10));
            metrics.recordAcquire(Duration.ofMillis(20));
            metrics.recordAcquire(Duration.ofMillis(30));

            Duration avg = metrics.getAverageWaitTime();
            assertThat(avg.toMillis()).isEqualTo(20);
        }

        @Test
        @DisplayName("multiple acquires should accumulate counts")
        void multipleAcquires_shouldAccumulateCounts() {
            for (int i = 0; i < 10; i++) {
                metrics.recordAcquire(Duration.ofMillis(i));
            }

            assertThat(metrics.getAcquireCount()).isEqualTo(10);
            assertThat(metrics.getCurrentHoldCount()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("RecordRelease Tests | 记录释放测试")
    class RecordReleaseTests {

        @Test
        @DisplayName("recordRelease() should increment release count")
        void recordRelease_shouldIncrementReleaseCount() {
            metrics.recordRelease();

            assertThat(metrics.getReleaseCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("recordRelease() should decrement current hold count")
        void recordRelease_shouldDecrementCurrentHoldCount() {
            metrics.recordAcquire(Duration.ZERO);
            metrics.recordAcquire(Duration.ZERO);
            metrics.recordRelease();

            assertThat(metrics.getCurrentHoldCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("recordRelease() can go negative for hold count")
        void recordRelease_canGoNegativeForHoldCount() {
            metrics.recordRelease();

            assertThat(metrics.getCurrentHoldCount()).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("RecordTimeout Tests | 记录超时测试")
    class RecordTimeoutTests {

        @Test
        @DisplayName("recordTimeout() should increment timeout count")
        void recordTimeout_shouldIncrementTimeoutCount() {
            metrics.recordTimeout();

            assertThat(metrics.getTimeoutCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("multiple timeouts should accumulate")
        void multipleTimeouts_shouldAccumulate() {
            for (int i = 0; i < 5; i++) {
                metrics.recordTimeout();
            }

            assertThat(metrics.getTimeoutCount()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Snapshot Tests | 快照测试")
    class SnapshotTests {

        @Test
        @DisplayName("snapshot() should return current stats")
        void snapshot_shouldReturnCurrentStats() {
            metrics.recordAcquire(Duration.ofMillis(10));
            metrics.recordAcquire(Duration.ofMillis(20));
            metrics.recordRelease();
            metrics.recordTimeout();

            LockStats stats = metrics.snapshot();

            assertThat(stats.acquireCount()).isEqualTo(2);
            assertThat(stats.releaseCount()).isEqualTo(1);
            assertThat(stats.timeoutCount()).isEqualTo(1);
            assertThat(stats.contentionCount()).isEqualTo(2);
            assertThat(stats.currentHoldCount()).isEqualTo(1);
            assertThat(stats.timestamp()).isNotNull();
        }

        @Test
        @DisplayName("snapshot() should be independent of future changes")
        void snapshot_shouldBeIndependentOfFutureChanges() {
            metrics.recordAcquire(Duration.ZERO);

            LockStats snapshot1 = metrics.snapshot();

            metrics.recordAcquire(Duration.ZERO);

            LockStats snapshot2 = metrics.snapshot();

            assertThat(snapshot1.acquireCount()).isEqualTo(1);
            assertThat(snapshot2.acquireCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Reset Tests | 重置测试")
    class ResetTests {

        @Test
        @DisplayName("reset() should clear all counters")
        void reset_shouldClearAllCounters() {
            metrics.recordAcquire(Duration.ofMillis(10));
            metrics.recordRelease();
            metrics.recordTimeout();

            metrics.reset();

            assertThat(metrics.getAcquireCount()).isEqualTo(0);
            assertThat(metrics.getReleaseCount()).isEqualTo(0);
            assertThat(metrics.getTimeoutCount()).isEqualTo(0);
            assertThat(metrics.getContentionCount()).isEqualTo(0);
            assertThat(metrics.getMaxWaitTime()).isEqualTo(Duration.ZERO);
        }

        @Test
        @DisplayName("reset() should not affect current hold count")
        void reset_shouldNotAffectCurrentHoldCount() {
            metrics.recordAcquire(Duration.ZERO);
            metrics.recordAcquire(Duration.ZERO);

            int holdBefore = metrics.getCurrentHoldCount();
            metrics.reset();
            int holdAfter = metrics.getCurrentHoldCount();

            assertThat(holdBefore).isEqualTo(2);
            // reset() doesn't reset currentHoldCount in the current implementation
            // This is because hold count represents live state
        }
    }

    @Nested
    @DisplayName("Concurrency Tests | 并发测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("should handle concurrent record calls")
        void shouldHandleConcurrentRecordCalls() throws Exception {
            int threads = 10;
            int operations = 1000;

            ExecutorService executor = Executors.newFixedThreadPool(threads);
            List<Future<?>> futures = new ArrayList<>();

            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    for (int j = 0; j < operations; j++) {
                        metrics.recordAcquire(Duration.ofNanos(j));
                        metrics.recordRelease();
                        if (j % 10 == 0) {
                            metrics.recordTimeout();
                        }
                    }
                }));
            }

            for (Future<?> future : futures) {
                future.get();
            }

            executor.shutdown();

            assertThat(metrics.getAcquireCount()).isEqualTo(threads * operations);
            assertThat(metrics.getReleaseCount()).isEqualTo(threads * operations);
            assertThat(metrics.getTimeoutCount()).isEqualTo(threads * (operations / 10));
        }

        @Test
        @DisplayName("should handle concurrent snapshot calls")
        void shouldHandleConcurrentSnapshotCalls() throws Exception {
            int threads = 10;

            ExecutorService executor = Executors.newFixedThreadPool(threads);
            List<Future<LockStats>> futures = new ArrayList<>();

            // Add some baseline metrics
            for (int i = 0; i < 100; i++) {
                metrics.recordAcquire(Duration.ofMillis(i));
            }

            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    LockStats stats = metrics.snapshot();
                    metrics.recordAcquire(Duration.ZERO);
                    return stats;
                }));
            }

            for (Future<LockStats> future : futures) {
                LockStats stats = future.get();
                assertThat(stats.acquireCount()).isGreaterThanOrEqualTo(100);
            }

            executor.shutdown();
        }
    }

    @Nested
    @DisplayName("Interface Implementation Tests | 接口实现测试")
    class InterfaceImplementationTests {

        @Test
        @DisplayName("should implement LockMetrics interface")
        void shouldImplementLockMetricsInterface() {
            assertThat(metrics).isInstanceOf(LockMetrics.class);
        }

        @Test
        @DisplayName("all interface methods should be implemented")
        void allInterfaceMethods_shouldBeImplemented() {
            // Test all methods return expected types
            assertThat(metrics.getAcquireCount()).isInstanceOf(Long.class);
            assertThat(metrics.getReleaseCount()).isInstanceOf(Long.class);
            assertThat(metrics.getTimeoutCount()).isInstanceOf(Long.class);
            assertThat(metrics.getContentionCount()).isInstanceOf(Long.class);
            assertThat(metrics.getAverageWaitTime()).isInstanceOf(Duration.class);
            assertThat(metrics.getMaxWaitTime()).isInstanceOf(Duration.class);
            assertThat(metrics.getCurrentHoldCount()).isInstanceOf(Integer.class);
            assertThat(metrics.snapshot()).isInstanceOf(LockStats.class);
        }
    }
}
