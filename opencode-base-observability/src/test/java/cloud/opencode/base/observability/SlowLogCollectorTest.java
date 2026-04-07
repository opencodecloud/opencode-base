package cloud.opencode.base.observability;

import cloud.opencode.base.observability.exception.ObservabilityException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for {@link SlowLogCollector}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-observability V1.0.0
 */
@DisplayName("SlowLogCollector - 慢日志收集器")
class SlowLogCollectorTest {

    // ==================== Factory Methods ====================

    @Nested
    @DisplayName("工厂方法")
    class FactoryMethods {

        @Test
        @DisplayName("默认工厂方法使用 10ms 阈值和 1024 最大条目数")
        void defaultFactoryMethodUsesDefaults() {
            SlowLogCollector collector = SlowLogCollector.create();

            assertThat(collector.threshold()).isEqualTo(Duration.ofMillis(10));
            assertThat(collector.maxEntries()).isEqualTo(1024);
            assertThat(collector.count()).isZero();
            assertThat(collector.getEntries()).isEmpty();
        }

        @Test
        @DisplayName("自定义阈值工厂方法使用默认最大条目数")
        void customThresholdFactoryMethod() {
            Duration threshold = Duration.ofMillis(50);
            SlowLogCollector collector = SlowLogCollector.create(threshold);

            assertThat(collector.threshold()).isEqualTo(threshold);
            assertThat(collector.maxEntries()).isEqualTo(1024);
        }

        @Test
        @DisplayName("自定义阈值和最大条目数工厂方法")
        void customThresholdAndMaxEntriesFactoryMethod() {
            Duration threshold = Duration.ofMillis(100);
            int maxEntries = 50;
            SlowLogCollector collector = SlowLogCollector.create(threshold, maxEntries);

            assertThat(collector.threshold()).isEqualTo(threshold);
            assertThat(collector.maxEntries()).isEqualTo(maxEntries);
        }

        @Test
        @DisplayName("空阈值抛出 NullPointerException")
        void nullThresholdThrows() {
            assertThatNullPointerException()
                    .isThrownBy(() -> SlowLogCollector.create(null));
        }

        @Test
        @DisplayName("非正数最大条目数抛出 ObservabilityException")
        void nonPositiveMaxEntriesThrows() {
            assertThatThrownBy(() -> SlowLogCollector.create(Duration.ofMillis(10), 0))
                    .isInstanceOf(ObservabilityException.class);

            assertThatThrownBy(() -> SlowLogCollector.create(Duration.ofMillis(10), -1))
                    .isInstanceOf(ObservabilityException.class);
        }

        @Test
        @DisplayName("负值阈值抛出 ObservabilityException（防止全量记录 DoS）")
        void negativeThresholdThrows() {
            assertThatThrownBy(() -> SlowLogCollector.create(Duration.ofMillis(-1)))
                    .isInstanceOf(ObservabilityException.class);
        }

        @Test
        @DisplayName("零阈值抛出 ObservabilityException（防止全量记录 DoS）")
        void zeroThresholdThrows() {
            assertThatThrownBy(() -> SlowLogCollector.create(Duration.ZERO))
                    .isInstanceOf(ObservabilityException.class);
        }
    }

    // ==================== Record ====================

    @Nested
    @DisplayName("记录操作")
    class RecordOperations {

        @Test
        @DisplayName("超过阈值的操作被记录")
        void recordAboveThreshold() {
            SlowLogCollector collector = SlowLogCollector.create(Duration.ofMillis(10));

            collector.record("GET", "user:123", Duration.ofMillis(15));

            assertThat(collector.getEntries()).hasSize(1);
            assertThat(collector.count()).isEqualTo(1);

            SlowLogCollector.Entry entry = collector.getEntries().getFirst();
            assertThat(entry.operation()).isEqualTo("GET");
            assertThat(entry.key()).isEqualTo("user:123");
            assertThat(entry.elapsed()).isEqualTo(Duration.ofMillis(15));
            assertThat(entry.timestamp()).isNotNull();
            assertThat(entry.threadName()).isNotBlank();
        }

        @Test
        @DisplayName("等于阈值的操作不被记录")
        void recordEqualToThresholdIgnored() {
            SlowLogCollector collector = SlowLogCollector.create(Duration.ofMillis(10));

            collector.record("GET", "user:123", Duration.ofMillis(10));

            assertThat(collector.getEntries()).isEmpty();
            assertThat(collector.count()).isZero();
        }

        @Test
        @DisplayName("低于阈值的操作不被记录")
        void recordBelowThresholdIgnored() {
            SlowLogCollector collector = SlowLogCollector.create(Duration.ofMillis(10));

            collector.record("GET", "user:123", Duration.ofMillis(5));

            assertThat(collector.getEntries()).isEmpty();
            assertThat(collector.count()).isZero();
        }

        @Test
        @DisplayName("空参数抛出 NullPointerException")
        void nullParametersThrow() {
            SlowLogCollector collector = SlowLogCollector.create();

            assertThatNullPointerException()
                    .isThrownBy(() -> collector.record(null, "key", Duration.ofMillis(50)));
            assertThatNullPointerException()
                    .isThrownBy(() -> collector.record("GET", null, Duration.ofMillis(50)));
            assertThatNullPointerException()
                    .isThrownBy(() -> collector.record("GET", "key", null));
        }

        @Test
        @DisplayName("记录多个操作，计数正确递增")
        void multipleRecordsIncrementCount() {
            SlowLogCollector collector = SlowLogCollector.create(Duration.ofMillis(5));

            collector.record("GET", "key1", Duration.ofMillis(10));
            collector.record("PUT", "key2", Duration.ofMillis(20));
            collector.record("DELETE", "key3", Duration.ofMillis(30));

            assertThat(collector.count()).isEqualTo(3);
            assertThat(collector.getEntries()).hasSize(3);
        }
    }

    // ==================== GetEntries ====================

    @Nested
    @DisplayName("获取条目")
    class GetEntries {

        @Test
        @DisplayName("条目按从新到旧排序")
        void entriesOrderedNewestFirst() {
            SlowLogCollector collector = SlowLogCollector.create(Duration.ofMillis(5));

            collector.record("GET", "key1", Duration.ofMillis(10));
            collector.record("PUT", "key2", Duration.ofMillis(20));
            collector.record("DELETE", "key3", Duration.ofMillis(30));

            var entries = collector.getEntries();
            assertThat(entries).hasSize(3);
            assertThat(entries.get(0).operation()).isEqualTo("DELETE");
            assertThat(entries.get(1).operation()).isEqualTo("PUT");
            assertThat(entries.get(2).operation()).isEqualTo("GET");
        }

        @Test
        @DisplayName("带限制的获取条目返回最新的 N 条")
        void getEntriesWithLimitReturnsLatest() {
            SlowLogCollector collector = SlowLogCollector.create(Duration.ofMillis(5));

            collector.record("GET", "key1", Duration.ofMillis(10));
            collector.record("PUT", "key2", Duration.ofMillis(20));
            collector.record("DELETE", "key3", Duration.ofMillis(30));

            var entries = collector.getEntries(2);
            assertThat(entries).hasSize(2);
            assertThat(entries.get(0).operation()).isEqualTo("DELETE");
            assertThat(entries.get(1).operation()).isEqualTo("PUT");
        }

        @Test
        @DisplayName("限制为 0 返回空列表")
        void limitZeroReturnsEmpty() {
            SlowLogCollector collector = SlowLogCollector.create(Duration.ofMillis(5));
            collector.record("GET", "key1", Duration.ofMillis(10));

            assertThat(collector.getEntries(0)).isEmpty();
        }

        @Test
        @DisplayName("负数限制抛出 ObservabilityException")
        void negativeLimitThrows() {
            SlowLogCollector collector = SlowLogCollector.create();

            assertThatThrownBy(() -> collector.getEntries(-1))
                    .isInstanceOf(ObservabilityException.class);
        }

        @Test
        @DisplayName("限制大于条目数返回所有条目")
        void limitExceedingEntriesReturnsAll() {
            SlowLogCollector collector = SlowLogCollector.create(Duration.ofMillis(5));
            collector.record("GET", "key1", Duration.ofMillis(10));
            collector.record("PUT", "key2", Duration.ofMillis(20));

            assertThat(collector.getEntries(100)).hasSize(2);
        }

        @Test
        @DisplayName("返回的列表是不可变的")
        void returnedListIsImmutable() {
            SlowLogCollector collector = SlowLogCollector.create(Duration.ofMillis(5));
            collector.record("GET", "key1", Duration.ofMillis(10));

            var entries = collector.getEntries();
            assertThatThrownBy(() -> entries.add(
                    new SlowLogCollector.Entry("X", "x", Duration.ofMillis(1), Instant.now(), "t")))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    // ==================== Bounded Buffer ====================

    @Nested
    @DisplayName("有界缓冲区")
    class BoundedBuffer {

        @Test
        @DisplayName("缓冲区满时驱逐最旧的条目")
        void evictsOldestWhenFull() {
            SlowLogCollector collector = SlowLogCollector.create(Duration.ofMillis(5), 3);

            collector.record("OP1", "k1", Duration.ofMillis(10));
            collector.record("OP2", "k2", Duration.ofMillis(20));
            collector.record("OP3", "k3", Duration.ofMillis(30));
            collector.record("OP4", "k4", Duration.ofMillis(40));

            var entries = collector.getEntries();
            assertThat(entries).hasSize(3);
            // Newest first: OP4, OP3, OP2; OP1 was evicted
            assertThat(entries.get(0).operation()).isEqualTo("OP4");
            assertThat(entries.get(1).operation()).isEqualTo("OP3");
            assertThat(entries.get(2).operation()).isEqualTo("OP2");
        }

        @Test
        @DisplayName("累积计数包含被驱逐的条目")
        void cumulativeCountIncludesEvictedEntries() {
            SlowLogCollector collector = SlowLogCollector.create(Duration.ofMillis(5), 2);

            collector.record("OP1", "k1", Duration.ofMillis(10));
            collector.record("OP2", "k2", Duration.ofMillis(20));
            collector.record("OP3", "k3", Duration.ofMillis(30));

            assertThat(collector.getEntries()).hasSize(2);
            assertThat(collector.count()).isEqualTo(3);
        }

        @Test
        @DisplayName("最大条目数为 1 时只保留最新条目")
        void maxEntriesOneKeepsOnlyLatest() {
            SlowLogCollector collector = SlowLogCollector.create(Duration.ofMillis(5), 1);

            collector.record("OP1", "k1", Duration.ofMillis(10));
            collector.record("OP2", "k2", Duration.ofMillis(20));

            assertThat(collector.getEntries()).hasSize(1);
            assertThat(collector.getEntries().getFirst().operation()).isEqualTo("OP2");
            assertThat(collector.count()).isEqualTo(2);
        }
    }

    // ==================== Clear ====================

    @Nested
    @DisplayName("清除操作")
    class ClearOperations {

        @Test
        @DisplayName("清除条目但保留累积计数")
        void clearEntriesButKeepCount() {
            SlowLogCollector collector = SlowLogCollector.create(Duration.ofMillis(5));

            collector.record("GET", "k1", Duration.ofMillis(10));
            collector.record("PUT", "k2", Duration.ofMillis(20));

            assertThat(collector.count()).isEqualTo(2);
            assertThat(collector.getEntries()).hasSize(2);

            collector.clear();

            assertThat(collector.getEntries()).isEmpty();
            assertThat(collector.count()).isEqualTo(2);
        }

        @Test
        @DisplayName("清除后可以继续记录")
        void canRecordAfterClear() {
            SlowLogCollector collector = SlowLogCollector.create(Duration.ofMillis(5));

            collector.record("GET", "k1", Duration.ofMillis(10));
            collector.clear();
            collector.record("PUT", "k2", Duration.ofMillis(20));

            assertThat(collector.getEntries()).hasSize(1);
            assertThat(collector.getEntries().getFirst().operation()).isEqualTo("PUT");
            assertThat(collector.count()).isEqualTo(2);
        }
    }

    // ==================== Stats ====================

    @Nested
    @DisplayName("统计信息")
    class StatsTests {

        @Test
        @DisplayName("无条目时返回 Stats.EMPTY")
        void emptyStatsWhenNoEntries() {
            SlowLogCollector collector = SlowLogCollector.create();

            SlowLogCollector.Stats stats = collector.stats();

            assertThat(stats).isSameAs(SlowLogCollector.Stats.EMPTY);
            assertThat(stats.totalSlowOps()).isZero();
            assertThat(stats.maxDuration()).isEqualTo(Duration.ZERO);
            assertThat(stats.avgDuration()).isEqualTo(Duration.ZERO);
            assertThat(stats.slowestOperation()).isEmpty();
        }

        @Test
        @DisplayName("清除后返回 Stats.EMPTY")
        void emptyStatsAfterClear() {
            SlowLogCollector collector = SlowLogCollector.create(Duration.ofMillis(5));
            collector.record("GET", "k1", Duration.ofMillis(10));
            collector.clear();

            assertThat(collector.stats()).isSameAs(SlowLogCollector.Stats.EMPTY);
        }

        @Test
        @DisplayName("正确计算最大耗时和最慢操作")
        void correctMaxDurationAndSlowestOperation() {
            SlowLogCollector collector = SlowLogCollector.create(Duration.ofMillis(5));

            collector.record("GET", "k1", Duration.ofMillis(10));
            collector.record("PUT", "k2", Duration.ofMillis(50));
            collector.record("DELETE", "k3", Duration.ofMillis(30));

            SlowLogCollector.Stats stats = collector.stats();

            assertThat(stats.totalSlowOps()).isEqualTo(3);
            assertThat(stats.maxDuration()).isEqualTo(Duration.ofMillis(50));
            assertThat(stats.slowestOperation()).isEqualTo("PUT");
        }

        @Test
        @DisplayName("正确计算平均耗时")
        void correctAverageDuration() {
            SlowLogCollector collector = SlowLogCollector.create(Duration.ofMillis(5));

            collector.record("GET", "k1", Duration.ofMillis(10));
            collector.record("PUT", "k2", Duration.ofMillis(20));
            collector.record("DELETE", "k3", Duration.ofMillis(30));

            SlowLogCollector.Stats stats = collector.stats();

            // Average of 10, 20, 30 = 20ms
            assertThat(stats.avgDuration()).isEqualTo(Duration.ofMillis(20));
        }

        @Test
        @DisplayName("单条目时统计信息正确")
        void singleEntryStats() {
            SlowLogCollector collector = SlowLogCollector.create(Duration.ofMillis(5));
            collector.record("GET", "k1", Duration.ofMillis(42));

            SlowLogCollector.Stats stats = collector.stats();

            assertThat(stats.totalSlowOps()).isEqualTo(1);
            assertThat(stats.maxDuration()).isEqualTo(Duration.ofMillis(42));
            assertThat(stats.avgDuration()).isEqualTo(Duration.ofMillis(42));
            assertThat(stats.slowestOperation()).isEqualTo("GET");
        }
    }

    // ==================== Entry Record ====================

    @Nested
    @DisplayName("Entry 记录")
    class EntryTests {

        @Test
        @DisplayName("Entry 构造时空参数抛出 NullPointerException")
        void entryNullParametersThrow() {
            Instant now = Instant.now();
            String thread = "test-thread";

            assertThatNullPointerException()
                    .isThrownBy(() -> new SlowLogCollector.Entry(null, "k", Duration.ofMillis(1), now, thread));
            assertThatNullPointerException()
                    .isThrownBy(() -> new SlowLogCollector.Entry("op", null, Duration.ofMillis(1), now, thread));
            assertThatNullPointerException()
                    .isThrownBy(() -> new SlowLogCollector.Entry("op", "k", null, now, thread));
            assertThatNullPointerException()
                    .isThrownBy(() -> new SlowLogCollector.Entry("op", "k", Duration.ofMillis(1), null, thread));
            assertThatNullPointerException()
                    .isThrownBy(() -> new SlowLogCollector.Entry("op", "k", Duration.ofMillis(1), now, null));
        }

        @Test
        @DisplayName("Entry 正确存储所有字段")
        void entryStoresAllFields() {
            Instant now = Instant.now();
            Duration elapsed = Duration.ofMillis(42);
            var entry = new SlowLogCollector.Entry("GET", "user:1", elapsed, now, "main");

            assertThat(entry.operation()).isEqualTo("GET");
            assertThat(entry.key()).isEqualTo("user:1");
            assertThat(entry.elapsed()).isEqualTo(elapsed);
            assertThat(entry.timestamp()).isEqualTo(now);
            assertThat(entry.threadName()).isEqualTo("main");
        }
    }
}
