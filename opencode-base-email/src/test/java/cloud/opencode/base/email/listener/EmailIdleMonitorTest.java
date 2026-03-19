package cloud.opencode.base.email.listener;

import cloud.opencode.base.email.EmailReceiveConfig;
import cloud.opencode.base.email.exception.EmailReceiveException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;

/**
 * EmailIdleMonitor 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("EmailIdleMonitor 测试")
class EmailIdleMonitorTest {

    @Nested
    @DisplayName("构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("使用IMAP配置构建")
        void testBuildWithImapConfig() {
            EmailReceiveConfig config = createImapConfig();

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(config)
                    .build();

            assertThat(monitor).isNotNull();
            assertThat(monitor.isRunning()).isFalse();
        }

        @Test
        @DisplayName("使用POP3配置构建失败")
        void testBuildWithPop3ConfigFails() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("pop.example.com")
                    .username("user@example.com")
                    .password("password")
                    .pop3()
                    .build();

            assertThatThrownBy(() -> EmailIdleMonitor.builder()
                    .config(config)
                    .build())
                    .isInstanceOf(EmailReceiveException.class)
                    .hasMessageContaining("IMAP protocol");
        }

        @Test
        @DisplayName("无配置构建失败")
        void testBuildWithoutConfigFails() {
            assertThatThrownBy(() -> EmailIdleMonitor.builder().build())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Configuration is required");
        }

        @Test
        @DisplayName("设置文件夹")
        void testSetFolder() {
            EmailReceiveConfig config = createImapConfig();

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(config)
                    .folder("INBOX")
                    .build();

            assertThat(monitor).isNotNull();
        }

        @Test
        @DisplayName("设置IDLE超时")
        void testSetIdleTimeout() {
            EmailReceiveConfig config = createImapConfig();

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(config)
                    .idleTimeout(Duration.ofMinutes(20))
                    .build();

            assertThat(monitor).isNotNull();
        }

        @Test
        @DisplayName("设置最大重连次数")
        void testSetMaxReconnectAttempts() {
            EmailReceiveConfig config = createImapConfig();

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(config)
                    .maxReconnectAttempts(10)
                    .build();

            assertThat(monitor).isNotNull();
        }

        @Test
        @DisplayName("设置重连延迟")
        void testSetReconnectDelay() {
            EmailReceiveConfig config = createImapConfig();

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(config)
                    .reconnectDelay(Duration.ofSeconds(10))
                    .build();

            assertThat(monitor).isNotNull();
        }

        @Test
        @DisplayName("添加监听器")
        void testAddListener() {
            EmailReceiveConfig config = createImapConfig();

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(config)
                    .listener(email -> {})
                    .build();

            assertThat(monitor).isNotNull();
        }

        @Test
        @DisplayName("使用onNewEmail添加处理器")
        void testOnNewEmailHandler() {
            EmailReceiveConfig config = createImapConfig();

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(config)
                    .onNewEmail(email -> System.out.println(email.subject()))
                    .build();

            assertThat(monitor).isNotNull();
        }

        @Test
        @DisplayName("null监听器被忽略")
        void testNullListenerIgnored() {
            EmailReceiveConfig config = createImapConfig();

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(config)
                    .listener(null)
                    .build();

            assertThat(monitor).isNotNull();
        }
    }

    @Nested
    @DisplayName("isRunning() 测试")
    class IsRunningTests {

        @Test
        @DisplayName("未启动时返回false")
        void testNotRunningByDefault() {
            EmailReceiveConfig config = createImapConfig();

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(config)
                    .build();

            assertThat(monitor.isRunning()).isFalse();
        }
    }

    @Nested
    @DisplayName("addListener() 测试")
    class AddListenerTests {

        @Test
        @DisplayName("添加监听器成功")
        void testAddListener() {
            EmailReceiveConfig config = createImapConfig();

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(config)
                    .build();

            assertThatNoException().isThrownBy(() ->
                    monitor.addListener(email -> {}));
        }

        @Test
        @DisplayName("添加null监听器不抛异常")
        void testAddNullListener() {
            EmailReceiveConfig config = createImapConfig();

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(config)
                    .build();

            assertThatNoException().isThrownBy(() ->
                    monitor.addListener(null));
        }
    }

    @Nested
    @DisplayName("removeListener() 测试")
    class RemoveListenerTests {

        @Test
        @DisplayName("移除监听器成功")
        void testRemoveListener() {
            EmailReceiveConfig config = createImapConfig();
            EmailListener listener = email -> {};

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(config)
                    .listener(listener)
                    .build();

            assertThatNoException().isThrownBy(() ->
                    monitor.removeListener(listener));
        }

        @Test
        @DisplayName("移除不存在的监听器不抛异常")
        void testRemoveNonExistentListener() {
            EmailReceiveConfig config = createImapConfig();

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(config)
                    .build();

            assertThatNoException().isThrownBy(() ->
                    monitor.removeListener(email -> {}));
        }
    }

    @Nested
    @DisplayName("stop() 测试")
    class StopTests {

        @Test
        @DisplayName("停止未运行的监控器不抛异常")
        void testStopWhenNotRunning() {
            EmailReceiveConfig config = createImapConfig();

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(config)
                    .build();

            assertThatNoException().isThrownBy(monitor::stop);
        }
    }

    @Nested
    @DisplayName("close() 测试")
    class CloseTests {

        @Test
        @DisplayName("关闭监控器不抛异常")
        void testClose() {
            EmailReceiveConfig config = createImapConfig();

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(config)
                    .build();

            assertThatNoException().isThrownBy(monitor::close);
        }

        @Test
        @DisplayName("可以多次关闭")
        void testMultipleClose() {
            EmailReceiveConfig config = createImapConfig();

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(config)
                    .build();

            assertThatNoException().isThrownBy(() -> {
                monitor.close();
                monitor.close();
            });
        }
    }

    @Nested
    @DisplayName("AutoCloseable测试")
    class AutoCloseableTests {

        @Test
        @DisplayName("可用于try-with-resources")
        void testTryWithResources() {
            EmailReceiveConfig config = createImapConfig();

            assertThatNoException().isThrownBy(() -> {
                try (EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                        .config(config)
                        .build()) {
                    assertThat(monitor.isRunning()).isFalse();
                }
            });
        }
    }

    private EmailReceiveConfig createImapConfig() {
        return EmailReceiveConfig.builder()
                .host("imap.example.com")
                .username("user@example.com")
                .password("password")
                .imap()
                .build();
    }
}
