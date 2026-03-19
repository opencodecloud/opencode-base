package cloud.opencode.base.event.dispatcher;

import cloud.opencode.base.event.Event;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;

/**
 * EventDispatcher 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("EventDispatcher 测试")
class EventDispatcherTest {

    static class TestEvent extends Event {
        public TestEvent() {
            super();
        }
    }

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("isAsync默认返回false")
        void testIsAsyncDefaultFalse() {
            EventDispatcher dispatcher = (event, listeners) -> {};

            assertThat(dispatcher.isAsync()).isFalse();
        }

        @Test
        @DisplayName("shutdown默认不抛异常")
        void testShutdownDefault() {
            EventDispatcher dispatcher = (event, listeners) -> {};

            assertThatNoException().isThrownBy(dispatcher::shutdown);
        }
    }

    @Nested
    @DisplayName("函数式接口测试")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("可用作Lambda")
        void testUsableAsLambda() {
            EventDispatcher dispatcher = (event, listeners) -> {
                for (Consumer<Event> listener : listeners) {
                    listener.accept(event);
                }
            };

            assertThat(dispatcher).isNotNull();
        }
    }
}
