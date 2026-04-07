package cloud.opencode.base.pool;

import cloud.opencode.base.pool.policy.EvictionPolicy;
import cloud.opencode.base.pool.policy.WaitPolicy;
import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * PoolConfigTest Tests
 * PoolConfigTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
@DisplayName("PoolConfig 测试")
class PoolConfigTest {

    @Nested
    @DisplayName("默认值测试")
    class DefaultValuesTests {

        @Test
        @DisplayName("defaults返回默认配置")
        void testDefaults() {
            PoolConfig config = PoolConfig.defaults();

            assertThat(config.maxTotal()).isEqualTo(8);
            assertThat(config.maxIdle()).isEqualTo(8);
            assertThat(config.minIdle()).isZero();
            assertThat(config.maxWait()).isEqualTo(Duration.ofSeconds(30));
            assertThat(config.testOnBorrow()).isFalse();
            assertThat(config.testOnReturn()).isFalse();
            assertThat(config.testOnCreate()).isFalse();
            assertThat(config.testWhileIdle()).isFalse();
            assertThat(config.waitPolicy()).isEqualTo(WaitPolicy.BLOCK);
            assertThat(config.lifo()).isTrue();
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("builder创建构建器")
        void testBuilder() {
            PoolConfig.Builder builder = PoolConfig.builder();
            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("maxTotal设置最大对象总数")
        void testMaxTotal() {
            PoolConfig config = PoolConfig.builder()
                    .maxTotal(20)
                    .build();

            assertThat(config.maxTotal()).isEqualTo(20);
        }

        @Test
        @DisplayName("maxIdle设置最大空闲数")
        void testMaxIdle() {
            PoolConfig config = PoolConfig.builder()
                    .maxTotal(20)
                    .maxIdle(10)
                    .build();

            assertThat(config.maxIdle()).isEqualTo(10);
        }

        @Test
        @DisplayName("minIdle设置最小空闲数")
        void testMinIdle() {
            PoolConfig config = PoolConfig.builder()
                    .minIdle(5)
                    .build();

            assertThat(config.minIdle()).isEqualTo(5);
        }

        @Test
        @DisplayName("maxWait设置最大等待时间")
        void testMaxWait() {
            PoolConfig config = PoolConfig.builder()
                    .maxWait(Duration.ofSeconds(10))
                    .build();

            assertThat(config.maxWait()).isEqualTo(Duration.ofSeconds(10));
        }

        @Test
        @DisplayName("minEvictableIdleTime设置最小可驱逐空闲时间")
        void testMinEvictableIdleTime() {
            PoolConfig config = PoolConfig.builder()
                    .minEvictableIdleTime(Duration.ofMinutes(10))
                    .build();

            assertThat(config.minEvictableIdleTime()).isEqualTo(Duration.ofMinutes(10));
        }

        @Test
        @DisplayName("timeBetweenEvictionRuns设置驱逐运行间隔")
        void testTimeBetweenEvictionRuns() {
            PoolConfig config = PoolConfig.builder()
                    .timeBetweenEvictionRuns(Duration.ofMinutes(5))
                    .build();

            assertThat(config.timeBetweenEvictionRuns()).isEqualTo(Duration.ofMinutes(5));
        }

        @Test
        @DisplayName("numTestsPerEvictionRun设置每次驱逐测试数")
        void testNumTestsPerEvictionRun() {
            PoolConfig config = PoolConfig.builder()
                    .numTestsPerEvictionRun(5)
                    .build();

            assertThat(config.numTestsPerEvictionRun()).isEqualTo(5);
        }

        @Test
        @DisplayName("testOnBorrow设置借用时测试")
        void testTestOnBorrow() {
            PoolConfig config = PoolConfig.builder()
                    .testOnBorrow(true)
                    .build();

            assertThat(config.testOnBorrow()).isTrue();
        }

        @Test
        @DisplayName("testOnReturn设置归还时测试")
        void testTestOnReturn() {
            PoolConfig config = PoolConfig.builder()
                    .testOnReturn(true)
                    .build();

            assertThat(config.testOnReturn()).isTrue();
        }

        @Test
        @DisplayName("testOnCreate设置创建时测试")
        void testTestOnCreate() {
            PoolConfig config = PoolConfig.builder()
                    .testOnCreate(true)
                    .build();

            assertThat(config.testOnCreate()).isTrue();
        }

        @Test
        @DisplayName("testWhileIdle设置空闲时测试")
        void testTestWhileIdle() {
            PoolConfig config = PoolConfig.builder()
                    .testWhileIdle(true)
                    .build();

            assertThat(config.testWhileIdle()).isTrue();
        }

        @Test
        @DisplayName("waitPolicy设置等待策略")
        void testWaitPolicy() {
            PoolConfig config = PoolConfig.builder()
                    .waitPolicy(WaitPolicy.FAIL)
                    .build();

            assertThat(config.waitPolicy()).isEqualTo(WaitPolicy.FAIL);
        }

        @Test
        @DisplayName("lifo设置LIFO顺序")
        void testLifo() {
            PoolConfig config = PoolConfig.builder()
                    .lifo(false)
                    .build();

            assertThat(config.lifo()).isFalse();
        }

        @Test
        @DisplayName("evictionPolicy设置驱逐策略")
        void testEvictionPolicy() {
            EvictionPolicy<String> policy = new EvictionPolicy.IdleTime<>(Duration.ofMinutes(10));
            PoolConfig config = PoolConfig.builder()
                    .evictionPolicy(policy)
                    .build();

            assertThat(config.evictionPolicy()).isEqualTo(policy);
        }

        @Test
        @DisplayName("链式调用设置多个属性")
        void testChainedConfiguration() {
            PoolConfig config = PoolConfig.builder()
                    .maxTotal(20)
                    .maxIdle(10)
                    .minIdle(5)
                    .maxWait(Duration.ofSeconds(10))
                    .testOnBorrow(true)
                    .testOnReturn(true)
                    .waitPolicy(WaitPolicy.BLOCK)
                    .lifo(true)
                    .build();

            assertThat(config.maxTotal()).isEqualTo(20);
            assertThat(config.maxIdle()).isEqualTo(10);
            assertThat(config.minIdle()).isEqualTo(5);
            assertThat(config.maxWait()).isEqualTo(Duration.ofSeconds(10));
            assertThat(config.testOnBorrow()).isTrue();
            assertThat(config.testOnReturn()).isTrue();
            assertThat(config.waitPolicy()).isEqualTo(WaitPolicy.BLOCK);
            assertThat(config.lifo()).isTrue();
        }
    }

    @Nested
    @DisplayName("辅助方法测试")
    class HelperMethodTests {

        @Test
        @DisplayName("isEvictionEnabled当间隔为零时返回false")
        void testIsEvictionEnabledZero() {
            PoolConfig config = PoolConfig.builder()
                    .timeBetweenEvictionRuns(Duration.ZERO)
                    .build();

            assertThat(config.isEvictionEnabled()).isFalse();
        }

        @Test
        @DisplayName("isEvictionEnabled当间隔为正时返回true")
        void testIsEvictionEnabledPositive() {
            PoolConfig config = PoolConfig.builder()
                    .timeBetweenEvictionRuns(Duration.ofMinutes(5))
                    .build();

            assertThat(config.isEvictionEnabled()).isTrue();
        }

        @Test
        @DisplayName("isEvictionEnabled当间隔为负时返回false")
        void testIsEvictionEnabledNegative() {
            PoolConfig config = PoolConfig.builder()
                    .timeBetweenEvictionRuns(Duration.ofSeconds(-1))
                    .build();

            assertThat(config.isEvictionEnabled()).isFalse();
        }

        @Test
        @DisplayName("blockWhenExhausted当策略为BLOCK时返回true")
        void testBlockWhenExhaustedBlock() {
            PoolConfig config = PoolConfig.builder()
                    .waitPolicy(WaitPolicy.BLOCK)
                    .build();

            assertThat(config.blockWhenExhausted()).isTrue();
        }

        @Test
        @DisplayName("blockWhenExhausted当策略为FAIL时返回false")
        void testBlockWhenExhaustedFail() {
            PoolConfig config = PoolConfig.builder()
                    .waitPolicy(WaitPolicy.FAIL)
                    .build();

            assertThat(config.blockWhenExhausted()).isFalse();
        }

        @Test
        @DisplayName("blockWhenExhausted当策略为GROW时返回false")
        void testBlockWhenExhaustedGrow() {
            PoolConfig config = PoolConfig.builder()
                    .waitPolicy(WaitPolicy.GROW)
                    .build();

            assertThat(config.blockWhenExhausted()).isFalse();
        }
    }

    @Nested
    @DisplayName("Record方法测试")
    class RecordMethodTests {

        @Test
        @DisplayName("equals比较相同配置返回true")
        void testEquals() {
            PoolConfig config1 = PoolConfig.builder()
                    .maxTotal(10)
                    .build();
            PoolConfig config2 = PoolConfig.builder()
                    .maxTotal(10)
                    .build();

            assertThat(config1).isEqualTo(config2);
        }

        @Test
        @DisplayName("hashCode相同配置返回相同值")
        void testHashCode() {
            PoolConfig config1 = PoolConfig.builder()
                    .maxTotal(10)
                    .build();
            PoolConfig config2 = PoolConfig.builder()
                    .maxTotal(10)
                    .build();

            assertThat(config1.hashCode()).isEqualTo(config2.hashCode());
        }

        @Test
        @DisplayName("toString返回字符串表示")
        void testToString() {
            PoolConfig config = PoolConfig.defaults();

            assertThat(config.toString()).contains("PoolConfig");
        }
    }
}
