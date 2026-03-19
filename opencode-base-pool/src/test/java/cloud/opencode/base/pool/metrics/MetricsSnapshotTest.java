package cloud.opencode.base.pool.metrics;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * MetricsSnapshotTest Tests
 * MetricsSnapshotTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
@DisplayName("MetricsSnapshot 测试")
class MetricsSnapshotTest {

    @Nested
    @DisplayName("Record组件测试")
    class RecordComponentTests {

        @Test
        @DisplayName("所有组件返回正确值")
        void testAllComponents() {
            Instant now = Instant.now();
            MetricsSnapshot snapshot = new MetricsSnapshot(
                    100L, 95L, 20L, 5L,
                    10, 15,
                    Duration.ofMillis(50),
                    Duration.ofMillis(200),
                    Duration.ofMillis(10),
                    now
            );

            assertThat(snapshot.borrowCount()).isEqualTo(100L);
            assertThat(snapshot.returnCount()).isEqualTo(95L);
            assertThat(snapshot.createdCount()).isEqualTo(20L);
            assertThat(snapshot.destroyedCount()).isEqualTo(5L);
            assertThat(snapshot.currentActive()).isEqualTo(10);
            assertThat(snapshot.currentIdle()).isEqualTo(15);
            assertThat(snapshot.avgBorrowDuration()).isEqualTo(Duration.ofMillis(50));
            assertThat(snapshot.maxBorrowDuration()).isEqualTo(Duration.ofMillis(200));
            assertThat(snapshot.avgWaitDuration()).isEqualTo(Duration.ofMillis(10));
            assertThat(snapshot.timestamp()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("totalCount方法测试")
    class TotalCountTests {

        @Test
        @DisplayName("返回活跃数加空闲数")
        void testTotalCount() {
            MetricsSnapshot snapshot = createSnapshot(10, 15);
            assertThat(snapshot.totalCount()).isEqualTo(25);
        }

        @Test
        @DisplayName("处理零值")
        void testTotalCountZero() {
            MetricsSnapshot snapshot = createSnapshot(0, 0);
            assertThat(snapshot.totalCount()).isZero();
        }
    }

    @Nested
    @DisplayName("utilizationRate方法测试")
    class UtilizationRateTests {

        @Test
        @DisplayName("计算正确的利用率")
        void testUtilizationRate() {
            MetricsSnapshot snapshot = createSnapshot(10, 10);
            assertThat(snapshot.utilizationRate()).isEqualTo(0.5);
        }

        @Test
        @DisplayName("全部活跃时返回1.0")
        void testFullUtilization() {
            MetricsSnapshot snapshot = createSnapshot(20, 0);
            assertThat(snapshot.utilizationRate()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("全部空闲时返回0.0")
        void testNoUtilization() {
            MetricsSnapshot snapshot = createSnapshot(0, 20);
            assertThat(snapshot.utilizationRate()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("总数为零时返回0.0")
        void testZeroTotal() {
            MetricsSnapshot snapshot = createSnapshot(0, 0);
            assertThat(snapshot.utilizationRate()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("hitRate方法测试")
    class HitRateTests {

        @Test
        @DisplayName("计算正确的命中率")
        void testHitRate() {
            MetricsSnapshot snapshot = new MetricsSnapshot(
                    100L, 90L, 20L, 5L,
                    10, 15,
                    Duration.ZERO, Duration.ZERO, Duration.ZERO,
                    Instant.now()
            );
            assertThat(snapshot.hitRate()).isEqualTo(0.9);
        }

        @Test
        @DisplayName("借用为零时返回0.0")
        void testZeroBorrow() {
            MetricsSnapshot snapshot = new MetricsSnapshot(
                    0L, 0L, 0L, 0L,
                    0, 0,
                    Duration.ZERO, Duration.ZERO, Duration.ZERO,
                    Instant.now()
            );
            assertThat(snapshot.hitRate()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("100%命中率")
        void testFullHitRate() {
            MetricsSnapshot snapshot = new MetricsSnapshot(
                    100L, 100L, 20L, 5L,
                    10, 15,
                    Duration.ZERO, Duration.ZERO, Duration.ZERO,
                    Instant.now()
            );
            assertThat(snapshot.hitRate()).isEqualTo(1.0);
        }
    }

    @Nested
    @DisplayName("creationRate方法测试")
    class CreationRateTests {

        @Test
        @DisplayName("计算正确的创建率")
        void testCreationRate() {
            MetricsSnapshot snapshot = new MetricsSnapshot(
                    100L, 90L, 20L, 5L,
                    10, 15,
                    Duration.ZERO, Duration.ZERO, Duration.ZERO,
                    Instant.now()
            );
            assertThat(snapshot.creationRate()).isEqualTo(0.2);
        }

        @Test
        @DisplayName("借用为零时返回0.0")
        void testZeroBorrow() {
            MetricsSnapshot snapshot = new MetricsSnapshot(
                    0L, 0L, 10L, 0L,
                    0, 0,
                    Duration.ZERO, Duration.ZERO, Duration.ZERO,
                    Instant.now()
            );
            assertThat(snapshot.creationRate()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("Record标准方法测试")
    class RecordStandardMethodTests {

        @Test
        @DisplayName("equals比较相同值返回true")
        void testEquals() {
            Instant now = Instant.now();
            MetricsSnapshot snapshot1 = new MetricsSnapshot(
                    100L, 90L, 20L, 5L, 10, 15,
                    Duration.ofMillis(50), Duration.ofMillis(200), Duration.ofMillis(10),
                    now
            );
            MetricsSnapshot snapshot2 = new MetricsSnapshot(
                    100L, 90L, 20L, 5L, 10, 15,
                    Duration.ofMillis(50), Duration.ofMillis(200), Duration.ofMillis(10),
                    now
            );

            assertThat(snapshot1).isEqualTo(snapshot2);
        }

        @Test
        @DisplayName("hashCode相同值返回相同结果")
        void testHashCode() {
            Instant now = Instant.now();
            MetricsSnapshot snapshot1 = new MetricsSnapshot(
                    100L, 90L, 20L, 5L, 10, 15,
                    Duration.ofMillis(50), Duration.ofMillis(200), Duration.ofMillis(10),
                    now
            );
            MetricsSnapshot snapshot2 = new MetricsSnapshot(
                    100L, 90L, 20L, 5L, 10, 15,
                    Duration.ofMillis(50), Duration.ofMillis(200), Duration.ofMillis(10),
                    now
            );

            assertThat(snapshot1.hashCode()).isEqualTo(snapshot2.hashCode());
        }

        @Test
        @DisplayName("toString返回字符串表示")
        void testToString() {
            MetricsSnapshot snapshot = createSnapshot(10, 15);
            String str = snapshot.toString();

            assertThat(str).contains("MetricsSnapshot");
        }
    }

    // ==================== Helper Methods ====================

    private MetricsSnapshot createSnapshot(int active, int idle) {
        return new MetricsSnapshot(
                100L, 90L, 20L, 5L,
                active, idle,
                Duration.ZERO, Duration.ZERO, Duration.ZERO,
                Instant.now()
        );
    }
}
