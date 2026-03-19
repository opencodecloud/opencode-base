package cloud.opencode.base.crypto.symmetric;

import cloud.opencode.base.crypto.exception.OpenKeyException;

/**
 * Shared AES key validation utility.
 * AES 密钥共享验证工具。
 *
 * <p>Used by both {@link AesCipher} and {@link AesGcmCipher} to validate key bytes.</p>
 * <p>被 {@link AesCipher} 和 {@link AesGcmCipher} 共同使用以验证密钥字节。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Validates AES key length (128/192/256 bits) - 验证 AES 密钥长度（128/192/256 位）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * AesKeyValidator.validateKeyBytes(keyBytes, "AES-GCM");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial - 空值安全: 部分</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
final class AesKeyValidator {

    private AesKeyValidator() {
    }

    /**
     * Validates that AES key bytes have a valid length (128, 192, or 256 bits).
     * 验证 AES 密钥字节是否具有有效长度（128、192 或 256 位）。
     *
     * @param key the key bytes to validate - 要验证的密钥字节
     * @param algorithmLabel label for error messages (e.g. "AES" or "AES-GCM") - 用于错误消息的算法标签
     * @throws OpenKeyException if the key is null or has invalid length - 如果密钥为空或长度无效
     */
    static void validateKeyBytes(byte[] key, String algorithmLabel) {
        if (key == null || (key.length != 16 && key.length != 24 && key.length != 32)) {
            throw new OpenKeyException(algorithmLabel + " key must be 128, 192, or 256 bits");
        }
    }
}
