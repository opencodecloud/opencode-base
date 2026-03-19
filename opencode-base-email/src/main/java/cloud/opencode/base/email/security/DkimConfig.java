package cloud.opencode.base.email.security;

import cloud.opencode.base.email.exception.EmailException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * DKIM Signing Configuration
 * DKIM签名配置
 *
 * <p>Configuration for DKIM (DomainKeys Identified Mail) signing.</p>
 * <p>DKIM（域名密钥识别邮件）签名的配置。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>RSA private key support - RSA私钥支持</li>
 *   <li>Configurable headers to sign - 可配置签名邮件头</li>
 *   <li>Domain and selector configuration - 域名和选择器配置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DkimConfig dkim = DkimConfig.load("example.com", "mail", Path.of("dkim-private.pem"));
 *
 * EmailConfig config = EmailConfig.builder()
 *     .host("smtp.example.com")
 *     .dkim(dkim)
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
public record DkimConfig(
        String domain,
        String selector,
        PrivateKey privateKey,
        Set<String> headersToSign
) {

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    /**
     * Default headers to sign
     * 默认签名的邮件头
     */
    private static final Set<String> DEFAULT_HEADERS_TO_SIGN = Set.of(
            "From", "To", "Subject", "Date", "Message-ID", "MIME-Version", "Content-Type"
    );

    /**
     * Load DKIM configuration from key file
     * 从密钥文件加载DKIM配置
     *
     * @param domain   the domain name | 域名
     * @param selector the DKIM selector | DKIM选择器
     * @param keyPath  the path to private key file (PEM format) | 私钥文件路径（PEM格式）
     * @return the DKIM configuration | DKIM配置
     */
    public static DkimConfig load(String domain, String selector, Path keyPath) {
        return load(domain, selector, keyPath, DEFAULT_HEADERS_TO_SIGN);
    }

    /**
     * Load DKIM configuration with custom headers
     * 使用自定义邮件头加载DKIM配置
     *
     * @param domain        the domain name | 域名
     * @param selector      the DKIM selector | DKIM选择器
     * @param keyPath       the path to private key file | 私钥文件路径
     * @param headersToSign the headers to sign | 要签名的邮件头
     * @return the DKIM configuration | DKIM配置
     */
    public static DkimConfig load(String domain, String selector, Path keyPath, Set<String> headersToSign) {
        PrivateKey key = loadPrivateKey(keyPath);
        return new DkimConfig(domain, selector, key, headersToSign);
    }

    /**
     * Create DKIM configuration with private key
     * 使用私钥创建DKIM配置
     *
     * @param domain     the domain name | 域名
     * @param selector   the DKIM selector | DKIM选择器
     * @param privateKey the private key | 私钥
     * @return the DKIM configuration | DKIM配置
     */
    public static DkimConfig of(String domain, String selector, PrivateKey privateKey) {
        return new DkimConfig(domain, selector, privateKey, DEFAULT_HEADERS_TO_SIGN);
    }

    /**
     * Create DKIM configuration with all parameters
     * 使用所有参数创建DKIM配置
     *
     * @param domain        the domain name | 域名
     * @param selector      the DKIM selector | DKIM选择器
     * @param privateKey    the private key | 私钥
     * @param headersToSign the headers to sign | 要签名的邮件头
     * @return the DKIM configuration | DKIM配置
     */
    public static DkimConfig of(String domain, String selector, PrivateKey privateKey, Set<String> headersToSign) {
        return new DkimConfig(domain, selector, privateKey, headersToSign);
    }

    /**
     * Get default headers to sign
     * 获取默认签名邮件头
     *
     * @return the default headers | 默认邮件头
     */
    public static Set<String> getDefaultHeadersToSign() {
        return DEFAULT_HEADERS_TO_SIGN;
    }

    /**
     * Load private key from PEM file
     * 从PEM文件加载私钥
     */
    private static PrivateKey loadPrivateKey(Path keyPath) {
        try {
            String content = Files.readString(keyPath);

            // Remove PEM headers and whitespace
            String privateKeyPEM = content
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                    .replace("-----END RSA PRIVATE KEY-----", "")
                    ;
            privateKeyPEM = WHITESPACE_PATTERN.matcher(privateKeyPEM).replaceAll("");

            byte[] decoded = Base64.getDecoder().decode(privateKeyPEM);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (IOException e) {
            throw new EmailException("Failed to read DKIM private key: " + keyPath, e);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new EmailException("Failed to parse DKIM private key: " + keyPath, e);
        }
    }
}
