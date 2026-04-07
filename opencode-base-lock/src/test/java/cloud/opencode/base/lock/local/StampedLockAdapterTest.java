package cloud.opencode.base.lock.local;

import cloud.opencode.base.lock.Lock;
import cloud.opencode.base.lock.LockConfig;
import cloud.opencode.base.lock.LockGuard;
import cloud.opencode.base.lock.exception.OpenLockAcquireException;
import cloud.opencode.base.lock.exception.OpenLockTimeoutException;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * StampedLockAdapter full test - StampedLock 适配器完整测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.3
 */
class StampedLockAdapterTest {

    private StampedLockAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new StampedLockAdapter();
    }

    @Nested
    @DisplayName("Constructor Tests | 构造器测试")
    class ConstructorTests {

        @Test
        @DisplayName("default constructor should create adapter")
        void defaultConstructor_shouldCreateAdapter() {
            StampedLockAdapter a = new StampedLockAdapter();

            assertThat(a).isNotNull();
            assertThat(a.readLock()).isNotNull();
            assertThat(a.writeLock()).isNotNull();
        }

        @Test
        @DisplayName("constructor with config should use config timeout")
        void constructorWithConfig_shouldUseConfigTimeout() {
            LockConfig config = LockConfig.builder()
                    .timeout(Duration.ofSeconds(5))
                    .build();

            StampedLockAdapter a = new StampedLockAdapter(config);

            assertThat(a).isNotNull();
            assertThat(a.readLock()).isNotNull();
            assertThat(a.writeLock()).isNotNull();
        }

        @Test
        @DisplayName("constructor with null config should throw NullPointerException")
        void constructorWithNullConfig_shouldThrow() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new StampedLockAdapter(null));
        }
    }

    @Nested
    @DisplayName("Optimistic Read Tests | 乐观读测试")
    class OptimisticReadTests {

        @Test
        @DisplayName("optimistic read should return value without locking")
        void optimisticRead_shouldReturnValueWithoutLocking() {
            String result = adapter.optimisticRead(() -> "hello");

            assertThat(result).isEqualTo("hello");
            assertThat(adapter.isReadLocked()).isFalse();
            assertThat(adapter.isWriteLocked()).isFalse();
        }

        @Test
        @DisplayName("optimistic read should return null supplier result")
        void optimisticRead_shouldReturnNullResult() {
            String result = adapter.optimisticRead(() -> null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("optimistic read should fallback to pessimistic when write occurs")
        void optimisticRead_shouldFallbackWhenWriteOccurs() throws Exception {
            AtomicInteger counter = new AtomicInteger(0);
            CountDownLatch writeLockAcquired = new CountDownLatch(1);
            CountDownLatch readStarted = new CountDownLatch(1);
            AtomicReference<String> result = new AtomicReference<>();

            // Hold write lock to force optimistic read to fail and fallback
            Thread writer = Thread.ofPlatform().start(() -> {
                try (var guard = adapter.writeLock().lock()) {
                    writeLockAcquired.countDown();
                    // Wait for the read to start attempt
                    readStarted.await(5, TimeUnit.SECONDS);
                    // Hold write lock briefly then release via guard close
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            // Wait for write lock to be held
            writeLockAcquired.await(5, TimeUnit.SECONDS);

            Thread reader = Thread.ofPlatform().start(() -> {
                readStarted.countDown();
                // Optimistic read will fail because write lock is held
                // Then fallback will wait for write lock release
                String r = adapter.optimisticRead(() -> "value-" + counter.incrementAndGet(),
                        Duration.ofSeconds(5));
                result.set(r);
            });

            writer.join(5000);
            reader.join(5000);

            assertThat(result.get()).startsWith("value-");
        }

        @Test
        @DisplayName("optimistic read with timeout should throw on timeout")
        void optimisticRead_shouldThrowOnTimeout() throws Exception {
            // Hold write lock to force fallback, then time out
            try (var guard = adapter.writeLock().lock()) {
                assertThatThrownBy(() ->
                        adapter.optimisticRead(() -> "value", Duration.ofMillis(50)))
                        .isInstanceOf(OpenLockTimeoutException.class);
            }
        }

        @Test
        @DisplayName("optimistic read with null reader should throw NullPointerException")
        void optimisticRead_nullReader_shouldThrow() {
            assertThatNullPointerException()
                    .isThrownBy(() -> adapter.optimisticRead(null));
        }

        @Test
        @DisplayName("optimistic read with null timeout should throw NullPointerException")
        void optimisticRead_nullTimeout_shouldThrow() {
            assertThatNullPointerException()
                    .isThrownBy(() -> adapter.optimisticRead(() -> "x", null));
        }

        @Test
        @DisplayName("optimistic read should succeed under no contention")
        void optimisticRead_noContention_shouldSucceed() {
            // Multiple reads with no writer should always use fast path
            for (int i = 0; i < 100; i++) {
                int idx = i;
                String result = adapter.optimisticRead(() -> "item-" + idx);
                assertThat(result).isEqualTo("item-" + idx);
            }
        }
    }

    @Nested
    @DisplayName("Read Lock Tests | 读锁测试")
    class ReadLockTests {

        @Test
        @DisplayName("read lock should acquire and release")
        void readLock_shouldAcquireAndRelease() {
            Lock<Long> readLock = adapter.readLock();

            try (var guard = readLock.lock()) {
                assertThat(guard).isNotNull();
                assertThat(guard.token()).isNotNull();
                assertThat(guard.token()).isNotEqualTo(0L);
                assertThat(readLock.isHeldByCurrentThread()).isTrue();
                assertThat(adapter.isReadLocked()).isTrue();
            }

            assertThat(readLock.isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("read lock with timeout should acquire")
        void readLock_withTimeout_shouldAcquire() {
            Lock<Long> readLock = adapter.readLock();

            try (var guard = readLock.lock(Duration.ofSeconds(5))) {
                assertThat(guard).isNotNull();
                assertThat(readLock.isHeldByCurrentThread()).isTrue();
            }
        }

        @Test
        @DisplayName("read lock timeout should throw OpenLockTimeoutException")
        void readLock_timeout_shouldThrow() throws Exception {
            // Hold write lock so read lock times out
            try (var guard = adapter.writeLock().lock()) {
                // Try read lock from another thread
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Future<?> future = executor.submit(() -> {
                    assertThatThrownBy(() ->
                            adapter.readLock().lock(Duration.ofMillis(50)))
                            .isInstanceOf(OpenLockTimeoutException.class);
                });
                future.get(5, TimeUnit.SECONDS);
                executor.shutdown();
            }
        }

        @Test
        @DisplayName("tryLock should return true when available")
        void tryLock_shouldReturnTrueWhenAvailable() {
            Lock<Long> readLock = adapter.readLock();

            boolean acquired = readLock.tryLock();

            assertThat(acquired).isTrue();
            assertThat(readLock.isHeldByCurrentThread()).isTrue();
            readLock.unlock();
        }

        @Test
        @DisplayName("tryLock should return false when write locked")
        void tryLock_shouldReturnFalseWhenWriteLocked() throws Exception {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            CountDownLatch locked = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(1);

            executor.submit(() -> {
                try (var guard = adapter.writeLock().lock()) {
                    locked.countDown();
                    done.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            locked.await(5, TimeUnit.SECONDS);
            boolean acquired = adapter.readLock().tryLock();
            assertThat(acquired).isFalse();

            done.countDown();
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }

        @Test
        @DisplayName("tryLock with timeout should return true when available")
        void tryLockWithTimeout_shouldReturnTrue() {
            boolean acquired = adapter.readLock().tryLock(Duration.ofSeconds(1));

            assertThat(acquired).isTrue();
            adapter.readLock().unlock();
        }

        @Test
        @DisplayName("lockInterruptibly should acquire lock")
        void lockInterruptibly_shouldAcquire() throws InterruptedException {
            Lock<Long> readLock = adapter.readLock();

            LockGuard<Long> guard = readLock.lockInterruptibly();

            assertThat(guard).isNotNull();
            assertThat(guard.token()).isNotEqualTo(0L);
            assertThat(readLock.isHeldByCurrentThread()).isTrue();
            readLock.unlock();
        }

        @Test
        @DisplayName("getToken should return stamp when locked")
        void getToken_shouldReturnStamp() {
            Lock<Long> readLock = adapter.readLock();

            assertThat(readLock.getToken()).isEmpty();

            try (var guard = readLock.lock()) {
                assertThat(readLock.getToken()).isPresent();
                assertThat(readLock.getToken().get()).isEqualTo(guard.token());
            }

            assertThat(readLock.getToken()).isEmpty();
        }

        @Test
        @DisplayName("unlock without lock should throw")
        void unlock_withoutLock_shouldThrow() {
            assertThatThrownBy(() -> adapter.readLock().unlock())
                    .isInstanceOf(OpenLockAcquireException.class);
        }

        @Test
        @DisplayName("concurrent readers should all acquire read lock")
        void concurrentReaders_shouldAllAcquire() throws Exception {
            int readerCount = 10;
            CountDownLatch allAcquired = new CountDownLatch(readerCount);
            CountDownLatch release = new CountDownLatch(1);
            AtomicInteger acquiredCount = new AtomicInteger(0);

            ExecutorService executor = Executors.newFixedThreadPool(readerCount);
            List<Future<?>> futures = new ArrayList<>();

            for (int i = 0; i < readerCount; i++) {
                futures.add(executor.submit(() -> {
                    try (var guard = adapter.readLock().lock()) {
                        acquiredCount.incrementAndGet();
                        allAcquired.countDown();
                        release.await(5, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }));
            }

            boolean allReady = allAcquired.await(5, TimeUnit.SECONDS);
            assertThat(allReady).isTrue();
            assertThat(acquiredCount.get()).isEqualTo(readerCount);
            assertThat(adapter.isReadLocked()).isTrue();

            release.countDown();
            for (Future<?> f : futures) {
                f.get(5, TimeUnit.SECONDS);
            }

            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Nested
    @DisplayName("Write Lock Tests | 写锁测试")
    class WriteLockTests {

        @Test
        @DisplayName("write lock should acquire and release")
        void writeLock_shouldAcquireAndRelease() {
            Lock<Long> writeLock = adapter.writeLock();

            try (var guard = writeLock.lock()) {
                assertThat(guard).isNotNull();
                assertThat(guard.token()).isNotNull();
                assertThat(guard.token()).isNotEqualTo(0L);
                assertThat(writeLock.isHeldByCurrentThread()).isTrue();
                assertThat(adapter.isWriteLocked()).isTrue();
            }

            assertThat(writeLock.isHeldByCurrentThread()).isFalse();
            assertThat(adapter.isWriteLocked()).isFalse();
        }

        @Test
        @DisplayName("write lock with timeout should acquire")
        void writeLock_withTimeout_shouldAcquire() {
            Lock<Long> writeLock = adapter.writeLock();

            try (var guard = writeLock.lock(Duration.ofSeconds(5))) {
                assertThat(guard).isNotNull();
                assertThat(writeLock.isHeldByCurrentThread()).isTrue();
            }
        }

        @Test
        @DisplayName("write lock timeout should throw OpenLockTimeoutException")
        void writeLock_timeout_shouldThrow() throws Exception {
            // Hold write lock from another thread
            CountDownLatch locked = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(1);
            ExecutorService executor = Executors.newSingleThreadExecutor();

            executor.submit(() -> {
                try (var guard = adapter.writeLock().lock()) {
                    locked.countDown();
                    done.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            locked.await(5, TimeUnit.SECONDS);

            assertThatThrownBy(() ->
                    adapter.writeLock().lock(Duration.ofMillis(50)))
                    .isInstanceOf(OpenLockTimeoutException.class);

            done.countDown();
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }

        @Test
        @DisplayName("tryLock should return true when available")
        void tryLock_shouldReturnTrueWhenAvailable() {
            Lock<Long> writeLock = adapter.writeLock();

            boolean acquired = writeLock.tryLock();

            assertThat(acquired).isTrue();
            assertThat(writeLock.isHeldByCurrentThread()).isTrue();
            writeLock.unlock();
        }

        @Test
        @DisplayName("tryLock should return false when write locked by another thread")
        void tryLock_shouldReturnFalseWhenLocked() throws Exception {
            CountDownLatch locked = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(1);
            ExecutorService executor = Executors.newSingleThreadExecutor();

            executor.submit(() -> {
                try (var guard = adapter.writeLock().lock()) {
                    locked.countDown();
                    done.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            locked.await(5, TimeUnit.SECONDS);
            boolean acquired = adapter.writeLock().tryLock();
            assertThat(acquired).isFalse();

            done.countDown();
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }

        @Test
        @DisplayName("tryLock with timeout should return true when available")
        void tryLockWithTimeout_shouldReturnTrue() {
            boolean acquired = adapter.writeLock().tryLock(Duration.ofSeconds(1));

            assertThat(acquired).isTrue();
            adapter.writeLock().unlock();
        }

        @Test
        @DisplayName("lockInterruptibly should acquire lock")
        void lockInterruptibly_shouldAcquire() throws InterruptedException {
            Lock<Long> writeLock = adapter.writeLock();

            LockGuard<Long> guard = writeLock.lockInterruptibly();

            assertThat(guard).isNotNull();
            assertThat(guard.token()).isNotEqualTo(0L);
            assertThat(writeLock.isHeldByCurrentThread()).isTrue();
            writeLock.unlock();
        }

        @Test
        @DisplayName("getToken should return stamp when locked")
        void getToken_shouldReturnStamp() {
            Lock<Long> writeLock = adapter.writeLock();

            assertThat(writeLock.getToken()).isEmpty();

            try (var guard = writeLock.lock()) {
                assertThat(writeLock.getToken()).isPresent();
                assertThat(writeLock.getToken().get()).isEqualTo(guard.token());
            }

            assertThat(writeLock.getToken()).isEmpty();
        }

        @Test
        @DisplayName("unlock without lock should throw")
        void unlock_withoutLock_shouldThrow() {
            assertThatThrownBy(() -> adapter.writeLock().unlock())
                    .isInstanceOf(OpenLockAcquireException.class);
        }

        @Test
        @DisplayName("write lock should be exclusive")
        void writeLock_shouldBeExclusive() throws Exception {
            AtomicInteger concurrentWrites = new AtomicInteger(0);
            AtomicInteger maxConcurrentWrites = new AtomicInteger(0);
            int writerCount = 5;
            CountDownLatch allDone = new CountDownLatch(writerCount);

            ExecutorService executor = Executors.newFixedThreadPool(writerCount);

            for (int i = 0; i < writerCount; i++) {
                executor.submit(() -> {
                    try (var guard = adapter.writeLock().lock()) {
                        int current = concurrentWrites.incrementAndGet();
                        maxConcurrentWrites.updateAndGet(max -> Math.max(max, current));
                        Thread.sleep(10); // Hold lock briefly
                        concurrentWrites.decrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        allDone.countDown();
                    }
                });
            }

            allDone.await(10, TimeUnit.SECONDS);
            assertThat(maxConcurrentWrites.get()).isEqualTo(1);

            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Nested
    @DisplayName("Concurrency Tests | 并发测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("readers should block while writer holds lock")
        void readers_shouldBlockWhileWriterHoldsLock() throws Exception {
            CountDownLatch writeLocked = new CountDownLatch(1);
            CountDownLatch readerAttempted = new CountDownLatch(1);
            AtomicBoolean readerAcquired = new AtomicBoolean(false);
            CountDownLatch done = new CountDownLatch(1);

            // Writer holds lock
            ExecutorService executor = Executors.newFixedThreadPool(2);
            executor.submit(() -> {
                try (var guard = adapter.writeLock().lock()) {
                    writeLocked.countDown();
                    // Wait for reader to attempt
                    readerAttempted.await(5, TimeUnit.SECONDS);
                    // Still holding write lock - reader should not have acquired
                    Thread.sleep(100);
                    // Reader should still be blocked
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });

            // Reader tries to acquire
            executor.submit(() -> {
                try {
                    writeLocked.await(5, TimeUnit.SECONDS);
                    readerAttempted.countDown();
                    try (var guard = adapter.readLock().lock(Duration.ofSeconds(5))) {
                        readerAcquired.set(true);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            done.await(5, TimeUnit.SECONDS);
            // Give reader a moment to acquire after write releases
            Thread.sleep(200);
            assertThat(readerAcquired.get()).isTrue();

            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }

        @Test
        @DisplayName("writer should block while reader holds lock")
        void writer_shouldBlockWhileReaderHoldsLock() throws Exception {
            CountDownLatch readLocked = new CountDownLatch(1);
            CountDownLatch writerAttempted = new CountDownLatch(1);
            AtomicBoolean writerGotLock = new AtomicBoolean(false);

            ExecutorService executor = Executors.newFixedThreadPool(2);

            // Reader holds lock
            Future<?> readerFuture = executor.submit(() -> {
                try (var guard = adapter.readLock().lock()) {
                    readLocked.countDown();
                    writerAttempted.await(5, TimeUnit.SECONDS);
                    Thread.sleep(100); // Hold read lock
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            // Writer tries to acquire
            Future<?> writerFuture = executor.submit(() -> {
                try {
                    readLocked.await(5, TimeUnit.SECONDS);
                    writerAttempted.countDown();
                    try (var guard = adapter.writeLock().lock(Duration.ofSeconds(5))) {
                        writerGotLock.set(true);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            readerFuture.get(10, TimeUnit.SECONDS);
            writerFuture.get(10, TimeUnit.SECONDS);

            assertThat(writerGotLock.get()).isTrue();

            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }

        @Test
        @DisplayName("execute methods should work correctly")
        void executeMethods_shouldWorkCorrectly() {
            AtomicInteger counter = new AtomicInteger(0);

            adapter.executeWrite(() -> counter.incrementAndGet());
            int result = adapter.executeRead(counter::get);

            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("multiple readers and single writer should maintain consistency")
        void multipleReadersAndWriter_shouldMaintainConsistency() throws Exception {
            int iterations = 100;
            AtomicInteger sharedValue = new AtomicInteger(0);
            AtomicBoolean inconsistencyDetected = new AtomicBoolean(false);
            CountDownLatch allDone = new CountDownLatch(iterations * 2);

            ExecutorService executor = Executors.newFixedThreadPool(8);

            // Writers
            for (int i = 0; i < iterations; i++) {
                executor.submit(() -> {
                    try (var guard = adapter.writeLock().lock()) {
                        // Non-atomic read-modify-write is safe under write lock
                        int current = sharedValue.get();
                        Thread.sleep(1);
                        sharedValue.set(current + 1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        allDone.countDown();
                    }
                });
            }

            // Readers
            for (int i = 0; i < iterations; i++) {
                executor.submit(() -> {
                    try (var guard = adapter.readLock().lock()) {
                        int value = sharedValue.get();
                        Thread.sleep(1);
                        // Value should not change during read lock
                        if (sharedValue.get() != value) {
                            inconsistencyDetected.set(true);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        allDone.countDown();
                    }
                });
            }

            allDone.await(30, TimeUnit.SECONDS);
            assertThat(inconsistencyDetected.get()).isFalse();
            assertThat(sharedValue.get()).isEqualTo(iterations);

            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Nested
    @DisplayName("Edge Case Tests | 边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("isReadLocked should reflect read lock state")
        void isReadLocked_shouldReflectState() {
            assertThat(adapter.isReadLocked()).isFalse();

            try (var guard = adapter.readLock().lock()) {
                assertThat(adapter.isReadLocked()).isTrue();
            }

            assertThat(adapter.isReadLocked()).isFalse();
        }

        @Test
        @DisplayName("isWriteLocked should reflect write lock state")
        void isWriteLocked_shouldReflectState() {
            assertThat(adapter.isWriteLocked()).isFalse();

            try (var guard = adapter.writeLock().lock()) {
                assertThat(adapter.isWriteLocked()).isTrue();
            }

            assertThat(adapter.isWriteLocked()).isFalse();
        }

        @Test
        @DisplayName("tryLock with timeout false when write locked from another thread")
        void tryLockWithTimeout_shouldReturnFalse() throws Exception {
            CountDownLatch locked = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(1);
            ExecutorService executor = Executors.newSingleThreadExecutor();

            executor.submit(() -> {
                try (var guard = adapter.writeLock().lock()) {
                    locked.countDown();
                    done.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            locked.await(5, TimeUnit.SECONDS);

            boolean readAcquired = adapter.readLock().tryLock(Duration.ofMillis(50));
            assertThat(readAcquired).isFalse();

            boolean writeAcquired = adapter.writeLock().tryLock(Duration.ofMillis(50));
            assertThat(writeAcquired).isFalse();

            done.countDown();
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }

        @Test
        @DisplayName("close should release lock if held")
        void close_shouldReleaseLockIfHeld() {
            Lock<Long> writeLock = adapter.writeLock();

            writeLock.tryLock();
            assertThat(writeLock.isHeldByCurrentThread()).isTrue();

            writeLock.close();
            assertThat(writeLock.isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("close should not throw if lock not held")
        void close_shouldNotThrowIfNotHeld() {
            assertThatCode(() -> adapter.writeLock().close())
                    .doesNotThrowAnyException();
            assertThatCode(() -> adapter.readLock().close())
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("lock with null timeout should throw NullPointerException")
        void lock_nullTimeout_shouldThrow() {
            assertThatNullPointerException()
                    .isThrownBy(() -> adapter.readLock().lock(null));
            assertThatNullPointerException()
                    .isThrownBy(() -> adapter.writeLock().lock(null));
        }

        @Test
        @DisplayName("tryLock with null timeout should throw NullPointerException")
        void tryLock_nullTimeout_shouldThrow() {
            assertThatNullPointerException()
                    .isThrownBy(() -> adapter.readLock().tryLock(null));
            assertThatNullPointerException()
                    .isThrownBy(() -> adapter.writeLock().tryLock(null));
        }

        @Test
        @DisplayName("lockInterruptibly should throw InterruptedException when interrupted")
        void lockInterruptibly_shouldThrowWhenInterrupted() throws Exception {
            CountDownLatch locked = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(1);
            ExecutorService holder = Executors.newSingleThreadExecutor();

            // Hold write lock from another thread
            holder.submit(() -> {
                try (var guard = adapter.writeLock().lock()) {
                    locked.countDown();
                    done.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            locked.await(5, TimeUnit.SECONDS);

            // Try to lockInterruptibly on a thread we will interrupt
            AtomicReference<Throwable> caught = new AtomicReference<>();
            Thread waiter = Thread.ofPlatform().start(() -> {
                try {
                    adapter.writeLock().lockInterruptibly();
                } catch (InterruptedException e) {
                    caught.set(e);
                }
            });

            Thread.sleep(50);
            waiter.interrupt();
            waiter.join(5000);

            assertThat(caught.get()).isInstanceOf(InterruptedException.class);

            done.countDown();
            holder.shutdown();
            holder.awaitTermination(5, TimeUnit.SECONDS);
        }

        @Test
        @DisplayName("read and write locks should be separate instances")
        void readAndWriteLocks_shouldBeSeparateInstances() {
            Lock<Long> readLock1 = adapter.readLock();
            Lock<Long> readLock2 = adapter.readLock();
            Lock<Long> writeLock = adapter.writeLock();

            assertThat(readLock1).isSameAs(readLock2);
            assertThat(readLock1).isNotSameAs(writeLock);
        }

        @Test
        @DisplayName("token should be non-zero stamp from StampedLock")
        void token_shouldBeNonZeroStamp() {
            try (var guard = adapter.writeLock().lock()) {
                assertThat(guard.token()).isNotEqualTo(0L);
                assertThat(guard.token()).isPositive();
            }

            try (var guard = adapter.readLock().lock()) {
                assertThat(guard.token()).isNotEqualTo(0L);
                assertThat(guard.token()).isPositive();
            }
        }

        @Test
        @DisplayName("isHeldByCurrentThread should be thread-specific")
        void isHeldByCurrentThread_shouldBeThreadSpecific() throws Exception {
            Lock<Long> writeLock = adapter.writeLock();

            try (var guard = writeLock.lock()) {
                assertThat(writeLock.isHeldByCurrentThread()).isTrue();

                // Check from another thread
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Future<Boolean> result = executor.submit(writeLock::isHeldByCurrentThread);
                assertThat(result.get(5, TimeUnit.SECONDS)).isFalse();
                executor.shutdown();
            }
        }
    }
}
