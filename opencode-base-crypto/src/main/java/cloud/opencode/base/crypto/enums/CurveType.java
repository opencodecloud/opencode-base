package cloud.opencode.base.crypto.enums;

/**
 * Elliptic curve type enumeration - 椭圆曲线类型枚举
 * 椭圆曲线类型的枚举定义
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Elliptic curve type definitions - 椭圆曲线类型定义</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CurveType curve = CurveType.P_256;
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
public enum CurveType {
    /**
     * NIST P-256 curve (secp256r1, prime256v1)
     */
    P_256("secp256r1", 256),

    /**
     * NIST P-384 curve (secp384r1)
     */
    P_384("secp384r1", 384),

    /**
     * NIST P-521 curve (secp521r1)
     */
    P_521("secp521r1", 521),

    /**
     * secp256k1 curve (used in Bitcoin and Ethereum)
     */
    SECP256K1("secp256k1", 256),

    /**
     * Curve25519 for ECDH (X25519)
     */
    CURVE25519("curve25519", 255),

    /**
     * Curve448 for ECDH (X448)
     */
    CURVE448("curve448", 448),

    /**
     * Ed25519 curve for EdDSA signatures
     */
    ED25519("ed25519", 255),

    /**
     * Ed448 curve for EdDSA signatures
     */
    ED448("ed448", 448),

    /**
     * SM2 curve (Chinese national standard, sm2p256v1)
     */
    SM2("sm2p256v1", 256);

    private final String curveName;
    private final int keySize;

    /**
     * Constructor for CurveType
     *
     * @param curveName the standard curve name
     * @param keySize   the key size in bits
     */
    CurveType(String curveName, int keySize) {
        this.curveName = curveName;
        this.keySize = keySize;
    }

    /**
     * Gets the standard curve name
     * 获取标准曲线名称
     *
     * @return the curve name
     */
    public String getCurveName() {
        return curveName;
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
}
