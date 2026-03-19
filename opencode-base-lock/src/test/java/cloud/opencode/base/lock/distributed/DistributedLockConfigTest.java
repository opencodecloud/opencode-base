package cloud.opencode.base.lock.distributed;

import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * DistributedLockConfig test - 分布式锁配置测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.0
 */
class DistributedLockConfigTest {

    @Nested
    @DisplayName("Factory Methods | 工厂方法")
    class FactoryMethodsTests {

        @Test
        @DisplayName("builder() should create new builder")
        void builder_shouldCreateNewBuilder() {
            DistributedLockConfig.Builder builder = DistributedLockConfig.builder();

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("defaults() should return default config")
        void defaults_shouldReturnDefaultConfig() {
            DistributedLockConfig config = DistributedLockConfig.defaults();

            assertThat(config).isNotNull();
            assertThat(config.lockTimeout()).isEqualTo(Duration.ofSeconds(30));
            assertThat(config.leaseTime()).isEqualTo(Duration.ofSeconds(30));
            assertThat(config.renewInterval()).isEqualTo(Duration.ofSeconds(10));
            assertThat(config.autoRenew()).isTrue();
            assertThat(config.retryCount()).isEqualTo(3);
            assertThat(config.retryInterval()).isEqualTo(Duration.ofMillis(100));
            assertThat(config.enableFencing()).isFalse();
        }
    }

    @Nested
    @DisplayName("Builder Tests | 构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("lockTimeout() should set lock timeout")
        void lockTimeout_shouldSetLockTimeout() {
            DistributedLockConfig config = DistributedLockConfig.builder()
                    .lockTimeout(Duration.ofMinutes(1))
                    .build();

            assertThat(config.lockTimeout()).isEqualTo(Duration.ofMinutes(1));
        }

        @Test
        @DisplayName("leaseTime() should set lease time")
        void leaseTime_shouldSetLeaseTime() {
            DistributedLockConfig config = DistributedLockConfig.builder()
                    .leaseTime(Duration.ofMinutes(5))
                    .build();

            assertThat(config.leaseTime()).isEqualTo(Duration.ofMinutes(5));
        }

        @Test
        @DisplayName("renewInterval() should set renew interval")
        void renewInterval_shouldSetRenewInterval() {
            DistributedLockConfig config = DistributedLockConfig.builder()
                    .renewInterval(Duration.ofSeconds(5))
                    .build();

            assertThat(config.renewInterval()).isEqualTo(Duration.ofSeconds(5));
        }

        @Test
        @DisplayName("autoRenew() should set auto renew")
        void autoRenew_shouldSetAutoRenew() {
            DistributedLockConfig config = DistributedLockConfig.builder()
                    .autoRenew(false)
                    .build();

            assertThat(config.autoRenew()).isFalse();
        }

        @Test
        @DisplayName("retryCount() should set retry count")
        void retryCount_shouldSetRetryCount() {
            DistributedLockConfig config = DistributedLockConfig.builder()
                    .retryCount(5)
                    .build();

            assertThat(config.retryCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("retryInterval() should set retry interval")
        void retryInterval_shouldSetRetryInterval() {
            DistributedLockConfig config = DistributedLockConfig.builder()
                    .retryInterval(Duration.ofMillis(200))
                    .build();

            assertThat(config.retryInterval()).isEqualTo(Duration.ofMillis(200));
        }

        @Test
        @DisplayName("enableFencing() should set fencing enabled")
        void enableFencing_shouldSetFencingEnabled() {
            DistributedLockConfig config = DistributedLockConfig.builder()
                    .enableFencing(true)
                    .build();

            assertThat(config.enableFencing()).isTrue();
        }

        @Test
        @DisplayName("builder should chain all methods")
        void builder_shouldChainAllMethods() {
            DistributedLockConfig config = DistributedLockConfig.builder()
                    .lockTimeout(Duration.ofSeconds(60))
                    .leaseTime(Duration.ofSeconds(120))
                    .renewInterval(Duration.ofSeconds(30))
                    .autoRenew(true)
                    .retryCount(10)
                    .retryInterval(Duration.ofMillis(500))
                    .enableFencing(true)
                    .build();

            assertThat(config.lockTimeout()).isEqualTo(Duration.ofSeconds(60));
            assertThat(config.leaseTime()).isEqualTo(Duration.ofSeconds(120));
            assertThat(config.renewInterval()).isEqualTo(Duration.ofSeconds(30));
            assertThat(config.autoRenew()).isTrue();
            assertThat(config.retryCount()).isEqualTo(10);
            assertThat(config.retryInterval()).isEqualTo(Duration.ofMillis(500));
            assertThat(config.enableFencing()).isTrue();
        }
    }

    @Nested
    @DisplayName("Record Methods | 记录方法")
    class RecordMethodsTests {

        @Test
        @DisplayName("equals() should work correctly")
        void equals_shouldWorkCorrectly() {
            DistributedLockConfig config1 = DistributedLockConfig.builder()
                    .lockTimeout(Duration.ofSeconds(10))
                    .autoRenew(true)
                    .build();

            DistributedLockConfig config2 = DistributedLockConfig.builder()
                    .lockTimeout(Duration.ofSeconds(10))
                    .autoRenew(true)
                    .build();

            DistributedLockConfig config3 = DistributedLockConfig.builder()
                    .lockTimeout(Duration.ofSeconds(20))
                    .autoRenew(true)
                    .build();

            assertThat(config1).isEqualTo(config2);
            assertThat(config1).isNotEqualTo(config3);
        }

        @Test
        @DisplayName("hashCode() should be consistent")
        void hashCode_shouldBeConsistent() {
            DistributedLockConfig config1 = DistributedLockConfig.builder()
                    .lockTimeout(Duration.ofSeconds(10))
                    .build();

            DistributedLockConfig config2 = DistributedLockConfig.builder()
                    .lockTimeout(Duration.ofSeconds(10))
                    .build();

            assertThat(config1.hashCode()).isEqualTo(config2.hashCode());
        }

        @Test
        @DisplayName("toString() should return readable string")
        void toString_shouldReturnReadableString() {
            DistributedLockConfig config = DistributedLockConfig.builder()
                    .autoRenew(true)
                    .enableFencing(true)
                    .build();

            String str = config.toString();

            assertThat(str).contains("DistributedLockConfig");
            assertThat(str).contains("autoRenew=true");
            assertThat(str).contains("enableFencing=true");
        }
    }

    @Nested
    @DisplayName("Accessor Methods | 访问器方法")
    class AccessorMethodsTests {

        @Test
        @DisplayName("all accessors should return correct values")
        void allAccessors_shouldReturnCorrectValues() {
            Duration lockTimeout = Duration.ofMinutes(1);
            Duration leaseTime = Duration.ofMinutes(2);
            Duration renewInterval = Duration.ofSeconds(15);
            Duration retryInterval = Duration.ofMillis(250);

            DistributedLockConfig config = new DistributedLockConfig(
                    lockTimeout,
                    leaseTime,
                    renewInterval,
                    true,
                    5,
                    retryInterval,
                    true
            );

            assertThat(config.lockTimeout()).isEqualTo(lockTimeout);
            assertThat(config.leaseTime()).isEqualTo(leaseTime);
            assertThat(config.renewInterval()).isEqualTo(renewInterval);
            assertThat(config.autoRenew()).isTrue();
            assertThat(config.retryCount()).isEqualTo(5);
            assertThat(config.retryInterval()).isEqualTo(retryInterval);
            assertThat(config.enableFencing()).isTrue();
        }
    }

    @Nested
    @DisplayName("Edge Cases | 边界情况")
    class EdgeCasesTests {

        @Test
        @DisplayName("should handle zero durations")
        void shouldHandleZeroDurations() {
            DistributedLockConfig config = DistributedLockConfig.builder()
                    .lockTimeout(Duration.ZERO)
                    .leaseTime(Duration.ZERO)
                    .renewInterval(Duration.ZERO)
                    .retryInterval(Duration.ZERO)
                    .build();

            assertThat(config.lockTimeout()).isEqualTo(Duration.ZERO);
            assertThat(config.leaseTime()).isEqualTo(Duration.ZERO);
        }

        @Test
        @DisplayName("should handle zero retry count")
        void shouldHandleZeroRetryCount() {
            DistributedLockConfig config = DistributedLockConfig.builder()
                    .retryCount(0)
                    .build();

            assertThat(config.retryCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("should handle large durations")
        void shouldHandleLargeDurations() {
            DistributedLockConfig config = DistributedLockConfig.builder()
                    .lockTimeout(Duration.ofDays(365))
                    .leaseTime(Duration.ofDays(365))
                    .build();

            assertThat(config.lockTimeout()).isEqualTo(Duration.ofDays(365));
        }

        @Test
        @DisplayName("should handle negative retry count")
        void shouldHandleNegativeRetryCount() {
            DistributedLockConfig config = DistributedLockConfig.builder()
                    .retryCount(-1)
                    .build();

            assertThat(config.retryCount()).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("Typical Configuration Scenarios | 典型配置场景")
    class TypicalConfigurationTests {

        @Test
        @DisplayName("high availability config")
        void highAvailabilityConfig() {
            DistributedLockConfig config = DistributedLockConfig.builder()
                    .lockTimeout(Duration.ofSeconds(60))
                    .leaseTime(Duration.ofSeconds(30))
                    .renewInterval(Duration.ofSeconds(10))
                    .autoRenew(true)
                    .retryCount(5)
                    .retryInterval(Duration.ofMillis(100))
                    .build();

            assertThat(config.autoRenew()).isTrue();
            assertThat(config.retryCount()).isEqualTo(5);
            assertThat(config.renewInterval().toSeconds()).isLessThan(config.leaseTime().toSeconds());
        }

        @Test
        @DisplayName("short-lived lock config")
        void shortLivedLockConfig() {
            DistributedLockConfig config = DistributedLockConfig.builder()
                    .lockTimeout(Duration.ofSeconds(5))
                    .leaseTime(Duration.ofSeconds(10))
                    .autoRenew(false)
                    .retryCount(1)
                    .build();

            assertThat(config.autoRenew()).isFalse();
            assertThat(config.leaseTime().toSeconds()).isEqualTo(10);
        }

        @Test
        @DisplayName("fencing token enabled config")
        void fencingTokenEnabledConfig() {
            DistributedLockConfig config = DistributedLockConfig.builder()
                    .enableFencing(true)
                    .leaseTime(Duration.ofMinutes(5))
                    .build();

            assertThat(config.enableFencing()).isTrue();
        }
    }
}
