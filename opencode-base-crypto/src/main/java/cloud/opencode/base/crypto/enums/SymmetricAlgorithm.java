package cloud.opencode.base.crypto.enums;

/**
 * Symmetric encryption algorithm enumeration - 对称加密算法枚举
 * 对称加密算法的枚举定义
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>All supported symmetric algorithm definitions - 所有支持的对称算法定义</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SymmetricAlgorithm alg = SymmetricAlgorithm.AES_GCM_256;
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
public enum SymmetricAlgorithm {
    /**
     * AES-GCM with 128-bit key (Recommended AEAD)
     */
    AES_GCM_128("AES/GCM/NoPadding", 128, true),

    /**
     * AES-GCM with 256-bit key (Recommended AEAD)
     */
    AES_GCM_256("AES/GCM/NoPadding", 256, true),

    /**
     * AES-CBC with 128-bit key and PKCS5 padding
     */
    AES_CBC_128("AES/CBC/PKCS5Padding", 128, false),

    /**
     * AES-CBC with 256-bit key and PKCS5 padding
     */
    AES_CBC_256("AES/CBC/PKCS5Padding", 256, false),

    /**
     * AES-CTR with 128-bit key (Counter mode)
     */
    AES_CTR_128("AES/CTR/NoPadding", 128, false),

    /**
     * AES-CTR with 256-bit key (Counter mode)
     */
    AES_CTR_256("AES/CTR/NoPadding", 256, false),

    /**
     * ChaCha20-Poly1305 with 256-bit key (Recommended AEAD)
     */
    CHACHA20_POLY1305("ChaCha20-Poly1305", 256, true),

    /**
     * SM4-GCM with 128-bit key (Chinese national standard AEAD)
     */
    SM4_GCM("SM4/GCM/NoPadding", 128, true),

    /**
     * SM4-CBC with 128-bit key and PKCS5 padding (Chinese national standard)
     */
    SM4_CBC("SM4/CBC/PKCS5Padding", 128, false);

    private final String transformation;
    private final int keySize;
    private final boolean aead;

    /**
     * Constructor for SymmetricAlgorithm
     *
     * @param transformation the cipher transformation string
     * @param keySize        the key size in bits
     * @param aead           whether this is an AEAD (Authenticated Encryption with Associated Data) algorithm
     */
    SymmetricAlgorithm(String transformation, int keySize, boolean aead) {
        this.transformation = transformation;
        this.keySize = keySize;
        this.aead = aead;
    }

    /**
     * Gets the cipher transformation string
     * 获取密码转换字符串
     *
     * @return the transformation string (e.g., "AES/GCM/NoPadding")
     */
    public String getTransformation() {
        return transformation;
    }

    /**
     * Gets the key size in bits
     * 获取密钥大小（位）
     *
     * @return the key size
     */
    public int getKeySize() {
        return keySize;
    }

    /**
     * Checks if this is an AEAD algorithm
     * 检查是否为 AEAD 算法
     * <p>
     * AEAD algorithms provide both confidentiality and authenticity
     *
     * @return true if this is an AEAD algorithm, false otherwise
     */
    public boolean isAead() {
        return aead;
    }

    /**
     * Checks if this algorithm is recommended for use
     * 检查算法是否推荐使用
     * <p>
     * AEAD algorithms are generally recommended for modern applications
     *
     * @return true if the algorithm is recommended, false otherwise
     */
    public boolean isRecommended() {
        return aead;
    }
}
