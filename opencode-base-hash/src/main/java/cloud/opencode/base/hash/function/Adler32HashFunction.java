package cloud.opencode.base.hash.function;

import cloud.opencode.base.hash.HashCode;
import cloud.opencode.base.hash.Hasher;

import java.util.zip.Adler32;

/**
 * Adler-32 hash function implementation
 * Adler-32 哈希函数实现
 *
 * <p>Wraps {@link java.util.zip.Adler32} to provide a 32-bit checksum hash function.
 * Adler-32 is faster than CRC32 but slightly less reliable for error detection.</p>
 * <p>封装 {@link java.util.zip.Adler32}，提供32位校验和哈希函数。
 * Adler-32 比 CRC32 更快，但在错误检测方面稍微不太可靠。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>32-bit checksum output - 32位校验和输出</li>
 *   <li>Fast computation - 快速计算</li>
 *   <li>Streaming hash via Hasher - 通过Hasher的流式哈希</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * HashCode hash = Adler32HashFunction.adler32().hashBytes(data);
 * int checksum = hash.asInt();
 *
 * // Streaming via Hasher
 * Hasher hasher = Adler32HashFunction.adler32().newHasher();
 * hasher.putUtf8("Hello").putInt(42);
 * HashCode streamHash = hasher.hash();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Not for cryptographic use - 不用于加密用途</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n = input size - O(n), n为输入大小</li>
 *   <li>Space complexity: O(1) - O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.3
 */
public final class Adler32HashFunction extends AbstractHashFunction {

    private Adler32HashFunction() {
        super(32, "adler32");
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates an Adler-32 hash function
     * 创建 Adler-32 哈希函数
     *
     * @return Adler-32 hash function | Adler-32 哈希函数
     */
    public static Adler32HashFunction adler32() {
        return new Adler32HashFunction();
    }

    // ==================== HashFunction Implementation | HashFunction实现 ====================

    @Override
    public Hasher newHasher() {
        return new Adler32Hasher();
    }

    @Override
    public HashCode hashBytes(byte[] input, int offset, int length) {
        java.util.Objects.requireNonNull(input, "input");
        if (offset < 0 || length < 0 || length > input.length - offset) {
            throw cloud.opencode.base.hash.exception.OpenHashException.invalidInput(
                    "offset=" + offset + ", length=" + length + ", array.length=" + input.length);
        }
        Adler32 adler = new Adler32();
        adler.update(input, offset, length);
        return HashCode.fromInt((int) adler.getValue());
    }

    // ==================== Hasher Implementation | Hasher实现 ====================

    /**
     * Adler-32 based Hasher implementation
     * 基于 Adler-32 的 Hasher 实现
     */
    private static final class Adler32Hasher extends AbstractHasher {

        private final Adler32 adler = new Adler32();

        @Override
        public Hasher putByte(byte b) {
            adler.update(b);
            return this;
        }

        @Override
        public Hasher putBytes(byte[] bytes, int offset, int length) {
            adler.update(bytes, offset, length);
            return this;
        }

        @Override
        protected HashCode doHash() {
            return HashCode.fromInt((int) adler.getValue());
        }
    }
}
