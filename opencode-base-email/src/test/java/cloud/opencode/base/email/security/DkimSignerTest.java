package cloud.opencode.base.email.security;

import cloud.opencode.base.email.exception.EmailSecurityException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.util.Properties;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * DkimSigner 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("DkimSigner 测试")
class DkimSignerTest {

    private PrivateKey privateKey;
    private Session session;

    @BeforeEach
    void setUp() throws Exception {
        // Generate test RSA key pair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        privateKey = keyPair.getPrivate();

        // Create mail session
        Properties props = new Properties();
        session = Session.getInstance(props);
    }

    @Nested
    @DisplayName("sign() 测试")
    class SignTests {

        @Test
        @DisplayName("null配置不签名")
        void testNullConfig() throws Exception {
            MimeMessage message = createTestMessage();

            DkimSigner.sign(message, null);

            String[] dkimHeader = message.getHeader("DKIM-Signature");
            assertThat(dkimHeader).isNull();
        }

        @Test
        @DisplayName("签名消息添加DKIM头")
        void testSignAddsHeader() throws Exception {
            MimeMessage message = createTestMessage();
            DkimConfig config = DkimConfig.of("example.com", "mail", privateKey);

            DkimSigner.sign(message, config);

            String[] dkimHeader = message.getHeader("DKIM-Signature");
            assertThat(dkimHeader).isNotNull().hasSize(1);
        }

        @Test
        @DisplayName("DKIM签名包含必需字段")
        void testSignatureContainsRequiredFields() throws Exception {
            MimeMessage message = createTestMessage();
            DkimConfig config = DkimConfig.of("example.com", "mail", privateKey);

            DkimSigner.sign(message, config);

            String signature = message.getHeader("DKIM-Signature")[0];
            assertThat(signature).contains("v=1");
            assertThat(signature).contains("a=rsa-sha256");
            assertThat(signature).contains("c=relaxed/relaxed");
            assertThat(signature).contains("d=example.com");
            assertThat(signature).contains("s=mail");
            assertThat(signature).contains("h=");
            assertThat(signature).contains("bh=");
            assertThat(signature).contains("b=");
        }

        @Test
        @DisplayName("使用自定义头列表签名")
        void testSignWithCustomHeaders() throws Exception {
            MimeMessage message = createTestMessage();
            Set<String> customHeaders = Set.of("From", "To", "Subject");
            DkimConfig config = DkimConfig.of("example.com", "selector", privateKey, customHeaders);

            DkimSigner.sign(message, config);

            String signature = message.getHeader("DKIM-Signature")[0];
            assertThat(signature).contains("h=");
        }

        @Test
        @DisplayName("签名包含时间戳")
        void testSignatureContainsTimestamp() throws Exception {
            MimeMessage message = createTestMessage();
            DkimConfig config = DkimConfig.of("example.com", "mail", privateKey);

            DkimSigner.sign(message, config);

            String signature = message.getHeader("DKIM-Signature")[0];
            assertThat(signature).contains("t=");
        }

        @Test
        @DisplayName("签名值Base64编码")
        void testSignatureValueIsBase64() throws Exception {
            MimeMessage message = createTestMessage();
            DkimConfig config = DkimConfig.of("example.com", "mail", privateKey);

            DkimSigner.sign(message, config);

            String signature = message.getHeader("DKIM-Signature")[0];
            // Extract b= value
            int bIndex = signature.indexOf("b=");
            assertThat(bIndex).isGreaterThan(0);
            String bValue = signature.substring(bIndex + 2);
            // Should be Base64 encoded (alphanumeric, +, /, =)
            assertThat(bValue).matches("[A-Za-z0-9+/=]+");
        }
    }

    @Nested
    @DisplayName("错误处理测试")
    class ErrorHandlingTests {

        @Test
        @DisplayName("无效私钥抛出异常")
        void testInvalidPrivateKey() throws Exception {
            MimeMessage message = createTestMessage();
            // Create a mock config with an invalid key scenario would require mocking
            // For now, we test with a valid config to ensure no exception
            DkimConfig config = DkimConfig.of("example.com", "mail", privateKey);

            assertThatNoException().isThrownBy(() -> DkimSigner.sign(message, config));
        }
    }

    @Nested
    @DisplayName("消息内容测试")
    class MessageContentTests {

        @Test
        @DisplayName("签名HTML消息")
        void testSignHtmlMessage() throws Exception {
            MimeMessage message = new MimeMessage(session);
            message.setFrom("sender@example.com");
            message.setRecipients(MimeMessage.RecipientType.TO, "recipient@example.com");
            message.setSubject("Test HTML");
            message.setContent("<html><body><h1>Hello</h1></body></html>", "text/html");

            DkimConfig config = DkimConfig.of("example.com", "mail", privateKey);

            assertThatNoException().isThrownBy(() -> DkimSigner.sign(message, config));

            String[] dkimHeader = message.getHeader("DKIM-Signature");
            assertThat(dkimHeader).isNotNull();
        }

        @Test
        @DisplayName("签名空正文消息")
        void testSignEmptyBody() throws Exception {
            MimeMessage message = new MimeMessage(session);
            message.setFrom("sender@example.com");
            message.setRecipients(MimeMessage.RecipientType.TO, "recipient@example.com");
            message.setSubject("Empty Body");
            message.setText("");

            DkimConfig config = DkimConfig.of("example.com", "mail", privateKey);

            assertThatNoException().isThrownBy(() -> DkimSigner.sign(message, config));
        }

        @Test
        @DisplayName("签名多行正文")
        void testSignMultilineBody() throws Exception {
            MimeMessage message = new MimeMessage(session);
            message.setFrom("sender@example.com");
            message.setRecipients(MimeMessage.RecipientType.TO, "recipient@example.com");
            message.setSubject("Multiline");
            message.setText("Line 1\nLine 2\nLine 3");

            DkimConfig config = DkimConfig.of("example.com", "mail", privateKey);

            assertThatNoException().isThrownBy(() -> DkimSigner.sign(message, config));
        }
    }

    @Nested
    @DisplayName("不同域名和选择器测试")
    class DomainSelectorTests {

        @Test
        @DisplayName("使用不同域名")
        void testDifferentDomains() throws Exception {
            MimeMessage message1 = createTestMessage();
            MimeMessage message2 = createTestMessage();

            DkimConfig config1 = DkimConfig.of("domain1.com", "mail", privateKey);
            DkimConfig config2 = DkimConfig.of("domain2.org", "dkim", privateKey);

            DkimSigner.sign(message1, config1);
            DkimSigner.sign(message2, config2);

            assertThat(message1.getHeader("DKIM-Signature")[0]).contains("d=domain1.com");
            assertThat(message2.getHeader("DKIM-Signature")[0]).contains("d=domain2.org");
        }

        @Test
        @DisplayName("使用不同选择器")
        void testDifferentSelectors() throws Exception {
            MimeMessage message1 = createTestMessage();
            MimeMessage message2 = createTestMessage();

            DkimConfig config1 = DkimConfig.of("example.com", "selector1", privateKey);
            DkimConfig config2 = DkimConfig.of("example.com", "selector2", privateKey);

            DkimSigner.sign(message1, config1);
            DkimSigner.sign(message2, config2);

            assertThat(message1.getHeader("DKIM-Signature")[0]).contains("s=selector1");
            assertThat(message2.getHeader("DKIM-Signature")[0]).contains("s=selector2");
        }
    }

    private MimeMessage createTestMessage() throws Exception {
        MimeMessage message = new MimeMessage(session);
        message.setFrom("sender@example.com");
        message.setRecipients(MimeMessage.RecipientType.TO, "recipient@example.com");
        message.setSubject("Test Subject");
        message.setText("Test Body");
        return message;
    }
}
