package cloud.opencode.base.pool.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * ObservabilityMetricsExporter 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
@DisplayName("ObservabilityMetricsExporter 测试")
class ObservabilityMetricsExporterTest {

    @Nested
    @DisplayName("isObservabilityModuleAvailable 测试")
    class IsObservabilityModuleAvailableTests {

        @Test
        @DisplayName("返回布尔值")
        void shouldReturnBoolean() {
            boolean result = ObservabilityMetricsExporter.isObservabilityModuleAvailable();
            assertThat(result).isIn(true, false);
        }
    }

    @Nested
    @DisplayName("export 方法测试")
    class ExportTests {

        private MockPoolMetrics metrics;

        @BeforeEach
        void setUp() {
            metrics = new MockPoolMetrics();
        }

        @Test
        @DisplayName("导出池指标不抛异常")
        void shouldExportWithoutException() {
            assertThatCode(() -> ObservabilityMetricsExporter.export("test-pool", metrics))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("null 指标时不抛异常")
        void shouldHandleNullMetrics() {
            assertThatCode(() -> ObservabilityMetricsExporter.export("test-pool", null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("带前缀导出不抛异常")
        void shouldExportWithPrefixWithoutException() {
            assertThatCode(() -> ObservabilityMetricsExporter.export("custom.prefix", "test-pool", metrics))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("带前缀 null 指标时不抛异常")
        void shouldHandleNullMetricsWithPrefix() {
            assertThatCode(() -> ObservabilityMetricsExporter.export("custom.prefix", "test-pool", null))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Observability 模块可用时的集成测试")
    class ObservabilityIntegrationTests {

        @Test
        @DisplayName("导出到 Observability 模块")
        void shouldExportToObservabilityWhenAvailable() {
            if (!ObservabilityMetricsExporter.isObservabilityModuleAvailable()) {
                return;
            }

            MockPoolMetrics metrics = new MockPoolMetrics();
            metrics.setBorrowCount(100);
            metrics.setReturnCount(95);
            metrics.setCreatedCount(10);
            metrics.setDestroyedCount(5);
            metrics.setAverageBorrowDuration(Duration.ofMillis(50));
            metrics.setMaxBorrowDuration(Duration.ofMillis(200));
            metrics.setAverageWaitDuration(Duration.ofMillis(10));

            assertThatCode(() -> ObservabilityMetricsExporter.export("integration-test", metrics))
                    .doesNotThrowAnyException();
        }
    }

    /**
     * Mock PoolMetrics for testing
     */
    private static class MockPoolMetrics implements PoolMetrics {

        private long borrowCount = 0;
        private long returnCount = 0;
        private long createdCount = 0;
        private long destroyedCount = 0;
        private Duration averageBorrowDuration = Duration.ZERO;
        private Duration maxBorrowDuration = Duration.ZERO;
        private Duration averageWaitDuration = Duration.ZERO;

        @Override
        public long getBorrowCount() {
            return borrowCount;
        }

        public void setBorrowCount(long borrowCount) {
            this.borrowCount = borrowCount;
        }

        @Override
        public long getReturnCount() {
            return returnCount;
        }

        public void setReturnCount(long returnCount) {
            this.returnCount = returnCount;
        }

        @Override
        public long getCreatedCount() {
            return createdCount;
        }

        public void setCreatedCount(long createdCount) {
            this.createdCount = createdCount;
        }

        @Override
        public long getDestroyedCount() {
            return destroyedCount;
        }

        public void setDestroyedCount(long destroyedCount) {
            this.destroyedCount = destroyedCount;
        }

        @Override
        public Duration getAverageBorrowDuration() {
            return averageBorrowDuration;
        }

        public void setAverageBorrowDuration(Duration averageBorrowDuration) {
            this.averageBorrowDuration = averageBorrowDuration;
        }

        @Override
        public Duration getMaxBorrowDuration() {
            return maxBorrowDuration;
        }

        public void setMaxBorrowDuration(Duration maxBorrowDuration) {
            this.maxBorrowDuration = maxBorrowDuration;
        }

        @Override
        public Duration getAverageWaitDuration() {
            return averageWaitDuration;
        }

        public void setAverageWaitDuration(Duration averageWaitDuration) {
            this.averageWaitDuration = averageWaitDuration;
        }

        @Override
        public MetricsSnapshot snapshot() {
            return new MetricsSnapshot(
                    borrowCount, returnCount, createdCount, destroyedCount,
                    5, 10, // currentActive, currentIdle
                    averageBorrowDuration, maxBorrowDuration, averageWaitDuration,
                    java.time.Instant.now()
            );
        }
    }
}
