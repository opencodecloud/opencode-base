package cloud.opencode.base.email;

import cloud.opencode.base.email.exception.EmailException;
import cloud.opencode.base.email.exception.EmailReceiveException;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenEmail 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("OpenEmail 测试")
class OpenEmailTest {

    @AfterEach
    void cleanUp() {
        OpenEmail.shutdown();
        OpenEmail.disableRateLimiting();
    }

    @Nested
    @DisplayName("configure() 测试")
    class ConfigureTests {

        @Test
        @DisplayName("基本配置")
        void testBasicConfigure() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .port(587)
                    .username("user@example.com")
                    .password("password")
                    .build();

            OpenEmail.configure(config);

            assertThat(OpenEmail.isConfigured()).isTrue();
            assertThat(OpenEmail.getConfig()).isEqualTo(config);
        }

        @Test
        @DisplayName("快速配置")
        void testQuickConfigure() {
            OpenEmail.configure("smtp.example.com", 587, "user@example.com", "password");

            assertThat(OpenEmail.isConfigured()).isTrue();
            assertThat(OpenEmail.getConfig().host()).isEqualTo("smtp.example.com");
            assertThat(OpenEmail.getConfig().port()).isEqualTo(587);
        }

        @Test
        @DisplayName("配置后getConfig()返回配置")
        void testGetConfig() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.test.com")
                    .port(25)
                    .build();

            OpenEmail.configure(config);

            EmailConfig retrieved = OpenEmail.getConfig();
            assertThat(retrieved).isEqualTo(config);
            assertThat(retrieved.host()).isEqualTo("smtp.test.com");
        }
    }

    @Nested
    @DisplayName("isConfigured() 测试")
    class IsConfiguredTests {

        @Test
        @DisplayName("未配置时返回false")
        void testNotConfigured() {
            assertThat(OpenEmail.isConfigured()).isFalse();
        }

        @Test
        @DisplayName("配置后返回true")
        void testAfterConfigure() {
            OpenEmail.configure(EmailConfig.builder()
                    .host("smtp.example.com")
                    .build());

            assertThat(OpenEmail.isConfigured()).isTrue();
        }

        @Test
        @DisplayName("shutdown后返回false")
        void testAfterShutdown() {
            OpenEmail.configure(EmailConfig.builder()
                    .host("smtp.example.com")
                    .build());
            OpenEmail.shutdown();

            assertThat(OpenEmail.isConfigured()).isFalse();
        }
    }

    @Nested
    @DisplayName("未配置时操作测试")
    class NotConfiguredTests {

        @Test
        @DisplayName("send()未配置时抛出异常")
        void testSendWithoutConfig() {
            Email email = Email.builder()
                    .to("test@example.com")
                    .subject("Test")
                    .text("Content")
                    .build();

            assertThatThrownBy(() -> OpenEmail.send(email))
                    .isInstanceOf(EmailException.class)
                    .hasMessageContaining("not configured");
        }

        @Test
        @DisplayName("sendAsync()未配置时抛出异常")
        void testSendAsyncWithoutConfig() {
            Email email = Email.builder()
                    .to("test@example.com")
                    .subject("Test")
                    .text("Content")
                    .build();

            assertThatThrownBy(() -> OpenEmail.sendAsync(email))
                    .isInstanceOf(EmailException.class)
                    .hasMessageContaining("not configured");
        }

        @Test
        @DisplayName("email()未配置时抛出异常")
        void testEmailBuilderWithoutConfig() {
            assertThatThrownBy(() -> OpenEmail.email())
                    .isInstanceOf(EmailException.class)
                    .hasMessageContaining("not configured");
        }
    }

    @Nested
    @DisplayName("速率限制测试")
    class RateLimitingTests {

        @Test
        @DisplayName("默认未启用速率限制")
        void testRateLimitingDisabledByDefault() {
            assertThat(OpenEmail.isRateLimitingEnabled()).isFalse();
        }

        @Test
        @DisplayName("启用默认速率限制")
        void testEnableRateLimiting() {
            OpenEmail.enableRateLimiting();

            assertThat(OpenEmail.isRateLimitingEnabled()).isTrue();
        }

        @Test
        @DisplayName("启用自定义速率限制")
        void testEnableCustomRateLimiting() {
            OpenEmail.enableRateLimiting(5, 50, 500);

            assertThat(OpenEmail.isRateLimitingEnabled()).isTrue();
        }

        @Test
        @DisplayName("禁用速率限制")
        void testDisableRateLimiting() {
            OpenEmail.enableRateLimiting();
            OpenEmail.disableRateLimiting();

            assertThat(OpenEmail.isRateLimitingEnabled()).isFalse();
        }

        @Test
        @DisplayName("获取全局配额")
        void testGetRateLimitQuota() {
            OpenEmail.enableRateLimiting(10, 100, 1000);

            var quota = OpenEmail.getRateLimitQuota();

            assertThat(quota).isNotNull();
            assertThat(quota.minuteRemaining()).isEqualTo(10);
        }

        @Test
        @DisplayName("未启用时配额为null")
        void testQuotaNullWhenDisabled() {
            assertThat(OpenEmail.getRateLimitQuota()).isNull();
        }
    }

    @Nested
    @DisplayName("接收器配置测试")
    class ReceiverConfigTests {

        @Test
        @DisplayName("默认未配置接收器")
        void testReceiverNotConfiguredByDefault() {
            assertThat(OpenEmail.isReceiverConfigured()).isFalse();
        }

        @Test
        @DisplayName("IMAP配置")
        void testImapConfigure() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .username("user@example.com")
                    .password("password")
                    .imap()
                    .build();

            OpenEmail.configureReceiver(config);

            assertThat(OpenEmail.isReceiverConfigured()).isTrue();
            assertThat(OpenEmail.getReceiveConfig()).isEqualTo(config);
        }

        @Test
        @DisplayName("POP3配置")
        void testPop3Configure() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("pop.example.com")
                    .username("user@example.com")
                    .password("password")
                    .pop3()
                    .build();

            OpenEmail.configureReceiver(config);

            assertThat(OpenEmail.isReceiverConfigured()).isTrue();
        }

        @Test
        @DisplayName("快速接收器配置")
        void testQuickReceiverConfigure() {
            OpenEmail.configureReceiver("imap.example.com", "user@example.com", "password", true);

            assertThat(OpenEmail.isReceiverConfigured()).isTrue();
            assertThat(OpenEmail.getReceiveConfig().isImap()).isTrue();
        }
    }

    @Nested
    @DisplayName("接收器未配置时操作测试")
    class ReceiverNotConfiguredTests {

        @Test
        @DisplayName("receiveUnread()未配置时抛出异常")
        void testReceiveUnreadWithoutConfig() {
            assertThatThrownBy(() -> OpenEmail.receiveUnread())
                    .isInstanceOf(EmailReceiveException.class)
                    .hasMessageContaining("not configured");
        }

        @Test
        @DisplayName("markAsRead()未配置时抛出异常")
        void testMarkAsReadWithoutConfig() {
            assertThatThrownBy(() -> OpenEmail.markAsRead("<123>"))
                    .isInstanceOf(EmailReceiveException.class)
                    .hasMessageContaining("not configured");
        }

        @Test
        @DisplayName("listFolders()未配置时抛出异常")
        void testListFoldersWithoutConfig() {
            assertThatThrownBy(() -> OpenEmail.listFolders())
                    .isInstanceOf(EmailReceiveException.class)
                    .hasMessageContaining("not configured");
        }
    }

    @Nested
    @DisplayName("Builder方法测试")
    class BuilderMethodTests {

        @Test
        @DisplayName("query()返回查询构建器")
        void testQueryBuilder() {
            var builder = OpenEmail.query();

            assertThat(builder).isNotNull();
            assertThat(builder).isInstanceOf(cloud.opencode.base.email.query.EmailQuery.Builder.class);
        }

        @Test
        @DisplayName("config()返回配置构建器")
        void testConfigBuilder() {
            var builder = OpenEmail.config();

            assertThat(builder).isNotNull();
            assertThat(builder).isInstanceOf(EmailConfig.Builder.class);
        }

        @Test
        @DisplayName("receiveConfig()返回接收配置构建器")
        void testReceiveConfigBuilder() {
            var builder = OpenEmail.receiveConfig();

            assertThat(builder).isNotNull();
            assertThat(builder).isInstanceOf(EmailReceiveConfig.Builder.class);
        }
    }

    @Nested
    @DisplayName("email() 构建器测试")
    class EmailBuilderTests {

        @Test
        @DisplayName("email()使用默认发件人")
        void testEmailBuilderWithDefaultFrom() {
            OpenEmail.configure(EmailConfig.builder()
                    .host("smtp.example.com")
                    .defaultFrom("sender@example.com", "Sender Name")
                    .build());

            Email email = OpenEmail.email()
                    .to("recipient@example.com")
                    .subject("Test")
                    .text("Content")
                    .build();

            assertThat(email.from()).isEqualTo("sender@example.com");
            assertThat(email.fromName()).isEqualTo("Sender Name");
        }

        @Test
        @DisplayName("email()无默认发件人时from为null")
        void testEmailBuilderWithoutDefaultFrom() {
            OpenEmail.configure(EmailConfig.builder()
                    .host("smtp.example.com")
                    .build());

            Email email = OpenEmail.email()
                    .to("recipient@example.com")
                    .subject("Test")
                    .text("Content")
                    .build();

            assertThat(email.from()).isNull();
        }
    }

    @Nested
    @DisplayName("shutdown() 测试")
    class ShutdownTests {

        @Test
        @DisplayName("shutdown()关闭所有资源")
        void testShutdown() {
            OpenEmail.configure(EmailConfig.builder()
                    .host("smtp.example.com")
                    .build());

            OpenEmail.configureReceiver(EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .username("user@example.com")
                    .password("password")
                    .imap()
                    .build());

            OpenEmail.shutdown();

            assertThat(OpenEmail.isConfigured()).isFalse();
            assertThat(OpenEmail.isReceiverConfigured()).isFalse();
        }

        @Test
        @DisplayName("shutdownSender()仅关闭发送器")
        void testShutdownSender() {
            OpenEmail.configure(EmailConfig.builder()
                    .host("smtp.example.com")
                    .build());

            OpenEmail.configureReceiver(EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .username("user@example.com")
                    .password("password")
                    .imap()
                    .build());

            OpenEmail.shutdownSender();

            assertThat(OpenEmail.isConfigured()).isFalse();
            assertThat(OpenEmail.isReceiverConfigured()).isTrue();
        }

        @Test
        @DisplayName("shutdownReceiver()仅关闭接收器")
        void testShutdownReceiver() {
            OpenEmail.configure(EmailConfig.builder()
                    .host("smtp.example.com")
                    .build());

            OpenEmail.configureReceiver(EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .username("user@example.com")
                    .password("password")
                    .imap()
                    .build());

            OpenEmail.shutdownReceiver();

            assertThat(OpenEmail.isConfigured()).isTrue();
            assertThat(OpenEmail.isReceiverConfigured()).isFalse();
        }
    }

    @Nested
    @DisplayName("getConfig() 测试")
    class GetConfigTests {

        @Test
        @DisplayName("未配置时返回null")
        void testGetConfigBeforeConfigure() {
            assertThat(OpenEmail.getConfig()).isNull();
        }

        @Test
        @DisplayName("配置后返回配置对象")
        void testGetConfigAfterConfigure() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.test.com")
                    .build();

            OpenEmail.configure(config);

            assertThat(OpenEmail.getConfig()).isEqualTo(config);
        }
    }

    @Nested
    @DisplayName("getReceiveConfig() 测试")
    class GetReceiveConfigTests {

        @Test
        @DisplayName("未配置时返回null")
        void testGetReceiveConfigBeforeConfigure() {
            assertThat(OpenEmail.getReceiveConfig()).isNull();
        }

        @Test
        @DisplayName("配置后返回配置对象")
        void testGetReceiveConfigAfterConfigure() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.test.com")
                    .username("user@test.com")
                    .password("password")
                    .imap()
                    .build();

            OpenEmail.configureReceiver(config);

            assertThat(OpenEmail.getReceiveConfig()).isEqualTo(config);
        }
    }
}
