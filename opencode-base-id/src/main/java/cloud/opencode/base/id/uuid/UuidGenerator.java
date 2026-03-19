package cloud.opencode.base.id.uuid;

import cloud.opencode.base.id.IdGenerator;

import java.util.UUID;

/**
 * UUID Generator
 * UUID生成器
 *
 * <p>Unified UUID generator supporting both v4 (random) and v7 (time-ordered).</p>
 * <p>统一的UUID生成器，支持v4（随机）和v7（时间有序）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>UUID v4 - Random UUID | 随机UUID</li>
 *   <li>UUID v7 - Time-ordered UUID | 时间有序UUID</li>
 *   <li>Multiple output formats - 多种输出格式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // UUID v4
 * UuidGenerator v4 = UuidGenerator.v4();
 * UUID uuid = v4.generate();
 *
 * // UUID v7
 * UuidGenerator v7 = UuidGenerator.v7();
 * UUID uuidV7 = v7.generate();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
public final class UuidGenerator implements IdGenerator<UUID> {

    private final int version;
    private final IdGenerator<UUID> delegate;

    private UuidGenerator(int version, IdGenerator<UUID> delegate) {
        this.version = version;
        this.delegate = delegate;
    }

    /**
     * Creates a UUID v4 generator
     * 创建UUID v4生成器
     *
     * @return generator | 生成器
     */
    public static UuidGenerator v4() {
        return new UuidGenerator(4, UuidV4Generator.create());
    }

    /**
     * Creates a UUID v7 generator
     * 创建UUID v7生成器
     *
     * @return generator | 生成器
     */
    public static UuidGenerator v7() {
        return new UuidGenerator(7, UuidV7Generator.create());
    }

    @Override
    public UUID generate() {
        return delegate.generate();
    }

    /**
     * Generates a UUID as string
     * 生成UUID字符串
     *
     * @return UUID string | UUID字符串
     */
    public String generateStr() {
        return generate().toString();
    }

    /**
     * Generates a simple UUID (no hyphens)
     * 生成简化UUID（无连字符）
     *
     * @return simple UUID string | 简化UUID字符串
     */
    public String generateSimple() {
        return OpenUuid.toSimpleString(generate());
    }

    /**
     * Gets the UUID version
     * 获取UUID版本
     *
     * @return version number | 版本号
     */
    public int getVersion() {
        return version;
    }

    @Override
    public String getType() {
        return "UUID-v" + version;
    }
}
