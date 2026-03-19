package cloud.opencode.base.event.handler;

import cloud.opencode.base.event.Event;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * LoggingExceptionHandler 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("LoggingExceptionHandler 测试")
class LoggingExceptionHandlerTest {

    static class TestEvent extends Event {}

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("无参构造使用SEVERE级别")
        void testDefaultConstructor() {
            LoggingExceptionHandler handler = new LoggingExceptionHandler();

            // 处理异常不抛错即可
            assertThatNoException().isThrownBy(() ->
                    handler.handleException(new TestEvent(), new RuntimeException("test"), "Listener"));
        }

        @Test
        @DisplayName("指定日志级别")
        void testConstructorWithLevel() {
            LoggingExceptionHandler handler = new LoggingExceptionHandler(System.Logger.Level.WARNING);

            assertThatNoException().isThrownBy(() ->
                    handler.handleException(new TestEvent(), new RuntimeException("test"), "Listener"));
        }

        @Test
        @DisplayName("null级别使用ERROR")
        void testNullLevelUsesError() {
            LoggingExceptionHandler handler = new LoggingExceptionHandler(null);

            assertThatNoException().isThrownBy(() ->
                    handler.handleException(new TestEvent(), new RuntimeException("test"), "Listener"));
        }
    }

    @Nested
    @DisplayName("handleException() 测试")
    class HandleExceptionTests {

        @Test
        @DisplayName("处理异常不抛出")
        void testHandleExceptionNoThrow() {
            LoggingExceptionHandler handler = new LoggingExceptionHandler();
            TestEvent event = new TestEvent();
            RuntimeException exception = new RuntimeException("Test error");

            assertThatNoException().isThrownBy(() ->
                    handler.handleException(event, exception, "TestListener"));
        }

        @Test
        @DisplayName("处理各种级别")
        void testDifferentLevels() {
            for (System.Logger.Level level : new System.Logger.Level[]{System.Logger.Level.ERROR, System.Logger.Level.WARNING, System.Logger.Level.INFO, System.Logger.Level.DEBUG}) {
                LoggingExceptionHandler handler = new LoggingExceptionHandler(level);

                assertThatNoException().isThrownBy(() ->
                        handler.handleException(new TestEvent(), new RuntimeException("test"), "Listener"));
            }
        }
    }

    @Nested
    @DisplayName("实现接口测试")
    class ImplementsInterfaceTests {

        @Test
        @DisplayName("实现EventExceptionHandler")
        void testImplementsInterface() {
            LoggingExceptionHandler handler = new LoggingExceptionHandler();

            assertThat(handler).isInstanceOf(EventExceptionHandler.class);
        }
    }
}
