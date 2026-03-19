package cloud.opencode.base.lock.distributed;

import cloud.opencode.base.lock.LockGuard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * DistributedLock Interface Tests
 * DistributedLock 接口测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.0
 */
@DisplayName("DistributedLock Interface Tests | DistributedLock 接口测试")
class DistributedLockTest {

    @Nested
    @DisplayName("Basic Operations Tests | 基本操作测试")
    class BasicOperationsTests {

        @Test
        @DisplayName("getName returns lock name | getName 返回锁名称")
        void testGetName() {
            DistributedLock lock = createTestLock("order:12345");
            assertThat(lock.getName()).isEqualTo("order:12345");
        }

        @Test
        @DisplayName("getValue returns lock value | getValue 返回锁值")
        void testGetValue() {
            DistributedLock lock = createTestLock("test");
            lock.lock();

            assertThat(lock.getValue()).isNotNull();
            lock.unlock();
        }
    }

    @Nested
    @DisplayName("TTL Operations Tests | TTL 操作测试")
    class TtlOperationsTests {

        @Test
        @DisplayName("extend returns true on success | extend 成功时返回 true")
        void testExtend() {
            DistributedLock lock = createTestLock("test");
            lock.lock();

            boolean extended = lock.extend(Duration.ofSeconds(30));

            assertThat(extended).isTrue();
            lock.unlock();
        }

        @Test
        @DisplayName("getRemainingTtl returns remaining time | getRemainingTtl 返回剩余时间")
        void testGetRemainingTtl() {
            DistributedLock lock = createTestLock("test");
            lock.lock();

            Optional<Duration> ttl = lock.getRemainingTtl();

            assertThat(ttl).isPresent();
            assertThat(ttl.get()).isPositive();
            lock.unlock();
        }

        @Test
        @DisplayName("getRemainingTtl returns empty when not locked | getRemainingTtl 未锁定时返回空")
        void testGetRemainingTtlNotLocked() {
            DistributedLock lock = createTestLock("test");

            Optional<Duration> ttl = lock.getRemainingTtl();

            assertThat(ttl).isEmpty();
        }

        @Test
        @DisplayName("isExpired returns false when valid | isExpired 有效时返回 false")
        void testIsExpiredFalse() {
            DistributedLock lock = createTestLock("test");
            lock.lock();

            assertThat(lock.isExpired()).isFalse();
            lock.unlock();
        }
    }

    @Nested
    @DisplayName("Inherited Lock Operations Tests | 继承的锁操作测试")
    class InheritedLockOperationsTests {

        @Test
        @DisplayName("lock acquires distributed lock | lock 获取分布式锁")
        void testLock() {
            DistributedLock lock = createTestLock("test");

            try (LockGuard<String> guard = lock.lock()) {
                assertThat(lock.isHeldByCurrentThread()).isTrue();
            }
        }

        @Test
        @DisplayName("tryLock attempts to acquire lock | tryLock 尝试获取锁")
        void testTryLock() {
            DistributedLock lock = createTestLock("test");

            boolean acquired = lock.tryLock();

            assertThat(acquired).isTrue();
            lock.unlock();
        }

        @Test
        @DisplayName("getToken returns string token | getToken 返回字符串令牌")
        void testGetToken() {
            DistributedLock lock = createTestLock("test");
            lock.lock();

            Optional<String> token = lock.getToken();

            assertThat(token).isPresent();
            assertThat(token.get()).isNotBlank();
            lock.unlock();
        }
    }

    @Nested
    @DisplayName("Execute With Extension Tests | 带扩展执行测试")
    class ExecuteWithExtensionTests {

        @Test
        @DisplayName("can extend during execution | 可以在执行期间扩展")
        void testExtendDuringExecution() {
            DistributedLock lock = createTestLock("test");

            lock.execute(() -> {
                // Simulate long-running operation
                if (lock.getRemainingTtl().map(d -> d.toMillis() < 5000).orElse(false)) {
                    lock.extend(Duration.ofSeconds(30));
                }
            });
        }
    }

    // Helper method to create a mock distributed lock for testing
    private DistributedLock createTestLock(String name) {
        return new DistributedLock() {
            private boolean locked = false;
            private String value = null;
            private Duration remainingTtl = Duration.ofSeconds(30);

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
                if (locked) {
                    remainingTtl = remainingTtl.plus(duration);
                    return true;
                }
                return false;
            }

            @Override
            public Optional<Duration> getRemainingTtl() {
                return locked ? Optional.of(remainingTtl) : Optional.empty();
            }

            @Override
            public boolean isExpired() {
                return locked && remainingTtl.isNegative();
            }

            @Override
            public LockGuard<String> lock() {
                locked = true;
                value = java.util.UUID.randomUUID().toString();
                return new LockGuard<>(this, value);
            }

            @Override
            public LockGuard<String> lock(Duration timeout) {
                return lock();
            }

            @Override
            public boolean tryLock() {
                locked = true;
                value = java.util.UUID.randomUUID().toString();
                return true;
            }

            @Override
            public boolean tryLock(Duration timeout) {
                return tryLock();
            }

            @Override
            public LockGuard<String> lockInterruptibly() {
                return lock();
            }

            @Override
            public void unlock() {
                locked = false;
                value = null;
            }

            @Override
            public boolean isHeldByCurrentThread() {
                return locked;
            }

            @Override
            public Optional<String> getToken() {
                return Optional.ofNullable(value);
            }
        };
    }
}
