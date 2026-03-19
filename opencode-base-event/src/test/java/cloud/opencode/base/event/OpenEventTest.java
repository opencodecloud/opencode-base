package cloud.opencode.base.event;

import cloud.opencode.base.event.annotation.Async;
import cloud.opencode.base.event.annotation.Priority;
import cloud.opencode.base.event.annotation.Subscribe;
import cloud.opencode.base.event.dispatcher.SyncDispatcher;
import cloud.opencode.base.event.exception.EventException;
import cloud.opencode.base.event.handler.EventExceptionHandler;
import cloud.opencode.base.event.store.InMemoryEventStore;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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

            eventBus.on(WaitableEvent.class, event -> {
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
}
