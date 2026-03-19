package cloud.opencode.base.web.crypto;

import cloud.opencode.base.web.Result;
import cloud.opencode.base.web.CommonResultCode;
import cloud.opencode.base.web.internal.TraceIdResolver;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

/**
 * Abstract Result Encryptor
 * 抽象响应加密器
 *
 * <p>Base class for result encryptors providing common functionality.</p>
 * <p>响应加密器基类，提供通用功能。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Base class for result encryptors - 响应加密器基类</li>
 *   <li>JSON serialization/deserialization - JSON序列化/反序列化</li>
 *   <li>Base64 encoding of encrypted data - 加密数据的Base64编码</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * public class MyEncryptor extends AbstractResultEncryptor {
 *     protected byte[] doEncrypt(byte[] data) { ... }
 *     protected byte[] doDecrypt(byte[] data) { ... }
 *     public String getAlgorithm() { return "MY-ALG"; }
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 取决于实现</li>
 *   <li>Null-safe: No (result must not be null) - 否（结果不能为null）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public abstract class AbstractResultEncryptor implements ResultEncryptor {

    @Override
    public <T> EncryptedResult encrypt(Result<T> result) {
        try {
            String json = serializeData(result.data());
            byte[] encrypted = doEncrypt(json.getBytes(StandardCharsets.UTF_8));
            String encoded = Base64.getEncoder().encodeToString(encrypted);
            return EncryptedResult.of(
                result.code(),
                encoded,
                getAlgorithm(),
                result.traceId()
            );
        } catch (Exception e) {
            throw OpenCryptoException.encryptionFailed(e.getMessage());
        }
    }

    @Override
    public <T> Result<T> decrypt(EncryptedResult encrypted, Class<T> dataType) {
        try {
            byte[] decoded = Base64.getDecoder().decode(encrypted.encryptedData());
            byte[] decrypted = doDecrypt(decoded);
            String json = new String(decrypted, StandardCharsets.UTF_8);
            T data = deserializeData(json, dataType);
            return new Result<>(
                encrypted.code(),
                "Success",
                data,
                CommonResultCode.SUCCESS.getCode().equals(encrypted.code()),
                Instant.now(),
                TraceIdResolver.resolve()
            );
        } catch (Exception e) {
            throw OpenCryptoException.decryptionFailed(e.getMessage());
        }
    }

    /**
     * Perform encryption
     * 执行加密
     *
     * @param data the data to encrypt | 要加密的数据
     * @return the encrypted data | 加密后的数据
     * @throws Exception if encryption fails | 如果加密失败
     */
    protected abstract byte[] doEncrypt(byte[] data) throws Exception;

    /**
     * Perform decryption
     * 执行解密
     *
     * @param data the data to decrypt | 要解密的数据
     * @return the decrypted data | 解密后的数据
     * @throws Exception if decryption fails | 如果解密失败
     */
    protected abstract byte[] doDecrypt(byte[] data) throws Exception;

    /**
     * Serialize data to JSON string
     * 将数据序列化为JSON字符串
     *
     * <p>Override this method to use custom serialization.</p>
     * <p>重写此方法以使用自定义序列化。</p>
     *
     * @param data the data to serialize | 要序列化的数据
     * @return the JSON string | JSON字符串
     */
    protected String serializeData(Object data) {
        if (data == null) {
            return "null";
        }
        if (data instanceof String s) {
            return "\"" + escapeJson(s) + "\"";
        }
        if (data instanceof Number || data instanceof Boolean) {
            return data.toString();
        }
        // For complex objects, return toString or implement JSON serialization
        return "\"" + escapeJson(data.toString()) + "\"";
    }

    /**
     * Deserialize JSON string to data
     * 将JSON字符串反序列化为数据
     *
     * <p>Override this method to use custom deserialization.</p>
     * <p>重写此方法以使用自定义反序列化。</p>
     *
     * @param json the JSON string | JSON字符串
     * @param dataType the data type | 数据类型
     * @param <T> the data type | 数据类型
     * @return the deserialized data | 反序列化的数据
     */
    @SuppressWarnings("unchecked")
    protected <T> T deserializeData(String json, Class<T> dataType) {
        if (json == null || "null".equals(json)) {
            return null;
        }
        if (dataType == String.class) {
            // Remove quotes if present
            if (json.startsWith("\"") && json.endsWith("\"")) {
                return (T) unescapeJson(json.substring(1, json.length() - 1));
            }
            return (T) json;
        }
        if (dataType == Integer.class || dataType == int.class) {
            return (T) Integer.valueOf(json);
        }
        if (dataType == Long.class || dataType == long.class) {
            return (T) Long.valueOf(json);
        }
        if (dataType == Double.class || dataType == double.class) {
            return (T) Double.valueOf(json);
        }
        if (dataType == Boolean.class || dataType == boolean.class) {
            return (T) Boolean.valueOf(json);
        }
        // For other types, return as string or implement custom deserialization
        return (T) json;
    }

    /**
     * Escape JSON special characters
     * 转义JSON特殊字符
     *
     * @param s the string to escape | 要转义的字符串
     * @return the escaped string | 转义后的字符串
     */
    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Unescape JSON special characters
     * 反转义JSON特殊字符
     *
     * @param s the string to unescape | 要反转义的字符串
     * @return the unescaped string | 反转义后的字符串
     */
    private String unescapeJson(String s) {
        return s.replace("\\\"", "\"")
                .replace("\\b", "\b")
                .replace("\\f", "\f")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\\", "\\");
    }
}
