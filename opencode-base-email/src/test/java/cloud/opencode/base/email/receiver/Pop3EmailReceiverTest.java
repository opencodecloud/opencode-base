package cloud.opencode.base.email.receiver;

import cloud.opencode.base.email.EmailReceiveConfig;
import cloud.opencode.base.email.exception.EmailReceiveException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Pop3EmailReceiver 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("Pop3EmailReceiver 测试")
class Pop3EmailReceiverTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("null配置抛出异常")
        void testNullConfig() {
            assertThatThrownBy(() -> new Pop3EmailReceiver(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null");
        }

        @Test
        @DisplayName("非POP3协议抛出异常")
        void testNonPop3Protocol() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .username("user@example.com")
                    .password("password")
                    .imap()
                    .build();

            assertThatThrownBy(() -> new Pop3EmailReceiver(config))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("POP3 protocol");
        }

        @Test
        @DisplayName("使用POP3配置创建成功")
        void testValidPop3Config() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("pop.example.com")
                    .username("user@example.com")
                    .password("password")
                    .pop3()
                    .build();

            Pop3EmailReceiver receiver = new Pop3EmailReceiver(config);

            assertThat(receiver).isNotNull();
            assertThat(receiver.isConnected()).isFalse();
        }

        @Test
        @DisplayName("使用POP3S配置创建成功")
        void testValidPop3sConfig() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("pop.example.com")
                    .port(995)
                    .username("user@example.com")
                    .password("password")
                    .pop3()
                    .ssl(true)
                    .build();

            Pop3EmailReceiver receiver = new Pop3EmailReceiver(config);

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
            Pop3EmailReceiver receiver = new Pop3EmailReceiver(config);

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
            Pop3EmailReceiver receiver = new Pop3EmailReceiver(config);

            assertThatNoException().isThrownBy(receiver::disconnect);
        }
    }

    @Nested
    @DisplayName("listFolders() 测试")
    class ListFoldersTests {

        @Test
        @DisplayName("POP3只返回INBOX")
        void testOnlyInbox() {
            EmailReceiveConfig config = createTestConfig();
            Pop3EmailReceiver receiver = new Pop3EmailReceiver(config);

            List<String> folders = receiver.listFolders();

            assertThat(folders).containsExactly("INBOX");
        }
    }

    @Nested
    @DisplayName("moveToFolder() 测试")
    class MoveToFolderTests {

        @Test
        @DisplayName("POP3不支持移动操作")
        void testMoveNotSupported() {
            EmailReceiveConfig config = createTestConfig();
            Pop3EmailReceiver receiver = new Pop3EmailReceiver(config);

            assertThatThrownBy(() -> receiver.moveToFolder("<123>", "Archive"))
                    .isInstanceOf(EmailReceiveException.class)
                    .hasMessageContaining("not supported by POP3");
        }
    }

    @Nested
    @DisplayName("markAsRead() 测试")
    class MarkAsReadTests {

        @Test
        @DisplayName("POP3标记已读是空操作")
        void testMarkAsReadIsNoop() {
            EmailReceiveConfig config = createTestConfig();
            Pop3EmailReceiver receiver = new Pop3EmailReceiver(config);

            // Should not throw - it's a no-op for POP3
            assertThatNoException().isThrownBy(() -> receiver.markAsRead("<123>"));
        }
    }

    @Nested
    @DisplayName("markAsUnread() 测试")
    class MarkAsUnreadTests {

        @Test
        @DisplayName("POP3标记未读是空操作")
        void testMarkAsUnreadIsNoop() {
            EmailReceiveConfig config = createTestConfig();
            Pop3EmailReceiver receiver = new Pop3EmailReceiver(config);

            // Should not throw - it's a no-op for POP3
            assertThatNoException().isThrownBy(() -> receiver.markAsUnread("<123>"));
        }
    }

    @Nested
    @DisplayName("setFlagged() 测试")
    class SetFlaggedTests {

        @Test
        @DisplayName("POP3设置标记是空操作")
        void testSetFlaggedIsNoop() {
            EmailReceiveConfig config = createTestConfig();
            Pop3EmailReceiver receiver = new Pop3EmailReceiver(config);

            // Should not throw - it's a no-op for POP3
            assertThatNoException().isThrownBy(() -> receiver.setFlagged("<123>", true));
        }
    }

    @Nested
    @DisplayName("配置选项测试")
    class ConfigOptionTests {

        @Test
        @DisplayName("SSL配置")
        void testSslConfig() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("pop.example.com")
                    .port(995)
                    .username("user@example.com")
                    .password("password")
                    .pop3()
                    .ssl(true)
                    .build();

            assertThat(config.ssl()).isTrue();
            assertThat(config.port()).isEqualTo(995);
        }

        @Test
        @DisplayName("STARTTLS配置")
        void testStarttlsConfig() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("pop.example.com")
                    .port(110)
                    .username("user@example.com")
                    .password("password")
                    .pop3()
                    .starttls(true)
                    .build();

            assertThat(config.starttls()).isTrue();
        }

        @Test
        @DisplayName("OAuth2配置")
        void testOAuth2Config() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("pop.gmail.com")
                    .port(995)
                    .username("user@gmail.com")
                    .oauth2Token("access-token")
                    .pop3()
                    .ssl(true)
                    .build();

            assertThat(config.hasOAuth2()).isTrue();
            assertThat(config.requiresAuth()).isTrue();
        }

        @Test
        @DisplayName("超时配置")
        void testTimeoutConfig() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("pop.example.com")
                    .username("user@example.com")
                    .password("password")
                    .pop3()
                    .timeout(Duration.ofSeconds(60))
                    .connectionTimeout(Duration.ofSeconds(30))
                    .build();

            assertThat(config.timeout()).isEqualTo(Duration.ofSeconds(60));
            assertThat(config.connectionTimeout()).isEqualTo(Duration.ofSeconds(30));
        }

        @Test
        @DisplayName("最大消息数配置")
        void testMaxMessagesConfig() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("pop.example.com")
                    .username("user@example.com")
                    .password("password")
                    .pop3()
                    .maxMessages(100)
                    .build();

            assertThat(config.maxMessages()).isEqualTo(100);
        }

        @Test
        @DisplayName("接收后删除配置")
        void testDeleteAfterReceiveConfig() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("pop.example.com")
                    .username("user@example.com")
                    .password("password")
                    .pop3()
                    .deleteAfterReceive(true)
                    .build();

            assertThat(config.deleteAfterReceive()).isTrue();
        }

        @Test
        @DisplayName("Debug模式配置")
        void testDebugConfig() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("pop.example.com")
                    .username("user@example.com")
                    .password("password")
                    .pop3()
                    .debug(true)
                    .build();

            assertThat(config.debug()).isTrue();
        }
    }

    @Nested
    @DisplayName("协议测试")
    class ProtocolTests {

        @Test
        @DisplayName("POP3协议")
        void testPop3Protocol() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("pop.example.com")
                    .username("user@example.com")
                    .password("password")
                    .pop3()
                    .build();

            assertThat(config.isPop3()).isTrue();
            assertThat(config.isImap()).isFalse();
        }

        @Test
        @DisplayName("POP3S协议")
        void testPop3sProtocol() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("pop.example.com")
                    .port(995)
                    .username("user@example.com")
                    .password("password")
                    .pop3()
                    .ssl(true)
                    .build();

            assertThat(config.isPop3()).isTrue();
            assertThat(config.getStoreProtocol()).isEqualTo("pop3s");
        }
    }

    @Nested
    @DisplayName("POP3限制测试")
    class Pop3LimitationsTests {

        @Test
        @DisplayName("不支持文件夹操作")
        void testNoFolderSupport() {
            EmailReceiveConfig config = createTestConfig();
            Pop3EmailReceiver receiver = new Pop3EmailReceiver(config);

            // POP3 only supports INBOX
            assertThat(receiver.listFolders()).containsExactly("INBOX");
        }

        @Test
        @DisplayName("不支持标记持久化")
        void testNoFlagPersistence() {
            EmailReceiveConfig config = createTestConfig();
            Pop3EmailReceiver receiver = new Pop3EmailReceiver(config);

            // These are no-ops for POP3
            assertThatNoException().isThrownBy(() -> {
                receiver.markAsRead("msg-id");
                receiver.markAsUnread("msg-id");
                receiver.setFlagged("msg-id", true);
            });
        }

        @Test
        @DisplayName("不支持移动操作")
        void testNoMoveSupport() {
            EmailReceiveConfig config = createTestConfig();
            Pop3EmailReceiver receiver = new Pop3EmailReceiver(config);

            assertThatThrownBy(() -> receiver.moveToFolder("msg-id", "Archive"))
                    .isInstanceOf(EmailReceiveException.class);
        }
    }

    private EmailReceiveConfig createTestConfig() {
        return EmailReceiveConfig.builder()
                .host("pop.example.com")
                .username("user@example.com")
                .password("password")
                .pop3()
                .build();
    }
}
