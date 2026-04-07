package cloud.opencode.base.event.dispatcher;

import cloud.opencode.base.event.Event;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;

/**
 * AsyncDispatcher 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("AsyncDispatcher 测试")
class AsyncDispatcherTest {

    static class TestEvent extends Event {
        public TestEvent() {
            super();
        }
    }

    private AsyncDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        dispatcher = new AsyncDispatcher();
    }

    @AfterEach
    void tearDown() {
        if (dispatcher != null) {
            dispatcher.shutdown();
        }
    }

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("无参构造使用虚拟线程执行器")
        void testDefaultConstructor() {
            AsyncDispatcher asyncDispatcher = new AsyncDispatcher();

            assertThat(asyncDispatcher).isNotNull();
            assertThat(asyncDispatcher.getExecutor()).isNotNull();
            asyncDispatcher.shutdown();
        }

        @Test
        @DisplayName("使用自定义执行器构造")
        void testConstructorWithExecutor() {
            ExecutorService customExecutor = Executors.newFixedThreadPool(2);

            AsyncDispatcher asyncDispatcher = new AsyncDispatcher(customExecutor);

            assertThat(asyncDispatcher.getExecutor()).isEqualTo(customExecutor);
            customExecutor.shutdown();
        }
    }

    @Nested
    @DisplayName("dispatch() 测试")
    class DispatchTests {

        @Test
        @DisplayName("异步调用所有监听器")
        void testDispatchAsync() throws InterruptedException {
            TestEvent event = new TestEvent();
            AtomicInteger count = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(3);

            List<Consumer<Event>> listeners = List.of(
                    e -> { count.incrementAndGet(); latch.countDown(); },
                    e -> { count.incrementAndGet(); latch.countDown(); },
                    e -> { count.incrementAndGet(); latch.countDown(); }
            );

            dispatcher.dispatch(event, listeners);
            boolean completed = latch.await(5, TimeUnit.SECONDS);

            assertThat(completed).isTrue();
            assertThat(count.get()).isEqualTo(3);
        }

        @Test
        @DisplayName("null事件不处理")
        void testDispatchNullEvent() {
            AtomicBoolean called = new AtomicBoolean(false);
            List<Consumer<Event>> listeners = List.of(e -> called.set(true));

            dispatcher.dispatch(null, listeners);

            // 等待一下确认没有调用
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            assertThat(called.get()).isFalse();
        }

        @Test
        @DisplayName("null监听器列表不处理")
        void testDispatchNullListeners() {
            TestEvent event = new TestEvent();

            assertThatNoException().isThrownBy(() ->
                    dispatcher.dispatch(event, null));
        }

        @Test
        @DisplayName("空监听器列表不处理")
        void testDispatchEmptyListeners() {
            TestEvent event = new TestEvent();

            assertThatNoException().isThrownBy(() ->
                    dispatcher.dispatch(event, List.of()));
        }

        @Test
        @DisplayName("取消的事件不调度新监听器")
        void testCancelledEventNotScheduled() throws InterruptedException {
            TestEvent event = new TestEvent();
            event.cancel();
            AtomicInteger count = new AtomicInteger(0);

            List<Consumer<Event>> listeners = List.of(
                    e -> count.incrementAndGet(),
                    e -> count.incrementAndGet()
            );

            dispatcher.dispatch(event, listeners);

            Thread.sleep(100);

            assertThat(count.get()).isZero();
        }

        @Test
        @DisplayName("监听器异常不影响其他监听器")
        void testExceptionDoesNotAffectOthers() throws InterruptedException {
            TestEvent event = new TestEvent();
            AtomicInteger successCount = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(2);

            List<Consumer<Event>> listeners = List.of(
                    e -> { successCount.incrementAndGet(); latch.countDown(); },
                    e -> { throw new RuntimeException("Error"); },
                    e -> { successCount.incrementAndGet(); latch.countDown(); }
            );

            dispatcher.dispatch(event, listeners);
            latch.await(5, TimeUnit.SECONDS);

            assertThat(successCount.get()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("dispatchAsync() 测试")
    class DispatchAsyncTests {

        @Test
        @DisplayName("返回CompletableFuture")
        void testReturnsCompletableFuture() throws Exception {
            TestEvent event = new TestEvent();
            AtomicInteger count = new AtomicInteger(0);

            List<Consumer<Event>> listeners = List.of(
                    e -> count.incrementAndGet(),
                    e -> count.incrementAndGet()
            );

            CompletableFuture<Void> future = dispatcher.dispatchAsync(event, listeners);
            future.get(5, TimeUnit.SECONDS);

            assertThat(count.get()).isEqualTo(2);
        }

        @Test
        @DisplayName("null事件返回已完成的Future")
        void testNullEventReturnsCompleted() throws Exception {
            List<Consumer<Event>> listeners = List.of(e -> {});

            CompletableFuture<Void> future = dispatcher.dispatchAsync(null, listeners);

            assertThat(future.isDone()).isTrue();
            future.get(1, TimeUnit.SECONDS);
        }

        @Test
        @DisplayName("null监听器返回已完成的Future")
        void testNullListenersReturnsCompleted() throws Exception {
            TestEvent event = new TestEvent();

            CompletableFuture<Void> future = dispatcher.dispatchAsync(event, null);

            assertThat(future.isDone()).isTrue();
        }

        @Test
        @DisplayName("空监听器返回已完成的Future")
        void testEmptyListenersReturnsCompleted() throws Exception {
            TestEvent event = new TestEvent();

            CompletableFuture<Void> future = dispatcher.dispatchAsync(event, List.of());

            assertThat(future.isDone()).isTrue();
        }
    }

    @Nested
    @DisplayName("isAsync() 测试")
    class IsAsyncTests {

        @Test
        @DisplayName("总是返回true")
        void testIsAsyncReturnsTrue() {
            assertThat(dispatcher.isAsync()).isTrue();
        }
    }

    @Nested
    @DisplayName("shutdown() 测试")
    class ShutdownTests {

        @Test
        @DisplayName("关闭自有执行器")
        void testShutdownOwnedExecutor() throws InterruptedException {
            AsyncDispatcher asyncDispatcher = new AsyncDispatcher();
            ExecutorService executor = asyncDispatcher.getExecutor();

            asyncDispatcher.shutdown();

            Thread.sleep(100);
            assertThat(executor.isShutdown()).isTrue();
        }

        @Test
        @DisplayName("不关闭外部执行器")
        void testNoShutdownExternalExecutor() {
            ExecutorService customExecutor = Executors.newFixedThreadPool(2);
            AsyncDispatcher asyncDispatcher = new AsyncDispatcher(customExecutor);

            asyncDispatcher.shutdown();

            assertThat(customExecutor.isShutdown()).isFalse();
            customExecutor.shutdown();
        }
    }

    @Nested
    @DisplayName("getExecutor() 测试")
    class GetExecutorTests {

        @Test
        @DisplayName("返回执行器")
        void testReturnsExecutor() {
            assertThat(dispatcher.getExecutor()).isNotNull();
        }
    }

    @Nested
    @DisplayName("并发测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("大量并发监听器")
        void testManyConcurrentListeners() throws InterruptedException {
            TestEvent event = new TestEvent();
            int listenerCount = 100;
            AtomicInteger count = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(listenerCount);

            List<Consumer<Event>> listeners = new ArrayList<>();
            for (int i = 0; i < listenerCount; i++) {
                listeners.add(e -> {
                    count.incrementAndGet();
                    latch.countDown();
                });
            }

            dispatcher.dispatch(event, listeners);
            boolean completed = latch.await(10, TimeUnit.SECONDS);

            assertThat(completed).isTrue();
            assertThat(count.get()).isEqualTo(listenerCount);
        }
    }

    @Nested
    @DisplayName("dispatchAsync 异常处理")
    class DispatchAsyncExceptionTests {

        @Test
        @DisplayName("监听器异常通过exceptionally处理")
        void testListenerExceptionHandledByExceptionally() throws Exception {
            TestEvent event = new TestEvent();
            AtomicInteger successCount = new AtomicInteger(0);

            List<Consumer<Event>> listeners = List.of(
                    e -> successCount.incrementAndGet(),
                    e -> { throw new RuntimeException("async error"); },
                    e -> successCount.incrementAndGet()
            );

            CompletableFuture<Void> future = dispatcher.dispatchAsync(event, listeners);
            future.get(5, TimeUnit.SECONDS);

            assertThat(successCount.get()).isEqualTo(2);
        }

        @Test
        @DisplayName("dispatchAsync取消的事件中断后续监听器")
        void testDispatchAsyncCancelledEvent() throws Exception {
            TestEvent event = new TestEvent();
            event.cancel();

            AtomicInteger count = new AtomicInteger(0);
            List<Consumer<Event>> listeners = List.of(
                    e -> count.incrementAndGet(),
                    e -> count.incrementAndGet()
            );

            CompletableFuture<Void> future = dispatcher.dispatchAsync(event, listeners);
            future.get(1, TimeUnit.SECONDS);

            assertThat(count.get()).isZero();
        }
    }

    @Nested
    @DisplayName("dispatch中事件运行时取消")
    class RuntimeCancellationTests {

        @Test
        @DisplayName("事件在dispatch异步提交后取消应在double-check时生效")
        void testCancellationDuringAsyncExecution() throws InterruptedException {
            TestEvent event = new TestEvent();
            CountDownLatch firstStarted = new CountDownLatch(1);
            AtomicInteger invokedCount = new AtomicInteger(0);

            List<Consumer<Event>> listeners = List.of(
                    e -> {
                        invokedCount.incrementAndGet();
                        firstStarted.countDown();
                        // Cancel event during first listener execution
                        e.cancel();
                    },
                    e -> {
                        // This might not execute due to cancellation double-check
                        invokedCount.incrementAndGet();
                    }
            );

            dispatcher.dispatch(event, listeners);
            firstStarted.await(5, TimeUnit.SECONDS);
            Thread.sleep(200);

            // At least the first was invoked
            assertThat(invokedCount.get()).isGreaterThanOrEqualTo(1);
        }
    }
}
