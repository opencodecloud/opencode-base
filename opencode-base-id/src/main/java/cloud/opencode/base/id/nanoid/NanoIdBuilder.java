package cloud.opencode.base.id.nanoid;

import java.security.SecureRandom;
import java.util.Random;

/**
 * NanoID Generator Builder
 * NanoID生成器构建器
 *
 * <p>Fluent builder for creating customized NanoIdGenerator instances.</p>
 * <p>用于创建自定义NanoIdGenerator实例的流式构建器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Custom size configuration - 自定义长度配置</li>
 *   <li>Custom alphabet selection - 自定义字母表选择</li>
 *   <li>Random source configuration - 随机源配置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * NanoIdGenerator gen = new NanoIdBuilder()
 *     .size(16)
 *     .alphabet(Alphabet.ALPHANUMERIC)
 *     .secureRandom()
 *     .build();
 *
 * NanoIdGenerator customGen = new NanoIdBuilder()
 *     .size(8)
 *     .alphabet("0123456789")
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (use before build) - 线程安全: 否（在build前使用）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for build() and all setter methods - 时间复杂度: build() 及所有 setter 方法均为 O(1)</li>
 *   <li>Space complexity: O(1) - stores only size, alphabet reference, and random source - 空间复杂度: O(1) - 仅存储长度、字母表引用和随机源</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
public final class NanoIdBuilder {

    private int size = NanoIdGenerator.DEFAULT_SIZE;
    private String alphabet = Alphabet.DEFAULT.getChars();
    private Random random;

    /**
     * Creates a new builder
     * 创建新的构建器
     */
    public NanoIdBuilder() {
    }

    /**
     * Sets the ID length
     * 设置ID长度
     *
     * @param size the length | 长度
     * @return this builder | 此构建器
     */
    public NanoIdBuilder size(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        this.size = size;
        return this;
    }

    /**
     * Sets the alphabet
     * 设置字母表
     *
     * @param alphabet the character set | 字符集
     * @return this builder | 此构建器
     */
    public NanoIdBuilder alphabet(String alphabet) {
        if (alphabet == null || alphabet.isEmpty()) {
            throw new IllegalArgumentException("Alphabet cannot be null or empty");
        }
        if (alphabet.length() > 256) {
            throw new IllegalArgumentException("Alphabet size cannot exceed 256");
        }
        this.alphabet = alphabet;
        return this;
    }

    /**
     * Sets the alphabet using predefined enum
     * 使用预定义枚举设置字母表
     *
     * @param alphabet the predefined alphabet | 预定义字母表
     * @return this builder | 此构建器
     */
    public NanoIdBuilder alphabet(Alphabet alphabet) {
        this.alphabet = alphabet.getChars();
        return this;
    }

    /**
     * Sets the random source
     * 设置随机源
     *
     * @param random the random generator | 随机生成器
     * @return this builder | 此构建器
     */
    public NanoIdBuilder random(Random random) {
        this.random = random;
        return this;
    }

    /**
     * Uses SecureRandom as random source
     * 使用SecureRandom作为随机源
     *
     * @return this builder | 此构建器
     */
    public NanoIdBuilder secureRandom() {
        this.random = new SecureRandom();
        return this;
    }

    /**
     * Builds the NanoIdGenerator
     * 构建NanoIdGenerator
     *
     * @return the generator | 生成器
     */
    public NanoIdGenerator build() {
        Random r = random != null ? random : new SecureRandom();
        return new NanoIdGenerator(size, alphabet, r);
    }
}
