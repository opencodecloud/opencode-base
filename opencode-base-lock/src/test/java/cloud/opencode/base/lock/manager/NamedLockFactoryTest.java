package cloud.opencode.base.lock.manager;

import cloud.opencode.base.lock.Lock;
import cloud.opencode.base.lock.LockConfig;
import cloud.opencode.base.lock.local.LocalLock;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * NamedLockFactory test - 命名锁工厂测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.0
 */
class NamedLockFactoryTest {

    @Nested
    @DisplayName("Constructor Tests | 构造器测试")
    class ConstructorTests {

        @Test
        @DisplayName("default constructor should create factory with 64 stripes")
        void defaultConstructor_shouldCreateFactoryWith64Stripes() {
            NamedLockFactory factory = new NamedLockFactory();

            assertThat(factory.getStripes()).isEqualTo(64);
            assertThat(factory.isStripingEnabled()).isTrue();
        }

        @Test
        @DisplayName("constructor with stripes should create factory with specified stripes")
        void constructorWithStripes_shouldCreateFactoryWithSpecifiedStripes() {
            NamedLockFactory factory = new NamedLockFactory(128);

            assertThat(factory.getStripes()).isEqualTo(128);
        }

        @Test
        @DisplayName("constructor should throw on zero stripes")
        void constructor_shouldThrowOnZeroStripes() {
            assertThatThrownBy(() -> new NamedLockFactory(0, true, LockConfig.defaults()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("constructor should throw on negative stripes")
        void constructor_shouldThrowOnNegativeStripes() {
            assertThatThrownBy(() -> new NamedLockFactory(-1, true, LockConfig.defaults()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("constructor with disabled striping should create unlimited locks")
        void constructorWithDisabledStriping_shouldCreateUnlimitedLocks() {
            NamedLockFactory factory = new NamedLockFactory(64, false, LockConfig.defaults());

            assertThat(factory.isStripingEnabled()).isFalse();
            assertThat(factory.getStripes()).isEqualTo(0);
        }

        @Test
        @DisplayName("constructor with config should apply config")
        void constructorWithConfig_shouldApplyConfig() {
            LockConfig config = LockConfig.builder()
                    .fair(true)
                    .build();

            NamedLockFactory factory = new NamedLockFactory(64, true, config);
            Lock<Long> lock = factory.getLock("test");

            assertThat(((LocalLock) lock).isFair()).isTrue();
        }
    }

    @Nested
    @DisplayName("GetLock Tests (Striping Enabled) | 获取锁测试 (条纹模式)")
    class GetLockStripingEnabledTests {

        private NamedLockFactory factory;

        @BeforeEach
        void setUp() {
            factory = new NamedLockFactory(16);
        }

        @Test
        @DisplayName("getLock() should return lock")
        void getLock_shouldReturnLock() {
            Lock<Long> lock = factory.getLock("test");

            assertThat(lock).isNotNull();
            assertThat(lock).isInstanceOf(LocalLock.class);
        }

        @Test
        @DisplayName("getLock() should return same lock for same name")
        void getLock_shouldReturnSameLockForSameName() {
            Lock<Long> lock1 = factory.getLock("test");
            Lock<Long> lock2 = factory.getLock("test");

            assertThat(lock1).isSameAs(lock2);
        }

        @Test
        @DisplayName("getLock() may return same lock for different names (due to striping)")
        void getLock_mayReturnSameLockForDifferentNames() {
            // With 16 stripes, some names will map to the same stripe
            boolean foundSame = false;
            Lock<Long> first = factory.getLock("key0");

            for (int i = 1; i < 100; i++) {
                Lock<Long> other = factory.getLock("key" + i);
                if (first == other) {
                    foundSame = true;
                    break;
                }
            }

            assertThat(foundSame).isTrue();
        }

        @Test
        @DisplayName("getNamedLockCount() should return 0 when striping enabled")
        void getNamedLockCount_shouldReturnZeroWhenStripingEnabled() {
            factory.getLock("test1");
            factory.getLock("test2");

            assertThat(factory.getNamedLockCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("GetLock Tests (Striping Disabled) | 获取锁测试 (非条纹模式)")
    class GetLockStripingDisabledTests {

        private NamedLockFactory factory;

        @BeforeEach
        void setUp() {
            factory = new NamedLockFactory(64, false, LockConfig.defaults());
        }

        @Test
        @DisplayName("getLock() should return lock")
        void getLock_shouldReturnLock() {
            Lock<Long> lock = factory.getLock("test");

            assertThat(lock).isNotNull();
        }

        @Test
        @DisplayName("getLock() should return same lock for same name")
        void getLock_shouldReturnSameLockForSameName() {
            Lock<Long> lock1 = factory.getLock("test");
            Lock<Long> lock2 = factory.getLock("test");

            assertThat(lock1).isSameAs(lock2);
        }

        @Test
        @DisplayName("getLock() should return different locks for different names")
        void getLock_shouldReturnDifferentLocksForDifferentNames() {
            Lock<Long> lock1 = factory.getLock("test1");
            Lock<Long> lock2 = factory.getLock("test2");

            assertThat(lock1).isNotSameAs(lock2);
        }

        @Test
        @DisplayName("getNamedLockCount() should track created locks")
        void getNamedLockCount_shouldTrackCreatedLocks() {
            factory.getLock("test1");
            factory.getLock("test2");
            factory.getLock("test3");

            assertThat(factory.getNamedLockCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("getNamedLockCount() should not count duplicates")
        void getNamedLockCount_shouldNotCountDuplicates() {
            factory.getLock("test");
            factory.getLock("test");
            factory.getLock("test");

            assertThat(factory.getNamedLockCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Execute Tests | 执行测试")
    class ExecuteTests {

        private NamedLockFactory factory;

        @BeforeEach
        void setUp() {
            factory = new NamedLockFactory();
        }

        @Test
        @DisplayName("execute() should run action with lock")
        void execute_shouldRunActionWithLock() {
            AtomicBoolean executed = new AtomicBoolean(false);

            factory.execute("test", () -> {
                executed.set(true);
                assertThat(factory.getLock("test").isHeldByCurrentThread()).isTrue();
            });

            assertThat(executed.get()).isTrue();
        }

        @Test
        @DisplayName("execute() should release lock after action")
        void execute_shouldReleaseLockAfterAction() {
            factory.execute("test", () -> {});

            assertThat(factory.getLock("test").isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("execute() should release lock on exception")
        void execute_shouldReleaseLockOnException() {
            assertThatThrownBy(() -> factory.execute("test", () -> {
                throw new RuntimeException("test");
            })).isInstanceOf(RuntimeException.class);

            assertThat(factory.getLock("test").isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("executeWithResult() should return result")
        void executeWithResult_shouldReturnResult() {
            String result = factory.executeWithResult("test", () -> "hello");

            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("executeWithResult() should handle null result")
        void executeWithResult_shouldHandleNullResult() {
            String result = factory.executeWithResult("test", () -> null);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Concurrency Tests | 并发测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("should handle concurrent access with striping")
        void shouldHandleConcurrentAccessWithStriping() throws Exception {
            NamedLockFactory factory = new NamedLockFactory(64);
            AtomicInteger counter = new AtomicInteger(0);

            int threads = 10;
            int operations = 100;
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            List<Future<?>> futures = new ArrayList<>();

            for (int i = 0; i < threads; i++) {
                final int threadId = i;
                futures.add(executor.submit(() -> {
                    for (int j = 0; j < operations; j++) {
                        factory.execute("key" + (threadId * operations + j), counter::incrementAndGet);
                    }
                }));
            }

            for (Future<?> future : futures) {
                future.get();
            }

            executor.shutdown();

            assertThat(counter.get()).isEqualTo(threads * operations);
        }

        @Test
        @DisplayName("should serialize access to same named lock")
        void shouldSerializeAccessToSameNamedLock() throws Exception {
            NamedLockFactory factory = new NamedLockFactory(64);
            AtomicInteger concurrentCount = new AtomicInteger(0);
            AtomicInteger maxConcurrent = new AtomicInteger(0);
            AtomicBoolean failed = new AtomicBoolean(false);

            int threads = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            List<Future<?>> futures = new ArrayList<>();

            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    for (int j = 0; j < 50; j++) {
                        factory.execute("sharedKey", () -> {
                            int current = concurrentCount.incrementAndGet();
                            maxConcurrent.updateAndGet(max -> Math.max(max, current));
                            if (current > 1) {
                                failed.set(true);
                            }
                            concurrentCount.decrementAndGet();
                        });
                    }
                }));
            }

            for (Future<?> future : futures) {
                future.get();
            }

            executor.shutdown();

            assertThat(failed.get()).isFalse();
            assertThat(maxConcurrent.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("should allow concurrent access to different stripes")
        void shouldAllowConcurrentAccessToDifferentStripes() throws Exception {
            NamedLockFactory factory = new NamedLockFactory(64);
            AtomicInteger concurrentCount = new AtomicInteger(0);
            AtomicInteger maxConcurrent = new AtomicInteger(0);

            int threads = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            CountDownLatch startLatch = new CountDownLatch(threads);
            CountDownLatch doneLatch = new CountDownLatch(threads);

            for (int i = 0; i < threads; i++) {
                final int id = i;
                executor.submit(() -> {
                    try {
                        startLatch.countDown();
                        startLatch.await();

                        // Use keys that map to different stripes
                        factory.execute("uniqueKey" + (id * 1000), () -> {
                            int current = concurrentCount.incrementAndGet();
                            maxConcurrent.updateAndGet(max -> Math.max(max, current));
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            concurrentCount.decrementAndGet();
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            doneLatch.await();
            executor.shutdown();

            // Should see concurrent access with different keys
            assertThat(maxConcurrent.get()).isGreaterThan(1);
        }
    }

    @Nested
    @DisplayName("Query Methods Tests | 查询方法测试")
    class QueryMethodsTests {

        @Test
        @DisplayName("getStripes() should return stripe count when enabled")
        void getStripes_shouldReturnStripeCountWhenEnabled() {
            NamedLockFactory factory = new NamedLockFactory(32);

            assertThat(factory.getStripes()).isEqualTo(32);
        }

        @Test
        @DisplayName("getStripes() should return 0 when disabled")
        void getStripes_shouldReturnZeroWhenDisabled() {
            NamedLockFactory factory = new NamedLockFactory(32, false, LockConfig.defaults());

            assertThat(factory.getStripes()).isEqualTo(0);
        }

        @Test
        @DisplayName("isStripingEnabled() should return correct value")
        void isStripingEnabled_shouldReturnCorrectValue() {
            NamedLockFactory enabled = new NamedLockFactory(32, true, LockConfig.defaults());
            NamedLockFactory disabled = new NamedLockFactory(32, false, LockConfig.defaults());

            assertThat(enabled.isStripingEnabled()).isTrue();
            assertThat(disabled.isStripingEnabled()).isFalse();
        }
    }
}
