package cloud.opencode.base.crypto.password;

/**
 * Argon2 algorithm type enumeration - Different variants of Argon2 password hashing
 * Argon2 算法类型枚举 - Argon2 密码哈希的不同变体
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Argon2 variant type definitions - Argon2 变体类型定义</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Argon2Type type = Argon2Type.ARGON2ID;
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
public enum Argon2Type {
    /**
     * Argon2d - Data-dependent mode, resistant to GPU attacks
     * Argon2d - 数据依赖模式，抵抗 GPU 攻击
     */
    ARGON2D(0),

    /**
     * Argon2i - Data-independent mode, resistant to side-channel attacks
     * Argon2i - 数据独立模式，抵抗旁道攻击
     */
    ARGON2I(1),

    /**
     * Argon2id - Hybrid mode combining Argon2d and Argon2i (Recommended)
     * Argon2id - 混合模式，结合 Argon2d 和 Argon2i（推荐）
     */
    ARGON2ID(2);

    private final int typeId;

    Argon2Type(int typeId) {
        this.typeId = typeId;
    }

    /**
     * Get the type identifier used by Bouncy Castle
     * 获取 Bouncy Castle 使用的类型标识符
     *
     * @return the type ID
     */
    public int getTypeId() {
        return typeId;
    }

    /**
     * Get the algorithm name for hash encoding
     * 获取用于哈希编码的算法名称
     *
     * @return the algorithm name
     */
    public String getAlgorithmName() {
        return switch (this) {
            case ARGON2D -> "argon2d";
            case ARGON2I -> "argon2i";
            case ARGON2ID -> "argon2id";
        };
    }
}
