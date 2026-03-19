package cloud.opencode.base.email.security;

import cloud.opencode.base.email.exception.EmailException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * DkimConfig 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("DkimConfig 测试")
class DkimConfigTest {

    @TempDir
    Path tempDir;

    private PrivateKey testPrivateKey;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        testPrivateKey = keyPair.getPrivate();
    }

    @Nested
    @DisplayName("of() 工厂方法测试")
    class OfFactoryMethodTests {

        @Test
        @DisplayName("创建基本配置")
        void testOfBasic() {
            DkimConfig config = DkimConfig.of("example.com", "mail", testPrivateKey);

            assertThat(config.domain()).isEqualTo("example.com");
            assertThat(config.selector()).isEqualTo("mail");
            assertThat(config.privateKey()).isEqualTo(testPrivateKey);
            assertThat(config.headersToSign()).isEqualTo(DkimConfig.getDefaultHeadersToSign());
        }

        @Test
        @DisplayName("创建自定义头列表配置")
        void testOfWithCustomHeaders() {
            Set<String> customHeaders = Set.of("From", "To", "Subject");
            DkimConfig config = DkimConfig.of("example.com", "dkim", testPrivateKey, customHeaders);

            assertThat(config.domain()).isEqualTo("example.com");
            assertThat(config.selector()).isEqualTo("dkim");
            assertThat(config.headersToSign()).isEqualTo(customHeaders);
        }
    }

    @Nested
    @DisplayName("load() 测试")
    class LoadTests {

        @Test
        @DisplayName("从PEM文件加载私钥")
        void testLoadFromPemFile() throws Exception {
            Path keyFile = createPemKeyFile();

            DkimConfig config = DkimConfig.load("example.com", "mail", keyFile);

            assertThat(config.domain()).isEqualTo("example.com");
            assertThat(config.selector()).isEqualTo("mail");
            assertThat(config.privateKey()).isNotNull();
            assertThat(config.headersToSign()).isEqualTo(DkimConfig.getDefaultHeadersToSign());
        }

        @Test
        @DisplayName("从PEM文件加载私钥(自定义头)")
        void testLoadWithCustomHeaders() throws Exception {
            Path keyFile = createPemKeyFile();
            Set<String> customHeaders = Set.of("From", "Subject");

            DkimConfig config = DkimConfig.load("example.com", "selector", keyFile, customHeaders);

            assertThat(config.headersToSign()).isEqualTo(customHeaders);
        }

        @Test
        @DisplayName("文件不存在抛出异常")
        void testLoadNonExistentFile() {
            Path nonExistent = tempDir.resolve("nonexistent.pem");

            assertThatThrownBy(() -> DkimConfig.load("example.com", "mail", nonExistent))
                    .isInstanceOf(EmailException.class)
                    .hasMessageContaining("Failed to read DKIM private key");
        }

        @Test
        @DisplayName("无效密钥格式抛出异常")
        void testLoadInvalidKeyFormat() throws Exception {
            Path invalidKey = tempDir.resolve("invalid.pem");
            Files.writeString(invalidKey, "This is not a valid PEM key");

            assertThatThrownBy(() -> DkimConfig.load("example.com", "mail", invalidKey))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("getDefaultHeadersToSign() 测试")
    class DefaultHeadersTests {

        @Test
        @DisplayName("返回默认头列表")
        void testDefaultHeaders() {
            Set<String> defaultHeaders = DkimConfig.getDefaultHeadersToSign();

            assertThat(defaultHeaders).isNotEmpty();
            assertThat(defaultHeaders).contains("From", "To", "Subject", "Date");
        }

        @Test
        @DisplayName("默认头列表是不可变的")
        void testDefaultHeadersImmutable() {
            Set<String> defaultHeaders = DkimConfig.getDefaultHeadersToSign();

            assertThatThrownBy(() -> defaultHeaders.add("Custom-Header"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Record方法测试")
    class RecordMethodTests {

        @Test
        @DisplayName("equals() 和 hashCode()")
        void testEqualsAndHashCode() {
            DkimConfig config1 = DkimConfig.of("example.com", "mail", testPrivateKey);
            DkimConfig config2 = DkimConfig.of("example.com", "mail", testPrivateKey);

            assertThat(config1).isEqualTo(config2);
            assertThat(config1.hashCode()).isEqualTo(config2.hashCode());
        }

        @Test
        @DisplayName("不同配置不相等")
        void testNotEquals() {
            DkimConfig config1 = DkimConfig.of("example.com", "mail", testPrivateKey);
            DkimConfig config2 = DkimConfig.of("other.com", "mail", testPrivateKey);

            assertThat(config1).isNotEqualTo(config2);
        }

        @Test
        @DisplayName("toString() 包含字段")
        void testToString() {
            DkimConfig config = DkimConfig.of("example.com", "mail", testPrivateKey);

            String str = config.toString();
            assertThat(str).contains("example.com");
            assertThat(str).contains("mail");
        }
    }

    private Path createPemKeyFile() throws Exception {
        // Generate a key pair and save as PEM
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        String base64Key = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        String pemContent = "-----BEGIN PRIVATE KEY-----\n" +
                base64Key.replaceAll("(.{64})", "$1\n") +
                "\n-----END PRIVATE KEY-----";

        Path keyFile = tempDir.resolve("private.pem");
        Files.writeString(keyFile, pemContent);
        return keyFile;
    }
}
