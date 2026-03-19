package cloud.opencode.base.pool.metrics;

import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * DefaultPoolMetricsTest Tests
 * DefaultPoolMetricsTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
@DisplayName("DefaultPoolMetrics 测试")
class DefaultPoolMetricsTest {

    private DefaultPoolMetrics metrics;

    @BeforeEach
    void setUp() {
        metrics = new DefaultPoolMetrics();
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建时所有计数器为零")
        void testInitialState() {
            assertThat(metrics.getBorrowCount()).isZero();
            assertThat(metrics.getReturnCount()).isZero();
            assertThat(metrics.getCreatedCount()).isZero();
            assertThat(metrics.getDestroyedCount()).isZero();
        }
    }

    @Nested
    @DisplayName("recordBorrow方法测试")
    class RecordBorrowTests {

        @Test
        @DisplayName("记录借用增加计数")
        void testRecordBorrow() {
            metrics.recordBorrow();
            metrics.recordBorrow();
            metrics.recordBorrow();

            assertThat(metrics.getBorrowCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("recordReturn方法测试")
    class RecordReturnTests {

        @Test
        @DisplayName("记录归还增加计数")
        void testRecordReturn() {
            metrics.recordReturn();
            metrics.recordReturn();

            assertThat(metrics.getReturnCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("recordCreate方法测试")
    class RecordCreateTests {

        @Test
        @DisplayName("记录创建增加计数")
        void testRecordCreate() {
            metrics.recordCreate();

            assertThat(metrics.getCreatedCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("recordDestroy方法测试")
    class RecordDestroyTests {

        @Test
        @DisplayName("记录销毁增加计数")
        void testRecordDestroy() {
            metrics.recordDestroy();
            metrics.recordDestroy();
            metrics.recordDestroy();

            assertThat(metrics.getDestroyedCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("recordBorrowDuration方法测试")
    class RecordBorrowDurationTests {

        @Test
        @DisplayName("记录借用时长并计算平均值")
        void testRecordBorrowDuration() {
            metrics.recordBorrow();
            metrics.recordBorrowDuration(Duration.ofMillis(100));
            metrics.recordBorrow();
            metrics.recordBorrowDuration(Duration.ofMillis(200));

            assertThat(metrics.getAverageBorrowDuration()).isEqualTo(Duration.ofMillis(150));
        }

        @Test
        @DisplayName("记录最大借用时长")
        void testMaxBorrowDuration() {
            metrics.recordBorrowDuration(Duration.ofMillis(100));
            metrics.recordBorrowDuration(Duration.ofMillis(300));
            metrics.recordBorrowDuration(Duration.ofMillis(200));

            assertThat(metrics.getMaxBorrowDuration()).isEqualTo(Duration.ofMillis(300));
        }

        @Test
        @DisplayName("无借用时平均值为零")
        void testZeroBorrowDuration() {
            assertThat(metrics.getAverageBorrowDuration()).isEqualTo(Duration.ZERO);
        }
    }

    @Nested
    @DisplayName("recordWaitDuration方法测试")
    class RecordWaitDurationTests {

        @Test
        @DisplayName("记录等待时长并计算平均值")
        void testRecordWaitDuration() {
            metrics.recordBorrow();
            metrics.recordWaitDuration(Duration.ofMillis(50));
            metrics.recordBorrow();
            metrics.recordWaitDuration(Duration.ofMillis(150));

            assertThat(metrics.getAverageWaitDuration()).isEqualTo(Duration.ofMillis(100));
        }

        @Test
        @DisplayName("无借用时平均等待时长为零")
        void testZeroWaitDuration() {
            assertThat(metrics.getAverageWaitDuration()).isEqualTo(Duration.ZERO);
        }
    }

    @Nested
    @DisplayName("setActiveSupplier和setIdleSupplier方法测试")
    class SupplierTests {

        @Test
        @DisplayName("设置活跃数供应器")
        void testSetActiveSupplier() {
            metrics.setActiveSupplier(() -> 5);

            MetricsSnapshot snapshot = metrics.snapshot();
            assertThat(snapshot.currentActive()).isEqualTo(5);
        }

        @Test
        @DisplayName("设置空闲数供应器")
        void testSetIdleSupplier() {
            metrics.setIdleSupplier(() -> 10);

            MetricsSnapshot snapshot = metrics.snapshot();
            assertThat(snapshot.currentIdle()).isEqualTo(10);
        }

        @Test
        @DisplayName("默认供应器返回零")
        void testDefaultSuppliers() {
            MetricsSnapshot snapshot = metrics.snapshot();

            assertThat(snapshot.currentActive()).isZero();
            assertThat(snapshot.currentIdle()).isZero();
        }
    }

    @Nested
    @DisplayName("snapshot方法测试")
    class SnapshotTests {

        @Test
        @DisplayName("快照包含所有指标")
        void testSnapshot() {
            metrics.setActiveSupplier(() -> 5);
            metrics.setIdleSupplier(() -> 10);
            metrics.recordBorrow();
            metrics.recordBorrow();
            metrics.recordReturn();
            metrics.recordCreate();
            metrics.recordDestroy();
            metrics.recordBorrowDuration(Duration.ofMillis(100));
            metrics.recordWaitDuration(Duration.ofMillis(50));

            MetricsSnapshot snapshot = metrics.snapshot();

            assertThat(snapshot.borrowCount()).isEqualTo(2);
            assertThat(snapshot.returnCount()).isEqualTo(1);
            assertThat(snapshot.createdCount()).isEqualTo(1);
            assertThat(snapshot.destroyedCount()).isEqualTo(1);
            assertThat(snapshot.currentActive()).isEqualTo(5);
            assertThat(snapshot.currentIdle()).isEqualTo(10);
            assertThat(snapshot.timestamp()).isNotNull();
        }

        @Test
        @DisplayName("快照时间戳接近当前时间")
        void testSnapshotTimestamp() {
            MetricsSnapshot snapshot = metrics.snapshot();

            assertThat(snapshot.timestamp()).isNotNull();
            assertThat(snapshot.timestamp().toEpochMilli())
                    .isCloseTo(System.currentTimeMillis(), within(1000L));
        }
    }

    @Nested
    @DisplayName("reset方法测试")
    class ResetTests {

        @Test
        @DisplayName("重置所有计数器")
        void testReset() {
            metrics.recordBorrow();
            metrics.recordReturn();
            metrics.recordCreate();
            metrics.recordDestroy();
            metrics.recordBorrowDuration(Duration.ofMillis(100));
            metrics.recordWaitDuration(Duration.ofMillis(50));

            metrics.reset();

            assertThat(metrics.getBorrowCount()).isZero();
            assertThat(metrics.getReturnCount()).isZero();
            assertThat(metrics.getCreatedCount()).isZero();
            assertThat(metrics.getDestroyedCount()).isZero();
            assertThat(metrics.getMaxBorrowDuration()).isEqualTo(Duration.ZERO);
            assertThat(metrics.getAverageBorrowDuration()).isEqualTo(Duration.ZERO);
            assertThat(metrics.getAverageWaitDuration()).isEqualTo(Duration.ZERO);
        }
    }

    @Nested
    @DisplayName("并发测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("并发记录指标")
        void testConcurrentRecording() throws InterruptedException {
            int threadCount = 10;
            int iterationsPerThread = 1000;
            Thread[] threads = new Thread[threadCount];

            for (int i = 0; i < threadCount; i++) {
                threads[i] = Thread.ofVirtual().unstarted(() -> {
                    for (int j = 0; j < iterationsPerThread; j++) {
                        metrics.recordBorrow();
                        metrics.recordReturn();
                        metrics.recordBorrowDuration(Duration.ofNanos(100));
                        metrics.recordWaitDuration(Duration.ofNanos(50));
                    }
                });
            }

            for (Thread thread : threads) {
                thread.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            long expected = (long) threadCount * iterationsPerThread;
            assertThat(metrics.getBorrowCount()).isEqualTo(expected);
            assertThat(metrics.getReturnCount()).isEqualTo(expected);
        }
    }
}
