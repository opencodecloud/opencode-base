package cloud.opencode.base.email.sender;

import cloud.opencode.base.email.Email;
import cloud.opencode.base.email.EmailConfig;
import cloud.opencode.base.email.attachment.ByteArrayAttachment;
import cloud.opencode.base.email.attachment.InlineAttachment;
import cloud.opencode.base.email.exception.EmailConfigException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * SmtpEmailSender test class
 * SmtpEmailSender 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("SmtpEmailSender 测试")
class SmtpEmailSenderTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用基本配置创建发送器")
        void testConstructorWithBasicConfig() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .port(587)
                    .build();

            SmtpEmailSender sender = new SmtpEmailSender(config);

            assertThat(sender.getConfig()).isEqualTo(config);
        }

        @Test
        @DisplayName("使用认证配置创建发送器")
        void testConstructorWithAuthConfig() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .port(587)
                    .username("user@example.com")
                    .password("password")
                    .build();

            SmtpEmailSender sender = new SmtpEmailSender(config);

            assertThat(sender.getConfig()).isEqualTo(config);
        }

        @Test
        @DisplayName("使用SSL配置创建发送器")
        void testConstructorWithSslConfig() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .port(465)
                    .ssl(true)
                    .build();

            SmtpEmailSender sender = new SmtpEmailSender(config);

            assertThat(sender.getConfig()).isNotNull();
        }

        @Test
        @DisplayName("使用STARTTLS配置创建发送器")
        void testConstructorWithStarttlsConfig() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .port(587)
                    .starttls(true)
                    .build();

            SmtpEmailSender sender = new SmtpEmailSender(config);

            assertThat(sender.getConfig()).isNotNull();
        }

        @Test
        @DisplayName("使用OAuth2配置创建发送器")
        void testConstructorWithOAuth2Config() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.gmail.com")
                    .port(587)
                    .username("user@gmail.com")
                    .oauth2Token("access-token")
                    .starttls(true)
                    .build();

            SmtpEmailSender sender = new SmtpEmailSender(config);

            assertThat(sender.getConfig().hasOAuth2()).isTrue();
        }

        @Test
        @DisplayName("使用Debug模式配置")
        void testConstructorWithDebugConfig() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .debug(true)
                    .build();

            SmtpEmailSender sender = new SmtpEmailSender(config);

            assertThat(sender.getConfig()).isNotNull();
        }

        @Test
        @DisplayName("使用超时配置")
        void testConstructorWithTimeoutConfig() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .timeout(Duration.ofSeconds(30))
                    .connectionTimeout(Duration.ofSeconds(10))
                    .build();

            SmtpEmailSender sender = new SmtpEmailSender(config);

            assertThat(sender.getConfig().timeout()).isEqualTo(Duration.ofSeconds(30));
        }
    }

    @Nested
    @DisplayName("getConfig() 测试")
    class GetConfigTests {

        @Test
        @DisplayName("返回配置对象")
        void testGetConfig() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .port(587)
                    .defaultFrom("sender@example.com")
                    .build();

            SmtpEmailSender sender = new SmtpEmailSender(config);

            assertThat(sender.getConfig()).isEqualTo(config);
            assertThat(sender.getConfig().host()).isEqualTo("smtp.example.com");
            assertThat(sender.getConfig().port()).isEqualTo(587);
        }
    }

    @Nested
    @DisplayName("Email构建测试")
    class EmailBuildingTests {

        @Test
        @DisplayName("创建简单邮件")
        void testSimpleEmail() {
            Email email = Email.builder()
                    .to("recipient@example.com")
                    .subject("Test Subject")
                    .text("Test content")
                    .build();

            assertThat(email.to()).contains("recipient@example.com");
            assertThat(email.subject()).isEqualTo("Test Subject");
            assertThat(email.content()).isEqualTo("Test content");
        }

        @Test
        @DisplayName("创建HTML邮件")
        void testHtmlEmail() {
            Email email = Email.builder()
                    .to("recipient@example.com")
                    .subject("HTML Email")
                    .html("<html><body>Hello</body></html>")
                    .build();

            assertThat(email.html()).isTrue();
        }

        @Test
        @DisplayName("创建带附件的邮件")
        void testEmailWithAttachment() {
            ByteArrayAttachment attachment = new ByteArrayAttachment("test.txt", "content".getBytes());

            Email email = Email.builder()
                    .to("recipient@example.com")
                    .subject("With Attachment")
                    .text("See attached")
                    .attach(attachment)
                    .build();

            assertThat(email.hasAttachments()).isTrue();
            assertThat(email.attachments()).hasSize(1);
        }

        @Test
        @DisplayName("创建带内嵌图片的邮件")
        void testEmailWithInlineAttachment() {
            InlineAttachment inline = InlineAttachment.of("logo", "logo.png", new byte[100], "image/png");

            Email email = Email.builder()
                    .to("recipient@example.com")
                    .subject("With Logo")
                    .html("<html><body><img src='cid:logo'/></body></html>")
                    .attach(inline)
                    .build();

            assertThat(email.hasInlineAttachments()).isTrue();
        }

        @Test
        @DisplayName("创建带自定义头的邮件")
        void testEmailWithCustomHeaders() {
            Email email = Email.builder()
                    .to("recipient@example.com")
                    .subject("Custom Headers")
                    .text("Test")
                    .header("X-Custom-Header", "custom-value")
                    .build();

            assertThat(email.headers()).containsEntry("X-Custom-Header", "custom-value");
        }

        @Test
        @DisplayName("创建带优先级的邮件")
        void testEmailWithPriority() {
            Email email = Email.builder()
                    .to("recipient@example.com")
                    .subject("High Priority")
                    .text("Urgent!")
                    .priority(Email.Priority.HIGH)
                    .build();

            assertThat(email.priority()).isEqualTo(Email.Priority.HIGH);
        }

        @Test
        @DisplayName("创建带CC/BCC的邮件")
        void testEmailWithCcAndBcc() {
            Email email = Email.builder()
                    .to("recipient@example.com")
                    .cc("cc@example.com")
                    .bcc("bcc@example.com")
                    .subject("Multi recipients")
                    .text("Test")
                    .build();

            assertThat(email.cc()).contains("cc@example.com");
            assertThat(email.bcc()).contains("bcc@example.com");
        }

        @Test
        @DisplayName("创建带Reply-To的邮件")
        void testEmailWithReplyTo() {
            Email email = Email.builder()
                    .to("recipient@example.com")
                    .subject("Reply To Test")
                    .text("Test")
                    .replyTo("reply@example.com")
                    .build();

            assertThat(email.replyTo()).isEqualTo("reply@example.com");
        }
    }

    @Nested
    @DisplayName("配置验证测试")
    class ConfigValidationTests {

        @Test
        @DisplayName("缺少发件人地址配置")
        void testMissingFromAddress() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .build();

            SmtpEmailSender sender = new SmtpEmailSender(config);

            Email email = Email.builder()
                    .to("recipient@example.com")
                    .subject("Test")
                    .text("Content")
                    .build();

            // This would throw during actual send, but we can verify config
            assertThat(config.defaultFrom()).isNull();
        }

        @Test
        @DisplayName("使用默认发件人地址")
        void testDefaultFromAddress() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .defaultFrom("sender@example.com", "Sender Name")
                    .build();

            assertThat(config.defaultFrom()).isEqualTo("sender@example.com");
            assertThat(config.defaultFromName()).isEqualTo("Sender Name");
        }
    }

    @Nested
    @DisplayName("DKIM配置测试")
    class DkimConfigTests {

        @Test
        @DisplayName("无DKIM配置")
        void testNoDkimConfig() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .build();

            assertThat(config.hasDkim()).isFalse();
            assertThat(config.dkim()).isNull();
        }
    }
}
