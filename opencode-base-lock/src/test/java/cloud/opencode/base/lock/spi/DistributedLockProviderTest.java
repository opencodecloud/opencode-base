package cloud.opencode.base.lock.spi;

import cloud.opencode.base.lock.LockGuard;
import cloud.opencode.base.lock.ReadWriteLock;
import cloud.opencode.base.lock.distributed.DistributedLock;
import cloud.opencode.base.lock.distributed.DistributedLockConfig;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * DistributedLockProvider SPI test - 分布式锁提供者SPI测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.0
 */
class DistributedLockProviderTest {

    @Nested
    @DisplayName("Interface Definition Tests | 接口定义测试")
    class InterfaceDefinitionTests {

        @Test
        @DisplayName("interface should define getName()")
        void interface_shouldDefineGetName() throws Exception {
            var method = DistributedLockProvider.class.getMethod("getName");
            assertThat(method.getReturnType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("interface should define createLock()")
        void interface_shouldDefineCreateLock() throws Exception {
            var method = DistributedLockProvider.class.getMethod("createLock", String.class, DistributedLockConfig.class);
            assertThat(method.getReturnType()).isEqualTo(DistributedLock.class);
        }

        @Test
        @DisplayName("interface should define createReadWriteLock() with default")
        void interface_shouldDefineCreateReadWriteLockWithDefault() throws Exception {
            var method = DistributedLockProvider.class.getMethod("createReadWriteLock", String.class, DistributedLockConfig.class);
            assertThat(method.getReturnType()).isEqualTo(ReadWriteLock.class);
            assertThat(method.isDefault()).isTrue();
        }

        @Test
        @DisplayName("interface should define isAvailable()")
        void interface_shouldDefineIsAvailable() throws Exception {
            var method = DistributedLockProvider.class.getMethod("isAvailable");
            assertThat(method.getReturnType()).isEqualTo(boolean.class);
        }

        @Test
        @DisplayName("interface should define shutdown() with default")
        void interface_shouldDefineShutdownWithDefault() throws Exception {
            var method = DistributedLockProvider.class.getMethod("shutdown");
            assertThat(method.isDefault()).isTrue();
        }
    }

    @Nested
    @DisplayName("Mock Provider Tests | 模拟提供者测试")
    class MockProviderTests {

        @Test
        @DisplayName("mock provider should implement all methods")
        void mockProvider_shouldImplementAllMethods() {
            MockDistributedLockProvider provider = new MockDistributedLockProvider();

            assertThat(provider.getName()).isEqualTo("mock");
            assertThat(provider.isAvailable()).isTrue();

            DistributedLock lock = provider.createLock("test", DistributedLockConfig.defaults());
            assertThat(lock).isNotNull();
            assertThat(lock.getName()).isEqualTo("test");

            assertThatCode(provider::shutdown).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("default createReadWriteLock() should throw UnsupportedOperationException")
        void defaultCreateReadWriteLock_shouldThrowUnsupportedOperationException() {
            MockDistributedLockProvider provider = new MockDistributedLockProvider();

            assertThatThrownBy(() -> provider.createReadWriteLock("test", DistributedLockConfig.defaults()))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("mock");
        }

        @Test
        @DisplayName("provider returning unavailable should work")
        void providerReturningUnavailable_shouldWork() {
            DistributedLockProvider unavailableProvider = new DistributedLockProvider() {
                @Override
                public String getName() {
                    return "unavailable";
                }

                @Override
                public DistributedLock createLock(String lockName, DistributedLockConfig config) {
                    throw new IllegalStateException("Provider not available");
                }

                @Override
                public boolean isAvailable() {
                    return false;
                }
            };

            assertThat(unavailableProvider.isAvailable()).isFalse();
            assertThatThrownBy(() -> unavailableProvider.createLock("test", DistributedLockConfig.defaults()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mock Distributed Lock Tests | 模拟分布式锁测试")
    class MockDistributedLockTests {

        @Test
        @DisplayName("mock lock should implement DistributedLock interface")
        void mockLock_shouldImplementDistributedLockInterface() {
            MockDistributedLockProvider provider = new MockDistributedLockProvider();
            DistributedLock lock = provider.createLock("testLock", DistributedLockConfig.defaults());

            assertThat(lock.getName()).isEqualTo("testLock");
            assertThat(lock.getValue()).isNotNull();
        }

        @Test
        @DisplayName("mock lock should support basic operations")
        void mockLock_shouldSupportBasicOperations() {
            MockDistributedLockProvider provider = new MockDistributedLockProvider();
            DistributedLock lock = provider.createLock("testLock", DistributedLockConfig.defaults());

            // Test lock/unlock
            try (var guard = lock.lock()) {
                assertThat(lock.isHeldByCurrentThread()).isTrue();
            }
            assertThat(lock.isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("mock lock should support tryLock")
        void mockLock_shouldSupportTryLock() {
            MockDistributedLockProvider provider = new MockDistributedLockProvider();
            DistributedLock lock = provider.createLock("testLock", DistributedLockConfig.defaults());

            assertThat(lock.tryLock()).isTrue();
            lock.unlock();
        }

        @Test
        @DisplayName("mock lock should support extend")
        void mockLock_shouldSupportExtend() {
            MockDistributedLockProvider provider = new MockDistributedLockProvider();
            DistributedLock lock = provider.createLock("testLock", DistributedLockConfig.defaults());

            lock.lock();
            boolean extended = lock.extend(Duration.ofSeconds(30));
            assertThat(extended).isTrue();
            lock.unlock();
        }

        @Test
        @DisplayName("mock lock should support getRemainingTtl")
        void mockLock_shouldSupportGetRemainingTtl() {
            MockDistributedLockProvider provider = new MockDistributedLockProvider();
            DistributedLock lock = provider.createLock("testLock", DistributedLockConfig.defaults());

            lock.lock();
            Optional<Duration> ttl = lock.getRemainingTtl();
            assertThat(ttl).isPresent();
            lock.unlock();
        }

        @Test
        @DisplayName("mock lock should support isExpired")
        void mockLock_shouldSupportIsExpired() {
            MockDistributedLockProvider provider = new MockDistributedLockProvider();
            DistributedLock lock = provider.createLock("testLock", DistributedLockConfig.defaults());

            assertThat(lock.isExpired()).isFalse();
        }
    }

    /**
     * Mock implementation for testing
     */
    static class MockDistributedLockProvider implements DistributedLockProvider {

        @Override
        public String getName() {
            return "mock";
        }

        @Override
        public DistributedLock createLock(String lockName, DistributedLockConfig config) {
            return new MockDistributedLock(lockName, config);
        }

        @Override
        public boolean isAvailable() {
            return true;
        }
    }

    /**
     * Mock DistributedLock for testing
     */
    static class MockDistributedLock implements DistributedLock {
        private final String name;
        private final String value;
        private final DistributedLockConfig config;
        private volatile boolean held = false;
        private volatile Thread owner = null;

        MockDistributedLock(String name, DistributedLockConfig config) {
            this.name = name;
            this.value = "mock-value-" + System.nanoTime();
            this.config = config;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public boolean extend(Duration duration) {
            return held;
        }

        @Override
        public Optional<Duration> getRemainingTtl() {
            return held ? Optional.of(config.leaseTime()) : Optional.empty();
        }

        @Override
        public boolean isExpired() {
            return false;
        }

        @Override
        public LockGuard<String> lock() {
            held = true;
            owner = Thread.currentThread();
            return new LockGuard<>(this, value);
        }

        @Override
        public LockGuard<String> lock(Duration timeout) {
            return lock();
        }

        @Override
        public boolean tryLock() {
            if (!held) {
                held = true;
                owner = Thread.currentThread();
                return true;
            }
            return owner == Thread.currentThread();
        }

        @Override
        public boolean tryLock(Duration timeout) {
            return tryLock();
        }

        @Override
        public LockGuard<String> lockInterruptibly() throws InterruptedException {
            return lock();
        }

        @Override
        public void unlock() {
            if (owner == Thread.currentThread()) {
                held = false;
                owner = null;
            }
        }

        @Override
        public boolean isHeldByCurrentThread() {
            return held && owner == Thread.currentThread();
        }

        @Override
        public Optional<String> getToken() {
            return held ? Optional.of(value) : Optional.empty();
        }
    }
}
