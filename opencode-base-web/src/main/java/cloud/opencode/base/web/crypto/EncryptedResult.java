package cloud.opencode.base.web.crypto;

import java.time.Instant;

/**
 * Encrypted Result
 * 加密响应
 *
 * <p>Response with encrypted data payload and signature for tamper protection.</p>
 * <p>带加密数据负载和防篡改签名的响应。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable encrypted result record - 不可变加密结果记录</li>
 *   <li>Algorithm and trace ID metadata - 算法和追踪ID元数据</li>
 *   <li>Timestamp tracking - 时间戳跟踪</li>
 *   <li>HMAC signature for tamper detection - HMAC签名防篡改</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EncryptedResult result = EncryptedResult.of("00000", "Success", encData, "AES-GCM");
 * boolean ok = result.isSuccess();
 * String data = result.encryptedData();
 * String sign = result.sign();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 是（不可变记录）</li>
 *   <li>Null-safe: No (code and data should not be null) - 否（响应码和数据不应为null）</li>
 * </ul>
 * @param code the result code | 响应码
 * @param message the result message | 响应消息
 * @param encryptedData the encrypted data | 加密数据
 * @param algorithm the encryption algorithm | 加密算法
 * @param timestamp the timestamp | 时间戳
 * @param traceId the trace ID | 追踪ID
 * @param sign the HMAC signature covering all fields | 覆盖所有字段的HMAC签名
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public record EncryptedResult(
    String code,
    String message,
    String encryptedData,
    String algorithm,
    Instant timestamp,
    String traceId,
    String sign
) {

    /**
     * Create encrypted result without signature
     * 创建不带签名的加密响应
     *
     * @param code the result code | 响应码
     * @param message the result message | 响应消息
     * @param encryptedData the encrypted data | 加密数据
     * @param algorithm the encryption algorithm | 加密算法
     * @return the encrypted result | 加密响应
     */
    public static EncryptedResult of(String code, String message, String encryptedData, String algorithm) {
        return new EncryptedResult(code, message, encryptedData, algorithm, Instant.now(), null, null);
    }

    /**
     * Create encrypted result with trace ID
     * 创建带追踪ID的加密响应
     *
     * @param code the result code | 响应码
     * @param message the result message | 响应消息
     * @param encryptedData the encrypted data | 加密数据
     * @param algorithm the encryption algorithm | 加密算法
     * @param traceId the trace ID | 追踪ID
     * @return the encrypted result | 加密响应
     */
    public static EncryptedResult of(String code, String message, String encryptedData, String algorithm, String traceId) {
        return new EncryptedResult(code, message, encryptedData, algorithm, Instant.now(), traceId, null);
    }

    /**
     * Create a copy with sign
     * 创建带签名的副本
     *
     * @param sign the HMAC signature | HMAC签名
     * @return the signed encrypted result | 已签名的加密响应
     */
    public EncryptedResult withSign(String sign) {
        return new EncryptedResult(code, message, encryptedData, algorithm, timestamp, traceId, sign);
    }

    /**
     * Build the payload string for HMAC signing
     * 构建用于HMAC签名的负载字符串
     *
     * <p>Concatenates all fields except sign in a deterministic order.</p>
     * <p>按确定性顺序拼接除sign之外的所有字段。</p>
     *
     * @return the payload string | 负载字符串
     */
    public String signPayload() {
        return code + "\n"
            + (message != null ? message : "") + "\n"
            + encryptedData + "\n"
            + algorithm + "\n"
            + timestamp.toString() + "\n"
            + (traceId != null ? traceId : "");
    }

    /**
     * Check if success
     * 检查是否成功
     *
     * @return true if success | 如果成功返回true
     */
    public boolean isSuccess() {
        return "00000".equals(code);
    }
}
