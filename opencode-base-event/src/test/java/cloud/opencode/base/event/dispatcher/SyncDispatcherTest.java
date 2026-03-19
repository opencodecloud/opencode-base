package cloud.opencode.base.event.dispatcher;

import cloud.opencode.base.event.Event;
import cloud.opencode.base.event.exception.EventListenerException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;

/**
 * SyncDispatcher 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("SyncDispatcher 测试")
class SyncDispatcherTest {

    static class TestEvent extends Event {
        public TestEvent() {
            super();
        }
    }

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("无参构造")
        void testDefaultConstructor() {
            SyncDispatcher dispatcher = new SyncDispatcher();

            assertThat(dispatcher).isNotNull();
            assertThat(dispatcher.isAsync()).isFalse();
        }

        @Test
        @DisplayName("带stopOnError构造")
        void testConstructorWithStopOnError() {
            SyncDispatcher dispatcher = new SyncDispatcher(true);

            assertThat(dispatcher).isNotNull();
        }
    }

    @Nested
    @DisplayName("dispatch() 测试")
    class DispatchTests {

        @Test
        @DisplayName("按顺序调用所有监听器")
        void testDispatchInOrder() {
            SyncDispatcher dispatcher = new SyncDispatcher();
            TestEvent event = new TestEvent();
            List<Integer> order = new ArrayList<>();

            List<Consumer<Event>> listeners = List.of(
                    e -> order.add(1),
                    e -> order.add(2),
                    e -> order.add(3)
            );

            dispatcher.dispatch(event, listeners);

            assertThat(order).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("null事件不处理")
        void testDispatchNullEvent() {
            SyncDispatcher dispatcher = new SyncDispatcher();
            AtomicInteger count = new AtomicInteger(0);

            List<Consumer<Event>> listeners = List.of(e -> count.incrementAndGet());

            dispatcher.dispatch(null, listeners);

            assertThat(count.get()).isZero();
        }

        @Test
        @DisplayName("null监听器列表不处理")
        void testDispatchNullListeners() {
            SyncDispatcher dispatcher = new SyncDispatcher();
            TestEvent event = new TestEvent();

            assertThatNoException().isThrownBy(() ->
                    dispatcher.dispatch(event, null));
        }

        @Test
        @DisplayName("空监听器列表不处理")
        void testDispatchEmptyListeners() {
            SyncDispatcher dispatcher = new SyncDispatcher();
            TestEvent event = new TestEvent();

            assertThatNoException().isThrownBy(() ->
                    dispatcher.dispatch(event, List.of()));
        }

        @Test
        @DisplayName("取消的事件停止分发")
        void testCancelledEventStopsDispatch() {
            SyncDispatcher dispatcher = new SyncDispatcher();
            TestEvent event = new TestEvent();
            List<Integer> order = new ArrayList<>();

            List<Consumer<Event>> listeners = List.of(
                    e -> {
                        order.add(1);
                        e.cancel();
                    },
                    e -> order.add(2),
                    e -> order.add(3)
            );

            dispatcher.dispatch(event, listeners);

            assertThat(order).containsExactly(1);
        }
    }

    @Nested
    @DisplayName("错误处理测试")
    class ErrorHandlingTests {

        @Test
        @DisplayName("stopOnError=false时继续执行")
        void testContinueOnError() {
            SyncDispatcher dispatcher = new SyncDispatcher(false);
            TestEvent event = new TestEvent();
            List<Integer> order = new ArrayList<>();

            List<Consumer<Event>> listeners = List.of(
                    e -> order.add(1),
                    e -> { throw new RuntimeException("Error"); },
                    e -> order.add(3)
            );

            dispatcher.dispatch(event, listeners);

            assertThat(order).containsExactly(1, 3);
        }

        @Test
        @DisplayName("stopOnError=true时抛出异常")
        void testStopOnError() {
            SyncDispatcher dispatcher = new SyncDispatcher(true);
            TestEvent event = new TestEvent();

            List<Consumer<Event>> listeners = List.of(
                    e -> { throw new RuntimeException("Error"); },
                    e -> {}
            );

            assertThatThrownBy(() -> dispatcher.dispatch(event, listeners))
                    .isInstanceOf(EventListenerException.class)
                    .hasMessageContaining("Listener invocation failed");
        }
    }

    @Nested
    @DisplayName("isAsync() 测试")
    class IsAsyncTests {

        @Test
        @DisplayName("总是返回false")
        void testIsAsyncReturnsFalse() {
            SyncDispatcher dispatcher = new SyncDispatcher();

            assertThat(dispatcher.isAsync()).isFalse();
        }
    }
}
