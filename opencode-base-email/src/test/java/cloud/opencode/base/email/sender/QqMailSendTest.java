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
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * QQ Mail End-to-End Email Sending Tests
 * QQ 邮箱端到端邮件发送测试
 *
 * <p>Tests real email sending through QQ Mail SMTP server (smtp.qq.com).
 * Requires a QQ email account with SMTP service enabled and an authorization code.</p>
 * <p>通过 QQ 邮箱 SMTP 服务器 (smtp.qq.com) 测试真实邮件发送。
 * 需要开启了 SMTP 服务的 QQ 邮箱账号和授权码。</p>
 *
 * <h3>Setup | 设置步骤:</h3>
 * <ol>
 *   <li>Log in to QQ Mail → Settings → Account → POP3/IMAP/SMTP service → Enable</li>
 *   <li>Generate an authorization code (授权码)</li>
 *   <li>Set environment variables as shown below</li>
 * </ol>
 * <ol>
 *   <li>登录 QQ 邮箱 → 设置 → 账户 → POP3/IMAP/SMTP 服务 → 开启</li>
 *   <li>生成授权码</li>
 *   <li>设置下方环境变量</li>
 * </ol>
 *
 * <h3>Environment Variables | 环境变量:</h3>
 * <pre>{@code
 * export QQ_SMTP_USER=your_qq_number@qq.com
 * export QQ_SMTP_PASS=your_authorization_code   # 授权码，非QQ密码
 * export QQ_SMTP_TO=recipient@example.com        # 收件人（可以是自己）
 *
 * mvn test -pl opencode-base-email -Dtest="QqMailSendTest"
 * }</pre>
 *
 * <h3>QQ Mail SMTP Configuration | QQ 邮箱 SMTP 配置:</h3>
 * <ul>
 *   <li>Server: smtp.qq.com</li>
 *   <li>SSL Port: 465 (recommended) / STARTTLS Port: 587</li>
 *   <li>Auth: QQ email address + authorization code (授权码)</li>
 * </ul>
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.3
 */
@EnabledIfEnvironmentVariable(named = "QQ_SMTP_USER", matches = ".+@qq\\.com")
@EnabledIfEnvironmentVariable(named = "QQ_SMTP_PASS", matches = ".+")
@EnabledIfEnvironmentVariable(named = "QQ_SMTP_TO", matches = ".+")
@DisplayName("QQ 邮箱端到端发送测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QqMailSendTest {

    private static final String QQ_SMTP_HOST = "smtp.qq.com";
    private static final int QQ_SMTP_SSL_PORT = 465;

    private static String user;
    private static String pass;
    private static String to;
    private static EmailConfig config;
    private static SmtpEmailSender sender;

    @BeforeAll
    static void setUp() {
        user = System.getenv("QQ_SMTP_USER");
        pass = System.getenv("QQ_SMTP_PASS");
        to = System.getenv("QQ_SMTP_TO");

        // QQ Mail uses SSL on port 465
        config = EmailConfig.builder()
                .host(QQ_SMTP_HOST)
                .port(QQ_SMTP_SSL_PORT)
                .username(user)
                .password(pass)
                .ssl(true)
                .defaultFrom(user, "OpenCode 邮件测试")
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

    // ==================== 连接测试 ====================

    @Nested
    @DisplayName("连接测试")
    class ConnectionTests {

        @Test
        @Order(1)
        @DisplayName("测试 QQ 邮箱 SMTP 连接 (SSL 465)")
        void testConnection() {
            ConnectionTestResult result = sender.testConnection();

            assertThat(result.success())
                    .describedAs("QQ SMTP 连接应成功 (smtp.qq.com:465 SSL)")
                    .isTrue();
            assertThat(result.latency()).isPositive();
            assertThat(result.serverGreeting()).isNotBlank();

            System.out.println("[OK] QQ SMTP 连接成功");
            System.out.println("     服务器: " + QQ_SMTP_HOST + ":" + QQ_SMTP_SSL_PORT + " (SSL)");
            System.out.println("     延迟: " + result.latency().toMillis() + "ms");
            System.out.println("     问候语: " + result.serverGreeting());
        }

        @Test
        @Order(2)
        @DisplayName("通过 OpenEmail 门面测试连接")
        void testConnectionViaFacade() {
            ConnectionTestResult result = OpenEmail.testConnection();
            assertThat(result.success()).isTrue();
            System.out.println("[OK] OpenEmail.testConnection() 延迟: " + result.latency().toMillis() + "ms");
        }
    }

    // ==================== 基础发送测试 ====================

    @Nested
    @DisplayName("基础发送测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class BasicSendTests {

        @Test
        @Order(10)
        @DisplayName("发送纯文本邮件")
        void sendTextEmail() {
            Email email = Email.builder()
                    .from(user, "OpenCode 测试")
                    .to(to)
                    .subject("[OpenCode] 纯文本测试 - " + Instant.now().toEpochMilli())
                    .text("这是一封由 OpenCode Base Email 通过 QQ 邮箱发送的纯文本测试邮件。\n\n"
                            + "This is a plain text test email sent via QQ Mail by OpenCode Base Email.\n\n"
                            + "发送时间: " + Instant.now() + "\n"
                            + "发送账号: " + user)
                    .build();

            SendResult result = sender.sendWithResult(email);

            assertThat(result.success()).isTrue();
            assertThat(result.messageId()).isNotBlank();
            System.out.println("[OK] 纯文本邮件发送成功, Message-ID: " + result.messageId());
        }

        @Test
        @Order(20)
        @DisplayName("发送 HTML 邮件")
        void sendHtmlEmail() {
            Email email = Email.builder()
                    .from(user, "OpenCode 测试")
                    .to(to)
                    .subject("[OpenCode] HTML 测试 - " + Instant.now().toEpochMilli())
                    .html("""
                            <html>
                            <body style="font-family: 'Microsoft YaHei', Arial, sans-serif; padding: 20px; background: #f5f5f5;">
                                <div style="max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                                    <h1 style="color: #2563eb; border-bottom: 2px solid #2563eb; padding-bottom: 10px;">
                                        OpenCode Base Email
                                    </h1>
                                    <p style="font-size: 16px;">这是一封通过 <strong>QQ 邮箱 SMTP</strong> 发送的 HTML 测试邮件。</p>
                                    <p>This is an HTML test email sent via <strong>QQ Mail SMTP</strong>.</p>
                                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                                    <h3>功能测试 | Feature Test</h3>
                                    <table border="0" cellpadding="10" cellspacing="0" style="border-collapse: collapse; width: 100%%;">
                                        <tr style="background: #f8fafc;">
                                            <th style="text-align: left; border-bottom: 2px solid #e2e8f0;">功能</th>
                                            <th style="text-align: center; border-bottom: 2px solid #e2e8f0;">状态</th>
                                        </tr>
                                        <tr><td style="border-bottom: 1px solid #f0f0f0;">SMTP/SSL 连接</td><td style="text-align:center; color:green;">✅</td></tr>
                                        <tr><td style="border-bottom: 1px solid #f0f0f0;">HTML 内容</td><td style="text-align:center; color:green;">✅</td></tr>
                                        <tr><td style="border-bottom: 1px solid #f0f0f0;">中文支持</td><td style="text-align:center; color:green;">✅</td></tr>
                                        <tr><td style="border-bottom: 1px solid #f0f0f0;">QQ 邮箱兼容</td><td style="text-align:center; color:green;">✅</td></tr>
                                    </table>
                                    <p style="color: #999; font-size: 12px; margin-top: 20px;">
                                        发送时间: %s<br>
                                        Powered by OpenCode Base Email V1.0.3 (Zero Dependencies)
                                    </p>
                                </div>
                            </body>
                            </html>
                            """.formatted(Instant.now()))
                    .build();

            SendResult result = sender.sendWithResult(email);

            assertThat(result.success()).isTrue();
            System.out.println("[OK] HTML 邮件发送成功, Message-ID: " + result.messageId());
        }

        @Test
        @Order(30)
        @DisplayName("发送 Multipart/Alternative 邮件 (纯文本 + HTML 双格式)")
        void sendMultipartAlternativeEmail() {
            Email email = Email.builder()
                    .from(user, "OpenCode 测试")
                    .to(to)
                    .subject("[OpenCode] 双格式测试 - " + Instant.now().toEpochMilli())
                    .textAndHtml(
                            "=== OpenCode 双格式邮件测试 ===\n\n"
                                    + "这是纯文本版本。\n"
                                    + "如果你看到这个，说明你的邮件客户端不支持 HTML。\n\n"
                                    + "This is the plain text version.",
                            "<html><body style='font-family: Microsoft YaHei, sans-serif;'>"
                                    + "<h2 style='color:#2563eb;'>OpenCode 双格式邮件测试</h2>"
                                    + "<p>这是 <strong>HTML 版本</strong>。</p>"
                                    + "<p>邮件客户端会自动选择最佳格式显示。</p>"
                                    + "<p style='color:green;'>✅ Multipart/Alternative 工作正常!</p>"
                                    + "</body></html>"
                    )
                    .build();

            SendResult result = sender.sendWithResult(email);

            assertThat(result.success()).isTrue();
            System.out.println("[OK] 双格式邮件发送成功, Message-ID: " + result.messageId());
        }
    }

    // ==================== 附件测试 ====================

    @Nested
    @DisplayName("附件测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class AttachmentTests {

        @Test
        @Order(40)
        @DisplayName("发送带附件的邮件")
        void sendWithAttachment() {
            byte[] csvData = ("姓名,年龄,城市\n"
                    + "张三,30,北京\n"
                    + "李四,25,上海\n"
                    + "王五,28,深圳\n").getBytes(StandardCharsets.UTF_8);

            byte[] jsonData = """
                    {
                      "project": "opencode-base-email",
                      "version": "1.0.3",
                      "features": ["SMTP", "IMAP", "POP3", "MIME", "DKIM"]
                    }
                    """.getBytes(StandardCharsets.UTF_8);

            Email email = Email.builder()
                    .from(user, "OpenCode 测试")
                    .to(to)
                    .subject("[OpenCode] 附件测试 - " + Instant.now().toEpochMilli())
                    .html("<html><body>"
                            + "<h2>附件测试</h2>"
                            + "<p>本邮件包含 2 个附件：</p>"
                            + "<ol>"
                            + "<li><strong>测试数据.csv</strong> - CSV 格式测试数据</li>"
                            + "<li><strong>项目信息.json</strong> - JSON 格式项目信息</li>"
                            + "</ol>"
                            + "<p>请查收。</p>"
                            + "</body></html>")
                    .attach(ByteArrayAttachment.of("测试数据.csv", csvData, "text/csv"))
                    .attach(ByteArrayAttachment.of("项目信息.json", jsonData, "application/json"))
                    .build();

            SendResult result = sender.sendWithResult(email);

            assertThat(result.success()).isTrue();
            System.out.println("[OK] 带附件邮件发送成功 (2 个附件), Message-ID: " + result.messageId());
        }

        @Test
        @Order(50)
        @DisplayName("发送带内嵌图片的 HTML 邮件")
        void sendWithInlineImage() {
            // 1x1 red PNG pixel
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
                    .from(user, "OpenCode 测试")
                    .to(to)
                    .subject("[OpenCode] 内嵌图片测试 - " + Instant.now().toEpochMilli())
                    .html("<html><body style='font-family: Microsoft YaHei, sans-serif;'>"
                            + "<h2>内嵌图片测试</h2>"
                            + "<p>下方是通过 CID 引用的内嵌图片：</p>"
                            + "<div style='padding:20px; background:#f0f0f0; text-align:center;'>"
                            + "<img src=\"cid:test-logo\" alt=\"OpenCode Logo\" "
                            + "width=\"50\" height=\"50\" "
                            + "style=\"border: 2px solid #2563eb; border-radius: 8px;\" />"
                            + "</div>"
                            + "<p style='color:#666; font-size:12px;'>如果你能看到上方的图片，说明 CID 内嵌图片功能正常。</p>"
                            + "</body></html>")
                    .attach(InlineAttachment.of("test-logo", "logo.png", pngData, "image/png"))
                    .build();

            SendResult result = sender.sendWithResult(email);

            assertThat(result.success()).isTrue();
            System.out.println("[OK] 内嵌图片邮件发送成功, Message-ID: " + result.messageId());
        }
    }

    // ==================== 批量发送测试 ====================

    @Nested
    @DisplayName("批量发送测试")
    class BatchSendTests {

        @Test
        @Order(60)
        @DisplayName("单连接批量发送 3 封邮件")
        void batchSend() {
            long ts = Instant.now().toEpochMilli();
            List<Email> emails = List.of(
                    Email.builder()
                            .from(user, "OpenCode 测试")
                            .to(to)
                            .subject("[OpenCode] 批量 1/3 - " + ts)
                            .text("批量邮件测试 1/3\nBatch email 1 of 3")
                            .build(),
                    Email.builder()
                            .from(user, "OpenCode 测试")
                            .to(to)
                            .subject("[OpenCode] 批量 2/3 - " + ts)
                            .text("批量邮件测试 2/3\nBatch email 2 of 3")
                            .build(),
                    Email.builder()
                            .from(user, "OpenCode 测试")
                            .to(to)
                            .subject("[OpenCode] 批量 3/3 - " + ts)
                            .text("批量邮件测试 3/3\nBatch email 3 of 3")
                            .build()
            );

            BatchSendResult result = sender.sendBatch(emails);

            assertThat(result.totalCount()).isEqualTo(3);
            assertThat(result.successCount()).isEqualTo(3);
            assertThat(result.allSucceeded()).isTrue();
            System.out.println("[OK] 批量发送成功: " + result.successCount() + "/" + result.totalCount()
                    + ", 耗时: " + result.duration().toMillis() + "ms");
        }
    }

    // ==================== 异步发送测试 ====================

    @Nested
    @DisplayName("异步发送测试")
    class AsyncSendTests {

        @Test
        @Order(70)
        @DisplayName("异步发送邮件")
        void sendAsync() throws Exception {
            Email email = Email.builder()
                    .from(user, "OpenCode 测试")
                    .to(to)
                    .subject("[OpenCode] 异步发送测试 - " + Instant.now().toEpochMilli())
                    .text("这封邮件通过 CompletableFuture 异步发送。\nThis email was sent asynchronously.")
                    .build();

            CompletableFuture<Void> future = OpenEmail.sendAsync(email);
            future.get(30, TimeUnit.SECONDS);

            System.out.println("[OK] 异步发送成功");
        }
    }

    // ==================== 模板发送测试 ====================

    @Nested
    @DisplayName("模板发送测试")
    class TemplateSendTests {

        @Test
        @Order(80)
        @DisplayName("发送模板邮件 (变量替换)")
        void sendTemplateEmail() {
            String template = """
                    <html>
                    <body style="font-family: 'Microsoft YaHei', sans-serif; padding: 20px;">
                        <div style="max-width: 600px; margin: 0 auto; background: #fff; padding: 30px; border-radius: 8px; border: 1px solid #eee;">
                            <h1 style="color: #2563eb;">订单确认 | Order Confirmation</h1>
                            <p>尊敬的 <strong>${userName}</strong>，您好！</p>
                            <p>您的订单已确认，详情如下：</p>
                            <table border="0" cellpadding="8" style="border-collapse: collapse; margin: 15px 0;">
                                <tr style="background: #f8fafc;"><td style="border: 1px solid #e2e8f0;"><strong>订单号</strong></td><td style="border: 1px solid #e2e8f0;">${orderNo}</td></tr>
                                <tr><td style="border: 1px solid #e2e8f0;"><strong>商品</strong></td><td style="border: 1px solid #e2e8f0;">${productName}</td></tr>
                                <tr style="background: #f8fafc;"><td style="border: 1px solid #e2e8f0;"><strong>数量</strong></td><td style="border: 1px solid #e2e8f0;">${quantity}</td></tr>
                                <tr><td style="border: 1px solid #e2e8f0;"><strong>总价</strong></td><td style="border: 1px solid #e2e8f0; color: #e11d48; font-weight: bold;">¥${amount}</td></tr>
                            </table>
                            <p>感谢您的购买！</p>
                            <hr style="border: none; border-top: 1px solid #eee;">
                            <p style="color: #999; font-size: 12px;">
                                此邮件由 OpenCode Base Email V1.0.3 模板引擎生成<br>
                                Powered by opencode-base-email (Zero Dependencies)
                            </p>
                        </div>
                    </body>
                    </html>
                    """;

            OpenEmail.sendTemplate(to,
                    "[OpenCode] 订单确认 #ORD-" + Instant.now().toEpochMilli(),
                    template,
                    Map.of(
                            "userName", "测试用户",
                            "orderNo", "ORD-" + Instant.now().toEpochMilli(),
                            "productName", "OpenCode Base 全模块套件",
                            "quantity", "1",
                            "amount", "999.00"
                    ));

            System.out.println("[OK] 模板邮件发送成功");
        }
    }

    // ==================== 特殊场景测试 ====================

    @Nested
    @DisplayName("特殊场景测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class SpecialTests {

        @Test
        @Order(90)
        @DisplayName("发送含自定义头和高优先级的邮件")
        void sendWithCustomHeaders() {
            Email email = Email.builder()
                    .from(user, "OpenCode 测试")
                    .to(to)
                    .subject("[OpenCode] 高优先级 - " + Instant.now().toEpochMilli())
                    .text("这是一封高优先级邮件，带有自定义头。\nThis is a high priority email with custom headers.")
                    .priority(Email.Priority.HIGH)
                    .header("X-OpenCode-Module", "opencode-base-email")
                    .header("X-OpenCode-Version", "1.0.3")
                    .replyTo(user)
                    .build();

            SendResult result = sender.sendWithResult(email);

            assertThat(result.success()).isTrue();
            System.out.println("[OK] 高优先级邮件发送成功, Message-ID: " + result.messageId());
        }

        @Test
        @Order(100)
        @DisplayName("发送抄送邮件 (CC 给自己)")
        void sendWithCc() {
            Email email = Email.builder()
                    .from(user, "OpenCode 测试")
                    .to(to)
                    .cc(user)
                    .subject("[OpenCode] 抄送测试 - " + Instant.now().toEpochMilli())
                    .text("这封邮件抄送给发件人自己。\nThis email is CC'd to the sender.")
                    .build();

            SendResult result = sender.sendWithResult(email);

            assertThat(result.success()).isTrue();
            System.out.println("[OK] 抄送邮件发送成功");
        }

        @Test
        @Order(110)
        @DisplayName("通过 OpenEmail 门面快速发送")
        void sendViaFacade() {
            OpenEmail.sendText(to,
                    "[OpenCode] 门面快发 - " + Instant.now().toEpochMilli(),
                    "通过 OpenEmail.sendText() 一行代码发送。\nSent via OpenEmail.sendText() in one line.");

            System.out.println("[OK] OpenEmail.sendText() 发送成功");
        }
    }

    // ==================== 完整场景测试 ====================

    @Nested
    @DisplayName("完整场景测试")
    class FullScenarioTests {

        @Test
        @Order(200)
        @DisplayName("全功能组合: 双格式 + 附件 + 内嵌图 + 抄送 + 优先级 + 自定义头")
        void fullScenario() {
            byte[] txtAttachment = ("OpenCode Base Email 功能测试报告\n"
                    + "============================\n\n"
                    + "测试时间: " + Instant.now() + "\n"
                    + "测试环境: QQ 邮箱 (smtp.qq.com:465 SSL)\n"
                    + "协议实现: 纯 JDK，零外部依赖\n\n"
                    + "测试项目:\n"
                    + "  [✓] SMTP SSL 连接\n"
                    + "  [✓] 用户名/授权码认证\n"
                    + "  [✓] 纯文本邮件\n"
                    + "  [✓] HTML 邮件\n"
                    + "  [✓] Multipart/Alternative\n"
                    + "  [✓] 文件附件\n"
                    + "  [✓] 内嵌图片 (CID)\n"
                    + "  [✓] 批量发送\n"
                    + "  [✓] 异步发送\n"
                    + "  [✓] 模板引擎\n"
                    + "  [✓] 自定义头\n"
                    + "  [✓] 优先级\n"
                    + "  [✓] 抄送/回复\n\n"
                    + "结论: 全部通过 ✅\n").getBytes(StandardCharsets.UTF_8);

            // 1x1 blue PNG pixel
            byte[] pngPixel = new byte[]{
                    (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                    0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
                    0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
                    0x08, 0x02, 0x00, 0x00, 0x00, (byte) 0x90, 0x77, 0x53,
                    (byte) 0xDE, 0x00, 0x00, 0x00, 0x0C, 0x49, 0x44, 0x41,
                    0x54, 0x08, (byte) 0xD7, 0x63, (byte) 0x60, 0x60, (byte) 0xF8,
                    0x0F, 0x00, 0x00, 0x02, 0x00, 0x01, (byte) 0xE2, 0x21,
                    (byte) 0xBC, 0x33, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45,
                    0x4E, 0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82
            };

            Email email = Email.builder()
                    .from(user, "OpenCode 全功能测试")
                    .to(to)
                    .cc(user)
                    .subject("[OpenCode] ★ 全功能测试 - " + Instant.now().toEpochMilli())
                    .textAndHtml(
                            "=== OpenCode Base Email 全功能测试 ===\n\n"
                                    + "这是纯文本版本。\n"
                                    + "完整功能: 双格式 + 附件 + 内嵌图 + 抄送 + 优先级 + 自定义头 + 回复地址\n\n"
                                    + "发送时间: " + Instant.now(),
                            """
                                    <html>
                                    <body style="font-family: 'Microsoft YaHei', Arial, sans-serif; padding: 20px; background: #f5f5f5;">
                                        <div style="max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                                            <div style="text-align: center; margin-bottom: 20px;">
                                                <img src="cid:badge" width="24" height="24" style="vertical-align: middle;" />
                                                <span style="font-size: 24px; font-weight: bold; color: #2563eb; vertical-align: middle;">
                                                    OpenCode Base Email
                                                </span>
                                            </div>
                                            <h2 style="color: #1e293b;">全功能组合测试 ✅</h2>
                                            <p>本邮件测试了 <strong>所有邮件功能</strong> 的组合使用：</p>
                                            <table border="0" cellpadding="6" cellspacing="0" style="border-collapse: collapse; width: 100%%;">
                                                <tr style="background:#eff6ff;"><td style="border:1px solid #dbeafe;">📧 Multipart/Alternative</td><td style="border:1px solid #dbeafe; color:green; text-align:center;">✅</td></tr>
                                                <tr><td style="border:1px solid #dbeafe;">📎 文件附件 (测试报告.txt)</td><td style="border:1px solid #dbeafe; color:green; text-align:center;">✅</td></tr>
                                                <tr style="background:#eff6ff;"><td style="border:1px solid #dbeafe;">🖼️ 内嵌图片 (CID 引用)</td><td style="border:1px solid #dbeafe; color:green; text-align:center;">✅</td></tr>
                                                <tr><td style="border:1px solid #dbeafe;">📋 抄送 (CC)</td><td style="border:1px solid #dbeafe; color:green; text-align:center;">✅</td></tr>
                                                <tr style="background:#eff6ff;"><td style="border:1px solid #dbeafe;">⚡ 高优先级</td><td style="border:1px solid #dbeafe; color:green; text-align:center;">✅</td></tr>
                                                <tr><td style="border:1px solid #dbeafe;">🔧 自定义 Header</td><td style="border:1px solid #dbeafe; color:green; text-align:center;">✅</td></tr>
                                                <tr style="background:#eff6ff;"><td style="border:1px solid #dbeafe;">↩️ Reply-To 回复地址</td><td style="border:1px solid #dbeafe; color:green; text-align:center;">✅</td></tr>
                                            </table>
                                            <hr style="border:none; border-top:1px solid #eee; margin:20px 0;">
                                            <p style="color: #64748b; font-size: 12px; text-align: center;">
                                                Powered by <strong>opencode-base-email V1.0.3</strong><br>
                                                零外部依赖 · 纯 JDK 实现 · SMTP/IMAP/POP3<br>
                                                发送时间: %s
                                            </p>
                                        </div>
                                    </body>
                                    </html>
                                    """.formatted(Instant.now())
                    )
                    .attach(ByteArrayAttachment.of("测试报告.txt", txtAttachment, "text/plain"))
                    .attach(InlineAttachment.of("badge", "badge.png", pngPixel, "image/png"))
                    .priority(Email.Priority.HIGH)
                    .header("X-OpenCode-Test", "full-scenario")
                    .header("X-OpenCode-Version", "1.0.3")
                    .header("X-Mail-Provider", "QQ Mail")
                    .replyTo(user)
                    .build();

            SendResult result = sender.sendWithResult(email);

            assertThat(result.success()).isTrue();
            assertThat(result.messageId()).isNotBlank();

            System.out.println("╔══════════════════════════════════════════╗");
            System.out.println("║  [OK] QQ 邮箱全功能测试通过!              ║");
            System.out.println("╠══════════════════════════════════════════╣");
            System.out.println("║  Message-ID: " + result.messageId());
            System.out.println("║  功能: 双格式+附件+内嵌图+CC+优先级+头+Reply-To");
            System.out.println("║  协议: SMTP SSL (smtp.qq.com:465)       ║");
            System.out.println("║  依赖: 零 (纯 JDK Socket 实现)           ║");
            System.out.println("╚══════════════════════════════════════════╝");
        }
    }
}
