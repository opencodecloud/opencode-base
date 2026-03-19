package cloud.opencode.base.crypto.codec;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PEM format encoding and decoding utility - Handle PEM encoded keys and certificates
 * PEM 格式编解码工具类 - 处理 PEM 编码的密钥和证书
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>PEM format encoding and decoding - PEM 格式编码和解码</li>
 *   <li>Certificate and key format support - 证书和密钥格式支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String pem = PemCodec.encode("CERTIFICATE", derBytes);
 * byte[] der = PemCodec.decode(pem);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class PemCodec {

    private static final String PEM_BEGIN = "-----BEGIN %s-----";
    private static final String PEM_END = "-----END %s-----";
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final int PEM_LINE_LENGTH = 64;

    private static final String TYPE_PUBLIC_KEY = "PUBLIC KEY";
    private static final String TYPE_PRIVATE_KEY = "PRIVATE KEY";
    private static final String TYPE_CERTIFICATE = "CERTIFICATE";

    private static final Pattern PEM_PATTERN = Pattern.compile(
        "-----BEGIN ([^-]+)-----\\s*([A-Za-z0-9+/=\\s]+)-----END \\1-----",
        Pattern.DOTALL
    );
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");

    private PemCodec() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Encode public key bytes to PEM format
     * 将公钥字节编码为 PEM 格式
     *
     * @param key public key bytes
     * @return PEM formatted string
     * @throws NullPointerException if key is null
     */
    public static String encodePublicKey(byte[] key) {
        return encode(TYPE_PUBLIC_KEY, key);
    }

    /**
     * Decode PEM formatted public key to bytes
     * 将 PEM 格式的公钥解码为字节
     *
     * @param pem PEM formatted public key
     * @return decoded key bytes
     * @throws NullPointerException if pem is null
     * @throws IllegalArgumentException if pem is not valid format
     */
    public static byte[] decodePublicKey(String pem) {
        return decodeAndValidateType(pem, TYPE_PUBLIC_KEY);
    }

    /**
     * Encode private key bytes to PEM format
     * 将私钥字节编码为 PEM 格式
     *
     * @param key private key bytes
     * @return PEM formatted string
     * @throws NullPointerException if key is null
     */
    public static String encodePrivateKey(byte[] key) {
        return encode(TYPE_PRIVATE_KEY, key);
    }

    /**
     * Decode PEM formatted private key to bytes
     * 将 PEM 格式的私钥解码为字节
     *
     * @param pem PEM formatted private key
     * @return decoded key bytes
     * @throws NullPointerException if pem is null
     * @throws IllegalArgumentException if pem is not valid format
     */
    public static byte[] decodePrivateKey(String pem) {
        return decodeAndValidateType(pem, TYPE_PRIVATE_KEY);
    }

    /**
     * Encode certificate bytes to PEM format
     * 将证书字节编码为 PEM 格式
     *
     * @param cert certificate bytes
     * @return PEM formatted string
     * @throws NullPointerException if cert is null
     */
    public static String encodeCertificate(byte[] cert) {
        return encode(TYPE_CERTIFICATE, cert);
    }

    /**
     * Decode PEM formatted certificate to bytes
     * 将 PEM 格式的证书解码为字节
     *
     * @param pem PEM formatted certificate
     * @return decoded certificate bytes
     * @throws NullPointerException if pem is null
     * @throws IllegalArgumentException if pem is not valid format
     */
    public static byte[] decodeCertificate(String pem) {
        return decodeAndValidateType(pem, TYPE_CERTIFICATE);
    }

    /**
     * Encode data to PEM format with custom type
     * 使用自定义类型将数据编码为 PEM 格式
     *
     * @param type PEM type identifier (e.g., "RSA PRIVATE KEY")
     * @param data data bytes to encode
     * @return PEM formatted string
     * @throws NullPointerException if type or data is null
     * @throws IllegalArgumentException if type is empty
     */
    public static String encode(String type, byte[] data) {
        if (type == null) {
            throw new NullPointerException("Type cannot be null");
        }
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        if (type.trim().isEmpty()) {
            throw new IllegalArgumentException("Type cannot be empty");
        }

        String base64 = Base64.getEncoder().encodeToString(data);
        StringBuilder pem = new StringBuilder();

        pem.append(String.format(PEM_BEGIN, type)).append(LINE_SEPARATOR);

        // Split base64 string into lines of specified length
        int offset = 0;
        while (offset < base64.length()) {
            int end = Math.min(offset + PEM_LINE_LENGTH, base64.length());
            pem.append(base64, offset, end).append(LINE_SEPARATOR);
            offset = end;
        }

        pem.append(String.format(PEM_END, type));

        return pem.toString();
    }

    /**
     * Decode PEM formatted string to bytes
     * 将 PEM 格式字符串解码为字节
     *
     * @param pem PEM formatted string
     * @return decoded bytes
     * @throws NullPointerException if pem is null
     * @throws IllegalArgumentException if pem is not valid format
     */
    public static byte[] decode(String pem) {
        if (pem == null) {
            throw new NullPointerException("PEM string cannot be null");
        }

        Matcher matcher = PEM_PATTERN.matcher(pem.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid PEM format");
        }

        String base64Data = WHITESPACE_PATTERN.matcher(matcher.group(2)).replaceAll("");

        try {
            return Base64.getDecoder().decode(base64Data);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 data in PEM", e);
        }
    }

    /**
     * Get PEM type identifier from PEM string
     * 从 PEM 字符串获取类型标识符
     *
     * @param pem PEM formatted string
     * @return PEM type identifier
     * @throws NullPointerException if pem is null
     * @throws IllegalArgumentException if pem is not valid format
     */
    public static String getType(String pem) {
        if (pem == null) {
            throw new NullPointerException("PEM string cannot be null");
        }

        Matcher matcher = PEM_PATTERN.matcher(pem.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid PEM format");
        }

        return matcher.group(1);
    }

    /**
     * Decode PEM and validate it matches expected type
     * 解码 PEM 并验证其类型是否匹配
     *
     * @param pem PEM formatted string
     * @param expectedType expected PEM type
     * @return decoded bytes
     * @throws IllegalArgumentException if type doesn't match
     */
    private static byte[] decodeAndValidateType(String pem, String expectedType) {
        String actualType = getType(pem);
        if (!expectedType.equals(actualType)) {
            throw new IllegalArgumentException(
                String.format("Expected PEM type '%s' but found '%s'", expectedType, actualType)
            );
        }
        return decode(pem);
    }
}
