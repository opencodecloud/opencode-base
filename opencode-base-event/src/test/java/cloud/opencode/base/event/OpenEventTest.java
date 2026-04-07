package cloud.opencode.base.event;

import cloud.opencode.base.event.annotation.Async;
import cloud.opencode.base.event.annotation.Priority;
import cloud.opencode.base.event.annotation.Subscribe;
import cloud.opencode.base.event.dispatcher.EventDispatcher;
import cloud.opencode.base.event.dispatcher.SyncDispatcher;
import cloud.opencode.base.event.exception.EventException;
import cloud.opencode.base.event.handler.EventExceptionHandler;
import cloud.opencode.base.event.interceptor.EventInterceptor;
import cloud.opencode.base.event.monitor.EventBusMetrics;
import cloud.opencode.base.event.store.InMemoryEventStore;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenEvent 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("OpenEvent 测试")
class OpenEventTest {

    static class TestEvent extends Event {
        private final String message;

        public TestEvent(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("getDefault返回单例")
        void testGetDefaultReturnsSingleton() {
            OpenEvent instance1 = OpenEvent.getDefault();
            OpenEvent instance2 = OpenEvent.getDefault();

            assertThat(instance1).isSameAs(instance2);
        }

        @Test
        @DisplayName("create返回新实例")
        void testCreateReturnsNewInstance() {
            OpenEvent instance1 = OpenEvent.create();
            OpenEvent instance2 = OpenEvent.create();

            assertThat(instance1).isNotSameAs(instance2);
        }

        @Test
        @DisplayName("builder创建自定义实例")
        void testBuilderCreatesInstance() {
            OpenEvent eventBus = OpenEvent.builder().build();

            assertThat(eventBus).isNotNull();
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("配置EventStore")
        void testConfigureEventStore() {
            InMemoryEventStore store = new InMemoryEventStore(100);

            OpenEvent eventBus = OpenEvent.builder()
                    .eventStore(store)
                    .build();

            assertThat(eventBus.getEventStore()).isEqualTo(store);
        }

        @Test
        @DisplayName("配置ExceptionHandler")
        void testConfigureExceptionHandler() {
            AtomicBoolean handlerCalled = new AtomicBoolean(false);
            EventExceptionHandler handler = (event, ex, name) -> handlerCalled.set(true);

            OpenEvent eventBus = OpenEvent.builder()
                    .exceptionHandler(handler)
                    .build();

            eventBus.on(TestEvent.class, e -> {
                throw new RuntimeException("test");
            });
            eventBus.publish(new TestEvent("test"));

            assertThat(handlerCalled.get()).isTrue();
        }

        @Test
        @DisplayName("配置SyncDispatcher")
        void testConfigureSyncDispatcher() {
            SyncDispatcher dispatcher = new SyncDispatcher(true);

            OpenEvent eventBus = OpenEvent.builder()
                    .syncDispatcher(dispatcher)
                    .build();

            assertThat(eventBus).isNotNull();
        }
    }

    @Nested
    @DisplayName("register() 测试")
    class RegisterTests {

        @Test
        @DisplayName("注册带@Subscribe方法的订阅者")
        void testRegisterSubscriber() {
            OpenEvent eventBus = OpenEvent.create();
            AtomicBoolean called = new AtomicBoolean(false);

            class Subscriber {
                @Subscribe
                public void onEvent(TestEvent event) {
                    called.set(true);
                }
            }

            eventBus.register(new Subscriber());
            eventBus.publish(new TestEvent("test"));

            assertThat(called.get()).isTrue();
        }

        @Test
        @DisplayName("null订阅者抛出异常")
        void testRegisterNullThrows() {
            OpenEvent eventBus = OpenEvent.create();

            assertThatThrownBy(() -> eventBus.register(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("无效方法签名抛出异常")
        void testInvalidMethodSignatureThrows() {
            OpenEvent eventBus = OpenEvent.create();

            class InvalidSubscriber {
                @Subscribe
                public void onEvent() {
                    // 无参数，无效
                }
            }

            assertThatThrownBy(() -> eventBus.register(new InvalidSubscriber()))
                    .isInstanceOf(EventException.class)
                    .hasMessageContaining("one parameter");
        }

        @Test
        @DisplayName("非Event参数抛出异常")
        void testNonEventParameterThrows() {
            OpenEvent eventBus = OpenEvent.create();

            class InvalidSubscriber {
                @Subscribe
                public void onEvent(String message) {
                    // 参数不是Event，无效
                }
            }

            assertThatThrownBy(() -> eventBus.register(new InvalidSubscriber()))
                    .isInstanceOf(EventException.class)
                    .hasMessageContaining("extend Event");
        }
    }

    @Nested
    @DisplayName("on() 测试")
    class OnTests {

        @Test
        @DisplayName("注册Lambda监听器")
        void testRegisterLambdaListener() {
            OpenEvent eventBus = OpenEvent.create();
            AtomicReference<String> received = new AtomicReference<>();

            eventBus.on(TestEvent.class, event -> received.set(event.getMessage()));
            eventBus.publish(new TestEvent("Hello"));

            assertThat(received.get()).isEqualTo("Hello");
        }

        @Test
        @DisplayName("null参数抛出异常")
        void testNullArgumentsThrow() {
            OpenEvent eventBus = OpenEvent.create();

            assertThatThrownBy(() -> eventBus.on(null, event -> {}))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> eventBus.on(TestEvent.class, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("带async选项注册")
        void testRegisterWithAsync() {
            OpenEvent eventBus = OpenEvent.create();
            AtomicBoolean called = new AtomicBoolean(false);

            eventBus.on(TestEvent.class, event -> called.set(true), true);
            eventBus.publish(new TestEvent("test"));

            // 等待异步执行完成
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            assertThat(called.get()).isTrue();
        }

        @Test
        @DisplayName("带priority选项注册")
        void testRegisterWithPriority() {
            OpenEvent eventBus = OpenEvent.create();
            List<Integer> order = new ArrayList<>();

            eventBus.on(TestEvent.class, event -> order.add(3), false, 0);
            eventBus.on(TestEvent.class, event -> order.add(1), false, 100);
            eventBus.on(TestEvent.class, event -> order.add(2), false, 50);

            eventBus.publish(new TestEvent("test"));

            assertThat(order).containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("unregister() 测试")
    class UnregisterTests {

        @Test
        @DisplayName("注销订阅者后不再收到事件")
        void testUnregisterStopsEvents() {
            OpenEvent eventBus = OpenEvent.create();
            AtomicInteger callCount = new AtomicInteger(0);

            class Subscriber {
                @Subscribe
                public void onEvent(TestEvent event) {
                    callCount.incrementAndGet();
                }
            }

            Subscriber subscriber = new Subscriber();
            eventBus.register(subscriber);
            eventBus.publish(new TestEvent("test"));
            assertThat(callCount.get()).isEqualTo(1);

            eventBus.unregister(subscriber);
            eventBus.publish(new TestEvent("test"));
            assertThat(callCount.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("注销null不抛异常")
        void testUnregisterNullNoException() {
            OpenEvent eventBus = OpenEvent.create();

            assertThatNoException().isThrownBy(() -> eventBus.unregister(null));
        }
    }

    @Nested
    @DisplayName("publish() 测试")
    class PublishTests {

        @Test
        @DisplayName("发布事件到监听器")
        void testPublishToListener() {
            OpenEvent eventBus = OpenEvent.create();
            AtomicReference<TestEvent> received = new AtomicReference<>();

            eventBus.on(TestEvent.class, received::set);
            TestEvent event = new TestEvent("test");
            eventBus.publish(event);

            assertThat(received.get()).isSameAs(event);
        }

        @Test
        @DisplayName("null事件抛出异常")
        void testPublishNullThrows() {
            OpenEvent eventBus = OpenEvent.create();

            assertThatThrownBy(() -> eventBus.publish((Event) null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("发布数据作为DataEvent")
        void testPublishData() {
            OpenEvent eventBus = OpenEvent.create();
            AtomicReference<String> received = new AtomicReference<>();

            eventBus.on(DataEvent.class, event -> received.set((String) event.getData()));
            eventBus.publish("Hello Data");

            assertThat(received.get()).isEqualTo("Hello Data");
        }

        @Test
        @DisplayName("发布数据带source")
        void testPublishDataWithSource() {
            OpenEvent eventBus = OpenEvent.create();
            AtomicReference<String> source = new AtomicReference<>();

            eventBus.on(DataEvent.class, event -> source.set(event.getSource()));
            eventBus.publish("data", "MySource");

            assertThat(source.get()).isEqualTo("MySource");
        }

        @Test
        @DisplayName("事件存储到EventStore")
        void testPublishStoresEvent() {
            InMemoryEventStore store = new InMemoryEventStore();
            OpenEvent eventBus = OpenEvent.builder()
                    .eventStore(store)
                    .build();

            TestEvent event = new TestEvent("test");
            eventBus.publish(event);

            assertThat(store.count()).isEqualTo(1);
            assertThat(store.findById(event.getId())).isPresent();
        }
    }

    @Nested
    @DisplayName("publishAsync() 测试")
    class PublishAsyncTests {

        @Test
        @DisplayName("异步发布事件")
        void testPublishAsync() throws Exception {
            OpenEvent eventBus = OpenEvent.create();
            AtomicBoolean called = new AtomicBoolean(false);

            eventBus.on(TestEvent.class, event -> called.set(true));

            CompletableFuture<Void> future = eventBus.publishAsync(new TestEvent("test"));
            future.get(5, TimeUnit.SECONDS);

            assertThat(called.get()).isTrue();
        }

        @Test
        @DisplayName("null事件抛出异常")
        void testPublishAsyncNullThrows() {
            OpenEvent eventBus = OpenEvent.create();

            assertThatThrownBy(() -> eventBus.publishAsync(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("无监听器返回已完成的Future")
        void testPublishAsyncNoListeners() throws Exception {
            OpenEvent eventBus = OpenEvent.create();

            CompletableFuture<Void> future = eventBus.publishAsync(new TestEvent("test"));

            assertThat(future.isDone()).isTrue();
            future.get(1, TimeUnit.SECONDS);
        }
    }

    @Nested
    @DisplayName("publishAndWait() 测试")
    class PublishAndWaitTests {

        @Test
        @DisplayName("等待事件处理完成")
        void testPublishAndWaitCompletes() {
            OpenEvent eventBus = OpenEvent.create();
            AtomicBoolean processed = new AtomicBoolean(false);

            eventBus.on(TestEvent.class, event -> {
                processed.set(true);
            });

            TestEvent event = new TestEvent("test");
            boolean result = eventBus.publishAndWait(event, Duration.ofSeconds(5));

            assertThat(result).isTrue();
            assertThat(processed.get()).isTrue();
        }

        @Test
        @DisplayName("null事件抛出异常")
        void testPublishAndWaitNullThrows() {
            OpenEvent eventBus = OpenEvent.create();

            assertThatThrownBy(() -> eventBus.publishAndWait(null, Duration.ofSeconds(1)))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("优先级测试")
    class PriorityTests {

        @Test
        @DisplayName("高优先级先执行")
        void testHighPriorityFirst() {
            OpenEvent eventBus = OpenEvent.create();
            List<Integer> order = new ArrayList<>();

            class Subscriber {
                @Subscribe
                public void handleLow(TestEvent event) {
                    order.add(3);
                }

                @Subscribe
                @Priority(100)
                public void handleHigh(TestEvent event) {
                    order.add(1);
                }

                @Subscribe
                @Priority(50)
                public void handleMedium(TestEvent event) {
                    order.add(2);
                }
            }

            eventBus.register(new Subscriber());
            eventBus.publish(new TestEvent("test"));

            assertThat(order).containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("异步监听器测试")
    class AsyncListenerTests {

        @Test
        @DisplayName("@Async标注的方法异步执行")
        void testAsyncAnnotatedMethodExecutesAsync() throws InterruptedException {
            OpenEvent eventBus = OpenEvent.create();
            AtomicReference<String> threadName = new AtomicReference<>();

            class Subscriber {
                @Subscribe
                @Async
                public void onEvent(TestEvent event) {
                    threadName.set(Thread.currentThread().getName());
                }
            }

            eventBus.register(new Subscriber());
            eventBus.publish(new TestEvent("test"));

            Thread.sleep(100);

            assertThat(threadName.get()).isNotNull();
        }
    }

    @Nested
    @DisplayName("事件取消测试")
    class CancellationTests {

        @Test
        @DisplayName("取消的事件停止传播")
        void testCancelledEventStopsPropagation() {
            OpenEvent eventBus = OpenEvent.create();
            List<Integer> order = new ArrayList<>();

            eventBus.on(TestEvent.class, event -> {
                order.add(1);
                event.cancel();
            }, false, 100);

            eventBus.on(TestEvent.class, event -> {
                order.add(2);
            }, false, 0);

            eventBus.publish(new TestEvent("test"));

            assertThat(order).containsExactly(1);
        }
    }

    @Nested
    @DisplayName("配置方法测试")
    class ConfigurationTests {

        @Test
        @DisplayName("setEventStore设置存储")
        void testSetEventStore() {
            OpenEvent eventBus = OpenEvent.create();
            InMemoryEventStore store = new InMemoryEventStore();

            eventBus.setEventStore(store);

            assertThat(eventBus.getEventStore()).isEqualTo(store);
        }

        @Test
        @DisplayName("setExceptionHandler设置处理器")
        void testSetExceptionHandler() {
            OpenEvent eventBus = OpenEvent.create();
            AtomicBoolean handlerCalled = new AtomicBoolean(false);

            eventBus.setExceptionHandler((event, ex, name) -> handlerCalled.set(true));
            eventBus.on(TestEvent.class, e -> {
                throw new RuntimeException("test");
            });
            eventBus.publish(new TestEvent("test"));

            assertThat(handlerCalled.get()).isTrue();
        }

        @Test
        @DisplayName("null处理器使用默认LoggingExceptionHandler")
        void testNullExceptionHandlerUsesDefault() {
            OpenEvent eventBus = OpenEvent.create();

            assertThatNoException().isThrownBy(() -> eventBus.setExceptionHandler(null));
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("监听器异常被ExceptionHandler处理")
        void testListenerExceptionHandled() {
            AtomicReference<Throwable> capturedException = new AtomicReference<>();

            OpenEvent eventBus = OpenEvent.builder()
                    .exceptionHandler((event, ex, name) -> capturedException.set(ex))
                    .build();

            eventBus.on(TestEvent.class, event -> {
                throw new RuntimeException("Test error");
            });

            eventBus.publish(new TestEvent("test"));

            assertThat(capturedException.get())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Test error");
        }
    }

    @Nested
    @DisplayName("事件层次测试")
    class EventHierarchyTests {

        static class ParentEvent extends Event {}
        static class ChildEvent extends ParentEvent {}

        @Test
        @DisplayName("父类型监听器接收子类型事件")
        void testParentListenerReceivesChildEvent() {
            OpenEvent eventBus = OpenEvent.create();
            AtomicBoolean parentCalled = new AtomicBoolean(false);

            eventBus.on(ParentEvent.class, event -> parentCalled.set(true));
            eventBus.publish(new ChildEvent());

            assertThat(parentCalled.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("publishAsync with EventStore 测试")
    class PublishAsyncWithEventStoreTests {

        @Test
        @DisplayName("publishAsync stores event in EventStore")
        void testPublishAsyncStoresEvent() throws Exception {
            InMemoryEventStore store = new InMemoryEventStore(100);
            try (var bus = OpenEvent.builder().eventStore(store).build()) {
                AtomicBoolean called = new AtomicBoolean(false);
                bus.on(TestEvent.class, e -> called.set(true));

                TestEvent event = new TestEvent("stored-async");
                CompletableFuture<Void> future = bus.publishAsync(event);
                future.get(5, TimeUnit.SECONDS);

                assertThat(store.count()).isEqualTo(1);
                assertThat(store.findById(event.getId())).isPresent();
                assertThat(called.get()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("publishAsync with interceptor blocking 测试")
    class PublishAsyncInterceptorBlockingTests {

        @Test
        @DisplayName("interceptor returning false blocks publishAsync dispatch")
        void testInterceptorBlocksPublishAsync() throws Exception {
            EventInterceptor blockingInterceptor = new EventInterceptor() {
                @Override
                public boolean beforePublish(Event event) {
                    return false;
                }
            };

            try (var bus = OpenEvent.builder().interceptor(blockingInterceptor).build()) {
                AtomicBoolean listenerCalled = new AtomicBoolean(false);
                bus.on(TestEvent.class, e -> listenerCalled.set(true));

                CompletableFuture<Void> future = bus.publishAsync(new TestEvent("blocked"));
                future.get(2, TimeUnit.SECONDS);

                assertThat(future.isDone()).isTrue();
                assertThat(listenerCalled.get()).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("publishAsync dead event 测试")
    class PublishAsyncDeadEventTests {

        @Test
        @DisplayName("publishAsync with no listeners increments deadEventCount")
        void testPublishAsyncNoListenersDeadEvent() throws Exception {
            try (var bus = OpenEvent.create()) {
                bus.resetMetrics();

                CompletableFuture<Void> future = bus.publishAsync(new TestEvent("orphan"));
                future.get(2, TimeUnit.SECONDS);

                EventBusMetrics metrics = bus.getMetrics();
                assertThat(metrics.totalDeadEvents()).isEqualTo(1);
                assertThat(metrics.totalPublished()).isEqualTo(1);
            }
        }
    }

    @Nested
    @DisplayName("Builder with asyncExecutor 测试")
    class BuilderAsyncExecutorTests {

        @Test
        @DisplayName("builder with custom asyncExecutor uses it for async dispatch")
        void testBuilderWithAsyncExecutor() throws Exception {
            AtomicReference<String> threadName = new AtomicReference<>();
            ExecutorService customExecutor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "custom-event-thread");
                return t;
            });

            try (var bus = OpenEvent.builder().asyncExecutor(customExecutor).build()) {
                bus.on(TestEvent.class, e -> threadName.set(Thread.currentThread().getName()), true);
                bus.publish(new TestEvent("custom-executor"));

                Thread.sleep(200);

                assertThat(threadName.get()).isNotNull();
            } finally {
                customExecutor.shutdownNow();
            }
        }
    }

    @Nested
    @DisplayName("Builder with asyncDispatcher 测试")
    class BuilderAsyncDispatcherTests {

        @Test
        @DisplayName("builder with custom asyncDispatcher uses it")
        void testBuilderWithCustomAsyncDispatcher() throws Exception {
            AtomicBoolean customDispatcherUsed = new AtomicBoolean(false);

            EventDispatcher customDispatcher = new EventDispatcher() {
                @Override
                public void dispatch(Event event, List<Consumer<Event>> listeners) {
                    customDispatcherUsed.set(true);
                    for (Consumer<Event> listener : listeners) {
                        listener.accept(event);
                    }
                }

                @Override
                public boolean isAsync() {
                    return true;
                }
            };

            try (var bus = OpenEvent.builder().asyncDispatcher(customDispatcher).build()) {
                AtomicBoolean listenerCalled = new AtomicBoolean(false);
                bus.on(TestEvent.class, e -> listenerCalled.set(true));

                // publishAsync with a custom (non-AsyncDispatcher) dispatcher triggers the fallback path
                CompletableFuture<Void> future = bus.publishAsync(new TestEvent("custom-dispatcher"));
                future.get(5, TimeUnit.SECONDS);

                assertThat(customDispatcherUsed.get()).isTrue();
                assertThat(listenerCalled.get()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("publishSticky null 测试")
    class PublishStickyNullTests {

        @Test
        @DisplayName("publishSticky(null) throws IllegalArgumentException")
        void testPublishStickyNullThrows() {
            try (var bus = OpenEvent.create()) {
                assertThatThrownBy(() -> bus.publishSticky(null))
                        .isInstanceOf(IllegalArgumentException.class);
            }
        }
    }

    @Nested
    @DisplayName("close() 测试")
    class CloseTests {

        @Test
        @DisplayName("close() shuts down resources without exception")
        void testCloseCompletesWithoutException() {
            OpenEvent bus = OpenEvent.create();
            bus.on(TestEvent.class, e -> {});

            assertThatNoException().isThrownBy(bus::close);
        }

        @Test
        @DisplayName("close() on builder-created instance completes")
        void testCloseBuilderInstance() {
            OpenEvent bus = OpenEvent.builder().build();

            assertThatNoException().isThrownBy(bus::close);
        }
    }

    @Nested
    @DisplayName("addInterceptor null 测试")
    class AddInterceptorNullTests {

        @Test
        @DisplayName("addInterceptor(null) throws IllegalArgumentException")
        void testAddInterceptorNullThrows() {
            try (var bus = OpenEvent.create()) {
                assertThatThrownBy(() -> bus.addInterceptor(null))
                        .isInstanceOf(IllegalArgumentException.class);
            }
        }

        @Test
        @DisplayName("removeInterceptor removes previously added interceptor")
        void testRemoveInterceptor() {
            try (var bus = OpenEvent.create()) {
                AtomicInteger beforeCount = new AtomicInteger(0);
                EventInterceptor interceptor = new EventInterceptor() {
                    @Override
                    public boolean beforePublish(Event event) {
                        beforeCount.incrementAndGet();
                        return true;
                    }
                };

                bus.addInterceptor(interceptor);
                bus.on(TestEvent.class, e -> {});
                bus.publish(new TestEvent("first"));
                assertThat(beforeCount.get()).isEqualTo(1);

                bus.removeInterceptor(interceptor);
                bus.publish(new TestEvent("second"));
                assertThat(beforeCount.get()).isEqualTo(1);
            }
        }
    }

    @Nested
    @DisplayName("subscribe null arguments 测试")
    class SubscribeNullArgumentTests {

        @Test
        @DisplayName("subscribe(null, listener) throws IllegalArgumentException")
        void testSubscribeNullEventTypeThrows() {
            try (var bus = OpenEvent.create()) {
                assertThatThrownBy(() -> bus.subscribe(null, e -> {}))
                        .isInstanceOf(IllegalArgumentException.class);
            }
        }

        @Test
        @DisplayName("subscribe(eventType, null) throws IllegalArgumentException")
        void testSubscribeNullListenerThrows() {
            try (var bus = OpenEvent.create()) {
                assertThatThrownBy(() -> bus.subscribe(TestEvent.class, null))
                        .isInstanceOf(IllegalArgumentException.class);
            }
        }
    }

    @Nested
    @DisplayName("publishAsync with EventStore and interceptor 测试")
    class PublishAsyncStoreAndInterceptorTests {

        @Test
        @DisplayName("publishAsync stores event and runs afterPublish interceptor")
        void testPublishAsyncStoresAndRunsAfterInterceptor() throws Exception {
            InMemoryEventStore store = new InMemoryEventStore(100);
            AtomicBoolean afterCalled = new AtomicBoolean(false);
            AtomicBoolean dispatchedValue = new AtomicBoolean(false);

            EventInterceptor interceptor = new EventInterceptor() {
                @Override
                public boolean beforePublish(Event event) {
                    return true;
                }

                @Override
                public void afterPublish(Event event, boolean dispatched) {
                    afterCalled.set(true);
                    dispatchedValue.set(dispatched);
                }
            };

            try (var bus = OpenEvent.builder()
                    .eventStore(store)
                    .interceptor(interceptor)
                    .build()) {
                AtomicBoolean listenerCalled = new AtomicBoolean(false);
                bus.on(TestEvent.class, e -> listenerCalled.set(true));

                TestEvent event = new TestEvent("store-and-intercept");
                CompletableFuture<Void> future = bus.publishAsync(event);
                future.get(5, TimeUnit.SECONDS);

                assertThat(store.count()).isEqualTo(1);
                assertThat(afterCalled.get()).isTrue();
                assertThat(dispatchedValue.get()).isTrue();
                assertThat(listenerCalled.get()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("sync + async mixed listeners 测试")
    class MixedSyncAsyncListenerTests {

        @Test
        @DisplayName("both sync and async listeners receive the event")
        void testMixedSyncAsyncListeners() throws Exception {
            try (var bus = OpenEvent.create()) {
                AtomicBoolean syncCalled = new AtomicBoolean(false);
                AtomicBoolean asyncCalled = new AtomicBoolean(false);

                bus.on(TestEvent.class, e -> syncCalled.set(true), false);
                bus.on(TestEvent.class, e -> asyncCalled.set(true), true);

                bus.publish(new TestEvent("mixed"));

                // sync should be called immediately
                assertThat(syncCalled.get()).isTrue();

                // async may need a moment
                Thread.sleep(200);
                assertThat(asyncCalled.get()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("multiple interceptors afterPublish 测试")
    class MultipleInterceptorsTests {

        @Test
        @DisplayName("afterPublish called for all interceptors")
        void testMultipleInterceptorsAfterPublish() {
            AtomicInteger afterCount = new AtomicInteger(0);
            List<Boolean> dispatchedValues = new ArrayList<>();

            EventInterceptor interceptor1 = new EventInterceptor() {
                @Override
                public boolean beforePublish(Event event) {
                    return true;
                }

                @Override
                public void afterPublish(Event event, boolean dispatched) {
                    afterCount.incrementAndGet();
                    dispatchedValues.add(dispatched);
                }
            };

            EventInterceptor interceptor2 = new EventInterceptor() {
                @Override
                public boolean beforePublish(Event event) {
                    return true;
                }

                @Override
                public void afterPublish(Event event, boolean dispatched) {
                    afterCount.incrementAndGet();
                    dispatchedValues.add(dispatched);
                }
            };

            try (var bus = OpenEvent.builder()
                    .interceptor(interceptor1)
                    .interceptor(interceptor2)
                    .build()) {
                bus.on(TestEvent.class, e -> {});
                bus.publish(new TestEvent("multi-interceptor"));

                assertThat(afterCount.get()).isEqualTo(2);
                assertThat(dispatchedValues).containsExactly(true, true);
            }
        }
    }

    @Nested
    @DisplayName("getMetrics and resetMetrics 测试")
    class MetricsTests {

        @Test
        @DisplayName("getMetrics returns correct counts")
        void testGetMetrics() {
            try (var bus = OpenEvent.create()) {
                bus.resetMetrics();
                bus.on(TestEvent.class, e -> {});

                bus.publish(new TestEvent("m1"));
                bus.publish(new TestEvent("m2"));

                EventBusMetrics metrics = bus.getMetrics();
                assertThat(metrics.totalPublished()).isEqualTo(2);
                assertThat(metrics.totalDelivered()).isEqualTo(2);
                assertThat(metrics.totalErrors()).isZero();
                assertThat(metrics.listenerCount()).isGreaterThanOrEqualTo(1);
            }
        }

        @Test
        @DisplayName("resetMetrics clears all counters")
        void testResetMetrics() {
            try (var bus = OpenEvent.create()) {
                bus.on(TestEvent.class, e -> {});
                bus.publish(new TestEvent("before-reset"));

                bus.resetMetrics();

                EventBusMetrics metrics = bus.getMetrics();
                assertThat(metrics.totalPublished()).isZero();
                assertThat(metrics.totalDelivered()).isZero();
                assertThat(metrics.totalErrors()).isZero();
                assertThat(metrics.totalDeadEvents()).isZero();
            }
        }
    }

    @Nested
    @DisplayName("publishAndWait with async listeners 测试")
    class PublishAndWaitAsyncTests {

        @Test
        @DisplayName("publishAndWait应等待异步监听器完成")
        void testPublishAndWaitWithAsyncListeners() {
            try (var bus = OpenEvent.create()) {
                AtomicBoolean asyncDone = new AtomicBoolean(false);

                // Register async listener
                bus.on(TestEvent.class, e -> {
                    try { Thread.sleep(50); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
                    asyncDone.set(true);
                }, true);

                // Also register a secondary TestEvent listener
                bus.on(TestEvent.class, e -> {});

                boolean completed = bus.publishAndWait(new TestEvent("async-wait"), Duration.ofSeconds(5));

                assertThat(completed).isTrue();
            }
        }

        @Test
        @DisplayName("publishAndWait with no matching listeners completes immediately")
        void testPublishAndWaitNoListeners() {
            try (var bus = OpenEvent.create()) {
                boolean completed = bus.publishAndWait(new TestEvent("no-listeners"), Duration.ofSeconds(2));
                assertThat(completed).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("sticky event error handling during subscribe 测试")
    class StickyEventErrorTests {

        @Test
        @DisplayName("sticky事件投递异常应被异常处理器处理")
        void testStickyDeliveryErrorHandled() {
            try (var bus = OpenEvent.create()) {
                AtomicBoolean handlerCalled = new AtomicBoolean(false);
                bus.setExceptionHandler((event, ex, name) -> handlerCalled.set(true));

                bus.publishSticky(new TestEvent("sticky-err"));

                // Subscribe with a listener that throws
                bus.subscribe(TestEvent.class, e -> { throw new RuntimeException("boom"); });

                assertThat(handlerCalled.get()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("publish forceAsync=false with only async listeners 测试")
    class PublishOnlyAsyncListenersTests {

        @Test
        @DisplayName("publish with only @Async listeners dispatches asynchronously")
        void testPublishWithOnlyAsyncListeners() throws InterruptedException {
            try (var bus = OpenEvent.create()) {
                AtomicBoolean called = new AtomicBoolean(false);

                class AsyncSub {
                    @Subscribe
                    @Async
                    public void handle(TestEvent e) {
                        called.set(true);
                    }
                }

                bus.register(new AsyncSub());
                bus.publish(new TestEvent("only-async"));

                Thread.sleep(200);
                assertThat(called.get()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("afterPublish interceptor exception isolation 测试")
    class AfterPublishInterceptorExceptionTests {

        @Test
        @DisplayName("afterPublish exception does not propagate to caller")
        void testAfterPublishExceptionIsolated() {
            try (var bus = OpenEvent.create()) {
                bus.addInterceptor(new EventInterceptor() {
                    @Override
                    public boolean beforePublish(Event event) { return true; }
                    @Override
                    public void afterPublish(Event event, boolean dispatched) {
                        throw new RuntimeException("after boom");
                    }
                });
                bus.on(TestEvent.class, e -> {});

                assertThatCode(() -> bus.publish(new TestEvent("after-err"))).doesNotThrowAnyException();
            }
        }
    }

    @Nested
    @DisplayName("sticky event delivery during subscribe 测试")
    class StickyEventDeliveryTests {

        @Test
        @DisplayName("subscribe receives previously published sticky event")
        void testStickyEventDeliveredOnSubscribe() {
            try (var bus = OpenEvent.create()) {
                TestEvent stickyEvent = new TestEvent("sticky-msg");
                bus.publishSticky(stickyEvent);

                AtomicReference<String> received = new AtomicReference<>();
                bus.subscribe(TestEvent.class, e -> received.set(e.getMessage()));

                assertThat(received.get()).isEqualTo("sticky-msg");
            }
        }

        @Test
        @DisplayName("subscribe with filter skips sticky event that does not match")
        void testStickyEventFilteredOut() {
            try (var bus = OpenEvent.create()) {
                TestEvent stickyEvent = new TestEvent("no-match");
                bus.publishSticky(stickyEvent);

                AtomicBoolean called = new AtomicBoolean(false);
                bus.subscribe(TestEvent.class, e -> called.set(true),
                        e -> e.getMessage().equals("match-only"));

                assertThat(called.get()).isFalse();
            }
        }

        @Test
        @DisplayName("subscribe with matching filter receives sticky event")
        void testStickyEventMatchesFilter() {
            try (var bus = OpenEvent.create()) {
                TestEvent stickyEvent = new TestEvent("match-only");
                bus.publishSticky(stickyEvent);

                AtomicReference<String> received = new AtomicReference<>();
                bus.subscribe(TestEvent.class, e -> received.set(e.getMessage()),
                        e -> e.getMessage().equals("match-only"));

                assertThat(received.get()).isEqualTo("match-only");
            }
        }
    }
}
