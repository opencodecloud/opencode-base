package cloud.opencode.base.email.listener;

import cloud.opencode.base.email.ReceivedEmail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * EmailListener 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("EmailListener 测试")
class EmailListenerTest {

    @Nested
    @DisplayName("onNewEmail() 工厂方法测试")
    class OnNewEmailFactoryTests {

        @Test
        @DisplayName("创建简单监听器")
        void testCreateSimpleListener() {
            AtomicBoolean called = new AtomicBoolean(false);
            EmailListener listener = EmailListener.onNewEmail(email -> called.set(true));

            assertThat(listener).isNotNull();
        }

        @Test
        @DisplayName("监听器接收邮件")
        void testListenerReceivesEmail() {
            AtomicReference<ReceivedEmail> received = new AtomicReference<>();
            ReceivedEmail email = createTestEmail();

            EmailListener listener = EmailListener.onNewEmail(received::set);
            listener.onNewEmail(email);

            assertThat(received.get()).isEqualTo(email);
        }
    }

    @Nested
    @DisplayName("of() 工厂方法测试")
    class OfFactoryTests {

        @Test
        @DisplayName("创建双处理器监听器")
        void testCreateDualHandlerListener() {
            AtomicBoolean emailHandled = new AtomicBoolean(false);
            AtomicBoolean errorHandled = new AtomicBoolean(false);

            EmailListener listener = EmailListener.of(
                    email -> emailHandled.set(true),
                    error -> errorHandled.set(true)
            );

            assertThat(listener).isNotNull();
        }

        @Test
        @DisplayName("处理新邮件")
        void testHandlesNewEmail() {
            AtomicReference<ReceivedEmail> received = new AtomicReference<>();

            EmailListener listener = EmailListener.of(
                    received::set,
                    error -> {}
            );

            ReceivedEmail email = createTestEmail();
            listener.onNewEmail(email);

            assertThat(received.get()).isEqualTo(email);
        }

        @Test
        @DisplayName("处理错误")
        void testHandlesError() {
            AtomicReference<Throwable> receivedError = new AtomicReference<>();

            EmailListener listener = EmailListener.of(
                    email -> {},
                    receivedError::set
            );

            RuntimeException error = new RuntimeException("Test error");
            listener.onError(error);

            assertThat(receivedError.get()).isEqualTo(error);
        }
    }

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("onEmailDeleted默认不抛异常")
        void testOnEmailDeletedDefault() {
            EmailListener listener = email -> {};

            assertThatNoException().isThrownBy(() -> listener.onEmailDeleted("msg-id"));
        }

        @Test
        @DisplayName("onFlagsChanged默认不抛异常")
        void testOnFlagsChangedDefault() {
            EmailListener listener = email -> {};

            assertThatNoException().isThrownBy(() ->
                    listener.onFlagsChanged("msg-id", "SEEN", true));
        }

        @Test
        @DisplayName("onError默认不抛异常")
        void testOnErrorDefault() {
            EmailListener listener = email -> {};

            assertThatNoException().isThrownBy(() ->
                    listener.onError(new RuntimeException("Test")));
        }

        @Test
        @DisplayName("onMonitoringStarted默认不抛异常")
        void testOnMonitoringStartedDefault() {
            EmailListener listener = email -> {};

            assertThatNoException().isThrownBy(() ->
                    listener.onMonitoringStarted("INBOX"));
        }

        @Test
        @DisplayName("onMonitoringStopped默认不抛异常")
        void testOnMonitoringStoppedDefault() {
            EmailListener listener = email -> {};

            assertThatNoException().isThrownBy(() ->
                    listener.onMonitoringStopped("INBOX"));
        }

        @Test
        @DisplayName("onReconnecting默认不抛异常")
        void testOnReconnectingDefault() {
            EmailListener listener = email -> {};

            assertThatNoException().isThrownBy(() ->
                    listener.onReconnecting(1));
        }

        @Test
        @DisplayName("onReconnected默认不抛异常")
        void testOnReconnectedDefault() {
            EmailListener listener = email -> {};

            assertThatNoException().isThrownBy(listener::onReconnected);
        }
    }

    @Nested
    @DisplayName("函数式接口测试")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("可用作Lambda表达式")
        void testUsableAsLambda() {
            EmailListener listener = email -> System.out.println("Email received");

            assertThat(listener).isNotNull();
        }

        @Test
        @DisplayName("可用作方法引用")
        void testUsableAsMethodReference() {
            EmailListenerTest handler = new EmailListenerTest();
            EmailListener listener = handler::handleEmail;

            assertThat(listener).isNotNull();
        }
    }

    // Helper method for method reference test
    void handleEmail(ReceivedEmail email) {
        // Handle email
    }

    private ReceivedEmail createTestEmail() {
        return ReceivedEmail.builder()
                .from("sender@example.com")
                .to(java.util.List.of("recipient@example.com"))
                .subject("Test Email")
                .textContent("Test content")
                .messageId("<test-id>")
                .build();
    }
}
