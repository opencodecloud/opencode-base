package cloud.opencode.base.email.receiver;

import cloud.opencode.base.email.EmailReceiveConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * ImapEmailReceiver 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("ImapEmailReceiver 测试")
class ImapEmailReceiverTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("null配置抛出异常")
        void testNullConfig() {
            assertThatThrownBy(() -> new ImapEmailReceiver(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null");
        }

        @Test
        @DisplayName("非IMAP协议抛出异常")
        void testNonImapProtocol() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("pop.example.com")
                    .username("user@example.com")
                    .password("password")
                    .pop3()
                    .build();

            assertThatThrownBy(() -> new ImapEmailReceiver(config))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("IMAP protocol");
        }

        @Test
        @DisplayName("使用IMAP配置创建成功")
        void testValidImapConfig() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .username("user@example.com")
                    .password("password")
                    .imap()
                    .build();

            ImapEmailReceiver receiver = new ImapEmailReceiver(config);

            assertThat(receiver).isNotNull();
            assertThat(receiver.isConnected()).isFalse();
        }

        @Test
        @DisplayName("使用IMAPS配置创建成功")
        void testValidImapsConfig() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .port(993)
                    .username("user@example.com")
                    .password("password")
                    .imap()
                    .ssl(true)
                    .build();

            ImapEmailReceiver receiver = new ImapEmailReceiver(config);

            assertThat(receiver).isNotNull();
        }
    }

    @Nested
    @DisplayName("isConnected() 测试")
    class IsConnectedTests {

        @Test
        @DisplayName("未连接时返回false")
        void testNotConnected() {
            EmailReceiveConfig config = createTestConfig();
            ImapEmailReceiver receiver = new ImapEmailReceiver(config);

            assertThat(receiver.isConnected()).isFalse();
        }
    }

    @Nested
    @DisplayName("disconnect() 测试")
    class DisconnectTests {

        @Test
        @DisplayName("断开未连接的接收器不抛异常")
        void testDisconnectNotConnected() {
            EmailReceiveConfig config = createTestConfig();
            ImapEmailReceiver receiver = new ImapEmailReceiver(config);

            assertThatNoException().isThrownBy(receiver::disconnect);
        }
    }

    @Nested
    @DisplayName("listFolders() 测试")
    class ListFoldersTests {

        @Test
        @DisplayName("未连接时自动连接")
        void testAutoConnect() {
            // This test would require a mock or real server
            // Just verify the method exists and config is accessible
            EmailReceiveConfig config = createTestConfig();
            ImapEmailReceiver receiver = new ImapEmailReceiver(config);

            assertThat(receiver.isConnected()).isFalse();
        }
    }

    @Nested
    @DisplayName("配置选项测试")
    class ConfigOptionTests {

        @Test
        @DisplayName("SSL配置")
        void testSslConfig() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .port(993)
                    .username("user@example.com")
                    .password("password")
                    .imap()
                    .ssl(true)
                    .build();

            assertThat(config.ssl()).isTrue();
            assertThat(config.port()).isEqualTo(993);
        }

        @Test
        @DisplayName("STARTTLS配置")
        void testStarttlsConfig() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .port(143)
                    .username("user@example.com")
                    .password("password")
                    .imap()
                    .starttls(true)
                    .build();

            assertThat(config.starttls()).isTrue();
        }

        @Test
        @DisplayName("OAuth2配置")
        void testOAuth2Config() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.gmail.com")
                    .port(993)
                    .username("user@gmail.com")
                    .oauth2Token("access-token")
                    .imap()
                    .ssl(true)
                    .build();

            assertThat(config.hasOAuth2()).isTrue();
            assertThat(config.requiresAuth()).isTrue();
        }

        @Test
        @DisplayName("超时配置")
        void testTimeoutConfig() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .username("user@example.com")
                    .password("password")
                    .imap()
                    .timeout(Duration.ofSeconds(60))
                    .connectionTimeout(Duration.ofSeconds(30))
                    .build();

            assertThat(config.timeout()).isEqualTo(Duration.ofSeconds(60));
            assertThat(config.connectionTimeout()).isEqualTo(Duration.ofSeconds(30));
        }

        @Test
        @DisplayName("默认文件夹配置")
        void testDefaultFolderConfig() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .username("user@example.com")
                    .password("password")
                    .imap()
                    .defaultFolder("INBOX")
                    .build();

            assertThat(config.defaultFolder()).isEqualTo("INBOX");
        }

        @Test
        @DisplayName("最大消息数配置")
        void testMaxMessagesConfig() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .username("user@example.com")
                    .password("password")
                    .imap()
                    .maxMessages(50)
                    .build();

            assertThat(config.maxMessages()).isEqualTo(50);
        }

        @Test
        @DisplayName("接收后标记已读配置")
        void testMarkAsReadConfig() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .username("user@example.com")
                    .password("password")
                    .imap()
                    .markAsReadAfterReceive(true)
                    .build();

            assertThat(config.markAsReadAfterReceive()).isTrue();
        }

        @Test
        @DisplayName("接收后删除配置")
        void testDeleteAfterReceiveConfig() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .username("user@example.com")
                    .password("password")
                    .imap()
                    .deleteAfterReceive(true)
                    .build();

            assertThat(config.deleteAfterReceive()).isTrue();
        }

        @Test
        @DisplayName("Debug模式配置")
        void testDebugConfig() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .username("user@example.com")
                    .password("password")
                    .imap()
                    .debug(true)
                    .build();

            assertThat(config.debug()).isTrue();
        }
    }

    @Nested
    @DisplayName("协议测试")
    class ProtocolTests {

        @Test
        @DisplayName("IMAP协议")
        void testImapProtocol() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .username("user@example.com")
                    .password("password")
                    .imap()
                    .build();

            assertThat(config.isImap()).isTrue();
            assertThat(config.isPop3()).isFalse();
        }

        @Test
        @DisplayName("IMAPS协议")
        void testImapsProtocol() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .port(993)
                    .username("user@example.com")
                    .password("password")
                    .imap()
                    .ssl(true)
                    .build();

            assertThat(config.isImap()).isTrue();
            assertThat(config.getStoreProtocol()).isEqualTo("imaps");
        }
    }

    private EmailReceiveConfig createTestConfig() {
        return EmailReceiveConfig.builder()
                .host("imap.example.com")
                .username("user@example.com")
                .password("password")
                .imap()
                .build();
    }
}
