package cloud.opencode.base.lock.metrics;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * LockStats test - 锁统计记录测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.0
 */
class LockStatsTest {

    @Nested
    @DisplayName("Record Constructor Tests | 记录构造器测试")
    class RecordConstructorTests {

        @Test
        @DisplayName("should create record with all parameters")
        void shouldCreateRecordWithAllParameters() {
            Instant now = Instant.now();

            LockStats stats = new LockStats(
                    100L,
                    90L,
                    10L,
                    50L,
                    Duration.ofMillis(15),
                    Duration.ofMillis(100),
                    5,
                    now
            );

            assertThat(stats.acquireCount()).isEqualTo(100L);
            assertThat(stats.releaseCount()).isEqualTo(90L);
            assertThat(stats.timeoutCount()).isEqualTo(10L);
            assertThat(stats.contentionCount()).isEqualTo(50L);
            assertThat(stats.averageWaitTime()).isEqualTo(Duration.ofMillis(15));
            assertThat(stats.maxWaitTime()).isEqualTo(Duration.ofMillis(100));
            assertThat(stats.currentHoldCount()).isEqualTo(5);
            assertThat(stats.timestamp()).isEqualTo(now);
        }

        @Test
        @DisplayName("should accept zero values")
        void shouldAcceptZeroValues() {
            LockStats stats = new LockStats(
                    0L, 0L, 0L, 0L,
                    Duration.ZERO, Duration.ZERO,
                    0, Instant.now()
            );

            assertThat(stats.acquireCount()).isEqualTo(0L);
        }

        @Test
        @DisplayName("should accept null timestamp")
        void shouldAcceptNullTimestamp() {
            LockStats stats = new LockStats(
                    0L, 0L, 0L, 0L,
                    Duration.ZERO, Duration.ZERO,
                    0, null
            );

            assertThat(stats.timestamp()).isNull();
        }
    }

    @Nested
    @DisplayName("GetSuccessRate Tests | 获取成功率测试")
    class GetSuccessRateTests {

        @Test
        @DisplayName("getSuccessRate() should return 1.0 when no operations")
        void getSuccessRate_shouldReturn1WhenNoOperations() {
            LockStats stats = new LockStats(
                    0L, 0L, 0L, 0L,
                    Duration.ZERO, Duration.ZERO,
                    0, Instant.now()
            );

            assertThat(stats.getSuccessRate()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("getSuccessRate() should return 1.0 when all succeed")
        void getSuccessRate_shouldReturn1WhenAllSucceed() {
            LockStats stats = new LockStats(
                    100L, 100L, 0L, 0L,
                    Duration.ZERO, Duration.ZERO,
                    0, Instant.now()
            );

            assertThat(stats.getSuccessRate()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("getSuccessRate() should return 0.0 when all timeout")
        void getSuccessRate_shouldReturn0WhenAllTimeout() {
            LockStats stats = new LockStats(
                    0L, 0L, 100L, 0L,
                    Duration.ZERO, Duration.ZERO,
                    0, Instant.now()
            );

            assertThat(stats.getSuccessRate()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("getSuccessRate() should calculate correctly")
        void getSuccessRate_shouldCalculateCorrectly() {
            LockStats stats = new LockStats(
                    90L, 90L, 10L, 0L,
                    Duration.ZERO, Duration.ZERO,
                    0, Instant.now()
            );

            assertThat(stats.getSuccessRate()).isEqualTo(0.9);
        }
    }

    @Nested
    @DisplayName("GetContentionRate Tests | 获取竞争率测试")
    class GetContentionRateTests {

        @Test
        @DisplayName("getContentionRate() should return 0.0 when no acquires")
        void getContentionRate_shouldReturn0WhenNoAcquires() {
            LockStats stats = new LockStats(
                    0L, 0L, 0L, 0L,
                    Duration.ZERO, Duration.ZERO,
                    0, Instant.now()
            );

            assertThat(stats.getContentionRate()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("getContentionRate() should return 0.0 when no contention")
        void getContentionRate_shouldReturn0WhenNoContention() {
            LockStats stats = new LockStats(
                    100L, 100L, 0L, 0L,
                    Duration.ZERO, Duration.ZERO,
                    0, Instant.now()
            );

            assertThat(stats.getContentionRate()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("getContentionRate() should return 1.0 when all have contention")
        void getContentionRate_shouldReturn1WhenAllHaveContention() {
            LockStats stats = new LockStats(
                    100L, 100L, 0L, 100L,
                    Duration.ofMillis(10), Duration.ofMillis(50),
                    0, Instant.now()
            );

            assertThat(stats.getContentionRate()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("getContentionRate() should calculate correctly")
        void getContentionRate_shouldCalculateCorrectly() {
            LockStats stats = new LockStats(
                    100L, 100L, 0L, 25L,
                    Duration.ofMillis(10), Duration.ofMillis(50),
                    0, Instant.now()
            );

            assertThat(stats.getContentionRate()).isEqualTo(0.25);
        }
    }

    @Nested
    @DisplayName("GetTimeoutRate Tests | 获取超时率测试")
    class GetTimeoutRateTests {

        @Test
        @DisplayName("getTimeoutRate() should return 0.0 when no operations")
        void getTimeoutRate_shouldReturn0WhenNoOperations() {
            LockStats stats = new LockStats(
                    0L, 0L, 0L, 0L,
                    Duration.ZERO, Duration.ZERO,
                    0, Instant.now()
            );

            assertThat(stats.getTimeoutRate()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("getTimeoutRate() should return 0.0 when no timeouts")
        void getTimeoutRate_shouldReturn0WhenNoTimeouts() {
            LockStats stats = new LockStats(
                    100L, 100L, 0L, 0L,
                    Duration.ZERO, Duration.ZERO,
                    0, Instant.now()
            );

            assertThat(stats.getTimeoutRate()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("getTimeoutRate() should return 1.0 when all timeout")
        void getTimeoutRate_shouldReturn1WhenAllTimeout() {
            LockStats stats = new LockStats(
                    0L, 0L, 100L, 0L,
                    Duration.ZERO, Duration.ZERO,
                    0, Instant.now()
            );

            assertThat(stats.getTimeoutRate()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("getTimeoutRate() should calculate correctly")
        void getTimeoutRate_shouldCalculateCorrectly() {
            LockStats stats = new LockStats(
                    90L, 90L, 10L, 0L,
                    Duration.ZERO, Duration.ZERO,
                    0, Instant.now()
            );

            assertThat(stats.getTimeoutRate()).isEqualTo(0.1);
        }
    }

    @Nested
    @DisplayName("Record Methods Tests | 记录方法测试")
    class RecordMethodsTests {

        @Test
        @DisplayName("equals() should work correctly")
        void equals_shouldWorkCorrectly() {
            Instant now = Instant.now();

            LockStats stats1 = new LockStats(
                    100L, 90L, 10L, 50L,
                    Duration.ofMillis(15), Duration.ofMillis(100),
                    5, now
            );

            LockStats stats2 = new LockStats(
                    100L, 90L, 10L, 50L,
                    Duration.ofMillis(15), Duration.ofMillis(100),
                    5, now
            );

            LockStats stats3 = new LockStats(
                    200L, 90L, 10L, 50L,
                    Duration.ofMillis(15), Duration.ofMillis(100),
                    5, now
            );

            assertThat(stats1).isEqualTo(stats2);
            assertThat(stats1).isNotEqualTo(stats3);
        }

        @Test
        @DisplayName("hashCode() should be consistent")
        void hashCode_shouldBeConsistent() {
            Instant now = Instant.now();

            LockStats stats1 = new LockStats(
                    100L, 90L, 10L, 50L,
                    Duration.ofMillis(15), Duration.ofMillis(100),
                    5, now
            );

            LockStats stats2 = new LockStats(
                    100L, 90L, 10L, 50L,
                    Duration.ofMillis(15), Duration.ofMillis(100),
                    5, now
            );

            assertThat(stats1.hashCode()).isEqualTo(stats2.hashCode());
        }

        @Test
        @DisplayName("toString() should return readable string")
        void toString_shouldReturnReadableString() {
            LockStats stats = new LockStats(
                    100L, 90L, 10L, 50L,
                    Duration.ofMillis(15), Duration.ofMillis(100),
                    5, Instant.now()
            );

            String str = stats.toString();

            assertThat(str).contains("LockStats");
            assertThat(str).contains("acquireCount=100");
            assertThat(str).contains("releaseCount=90");
            assertThat(str).contains("timeoutCount=10");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests | 边界情况测试")
    class EdgeCasesTests {

        @Test
        @DisplayName("should handle large numbers")
        void shouldHandleLargeNumbers() {
            LockStats stats = new LockStats(
                    Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE,
                    Duration.ofDays(365), Duration.ofDays(365),
                    Integer.MAX_VALUE, Instant.now()
            );

            assertThat(stats.acquireCount()).isEqualTo(Long.MAX_VALUE);
        }

        @Test
        @DisplayName("rates should handle edge cases without exception")
        void rates_shouldHandleEdgeCasesWithoutException() {
            LockStats stats = new LockStats(
                    Long.MAX_VALUE, Long.MAX_VALUE, 1L, Long.MAX_VALUE,
                    Duration.ZERO, Duration.ZERO,
                    0, Instant.now()
            );

            assertThatCode(() -> {
                stats.getSuccessRate();
                stats.getContentionRate();
                stats.getTimeoutRate();
            }).doesNotThrowAnyException();
        }
    }
}
