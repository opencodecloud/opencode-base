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
 * Alibaba Cloud Enterprise Mail End-to-End Sending Tests
 * 阿里云企业邮箱端到端邮件发送测试
 *
 * <p>Tests real email sending through Alibaba Cloud Enterprise Mail SMTP server.
 * Requires an Alibaba Cloud enterprise email account.</p>
 * <p>通过阿里云企业邮箱 SMTP 服务器测试真实邮件发送。
 * 需要阿里云企业邮箱账号。</p>
 *
 * <h3>Alibaba Cloud Enterprise Mail SMTP | 阿里云企业邮箱 SMTP 配置:</h3>
 * <ul>
 *   <li>Server: smtp.qiye.aliyun.com (enterprise) / smtp.aliyun.com (personal)</li>
 *   <li>SSL Port: 465 (recommended)</li>
 *   <li>STARTTLS Port: 25 or 80</li>
 *   <li>Auth: full email address + password</li>
 * </ul>
 *
 * <h3>Environment Variables | 环境变量:</h3>
 * <pre>{@code
 * # 阿里云企业邮箱
 * export ALI_SMTP_HOST=smtp.qiye.aliyun.com      # 企业邮箱
 * # export ALI_SMTP_HOST=smtp.aliyun.com          # 个人邮箱
 * export ALI_SMTP_PORT=465                         # SSL 端口
 * export ALI_SMTP_USER=user@your-domain.com        # 完整邮箱地址
 * export ALI_SMTP_PASS=your_password               # 邮箱密码
 * export ALI_SMTP_TO=recipient@example.com          # 收件人
 *
 * mvn test -pl opencode-base-email -Dtest="AliMailSendTest"
 * }</pre>
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.3
 */
@EnabledIfEnvironmentVariable(named = "ALI_SMTP_USER", matches = ".+")
@EnabledIfEnvironmentVariable(named = "ALI_SMTP_PASS", matches = ".+")
@EnabledIfEnvironmentVariable(named = "ALI_SMTP_TO", matches = ".+")
@DisplayName("阿里云企业邮箱端到端发送测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AliMailSendTest {

    private static String host;
    private static int port;
    private static String user;
    private static String pass;
    private static String to;
    private static EmailConfig config;
    private static SmtpEmailSender sender;

    @BeforeAll
    static void setUp() {
        host = System.getenv().getOrDefault("ALI_SMTP_HOST", "smtp.qiye.aliyun.com");
        port = Integer.parseInt(System.getenv().getOrDefault("ALI_SMTP_PORT", "465"));
        user = System.getenv("ALI_SMTP_USER");
        pass = System.getenv("ALI_SMTP_PASS");
        to = System.getenv("ALI_SMTP_TO");

        boolean useSsl = (port == 465);
        boolean useStarttls = (port == 25 || port == 80 || port == 587);

        config = EmailConfig.builder()
                .host(host)
                .port(port)
                .username(user)
                .password(pass)
                .ssl(useSsl)
                .starttls(useStarttls)
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
        @DisplayName("测试阿里云企业邮箱 SMTP 连接")
        void testConnection() {
            ConnectionTestResult result = sender.testConnection();

            assertThat(result.success())
                    .describedAs("阿里云 SMTP 连接应成功 (%s:%d)", host, port)
                    .isTrue();
            assertThat(result.latency()).isPositive();
            assertThat(result.serverGreeting()).isNotBlank();

            System.out.println("[OK] 阿里云企业邮箱 SMTP 连接成功");
            System.out.println("     服务器: " + host + ":" + port + (port == 465 ? " (SSL)" : " (STARTTLS)"));
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
                    .subject("[OpenCode·阿里云] 纯文本测试 - " + Instant.now().toEpochMilli())
                    .text("这是一封由 OpenCode Base Email 通过阿里云企业邮箱发送的纯文本测试邮件。\n\n"
                            + "This is a plain text test email sent via Alibaba Cloud Enterprise Mail.\n\n"
                            + "发送时间: " + Instant.now() + "\n"
                            + "发送账号: " + user + "\n"
                            + "SMTP 服务器: " + host + ":" + port)
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
            String domain = user.contains("@") ? user.substring(user.indexOf('@') + 1) : "unknown";
            Email email = Email.builder()
                    .from(user, "OpenCode 测试")
                    .to(to)
                    .subject("[OpenCode·阿里云] HTML 测试 - " + Instant.now().toEpochMilli())
                    .html("""
                            <html>
                            <body style="font-family: 'Microsoft YaHei', Arial, sans-serif; padding: 20px; background: #f5f5f5;">
                                <div style="max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                                    <h1 style="color: #ff6a00; border-bottom: 2px solid #ff6a00; padding-bottom: 10px;">
                                        阿里云企业邮箱 × OpenCode
                                    </h1>
                                    <p style="font-size: 16px;">这是一封通过 <strong>阿里云企业邮箱</strong> 发送的 HTML 测试邮件。</p>
                                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                                    <table border="0" cellpadding="10" style="border-collapse: collapse; width: 100%%;">
                                        <tr style="background: #fff7ed;">
                                            <th style="text-align:left; border-bottom: 2px solid #fed7aa;">配置项</th>
                                            <th style="text-align:left; border-bottom: 2px solid #fed7aa;">值</th>
                                        </tr>
                                        <tr><td style="border-bottom:1px solid #f0f0f0;">SMTP 服务器</td><td style="border-bottom:1px solid #f0f0f0;">%s</td></tr>
                                        <tr><td style="border-bottom:1px solid #f0f0f0;">端口</td><td style="border-bottom:1px solid #f0f0f0;">%d (%s)</td></tr>
                                        <tr><td style="border-bottom:1px solid #f0f0f0;">发件域名</td><td style="border-bottom:1px solid #f0f0f0;">%s</td></tr>
                                        <tr><td style="border-bottom:1px solid #f0f0f0;">协议实现</td><td style="border-bottom:1px solid #f0f0f0;">纯 JDK，零外部依赖</td></tr>
                                    </table>
                                    <p style="color: #999; font-size: 12px; margin-top: 20px;">
                                        发送时间: %s<br>
                                        Powered by OpenCode Base Email V1.0.3
                                    </p>
                                </div>
                            </body>
                            </html>
                            """.formatted(host, port, port == 465 ? "SSL" : "STARTTLS", domain, Instant.now()))
                    .build();

            SendResult result = sender.sendWithResult(email);

            assertThat(result.success()).isTrue();
            System.out.println("[OK] HTML 邮件发送成功, Message-ID: " + result.messageId());
        }

        @Test
        @Order(30)
        @DisplayName("发送 Multipart/Alternative 双格式邮件")
        void sendMultipartAlternativeEmail() {
            Email email = Email.builder()
                    .from(user, "OpenCode 测试")
                    .to(to)
                    .subject("[OpenCode·阿里云] 双格式测试 - " + Instant.now().toEpochMilli())
                    .textAndHtml(
                            "=== 阿里云企业邮箱 × OpenCode 双格式测试 ===\n\n"
                                    + "这是纯文本版本。你的邮件客户端如果支持 HTML 则会显示 HTML 版本。\n\n"
                                    + "This is the plain text version.",
                            "<html><body style='font-family: Microsoft YaHei, sans-serif;'>"
                                    + "<h2 style='color:#ff6a00;'>阿里云 × OpenCode 双格式测试</h2>"
                                    + "<p>这是 <strong>HTML 版本</strong>。</p>"
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
        @DisplayName("发送带附件的邮件 (中文文件名)")
        void sendWithAttachment() {
            byte[] csvData = ("姓名,部门,邮箱\n"
                    + "张三,技术部," + user + "\n"
                    + "李四,产品部,lisi@example.com\n"
                    + "王五,运营部,wangwu@example.com\n").getBytes(StandardCharsets.UTF_8);

            byte[] jsonData = ("""
                    {
                      "project": "opencode-base-email",
                      "version": "1.0.3",
                      "provider": "阿里云企业邮箱",
                      "host": "%s",
                      "features": ["SMTP", "SSL", "MIME", "Attachment", "Template"]
                    }
                    """.formatted(host)).getBytes(StandardCharsets.UTF_8);

            Email email = Email.builder()
                    .from(user, "OpenCode 测试")
                    .to(to)
                    .subject("[OpenCode·阿里云] 附件测试 - " + Instant.now().toEpochMilli())
                    .html("<html><body>"
                            + "<h2>附件测试</h2>"
                            + "<p>本邮件包含 2 个附件：</p>"
                            + "<ol>"
                            + "<li><strong>员工列表.csv</strong> — CSV 格式</li>"
                            + "<li><strong>项目信息.json</strong> — JSON 格式</li>"
                            + "</ol>"
                            + "</body></html>")
                    .attach(ByteArrayAttachment.of("员工列表.csv", csvData, "text/csv"))
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
                    .subject("[OpenCode·阿里云] 内嵌图片测试 - " + Instant.now().toEpochMilli())
                    .html("<html><body style='font-family: Microsoft YaHei, sans-serif;'>"
                            + "<h2>内嵌图片测试</h2>"
                            + "<p>下方是通过 CID 引用的内嵌图片：</p>"
                            + "<div style='padding:20px; background:#fff7ed; text-align:center; border-radius:8px;'>"
                            + "<img src=\"cid:ali-logo\" alt=\"Logo\" "
                            + "width=\"50\" height=\"50\" "
                            + "style=\"border: 2px solid #ff6a00; border-radius: 8px;\" />"
                            + "</div>"
                            + "<p style='color:#666; font-size:12px;'>如果能看到图片，说明 CID 内嵌图片功能正常。</p>"
                            + "</body></html>")
                    .attach(InlineAttachment.of("ali-logo", "logo.png", pngData, "image/png"))
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
                    Email.builder().from(user, "OpenCode 测试").to(to)
                            .subject("[OpenCode·阿里云] 批量 1/3 - " + ts)
                            .text("批量邮件 1/3").build(),
                    Email.builder().from(user, "OpenCode 测试").to(to)
                            .subject("[OpenCode·阿里云] 批量 2/3 - " + ts)
                            .text("批量邮件 2/3").build(),
                    Email.builder().from(user, "OpenCode 测试").to(to)
                            .subject("[OpenCode·阿里云] 批量 3/3 - " + ts)
                            .text("批量邮件 3/3").build()
            );

            BatchSendResult result = sender.sendBatch(emails);

            assertThat(result.totalCount()).isEqualTo(3);
            assertThat(result.successCount()).isEqualTo(3);
            assertThat(result.allSucceeded()).isTrue();
            System.out.println("[OK] 批量发送: " + result.successCount() + "/" + result.totalCount()
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
                    .subject("[OpenCode·阿里云] 异步测试 - " + Instant.now().toEpochMilli())
                    .text("异步发送测试。\nAsync send test via Alibaba Cloud Enterprise Mail.")
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
                            <h1 style="color: #ff6a00;">系统通知 | System Notification</h1>
                            <p>尊敬的 <strong>${userName}</strong>：</p>
                            <p>您的服务器 <code>${serverName}</code> 状态发生变更：</p>
                            <table border="0" cellpadding="8" style="border-collapse: collapse; margin: 15px 0; width: 100%%;">
                                <tr style="background: #fff7ed;">
                                    <td style="border: 1px solid #fed7aa;"><strong>服务器</strong></td>
                                    <td style="border: 1px solid #fed7aa;">${serverName}</td>
                                </tr>
                                <tr>
                                    <td style="border: 1px solid #fed7aa;"><strong>状态</strong></td>
                                    <td style="border: 1px solid #fed7aa; color: green; font-weight: bold;">${status}</td>
                                </tr>
                                <tr style="background: #fff7ed;">
                                    <td style="border: 1px solid #fed7aa;"><strong>时间</strong></td>
                                    <td style="border: 1px solid #fed7aa;">${timestamp}</td>
                                </tr>
                                <tr>
                                    <td style="border: 1px solid #fed7aa;"><strong>IP</strong></td>
                                    <td style="border: 1px solid #fed7aa;">${ipAddress}</td>
                                </tr>
                            </table>
                            <p>如有疑问，请联系运维团队。</p>
                            <hr style="border: none; border-top: 1px solid #eee;">
                            <p style="color: #999; font-size: 12px;">
                                此邮件由 OpenCode Base Email 模板引擎自动生成<br>
                                通过阿里云企业邮箱发送
                            </p>
                        </div>
                    </body>
                    </html>
                    """;

            OpenEmail.sendTemplate(to,
                    "[OpenCode·阿里云] 服务器通知 - " + Instant.now().toEpochMilli(),
                    template,
                    Map.of(
                            "userName", "管理员",
                            "serverName", "prod-web-01",
                            "status", "运行正常 (Running)",
                            "timestamp", Instant.now().toString(),
                            "ipAddress", "172.16.0.100"
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
        @DisplayName("高优先级 + 自定义头 + Reply-To")
        void sendWithCustomHeaders() {
            Email email = Email.builder()
                    .from(user, "OpenCode 测试")
                    .to(to)
                    .subject("[OpenCode·阿里云] 高优先级 - " + Instant.now().toEpochMilli())
                    .text("高优先级邮件，带自定义 Header 和 Reply-To。")
                    .priority(Email.Priority.HIGH)
                    .header("X-OpenCode-Module", "opencode-base-email")
                    .header("X-OpenCode-Version", "1.0.3")
                    .header("X-Mail-Provider", "Alibaba Cloud Enterprise Mail")
                    .replyTo(user)
                    .build();

            SendResult result = sender.sendWithResult(email);

            assertThat(result.success()).isTrue();
            System.out.println("[OK] 高优先级邮件发送成功, Message-ID: " + result.messageId());
        }

        @Test
        @Order(100)
        @DisplayName("CC 抄送")
        void sendWithCc() {
            Email email = Email.builder()
                    .from(user, "OpenCode 测试")
                    .to(to)
                    .cc(user)
                    .subject("[OpenCode·阿里云] 抄送测试 - " + Instant.now().toEpochMilli())
                    .text("抄送给发件人自己。")
                    .build();

            SendResult result = sender.sendWithResult(email);

            assertThat(result.success()).isTrue();
            System.out.println("[OK] 抄送邮件发送成功");
        }

        @Test
        @Order(110)
        @DisplayName("OpenEmail 门面快速发送")
        void sendViaFacade() {
            OpenEmail.sendText(to,
                    "[OpenCode·阿里云] 门面快发 - " + Instant.now().toEpochMilli(),
                    "通过 OpenEmail.sendText() 一行代码发送。");
            System.out.println("[OK] OpenEmail.sendText() 发送成功");
        }
    }

    // ==================== 完整场景测试 ====================

    @Nested
    @DisplayName("完整场景测试")
    class FullScenarioTests {

        @Test
        @Order(200)
        @DisplayName("全功能组合: 双格式 + 附件 + 内嵌图 + CC + 优先级 + 自定义头 + Reply-To")
        void fullScenario() {
            byte[] reportData = ("OpenCode Base Email 阿里云企业邮箱测试报告\n"
                    + "==========================================\n\n"
                    + "测试时间: " + Instant.now() + "\n"
                    + "SMTP 服务器: " + host + ":" + port + "\n"
                    + "协议实现: 纯 JDK Socket，零外部依赖\n\n"
                    + "测试结果:\n"
                    + "  [✓] SMTP SSL/STARTTLS 连接\n"
                    + "  [✓] 用户名/密码认证\n"
                    + "  [✓] 纯文本邮件\n"
                    + "  [✓] HTML 邮件\n"
                    + "  [✓] Multipart/Alternative 双格式\n"
                    + "  [✓] 文件附件（中文文件名）\n"
                    + "  [✓] 内嵌图片 (CID)\n"
                    + "  [✓] 批量发送（连接复用）\n"
                    + "  [✓] 异步发送\n"
                    + "  [✓] 模板引擎变量替换\n"
                    + "  [✓] 自定义头 / 优先级 / 抄送 / 回复地址\n\n"
                    + "结论: 全部通过\n").getBytes(StandardCharsets.UTF_8);

            byte[] pngPixel = new byte[]{
                    (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                    0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
                    0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
                    0x08, 0x02, 0x00, 0x00, 0x00, (byte) 0x90, 0x77, 0x53,
                    (byte) 0xDE, 0x00, 0x00, 0x00, 0x0C, 0x49, 0x44, 0x41,
                    0x54, 0x08, (byte) 0xD7, 0x63, (byte) 0xF8, 0x4F, 0x00,
                    0x00, 0x00, 0x02, 0x00, 0x01, (byte) 0xE2, 0x21,
                    (byte) 0xBC, 0x33, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45,
                    0x4E, 0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82
            };

            Email email = Email.builder()
                    .from(user, "OpenCode 全功能测试")
                    .to(to)
                    .cc(user)
                    .subject("[OpenCode·阿里云] ★ 全功能测试 - " + Instant.now().toEpochMilli())
                    .textAndHtml(
                            "=== 阿里云企业邮箱 × OpenCode 全功能测试 ===\n\n"
                                    + "纯文本版本。\n"
                                    + "功能: 双格式 + 附件 + 内嵌图 + CC + 优先级 + 自定义头 + Reply-To\n\n"
                                    + "发送时间: " + Instant.now(),
                            """
                                    <html>
                                    <body style="font-family: 'Microsoft YaHei', Arial, sans-serif; padding: 20px; background: #f5f5f5;">
                                        <div style="max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                                            <div style="text-align: center; margin-bottom: 20px;">
                                                <img src="cid:ali-badge" width="24" height="24" style="vertical-align: middle;" />
                                                <span style="font-size: 24px; font-weight: bold; color: #ff6a00; vertical-align: middle;">
                                                    阿里云 × OpenCode
                                                </span>
                                            </div>
                                            <h2 style="color: #1e293b;">全功能组合测试</h2>
                                            <table border="0" cellpadding="6" cellspacing="0" style="border-collapse: collapse; width: 100%%;">
                                                <tr style="background:#fff7ed;"><td style="border:1px solid #fed7aa;">Multipart/Alternative</td><td style="border:1px solid #fed7aa; color:green; text-align:center;">✅</td></tr>
                                                <tr><td style="border:1px solid #fed7aa;">文件附件 (测试报告.txt)</td><td style="border:1px solid #fed7aa; color:green; text-align:center;">✅</td></tr>
                                                <tr style="background:#fff7ed;"><td style="border:1px solid #fed7aa;">内嵌图片 (CID)</td><td style="border:1px solid #fed7aa; color:green; text-align:center;">✅</td></tr>
                                                <tr><td style="border:1px solid #fed7aa;">抄送 (CC)</td><td style="border:1px solid #fed7aa; color:green; text-align:center;">✅</td></tr>
                                                <tr style="background:#fff7ed;"><td style="border:1px solid #fed7aa;">高优先级</td><td style="border:1px solid #fed7aa; color:green; text-align:center;">✅</td></tr>
                                                <tr><td style="border:1px solid #fed7aa;">自定义 Header</td><td style="border:1px solid #fed7aa; color:green; text-align:center;">✅</td></tr>
                                                <tr style="background:#fff7ed;"><td style="border:1px solid #fed7aa;">Reply-To</td><td style="border:1px solid #fed7aa; color:green; text-align:center;">✅</td></tr>
                                            </table>
                                            <hr style="border:none; border-top:1px solid #eee; margin:20px 0;">
                                            <p style="color: #64748b; font-size: 12px; text-align: center;">
                                                Powered by <strong>opencode-base-email V1.0.3</strong><br>
                                                零外部依赖 · 纯 JDK 实现 · SMTP/IMAP/POP3<br>
                                                通过阿里云企业邮箱 (%s:%d) 发送<br>
                                                %s
                                            </p>
                                        </div>
                                    </body>
                                    </html>
                                    """.formatted(host, port, Instant.now())
                    )
                    .attach(ByteArrayAttachment.of("测试报告.txt", reportData, "text/plain"))
                    .attach(InlineAttachment.of("ali-badge", "badge.png", pngPixel, "image/png"))
                    .priority(Email.Priority.HIGH)
                    .header("X-OpenCode-Test", "full-scenario")
                    .header("X-OpenCode-Version", "1.0.3")
                    .header("X-Mail-Provider", "Alibaba Cloud Enterprise Mail")
                    .replyTo(user)
                    .build();

            SendResult result = sender.sendWithResult(email);

            assertThat(result.success()).isTrue();
            assertThat(result.messageId()).isNotBlank();

            System.out.println("╔══════════════════════════════════════════════╗");
            System.out.println("║  [OK] 阿里云企业邮箱全功能测试通过!            ║");
            System.out.println("╠══════════════════════════════════════════════╣");
            System.out.println("║  Message-ID: " + result.messageId());
            System.out.println("║  服务器: " + host + ":" + port);
            System.out.println("║  功能: 双格式+附件+内嵌图+CC+优先级+头+Reply-To");
            System.out.println("║  协议: 纯 JDK Socket 实现，零外部依赖          ║");
            System.out.println("╚══════════════════════════════════════════════╝");
        }
    }
}
