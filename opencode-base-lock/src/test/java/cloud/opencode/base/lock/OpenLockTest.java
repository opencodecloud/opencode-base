package cloud.opencode.base.lock;

import cloud.opencode.base.lock.distributed.DistributedLockConfig;
import cloud.opencode.base.lock.local.LocalLock;
import cloud.opencode.base.lock.local.LocalReadWriteLock;
import cloud.opencode.base.lock.local.SegmentLock;
import cloud.opencode.base.lock.local.SpinLock;
import cloud.opencode.base.lock.manager.LockGroup;
import cloud.opencode.base.lock.manager.LockManager;
import cloud.opencode.base.lock.manager.NamedLockFactory;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenLock facade test - OpenLock 门面测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.0
 */
class OpenLockTest {

    @Nested
    @DisplayName("Local Lock Factory Methods | 本地锁工厂方法")
    class LocalLockFactoryMethodsTests {

        @Test
        @DisplayName("lock() should create LocalLock")
        void lock_shouldCreateLocalLock() {
            Lock<Long> lock = OpenLock.lock();

            assertThat(lock).isNotNull();
            assertThat(lock).isInstanceOf(LocalLock.class);
        }

        @Test
        @DisplayName("lock(config) should create LocalLock with config")
        void lockWithConfig_shouldCreateLocalLockWithConfig() {
            LockConfig config = LockConfig.builder()
                    .fair(true)
                    .enableMetrics(true)
                    .build();

            Lock<Long> lock = OpenLock.lock(config);

            assertThat(lock).isNotNull();
            assertThat(lock).isInstanceOf(LocalLock.class);
            assertThat(((LocalLock) lock).isFair()).isTrue();
        }

        @Test
        @DisplayName("fairLock() should create fair lock")
        void fairLock_shouldCreateFairLock() {
            Lock<Long> lock = OpenLock.fairLock();

            assertThat(lock).isNotNull();
            assertThat(lock).isInstanceOf(LocalLock.class);
            assertThat(((LocalLock) lock).isFair()).isTrue();
        }

        @Test
        @DisplayName("readWriteLock() should create LocalReadWriteLock")
        void readWriteLock_shouldCreateLocalReadWriteLock() {
            ReadWriteLock<Long> rwLock = OpenLock.readWriteLock();

            assertThat(rwLock).isNotNull();
            assertThat(rwLock).isInstanceOf(LocalReadWriteLock.class);
        }

        @Test
        @DisplayName("readWriteLock(config) should create LocalReadWriteLock with config")
        void readWriteLockWithConfig_shouldCreateLocalReadWriteLockWithConfig() {
            LockConfig config = LockConfig.builder()
                    .fair(true)
                    .build();

            ReadWriteLock<Long> rwLock = OpenLock.readWriteLock(config);

            assertThat(rwLock).isNotNull();
            assertThat(rwLock).isInstanceOf(LocalReadWriteLock.class);
        }

        @Test
        @DisplayName("spinLock() should create SpinLock")
        void spinLock_shouldCreateSpinLock() {
            Lock<Long> lock = OpenLock.spinLock();

            assertThat(lock).isNotNull();
            assertThat(lock).isInstanceOf(SpinLock.class);
        }

        @Test
        @DisplayName("spinLock(maxSpinCount) should create SpinLock with max spin")
        void spinLockWithMaxSpin_shouldCreateSpinLockWithMaxSpin() {
            Lock<Long> lock = OpenLock.spinLock(500);

            assertThat(lock).isNotNull();
            assertThat(lock).isInstanceOf(SpinLock.class);
        }

        @Test
        @DisplayName("segmentLock() should create SegmentLock with default segments")
        void segmentLock_shouldCreateSegmentLockWithDefaultSegments() {
            SegmentLock<String> lock = OpenLock.segmentLock();

            assertThat(lock).isNotNull();
            assertThat(lock.getSegments()).isEqualTo(16);
        }

        @Test
        @DisplayName("segmentLock(segments) should create SegmentLock with specified segments")
        void segmentLockWithSegments_shouldCreateSegmentLockWithSpecifiedSegments() {
            SegmentLock<String> lock = OpenLock.segmentLock(32);

            assertThat(lock).isNotNull();
            assertThat(lock.getSegments()).isEqualTo(32);
        }
    }

    @Nested
    @DisplayName("Named Lock Factory Methods | 命名锁工厂方法")
    class NamedLockFactoryMethodsTests {

        @Test
        @DisplayName("namedLockFactory() should create NamedLockFactory")
        void namedLockFactory_shouldCreateNamedLockFactory() {
            NamedLockFactory factory = OpenLock.namedLockFactory();

            assertThat(factory).isNotNull();
            assertThat(factory.isStripingEnabled()).isTrue();
        }

        @Test
        @DisplayName("namedLockFactory(stripes) should create NamedLockFactory with stripes")
        void namedLockFactoryWithStripes_shouldCreateNamedLockFactoryWithStripes() {
            NamedLockFactory factory = OpenLock.namedLockFactory(128);

            assertThat(factory).isNotNull();
            assertThat(factory.getStripes()).isEqualTo(128);
        }
    }

    @Nested
    @DisplayName("Lock Group Methods | 锁组方法")
    class LockGroupMethodsTests {

        @Test
        @DisplayName("lockGroup() should return builder")
        void lockGroup_shouldReturnBuilder() {
            LockGroup.Builder builder = OpenLock.lockGroup();

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("lockGroup() builder should create LockGroup")
        void lockGroup_builderShouldCreateLockGroup() {
            Lock<Long> lockA = OpenLock.lock();
            Lock<Long> lockB = OpenLock.lock();

            LockGroup group = OpenLock.lockGroup()
                    .add(lockA)
                    .add(lockB)
                    .timeout(Duration.ofSeconds(5))
                    .build();

            assertThat(group).isNotNull();
            assertThat(group.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Lock Manager Methods | 锁管理器方法")
    class LockManagerMethodsTests {

        @Test
        @DisplayName("manager() should return singleton")
        void manager_shouldReturnSingleton() {
            LockManager manager1 = OpenLock.manager();
            LockManager manager2 = OpenLock.manager();

            assertThat(manager1).isSameAs(manager2);
        }

        @Test
        @DisplayName("manager(config) should create new manager")
        void managerWithConfig_shouldCreateNewManager() {
            LockConfig config = LockConfig.builder()
                    .fair(true)
                    .build();

            LockManager manager = OpenLock.manager(config);

            assertThat(manager).isNotNull();
        }
    }

    @Nested
    @DisplayName("Convenience Methods | 便捷方法")
    class ConvenienceMethodsTests {

        @Test
        @DisplayName("execute() should run action with lock")
        void execute_shouldRunActionWithLock() {
            AtomicBoolean executed = new AtomicBoolean(false);

            OpenLock.execute(() -> executed.set(true));

            assertThat(executed.get()).isTrue();
        }

        @Test
        @DisplayName("execute() should handle exceptions")
        void execute_shouldHandleExceptions() {
            assertThatThrownBy(() -> OpenLock.execute(() -> {
                throw new RuntimeException("test error");
            })).isInstanceOf(RuntimeException.class)
               .hasMessage("test error");
        }

        @Test
        @DisplayName("executeWithResult() should return result")
        void executeWithResult_shouldReturnResult() {
            String result = OpenLock.executeWithResult(() -> "hello world");

            assertThat(result).isEqualTo("hello world");
        }

        @Test
        @DisplayName("executeWithResult() should handle null result")
        void executeWithResult_shouldHandleNullResult() {
            String result = OpenLock.executeWithResult(() -> null);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Configuration Builder Methods | 配置构建器方法")
    class ConfigurationBuilderMethodsTests {

        @Test
        @DisplayName("configBuilder() should return builder")
        void configBuilder_shouldReturnBuilder() {
            LockConfig.Builder builder = OpenLock.configBuilder();

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("defaultConfig() should return default configuration")
        void defaultConfig_shouldReturnDefaultConfiguration() {
            LockConfig config = OpenLock.defaultConfig();

            assertThat(config).isNotNull();
            assertThat(config.defaultTimeout()).isEqualTo(Duration.ofSeconds(30));
            assertThat(config.fair()).isFalse();
            assertThat(config.reentrant()).isTrue();
        }

        @Test
        @DisplayName("distributedConfigBuilder() should return builder")
        void distributedConfigBuilder_shouldReturnBuilder() {
            DistributedLockConfig.Builder builder = OpenLock.distributedConfigBuilder();

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("defaultDistributedConfig() should return default configuration")
        void defaultDistributedConfig_shouldReturnDefaultConfiguration() {
            DistributedLockConfig config = OpenLock.defaultDistributedConfig();

            assertThat(config).isNotNull();
            assertThat(config.lockTimeout()).isEqualTo(Duration.ofSeconds(30));
            assertThat(config.autoRenew()).isTrue();
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests | 线程安全测试")
    class ThreadSafetyTests {

        @Test
        @DisplayName("execute() should be thread-safe")
        void execute_shouldBeThreadSafe() throws Exception {
            AtomicInteger counter = new AtomicInteger(0);
            Lock<Long> lock = OpenLock.lock();

            Thread[] threads = new Thread[10];
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < 100; j++) {
                        lock.execute(counter::incrementAndGet);
                    }
                });
            }

            for (Thread thread : threads) {
                thread.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            assertThat(counter.get()).isEqualTo(1000);
        }
    }
}
