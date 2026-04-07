package cloud.opencode.base.hash.function;

import cloud.opencode.base.hash.HashCode;
import cloud.opencode.base.hash.Hasher;

import java.util.zip.CRC32;
import java.util.zip.CRC32C;
import java.util.zip.Checksum;

/**
 * CRC32 hash function implementation
 * CRC32 哈希函数实现
 *
 * <p>Provides CRC32 and CRC32C (Castagnoli) hash functions using
 * the JDK's built-in implementations.</p>
 * <p>使用 JDK 内置实现提供 CRC32 和 CRC32C (Castagnoli) 哈希函数。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Standard CRC32 - 标准CRC32</li>
 *   <li>CRC32C (Castagnoli) - CRC32C (Castagnoli)</li>
 *   <li>Hardware acceleration when available - 硬件加速（可用时）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Standard CRC32
 * HashCode hash = Crc32HashFunction.crc32().hashBytes(data);
 *
 * // CRC32C (faster on modern CPUs)
 * HashCode hashC = Crc32HashFunction.crc32c().hashBytes(data);
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
 * @since JDK 25, opencode-base-hash V1.0.0
 */
public final class Crc32HashFunction extends AbstractHashFunction {

    private final boolean useCastagnoli;

    private Crc32HashFunction(boolean useCastagnoli) {
        super(32, useCastagnoli ? "crc32c" : "crc32");
        this.useCastagnoli = useCastagnoli;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a standard CRC32 function
     * 创建标准CRC32函数
     *
     * @return hash function | 哈希函数
     */
    public static Crc32HashFunction crc32() {
        return new Crc32HashFunction(false);
    }

    /**
     * Creates a CRC32C (Castagnoli) function
     * 创建CRC32C (Castagnoli) 函数
     *
     * <p>CRC32C is optimized for hardware acceleration on modern CPUs.</p>
     * <p>CRC32C 在现代 CPU 上针对硬件加速进行了优化。</p>
     *
     * @return hash function | 哈希函数
     */
    public static Crc32HashFunction crc32c() {
        return new Crc32HashFunction(true);
    }

    @Override
    public Hasher newHasher() {
        return new Crc32Hasher(useCastagnoli);
    }

    @Override
    public HashCode hashBytes(byte[] input, int offset, int length) {
        java.util.Objects.requireNonNull(input, "input");
        if (offset < 0 || length < 0 || length > input.length - offset) {
            throw cloud.opencode.base.hash.exception.OpenHashException.invalidInput(
                    "offset=" + offset + ", length=" + length + ", array.length=" + input.length);
        }
        Checksum checksum = useCastagnoli ? new CRC32C() : new CRC32();
        checksum.update(input, offset, length);
        return HashCode.fromInt((int) checksum.getValue());
    }

    // ==================== Hasher Implementation | Hasher实现 ====================

    private static class Crc32Hasher extends AbstractHasher {
        private final Checksum checksum;

        Crc32Hasher(boolean useCastagnoli) {
            this.checksum = useCastagnoli ? new CRC32C() : new CRC32();
        }

        @Override
        public Hasher putByte(byte b) {
            checksum.update(b);
            return this;
        }

        @Override
        public Hasher putBytes(byte[] bytes, int offset, int length) {
            checksum.update(bytes, offset, length);
            return this;
        }

        @Override
        protected HashCode doHash() {
            return HashCode.fromInt((int) checksum.getValue());
        }
    }
}
