package cloud.opencode.base.email.security;

import cloud.opencode.base.email.exception.EmailSecurityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * DkimSigner test class
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

    private static final String TEST_MESSAGE =
            "From: test@example.com\r\n" +
            "To: recipient@example.com\r\n" +
            "Subject: Test\r\n" +
            "Date: Mon, 1 Jan 2024 00:00:00 +0000\r\n" +
            "Message-ID: <test@example.com>\r\n" +
            "MIME-Version: 1.0\r\n" +
            "Content-Type: text/plain; charset=UTF-8\r\n" +
            "\r\n" +
            "Hello World";

    @BeforeEach
    void setUp() throws Exception {
        // Generate test RSA key pair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        privateKey = keyPair.getPrivate();
    }

    @Nested
    @DisplayName("sign() 测试")
    class SignTests {

        @Test
        @DisplayName("null配置不签名")
        void testNullConfig() {
            String result = DkimSigner.sign(TEST_MESSAGE, null);

            assertThat(result).isEqualTo(TEST_MESSAGE);
            assertThat(result).doesNotContain("DKIM-Signature");
        }

        @Test
        @DisplayName("签名消息添加DKIM头")
        void testSignAddsHeader() {
            DkimConfig config = DkimConfig.of("example.com", "mail", privateKey);

            String result = DkimSigner.sign(TEST_MESSAGE, config);

            assertThat(result).startsWith("DKIM-Signature:");
            assertThat(result).contains(TEST_MESSAGE);
        }

        @Test
        @DisplayName("DKIM签名包含必需字段")
        void testSignatureContainsRequiredFields() {
            DkimConfig config = DkimConfig.of("example.com", "mail", privateKey);

            String result = DkimSigner.sign(TEST_MESSAGE, config);

            // Extract the DKIM-Signature header value (first line before CRLF + original message)
            String signatureLine = result.substring(0, result.indexOf("\r\n" + "From:"));
            assertThat(signatureLine).contains("v=1");
            assertThat(signatureLine).contains("a=rsa-sha256");
            assertThat(signatureLine).contains("c=relaxed/relaxed");
            assertThat(signatureLine).contains("d=example.com");
            assertThat(signatureLine).contains("s=mail");
            assertThat(signatureLine).contains("h=");
            assertThat(signatureLine).contains("bh=");
            assertThat(signatureLine).contains("b=");
        }

        @Test
        @DisplayName("使用自定义头列表签名")
        void testSignWithCustomHeaders() {
            Set<String> customHeaders = Set.of("From", "To", "Subject");
            DkimConfig config = DkimConfig.of("example.com", "selector", privateKey, customHeaders);

            String result = DkimSigner.sign(TEST_MESSAGE, config);

            assertThat(result).startsWith("DKIM-Signature:");
            assertThat(result).contains("h=");
        }

        @Test
        @DisplayName("签名包含时间戳")
        void testSignatureContainsTimestamp() {
            DkimConfig config = DkimConfig.of("example.com", "mail", privateKey);

            String result = DkimSigner.sign(TEST_MESSAGE, config);

            assertThat(result).contains("t=");
        }

        @Test
        @DisplayName("签名值Base64编码")
        void testSignatureValueIsBase64() {
            DkimConfig config = DkimConfig.of("example.com", "mail", privateKey);

            String result = DkimSigner.sign(TEST_MESSAGE, config);

            // Extract the DKIM-Signature header line
            String signatureLine = result.substring("DKIM-Signature: ".length(),
                    result.indexOf("\r\nFrom:"));
            // Extract b= value (last field in the signature)
            int bIndex = signatureLine.lastIndexOf("b=");
            assertThat(bIndex).isGreaterThan(0);
            String bValue = signatureLine.substring(bIndex + 2);
            // Should be Base64 encoded (alphanumeric, +, /, =)
            assertThat(bValue).matches("[A-Za-z0-9+/=]+");
        }
    }

    @Nested
    @DisplayName("错误处理测试")
    class ErrorHandlingTests {

        @Test
        @DisplayName("有效私钥不抛出异常")
        void testValidPrivateKeyNoException() {
            DkimConfig config = DkimConfig.of("example.com", "mail", privateKey);

            assertThatNoException().isThrownBy(() -> DkimSigner.sign(TEST_MESSAGE, config));
        }
    }

    @Nested
    @DisplayName("消息内容测试")
    class MessageContentTests {

        @Test
        @DisplayName("签名HTML消息")
        void testSignHtmlMessage() {
            String htmlMessage =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test HTML\r\n" +
                    "MIME-Version: 1.0\r\n" +
                    "Content-Type: text/html; charset=UTF-8\r\n" +
                    "\r\n" +
                    "<html><body><h1>Hello</h1></body></html>";

            DkimConfig config = DkimConfig.of("example.com", "mail", privateKey);

            String result = DkimSigner.sign(htmlMessage, config);

            assertThat(result).startsWith("DKIM-Signature:");
        }

        @Test
        @DisplayName("签名空正文消息")
        void testSignEmptyBody() {
            String emptyBodyMessage =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Empty Body\r\n" +
                    "MIME-Version: 1.0\r\n" +
                    "Content-Type: text/plain; charset=UTF-8\r\n" +
                    "\r\n";

            DkimConfig config = DkimConfig.of("example.com", "mail", privateKey);

            assertThatNoException().isThrownBy(() -> DkimSigner.sign(emptyBodyMessage, config));
        }

        @Test
        @DisplayName("签名多行正文")
        void testSignMultilineBody() {
            String multilineMessage =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Multiline\r\n" +
                    "MIME-Version: 1.0\r\n" +
                    "Content-Type: text/plain; charset=UTF-8\r\n" +
                    "\r\n" +
                    "Line 1\r\nLine 2\r\nLine 3";

            DkimConfig config = DkimConfig.of("example.com", "mail", privateKey);

            assertThatNoException().isThrownBy(() -> DkimSigner.sign(multilineMessage, config));
        }
    }

    @Nested
    @DisplayName("不同域名和选择器测试")
    class DomainSelectorTests {

        @Test
        @DisplayName("使用不同域名")
        void testDifferentDomains() {
            DkimConfig config1 = DkimConfig.of("domain1.com", "mail", privateKey);
            DkimConfig config2 = DkimConfig.of("domain2.org", "dkim", privateKey);

            String result1 = DkimSigner.sign(TEST_MESSAGE, config1);
            String result2 = DkimSigner.sign(TEST_MESSAGE, config2);

            assertThat(result1).contains("d=domain1.com");
            assertThat(result2).contains("d=domain2.org");
        }

        @Test
        @DisplayName("使用不同选择器")
        void testDifferentSelectors() {
            DkimConfig config1 = DkimConfig.of("example.com", "selector1", privateKey);
            DkimConfig config2 = DkimConfig.of("example.com", "selector2", privateKey);

            String result1 = DkimSigner.sign(TEST_MESSAGE, config1);
            String result2 = DkimSigner.sign(TEST_MESSAGE, config2);

            assertThat(result1).contains("s=selector1");
            assertThat(result2).contains("s=selector2");
        }
    }
}
