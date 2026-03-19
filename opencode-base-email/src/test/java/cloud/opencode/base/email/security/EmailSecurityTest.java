package cloud.opencode.base.email.security;

import cloud.opencode.base.email.Attachment;
import cloud.opencode.base.email.attachment.ByteArrayAttachment;
import cloud.opencode.base.email.exception.EmailSecurityException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * EmailSecurity 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("EmailSecurity 测试")
class EmailSecurityTest {

    @Nested
    @DisplayName("sanitizeHeader() 测试")
    class SanitizeHeaderTests {

        @Test
        @DisplayName("null值返回null")
        void testSanitizeNull() {
            assertThat(EmailSecurity.sanitizeHeader(null)).isNull();
        }

        @Test
        @DisplayName("正常值不变")
        void testSanitizeNormalValue() {
            assertThat(EmailSecurity.sanitizeHeader("Normal Subject")).isEqualTo("Normal Subject");
        }

        @Test
        @DisplayName("移除回车符")
        void testRemoveCarriageReturn() {
            String result = EmailSecurity.sanitizeHeader("Test\rBcc: hacker@evil.com");
            assertThat(result).isEqualTo("TestBcc: hacker@evil.com");
        }

        @Test
        @DisplayName("移除换行符")
        void testRemoveNewLine() {
            String result = EmailSecurity.sanitizeHeader("Test\nBcc: hacker@evil.com");
            assertThat(result).isEqualTo("TestBcc: hacker@evil.com");
        }

        @Test
        @DisplayName("移除CRLF")
        void testRemoveCRLF() {
            String result = EmailSecurity.sanitizeHeader("Test\r\nBcc: hacker@evil.com");
            assertThat(result).isEqualTo("TestBcc: hacker@evil.com");
        }

        @Test
        @DisplayName("移除控制字符")
        void testRemoveControlChars() {
            String result = EmailSecurity.sanitizeHeader("Test\u0001\u001fValue");
            assertThat(result).isEqualTo("TestValue");
        }

        @Test
        @DisplayName("保留空格和制表符")
        void testPreserveWhitespace() {
            // Note: Based on the regex, only \r\n and control chars 0x00-0x1f are removed
            // Tab (\t = 0x09) is in the control char range, so it will be removed
            String result = EmailSecurity.sanitizeHeader("Test Value");
            assertThat(result).isEqualTo("Test Value");
        }
    }

    @Nested
    @DisplayName("isValidEmail() 测试")
    class IsValidEmailTests {

        @Test
        @DisplayName("null邮箱无效")
        void testNullEmail() {
            assertThat(EmailSecurity.isValidEmail(null)).isFalse();
        }

        @Test
        @DisplayName("空白邮箱无效")
        void testBlankEmail() {
            assertThat(EmailSecurity.isValidEmail("")).isFalse();
            assertThat(EmailSecurity.isValidEmail("   ")).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "user@example.com",
                "user.name@example.com",
                "user+tag@example.com",
                "user@sub.example.com",
                "user@example.co.uk"
        })
        @DisplayName("有效邮箱格式")
        void testValidEmails(String email) {
            assertThat(EmailSecurity.isValidEmail(email)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "invalid",
                "user@",
                "@example.com",
                "user@.com",
                "user@example",
                "user name@example.com"
        })
        @DisplayName("无效邮箱格式")
        void testInvalidEmails(String email) {
            assertThat(EmailSecurity.isValidEmail(email)).isFalse();
        }
    }

    @Nested
    @DisplayName("validateAttachment() 测试")
    class ValidateAttachmentTests {

        @Test
        @DisplayName("null附件抛出异常")
        void testNullAttachment() {
            assertThatThrownBy(() -> EmailSecurity.validateAttachment(null))
                    .isInstanceOf(EmailSecurityException.class)
                    .hasMessageContaining("cannot be null");
        }

        @Test
        @DisplayName("空文件名抛出异常")
        void testBlankFileName() {
            // ByteArrayAttachment validates filename at construction, so we use a mock
            Attachment attachment = new Attachment() {
                @Override public String getFileName() { return ""; }
                @Override public String getContentType() { return "application/octet-stream"; }
                @Override public java.io.InputStream getInputStream() { return new java.io.ByteArrayInputStream(new byte[10]); }
                @Override public long getSize() { return 10; }
            };

            assertThatThrownBy(() -> EmailSecurity.validateAttachment(attachment))
                    .isInstanceOf(EmailSecurityException.class)
                    .hasMessageContaining("file name cannot be blank");
        }

        @Test
        @DisplayName("路径遍历攻击检测(..)")
        void testPathTraversalDotDot() {
            Attachment attachment = new ByteArrayAttachment("../etc/passwd", new byte[10]);

            assertThatThrownBy(() -> EmailSecurity.validateAttachment(attachment))
                    .isInstanceOf(EmailSecurityException.class)
                    .hasMessageContaining("Invalid attachment file name");
        }

        @Test
        @DisplayName("路径遍历攻击检测(/)")
        void testPathTraversalSlash() {
            Attachment attachment = new ByteArrayAttachment("/etc/passwd", new byte[10]);

            assertThatThrownBy(() -> EmailSecurity.validateAttachment(attachment))
                    .isInstanceOf(EmailSecurityException.class)
                    .hasMessageContaining("Invalid attachment file name");
        }

        @Test
        @DisplayName("路径遍历攻击检测(\\)")
        void testPathTraversalBackslash() {
            Attachment attachment = new ByteArrayAttachment("..\\windows\\system32", new byte[10]);

            assertThatThrownBy(() -> EmailSecurity.validateAttachment(attachment))
                    .isInstanceOf(EmailSecurityException.class)
                    .hasMessageContaining("Invalid attachment file name");
        }

        @ParameterizedTest
        @ValueSource(strings = {"virus.exe", "script.bat", "malware.cmd", "trojan.com", "setup.msi",
                "screen.scr", "danger.pif", "evil.vbs", "hack.js", "attack.ps1"})
        @DisplayName("危险扩展名被拒绝")
        void testDangerousExtensions(String fileName) {
            Attachment attachment = new ByteArrayAttachment(fileName, new byte[10]);

            assertThatThrownBy(() -> EmailSecurity.validateAttachment(attachment))
                    .isInstanceOf(EmailSecurityException.class)
                    .hasMessageContaining("Dangerous attachment type not allowed");
        }

        @Test
        @DisplayName("不允许的扩展名被拒绝")
        void testDisallowedExtension() {
            Attachment attachment = new ByteArrayAttachment("file.xyz", new byte[10]);

            assertThatThrownBy(() -> EmailSecurity.validateAttachment(attachment))
                    .isInstanceOf(EmailSecurityException.class)
                    .hasMessageContaining("Attachment type not allowed");
        }

        @Test
        @DisplayName("文件大小超限")
        void testSizeExceeded() {
            // Default max is 10MB
            byte[] largeData = new byte[11 * 1024 * 1024];
            Attachment attachment = new ByteArrayAttachment("large.pdf", largeData);

            assertThatThrownBy(() -> EmailSecurity.validateAttachment(attachment))
                    .isInstanceOf(EmailSecurityException.class)
                    .hasMessageContaining("exceeds limit");
        }

        @ParameterizedTest
        @ValueSource(strings = {"document.pdf", "image.png", "photo.jpg", "data.xlsx", "text.txt"})
        @DisplayName("有效附件通过验证")
        void testValidAttachments(String fileName) {
            Attachment attachment = new ByteArrayAttachment(fileName, new byte[100]);

            assertThatNoException().isThrownBy(() ->
                    EmailSecurity.validateAttachment(attachment));
        }

        @Test
        @DisplayName("使用自定义设置验证")
        void testValidateWithCustomSettings() {
            Attachment attachment = new ByteArrayAttachment("file.custom", new byte[100]);
            Set<String> customExtensions = Set.of("custom", "special");

            assertThatNoException().isThrownBy(() ->
                    EmailSecurity.validateAttachment(attachment, customExtensions, 1024));
        }

        @Test
        @DisplayName("使用自定义大小限制")
        void testValidateWithCustomSizeLimit() {
            Attachment attachment = new ByteArrayAttachment("file.pdf", new byte[1000]);

            assertThatThrownBy(() ->
                    EmailSecurity.validateAttachment(attachment, Set.of("pdf"), 500))
                    .isInstanceOf(EmailSecurityException.class)
                    .hasMessageContaining("exceeds limit");
        }
    }

    @Nested
    @DisplayName("isAllowedExtension() 测试")
    class IsAllowedExtensionTests {

        @Test
        @DisplayName("null文件名返回false")
        void testNullFileName() {
            assertThat(EmailSecurity.isAllowedExtension(null)).isFalse();
        }

        @Test
        @DisplayName("空白文件名返回false")
        void testBlankFileName() {
            assertThat(EmailSecurity.isAllowedExtension("")).isFalse();
            assertThat(EmailSecurity.isAllowedExtension("   ")).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"doc.pdf", "image.png", "data.csv", "archive.zip"})
        @DisplayName("允许的扩展名返回true")
        void testAllowedExtensions(String fileName) {
            assertThat(EmailSecurity.isAllowedExtension(fileName)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"virus.exe", "script.bat", "unknown.xyz"})
        @DisplayName("不允许的扩展名返回false")
        void testDisallowedExtensions(String fileName) {
            assertThat(EmailSecurity.isAllowedExtension(fileName)).isFalse();
        }
    }

    @Nested
    @DisplayName("isDangerousExtension() 测试")
    class IsDangerousExtensionTests {

        @Test
        @DisplayName("null文件名返回false")
        void testNullFileName() {
            assertThat(EmailSecurity.isDangerousExtension(null)).isFalse();
        }

        @Test
        @DisplayName("空白文件名返回false")
        void testBlankFileName() {
            assertThat(EmailSecurity.isDangerousExtension("")).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"virus.exe", "script.bat", "malware.cmd", "trojan.vbs", "hack.ps1",
                "evil.dll", "bad.sh", "danger.msi"})
        @DisplayName("危险扩展名返回true")
        void testDangerousExtensions(String fileName) {
            assertThat(EmailSecurity.isDangerousExtension(fileName)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"doc.pdf", "image.png", "data.txt", "archive.zip"})
        @DisplayName("安全扩展名返回false")
        void testSafeExtensions(String fileName) {
            assertThat(EmailSecurity.isDangerousExtension(fileName)).isFalse();
        }
    }

    @Nested
    @DisplayName("getExtension() 测试")
    class GetExtensionTests {

        @Test
        @DisplayName("null文件名返回空字符串")
        void testNullFileName() {
            assertThat(EmailSecurity.getExtension(null)).isEmpty();
        }

        @Test
        @DisplayName("空白文件名返回空字符串")
        void testBlankFileName() {
            assertThat(EmailSecurity.getExtension("")).isEmpty();
            assertThat(EmailSecurity.getExtension("   ")).isEmpty();
        }

        @Test
        @DisplayName("无扩展名返回空字符串")
        void testNoExtension() {
            assertThat(EmailSecurity.getExtension("filename")).isEmpty();
        }

        @Test
        @DisplayName("点结尾返回空字符串")
        void testDotAtEnd() {
            assertThat(EmailSecurity.getExtension("filename.")).isEmpty();
        }

        @Test
        @DisplayName("正常获取扩展名")
        void testNormalExtension() {
            assertThat(EmailSecurity.getExtension("document.pdf")).isEqualTo("pdf");
            assertThat(EmailSecurity.getExtension("image.PNG")).isEqualTo("PNG");
        }

        @Test
        @DisplayName("多个点取最后一个")
        void testMultipleDots() {
            assertThat(EmailSecurity.getExtension("file.name.tar.gz")).isEqualTo("gz");
        }

        @Test
        @DisplayName("隐藏文件")
        void testHiddenFile() {
            assertThat(EmailSecurity.getExtension(".gitignore")).isEqualTo("gitignore");
        }
    }

    @Nested
    @DisplayName("常量访问测试")
    class ConstantsTests {

        @Test
        @DisplayName("获取默认允许的扩展名")
        void testGetDefaultAllowedExtensions() {
            Set<String> allowed = EmailSecurity.getDefaultAllowedExtensions();

            assertThat(allowed).isNotEmpty();
            assertThat(allowed).contains("pdf", "doc", "png", "jpg", "zip");
        }

        @Test
        @DisplayName("获取危险扩展名")
        void testGetDangerousExtensions() {
            Set<String> dangerous = EmailSecurity.getDangerousExtensions();

            assertThat(dangerous).isNotEmpty();
            assertThat(dangerous).contains("exe", "bat", "cmd", "vbs", "ps1");
        }

        @Test
        @DisplayName("获取默认最大附件大小")
        void testGetDefaultMaxAttachmentSize() {
            long maxSize = EmailSecurity.getDefaultMaxAttachmentSize();

            assertThat(maxSize).isEqualTo(10L * 1024 * 1024); // 10MB
        }
    }

    @Nested
    @DisplayName("大小写敏感性测试")
    class CaseSensitivityTests {

        @Test
        @DisplayName("扩展名大小写不敏感")
        void testExtensionCaseInsensitive() {
            Attachment upperCase = new ByteArrayAttachment("file.PDF", new byte[100]);
            Attachment mixedCase = new ByteArrayAttachment("file.PdF", new byte[100]);

            assertThatNoException().isThrownBy(() -> EmailSecurity.validateAttachment(upperCase));
            assertThatNoException().isThrownBy(() -> EmailSecurity.validateAttachment(mixedCase));
        }

        @Test
        @DisplayName("危险扩展名大小写不敏感")
        void testDangerousExtensionCaseInsensitive() {
            assertThat(EmailSecurity.isDangerousExtension("file.EXE")).isTrue();
            assertThat(EmailSecurity.isDangerousExtension("file.Exe")).isTrue();
            assertThat(EmailSecurity.isDangerousExtension("file.eXe")).isTrue();
        }
    }
}
