package cloud.opencode.base.crypto.symmetric;

/**
 * Padding scheme enumeration for block cipher algorithms.
 * 分组密码算法的填充方案枚举。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>PKCS5, PKCS7, NoPadding definitions - PKCS5、PKCS7、NoPadding 定义</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Padding padding = Padding.PKCS5;
 * String name = padding.paddingName();
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
public enum Padding {
    /**
     * No padding - Plaintext must be multiple of block size.
     * 无填充 - 明文必须是块大小的倍数。
     */
    NO_PADDING("NoPadding"),

    /**
     * PKCS5 padding scheme.
     * PKCS5 填充方案。
     */
    PKCS5("PKCS5Padding"),

    /**
     * PKCS7 padding scheme - Recommended.
     * PKCS7 填充方案 - 推荐使用。
     * Note: JCE uses "PKCS5Padding" which is functionally equivalent to PKCS7 for AES.
     * 注意：JCE 使用 "PKCS5Padding"，对于 AES 来说与 PKCS7 功能等效。
     */
    PKCS7("PKCS5Padding"),

    /**
     * ISO 10126 padding scheme.
     * ISO 10126 填充方案。
     */
    ISO10126("ISO10126Padding");

    private final String value;

    Padding(String value) {
        this.value = value;
    }

    /**
     * Get the JCE padding scheme name.
     * 获取 JCE 填充方案名称。
     *
     * @return JCE padding scheme name / JCE 填充方案名称
     */
    public String getValue() {
        return value;
    }
}
