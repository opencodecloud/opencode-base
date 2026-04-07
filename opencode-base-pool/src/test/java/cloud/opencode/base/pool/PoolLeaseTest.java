package cloud.opencode.base.pool;

import cloud.opencode.base.pool.exception.OpenPoolException;
import cloud.opencode.base.pool.factory.BasePooledObjectFactory;
import cloud.opencode.base.pool.factory.SimplePooledObjectFactory;
import cloud.opencode.base.pool.impl.GenericObjectPool;
import org.junit.jupiter.api.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * PoolLeaseTest - Tests for PoolLease AutoCloseable Lease
 * PoolLeaseTest - PoolLease 自动关闭租约测试类
 *
 * <p>Verifies try-with-resources, idempotent close, invalidation,
 * and integration with GenericObjectPool.</p>
 * <p>验证 try-with-resources、幂等关闭、失效标记以及与 GenericObjectPool 的集成。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.3
 */
@DisplayName("PoolLease 测试")
class PoolLeaseTest {

    private GenericObjectPool<String> pool;
    private AtomicInteger createCounter;

    @BeforeEach
    void setUp() {
        createCounter = new AtomicInteger(0);
    }

    @AfterEach
    void tearDown() {
        if (pool != null) {
            pool.close();
        }
    }

    private GenericObjectPool<String> createPool() {
        return createPool(PoolConfig.builder().maxTotal(8).minIdle(0).build());
    }

    private GenericObjectPool<String> createPool(PoolConfig config) {
        return new GenericObjectPool<>(new BasePooledObjectFactory<>() {
            @Override
            protected String create() {
                return "object-" + createCounter.incrementAndGet();
            }
        }, config);
    }

    @Nested
    @DisplayName("基本功能测试 - Basic Functionality")
    class BasicTests {

        /**
         * Tests that get() returns the correct borrowed object.
         * 测试 get() 返回正确的借用对象。
         */
        @Test
        @DisplayName("get() 返回正确的对象")
        void testGetReturnsCorrectObject() {
            pool = createPool();

            PoolLease<String> lease = pool.borrowLease();
            String obj = lease.get();

            assertThat(obj).startsWith("object-");
            lease.close();
        }

        /**
         * Tests that isClosed() returns false before close and true after close.
         * 测试 isClosed() 在关闭前返回 false，关闭后返回 true。
         */
        @Test
        @DisplayName("isClosed() 状态检查")
        void testIsClosedState() {
            pool = createPool();

            PoolLease<String> lease = pool.borrowLease();

            assertThat(lease.isClosed()).isFalse();
            lease.close();
            assertThat(lease.isClosed()).isTrue();
        }

        /**
         * Tests that get() throws IllegalStateException after the lease is closed.
         * 测试租约关闭后 get() 抛出 IllegalStateException。
         */
        @Test
        @DisplayName("关闭后 get() 抛出异常")
        void testGetThrowsAfterClose() {
            pool = createPool();

            PoolLease<String> lease = pool.borrowLease();
            lease.close();

            assertThatThrownBy(lease::get)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("closed");
        }
    }

    @Nested
    @DisplayName("try-with-resources 测试")
    class TryWithResourcesTests {

        /**
         * Tests that try-with-resources returns object to pool on close.
         * 测试 try-with-resources 在关闭时将对象归还到池中。
         */
        @Test
        @DisplayName("try-with-resources 自动归还对象")
        void testTryWithResourcesReturnsObject() {
            pool = createPool();

            try (PoolLease<String> lease = pool.borrowLease()) {
                String obj = lease.get();
                assertThat(obj).isNotNull();
                assertThat(pool.getNumActive()).isEqualTo(1);
            }

            assertThat(pool.getNumActive()).isZero();
            assertThat(pool.getNumIdle()).isGreaterThanOrEqualTo(1);
        }

        /**
         * Tests that try-with-resources works even when an exception is thrown inside the block.
         * 测试即使块内抛出异常，try-with-resources 也能正常工作。
         */
        @Test
        @DisplayName("异常情况下 try-with-resources 仍归还对象")
        void testTryWithResourcesReturnsObjectOnException() {
            pool = createPool();

            try {
                try (PoolLease<String> lease = pool.borrowLease()) {
                    lease.get();
                    throw new RuntimeException("simulated error");
                }
            } catch (RuntimeException _) {
                // expected
            }

            assertThat(pool.getNumActive()).isZero();
        }
    }

    @Nested
    @DisplayName("失效标记测试 - Invalidation")
    class InvalidationTests {

        /**
         * Tests that invalidate() causes close to destroy the object instead of returning it.
         * 测试 invalidate() 导致关闭时销毁对象而非归还。
         */
        @Test
        @DisplayName("invalidate() 导致关闭时调用 invalidateObject")
        void testInvalidateCausesDestruction() {
            pool = createPool(PoolConfig.builder().maxTotal(1).minIdle(0).build());

            try (PoolLease<String> lease = pool.borrowLease()) {
                lease.invalidate();
            }

            // After invalidation, the object is destroyed, not returned to idle pool
            assertThat(pool.getNumActive()).isZero();
            assertThat(pool.getNumIdle()).isZero();
        }

        /**
         * Tests that invalidate() can be called multiple times safely.
         * 测试 invalidate() 可以安全地多次调用。
         */
        @Test
        @DisplayName("invalidate() 多次调用安全")
        void testInvalidateIdempotent() {
            pool = createPool();

            try (PoolLease<String> lease = pool.borrowLease()) {
                assertThatCode(() -> {
                    lease.invalidate();
                    lease.invalidate();
                    lease.invalidate();
                }).doesNotThrowAnyException();
            }
        }
    }

    @Nested
    @DisplayName("幂等关闭测试 - Idempotent Close")
    class IdempotentCloseTests {

        /**
         * Tests that calling close() twice does not cause double return.
         * 测试多次调用 close() 不会导致重复归还。
         */
        @Test
        @DisplayName("close() 幂等——多次调用不会重复归还")
        void testCloseIsIdempotent() {
            pool = createPool();

            PoolLease<String> lease = pool.borrowLease();

            assertThatCode(() -> {
                lease.close();
                lease.close();
                lease.close();
            }).doesNotThrowAnyException();

            assertThat(pool.getNumActive()).isZero();
            assertThat(pool.getNumIdle()).isEqualTo(1);
        }

        /**
         * Tests that close is safe from concurrent threads.
         * 测试并发线程下关闭的安全性。
         */
        @Test
        @DisplayName("并发 close() 安全")
        void testConcurrentClose() throws InterruptedException {
            pool = createPool();
            PoolLease<String> lease = pool.borrowLease();

            int threadCount = 10;
            java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
            java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                Thread.ofVirtual().start(() -> {
                    try {
                        start.await();
                        lease.close();
                    } catch (InterruptedException _) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }

            start.countDown();
            done.await();

            assertThat(pool.getNumActive()).isZero();
        }
    }

    @Nested
    @DisplayName("集成测试 - Integration")
    class IntegrationTests {

        /**
         * Tests borrowLease() integration with GenericObjectPool.
         * 测试 borrowLease() 与 GenericObjectPool 的集成。
         */
        @Test
        @DisplayName("borrowLease() 与 GenericObjectPool 集成")
        void testBorrowLeaseWithPool() {
            pool = createPool();

            try (PoolLease<String> lease = pool.borrowLease()) {
                assertThat(lease.get()).startsWith("object-");
                assertThat(pool.getNumActive()).isEqualTo(1);
            }
            assertThat(pool.getNumActive()).isZero();
        }

        /**
         * Tests that both execute() and borrowLease() patterns work correctly.
         * 测试 execute() 和 borrowLease() 两种模式都能正确工作。
         */
        @Test
        @DisplayName("execute() 与 borrowLease() 两种模式均工作")
        void testBothPatternsWork() {
            pool = createPool();

            // Pattern 1: execute (with explicit Function type to avoid ambiguity)
            String result1 = pool.execute((java.util.function.Function<String, String>) obj -> obj.toUpperCase());

            // Pattern 2: borrowLease
            String result2;
            try (PoolLease<String> lease = pool.borrowLease()) {
                result2 = lease.get().toUpperCase();
            }

            assertThat(result1).startsWith("OBJECT-");
            assertThat(result2).startsWith("OBJECT-");
            assertThat(pool.getNumActive()).isZero();
        }

        /**
         * Tests that borrowLease with timeout works correctly.
         * 测试带超时的 borrowLease 正确工作。
         */
        @Test
        @DisplayName("borrowLease(timeout) 正确工作")
        void testBorrowLeaseWithTimeout() {
            pool = createPool();

            try (PoolLease<String> lease = pool.borrowLease(java.time.Duration.ofSeconds(5))) {
                assertThat(lease.get()).isNotNull();
            }
        }

        /**
         * Tests multiple sequential leases.
         * 测试多个连续租约。
         */
        @Test
        @DisplayName("多个连续租约正常工作")
        void testMultipleSequentialLeases() {
            pool = createPool(PoolConfig.builder().maxTotal(1).minIdle(0).build());

            for (int i = 0; i < 5; i++) {
                try (PoolLease<String> lease = pool.borrowLease()) {
                    assertThat(lease.get()).isNotNull();
                }
            }
            assertThat(pool.getNumActive()).isZero();
        }

        /**
         * Tests that object returned via lease can be borrowed again.
         * 测试通过租约归还的对象可以再次借用。
         */
        @Test
        @DisplayName("归还后对象可复用")
        void testObjectReusedAfterLeaseClose() {
            pool = createPool(PoolConfig.builder().maxTotal(1).minIdle(0).build());

            String first;
            try (PoolLease<String> lease = pool.borrowLease()) {
                first = lease.get();
            }

            String second;
            try (PoolLease<String> lease = pool.borrowLease()) {
                second = lease.get();
            }

            assertThat(second).isEqualTo(first);
        }
    }
}
