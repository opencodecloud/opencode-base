package cloud.opencode.base.email.sender;

import cloud.opencode.base.email.BatchSendResult;
import cloud.opencode.base.email.ConnectionTestResult;
import cloud.opencode.base.email.Email;
import cloud.opencode.base.email.EmailConfig;
import cloud.opencode.base.email.OpenEmail;
import cloud.opencode.base.email.SendResult;
import cloud.opencode.base.email.attachment.ByteArrayAttachment;
import cloud.opencode.base.email.attachment.InlineAttachment;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Real SMTP End-to-End Email Sending Tests
 * 真实 SMTP 端到端邮件发送测试
 *
 * <p>These tests send actual emails through a real SMTP server.
 * They are disabled by default and only run when the required
 * environment variables are set.</p>
 * <p>这些测试通过真实 SMTP 服务器发送实际邮件。
 * 默认禁用，仅在设置了所需环境变量时运行。</p>
 *
 * <h3>Environment Variables | 环境变量:</h3>
 * <ul>
 *   <li>{@code SMTP_HOST} — SMTP server host (e.g., smtp.gmail.com)</li>
 *   <li>{@code SMTP_PORT} — SMTP server port (e.g., 587)</li>
 *   <li>{@code SMTP_USER} — SMTP username / email address</li>
 *   <li>{@code SMTP_PASS} — SMTP password / app password</li>
 *   <li>{@code SMTP_TO}   — Recipient email address for test emails</li>
 * </ul>
 *
 * <h3>Example | 示例:</h3>
 * <pre>{@code
 * # Gmail (requires App Password)
 * export SMTP_HOST=smtp.gmail.com
 * export SMTP_PORT=587
 * export SMTP_USER=your@gmail.com
 * export SMTP_PASS=xxxx-xxxx-xxxx-xxxx
 * export SMTP_TO=recipient@example.com
 *
 * mvn test -pl opencode-base-email -Dtest="RealSmtpSendTest"
 * }</pre>
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.3
 */
@EnabledIfEnvironmentVariable(named = "SMTP_HOST", matches = ".+")
@EnabledIfEnvironmentVariable(named = "SMTP_USER", matches = ".+")
@EnabledIfEnvironmentVariable(named = "SMTP_PASS", matches = ".+")
@EnabledIfEnvironmentVariable(named = "SMTP_TO", matches = ".+")
@DisplayName("Real SMTP 端到端邮件发送测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RealSmtpSendTest {

    private static String host;
    private static int port;
    private static String user;
    private static String pass;
    private static String to;
    private static EmailConfig config;
    private static SmtpEmailSender sender;

    @BeforeAll
    static void setUp() {
        host = System.getenv("SMTP_HOST");
        port = Integer.parseInt(System.getenv().getOrDefault("SMTP_PORT", "587"));
        user = System.getenv("SMTP_USER");
        pass = System.getenv("SMTP_PASS");
        to = System.getenv("SMTP_TO");

        // Auto-detect SSL vs STARTTLS based on port
        boolean useSsl = (port == 465);
        boolean useStarttls = (port == 587 || port == 25);

        config = EmailConfig.builder()
                .host(host)
                .port(port)
                .username(user)
                .password(pass)
                .ssl(useSsl)
                .starttls(useStarttls)
                .defaultFrom(user, "OpenCode Email Test")
                .timeout(Duration.ofSeconds(30))
                .connectionTimeout(Duration.ofSeconds(15))
                .build();

        sender = new SmtpEmailSender(config);
        OpenEmail.configure(config);
    }

    @AfterAll
    static void tearDown() {
        OpenEmail.shutdown();
    }

    // ==================== Connection Tests ====================

    @Nested
    @DisplayName("连接测试")
    class ConnectionTests {

        @Test
        @Order(1)
        @DisplayName("Test SMTP connection")
        void testConnection() {
            ConnectionTestResult result = sender.testConnection();

            assertThat(result.success()).isTrue();
            assertThat(result.latency()).isPositive();
            assertThat(result.serverGreeting()).isNotBlank();
            System.out.println("[OK] Connected to " + host + ":" + port
                    + " in " + result.latency().toMillis() + "ms");
            System.out.println("     Greeting: " + result.serverGreeting());
        }
    }

    // ==================== Send Tests ====================

    @Nested
    @DisplayName("发送测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class SendTests {

        @Test
        @Order(10)
        @DisplayName("Send plain text email")
        void sendTextEmail() {
            Email email = Email.builder()
                    .from(user, "OpenCode Test")
                    .to(to)
                    .subject("[OpenCode Test] Plain Text - " + System.currentTimeMillis())
                    .text("This is a plain text email sent by OpenCode Base Email.\n\n"
                            + "这是一封由 OpenCode Base Email 发送的纯文本测试邮件。\n\n"
                            + "Timestamp: " + java.time.Instant.now())
                    .build();

            SendResult result = sender.sendWithResult(email);

            assertThat(result.success()).isTrue();
            assertThat(result.messageId()).isNotBlank();
            System.out.println("[OK] Text email sent, Message-ID: " + result.messageId());
        }

        @Test
        @Order(20)
        @DisplayName("Send HTML email")
        void sendHtmlEmail() {
            Email email = Email.builder()
                    .from(user, "OpenCode Test")
                    .to(to)
                    .subject("[OpenCode Test] HTML Email - " + System.currentTimeMillis())
                    .html("""
                            <html>
                            <body style="font-family: Arial, sans-serif; padding: 20px;">
                                <h1 style="color: #2563eb;">OpenCode Base Email</h1>
                                <p>This is an <strong>HTML email</strong> sent by OpenCode Base Email.</p>
                                <p>这是一封由 OpenCode Base Email 发送的 <em>HTML 测试邮件</em>。</p>
                                <hr>
                                <table border="1" cellpadding="8" style="border-collapse: collapse;">
                                    <tr><th>Feature</th><th>Status</th></tr>
                                    <tr><td>SMTP</td><td style="color:green;">✓</td></tr>
                                    <tr><td>STARTTLS</td><td style="color:green;">✓</td></tr>
                                    <tr><td>HTML Content</td><td style="color:green;">✓</td></tr>
                                </table>
                                <p style="color: #666; font-size: 12px;">
                                    Sent at: %s
                                </p>
                            </body>
                            </html>
                            """.formatted(java.time.Instant.now()))
                    .build();

            SendResult result = sender.sendWithResult(email);

            assertThat(result.success()).isTrue();
            assertThat(result.messageId()).isNotBlank();
            System.out.println("[OK] HTML email sent, Message-ID: " + result.messageId());
        }

        @Test
        @Order(30)
        @DisplayName("Send multipart/alternative email (text + HTML)")
        void sendMultipartAlternativeEmail() {
            Email email = Email.builder()
                    .from(user, "OpenCode Test")
                    .to(to)
                    .subject("[OpenCode Test] Multipart/Alternative - " + System.currentTimeMillis())
                    .textAndHtml(
                            "This is the PLAIN TEXT version.\n这是纯文本版本。",
                            "<html><body><h2>This is the <em>HTML</em> version.</h2>"
                                    + "<p>这是 HTML 版本。</p></body></html>"
                    )
                    .build();

            SendResult result = sender.sendWithResult(email);

            assertThat(result.success()).isTrue();
            System.out.println("[OK] Multipart/alternative email sent, Message-ID: " + result.messageId());
        }

        @Test
        @Order(40)
        @DisplayName("Send email with attachment")
        void sendWithAttachment() {
            byte[] csvData = "Name,Age,City\nAlice,30,Beijing\nBob,25,Shanghai\n"
                    .getBytes(StandardCharsets.UTF_8);

            Email email = Email.builder()
                    .from(user, "OpenCode Test")
                    .to(to)
                    .subject("[OpenCode Test] With Attachment - " + System.currentTimeMillis())
                    .text("Please find the attached CSV file.\n请查看附件中的 CSV 文件。")
                    .attach(ByteArrayAttachment.of("test-data.csv", csvData, "text/csv"))
                    .build();

            SendResult result = sender.sendWithResult(email);

            assertThat(result.success()).isTrue();
            System.out.println("[OK] Email with attachment sent, Message-ID: " + result.messageId());
        }

        @Test
        @Order(50)
        @DisplayName("Send email with inline image (embedded HTML)")
        void sendWithInlineImage() {
            // Create a simple 1x1 red PNG pixel
            byte[] pngData = new byte[]{
                    (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                    0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
                    0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
                    0x08, 0x02, 0x00, 0x00, 0x00, (byte) 0x90, 0x77, 0x53,
                    (byte) 0xDE, 0x00, 0x00, 0x00, 0x0C, 0x49, 0x44, 0x41,
                    0x54, 0x08, (byte) 0xD7, 0x63, (byte) 0xF8, (byte) 0xCF, (byte) 0xC0,
                    0x00, 0x00, 0x00, 0x02, 0x00, 0x01, (byte) 0xE2, 0x21,
                    (byte) 0xBC, 0x33, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45,
                    0x4E, 0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82
            };

            Email email = Email.builder()
                    .from(user, "OpenCode Test")
                    .to(to)
                    .subject("[OpenCode Test] Inline Image - " + System.currentTimeMillis())
                    .html("<html><body>"
                            + "<h2>Email with Inline Image</h2>"
                            + "<p>Below is an embedded image:</p>"
                            + "<img src=\"cid:test-image\" alt=\"test\" width=\"50\" height=\"50\" />"
                            + "<p>邮件内嵌图片测试</p>"
                            + "</body></html>")
                    .attach(InlineAttachment.of("test-image", "test-pixel.png", pngData, "image/png"))
                    .build();

            SendResult result = sender.sendWithResult(email);

            assertThat(result.success()).isTrue();
            System.out.println("[OK] Inline image email sent, Message-ID: " + result.messageId());
        }

        @Test
        @Order(60)
        @DisplayName("Send email with custom headers and priority")
        void sendWithCustomHeaders() {
            Email email = Email.builder()
                    .from(user, "OpenCode Test")
                    .to(to)
                    .subject("[OpenCode Test] Custom Headers - " + System.currentTimeMillis())
                    .text("This email has custom headers and high priority.\n这封邮件有自定义头和高优先级。")
                    .priority(Email.Priority.HIGH)
                    .header("X-OpenCode-Test", "true")
                    .header("X-Test-Timestamp", String.valueOf(System.currentTimeMillis()))
                    .replyTo(user)
                    .build();

            SendResult result = sender.sendWithResult(email);

            assertThat(result.success()).isTrue();
            System.out.println("[OK] Custom headers email sent, Message-ID: " + result.messageId());
        }

        @Test
        @Order(70)
        @DisplayName("Send email with multiple recipients (CC)")
        void sendWithCc() {
            Email email = Email.builder()
                    .from(user, "OpenCode Test")
                    .to(to)
                    .cc(user) // CC to self
                    .subject("[OpenCode Test] CC Test - " + System.currentTimeMillis())
                    .text("This email is CC'd to the sender.\n这封邮件抄送给发件人。")
                    .build();

            SendResult result = sender.sendWithResult(email);

            assertThat(result.success()).isTrue();
            System.out.println("[OK] CC email sent, Message-ID: " + result.messageId());
        }
    }

    // ==================== Batch Send Tests ====================

    @Nested
    @DisplayName("批量发送测试")
    class BatchSendTests {

        @Test
        @Order(80)
        @DisplayName("Batch send 3 emails in single connection")
        void batchSend() {
            List<Email> emails = List.of(
                    Email.builder()
                            .from(user, "OpenCode Test")
                            .to(to)
                            .subject("[OpenCode Test] Batch 1/3 - " + System.currentTimeMillis())
                            .text("Batch email 1 of 3")
                            .build(),
                    Email.builder()
                            .from(user, "OpenCode Test")
                            .to(to)
                            .subject("[OpenCode Test] Batch 2/3 - " + System.currentTimeMillis())
                            .text("Batch email 2 of 3")
                            .build(),
                    Email.builder()
                            .from(user, "OpenCode Test")
                            .to(to)
                            .subject("[OpenCode Test] Batch 3/3 - " + System.currentTimeMillis())
                            .text("Batch email 3 of 3")
                            .build()
            );

            BatchSendResult result = sender.sendBatch(emails);

            assertThat(result.totalCount()).isEqualTo(3);
            assertThat(result.successCount()).isEqualTo(3);
            assertThat(result.failureCount()).isZero();
            assertThat(result.allSucceeded()).isTrue();
            assertThat(result.duration()).isPositive();
            System.out.println("[OK] Batch sent " + result.successCount() + "/" + result.totalCount()
                    + " in " + result.duration().toMillis() + "ms");
        }
    }

    // ==================== Async Send Tests ====================

    @Nested
    @DisplayName("异步发送测试")
    class AsyncSendTests {

        @Test
        @Order(90)
        @DisplayName("Send email asynchronously")
        void sendAsync() throws Exception {
            Email email = Email.builder()
                    .from(user, "OpenCode Test")
                    .to(to)
                    .subject("[OpenCode Test] Async Send - " + System.currentTimeMillis())
                    .text("This email was sent asynchronously.\n这封邮件是异步发送的。")
                    .build();

            CompletableFuture<Void> future = OpenEmail.sendAsync(email);
            future.get(30, TimeUnit.SECONDS);

            System.out.println("[OK] Async email sent successfully");
        }
    }

    // ==================== Template Send Tests ====================

    @Nested
    @DisplayName("模板发送测试")
    class TemplateSendTests {

        @Test
        @Order(100)
        @DisplayName("Send template email with variable substitution")
        void sendTemplateEmail() {
            String template = """
                    <html>
                    <body style="font-family: Arial, sans-serif; padding: 20px;">
                        <h1>Hello ${userName}!</h1>
                        <p>Your order <strong>#${orderNo}</strong> has been confirmed.</p>
                        <p>Total: <strong>$${amount}</strong></p>
                        <p>尊敬的 ${userName}，您的订单 #${orderNo} 已确认。</p>
                        <hr>
                        <p style="color: #666;">Sent by OpenCode Base Email</p>
                    </body>
                    </html>
                    """;

            OpenEmail.sendTemplate(to,
                    "[OpenCode Test] Template Email - " + System.currentTimeMillis(),
                    template,
                    Map.of(
                            "userName", "Test User",
                            "orderNo", "ORD-" + System.currentTimeMillis(),
                            "amount", "99.99"
                    ));

            System.out.println("[OK] Template email sent successfully");
        }
    }

    // ==================== OpenEmail Facade Tests ====================

    @Nested
    @DisplayName("OpenEmail 门面测试")
    class OpenEmailFacadeTests {

        @Test
        @Order(110)
        @DisplayName("Send via OpenEmail.sendText()")
        void sendViaFacadeText() {
            OpenEmail.sendText(to,
                    "[OpenCode Test] Facade Text - " + System.currentTimeMillis(),
                    "Sent via OpenEmail.sendText() facade.\n通过 OpenEmail.sendText() 门面发送。");

            System.out.println("[OK] OpenEmail.sendText() succeeded");
        }

        @Test
        @Order(120)
        @DisplayName("Send via OpenEmail.sendHtml()")
        void sendViaFacadeHtml() {
            OpenEmail.sendHtml(to,
                    "[OpenCode Test] Facade HTML - " + System.currentTimeMillis(),
                    "<html><body><h2>Sent via OpenEmail.sendHtml()</h2>"
                            + "<p>通过 OpenEmail.sendHtml() 门面发送</p></body></html>");

            System.out.println("[OK] OpenEmail.sendHtml() succeeded");
        }

        @Test
        @Order(130)
        @DisplayName("Send via OpenEmail.sendWithResult()")
        void sendViaFacadeWithResult() {
            Email email = OpenEmail.email()
                    .to(to)
                    .subject("[OpenCode Test] Facade Result - " + System.currentTimeMillis())
                    .text("Sent via OpenEmail.sendWithResult()")
                    .build();

            SendResult result = OpenEmail.sendWithResult(email);

            assertThat(result.success()).isTrue();
            assertThat(result.messageId()).isNotBlank();
            System.out.println("[OK] OpenEmail.sendWithResult() Message-ID: " + result.messageId());
        }

        @Test
        @Order(140)
        @DisplayName("Test connection via OpenEmail.testConnection()")
        void testConnectionViaFacade() {
            ConnectionTestResult result = OpenEmail.testConnection();

            assertThat(result.success()).isTrue();
            System.out.println("[OK] OpenEmail.testConnection() latency: "
                    + result.latency().toMillis() + "ms");
        }
    }

    // ==================== Complete Scenario Test ====================

    @Nested
    @DisplayName("完整场景测试")
    class CompleteScenarioTests {

        @Test
        @Order(200)
        @DisplayName("Full scenario: text + HTML + attachment + inline image + CC + priority + headers")
        void fullScenario() {
            byte[] attachmentData = ("This is a test attachment content.\n"
                    + "测试附件内容。\n"
                    + "Line 3\nLine 4\n").getBytes(StandardCharsets.UTF_8);

            // 1x1 transparent PNG
            byte[] pngPixel = new byte[]{
                    (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                    0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
                    0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
                    0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4,
                    (byte) 0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41,
                    0x54, 0x78, (byte) 0x9C, 0x62, 0x00, 0x00, 0x00, 0x02,
                    0x00, 0x01, (byte) 0xE5, 0x27, (byte) 0xDE, (byte) 0xFC,
                    0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44,
                    (byte) 0xAE, 0x42, 0x60, (byte) 0x82
            };

            Email email = Email.builder()
                    .from(user, "OpenCode Full Test")
                    .to(to)
                    .cc(user)
                    .subject("[OpenCode Test] FULL SCENARIO - " + System.currentTimeMillis())
                    .textAndHtml(
                            "=== FULL SCENARIO TEST ===\n\n"
                                    + "This email tests ALL features:\n"
                                    + "- Multipart/alternative (text + HTML)\n"
                                    + "- File attachment\n"
                                    + "- Inline image\n"
                                    + "- CC recipient\n"
                                    + "- High priority\n"
                                    + "- Custom headers\n"
                                    + "- Reply-To\n\n"
                                    + "完整场景测试：所有功能组合",
                            "<html><body style='font-family:Arial;padding:20px;'>"
                                    + "<h1 style='color:#2563eb;'>Full Scenario Test</h1>"
                                    + "<h2>完整场景测试</h2>"
                                    + "<p>This email tests <strong>ALL</strong> features:</p>"
                                    + "<ul>"
                                    + "<li>Multipart/alternative (text + HTML)</li>"
                                    + "<li>File attachment (test-data.txt)</li>"
                                    + "<li>Inline image: <img src='cid:logo' width='16' height='16'/></li>"
                                    + "<li>CC recipient</li>"
                                    + "<li>High priority</li>"
                                    + "<li>Custom headers</li>"
                                    + "<li>Reply-To</li>"
                                    + "</ul>"
                                    + "<p style='color:green;font-weight:bold;'>ALL FEATURES WORKING!</p>"
                                    + "</body></html>"
                    )
                    .attach(ByteArrayAttachment.of("test-data.txt", attachmentData, "text/plain"))
                    .attach(InlineAttachment.of("logo", "logo.png", pngPixel, "image/png"))
                    .priority(Email.Priority.HIGH)
                    .header("X-OpenCode-Test", "full-scenario")
                    .header("X-Test-ID", String.valueOf(System.currentTimeMillis()))
                    .replyTo(user)
                    .build();

            SendResult result = sender.sendWithResult(email);

            assertThat(result.success()).isTrue();
            assertThat(result.messageId()).isNotBlank();
            System.out.println("[OK] FULL SCENARIO email sent successfully!");
            System.out.println("     Message-ID: " + result.messageId());
            System.out.println("     Features: text+HTML, attachment, inline image, CC, priority, headers, reply-to");
        }
    }
}
