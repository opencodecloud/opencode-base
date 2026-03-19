package cloud.opencode.base.id.uuid;

import cloud.opencode.base.id.IdGenerator;

import java.util.UUID;

/**
 * UUID v4 Generator
 * UUID v4生成器
 *
 * <p>Generates random UUIDs based on RFC 4122 version 4.
 * Uses secure random number generator.</p>
 * <p>基于RFC 4122版本4生成随机UUID。使用安全随机数生成器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Cryptographically secure - 加密安全</li>
 *   <li>High entropy - 高熵值</li>
 *   <li>No central coordination required - 无需中心协调</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * UuidV4Generator gen = UuidV4Generator.create();
 * UUID uuid = gen.generate();
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>~3M ops/sec single thread - 单线程约3M次/秒</li>
 *   <li>~20M ops/sec with 8 threads - 8线程约20M次/秒</li>
 * </ul>
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
public final class UuidV4Generator implements IdGenerator<UUID> {

    private static final UuidV4Generator INSTANCE = new UuidV4Generator();

    private UuidV4Generator() {
    }

    /**
     * Creates a UUID v4 generator
     * 创建UUID v4生成器
     *
     * @return generator | 生成器
     */
    public static UuidV4Generator create() {
        return INSTANCE;
    }

    @Override
    public UUID generate() {
        return UUID.randomUUID();
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

    @Override
    public String getType() {
        return "UUID-v4";
    }
}
