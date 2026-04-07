package cloud.opencode.base.lock.event;

import cloud.opencode.base.lock.Lock;
import cloud.opencode.base.lock.LockGuard;
import cloud.opencode.base.lock.exception.OpenLockTimeoutException;
import cloud.opencode.base.lock.local.LocalLock;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * ObservableLock test - 可观察锁测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.3
 */
class ObservableLockTest {

    private LocalLock delegate;
    private ObservableLock<Long> lock;
    private List<LockEvent> capturedEvents;
    private LockListener capturingListener;

    @BeforeEach
    void setUp() {
        delegate = new LocalLock();
        capturedEvents = Collections.synchronizedList(new ArrayList<>());
        capturingListener = capturedEvents::add;
        lock = new ObservableLock<>(delegate, "test-lock", capturingListener);
    }

    @Nested
    @DisplayName("Acquire Event Tests | 获取事件测试")
    class AcquireEventTests {

        @Test
        @DisplayName("lock() should fire ACQUIRED event")
        void lock_shouldFireAcquiredEvent() {
            try (var guard = lock.lock()) {
                assertThat(capturedEvents).hasSize(1);

                LockEvent event = capturedEvents.getFirst();
                assertThat(event.type()).isEqualTo(LockEvent.EventType.ACQUIRED);
                assertThat(event.lockName()).isEqualTo("test-lock");
                assertThat(event.waitTime()).isNotNull();
                assertThat(event.threadName()).isEqualTo(Thread.currentThread().getName());
            }
        }

        @Test
        @DisplayName("lock(timeout) should fire ACQUIRED event on success")
        void lockWithTimeout_shouldFireAcquiredEventOnSuccess() {
            try (var guard = lock.lock(Duration.ofSeconds(5))) {
                assertThat(capturedEvents).hasSize(1);

                LockEvent event = capturedEvents.getFirst();
                assertThat(event.type()).isEqualTo(LockEvent.EventType.ACQUIRED);
                assertThat(event.lockName()).isEqualTo("test-lock");
                assertThat(event.waitTime()).isNotNull();
            }
        }

        @Test
        @DisplayName("tryLock() should fire ACQUIRED event on success")
        void tryLock_shouldFireAcquiredEventOnSuccess() {
            boolean acquired = lock.tryLock();

            assertThat(acquired).isTrue();
            assertThat(capturedEvents).hasSize(1);

            LockEvent event = capturedEvents.getFirst();
            assertThat(event.type()).isEqualTo(LockEvent.EventType.ACQUIRED);
            assertThat(event.waitTime()).isEqualTo(Duration.ZERO);

            lock.unlock();
        }

        @Test
        @DisplayName("tryLock() should not fire event on failure")
        void tryLock_shouldNotFireEventOnFailure() throws Exception {
            CountDownLatch locked = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(1);

            Thread holder = Thread.ofVirtual().start(() -> {
                delegate.lock();
                locked.countDown();
                try {
                    done.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    delegate.unlock();
                }
            });

            locked.await(5, TimeUnit.SECONDS);
            capturedEvents.clear();

            boolean acquired = lock.tryLock();

            assertThat(acquired).isFalse();
            assertThat(capturedEvents).isEmpty();

            done.countDown();
            holder.join(5000);
        }

        @Test
        @DisplayName("tryLock(timeout) should fire ACQUIRED event on success")
        void tryLockWithTimeout_shouldFireAcquiredEventOnSuccess() {
            boolean acquired = lock.tryLock(Duration.ofSeconds(5));

            assertThat(acquired).isTrue();
            assertThat(capturedEvents).hasSize(1);

            LockEvent event = capturedEvents.getFirst();
            assertThat(event.type()).isEqualTo(LockEvent.EventType.ACQUIRED);

            lock.unlock();
        }

        @Test
        @DisplayName("lockInterruptibly() should fire ACQUIRED event")
        void lockInterruptibly_shouldFireAcquiredEvent() throws InterruptedException {
            LockGuard<Long> guard = lock.lockInterruptibly();

            assertThat(capturedEvents).hasSize(1);

            LockEvent event = capturedEvents.getFirst();
            assertThat(event.type()).isEqualTo(LockEvent.EventType.ACQUIRED);
            assertThat(event.waitTime()).isNotNull();

            guard.close();
        }
    }

    @Nested
    @DisplayName("Release Event Tests | 释放事件测试")
    class ReleaseEventTests {

        @Test
        @DisplayName("unlock() should fire RELEASED event")
        void unlock_shouldFireReleasedEvent() {
            lock.lock();
            capturedEvents.clear();

            lock.unlock();

            assertThat(capturedEvents).hasSize(1);

            LockEvent event = capturedEvents.getFirst();
            assertThat(event.type()).isEqualTo(LockEvent.EventType.RELEASED);
            assertThat(event.lockName()).isEqualTo("test-lock");
            assertThat(event.waitTime()).isNull();
        }

        @Test
        @DisplayName("LockGuard close should fire RELEASED event")
        void lockGuardClose_shouldFireReleasedEvent() {
            LockGuard<Long> guard = lock.lock();
            capturedEvents.clear();

            guard.close();

            assertThat(capturedEvents).hasSize(1);
            assertThat(capturedEvents.getFirst().type())
                    .isEqualTo(LockEvent.EventType.RELEASED);
        }

        @Test
        @DisplayName("try-with-resources should fire ACQUIRED then RELEASED")
        void tryWithResources_shouldFireAcquiredThenReleased() {
            try (var guard = lock.lock()) {
                // held
            }

            assertThat(capturedEvents).hasSize(2);
            assertThat(capturedEvents.get(0).type())
                    .isEqualTo(LockEvent.EventType.ACQUIRED);
            assertThat(capturedEvents.get(1).type())
                    .isEqualTo(LockEvent.EventType.RELEASED);
        }
    }

    @Nested
    @DisplayName("Timeout Event Tests | 超时事件测试")
    class TimeoutEventTests {

        @Test
        @DisplayName("lock(timeout) should fire TIMEOUT event on failure")
        void lockWithTimeout_shouldFireTimeoutEventOnFailure() throws Exception {
            CountDownLatch locked = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(1);

            Thread holder = Thread.ofVirtual().start(() -> {
                delegate.lock();
                locked.countDown();
                try {
                    done.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    delegate.unlock();
                }
            });

            locked.await(5, TimeUnit.SECONDS);
            capturedEvents.clear();

            assertThatThrownBy(() -> lock.lock(Duration.ofMillis(50)))
                    .isInstanceOf(OpenLockTimeoutException.class);

            assertThat(capturedEvents).hasSize(1);

            LockEvent event = capturedEvents.getFirst();
            assertThat(event.type()).isEqualTo(LockEvent.EventType.TIMEOUT);
            assertThat(event.lockName()).isEqualTo("test-lock");
            assertThat(event.waitTime()).isNotNull();
            assertThat(event.waitTime()).isGreaterThanOrEqualTo(Duration.ofMillis(40));

            done.countDown();
            holder.join(5000);
        }

        @Test
        @DisplayName("tryLock(timeout) should fire TIMEOUT event on failure")
        void tryLockWithTimeout_shouldFireTimeoutEventOnFailure() throws Exception {
            CountDownLatch locked = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(1);

            Thread holder = Thread.ofVirtual().start(() -> {
                delegate.lock();
                locked.countDown();
                try {
                    done.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    delegate.unlock();
                }
            });

            locked.await(5, TimeUnit.SECONDS);
            capturedEvents.clear();

            boolean acquired = lock.tryLock(Duration.ofMillis(50));

            assertThat(acquired).isFalse();
            assertThat(capturedEvents).hasSize(1);

            LockEvent event = capturedEvents.getFirst();
            assertThat(event.type()).isEqualTo(LockEvent.EventType.TIMEOUT);
            assertThat(event.waitTime()).isNotNull();

            done.countDown();
            holder.join(5000);
        }
    }

    @Nested
    @DisplayName("Listener Management Tests | 监听器管理测试")
    class ListenerManagementTests {

        @Test
        @DisplayName("addListener should register listener")
        void addListener_shouldRegisterListener() {
            ObservableLock<Long> freshLock = new ObservableLock<>(new LocalLock(), "fresh");
            AtomicReference<LockEvent> captured = new AtomicReference<>();

            freshLock.addListener(captured::set);

            try (var guard = freshLock.lock()) {
                // held
            }

            assertThat(captured.get()).isNotNull();
            // Last event should be RELEASED
            assertThat(captured.get().type()).isEqualTo(LockEvent.EventType.RELEASED);
        }

        @Test
        @DisplayName("removeListener should unregister listener")
        void removeListener_shouldUnregisterListener() {
            ObservableLock<Long> freshLock = new ObservableLock<>(new LocalLock(), "fresh");
            List<LockEvent> events = new ArrayList<>();
            LockListener listener = events::add;

            freshLock.addListener(listener);

            try (var guard = freshLock.lock()) {
                // held
            }

            int eventCountBefore = events.size();

            freshLock.removeListener(listener);
            events.clear();

            try (var guard = freshLock.lock()) {
                // held
            }

            assertThat(eventCountBefore).isGreaterThan(0);
            assertThat(events).isEmpty();
        }

        @Test
        @DisplayName("multiple listeners should all receive events")
        void multipleListeners_shouldAllReceiveEvents() {
            ObservableLock<Long> freshLock = new ObservableLock<>(new LocalLock(), "fresh");
            AtomicInteger counter1 = new AtomicInteger();
            AtomicInteger counter2 = new AtomicInteger();
            AtomicInteger counter3 = new AtomicInteger();

            freshLock.addListener(e -> counter1.incrementAndGet());
            freshLock.addListener(e -> counter2.incrementAndGet());
            freshLock.addListener(e -> counter3.incrementAndGet());

            try (var guard = freshLock.lock()) {
                // held
            }

            // Should fire ACQUIRED + RELEASED = 2 events to each listener
            assertThat(counter1.get()).isEqualTo(2);
            assertThat(counter2.get()).isEqualTo(2);
            assertThat(counter3.get()).isEqualTo(2);
        }

        @Test
        @DisplayName("addListener should return this for fluent chaining")
        void addListener_shouldReturnThisForFluentChaining() {
            ObservableLock<Long> freshLock = new ObservableLock<>(new LocalLock(), "fresh");

            ObservableLock<Long> result = freshLock
                    .addListener(e -> {})
                    .addListener(e -> {});

            assertThat(result).isSameAs(freshLock);
        }

        @Test
        @DisplayName("removeListener should return this for fluent chaining")
        void removeListener_shouldReturnThisForFluentChaining() {
            LockListener listener = e -> {};

            ObservableLock<Long> result = lock.removeListener(listener);

            assertThat(result).isSameAs(lock);
        }

        @Test
        @DisplayName("addListener with null should throw")
        void addListener_withNull_shouldThrow() {
            assertThatThrownBy(() -> lock.addListener(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Listener must not be null");
        }

        @Test
        @DisplayName("removeListener with null should not throw")
        void removeListener_withNull_shouldNotThrow() {
            assertThatCode(() -> lock.removeListener(null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("constructor with listeners should register them")
        void constructor_withListeners_shouldRegisterThem() {
            AtomicInteger counter = new AtomicInteger();
            LockListener listener1 = e -> counter.incrementAndGet();
            LockListener listener2 = e -> counter.incrementAndGet();

            ObservableLock<Long> freshLock = new ObservableLock<>(
                    new LocalLock(), "fresh", listener1, listener2
            );

            try (var guard = freshLock.lock()) {
                // held
            }

            // 2 listeners x 2 events (ACQUIRED + RELEASED) = 4
            assertThat(counter.get()).isEqualTo(4);
        }

        @Test
        @DisplayName("constructor with null listener array should not throw")
        void constructor_withNullListenerArray_shouldNotThrow() {
            assertThatCode(() -> new ObservableLock<>(
                    new LocalLock(), "fresh", (LockListener[]) null
            )).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Listener Isolation Tests | 监听器隔离测试")
    class ListenerIsolationTests {

        @Test
        @DisplayName("listener exception should not affect lock operation")
        void listenerException_shouldNotAffectLockOperation() {
            ObservableLock<Long> freshLock = new ObservableLock<>(new LocalLock(), "fresh");

            freshLock.addListener(e -> {
                throw new RuntimeException("Listener failure!");
            });

            // Lock should still work despite listener exception
            assertThatCode(() -> {
                try (var guard = freshLock.lock()) {
                    assertThat(freshLock.isHeldByCurrentThread()).isTrue();
                }
            }).doesNotThrowAnyException();

            assertThat(freshLock.isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("failing listener should not prevent other listeners from receiving events")
        void failingListener_shouldNotPreventOtherListeners() {
            ObservableLock<Long> freshLock = new ObservableLock<>(new LocalLock(), "fresh");
            List<LockEvent> events = new ArrayList<>();

            // First listener throws
            freshLock.addListener(e -> {
                throw new RuntimeException("Boom!");
            });
            // Second listener should still receive events
            freshLock.addListener(events::add);

            try (var guard = freshLock.lock()) {
                // held
            }

            assertThat(events).hasSize(2);
            assertThat(events.get(0).type()).isEqualTo(LockEvent.EventType.ACQUIRED);
            assertThat(events.get(1).type()).isEqualTo(LockEvent.EventType.RELEASED);
        }

        @Test
        @DisplayName("listener exception during unlock should not prevent lock release")
        void listenerExceptionDuringUnlock_shouldNotPreventLockRelease() {
            ObservableLock<Long> freshLock = new ObservableLock<>(new LocalLock(), "fresh");

            freshLock.addListener(e -> {
                if (e.type() == LockEvent.EventType.RELEASED) {
                    throw new RuntimeException("Release listener failure!");
                }
            });

            freshLock.lock();
            assertThat(freshLock.isHeldByCurrentThread()).isTrue();

            // unlock should complete despite listener exception
            assertThatCode(freshLock::unlock).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Delegation Tests | 委托测试")
    class DelegationTests {

        @Test
        @DisplayName("lock() should delegate to underlying lock")
        void lock_shouldDelegateToUnderlyingLock() {
            LockGuard<Long> guard = lock.lock();

            assertThat(delegate.isHeldByCurrentThread()).isTrue();
            assertThat(guard.token()).isNotNull();

            guard.close();

            assertThat(delegate.isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("tryLock() should delegate to underlying lock")
        void tryLock_shouldDelegateToUnderlyingLock() {
            boolean acquired = lock.tryLock();

            assertThat(acquired).isTrue();
            assertThat(delegate.isHeldByCurrentThread()).isTrue();

            lock.unlock();

            assertThat(delegate.isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("isHeldByCurrentThread() should delegate to underlying lock")
        void isHeldByCurrentThread_shouldDelegate() {
            assertThat(lock.isHeldByCurrentThread()).isFalse();

            lock.lock();
            assertThat(lock.isHeldByCurrentThread()).isTrue();

            lock.unlock();
            assertThat(lock.isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("getToken() should delegate to underlying lock")
        void getToken_shouldDelegate() {
            assertThat(lock.getToken()).isEmpty();

            lock.lock();
            Optional<Long> token = lock.getToken();
            assertThat(token).isPresent();

            lock.unlock();
        }

        @Test
        @DisplayName("constructor should reject null delegate")
        void constructor_shouldRejectNullDelegate() {
            assertThatThrownBy(() -> new ObservableLock<>(null, "test"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Delegate lock must not be null");
        }

        @Test
        @DisplayName("constructor should reject null lockName")
        void constructor_shouldRejectNullLockName() {
            assertThatThrownBy(() -> new ObservableLock<>(new LocalLock(), null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Lock name must not be null");
        }

        @Test
        @DisplayName("getLockName() should return configured name")
        void getLockName_shouldReturnConfiguredName() {
            assertThat(lock.getLockName()).isEqualTo("test-lock");
        }

        @Test
        @DisplayName("execute() default method should work through decorator")
        void execute_shouldWorkThroughDecorator() {
            AtomicInteger counter = new AtomicInteger();

            lock.execute(counter::incrementAndGet);

            assertThat(counter.get()).isEqualTo(1);
            assertThat(capturedEvents).hasSize(2);
            assertThat(capturedEvents.get(0).type())
                    .isEqualTo(LockEvent.EventType.ACQUIRED);
            assertThat(capturedEvents.get(1).type())
                    .isEqualTo(LockEvent.EventType.RELEASED);
        }

        @Test
        @DisplayName("executeWithResult() default method should work through decorator")
        void executeWithResult_shouldWorkThroughDecorator() {
            String result = lock.executeWithResult(() -> "hello");

            assertThat(result).isEqualTo("hello");
            assertThat(capturedEvents).hasSize(2);
        }
    }
}
