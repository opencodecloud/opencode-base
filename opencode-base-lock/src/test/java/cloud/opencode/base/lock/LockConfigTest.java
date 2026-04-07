package cloud.opencode.base.lock;

import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * LockConfig test - 锁配置测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.0
 */
class LockConfigTest {

    @Nested
    @DisplayName("Factory Methods | 工厂方法")
    class FactoryMethodsTests {

        @Test
        @DisplayName("builder() should create new builder")
        void builder_shouldCreateNewBuilder() {
            LockConfig.Builder builder = LockConfig.builder();

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("defaults() should return default config")
        void defaults_shouldReturnDefaultConfig() {
            LockConfig config = LockConfig.defaults();

            assertThat(config).isNotNull();
            assertThat(config.defaultTimeout()).isEqualTo(Duration.ofSeconds(30));
            assertThat(config.fair()).isFalse();
            assertThat(config.reentrant()).isTrue();
            assertThat(config.spinCount()).isEqualTo(1000);
            assertThat(config.enableMetrics()).isFalse();
            assertThat(config.lockType()).isEqualTo(LockType.REENTRANT);
        }
    }

    @Nested
    @DisplayName("Builder Tests | 构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("timeout() should set default timeout")
        void timeout_shouldSetDefaultTimeout() {
            LockConfig config = LockConfig.builder()
                    .timeout(Duration.ofMinutes(1))
                    .build();

            assertThat(config.defaultTimeout()).isEqualTo(Duration.ofMinutes(1));
        }

        @Test
        @DisplayName("fair() should set fairness")
        void fair_shouldSetFairness() {
            LockConfig config = LockConfig.builder()
                    .fair(true)
                    .build();

            assertThat(config.fair()).isTrue();
        }

        @Test
        @DisplayName("reentrant() should set reentrant")
        void reentrant_shouldSetReentrant() {
            LockConfig config = LockConfig.builder()
                    .reentrant(false)
                    .build();

            assertThat(config.reentrant()).isFalse();
        }

        @Test
        @DisplayName("spinCount() should set spin count")
        void spinCount_shouldSetSpinCount() {
            LockConfig config = LockConfig.builder()
                    .spinCount(500)
                    .build();

            assertThat(config.spinCount()).isEqualTo(500);
        }

        @Test
        @DisplayName("enableMetrics() should set metrics enabled")
        void enableMetrics_shouldSetMetricsEnabled() {
            LockConfig config = LockConfig.builder()
                    .enableMetrics(false)
                    .build();

            assertThat(config.enableMetrics()).isFalse();
        }

        @Test
        @DisplayName("lockType() should set lock type")
        void lockType_shouldSetLockType() {
            LockConfig config = LockConfig.builder()
                    .lockType(LockType.SPIN)
                    .build();

            assertThat(config.lockType()).isEqualTo(LockType.SPIN);
        }

        @Test
        @DisplayName("builder should chain all methods")
        void builder_shouldChainAllMethods() {
            LockConfig config = LockConfig.builder()
                    .timeout(Duration.ofSeconds(60))
                    .fair(true)
                    .reentrant(true)
                    .spinCount(2000)
                    .enableMetrics(true)
                    .lockType(LockType.READ_WRITE)
                    .build();

            assertThat(config.defaultTimeout()).isEqualTo(Duration.ofSeconds(60));
            assertThat(config.fair()).isTrue();
            assertThat(config.reentrant()).isTrue();
            assertThat(config.spinCount()).isEqualTo(2000);
            assertThat(config.enableMetrics()).isTrue();
            assertThat(config.lockType()).isEqualTo(LockType.READ_WRITE);
        }
    }

    @Nested
    @DisplayName("Record Methods | 记录方法")
    class RecordMethodsTests {

        @Test
        @DisplayName("equals() should work correctly")
        void equals_shouldWorkCorrectly() {
            LockConfig config1 = LockConfig.builder()
                    .timeout(Duration.ofSeconds(10))
                    .fair(true)
                    .build();

            LockConfig config2 = LockConfig.builder()
                    .timeout(Duration.ofSeconds(10))
                    .fair(true)
                    .build();

            LockConfig config3 = LockConfig.builder()
                    .timeout(Duration.ofSeconds(20))
                    .fair(true)
                    .build();

            assertThat(config1).isEqualTo(config2);
            assertThat(config1).isNotEqualTo(config3);
        }

        @Test
        @DisplayName("hashCode() should be consistent")
        void hashCode_shouldBeConsistent() {
            LockConfig config1 = LockConfig.builder()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            LockConfig config2 = LockConfig.builder()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            assertThat(config1.hashCode()).isEqualTo(config2.hashCode());
        }

        @Test
        @DisplayName("toString() should return readable string")
        void toString_shouldReturnReadableString() {
            LockConfig config = LockConfig.builder()
                    .fair(true)
                    .build();

            String str = config.toString();

            assertThat(str).contains("LockConfig");
            assertThat(str).contains("fair=true");
        }
    }

    @Nested
    @DisplayName("Accessor Methods | 访问器方法")
    class AccessorMethodsTests {

        @Test
        @DisplayName("all accessors should return correct values")
        void allAccessors_shouldReturnCorrectValues() {
            Duration timeout = Duration.ofMinutes(2);
            LockConfig config = new LockConfig(
                    timeout,
                    true,
                    false,
                    500,
                    true,
                    LockType.STAMPED
            );

            assertThat(config.defaultTimeout()).isEqualTo(timeout);
            assertThat(config.fair()).isTrue();
            assertThat(config.reentrant()).isFalse();
            assertThat(config.spinCount()).isEqualTo(500);
            assertThat(config.enableMetrics()).isTrue();
            assertThat(config.lockType()).isEqualTo(LockType.STAMPED);
        }
    }
}
