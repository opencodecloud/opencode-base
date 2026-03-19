package cloud.opencode.base.pool.metrics;

import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * PoolMetricsTest Tests
 * PoolMetricsTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
@DisplayName("PoolMetrics 接口测试")
class PoolMetricsTest {

    @Nested
    @DisplayName("DefaultPoolMetrics实现测试")
    class DefaultPoolMetricsImplTests {

        @Test
        @DisplayName("初始借用计数为0")
        void testInitialBorrowCount() {
            PoolMetrics metrics = new DefaultPoolMetrics();
            assertThat(metrics.getBorrowCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("初始归还计数为0")
        void testInitialReturnCount() {
            PoolMetrics metrics = new DefaultPoolMetrics();
            assertThat(metrics.getReturnCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("初始创建计数为0")
        void testInitialCreatedCount() {
            PoolMetrics metrics = new DefaultPoolMetrics();
            assertThat(metrics.getCreatedCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("初始销毁计数为0")
        void testInitialDestroyedCount() {
            PoolMetrics metrics = new DefaultPoolMetrics();
            assertThat(metrics.getDestroyedCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("snapshot返回非空快照")
        void testSnapshot() {
            PoolMetrics metrics = new DefaultPoolMetrics();
            MetricsSnapshot snapshot = metrics.snapshot();
            assertThat(snapshot).isNotNull();
        }
    }

    @Nested
    @DisplayName("接口方法签名测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("接口定义所有必要方法")
        void testInterfaceHasMethods() {
            assertThat(PoolMetrics.class.getMethods())
                    .extracting("name")
                    .contains("getBorrowCount", "getReturnCount",
                            "getCreatedCount", "getDestroyedCount",
                            "getAverageBorrowDuration", "getMaxBorrowDuration",
                            "getAverageWaitDuration", "snapshot");
        }
    }
}
