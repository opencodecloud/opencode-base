package cloud.opencode.base.event.handler;

import cloud.opencode.base.event.Event;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * RetryExceptionHandler 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("RetryExceptionHandler 测试")
class RetryExceptionHandlerTest {

    static class TestEvent extends Event {}

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("无参构造使用默认值")
        void testDefaultConstructor() {
            RetryExceptionHandler handler = new RetryExceptionHandler();

            assertThat(handler.getMaxRetries()).isEqualTo(3);
            assertThat(handler.getDelay()).isEqualTo(Duration.ofSeconds(1));
        }

        @Test
        @DisplayName("自定义重试次数和延迟")
        void testCustomConstructor() {
            RetryExceptionHandler handler = new RetryExceptionHandler(5, Duration.ofMillis(100));

            assertThat(handler.getMaxRetries()).isEqualTo(5);
            assertThat(handler.getDelay()).isEqualTo(Duration.ofMillis(100));
        }
    }

    @Nested
    @DisplayName("setRetryAction() 测试")
    class SetRetryActionTests {

        @Test
        @DisplayName("设置重试动作")
        void testSetRetryAction() {
            RetryExceptionHandler handler = new RetryExceptionHandler(1, Duration.ofMillis(10));
            AtomicInteger retryCount = new AtomicInteger(0);

            handler.setRetryAction(event -> retryCount.incrementAndGet());
            handler.handleException(new TestEvent(), new RuntimeException("test"), "Listener");

            assertThat(retryCount.get()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("handleException() 测试")
    class HandleExceptionTests {

        @Test
        @DisplayName("无重试动作时加入死信队列")
        void testNoRetryActionAddsToDeadLetter() {
            RetryExceptionHandler handler = new RetryExceptionHandler();
            TestEvent event = new TestEvent();

            handler.handleException(event, new RuntimeException("test"), "Listener");

            assertThat(handler.getDeadLetterQueueSize()).isEqualTo(1);
        }

        @Test
        @DisplayName("重试成功不加入死信队列")
        void testRetrySuccessNoDeadLetter() {
            RetryExceptionHandler handler = new RetryExceptionHandler(3, Duration.ofMillis(10));

            handler.setRetryAction(event -> {
                // 成功处理
            });

            handler.handleException(new TestEvent(), new RuntimeException("test"), "Listener");

            assertThat(handler.getDeadLetterQueueSize()).isZero();
        }

        @Test
        @DisplayName("重试失败加入死信队列")
        void testRetryFailureAddsToDeadLetter() {
            RetryExceptionHandler handler = new RetryExceptionHandler(2, Duration.ofMillis(10));

            handler.setRetryAction(event -> {
                throw new RuntimeException("Always fails");
            });

            handler.handleException(new TestEvent(), new RuntimeException("test"), "Listener");

            assertThat(handler.getDeadLetterQueueSize()).isEqualTo(1);
        }

        @Test
        @DisplayName("重试指定次数")
        void testRetryCount() {
            RetryExceptionHandler handler = new RetryExceptionHandler(3, Duration.ofMillis(10));
            AtomicInteger attemptCount = new AtomicInteger(0);

            handler.setRetryAction(event -> {
                attemptCount.incrementAndGet();
                throw new RuntimeException("Always fails");
            });

            handler.handleException(new TestEvent(), new RuntimeException("test"), "Listener");

            assertThat(attemptCount.get()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("死信队列测试")
    class DeadLetterQueueTests {

        @Test
        @DisplayName("processDeadLetters处理所有失败事件")
        void testProcessDeadLetters() {
            RetryExceptionHandler handler = new RetryExceptionHandler();
            AtomicInteger processedCount = new AtomicInteger(0);

            // 添加几个失败事件
            handler.handleException(new TestEvent(), new RuntimeException("test1"), "L1");
            handler.handleException(new TestEvent(), new RuntimeException("test2"), "L2");

            handler.processDeadLetters(failed -> processedCount.incrementAndGet());

            assertThat(processedCount.get()).isEqualTo(2);
            assertThat(handler.getDeadLetterQueueSize()).isZero();
        }

        @Test
        @DisplayName("clearDeadLetterQueue清空队列")
        void testClearDeadLetterQueue() {
            RetryExceptionHandler handler = new RetryExceptionHandler();

            handler.handleException(new TestEvent(), new RuntimeException("test"), "Listener");
            assertThat(handler.getDeadLetterQueueSize()).isEqualTo(1);

            handler.clearDeadLetterQueue();
            assertThat(handler.getDeadLetterQueueSize()).isZero();
        }

        @Test
        @DisplayName("getDeadLetterQueueSize返回正确大小")
        void testGetDeadLetterQueueSize() {
            RetryExceptionHandler handler = new RetryExceptionHandler();

            assertThat(handler.getDeadLetterQueueSize()).isZero();

            handler.handleException(new TestEvent(), new RuntimeException("test1"), "L1");
            assertThat(handler.getDeadLetterQueueSize()).isEqualTo(1);

            handler.handleException(new TestEvent(), new RuntimeException("test2"), "L2");
            assertThat(handler.getDeadLetterQueueSize()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("FailedEvent记录测试")
    class FailedEventTests {

        @Test
        @DisplayName("FailedEvent包含所有信息")
        void testFailedEventContainsAllInfo() {
            RetryExceptionHandler handler = new RetryExceptionHandler();
            TestEvent event = new TestEvent();
            RuntimeException exception = new RuntimeException("test");

            handler.handleException(event, exception, "TestListener");

            AtomicReference<RetryExceptionHandler.FailedEvent> capturedFailed = new AtomicReference<>();
            handler.processDeadLetters(capturedFailed::set);

            RetryExceptionHandler.FailedEvent failed = capturedFailed.get();
            assertThat(failed).isNotNull();
            assertThat(failed.event()).isEqualTo(event);
            assertThat(failed.exception()).isEqualTo(exception);
            assertThat(failed.listenerName()).isEqualTo("TestListener");
            assertThat(failed.attempts()).isZero(); // 无重试动作，attempts为0
        }
    }

    @Nested
    @DisplayName("getter方法测试")
    class GetterTests {

        @Test
        @DisplayName("getMaxRetries返回重试次数")
        void testGetMaxRetries() {
            RetryExceptionHandler handler = new RetryExceptionHandler(5, Duration.ofSeconds(1));

            assertThat(handler.getMaxRetries()).isEqualTo(5);
        }

        @Test
        @DisplayName("getDelay返回延迟")
        void testGetDelay() {
            RetryExceptionHandler handler = new RetryExceptionHandler(3, Duration.ofMillis(500));

            assertThat(handler.getDelay()).isEqualTo(Duration.ofMillis(500));
        }
    }
}
