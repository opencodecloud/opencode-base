package cloud.opencode.base.email;

import cloud.opencode.base.email.exception.EmailErrorCode;
import cloud.opencode.base.email.exception.EmailException;
import cloud.opencode.base.email.exception.EmailReceiveException;
import cloud.opencode.base.email.exception.EmailSendException;
import cloud.opencode.base.email.internal.EmailSender;
import cloud.opencode.base.email.query.EmailQuery;
import cloud.opencode.base.email.security.EmailRateLimiter;
import cloud.opencode.base.email.template.SimpleEmailTemplate;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenEmail Integration Test
 * OpenEmail 集成测试
 *
 * <p>Tests the OpenEmail static facade with InMemoryEmailSender for send-side tests.</p>
 * <p>使用 InMemoryEmailSender 测试 OpenEmail 静态门面的发送端功能。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.3
 */
@DisplayName("OpenEmail Integration Tests")
class OpenEmailIntegrationTest {

    @AfterEach
    void reset() {
        OpenEmail.disableRateLimiting();
        OpenEmail.shutdown();
    }

    /**
     * Simple in-memory email sender for testing.
     * Records all sent emails and their results.
     */
    static final class InMemoryEmailSender implements EmailSender {
        private final List<Email> sentEmails = new CopyOnWriteArrayList<>();
        private int messageCounter = 0;

        @Override
        public void send(Email email) {
            sentEmails.add(email);
        }

        @Override
        public SendResult sendWithResult(Email email) {
            sentEmails.add(email);
            messageCounter++;
            return SendResult.success("<msg-" + messageCounter + "@inmemory>");
        }

        @Override
        public void close() {
            // no-op
        }

        List<Email> getSentEmails() {
            return Collections.unmodifiableList(sentEmails);
        }

        void clear() {
            sentEmails.clear();
            messageCounter = 0;
        }
    }

    private static EmailConfig testConfig() {
        return EmailConfig.builder()
                .host("smtp.test.local")
                .port(587)
                .username("user@test.local")
                .password("secret")
                .defaultFrom("noreply@test.local", "Test Sender")
                .build();
    }

    private static InMemoryEmailSender configureWithInMemorySender() {
        InMemoryEmailSender sender = new InMemoryEmailSender();
        OpenEmail.configure(testConfig(), sender);
        return sender;
    }

    @Nested
    @DisplayName("ConfigureTests")
    class ConfigureTests {

        @Test
        @DisplayName("configure(host, port, user, pass) - quick config")
        void testQuickConfig() {
            OpenEmail.configure("smtp.quick.local", 25, "admin", "pw");

            assertThat(OpenEmail.isConfigured()).isTrue();
            assertThat(OpenEmail.getConfig().host()).isEqualTo("smtp.quick.local");
            assertThat(OpenEmail.getConfig().port()).isEqualTo(25);
            assertThat(OpenEmail.getConfig().username()).isEqualTo("admin");
        }

        @Test
        @DisplayName("configure(config, sender) - custom sender")
        void testConfigureCustomSender() {
            InMemoryEmailSender sender = new InMemoryEmailSender();
            EmailConfig config = testConfig();

            OpenEmail.configure(config, sender);

            assertThat(OpenEmail.isConfigured()).isTrue();
            assertThat(OpenEmail.getConfig()).isEqualTo(config);
        }

        @Test
        @DisplayName("isConfigured() before configure returns false")
        void testIsConfiguredBeforeConfigure() {
            assertThat(OpenEmail.isConfigured()).isFalse();
        }

        @Test
        @DisplayName("isConfigured() after configure returns true")
        void testIsConfiguredAfterConfigure() {
            configureWithInMemorySender();

            assertThat(OpenEmail.isConfigured()).isTrue();
        }

        @Test
        @DisplayName("getConfig() returns correct config")
        void testGetConfig() {
            EmailConfig config = testConfig();
            OpenEmail.configure(config, new InMemoryEmailSender());

            assertThat(OpenEmail.getConfig()).isSameAs(config);
            assertThat(OpenEmail.getConfig().host()).isEqualTo("smtp.test.local");
            assertThat(OpenEmail.getConfig().port()).isEqualTo(587);
        }
    }

    @Nested
    @DisplayName("SendTests")
    class SendTests {

        private InMemoryEmailSender sender;

        @BeforeEach
        void setUp() {
            sender = configureWithInMemorySender();
        }

        @Test
        @DisplayName("sendText() sends a plain text email")
        void testSendText() {
            OpenEmail.sendText("alice@example.com", "Hello", "Hello World!");

            assertThat(sender.getSentEmails()).hasSize(1);
            Email sent = sender.getSentEmails().getFirst();
            assertThat(sent.to()).containsExactly("alice@example.com");
            assertThat(sent.subject()).isEqualTo("Hello");
            assertThat(sent.content()).isEqualTo("Hello World!");
            assertThat(sent.html()).isFalse();
        }

        @Test
        @DisplayName("sendHtml() sends an HTML email")
        void testSendHtml() {
            OpenEmail.sendHtml("bob@example.com", "Welcome",
                    "<h1>Welcome</h1><p>Thanks!</p>");

            assertThat(sender.getSentEmails()).hasSize(1);
            Email sent = sender.getSentEmails().getFirst();
            assertThat(sent.to()).containsExactly("bob@example.com");
            assertThat(sent.subject()).isEqualTo("Welcome");
            assertThat(sent.content()).contains("<h1>Welcome</h1>");
            assertThat(sent.html()).isTrue();
        }

        @Test
        @DisplayName("sendWithResult() returns SendResult with messageId")
        void testSendWithResult() {
            Email email = OpenEmail.email()
                    .to("charlie@example.com")
                    .subject("Test")
                    .text("content")
                    .build();

            SendResult result = OpenEmail.sendWithResult(email);

            assertThat(result).isNotNull();
            assertThat(result.success()).isTrue();
            assertThat(result.messageId()).startsWith("<msg-");
            assertThat(result.sentAt()).isNotNull();
        }

        @Test
        @DisplayName("sendToMultiple() sends to all recipients")
        void testSendToMultiple() {
            List<String> recipients = List.of("a@test.com", "b@test.com", "c@test.com");

            OpenEmail.sendToMultiple(recipients, "Announcement", "Hello all!");

            assertThat(sender.getSentEmails()).hasSize(1);
            Email sent = sender.getSentEmails().getFirst();
            assertThat(sent.to()).containsExactly("a@test.com", "b@test.com", "c@test.com");
        }

        @Test
        @DisplayName("send(Email) with builder sets default from")
        void testSendWithBuilder() {
            Email email = OpenEmail.email()
                    .to("user@example.com")
                    .subject("Via Builder")
                    .text("Builder content")
                    .build();

            OpenEmail.send(email);

            assertThat(sender.getSentEmails()).hasSize(1);
            Email sent = sender.getSentEmails().getFirst();
            assertThat(sent.from()).isEqualTo("noreply@test.local");
            assertThat(sent.fromName()).isEqualTo("Test Sender");
            assertThat(sent.subject()).isEqualTo("Via Builder");
        }
    }

    @Nested
    @DisplayName("TemplateTests")
    class TemplateTests {

        private InMemoryEmailSender sender;

        @BeforeEach
        void setUp() {
            sender = configureWithInMemorySender();
            OpenEmail.setTemplateEngine(SimpleEmailTemplate.getInstance());
        }

        @Test
        @DisplayName("sendTemplate() renders variables in template content")
        void testSendTemplateWithVariables() {
            String template = "Hello ${name}, your order #${orderId} is confirmed.";
            Map<String, Object> vars = Map.of("name", "Alice", "orderId", "12345");

            OpenEmail.sendTemplate("alice@example.com", "Order Confirmation", template, vars);

            assertThat(sender.getSentEmails()).hasSize(1);
            Email sent = sender.getSentEmails().getFirst();
            assertThat(sent.content()).isEqualTo("Hello Alice, your order #12345 is confirmed.");
            assertThat(sent.html()).isTrue();
        }

        @Test
        @DisplayName("sendTemplate() with mustache-style variables")
        void testSendTemplateWithMustacheVars() {
            String template = "Welcome {{user}}! Your plan: {{plan}}.";
            Map<String, Object> vars = Map.of("user", "Bob", "plan", "Pro");

            OpenEmail.sendTemplate("bob@example.com", "Welcome", template, vars);

            assertThat(sender.getSentEmails()).hasSize(1);
            Email sent = sender.getSentEmails().getFirst();
            assertThat(sent.content()).isEqualTo("Welcome Bob! Your plan: Pro.");
        }
    }

    @Nested
    @DisplayName("RateLimitingTests")
    class RateLimitingTests {

        @Test
        @DisplayName("enableRateLimiting() enables rate limiting")
        void testEnableRateLimiting() {
            OpenEmail.enableRateLimiting();

            assertThat(OpenEmail.isRateLimitingEnabled()).isTrue();
        }

        @Test
        @DisplayName("getRateLimitQuota() returns non-null when enabled")
        void testGetGlobalQuotaEnabled() {
            OpenEmail.enableRateLimiting();

            EmailRateLimiter.RateLimitQuota quota = OpenEmail.getRateLimitQuota();

            assertThat(quota).isNotNull();
            assertThat(quota.minuteRemaining()).isEqualTo(10);
            assertThat(quota.hourRemaining()).isEqualTo(100);
            assertThat(quota.dayRemaining()).isEqualTo(1000);
        }

        @Test
        @DisplayName("getRateLimitQuota(recipient) returns non-null when enabled")
        void testGetRecipientQuotaEnabled() {
            OpenEmail.enableRateLimiting(5, 50, 500);

            EmailRateLimiter.RateLimitQuota quota = OpenEmail.getRateLimitQuota("user@test.com");

            assertThat(quota).isNotNull();
            assertThat(quota.minuteRemaining()).isEqualTo(5);
        }

        @Test
        @DisplayName("disableRateLimiting() disables it")
        void testDisableRateLimiting() {
            OpenEmail.enableRateLimiting();
            OpenEmail.disableRateLimiting();

            assertThat(OpenEmail.isRateLimitingEnabled()).isFalse();
            assertThat(OpenEmail.getRateLimitQuota()).isNull();
        }

        @Test
        @DisplayName("enableRateLimiting(custom limits) uses custom values")
        void testCustomLimits() {
            OpenEmail.enableRateLimiting(3, 30, 300);

            EmailRateLimiter.RateLimitQuota quota = OpenEmail.getRateLimitQuota();

            assertThat(quota).isNotNull();
            assertThat(quota.minuteRemaining()).isEqualTo(3);
            assertThat(quota.hourRemaining()).isEqualTo(30);
            assertThat(quota.dayRemaining()).isEqualTo(300);
        }

        @Test
        @DisplayName("Send exceeding rate limit throws EmailSendException with RATE_LIMITED code")
        void testRateLimitExceeded() {
            InMemoryEmailSender sender = configureWithInMemorySender();
            OpenEmail.enableRateLimiting(2, 100, 1000);

            // First two sends should succeed
            OpenEmail.sendText("user@test.com", "Test 1", "content 1");
            OpenEmail.sendText("user@test.com", "Test 2", "content 2");

            // Third send should fail due to global per-minute limit
            assertThatThrownBy(() -> OpenEmail.sendText("user@test.com", "Test 3", "content 3"))
                    .isInstanceOf(EmailSendException.class)
                    .satisfies(e -> {
                        EmailSendException ese = (EmailSendException) e;
                        assertThat(ese.getEmailErrorCode()).isEqualTo(EmailErrorCode.RATE_LIMITED);
                    });

            // Only 2 emails should have been sent
            assertThat(sender.getSentEmails()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("AsyncSendTests")
    class AsyncSendTests {

        private InMemoryEmailSender sender;

        @BeforeEach
        void setUp() {
            sender = configureWithInMemorySender();
        }

        @Test
        @DisplayName("sendAsync() returns completed future")
        void testSendAsync() {
            Email email = OpenEmail.email()
                    .to("async@test.com")
                    .subject("Async")
                    .text("async content")
                    .build();

            CompletableFuture<Void> future = OpenEmail.sendAsync(email);

            assertThat(future).succeedsWithin(Duration.ofSeconds(5));
            assertThat(sender.getSentEmails()).isNotEmpty();
        }

        @Test
        @DisplayName("sendTextAsync() completes successfully")
        void testSendTextAsync() {
            CompletableFuture<Void> future = OpenEmail.sendTextAsync(
                    "async@test.com", "Async Text", "text content");

            assertThat(future).succeedsWithin(Duration.ofSeconds(5));
        }

        @Test
        @DisplayName("sendHtmlAsync() completes successfully")
        void testSendHtmlAsync() {
            CompletableFuture<Void> future = OpenEmail.sendHtmlAsync(
                    "async@test.com", "Async HTML", "<p>html</p>");

            assertThat(future).succeedsWithin(Duration.ofSeconds(5));
        }

        @Test
        @DisplayName("sendAllAsync() returns list of futures that all complete")
        void testSendAllAsync() {
            List<Email> emails = List.of(
                    Email.builder().to("a@test.com").subject("A").text("a").build(),
                    Email.builder().to("b@test.com").subject("B").text("b").build(),
                    Email.builder().to("c@test.com").subject("C").text("c").build()
            );

            List<CompletableFuture<Void>> futures = OpenEmail.sendAllAsync(emails);

            assertThat(futures).hasSize(3);
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .orTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                    .join();
        }

        @Test
        @DisplayName("sendAllAndWait() completes when all emails sent")
        void testSendAllAndWait() {
            List<Email> emails = List.of(
                    Email.builder().to("x@test.com").subject("X").text("x").build(),
                    Email.builder().to("y@test.com").subject("Y").text("y").build()
            );

            CompletableFuture<Void> future = OpenEmail.sendAllAndWait(emails);

            assertThat(future).succeedsWithin(Duration.ofSeconds(5));
        }
    }

    @Nested
    @DisplayName("BatchSendTests")
    class BatchSendTests {

        @Test
        @DisplayName("sendBatch() with InMemoryEmailSender uses fallback path")
        void testSendBatchFallbackPath() {
            InMemoryEmailSender sender = configureWithInMemorySender();

            List<Email> emails = List.of(
                    Email.builder().to("a@batch.com").subject("A").text("a").build(),
                    Email.builder().to("b@batch.com").subject("B").text("b").build(),
                    Email.builder().to("c@batch.com").subject("C").text("c").build()
            );

            BatchSendResult result = OpenEmail.sendBatch(emails);

            assertThat(result).isNotNull();
            assertThat(result.totalCount()).isEqualTo(3);
            assertThat(result.successCount()).isEqualTo(3);
            assertThat(result.failureCount()).isEqualTo(0);
            assertThat(result.allSucceeded()).isTrue();
            assertThat(result.startedAt()).isNotNull();
            assertThat(result.duration()).isNotNull();
            assertThat(sender.getSentEmails()).hasSize(3);
        }

        @Test
        @DisplayName("sendBatch() with rate limiting applies per-email rate checks")
        void testSendBatchWithRateLimiting() {
            configureWithInMemorySender();
            OpenEmail.enableRateLimiting(2, 100, 1000);

            List<Email> emails = List.of(
                    Email.builder().to("a@batch.com").subject("A").text("a").build(),
                    Email.builder().to("b@batch.com").subject("B").text("b").build(),
                    Email.builder().to("c@batch.com").subject("C").text("c").build()
            );

            BatchSendResult result = OpenEmail.sendBatch(emails);

            // First 2 pass global rate limit, 3rd is rate-limited
            assertThat(result.totalCount()).isEqualTo(3);
            assertThat(result.successCount()).isEqualTo(2);
            assertThat(result.failureCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("BuilderTests")
    class BuilderTests {

        @Test
        @DisplayName("email() builder returns builder with default from")
        void testEmailBuilder() {
            configureWithInMemorySender();

            Email email = OpenEmail.email()
                    .to("user@test.com")
                    .subject("Test")
                    .text("content")
                    .build();

            assertThat(email.from()).isEqualTo("noreply@test.local");
            assertThat(email.fromName()).isEqualTo("Test Sender");
        }

        @Test
        @DisplayName("query() builder returns EmailQuery.Builder")
        void testQueryBuilder() {
            EmailQuery.Builder builder = OpenEmail.query();

            assertThat(builder).isNotNull();
            EmailQuery query = builder.limit(10).build();
            assertThat(query.limit()).isEqualTo(10);
        }

        @Test
        @DisplayName("config() builder returns EmailConfig.Builder")
        void testConfigBuilder() {
            EmailConfig.Builder builder = OpenEmail.config();

            assertThat(builder).isNotNull();
            EmailConfig config = builder.host("test.local").build();
            assertThat(config.host()).isEqualTo("test.local");
        }

        @Test
        @DisplayName("receiveConfig() builder returns EmailReceiveConfig.Builder")
        void testReceiveConfigBuilder() {
            EmailReceiveConfig.Builder builder = OpenEmail.receiveConfig();

            assertThat(builder).isNotNull();
            EmailReceiveConfig config = builder
                    .host("imap.test.local")
                    .username("user")
                    .password("pass")
                    .imap()
                    .build();
            assertThat(config.host()).isEqualTo("imap.test.local");
        }
    }

    @Nested
    @DisplayName("ShutdownTests")
    class ShutdownTests {

        @Test
        @DisplayName("shutdown() clears all state")
        void testShutdownClearsAll() {
            configureWithInMemorySender();
            OpenEmail.configureReceiver(EmailReceiveConfig.builder()
                    .host("imap.test.local")
                    .username("user")
                    .password("pass")
                    .imap()
                    .build());

            OpenEmail.shutdown();

            assertThat(OpenEmail.isConfigured()).isFalse();
            assertThat(OpenEmail.isReceiverConfigured()).isFalse();
            assertThat(OpenEmail.getConfig()).isNull();
            assertThat(OpenEmail.getReceiveConfig()).isNull();
        }

        @Test
        @DisplayName("shutdownSender() only clears sender")
        void testShutdownSenderOnly() {
            configureWithInMemorySender();
            OpenEmail.configureReceiver(EmailReceiveConfig.builder()
                    .host("imap.test.local")
                    .username("user")
                    .password("pass")
                    .imap()
                    .build());

            OpenEmail.shutdownSender();

            assertThat(OpenEmail.isConfigured()).isFalse();
            assertThat(OpenEmail.isReceiverConfigured()).isTrue();
        }

        @Test
        @DisplayName("shutdownReceiver() only clears receiver")
        void testShutdownReceiverOnly() {
            configureWithInMemorySender();
            OpenEmail.configureReceiver(EmailReceiveConfig.builder()
                    .host("imap.test.local")
                    .username("user")
                    .password("pass")
                    .imap()
                    .build());

            OpenEmail.shutdownReceiver();

            assertThat(OpenEmail.isConfigured()).isTrue();
            assertThat(OpenEmail.isReceiverConfigured()).isFalse();
        }

        @Test
        @DisplayName("After shutdown, send throws EmailException")
        void testSendAfterShutdown() {
            configureWithInMemorySender();
            OpenEmail.shutdown();

            assertThatThrownBy(() -> OpenEmail.sendText("user@test.com", "Test", "content"))
                    .isInstanceOf(EmailException.class)
                    .hasMessageContaining("not configured");
        }
    }

    @Nested
    @DisplayName("NotConfiguredTests")
    class NotConfiguredTests {

        @Test
        @DisplayName("send() without configure throws EmailException")
        void testSendWithoutConfigure() {
            Email email = Email.builder()
                    .to("test@example.com")
                    .subject("Test")
                    .text("content")
                    .build();

            assertThatThrownBy(() -> OpenEmail.send(email))
                    .isInstanceOf(EmailException.class)
                    .hasMessageContaining("not configured");
        }

        @Test
        @DisplayName("sendText() without configure throws EmailException")
        void testSendTextWithoutConfigure() {
            assertThatThrownBy(() -> OpenEmail.sendText("a@b.com", "s", "c"))
                    .isInstanceOf(EmailException.class)
                    .hasMessageContaining("not configured");
        }

        @Test
        @DisplayName("receiveUnread() without configureReceiver throws EmailReceiveException")
        void testReceiveUnreadWithoutConfig() {
            assertThatThrownBy(OpenEmail::receiveUnread)
                    .isInstanceOf(EmailReceiveException.class)
                    .hasMessageContaining("not configured");
        }

        @Test
        @DisplayName("receive() without configureReceiver throws EmailReceiveException")
        void testReceiveWithoutConfig() {
            EmailQuery query = EmailQuery.builder().build();
            assertThatThrownBy(() -> OpenEmail.receive(query))
                    .isInstanceOf(EmailReceiveException.class)
                    .hasMessageContaining("not configured");
        }

        @Test
        @DisplayName("email() without configure throws EmailException")
        void testEmailBuilderWithoutConfig() {
            assertThatThrownBy(OpenEmail::email)
                    .isInstanceOf(EmailException.class)
                    .hasMessageContaining("not configured");
        }
    }

    @Nested
    @DisplayName("TestConnectionTests")
    class TestConnectionTests {

        @Test
        @DisplayName("testConnection with non-SMTP sender returns success result")
        void testConnectionWithInMemorySender() {
            configureWithInMemorySender();

            ConnectionTestResult result = OpenEmail.testConnection();

            assertThat(result).isNotNull();
            assertThat(result.success()).isTrue();
            assertThat(result.serverGreeting()).isEqualTo("Non-SMTP sender configured");
            assertThat(result.latency()).isEqualTo(Duration.ZERO);
        }
    }
}
