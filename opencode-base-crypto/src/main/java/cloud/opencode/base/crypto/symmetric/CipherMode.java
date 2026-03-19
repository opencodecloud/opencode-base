package cloud.opencode.base.crypto.symmetric;

/**
 * Cipher mode enumeration for symmetric encryption algorithms.
 * 对称加密算法的加密模式枚举。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>CBC, CTR, GCM, ECB mode definitions - CBC、CTR、GCM、ECB 模式定义</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CipherMode mode = CipherMode.GCM;
 * String name = mode.modeName();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial - 空值安全: 部分</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) - 时间复杂度: O(1)</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public enum CipherMode {
    /**
     * Electronic Codebook mode - Not recommended for most use cases.
     * 电子密码本模式 - 不推荐用于大多数场景。
     * @deprecated ECB mode is insecure as it does not hide data patterns.
     *             Identical plaintext blocks produce identical ciphertext blocks.
     *             Use GCM (recommended), CBC, or CTR mode instead.
     */
    @Deprecated(since = "1.0.0", forRemoval = false)
    ECB,

    /**
     * Cipher Block Chaining mode - Requires IV.
     * 密码块链接模式 - 需要初始化向量。
     */
    CBC,

    /**
     * Counter mode - Requires IV.
     * 计数器模式 - 需要初始化向量。
     */
    CTR,

    /**
     * Galois/Counter Mode - AEAD mode, recommended.
     * 伽罗瓦/计数器模式 - AEAD 模式，推荐使用。
     */
    GCM,

    /**
     * Counter with CBC-MAC mode - AEAD mode.
     * CBC-MAC计数器模式 - AEAD 模式。
     */
    CCM
}
