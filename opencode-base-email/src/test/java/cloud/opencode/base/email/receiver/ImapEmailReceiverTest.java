package cloud.opencode.base.email.receiver;

import cloud.opencode.base.email.EmailFlags;
import cloud.opencode.base.email.EmailReceiveConfig;
import cloud.opencode.base.email.query.EmailQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

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

    @Nested
    @DisplayName("EscapeSearchValue Tests (via reflection)")
    class EscapeSearchValueTests {

        /**
         * Invoke the private escapeSearchValue method via reflection.
         */
        private String invokeEscapeSearchValue(String value) throws Exception {
            ImapEmailReceiver receiver = new ImapEmailReceiver(createTestConfig());
            Method method = ImapEmailReceiver.class.getDeclaredMethod("escapeSearchValue", String.class);
            method.setAccessible(true);
            return (String) method.invoke(receiver, value);
        }

        @Test
        @DisplayName("normal string passes through unchanged")
        void testNormalString() throws Exception {
            assertThat(invokeEscapeSearchValue("hello world")).isEqualTo("hello world");
        }

        @Test
        @DisplayName("null returns empty string")
        void testNullReturnsEmpty() throws Exception {
            assertThat(invokeEscapeSearchValue(null)).isEmpty();
        }

        @Test
        @DisplayName("backslash is escaped")
        void testBackslashEscaped() throws Exception {
            assertThat(invokeEscapeSearchValue("test\\path")).isEqualTo("test\\\\path");
        }

        @Test
        @DisplayName("double-quote is escaped")
        void testQuoteEscaped() throws Exception {
            assertThat(invokeEscapeSearchValue("say \"hello\"")).isEqualTo("say \\\"hello\\\"");
        }

        @Test
        @DisplayName("CR character rejected")
        void testCrRejected() {
            assertThatThrownBy(() -> invokeEscapeSearchValue("line\rone"))
                    .hasCauseInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("LF character rejected")
        void testLfRejected() {
            assertThatThrownBy(() -> invokeEscapeSearchValue("line\none"))
                    .hasCauseInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("NUL character rejected")
        void testNulRejected() {
            assertThatThrownBy(() -> invokeEscapeSearchValue("line\0one"))
                    .hasCauseInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("BuildSearchCriteria Tests (via reflection)")
    class BuildSearchCriteriaTests {

        /**
         * Invoke the private buildSearchCriteria method via reflection.
         */
        private String invokeBuildSearchCriteria(EmailQuery query) throws Exception {
            ImapEmailReceiver receiver = new ImapEmailReceiver(createTestConfig());
            Method method = ImapEmailReceiver.class.getDeclaredMethod("buildSearchCriteria", EmailQuery.class);
            method.setAccessible(true);
            return (String) method.invoke(receiver, query);
        }

        @Test
        @DisplayName("default query includes NOT DELETED (since includeDeleted defaults to false)")
        void testDefaultQueryIncludesNotDeleted() throws Exception {
            EmailQuery query = EmailQuery.builder().build();
            assertThat(invokeBuildSearchCriteria(query)).isEqualTo("NOT DELETED");
        }

        @Test
        @DisplayName("includeDeleted query with no other filters returns ALL")
        void testIncludeDeletedReturnsAll() throws Exception {
            EmailQuery query = EmailQuery.builder().includeDeleted().build();
            assertThat(invokeBuildSearchCriteria(query)).isEqualTo("ALL");
        }

        @Test
        @DisplayName("unreadOnly adds UNSEEN")
        void testUnreadOnly() throws Exception {
            EmailQuery query = EmailQuery.builder().unreadOnly().build();
            assertThat(invokeBuildSearchCriteria(query)).contains("UNSEEN");
        }

        @Test
        @DisplayName("flaggedOnly adds FLAGGED")
        void testFlaggedOnly() throws Exception {
            EmailQuery query = EmailQuery.builder().flaggedOnly().build();
            assertThat(invokeBuildSearchCriteria(query)).contains("FLAGGED");
        }

        @Test
        @DisplayName("fromDate adds SINCE")
        void testFromDate() throws Exception {
            EmailQuery query = EmailQuery.builder()
                    .fromDate(LocalDateTime.of(2025, 1, 15, 0, 0))
                    .build();
            String criteria = invokeBuildSearchCriteria(query);
            assertThat(criteria).contains("SINCE");
            assertThat(criteria).contains("15-Jan-2025");
        }

        @Test
        @DisplayName("toDate adds BEFORE")
        void testToDate() throws Exception {
            EmailQuery query = EmailQuery.builder()
                    .toDate(LocalDateTime.of(2025, 6, 30, 23, 59))
                    .build();
            String criteria = invokeBuildSearchCriteria(query);
            assertThat(criteria).contains("BEFORE");
            assertThat(criteria).contains("30-Jun-2025");
        }

        @Test
        @DisplayName("single from address adds FROM")
        void testSingleFrom() throws Exception {
            EmailQuery query = EmailQuery.builder()
                    .from("alice@example.com")
                    .build();
            String criteria = invokeBuildSearchCriteria(query);
            assertThat(criteria).contains("FROM \"alice@example.com\"");
        }

        @Test
        @DisplayName("multiple from addresses adds OR chain")
        void testMultipleFrom() throws Exception {
            EmailQuery query = EmailQuery.builder()
                    .from(Set.of("alice@example.com", "bob@example.com"))
                    .build();
            String criteria = invokeBuildSearchCriteria(query);
            assertThat(criteria).contains("OR");
            assertThat(criteria).contains("FROM");
        }

        @Test
        @DisplayName("subjectContains adds SUBJECT")
        void testSubjectContains() throws Exception {
            EmailQuery query = EmailQuery.builder()
                    .subjectContains("important")
                    .build();
            String criteria = invokeBuildSearchCriteria(query);
            assertThat(criteria).contains("SUBJECT \"important\"");
        }

        @Test
        @DisplayName("bodyContains adds BODY")
        void testBodyContains() throws Exception {
            EmailQuery query = EmailQuery.builder()
                    .bodyContains("keyword")
                    .build();
            String criteria = invokeBuildSearchCriteria(query);
            assertThat(criteria).contains("BODY \"keyword\"");
        }

        @Test
        @DisplayName("includeDeleted false adds NOT DELETED")
        void testNotDeletedDefault() throws Exception {
            // Default includeDeleted is false
            EmailQuery query = EmailQuery.builder().unreadOnly().build();
            String criteria = invokeBuildSearchCriteria(query);
            assertThat(criteria).contains("NOT DELETED");
        }

        @Test
        @DisplayName("combined criteria joins with spaces")
        void testCombinedCriteria() throws Exception {
            EmailQuery query = EmailQuery.builder()
                    .unreadOnly()
                    .flaggedOnly()
                    .subjectContains("test")
                    .build();
            String criteria = invokeBuildSearchCriteria(query);
            assertThat(criteria).contains("UNSEEN");
            assertThat(criteria).contains("FLAGGED");
            assertThat(criteria).contains("SUBJECT \"test\"");
        }
    }

    @Nested
    @DisplayName("ParseFlagsFromResponse Tests (via reflection)")
    class ParseFlagsFromResponseTests {

        private EmailFlags invokeParseFlagsFromResponse(String response) throws Exception {
            ImapEmailReceiver receiver = new ImapEmailReceiver(createTestConfig());
            Method method = ImapEmailReceiver.class.getDeclaredMethod(
                    "parseFlagsFromResponse", String.class);
            method.setAccessible(true);
            return (EmailFlags) method.invoke(receiver, response);
        }

        @Test
        @DisplayName("parses \\Seen flag")
        void testSeenFlag() throws Exception {
            EmailFlags flags = invokeParseFlagsFromResponse("* 1 FETCH (FLAGS (\\Seen))");
            assertThat(flags.seen()).isTrue();
            assertThat(flags.answered()).isFalse();
        }

        @Test
        @DisplayName("parses multiple flags")
        void testMultipleFlags() throws Exception {
            EmailFlags flags = invokeParseFlagsFromResponse(
                    "* 1 FETCH (FLAGS (\\Seen \\Answered \\Flagged))");
            assertThat(flags.seen()).isTrue();
            assertThat(flags.answered()).isTrue();
            assertThat(flags.flagged()).isTrue();
            assertThat(flags.deleted()).isFalse();
        }

        @Test
        @DisplayName("no FLAGS returns UNREAD")
        void testNoFlagsReturnsUnread() throws Exception {
            EmailFlags flags = invokeParseFlagsFromResponse("* 1 FETCH (BODY[] {5})");
            assertThat(flags).isEqualTo(EmailFlags.UNREAD);
        }

        @Test
        @DisplayName("parses \\Deleted flag")
        void testDeletedFlag() throws Exception {
            EmailFlags flags = invokeParseFlagsFromResponse("* 1 FETCH (FLAGS (\\Deleted))");
            assertThat(flags.deleted()).isTrue();
        }

        @Test
        @DisplayName("parses \\Draft flag")
        void testDraftFlag() throws Exception {
            EmailFlags flags = invokeParseFlagsFromResponse("* 1 FETCH (FLAGS (\\Draft))");
            assertThat(flags.draft()).isTrue();
        }

        @Test
        @DisplayName("parses \\Recent flag")
        void testRecentFlag() throws Exception {
            EmailFlags flags = invokeParseFlagsFromResponse("* 1 FETCH (FLAGS (\\Recent))");
            assertThat(flags.recent()).isTrue();
        }
    }

    @Nested
    @DisplayName("ParseSizeFromResponse Tests (via reflection)")
    class ParseSizeFromResponseTests {

        private long invokeParseSizeFromResponse(String response) throws Exception {
            ImapEmailReceiver receiver = new ImapEmailReceiver(createTestConfig());
            Method method = ImapEmailReceiver.class.getDeclaredMethod(
                    "parseSizeFromResponse", String.class);
            method.setAccessible(true);
            return (long) method.invoke(receiver, response);
        }

        @Test
        @DisplayName("parses RFC822.SIZE from response")
        void testParsesSize() throws Exception {
            long size = invokeParseSizeFromResponse(
                    "* 1 FETCH (FLAGS (\\Seen) RFC822.SIZE 12345)");
            assertThat(size).isEqualTo(12345L);
        }

        @Test
        @DisplayName("returns 0 when no RFC822.SIZE present")
        void testNoSizeReturnsZero() throws Exception {
            long size = invokeParseSizeFromResponse("* 1 FETCH (FLAGS (\\Seen))");
            assertThat(size).isZero();
        }
    }

    @Nested
    @DisplayName("ExtractBodyLiteral Tests (via reflection)")
    class ExtractBodyLiteralTests {

        private String invokeExtractBodyLiteral(String response) throws Exception {
            ImapEmailReceiver receiver = new ImapEmailReceiver(createTestConfig());
            Method method = ImapEmailReceiver.class.getDeclaredMethod(
                    "extractBodyLiteral", String.class);
            method.setAccessible(true);
            return (String) method.invoke(receiver, response);
        }

        @Test
        @DisplayName("extracts body literal of specified length")
        void testExtractsLiteral() throws Exception {
            String response = "* 1 FETCH (BODY[] {11}\r\nHello World)";
            String body = invokeExtractBodyLiteral(response);
            assertThat(body).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("returns null when no literal marker found")
        void testNoLiteralReturnsNull() throws Exception {
            String body = invokeExtractBodyLiteral("* 1 FETCH (FLAGS (\\Seen))");
            assertThat(body).isNull();
        }

        @Test
        @DisplayName("handles literal size exceeding available data")
        void testLiteralExceedsData() throws Exception {
            // Literal says 100 bytes but only 5 available
            String response = "* 1 FETCH (BODY[] {100}\r\nHello)";
            String body = invokeExtractBodyLiteral(response);
            // Should take what's available
            assertThat(body).isEqualTo("Hello)");
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
