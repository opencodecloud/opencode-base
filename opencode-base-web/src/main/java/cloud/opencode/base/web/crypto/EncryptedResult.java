package cloud.opencode.base.web.crypto;

import java.time.Instant;

/**
 * Encrypted Result
 * 加密响应
 *
 * <p>Response with encrypted data payload.</p>
 * <p>带加密数据负载的响应。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable encrypted result record - 不可变加密结果记录</li>
 *   <li>Algorithm and trace ID metadata - 算法和追踪ID元数据</li>
 *   <li>Timestamp tracking - 时间戳跟踪</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EncryptedResult result = EncryptedResult.of("00000", encData, "AES-GCM");
 * boolean ok = result.isSuccess();
 * String data = result.encryptedData();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 是（不可变记录）</li>
 *   <li>Null-safe: No (code and data should not be null) - 否（响应码和数据不应为null）</li>
 * </ul>
 * @param code the result code | 响应码
 * @param encryptedData the encrypted data | 加密数据
 * @param algorithm the encryption algorithm | 加密算法
 * @param timestamp the timestamp | 时间戳
 * @param traceId the trace ID | 追踪ID
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public record EncryptedResult(
    String code,
    String encryptedData,
    String algorithm,
    Instant timestamp,
    String traceId
) {

    /**
     * Create encrypted result
     * 创建加密响应
     *
     * @param code the result code | 响应码
     * @param encryptedData the encrypted data | 加密数据
     * @param algorithm the encryption algorithm | 加密算法
     * @return the encrypted result | 加密响应
     */
    public static EncryptedResult of(String code, String encryptedData, String algorithm) {
        return new EncryptedResult(code, encryptedData, algorithm, Instant.now(), null);
    }

    /**
     * Create encrypted result with trace ID
     * 创建带追踪ID的加密响应
     *
     * @param code the result code | 响应码
     * @param encryptedData the encrypted data | 加密数据
     * @param algorithm the encryption algorithm | 加密算法
     * @param traceId the trace ID | 追踪ID
     * @return the encrypted result | 加密响应
     */
    public static EncryptedResult of(String code, String encryptedData, String algorithm, String traceId) {
        return new EncryptedResult(code, encryptedData, algorithm, Instant.now(), traceId);
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
